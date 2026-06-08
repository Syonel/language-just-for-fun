package de.bzembrodt.parser;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum BuildinType {
    INT("int"),
    BOOL("bool"),
    STRING("string"),
    ;

    public final String name;

    BuildinType(String name) {
        this.name = name;
    }

    public static final Set<String> ALL_BUILDIN_TYPES = Arrays.stream(values()).map(BuildinType::name).collect(Collectors.toSet());
}
