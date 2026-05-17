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
            case MULTIPLY -> (Long) this.lhs.evaluate(interpreter) * (Long) this.rhs.evaluate(interpreter);
            case DIVIDE -> (Long) this.lhs.evaluate(interpreter) / (Long) this.rhs.evaluate(interpreter);
            case PLUS -> (Long) this.lhs.evaluate(interpreter) + (Long) this.rhs.evaluate(interpreter);
            case MINUS -> (Long) this.lhs.evaluate(interpreter) - (Long) this.rhs.evaluate(interpreter);
            case LESS_THAN -> (Long) this.lhs.evaluate(interpreter) < (Long) this.rhs.evaluate(interpreter);
            case GREATER_THAN -> (Long) this.lhs.evaluate(interpreter) > (Long) this.rhs.evaluate(interpreter);
            case LESS_EQUAL -> (Long) this.lhs.evaluate(interpreter) <= (Long) this.rhs.evaluate(interpreter);
            case GREATER_EQUAL -> (Long) this.lhs.evaluate(interpreter) >= (Long) this.rhs.evaluate(interpreter);
            case EQUAL -> ((Long) this.lhs.evaluate(interpreter)).equals((Long) this.rhs.evaluate(interpreter));
            case NOT_EQUAL -> !((Long) this.lhs.evaluate(interpreter)).equals((Long) this.rhs.evaluate(interpreter));
            case AND -> (Boolean) this.lhs.evaluate(interpreter) && (Boolean) this.rhs.evaluate(interpreter);
            case OR -> (Boolean) this.lhs.evaluate(interpreter) || (Boolean) this.rhs.evaluate(interpreter);
            //Handled by other node
            case ASSIGN -> {
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
        MULTIPLY("*", 5),
        DIVIDE("/", 5),
        PLUS("+", 4),
        MINUS("-", 4),
        LESS_THAN("<", 3),
        GREATER_THAN(">", 3),
        LESS_EQUAL("<=", 3),
        GREATER_EQUAL(">=", 3),
        EQUAL("==", 3),
        NOT_EQUAL("!=", 3),
        AND("&&", 2),
        OR("||", 2),
        ASSIGN("=", 1),
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
