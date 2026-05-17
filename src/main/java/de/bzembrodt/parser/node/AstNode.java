package de.bzembrodt.parser.node;

import de.bzembrodt.interpreter.Interpreter;
import de.bzembrodt.interpreter.RuntimeValue;
import de.bzembrodt.lexer.Token;

public abstract class AstNode {

    public final Token token;

    public AstNode(Token token) {
        this.token = token;
    }

    public abstract RuntimeValue evaluate(Interpreter interpreter);
    public abstract String toString();
}
