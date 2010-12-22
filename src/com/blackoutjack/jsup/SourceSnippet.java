package com.blackoutjack.jsup;

import com.blackoutjack.jsup.grammars.*;
import org.antlr.runtime.tree.*;

/**
 * Represents a JavaScript snippet and it's position within the original 
 * source file.
 */

/* 
 * Issues:
 *   Only works if <script> open and close tags are on separate lines from
 *   the JavaScript!
 * 
 */

public class SourceSnippet {
	protected String text;
	protected int lineOffset;
	protected int charOffset;
	protected int type;
	public String openTag = "";
	public String closeTag = "";

	public static final int JAVASCRIPT = 0;
	public static final int HTML = 1;

	// Collect relevant information from a ScriptStrip AST.
	public SourceSnippet(Tree tree) {
		int childCount = tree.getChildCount();
		int first_js = 0;
		int last_js = childCount;
		
		if (tree.getType() == ScriptStripParser.SCRIPT) {
			openTag = tree.getChild(0).getText();
			closeTag = tree.getChild(childCount - 1).getText();
			first_js++;
			last_js--;
		} 

		// Get all characters from the JavaScript source code.
		StringBuilder sb = new StringBuilder();
		for (int j=first_js; j<last_js; j++) {
			Tree child = tree.getChild(j);
			sb.append(child.getText());
		}
		setText(sb.toString());

		setOffsets(tree.getLine(), tree.getCharPositionInLine());
		setType(SourceSnippet.JAVASCRIPT);	
	}

	public SourceSnippet(String text, int type) {
		this.text = text;

		if (type == HTML) {
			setType(HTML);
		} else {
			setType(JAVASCRIPT);
		}	
	}

	public String getText() {
		return text;
	}

	public void setOffsets(int line, int chr) {
		lineOffset = line;
		charOffset = chr;
	}

	public int getLineOffset() {
		return lineOffset;
	}

	public int getCharOffset() {
		return charOffset;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String toString() {
		return openTag + text + closeTag;
	}
}
