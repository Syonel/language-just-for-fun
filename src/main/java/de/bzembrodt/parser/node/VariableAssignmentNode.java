package de.bzembrodt.parser.node;

import de.bzembrodt.interpreter.Interpreter;
import de.bzembrodt.interpreter.RuntimeValue;
import de.bzembrodt.lexer.Token;

public class VariableAssignmentNode extends AstNode {

    public final String name;
    public final AstNode rhs;

    public VariableAssignmentNode(String name, AstNode rhs, Token token) {
        super(token);

        this.name = name;
        this.rhs = rhs;
    }

    @Override
    public RuntimeValue evaluate(Interpreter interpreter) {
        RuntimeValue value = rhs.evaluate(interpreter);
        interpreter.assignVariable(name, value);
        return value;
    }

    @Override
    public String toString() {
        return "set['" + name + "'] = " + rhs;
    }
}
