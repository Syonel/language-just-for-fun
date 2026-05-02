package de.bzembrodt.parser.node;

import de.bzembrodt.interpreter.Interpreter;
import de.bzembrodt.lexer.Token;

public class NumberNode extends AstNode {
    public final long value;

    public NumberNode(long value, Token token) {
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
