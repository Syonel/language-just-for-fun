package de.bzembrodt.parser.node;

import de.bzembrodt.interpreter.Interpreter;
import de.bzembrodt.lexer.Token;

public class NameLookupNode extends AstNode {
    public final String name;

    public NameLookupNode(String name, Token token) {
        super(token);
        this.name = name;
    }

    @Override
    public Object evaluate(Interpreter interpreter) {
        return interpreter.lookupVariable(name);
    }

    @Override
    public String toString() {
        return "@" + name;
    }
}
