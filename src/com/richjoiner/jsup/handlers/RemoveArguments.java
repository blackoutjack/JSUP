package com.richjoiner.jsup.handlers;

import org.antlr.runtime.tree.*;
import org.antlr.runtime.*;
import java.util.*;
import com.richjoiner.jsup.*;

public class RemoveArguments extends FunctionCallTranslation {
	protected int[] argumentsToRemove;

	public RemoveArguments(CommonTree trans) throws MalformedRuleException {
		super(trans);
		
		// %%% Member chains must match in this rule type.

		// Take stock of the "from" and "to" argument tokens.
		int removing = fromPlaceholders.size() - toPlaceholders.size();
		if (removing < 1) {
			throw new MalformedRuleException("Too many \"to\" arguments.");
		}
		argumentsToRemove = new int[removing];

		// Loop through the "from" arguments to see if they're present in "to".
		int removed = 0;
		for (int i=0; i<fromPlaceholders.size(); i++) {
			String txt = fromPlaceholders.get(i).getText();
			boolean found = false;
			for (int j=0; j<toPlaceholders.size(); j++) {
				if (txt.equals(toPlaceholders.get(j).getText())) {
					found = true;
					break;
				}
			}
			if (!found) {
				if (removed >= removing) {
					throw new MalformedRuleException("Too many arguments.");
				}
				argumentsToRemove[removed] = i;
				removed++;
			}
		}
		if (removed != removing) {
			throw new MalformedRuleException("");
		}

	}

	public boolean evaluate(JSUPTree ast, TokenRewriteStream trs) {
		if (ast.getType() != JavaScriptParser.CALL)
			return false;

		tokenStream = trs;
		
		if (!checkMemberChain(ast))
			return false;

		int childCount = ast.getChildCount();

		JSUPTree memberChainTree = (JSUPTree)ast.getChild(0);
		if (childCount - 1 != toMemberChain.size())
			return false;

		CommonToken[] callMemberChain = new CommonToken[childCount - 1];
        JSUPTree args = null;
		int chainCount = 0;
        for (int i=0; i<childCount; i++) {
            JSUPTree child = (JSUPTree)ast.getChild(i);
            if (child.getType() == JavaScriptParser.ARGUMENTS) {
                args = child;
                break;
            } else if (child.getType() == JavaScriptParser.Identifier) {
                callMemberChain[chainCount] = (CommonToken)child.getToken();
				chainCount++;
            } else if (child.getType() == JavaScriptParser.PROPERTY) {
                JSUPTree prop = (JSUPTree)child.getChild(0);
                callMemberChain[chainCount] = (CommonToken)prop.getToken();
				chainCount++;
            }
        }
		if (chainCount != toMemberChain.size() || args == null)
			return false;

		for (int i=0; i<toMemberChain.size(); i++) {
			if (!toMemberChain.get(i).equals(callMemberChain[i].getText())) {
				return false;
			}
		}

		// We have a match.

		int insertAfter = args.getTokenStartIndex();
		
		// Alter tree to have only the remaining children
		for (int i=0; i<argumentsToRemove.length; i++) {
			int argIndex = argumentsToRemove[i];			
			JSUPTree child = args.deleteChild(argIndex);
		}

		// Remove all arguments from the stream to avoid dangling commas.
		ArrayList<CommonToken> allArgTokens = args.getStreamTokens();
		for (int i=1; i<allArgTokens.size()-1; i++) {
			tokenStream.delete(allArgTokens.get(i));
		}

		// Add remaining children back to stream.
		for (int i=0; i<args.getChildCount(); i++) {
			JSUPTree child = args.getChild(i);
			if (i > 0) {
				CommonToken comma = new CommonToken(JavaScriptLexer.T__44, ",");
				tokenStream.insertAfter(insertAfter, comma.getText());
				insertAfter++;
			}
			ArrayList<CommonToken> replToks = child.getStreamTokens();
			for (int j=0; j<replToks.size(); j++) {
				CommonToken replTok = replToks.get(j);
				tokenStream.insertAfter(insertAfter, replTok.getText());
				insertAfter++;
			}
		}
		
		return true;
	}
}




