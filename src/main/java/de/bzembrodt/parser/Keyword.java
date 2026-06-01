package de.bzembrodt.parser;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum Keyword {
    CONST("const"),
    VAR("var"),
    FN("fn"),
    RETURN("return"),
    TRUE("true"),
    FALSE("false"),
    IF("if"),
    ELSE("else"),
    FOR("for"),
    BREAK("break"),
    CONTINUE("continue"),
    ;

    public final String name;

    Keyword(String name) {
        this.name = name;
    }

    public static final Set<String> ALL_KEYWORDS = Arrays.stream(values()).map(Keyword::name).collect(Collectors.toSet());
}
