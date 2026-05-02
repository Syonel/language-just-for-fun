package de.bzembrodt.parser.node;

import de.bzembrodt.lexer.Token;

public class NumberNode extends AstNode {
    public final long value;
    public final Token token;

    public NumberNode(long value, Token token) {
        this.value = value;
        this.token = token;
    }


    @Override
    public String toString() {
        return "#" + value;
    }
}
