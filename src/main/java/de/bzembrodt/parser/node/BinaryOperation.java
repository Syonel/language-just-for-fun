package de.bzembrodt.parser.node;

import de.bzembrodt.interpreter.Interpreter;
import de.bzembrodt.lexer.Token;

public class BinaryOperation extends AstNode {
    public final AstNode lhs;
    public final Operator operator;
    public final AstNode rhs;

    public BinaryOperation(AstNode lhs, Operator operator, AstNode rhs, Token token) {
        super(token);
        this.lhs = lhs;
        this.operator = operator;
        this.rhs = rhs;
    }

    @Override
    public Object evaluate(Interpreter interpreter) {
        Long lhsValue = (Long) this.lhs.evaluate(interpreter);
        Long rhsValue = (Long) this.rhs.evaluate(interpreter);
        return switch (operator) {
            case PLUS -> lhsValue + rhsValue;
            case MINUS -> lhsValue - rhsValue;
            case MULTIPLY -> lhsValue * rhsValue;
            case DIVIDE -> lhsValue / rhsValue;
        };
    }

    @Override
    public String toString() {
        return lhs.toString() + " " + operator.toString() + " " + rhs.toString();
    }

    public enum Operator {
        PLUS("+", 1),
        MINUS("-", 1),
        MULTIPLY("*", 2),
        DIVIDE("/", 2),
        ;

        private final String stringValue;
        public final int precedence;

        Operator(String stringValue, int precedence) {
            this.stringValue = stringValue;
            this.precedence = precedence;
        }

        @Override
        public String toString() {
            return stringValue;
        }
    }
}
