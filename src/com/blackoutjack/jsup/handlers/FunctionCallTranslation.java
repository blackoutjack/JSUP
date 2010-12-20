package com.blackoutjack.jsup.handlers;

import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;
import java.util.*;
import com.blackoutjack.jsup.*;
import com.blackoutjack.jsup.grammars.*;

public abstract class FunctionCallTranslation extends PatchHandler {
	
	protected ArrayList<String> fromMemberChain;
	protected ArrayList<String> toMemberChain;
	protected ArrayList<CommonTree> fromPlaceholders;
	protected ArrayList<CommonTree> toPlaceholders;

	protected ArrayList<String> callMemberChain;
	protected JSUPTree callArguments;

	protected TokenRewriteStream tokenStream;
	protected CommonTree translation; 
	
	int realArgumentCount;
	protected int writePointer;
	
	public FunctionCallTranslation(CommonTree trans) 
		throws MalformedRuleException {
		translation = trans;
		parseRule();
	}

	protected void parseRule() throws MalformedRuleException {
        Tree tfrom = translation.getChild(0);
        Tree tto = translation.getChild(1);

		Tree fMembChain = tfrom.getChild(0);
		Tree tMembChain = tto.getChild(0);

        // Sanity check on rule syntax.
        if (tfrom.getType() != JSUPPatchParser.FUNCTION_CALL
            || tto.getType() != JSUPPatchParser.FUNCTION_CALL) {
            throw new MalformedRuleException("Requires function call form.");
        }

        // Parse the "from" construct.
        int membCountFrom = fMembChain.getChildCount();
		
		// Get the "from" member chain for recognition.
        fromMemberChain = new ArrayList<String>(membCountFrom);
        for (int i=0; i<membCountFrom; i++) {
            CommonTree t = (CommonTree)fMembChain.getChild(i);
            String from = t.getText();
            fromMemberChain.add(from);
        }

        // Gather the placeholders in the "from" argument list.
        Tree fromArgs = tfrom.getChild(1);
        int argCountFrom = fromArgs.getChildCount();
        fromPlaceholders = new ArrayList<CommonTree>(argCountFrom);
        for (int i=0; i<argCountFrom; i++) {
            fromPlaceholders.add((CommonTree)fromArgs.getChild(i));
        }

		// Parse the "to" construct;
        int membCountTo = tMembChain.getChildCount();
		// Get the "to" member chain for recognition.
        toMemberChain = new ArrayList<String>(membCountTo);
        for (int i=0; i<membCountTo; i++) {
            CommonTree t = (CommonTree)tMembChain.getChild(i);
            String to = t.getText();
            toMemberChain.add(to);
        }

        // Gather the placeholders in the "to" argument list.
        Tree toArgs = tto.getChild(1);
        int argCountTo = toArgs.getChildCount();
        toPlaceholders = new ArrayList<CommonTree>(argCountTo);
        for (int i=0; i<argCountTo; i++) {
            toPlaceholders.add((CommonTree)toArgs.getChild(i));
        }
	}

	protected boolean checkMemberChain(JSUPTree ast) {
		
        JSUPTree memberChainTree = (JSUPTree)ast.getChild(0);
		callArguments = (JSUPTree)ast.getChild(1);
		realArgumentCount = callArguments.getChildCount();

        ArrayList<CommonToken> membChainParts = memberChainTree.getTokens();
		callMemberChain = new ArrayList<String>();
		for (int i=0; i<membChainParts.size(); i++) {
			callMemberChain.add(membChainParts.get(i).getText());
        }

		boolean match = true;
		if (callMemberChain.size() != fromMemberChain.size()) {
			match = false;
		} else {
			for (int i=0; i<callMemberChain.size(); i++) {
				String memb = callMemberChain.get(i);
				if (fromMemberChain.size() > i) {
					String fmemb = fromMemberChain.get(i);
					if (fmemb.equals("*") || memb.equals(fmemb)) {
						continue;
					}
				}
				match = false;
			}
		}

		return match;
	}

	protected void clearArguments() {

	}

	protected JSUPTree removeArgument(int index) {
		if (index < callArguments.getChildCount()) {
			JSUPTree child = (JSUPTree)callArguments.deleteChild(index);
			if (child.getType() != JavaScriptParser.Comment) {
				realArgumentCount--;
			}
			return child;
		}
		return null;
	}

	protected void addArgument(String arg, int type) {
		CommonToken tok = new CommonToken(type, arg);
		addArgument(tok);
	}

	protected void addArgument(CommonToken arg) {
		JSUPTree tree = new JSUPTree(arg);
		addArgument(tree);
	}
	
	protected void addArgument(JSUPTree arg) {
		if (arg.getType() != JavaScriptParser.Comment) {
			if (realArgumentCount > 0) {
				tokenStream.insertBefore(writePointer, ",");
			}
			realArgumentCount++;
		}
		if (arg.getChildCount() == 0) {
			tokenStream.insertBefore(writePointer, arg.getText());
		} else { 
			for (int i=arg.getChildCount()-1; i>=0; i--) {
				tokenStream.insertBefore(writePointer, arg.getChild(i).getText());
			}
		}
		callArguments.addChild(arg);
	}


}
