package de.bzembrodt.lexer;

import java.util.function.BiFunction;
import java.util.function.Predicate;

public enum TokenType {
    IDENTIFIER(TokenType::isAlphabetic, (c, _) -> isAlphanumeric(c)),
    NUMBER(TokenType::isNumeric, (c, _) -> isNumeric(c)),

    SEMICOLON(';'),
    PLUS('+'),
    MINUS('-'),
    MULTIPLY('*'),
    DIVIDE('/'),
    EQUALS('='),
    COMMA(','),
    COLON(':'),
    OPEN_ROUND_BRACKET('('),
    CLOSE_ROUND_BRACKET(')'),
    OPEN_CURLY_BRACKET('{'),
    CLOSE_CURLY_BRACKET('}'),
    AMPERSAND('&'),
    PIPE('|'),
    EXCLAMATION_MARK('!'),
    LESS_THAN('<'),
    GREATER_THAN('>'),
    EOF(_ -> false, (_, _) -> false);

    public final Predicate<Character> matchesFunc;
    public final BiFunction<Character, String, Boolean> canExtendFunc;

    TokenType(Character singleChar) {
        matchesFunc = singleChar::equals;
        canExtendFunc = (_, _) -> false;
    }

    TokenType(Predicate<Character> matchesFunc, BiFunction<Character, String, Boolean> canExtendFunc) {
        this.matchesFunc = matchesFunc;
        this.canExtendFunc = canExtendFunc;
    }

    private static boolean isAlphabetic(char c) {
        return Character.isAlphabetic(c);
    }

    private static boolean isNumeric(char c) {
        return Character.isDigit(c);
    }

    private static boolean isAlphanumeric(char c) {
        return isAlphabetic(c) || isNumeric(c);
    }

    static TokenType getTokenType(char c) {
        for (TokenType type : values()) {
            if (type.matchesFunc.test(c)) {
                return type;
            }
        }
        return null;
    }
}
