package com.richjoiner.jsup;

import org.antlr.runtime.tree.*;
import org.antlr.runtime.*;

public abstract class PatchHandler {
    
    public abstract boolean evaluate(JSUPTree tree, TokenRewriteStream trs);

}
