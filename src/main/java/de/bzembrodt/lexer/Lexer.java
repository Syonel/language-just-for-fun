package de.bzembrodt.lexer;

import java.util.ArrayList;
import java.util.List;

public class Lexer {

    public List<Token> lex(String source) {

        List<Token> tokens = new ArrayList<>();

        Token activeToken = null;

        Position position = new Position();

        char[] sourceArray = source.toCharArray();
        //TODO comment handling is quite nasty right now. Should be improved
        for (int i = 0; i < sourceArray.length; i++) {
            char c = sourceArray[i];
            if (c == '/' && i + 1 < sourceArray.length && sourceArray[i + 1] == '/') {
                for (; i < sourceArray.length && sourceArray[i] != '\n'; i++) {
                    position = position.advance();
                }
                // step back so the \n can be consumed by the outer loop
                i--;
                continue;
            }
            if (c == '/' && i + 1 < sourceArray.length && sourceArray[i + 1] == '*') {
                while (true) {
                    if (sourceArray[i] == '\n') {
                        position = position.newLine();
                    } else {
                        position = position.advance();
                    }
                    i++;
                    // Unclosed comment
                    assert i < sourceArray.length;
                    if (sourceArray[i] == '*' && i + 1 < sourceArray.length && sourceArray[i + 1] == '/') {
                        i++;
                        position = position.advance();
                        position = position.advance();
                        break;
                    }
                }
                continue;
            }

            activeToken = getOrExtendToken(c, activeToken, position, tokens);

            if (c == '\n') {
                position = position.newLine();
            } else {
                position = position.advance();
            }
        }
        if (activeToken != null) {
            tokens.add(activeToken);
        }
        tokens.add(new Token(TokenType.EOF, (char) 0, position));
        return tokens;
    }

    private static Token getOrExtendToken(char c, Token activeToken, Position position, List<Token> tokens) {
        if (activeToken == null) {
            return createNewToken(c, position);
        }
        if (activeToken.type.canExtendFunc.apply(c, activeToken.value)) {
            activeToken.extend(c);
            return activeToken;
        }

        tokens.add(activeToken);

        return createNewToken(c, position);
    }

    private static Token createNewToken(char c, Position position) {
        TokenType type = TokenType.getTokenType(c);
        if (type != null) {
            return new Token(TokenType.getTokenType(c), c, position);
        }
        return null;
    }

}
