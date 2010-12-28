package com.blackoutjack.jsup;

import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;
import java.util.*;

/**
 * Provides functionality designed to facilitate consistent manipulations
 * to the input token stream when the tree structure is altered.
 */
public class JSUPTree extends CommonTree {
	/**
	 * Contains all the text from the original input that is internal to this
	 * (sub)tree.
	 */
    protected String text;

	/**
	 * The entire token stream associated with the input.
	 */
    protected TokenRewriteStream tokenStream;

    public JSUPTree(Token t) {
        super(t);
		text = (t == null) ? "" : t.getText();
    }

	/**
	 * Recursively adds copies of the children of the original tree to the 
	 * constructed tree.
	 */
    public JSUPTree(JSUPTree ast) {
        super(ast);
		for (int i=0; i<ast.getChildCount(); i++) {
			this.addChild(new JSUPTree(ast.getChild(i)));
		}
    }

	/**
	 * Sets the token stream and text for the tree and all of it's children 
	 * recursively (and therefore should only be called on the top-level tree).
	 */
    public void setTokenStream(TokenRewriteStream ts) {
        tokenStream = ts;
		for (int i=0; i<getChildCount(); i++) {
			getChild(i).setTokenStream(ts);
		}
		if (ts != null) {
			List toks = ts.getTokens(getTokenStartIndex(),getTokenStopIndex());
			StringBuilder sb = new StringBuilder();
			if (toks != null) {
				for (int i=0; i<toks.size(); i++) {
					CommonToken tok = (CommonToken)toks.get(i);
					sb.append(tok.getText());
				}
			}
			text = sb.toString();
		}
    }

	public TokenRewriteStream getTokenStream() {
		return tokenStream;
	}

	public String getText() {
		return text;
	}

	/*
    public void print() {
		if (getChildCount() == 0) {
			System.out.print(text);
		} else {
			for (int i=0; i<getChildCount(); i++) {
				JSUPTree child = getChild(i);
				child.print();
			}
		}
    }
	*/

	public JSUPTree getChild(int index) {
		CommonTree child = (CommonTree)super.getChild(index);
		if (child instanceof CommonErrorNode) {
			System.err.println(((CommonErrorNode)child).trappedException.getMessage());
			return null;
		}

		
		return (JSUPTree)child;
	}

	public ArrayList<CommonToken> getTokens() {
		ArrayList<CommonToken> toks = new ArrayList<CommonToken>();
		if (getChildCount() > 0) {
			for (int i=0; i<getChildCount(); i++) {
				JSUPTree child = (JSUPTree)getChild(i);
				toks.addAll(child.getTokens());
			}
		} else {
			toks.add((CommonToken)getToken());
		}
		return toks;
	}

	public ArrayList<CommonToken> getStreamTokens() {
		if (tokenStream == null) {
			return getTokens();
		}
		List streamTokens = tokenStream.getTokens();
		ArrayList<CommonToken> toks = new ArrayList<CommonToken>();
		for (int i=getTokenStartIndex(); i<=getTokenStopIndex(); i++) {
			toks.add((CommonToken)streamTokens.get(i));
		}
		return toks;
	}

	public JSUPTree getParent() {
		return (JSUPTree)parent;
	}

	public JSUPTree deleteChild(int index) {
		JSUPTree child = (JSUPTree)super.deleteChild(index);
		return child;
	}

    protected void printTree(CommonTree t) {
        if (t == null) return;
        
        int treeStart = t.getTokenStartIndex();
        int treeStop = t.getTokenStopIndex() + 1;

       
        for (int i=treeStart; i<treeStop; i++) {
            if (t.getChildCount() == 0) {
                System.out.print(tokenStream.get(i).getText());
            } else {
                boolean wasChild = false;
                for (int j=0; j<t.getChildCount(); j++) {
                    
                    CommonTree child = (CommonTree)t.getChild(j);
                    if (i == child.getTokenStartIndex()) {
                        printTree(child);
                        i = child.getTokenStopIndex();
                        wasChild = true;
                        break;
                    }
                }

                // Print any hidden tokens that may be lurking
                if (!wasChild) {
                    Token tok = tokenStream.get(i);
                    System.out.print(tok.getText());
                }
            }
        }
        
    }
}
