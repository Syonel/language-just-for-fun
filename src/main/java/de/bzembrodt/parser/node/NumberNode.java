package de.bzembrodt.parser.node;

import de.bzembrodt.interpreter.Interpreter;
import de.bzembrodt.interpreter.RuntimeBuildinType;
import de.bzembrodt.interpreter.RuntimeValue;
import de.bzembrodt.lexer.Token;

public class NumberNode extends AstNode {
    public final long value;

    public NumberNode(long value, Token token) {
        super(token);
        this.value = value;
    }

    @Override
    public RuntimeValue evaluate(Interpreter interpreter) {
        return new RuntimeValue(RuntimeBuildinType.INT, value);
    }

    @Override
    public String toString() {
        return "#" + value;
    }
}
