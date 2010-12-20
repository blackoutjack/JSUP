package com.blackoutjack.jsup;

/**
 * Represents a JavaScript snippet and it's position within the original 
 * source file.
 */

public class SourceSnippet {
	protected String text;
	protected int lineOffset;
	protected int charOffset;
	protected int type;
	protected int lines;
	public String openTag;
	public String closeTag;

	public static final int JAVASCRIPT = 0;
	public static final int HTML = 1;

	public String getText() {
		return text;
	}

	public void setOffsets(int line, int chr) {
		lineOffset = line;
		charOffset = chr;
		lines = text.split("\n").length;
	}

	public int getLineOffset() {
		return lineOffset;
	}

	public int getCharOffset() {
		return charOffset;
	}

	public int getLines() {
		return lines;

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
}
