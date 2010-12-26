grammar JSUPPatch;

options
{
    output=AST;
}

tokens
{
    RULE;
    TRANSLATION;
    PLACEHOLDERS;
    FUNCTION_CALL;
	MEMBERCHAIN;
}

@header {
	package com.blackoutjack.jsup.grammars;
}

@lexer::header {
	package com.blackoutjack.jsup.grammars;
}

ruleList
    : LT!* (rule LT!*)*
    ;

rule
    : Keyword WS* translation
    -> ^(RULE Keyword translation)
    ;

translation
    : translationElement WS* translationElement
    -> ^(TRANSLATION translationElement translationElement)
    ;

translationElement
    : functionCall
    | memberChain
    ;

functionCall
    : memberChain placeholderList
    -> ^(FUNCTION_CALL memberChain placeholderList)
    ;

memberChain
	: (singleIdentifier '.')* singleIdentifier
	-> ^(MEMBERCHAIN singleIdentifier*)
	;

placeholderList
    : '(' singleIdentifier? (',' singleIdentifier)* ')' 
    -> ^(PLACEHOLDERS singleIdentifier*) 
    ;

singleIdentifier
    : Identifier
	| Placeholder
	| JSComment
	| Wild
	| String
    ;

JSComment
	: '/*' (options {greedy=false;} :~LT)* '*/'
	;

Comment
    : '#' (~LT)*
    { $channel=HIDDEN; }
    ;

Keyword
    : ('replace_id' | 'alter_arguments' | 'remove_arguments' | 'insert_before')
    ;

Identifier
    : ('a'..'z' | 'A'..'Z' | '_' | '0'..'9' | '$')+
    ;

String
	: '\'' (options {greedy=false;} :~LT)* '\''
	| '"' (options {greedy=false;} :~LT)* '"'
	;


Placeholder
    : '%' (('0'..'9')+ | '%')
    ;

Wild
	: '*'
	;

LT
    : '\n'      // Line feed. 
    | '\r'      // Carriage return. 
    | '\u2028'  // Line separator. 
    | '\u2029'  // Paragraph separator. 
    ; 
 
WS // Tab, vertical tab, form feed, space, non-breaking space and any other unicode "space separator". 
    : ('\t' | '\v' | '\f' | ' ' | '\u00A0') 
    ;

