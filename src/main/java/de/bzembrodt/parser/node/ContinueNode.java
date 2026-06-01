package de.bzembrodt.parser.node;

import de.bzembrodt.interpreter.Interpreter;
import de.bzembrodt.interpreter.RuntimeValue;
import de.bzembrodt.lexer.Token;

public class ContinueNode extends AstNode {

    public ContinueNode(Token token) {
        super(token);
    }

    @Override
    public RuntimeValue evaluate(Interpreter interpreter) {
        interpreter.doContinue();
        return null;
    }

    @Override
    public String toString() {
        return "continue";
    }
}
