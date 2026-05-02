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
            //Handled by other node
            case EQUALS -> {
                assert false;
                yield null;
            }
        };
    }

    @Override
    public String toString() {
        return "(" + lhs.toString() + " " + operator.toString() + " " + rhs.toString() + ")";
    }

    public enum Operator {
        PLUS("+", 2),
        MINUS("-", 2),
        MULTIPLY("*", 3),
        DIVIDE("/", 3),
        EQUALS("=", 1),
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
