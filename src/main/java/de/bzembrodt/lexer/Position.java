package de.bzembrodt.lexer;

public class Position {
    public final int offset;
    public final int line;
    public final int column;

    public Position() {
        this(0, 1, 1);
    }

    private Position(int offset, int line, int column) {
        this.offset = offset;
        this.line = line;
        this.column = column;
    }

    public Position advance() {
        return new Position(offset + 1, line, column + 1);
    }

    public Position newLine() {
        return new Position(offset + 1, line + 1, 1);
    }
}
