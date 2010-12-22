package com.blackoutjack.jsup;

import org.antlr.runtime.*;

class JSUPStream extends ANTLRStringStream {

	public JSUPStream(String input) {
		super(input);
	}


	public String toString() {
		System.out.println("heyho");
		return new String(data);
	}
}
