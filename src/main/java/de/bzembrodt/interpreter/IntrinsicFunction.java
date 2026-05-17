package de.bzembrodt.interpreter;

import java.util.List;
import java.util.function.Function;

public class IntrinsicFunction {
    private final Function<List<RuntimeValue>, RuntimeValue> function;


    public IntrinsicFunction(Function<List<RuntimeValue>, RuntimeValue> function) {
        this.function = function;
    }

    public RuntimeValue call(List<RuntimeValue> arguments) {
        return function.apply(arguments);
    }
}
