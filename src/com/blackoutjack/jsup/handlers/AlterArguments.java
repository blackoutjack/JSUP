package com.blackoutjack.jsup.handlers;

import org.antlr.runtime.tree.*;
import org.antlr.runtime.*;
import java.util.*;
import com.blackoutjack.jsup.*;
import com.blackoutjack.jsup.grammars.*;

public class AlterArguments extends FunctionCallTranslation {

	public static final String TYPE = "alter_arguments";

	public AlterArguments(CommonTree trans)	throws MalformedRuleException {
   		super(trans);

		// Verify that the member chains are the same.
		if (fromMemberChain.size() != toMemberChain.size()) {
			throw new MalformedRuleException("Function name translation not supported by this rule type.");
		}
		for (int i=0; i<fromMemberChain.size(); i++) {		
			if (!fromMemberChain.get(i).equals(toMemberChain.get(i))) {
				throw new MalformedRuleException("Function name translation not supported by this rule type.");
			}
		}
    }

    public boolean evaluate(JSUPTree ast, TokenRewriteStream trs) {
        if (ast.getType() != JavaScriptParser.CALL) 
			return false;

		tokenStream = trs;

		if (!checkMemberChain(ast))
			return false;

		// Remove all argument from the end, since deleteChild updates
		// indices. Save the argument Trees for use later.
		int numArgs = callArguments.getChildCount();
		JSUPTree[] origArgs = new JSUPTree[numArgs];

		// Remember where original arguments begin and end, excluding parens
		int argsStart = callArguments.getTokenStartIndex() + 1;
		int argsStop = callArguments.getTokenStopIndex();
		writePointer = argsStart; // Assumes insertion from left to right.

		// Disregard the parentheses since those are given.
		for (int i=numArgs-1; i>=0; i--) {
			origArgs[i] = removeArgument(i);
		}
		for (int j=argsStart; j<argsStop; j++) {
			tokenStream.delete(j);
		}

		// Replace arguments according to the argument_map.
		int insertAt = argsStart;
		for (int k=toPlaceholders.size()-1; k>=0; k--) {
			CommonTree toph = toPlaceholders.get(k);
			int type = toph.getType();
			if (type == JSUPPatchParser.Wild) {
				System.err.println("Wild card specified in \"to\" construct.");
				continue;
			} else if (type == JSUPPatchParser.Comment) {
				addArgument(toph.getText(), JavaScriptParser.Comment);
			} else if (type == JSUPPatchParser.Placeholder) {
				boolean found = false;
				for (int j=0; j<fromPlaceholders.size(); j++) {
					CommonTree frph = fromPlaceholders.get(j);
					if (frph.getText().equals(toph.getText())) {
						if (origArgs.length > j) {
							JSUPTree arg = origArgs[j];
							addArgument(arg);
							found = true;
						} else {
							System.err.println("Corresponding argument missing.");
						}
						break;
					}

				}
				if (!found) {
					addArgument(" /* new argument */ ", JavaScriptParser.Comment);
				}
				
			} else if (type == JSUPPatchParser.Identifier) {
				addArgument(toph.getText(), JavaScriptParser.Identifier);
			} else if (type == JSUPPatchParser.String) {
				addArgument(toph.getText(), JavaScriptParser.StringLiteral);
			}

        }
		return true;
    }
}
