lexer grammar ScriptStripLexer;

options {
    filter=true;
}

@header {
	package com.richjoiner.jsup;
}

@members {
    boolean scriptMode = false;
}
/*
SCRIPT_ELEMENT : SCRIPT_OPEN_TAG SCRIPT_TAIL {
    System.out.println(getText());
};
SCRIPT_LINK_ELEMENT : SCRIPT_HEAD EMPTY_TAG_END {
    System.out.println(getText());
};*/
/*
SCRIPT_ELEMENT : SCRIPT_OPEN_TAG body=SCRIPT_BODY SCRIPT_CLOSE_TAG {
    System.out.println(getText());
};
*/
SCRIPT_OPEN_TAG : SCRIPT_HEAD TAG_END { scriptMode = true; };
fragment SCRIPT_HEAD : TAG_START WS? SCRIPT_TAG_TEXT (WS TAG_ATTRS)? WS?; 
SCRIPT_CLOSE_TAG : {scriptMode}?=> CLOSE_TAG_START WS? SCRIPT_TAG_TEXT WS? TAG_END { scriptMode=false; };
SCRIPT_BODY_CHAR : {scriptMode}?=> ~CLOSE_TAG_START;

fragment SCRIPT_TAG_TEXT : ('s'|'S') ('c'|'C') ('r'|'R') ('i'|'I') ('p'|'P') ('t'|'T');

fragment TAG_ATTRS : (ATTR WS?)+;
fragment ATTR : (ALPHA|'_')+ WS? '=' WS? (QUOT1|QUOT2);

fragment QUOT1 : SQUOT (~SQUOT)* SQUOT;
fragment QUOT2 : DQUOT (~DQUOT)* DQUOT;

fragment EMPTY_TAG_END : '/' TAG_END;
fragment CLOSE_TAG_START : '</';
fragment TAG_START : '<';
fragment TAG_END : '>';

fragment ALPHA : 'a'..'z'|'A'..'Z';
fragment NUM : '0'..'9';
fragment WS : (' '|'\t'|'\n'|'\r')+;
fragment SQUOT : '\'';
fragment DQUOT : '"';
