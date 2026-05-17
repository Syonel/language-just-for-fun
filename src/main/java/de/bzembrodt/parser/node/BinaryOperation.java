package de.bzembrodt.parser.node;

import de.bzembrodt.interpreter.Interpreter;
import de.bzembrodt.interpreter.RuntimeBuildinType;
import de.bzembrodt.interpreter.RuntimeValue;
import de.bzembrodt.lexer.Token;

import java.util.function.BiFunction;

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
    public RuntimeValue evaluate(Interpreter interpreter) {
        return switch (operator) {
            case MULTIPLY -> intsInIntOut(interpreter, (a, b) -> a * b);
            case DIVIDE -> intsInIntOut(interpreter, (a, b) -> a / b);
            case PLUS -> intsInIntOut(interpreter, (a, b) -> a + b);
            case MINUS -> intsInIntOut(interpreter, (a, b) -> a - b);
            case LESS_THAN -> intsInBoolOut(interpreter, (a, b) -> a < b);
            case GREATER_THAN -> intsInBoolOut(interpreter, (a, b) -> a > b);
            case LESS_EQUAL -> intsInBoolOut(interpreter, (a, b) -> a <= b);
            case GREATER_EQUAL -> intsInBoolOut(interpreter, (a, b) -> a >= b);
            case EQUAL -> intsInBoolOut(interpreter, (a, b) -> a.equals(b));
            case NOT_EQUAL -> intsInBoolOut(interpreter, (a, b) -> !a.equals(b));
            case AND -> boolsInBoolOut(interpreter, (a, b) -> a && b, false);
            case OR -> boolsInBoolOut(interpreter, (a, b) -> a || b, true);
            //Handled by other node
            case ASSIGN -> {
                assert false;
                yield null;
            }
        };
    }

    private RuntimeValue intsInIntOut(Interpreter interpreter, BiFunction<Long, Long, Long> evaluator) {
        RuntimeValue lhsValue = this.lhs.evaluate(interpreter);
        RuntimeValue rhsValue = this.rhs.evaluate(interpreter);
        assert lhsValue.type.equals(RuntimeBuildinType.INT);
        assert rhsValue.type.equals(RuntimeBuildinType.INT);

        Long result = evaluator.apply((Long) lhsValue.value, (Long) rhsValue.value);
        return new RuntimeValue(RuntimeBuildinType.INT, result);
    }

    private RuntimeValue intsInBoolOut(Interpreter interpreter, BiFunction<Long, Long, Boolean> evaluator) {
        RuntimeValue lhsValue = this.lhs.evaluate(interpreter);
        RuntimeValue rhsValue = this.rhs.evaluate(interpreter);
        assert lhsValue.type.equals(RuntimeBuildinType.INT);
        assert rhsValue.type.equals(RuntimeBuildinType.INT);

        Boolean result = evaluator.apply((Long) lhsValue.value, (Long) rhsValue.value);
        return new RuntimeValue(RuntimeBuildinType.BOOL, result);
    }

    private RuntimeValue boolsInBoolOut(Interpreter interpreter, BiFunction<Boolean, Boolean, Boolean> evaluator, boolean shortCircuitOn) {
        RuntimeValue lhsValue = this.lhs.evaluate(interpreter);
        assert lhsValue.type.equals(RuntimeBuildinType.BOOL);
        if (lhsValue.value.equals(shortCircuitOn)) {
            return new RuntimeValue(RuntimeBuildinType.BOOL, lhsValue.value);
        }

        RuntimeValue rhsValue = this.rhs.evaluate(interpreter);
        assert rhsValue.type.equals(RuntimeBuildinType.BOOL);

        Boolean result = evaluator.apply((Boolean) lhsValue.value, (Boolean) rhsValue.value);
        return new RuntimeValue(RuntimeBuildinType.BOOL, result);
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
