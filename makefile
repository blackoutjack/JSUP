# 
# Makefile for the JSUP project.
#
# Written by Rich Joiner (rich@richjoiner.com) in collaboration with Raja Bala
# (meetraja@cs.wisc.edu) under advisory supervision by Professor Ben Liblit.
#

# Class used to create Lexer and Parser classes from grammar files.
ANTLR = org.antlr.Tool

# General JSUP components to be compiled.
GENERAL = JSUPTree PatchHandler SourceSnippet MalformedRuleException ScriptStripAST JSUPStream
MAIN = JSUP

# Subclasses of PatchHandler to be compiled.
HANDLERS = FunctionCallTranslation ReplaceID RemoveArguments AlterArguments InsertBeforeID

# JavaScript grammar.
JS_GRAMMAR = JavaScript.g
JS_LEXER = $(JS_GRAMMAR:.g=Lexer)
JS_PARSER = $(JS_GRAMMAR:.g=Parser)

# Grammar the extracts JavaScript snippets from HTML.
HTML_LEXER = ScriptStripLexer
HTML_PARSER = ScriptStripParser

# Grammar to parse patch files.
PATCH_GRAMMAR = JSUPPatch.g
PATCH_LEXER = $(PATCH_GRAMMAR:.g=Lexer)
PATCH_PARSER = $(PATCH_GRAMMAR:.g=Parser)

# Package and directory tree layout.
PACKAGE = com.blackoutjack.jsup
PKGDIR = $(subst .,/,$(PACKAGE))
SRCDIR = src
BINDIR = bin
HDLDIR = handlers
GRMDIR = grammars
GRMPATH = $(PKGDIR)/$(GRMDIR)

# Create lists of all required .java and .class files for the project.
GRAMMAR_COMPONENTS = $(JS_LEXER) $(JS_PARSER) $(PATCH_LEXER) $(PATCH_PARSER) $(HTML_LEXER) $(HTML_PARSER)
COMPONENTS = $(addprefix $(GRMDIR)/,$(GRAMMAR_COMPONENTS)) $(GENERAL) $(addprefix $(HDLDIR)/,$(HANDLERS)) $(MAIN)
COMPONENTS_JAVA = $(COMPONENTS:=.java)
COMPONENTS_SRC = $(addprefix $(SRCDIR)/$(PKGDIR)/,$(COMPONENTS_JAVA))
COMPONENTS_CLASS = $(COMPONENTS:=.class)
COMPONENTS_BIN = $(addprefix $(BINDIR)/$(PKGDIR)/,$(COMPONENTS_CLASS))

# Use file extension rules when possible.
.SUFFIXES: .g .java .class

# Uncomment to retain intermediate .java files.
#.PRECIOUS: %.java

# Default target. Makes entire JSUP project.
all: $(COMPONENTS_SRC) $(COMPONENTS_BIN)

# Remove all files produced by compilation.
clean:
	rm -rf $(BINDIR)/* *.tokens $(SRCDIR)/$(PKGDIR)/$(GRMDIR)/*.java 

# Rules for lexer/parser components created from a single grammar file.
$(SRCDIR)/$(GRMPATH)/$(PATCH_LEXER).java: $(SRCDIR)/$(GRMPATH)/$(PATCH_GRAMMAR)
	java $(ANTLR) $^
$(SRCDIR)/$(GRMPATH)/$(PATCH_PARSER).java: $(SRCDIR)/$(GRMPATH)/$(PATCH_GRAMMAR)
	java $(ANTLR) $^
$(SRCDIR)/$(GRMPATH)/$(JS_LEXER).java: $(SRCDIR)/$(GRMPATH)/$(JS_GRAMMAR)
	java $(ANTLR) $^
$(SRCDIR)/$(GRMPATH)/$(JS_PARSER).java: $(SRCDIR)/$(GRMPATH)/$(JS_GRAMMAR)
	java $(ANTLR) $^

# Circular dependence between JSUPTree and PatchHandler.
$(BINDIR)/$(PKGDIR)/JSUPTree.class: $(SRCDIR)/$(PKGDIR)/JSUPTree.java $(SRCDIR)/$(PKGDIR)/PatchHandler.java
	javac -d $(BINDIR) -g $(SRCDIR)/$(PKGDIR)/JSUPTree.java $(SRCDIR)/$(PKGDIR)/PatchHandler.java

# General compilation of a .java file from .g file.
.g.java:
	java $(ANTLR) $<

# Create .class files in bin/ directory parallel to .java files in src/.
bin/%.class: src/%.java
	@if [[ ! -d $(subst $(SRCDIR),$(BINDIR),$(@D)) ]]; then \
		mkdir -p $(subst $(SRCDIR),$(BINDIR),$(@D)); \
	fi
	javac -cp $(BINDIR):$(CLASSPATH) -d $(BINDIR) -g $(subst .class,.java,$(subst $(BINDIR),$(SRCDIR),$@))

#.java.class:
#	javac -d $(BINDIR) -g $<
 


