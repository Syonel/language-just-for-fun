package de.bzembrodt.parser;

import de.bzembrodt.lexer.Token;

import java.util.List;

public class TokenList {

    private final List<Token> tokens;
    private int position = 0;

    public TokenList(List<Token> tokens) {
        this.tokens = tokens;
    }

    public Token advance() {
        position++;
        return getToken();
    }

    public Token getToken() {
        return tokens.get(position);
    }

    public Token peek() {
        return peek(1);
    }

    public Token peek(int offset) {
        return tokens.get(position + offset);
    }

}
