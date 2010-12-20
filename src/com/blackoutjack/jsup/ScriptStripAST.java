package com.blackoutjack.jsup;

import java.util.*;
import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;

class ScriptStripAST extends CommonTree {

    protected CommonTokenStream tokenStream;
	protected String text;

    public ScriptStripAST(Token t) {
        super(t);
    }
    
    public ScriptStripAST(CommonTree ast) {
        super(ast);
        if (ast.getChildren() != null)
            this.addChildren(ast.getChildren());	
    }

    public void setTokenStream(CommonTokenStream ts) {
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

	public ScriptStripAST getChild(int index) {
		return (ScriptStripAST)super.getChild(index);
	}

	public String getOldText() {
		return super.getText();
	}

	public String getChildText() {
		StringBuilder sb = new StringBuilder();
		if (getToken() == null) {
			sb.append(getToken().getText());
		}
		for (int i=0; i<getChildCount(); i++) {
			sb.append(getChild(i).getText());
		}
		return sb.toString();
	}

	public String getText() {
		return text;
	}

}

