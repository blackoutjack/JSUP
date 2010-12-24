package com.blackoutjack.jsup;

import org.antlr.runtime.*;

class JSUPStream extends ANTLRStringStream {

	public JSUPStream(String input) {
		super(input);
	}


	public String toString() {
		return new String(data);
	}
}
