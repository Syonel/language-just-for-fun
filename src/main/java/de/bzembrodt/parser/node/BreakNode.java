package de.bzembrodt.parser.node;

import de.bzembrodt.interpreter.Interpreter;
import de.bzembrodt.interpreter.RuntimeValue;
import de.bzembrodt.lexer.Token;

public class BreakNode extends AstNode {

    public BreakNode(Token token) {
        super(token);
    }

    @Override
    public RuntimeValue evaluate(Interpreter interpreter) {
        interpreter.doBreak();
        return null;
    }

    @Override
    public String toString() {
        return "break";
    }
}
