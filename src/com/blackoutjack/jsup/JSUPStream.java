package com.blackoutjack.jsup;

import org.antlr.runtime.*;

class JSUPStream extends ANTLRStringStream {

	public JSUPStream(String input) {
		super(input);
	}

	// ANTLRStringStream is documented to have this behavior, but it wasn't
	// working as of 2010-12-22.
	public String toString() {
		return new String(data);
	}
}
