package de.bzembrodt.interpreter;

public class RuntimeValue {
    public final String type;
    public boolean isConst;
    public boolean isInitialized;
    public Object value;

    public RuntimeValue(String type, boolean isConst, boolean isInitialized, Object value) {
        this.isInitialized = isInitialized;
        this.isConst = isConst;
        this.type = type;
        this.value = value;
    }
}
