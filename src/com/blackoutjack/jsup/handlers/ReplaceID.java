package com.blackoutjack.jsup.handlers;

import org.antlr.runtime.tree.*;
import org.antlr.runtime.*;
import java.util.*;
import com.blackoutjack.jsup.*;
import com.blackoutjack.jsup.grammars.*;

public class ReplaceID extends PatchHandler {
	public static final String TYPE = "replace_id";	

    protected ArrayList<String> fromMemberChain;
    protected ArrayList<String> toMemberChain;

	protected ArrayList<String> sourceMemberChain;
   
    public ReplaceID(CommonTree translation) {
		parseRule(translation);
    }

    public boolean evaluate(JSUPTree ast, TokenRewriteStream trs) {
		if (ast.getType() != JavaScriptParser.MEMBERCHAIN && ast.getType() != JavaScriptParser.Identifier) {
            return false;
        }

        if (checkMemberChain(ast)) {
			int insertAt = ast.getTokenStartIndex();
			for (int i=insertAt; i<=ast.getTokenStopIndex(); i++) {
				trs.delete(i);
			}
			for (int i=toMemberChain.size()-1; i>=0; i--) {
				String toText = toMemberChain.get(i);
				if (toText.equals("*")) {
					trs.insertBefore(insertAt, sourceMemberChain.get(i));
				} else {
					trs.insertBefore(insertAt, toMemberChain.get(i));
				}

				if (i > 0) {
					trs.insertBefore(insertAt, ".");
				}
			}
            return true;
        }

        return false;
    }

    protected boolean checkMemberChain(JSUPTree memberChainTree) {
        ArrayList<CommonToken> callMemberChain = memberChainTree.getTokens();

        boolean match = true;
        if (callMemberChain.size() != fromMemberChain.size()) {
            match = false;
        } else {
			sourceMemberChain = new ArrayList<String>();
            for (int i=0; i<callMemberChain.size(); i++) {
                String memb = callMemberChain.get(i).getText();
				sourceMemberChain.add(memb);
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

    protected void parseRule(CommonTree translation) { 
        Tree tfrom = translation.getChild(0); 
        Tree tto = translation.getChild(1); 
 
        int childCountFrom = tfrom.getChildCount(); 
         
        // Get the "from" member chain for recognition. 
        fromMemberChain = new ArrayList<String>(childCountFrom - 1); 
        for (int i=0; i<childCountFrom; i++) { 
            CommonTree t = (CommonTree)tfrom.getChild(i);
            String from = t.getText(); 
            fromMemberChain.add(from);
        } 
 
        // Parse the "to" construct; 
        int childCountTo = tto.getChildCount(); 
        // Get the "to" member chain for recognition. 
        toMemberChain = new ArrayList<String>(childCountTo - 1); 
        for (int i=0; i<childCountTo; i++) { 
            CommonTree t = (CommonTree)tto.getChild(i); 
            String to = t.getText(); 
            toMemberChain.add(to); 
        } 
    } 
}
