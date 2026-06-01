package de.bzembrodt.interpreter;

import java.util.Objects;

public class RuntimeArrayType extends RuntimeType {
    public final RuntimeType elementsType;
    public final int size;

    public RuntimeArrayType(RuntimeType elementsType, int size) {
        this.elementsType = elementsType;
        this.size = size;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RuntimeArrayType that = (RuntimeArrayType) o;
        return size == that.size && Objects.equals(elementsType, that.elementsType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementsType, size);
    }
}
