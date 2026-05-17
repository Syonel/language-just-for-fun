package de.bzembrodt.interpreter;

import java.util.List;
import java.util.Objects;

public class RuntimeFunctionType extends RuntimeType {
    public final RuntimeType returnType;
    public final List<RuntimeType> argTypes;

    public RuntimeFunctionType(RuntimeType returnType, List<RuntimeType> argTypes) {
        this.returnType = returnType;
        this.argTypes = argTypes;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RuntimeFunctionType that = (RuntimeFunctionType) o;
        return Objects.equals(returnType, that.returnType) && Objects.equals(argTypes, that.argTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(returnType, argTypes);
    }
}
