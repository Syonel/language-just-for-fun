package de.bzembrodt.parser.node;

import de.bzembrodt.interpreter.Interpreter;
import de.bzembrodt.interpreter.RuntimeBuildinType;
import de.bzembrodt.interpreter.RuntimeValue;
import de.bzembrodt.lexer.Token;

public class ArrayIndexAssignmentNode extends AstNode {

    public final String name;
    public final AstNode index;
    public final AstNode rhs;

    public ArrayIndexAssignmentNode(String name, AstNode index, AstNode rhs, Token token) {
        super(token);

        this.name = name;
        this.index = index;
        this.rhs = rhs;
    }

    @Override
    public RuntimeValue evaluate(Interpreter interpreter) {
        RuntimeValue indexValue = index.evaluate(interpreter);
        assert indexValue.type.equals(RuntimeBuildinType.INT);

        RuntimeValue value = rhs.evaluate(interpreter);

        interpreter.assignArrayIndexVariable(name, (long) indexValue.value, value);
        return value;
    }

    @Override
    public String toString() {
        return "set['" + name + "'][" + index + "] = " + rhs;
    }
}
