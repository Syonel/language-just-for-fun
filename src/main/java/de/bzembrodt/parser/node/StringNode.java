package de.bzembrodt.parser.node;

import de.bzembrodt.interpreter.Interpreter;
import de.bzembrodt.interpreter.RuntimeBuildinType;
import de.bzembrodt.interpreter.RuntimeValue;
import de.bzembrodt.lexer.Token;

public class StringNode extends AstNode {
    public final String value;

    public StringNode(String value, Token token) {
        super(token);
        this.value = value;
    }

    @Override
    public RuntimeValue evaluate(Interpreter interpreter) {
        return new RuntimeValue(RuntimeBuildinType.STRING, value);
    }

    @Override
    public String toString() {
        return "#'" + value + "'";
    }
}
