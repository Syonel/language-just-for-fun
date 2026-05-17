package de.bzembrodt.parser.node;

import de.bzembrodt.interpreter.Interpreter;
import de.bzembrodt.interpreter.RuntimeValue;
import de.bzembrodt.lexer.Token;

public class NameLookupNode extends AstNode {
    public final String name;

    public NameLookupNode(String name, Token token) {
        super(token);
        this.name = name;
    }

    @Override
    public RuntimeValue evaluate(Interpreter interpreter) {
        return interpreter.lookupVariable(name);
    }

    @Override
    public String toString() {
        return "@" + name;
    }
}
