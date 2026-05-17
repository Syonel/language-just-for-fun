package de.bzembrodt.interpreter;

public class RuntimeValue {
    public final RuntimeType type;
    public final Object value;

    public RuntimeValue(RuntimeType type, Object value) {
        this.type = type;
        this.value = value;
    }
}
