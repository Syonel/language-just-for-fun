package de.bzembrodt.interpreter;

import de.bzembrodt.parser.BuildinType;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RuntimeBuildinType extends RuntimeType {

    private static final Map<BuildinType, RuntimeBuildinType> types = Arrays.stream(BuildinType.values())
            .collect(Collectors.toMap(Function.identity(),
                    RuntimeBuildinType::new,
                    (l, r) -> {
                        throw new IllegalArgumentException("Duplicate Key");
                    },
                    () -> new EnumMap<>(BuildinType.class)));

    public static final RuntimeBuildinType INT = forType(BuildinType.INT);
    public static final RuntimeBuildinType BOOL = forType(BuildinType.BOOL);
    public static final RuntimeBuildinType STRING = forType(BuildinType.STRING);

    public final BuildinType type;

    private RuntimeBuildinType(BuildinType type) {
        this.type = type;
    }


    public static RuntimeBuildinType forType(BuildinType type) {
        assert types != null;
        return types.get(type);
    }
}
