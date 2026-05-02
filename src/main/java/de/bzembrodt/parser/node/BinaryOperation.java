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
        Object lhsValue = this.lhs.evaluate(interpreter);
        Object rhsValue = this.rhs.evaluate(interpreter);
        assert lhsValue instanceof Long && rhsValue instanceof Long;
        return (Long) lhsValue + (Long) rhsValue;
    }

    @Override
    public String toString() {
        return lhs.toString() + " " + operator.toString() + " " + rhs.toString();
    }

    public enum Operator {
        PLUS("+");

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
