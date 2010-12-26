package com.blackoutjack.jsup.handlers;

import org.antlr.runtime.tree.*;
import org.antlr.runtime.*;
import java.util.*;
import com.blackoutjack.jsup.*;
import com.blackoutjack.jsup.grammars.*;

public class InsertBeforeID extends PatchHandler {
	public static final String TYPE = "insert_before";	

    protected ArrayList<String> recogMemberChain;
	protected ArrayList<String> sourceMemberChain;
	protected String toInsert;
   
    public InsertBeforeID(CommonTree translation) {
		parseRule(translation);
    }

    public boolean evaluate(JSUPTree ast, TokenRewriteStream trs) {
		if (ast.getType() != JavaScriptParser.MEMBERCHAIN && ast.getType() != JavaScriptParser.Identifier) {
            return false;
        }

        if (checkMemberChain(ast)) {
			int insertAt = ast.getTokenStartIndex();
			
			trs.insertBefore(insertAt, toInsert);
            return true;
        }

        return false;
    }

    protected boolean checkMemberChain(JSUPTree memberChainTree) {
        ArrayList<CommonToken> memberChain = memberChainTree.getTokens();

        boolean match = true;
        if (memberChain.size() != recogMemberChain.size()) {
            match = false;
        } else {
			sourceMemberChain = new ArrayList<String>();
            for (int i=0; i<memberChain.size(); i++) {
                String memb = memberChain.get(i).getText();
				sourceMemberChain.add(memb);
                if (recogMemberChain.size() > i) {
                    String rmemb = recogMemberChain.get(i);
                    if (rmemb.equals("*") || rmemb.equals(memb)) {
                        continue;
                    }
                }
                match = false;
            }
        }

        return match;
    }

    protected void parseRule(CommonTree translation) { 
        Tree trecog = translation.getChild(0); 
        Tree tinsert = translation.getChild(1); 
 
        int childCountRecog = trecog.getChildCount(); 
	
        // Get the recognition member chain. 
        recogMemberChain = new ArrayList<String>(childCountRecog - 1); 
        for (int i=0; i<childCountRecog; i++) { 
            CommonTree t = (CommonTree)trecog.getChild(i);
            String recog = t.getText(); 
            recogMemberChain.add(recog);
        } 
 
		// Get the comment to insert.
		if (tinsert.getType() != JSUPPatchLexer.MEMBERCHAIN) {
			System.err.println("Only comments can be inserted.");
			return;
		}		
		if (tinsert.getChildCount() != 1) {
			System.err.println("Only single comments can be inserted.");
		}

		Tree toInsertToken = tinsert.getChild(0);
		if (toInsertToken.getType() != JSUPPatchLexer.JSComment) {
			System.err.println("Only comments can be inserted.");
			return;
		}

		toInsert = toInsertToken.getText();
    } 
}
