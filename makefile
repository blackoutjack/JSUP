ANTLR = org.antlr.Tool
GRAMMAR_FILE = JavaScript.g
LEXER = JavaScriptLexer
PARSER = JavaScriptParser
AST = JSUPTree
PATCH_GRAMMAR = JSUPPatch.g
EXCEPTIONS = MalformedRuleException
HANDLER = PatchHandler
HANDLERS = FunctionCallTranslation ReplaceID RemoveArguments AlterArguments
HDLDIR = handlers
HTML_LEXER = ScriptStripLexer
HTML_PARSER = ScriptStripParser
HTML_AST = ScriptStripAST
SNIPPET = SourceSnippet
MAIN = JSUP
PATCH_LEXER = $(PATCH_GRAMMAR:.g=Lexer)
PATCH_PARSER = $(PATCH_GRAMMAR:.g=Parser)

PACKAGE = com.richjoiner.jsup
PKGDIR = $(subst .,/,$(PACKAGE))
GRMDIR = grammars
SRCDIR = src
BINDIR = bin
GRMPATH = $(PKGDIR)/$(GRMDIR)

GRAMMAR_COMPONENTS = $(LEXER) $(PARSER) $(PATCH_LEXER) $(PATCH_PARSER) $(HTML_LEXER) $(HTML_PARSER)
GENERAL_COMPONENTS = $(EXCEPTIONS) $(AST) $(HANDLER) $(HTML_AST) $(SNIPPET)
COMPONENTS = $(addprefix $(GRMDIR)/,$(GRAMMAR_COMPONENTS)) $(GENERAL_COMPONENTS) $(addprefix $(HDLDIR)/,$(HANDLERS))
COMPONENTS_JAVA = $(COMPONENTS:=.java)
COMPONENTS_SRC = $(addprefix $(SRCDIR)/$(PKGDIR)/,$(COMPONENTS_JAVA))
COMPONENTS_CLASS = $(COMPONENTS:=.class)
COMPONENTS_BIN = $(addprefix $(BINDIR)/$(PKGDIR)/,$(COMPONENTS_CLASS))

.SUFFIXES: .g .java .class
.PRECIOUS: %.java

all: $(COMPONENTS_SRC) $(COMPONENTS_BIN) $(BINDIR)/$(PKGDIR)/$(MAIN).class

test:
	echo "$(SRCDIR)/$(GRMPATH)/$(LEXER).java: $(SRCDIR)/$(GRMPATH)/$(GRAMMAR_FILE)"
echo "grm: $(SRCDIR)/$(GRMPATH)/$(LEXER).java"

clean:
	rm -rf $(BINDIR)/* *.tokens $(SRCDIR)/$(PKGDIR)/$(GRMDIR)/*.java 


$(SRCDIR)/$(GRMPATH)/$(PATCH_LEXER).java: $(SRCDIR)/$(GRMPATH)/$(PATCH_GRAMMAR)
	java $(ANTLR) $^

$(SRCDIR)/$(GRMPATH)/$(PATCH_PARSER).java: $(SRCDIR)/$(GRMPATH)/$(PATCH_GRAMMAR)
	java $(ANTLR) $^

$(SRCDIR)/$(GRMPATH)/$(LEXER).java: $(SRCDIR)/$(GRMPATH)/$(GRAMMAR_FILE)
	java $(ANTLR) $^
	
$(SRCDIR)/$(GRMPATH)/$(PARSER).java: $(SRCDIR)/$(GRMPATH)/$(GRAMMAR_FILE)
	java $(ANTLR) $^

$(BINDIR)/$(PKGDIR)/JSUPTree.class:
	javac -d $(BINDIR) -g $(SRCDIR)/$(PKGDIR)/JSUPTree.java $(SRCDIR)/$(PKGDIR)/PatchHandler.java

.g.java:
	java $(ANTLR) $<

%.class:
	@if [[ ! -d $(subst $(SRCDIR),$(BINDIR),$(@D)) ]]; then \
		mkdir -p $(subst $(SRCDIR),$(BINDIR),$(@D)); \
	fi
	javac -cp $(BINDIR):$(CLASSPATH) -d $(BINDIR) -g $(subst .class,.java,$(subst $(BINDIR),$(SRCDIR),$@))

.java.class:
	javac -d $(BINDIR) -g $<
 


