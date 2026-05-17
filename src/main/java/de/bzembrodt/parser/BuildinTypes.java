package de.bzembrodt.parser;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum BuildinTypes {
    INT("int"),
    BOOL("bool"),
    ;

    public final String name;

    BuildinTypes(String name) {
        this.name = name;
    }

    public static final Set<String> ALL_BUILDIN_TYPES = Arrays.stream(values()).map(BuildinTypes::name).collect(Collectors.toSet());
}
