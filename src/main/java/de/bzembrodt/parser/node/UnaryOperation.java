package de.bzembrodt.parser.node;

import de.bzembrodt.interpreter.Interpreter;
import de.bzembrodt.interpreter.RuntimeBuildinType;
import de.bzembrodt.interpreter.RuntimeValue;
import de.bzembrodt.lexer.Token;

public class UnaryOperation extends AstNode {
    public final Operator operator;
    public final AstNode rhs;

    public UnaryOperation(Operator operator, AstNode rhs, Token token) {
        super(token);
        this.operator = operator;
        this.rhs = rhs;
    }

    @Override
    public RuntimeValue evaluate(Interpreter interpreter) {
        RuntimeValue rhsValue = this.rhs.evaluate(interpreter);
        return switch (operator) {
            case NEGATE -> {
                assert rhsValue.type.equals(RuntimeBuildinType.INT);
                yield new RuntimeValue(RuntimeBuildinType.INT, -(Long) rhsValue.value);
            }
            case NOT -> {
                assert rhsValue.type.equals(RuntimeBuildinType.BOOL);
                yield new RuntimeValue(RuntimeBuildinType.BOOL, !(Boolean) rhsValue.value);
            }
        };
    }

    @Override
    public String toString() {
        return "(" + operator.toString() + " " + rhs.toString() + ")";
    }

    public enum Operator {
        NEGATE("-"),
        NOT("!"),
        ;

        private final String stringValue;

        Operator(String stringValue) {
            this.stringValue = stringValue;
        }

        @Override
        public String toString() {
            return stringValue;
        }
    }
}
