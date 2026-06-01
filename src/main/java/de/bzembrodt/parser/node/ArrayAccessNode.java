package de.bzembrodt.parser.node;

import de.bzembrodt.interpreter.Interpreter;
import de.bzembrodt.interpreter.RuntimeArrayType;
import de.bzembrodt.interpreter.RuntimeBuildinType;
import de.bzembrodt.interpreter.RuntimeValue;
import de.bzembrodt.lexer.Token;

import java.util.List;

public class ArrayAccessNode extends AstNode {

    public final AstNode array;
    public final AstNode index;

    public ArrayAccessNode(AstNode array, AstNode index, Token token) {
        super(token);
        this.array = array;
        this.index = index;
    }

    @Override
    public RuntimeValue evaluate(Interpreter interpreter) {
        RuntimeValue arrayValue = array.evaluate(interpreter);
        assert arrayValue.type instanceof RuntimeArrayType;

        RuntimeValue indexValue = index.evaluate(interpreter);
        assert indexValue.type.equals(RuntimeBuildinType.INT);

        long i = (long) indexValue.value;

        List<RuntimeValue> listValue = (List<RuntimeValue>) arrayValue.value;

        assert listValue.size() > i;

        return listValue.get((int) i);
    }

    @Override
    public String toString() {
        return array + "[" + index + "]";
    }
}
