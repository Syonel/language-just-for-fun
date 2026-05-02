package de.bzembrodt.lexer;

public class Token {
    public final TokenType type;
    public String value;
    public final int offset;
    public final int line;
    public final int column;

    public Token(TokenType type, char c, int offset, int line, int column) {
        this.type = type;
        this.value = String.valueOf(c);
        this.offset = offset;
        this.line = line;
        this.column = column;
    }

    public void extend(char c) {
        value += String.valueOf(c);
    }

    @Override
    public String toString() {
        return type.name() + "('" + value + "')[" + line + ":" + column + "]";
    }
}
