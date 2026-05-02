package de.bzembrodt.lexer;

public class Token {
    public final TokenType type;
    public String value;
    public final Position position;

    public Token(TokenType type, char c, Position position) {
        this.type = type;
        this.value = String.valueOf(c);
        this.position = position;
    }

    public void extend(char c) {
        value += String.valueOf(c);
    }

    @Override
    public String toString() {
        return type.name() + "('" + value + "')[" + position.line + ":" + position.column + "]";
    }
}
