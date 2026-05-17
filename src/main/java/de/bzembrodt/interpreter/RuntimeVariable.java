package de.bzembrodt.interpreter;

public class RuntimeVariable {
    public final RuntimeType type;
    public final boolean isConst;
    public boolean isInitialized;
    public RuntimeValue value;

    public RuntimeVariable(RuntimeType type, boolean isConst, boolean isInitialized, RuntimeValue value) {
        this.type = type;
        this.isConst = isConst;
        this.isInitialized = isInitialized;
        this.value = value;
    }
}
