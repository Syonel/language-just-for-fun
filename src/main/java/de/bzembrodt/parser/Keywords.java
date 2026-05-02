package de.bzembrodt.parser;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum Keywords {
    CONST("const");

    public final String name;

    Keywords(String name) {
        this.name = name;
    }

    public static final Set<String> ALL_KEYWORDS = Arrays.stream(values()).map(Keywords::name).collect(Collectors.toSet());
}
