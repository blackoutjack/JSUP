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

	// Create a stream reader for local files or over HTTP.
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
			// Read whole file in and create JSUPStream which is a subclass
			// of ANTLRStringStream. toString for ANTLRStringStream is supposed
			// to emit the text, but it was returning the object id.
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = inputReader.readLine()) != null) {
				sb.append(line + "\n");		
			}
           
    		JSUPStream input = new JSUPStream(sb.toString());

		  	// %%% Generalize to other file extensions.
			if (inputFile.endsWith(".html")) {
                try {
                    ScriptStripLexer hlex = new ScriptStripLexer(input);
                    CommonTokenStream hcts = new CommonTokenStream(hlex);
                    ScriptStripParser hpars = new ScriptStripParser(hcts);
                    //hpars.setTreeAdaptor(ssadaptor);
                    ScriptStripParser.document_return result = hpars.document();
                    CommonTree ast = (CommonTree)result.getTree();
                    //ast.setTokenStream(hcts);

					SourceSnippet last = null;
					SourceSnippet snip = null;
					for (int i=0; i<ast.getChildCount(); i++) {
						if (snip != null)
							last = snip;
							
						Tree child = ast.getChild(i);
						snip = new SourceSnippet(child);

						// Hack because I don't know how to get ANTLR
						// to save the HTML.
						SourceSnippet priorHTML = getHTMLSnippet(last, snip, input);	
						snippets.add(priorHTML);					
						snippets.add(snip);
					}
					if (snip != null) {
						SourceSnippet lastHTML = getHTMLSnippet(snip, null, input);
						snippets.add(lastHTML);
					}
	                    
					input = new JSUPStream(snippets.get(0).getText());
                } catch (RecognitionException ex) {
                    ex.printStackTrace();
                }

			// Not HTML, just JavaScript source code
            } else {
				String js_code = input.toString();
				//System.out.println("code: " + js_code);
				SourceSnippet js = new SourceSnippet(js_code, SourceSnippet.JAVASCRIPT);
				snippets.add(js);
			}

			for (int i=0; i<snippets.size(); i++) {
				SourceSnippet snip = snippets.get(i);
				if (snip.getType() == SourceSnippet.HTML)
					continue;

				input = new JSUPStream(snip.getText());		
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
						
						// setTokenStream also saves original text for 
						// each subtree.
						ast.setTokenStream(tokens);
					
						// Apply the rule and get the resulting stream.
						boolean changed = apply(ast, tokens, handler);
						String intermediate = tokens.toString();
						//if (changed) System.out.println(intermediate);

						// Start the process over for the next rule.
						input = new JSUPStream(intermediate);
					} catch (RecognitionException ex) {
						ex.printStackTrace();
					}
				}

				// Save the final product back to the snippet.
				snip.setText(tokens.toString());
			}
			
			for (int i=0; i<snippets.size(); i++) {
				SourceSnippet snip = snippets.get(i);
				System.out.print(snip.toString());
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
			
			// Need an automatic plug-in recognition scheme to replace this.
			try {
				if (ruleType.equals("replace_id")) {           
					handler = new ReplaceID(translation);
				} else if (ruleType.equals("alter_arguments")) {
					handler = new AlterArguments(translation);
				} else if (ruleType.equals("remove_arguments")) {
					handler = new RemoveArguments(translation);
				} else if (ruleType.equals("insert_before")) {
					System.out.println("gurg");
					handler = new InsertBeforeID(translation);
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
	
	protected SourceSnippet getHTMLSnippet(SourceSnippet prev, SourceSnippet next, ANTLRStringStream input) {
		int pos = 0;
		int eof = input.size();
		input.reset();
		
		if (prev != null) {
			while (true) {
				input.consume();
				if (input.getLine() == prev.getLineOffset()) {
					break;
				}
				pos++;
			}
			for (int i=0; i<prev.getCharOffset(); i++) {
				input.consume();
				pos++;		
			}
			for (int i=0; i<prev.toString().length(); i++) {
				input.consume();
				pos++;
			}

		}

		StringBuilder sb = new StringBuilder();
	
		if (next != null) {
			while (true) {
				char in = (char)input.LT(1);
				input.consume();
				sb.append(in);
				if (input.getLine() == next.getLineOffset()) {
					break;
				}
				pos++;
			}
			for (int i=0; i<next.getCharOffset(); i++) {
				char in = (char)input.LT(1);
				input.consume();
				sb.append(in);
				pos++;
			}
		} else {
			for (; pos<input.size()-1; pos++) {
				char in = (char)input.LT(1);
				input.consume();
				sb.append(in);
			}
		}

		SourceSnippet snip = new SourceSnippet(sb.toString(), SourceSnippet.HTML);
		return snip;
	}

}
