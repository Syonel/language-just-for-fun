package de.bzembrodt.parser.node;

import de.bzembrodt.interpreter.Interpreter;
import de.bzembrodt.lexer.Token;

public abstract class AstNode {

    public Token token;

    public AstNode(Token token) {
        this.token = token;
    }

    public abstract Object evaluate(Interpreter interpreter);
    public abstract String toString();
}
