package com.blackoutjack.jsup;

/** 
 * <p>JSUP is an extendable framework for refactoring and annotating JavaScript
 * source code using semantic patches. It is built on top of and requires 
 * the ANTLR parser generator.</p>
 * 
 *
 */

import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;
//import org.antlr.runtime.debug.*;
import java.io.*;
import java.util.ArrayList;
import java.net.URL;
import com.blackoutjack.jsup.handlers.*;
import com.blackoutjack.jsup.grammars.*;

public class JSUP {
	public final static String patchHandlerDirectory = "handlers";

    protected BufferedReader inputReader;
    protected BufferedReader patchReader;
	protected String inputFile;
	protected String patchFile;

	protected ArrayList<PatchHandler> handlers;
	protected ArrayList<SourceSnippet> snippets;

    public static void showUsage() {
        System.err.println("usage: java JSUP <input_file> <patch_file>");
    }
    
    protected TreeAdaptor jsadaptor = new CommonTreeAdaptor() {
        public Object create(Token payload) {
            return new JSUPTree(payload);
        }
    };

    //protected TreeAdaptor ssadaptor = new CommonTreeAdaptor() {
    //    public Object create(Token payload) {
    //        return new ScriptStripAST(payload);
    //    }
    //};

	public static void main(String[] args) throws Exception {

        if (args.length < 2) {
            showUsage();
            System.exit(1);
        }

        String input = args[0];
        String patches = args[1];

        JSUP jsup = new JSUP(input, patches);
        jsup.process();
    }

    public JSUP(String input, String patches) {
		inputFile = input;
		patchFile = patches;
		inputReader = initializeReader(input);
		patchReader = initializeReader(patches);
		snippets = new ArrayList<SourceSnippet>();
    }

	protected BufferedReader initializeReader(String filename) {
		try {
			InputStream in;
			if (filename.matches("^https?://.+$")) {
				in = new URL(filename).openStream();
			} else {
				in = new FileInputStream(filename);
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			return reader;
		} catch (IOException ex) {
			System.err.println(ex.getMessage());
			System.exit(1);
		}
		return null;
	}

    public void process() {
        readPatches();
	
        try {
    		CharStream input = new ANTLRReaderStream(inputReader);
           
		   
			if (inputFile.endsWith(".html")) {
                try {
                    ScriptStripLexer hlex = new ScriptStripLexer(input);
                    CommonTokenStream hcts = new CommonTokenStream(hlex);
                    ScriptStripParser hpars = new ScriptStripParser(hcts);
                    //hpars.setTreeAdaptor(ssadaptor);
                    ScriptStripParser.document_return result = hpars.document();
                    CommonTree ast = (CommonTree)result.getTree();
                    //ast.setTokenStream(hcts);

					for (int i=0; i<ast.getChildCount(); i++) {
						SourceSnippet snip = new SourceSnippet();
						Tree child = ast.getChild(i);

						StringBuilder sb = new StringBuilder();
						for (int j=1; j<child.getChildCount()-1; j++) {
							Tree grandchild = child.getChild(j);
							sb.append(grandchild.getText());
						}
						snip.setText(sb.toString());
						snip.openTag = child.getChild(0).getText();
						snip.closeTag = child.getChild(child.getChildCount()-1).getText();
						if (sb.toString().trim().equals(""))
							continue;
						//System.out.println("SNIPPET: " + sb.toString());
						snip.setOffsets(child.getLine(), child.getCharPositionInLine());
						snip.setType(SourceSnippet.JAVASCRIPT);
						snippets.add(snip);
						
					}
					if (snippets.size() > 0) {
	                    input = new ANTLRStringStream(snippets.get(0).getText());
					}
                } catch (RecognitionException ex) {
                    ex.printStackTrace();
                }
            }
			// Need the stream outside the loop to print out later.
			TokenRewriteStream tokens = null;

			// Apply one rule, rerun through the parsing process and repeat.
			for (PatchHandler handler : handlers) {
				
            	JavaScriptLexer lex = new JavaScriptLexer(input);
	            tokens = new TokenRewriteStream(lex);
    	        JavaScriptParser parser = new JavaScriptParser(tokens);
        	    parser.setTreeAdaptor(jsadaptor);

	            try {
    	            JavaScriptParser.program_return prog = parser.program();
        	        JSUPTree ast = (JSUPTree)prog.getTree();
					 // Also saves original text for each subtree.
					ast.setTokenStream(tokens);
				
					// Apply the rule and get the resulting stream.
                	boolean changed = apply(ast, tokens, handler);
	                String intermediate = tokens.toString();
					//if (changed) System.out.println(intermediate);

					// Start the process over for the next rule.
					input = new ANTLRStringStream(intermediate);
    	        } catch (RecognitionException ex) {
        	        ex.printStackTrace();
				}
           	}
			// Output the final product.
			if (tokens != null && snippets.size() == 0) {
				System.out.print(tokens.toString());
			}

			if (snippets.size() > 0) {
				snippets.get(0).setText(tokens.toString());
				for (int i=1; i<snippets.size(); i++) {
					input = new ANTLRStringStream(snippets.get(i).getText());
					for (PatchHandler handler : handlers) {
						
						JavaScriptLexer lex = new JavaScriptLexer(input);
						tokens = new TokenRewriteStream(lex);
						JavaScriptParser parser = new JavaScriptParser(tokens);
						parser.setTreeAdaptor(jsadaptor);

						try {
							JavaScriptParser.program_return prog = parser.program();
							JSUPTree ast = (JSUPTree)prog.getTree();
							 // Also saves original text for each subtree.
							ast.setTokenStream(tokens);
						
							// Apply the rule and get the resulting stream.
							boolean changed = apply(ast, tokens, handler);
							String intermediate = tokens.toString();
							
							if (changed) {
								//System.out.println(intermediate);
							}

							// Start the process over for the next rule.
							input = new ANTLRStringStream(intermediate);
						} catch (RecognitionException ex) {
							ex.printStackTrace();
						}
					}
					// Output the final product.
					if (tokens != null) {
						snippets.get(i).setText(tokens.toString());
						//System.out.print(tokens.toString());
					}
				}
			}

			if (snippets.size() > 0) {
				// Read in file
				int linePointer = 0;
				int charPointer = 0;
				int snippetIndex = 0;
				SourceSnippet snip = snippets.get(0);
				
				String line;
				BufferedReader newReader = initializeReader(inputFile);
				while ((line = newReader.readLine()) != null) {
					linePointer++;

					if (snip != null && linePointer == snip.getLineOffset()) {
						//String[] newLines = snip.getText().split("\n");
						// Consume original text.
						for (int i=0; i<snip.getLines(); i++) {
							line = newReader.readLine();
							linePointer++;
						}
						System.out.print(snip.openTag);
						System.out.print(snip.getText());
						System.out.println(snip.closeTag);

						snippetIndex++;
						if (snippetIndex == snippets.size()) {
							snip = null;
						} else {
							snip = snippets.get(snippetIndex);
						}
					} else {
						System.out.println(line);
					}
				}
			}

		} catch (IOException ex) {
			ex.printStackTrace();
		}

		
	}

	public boolean apply(JSUPTree ast, TokenRewriteStream trs, PatchHandler ph) {
		boolean changed = ph.evaluate(ast, trs);
		for (int i=0; i<ast.getChildCount(); i++) {
			boolean childChanged = apply(ast.getChild(i), trs, ph);
			if (childChanged) changed = true;
		}
		return changed;
	}

    protected void readPatches() { 
		CommonTree ast;
		handlers= new ArrayList<PatchHandler>();
        try {
    		CharStream input = new ANTLRReaderStream(patchReader);
            JSUPPatchLexer jlex = new JSUPPatchLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(jlex);
                   
            JSUPPatchParser jparser = new JSUPPatchParser(tokens);

            try {
                JSUPPatchParser.ruleList_return rules = jparser.ruleList();
                ast = (CommonTree)rules.getTree();
				loadRules(ast);
            } catch (RecognitionException ex) {
                ex.printStackTrace();
				return;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
			return;
        }
	}

	protected void loadRules(CommonTree ast) {
        if (ast.getType() == JSUPPatchParser.RULE) {
            String ruleType = ast.getChild(0).getText();
            CommonTree translation = (CommonTree)ast.getChild(1);

			// Load all PatchHandlers from the handlers subdirectory.
            PatchHandler handler = null;
			File phdir = new File(patchHandlerDirectory);
			if (phdir.isDirectory()) {
					
			}
			

			try {
				if (ruleType.equals("replace_id")) {           
					handler = new ReplaceID(translation);
				} else if (ruleType.equals("alter_arguments")) {
					handler = new AlterArguments(translation);
				} else if (ruleType.equals("remove_arguments")) {
					handler = new RemoveArguments(translation);
				}
			} catch (MalformedRuleException ex) {
				System.out.println(ex.getMessage());
				return;
			}

            if (handler == null) {
                System.err.println("Unknown rule type: " + ruleType);
            } else {
                handlers.add(handler);
            }
        } else {
            for (int i=0; i<ast.getChildCount(); i++) {
                loadRules((CommonTree)ast.getChild(i));
            }
        }
    }
}
