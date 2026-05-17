package de.bzembrodt.parser.node;

import de.bzembrodt.interpreter.Interpreter;
import de.bzembrodt.lexer.Token;

public class BoolNode extends AstNode {
    public final boolean value;

    public BoolNode(boolean value, Token token) {
        super(token);
        this.value = value;
    }

    @Override
    public Object evaluate(Interpreter interpreter) {
        return value;
    }

    @Override
    public String toString() {
        return "#" + value;
    }
}
