package de.bzembrodt.lexer;

import java.util.ArrayList;
import java.util.List;

public class Lexer {

    public List<Token> lex(String source) {

        List<Token> tokens = new ArrayList<>();

        Token activeToken = null;

        int currentOffset = 0;
        int currentLine = 1;
        int currentColumn = 1;

        for (char c : source.toCharArray()) {
            activeToken = getOrExtendToken(c, activeToken, currentOffset, currentLine, currentColumn, tokens);

            currentOffset++;
            currentColumn++;
            if (c == '\n') {
                currentLine++;
                currentColumn = 1;
            }
        }
        if (activeToken != null) {
            tokens.add(activeToken);
        }
        return tokens;
    }

    private static Token getOrExtendToken(char c, Token activeToken, int currentOffset, int currentLine, int currentColumn, List<Token> tokens) {
        if (activeToken == null) {
            return createNewToken(c, currentOffset, currentLine, currentColumn);
        }
        if (activeToken.type.canExtendFunc.apply(c, activeToken.value)) {
            activeToken.extend(c);
            return activeToken;
        }

        tokens.add(activeToken);

        return createNewToken(c, currentOffset, currentLine, currentColumn);
    }

    private static Token createNewToken(char c, int currentOffset, int currentLine, int currentColumn) {
        TokenType type = TokenType.getTokenType(c);
        if (type != null) {
            return new Token(TokenType.getTokenType(c), c, currentOffset, currentLine, currentColumn);
        }
        return null;
    }

}
