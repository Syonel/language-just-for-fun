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
        return switch (operator) {
            case PLUS -> (Long) this.lhs.evaluate(interpreter) + (Long) this.rhs.evaluate(interpreter);
            case MINUS -> (Long) this.lhs.evaluate(interpreter) - (Long) this.rhs.evaluate(interpreter);
            case MULTIPLY -> (Long) this.lhs.evaluate(interpreter) * (Long) this.rhs.evaluate(interpreter);
            case DIVIDE -> (Long) this.lhs.evaluate(interpreter) / (Long) this.rhs.evaluate(interpreter);
            case AND -> (Boolean) this.lhs.evaluate(interpreter) && (Boolean) this.rhs.evaluate(interpreter);
            case OR -> (Boolean) this.lhs.evaluate(interpreter) || (Boolean) this.rhs.evaluate(interpreter);
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
        PLUS("+", 3),
        MINUS("-", 3),
        MULTIPLY("*", 4),
        DIVIDE("/", 4),
        EQUALS("=", 1),
        AND("&&", 2),
        OR("||", 2),
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
