package de.bzembrodt.parser.node;

import de.bzembrodt.interpreter.Interpreter;
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
    public Object evaluate(Interpreter interpreter) {
        Object rhsValue = this.rhs.evaluate(interpreter);
        return switch (operator) {
            case NEGATE -> -(Long) rhsValue;
            case NOT -> !(Boolean) rhsValue;
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
