package de.bzembrodt.parser.node;

import de.bzembrodt.interpreter.Interpreter;
import de.bzembrodt.interpreter.RuntimeBuildinType;
import de.bzembrodt.interpreter.RuntimeValue;
import de.bzembrodt.lexer.Token;

import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class IfNode extends AstNode {

    private final AstNode condition;
    private final StatementsNode trueBody;
    private final Optional<StatementsNode> falseBody;

    public IfNode(AstNode condition, StatementsNode trueBody, Optional<StatementsNode> falseBody, Token token) {
        super(token);
        this.condition = condition;
        this.trueBody = trueBody;
        this.falseBody = falseBody;
    }

    @Override
    public RuntimeValue evaluate(Interpreter interpreter) {
        RuntimeValue conditionValue = condition.evaluate(interpreter);
        assert conditionValue.type.equals(RuntimeBuildinType.BOOL);

        if ((Boolean) conditionValue.value) {
            interpreter.enterScope();
            trueBody.evaluate(interpreter);
            interpreter.exitScope();
        } else if (falseBody.isPresent()) {
            interpreter.enterScope();
            falseBody.get().evaluate(interpreter);
            interpreter.exitScope();
        }

        return null;
    }

    @Override
    public String toString() {
        String trueBodyString = trueBody.statements.stream().map(s -> "\t" + s.toString()).collect(Collectors.joining("\n"));

        String falseBodyString = "";
        if (falseBody.isPresent()) {
            falseBodyString = " else {\n" + falseBody.get().statements.stream().map(s -> "\t" + s.toString()).collect(Collectors.joining("\n")) + "\n}";
        }
        return "if(" + condition + ") = {\n" + trueBodyString + "\n}";
    }
}
