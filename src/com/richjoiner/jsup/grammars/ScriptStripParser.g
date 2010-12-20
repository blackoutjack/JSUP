parser grammar ScriptStripParser;

options {
    tokenVocab = ScriptStripLexer;
    output = AST;
}

tokens {
    DOCUMENT;
	SCRIPT;
}

@header {
	package com.richjoiner.jsup;
}

@members {
}

document: script_element* EOF -> ^(DOCUMENT script_element*);

script_element 
	: SCRIPT_OPEN_TAG SCRIPT_BODY_CHAR* SCRIPT_CLOSE_TAG
	-> ^(SCRIPT SCRIPT_OPEN_TAG SCRIPT_BODY_CHAR* SCRIPT_CLOSE_TAG)
	; 

/*script_body : SCRIPT_BODY_CHAR*;*/

