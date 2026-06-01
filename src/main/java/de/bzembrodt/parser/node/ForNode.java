package de.bzembrodt.parser.node;

import de.bzembrodt.interpreter.Interpreter;
import de.bzembrodt.interpreter.RuntimeBuildinType;
import de.bzembrodt.interpreter.RuntimeValue;
import de.bzembrodt.lexer.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ForNode extends AstNode {

    private final Optional<AstNode> init;
    private final Optional<AstNode> condition;
    private final Optional<AstNode> step;
    private final StatementsNode body;

    public ForNode(Optional<AstNode> init, Optional<AstNode> condition, Optional<AstNode> step, StatementsNode body, Token token) {
        super(token);
        this.init = init;
        this.condition = condition;
        this.step = step;
        this.body = body;
    }

    @Override
    public RuntimeValue evaluate(Interpreter interpreter) {
        interpreter.enterScope();
        init.ifPresent(interpreter::interpret);

        while (checkCondition(interpreter)) {
            interpreter.enterScope();
            body.evaluate(interpreter);
            if (interpreter.shouldBreak()) {
                interpreter.clearBreak();
                interpreter.exitScope();
                break;
            }
            interpreter.clearContinue();
            interpreter.exitScope();

            step.ifPresent(interpreter::interpret);
        }

        interpreter.exitScope();
        return null;
    }

    private boolean checkCondition(Interpreter interpreter) {
        if (condition.isEmpty()) {
            return true;
        }
        RuntimeValue conditionValue = condition.get().evaluate(interpreter);
        assert conditionValue.type.equals(RuntimeBuildinType.BOOL);
        return (boolean) conditionValue.value;
    }

    @Override
    public String toString() {
        String bodyString = body.statements.stream().map(s -> "\t" + s.toString()).collect(Collectors.joining("\n"));

        List<String> header = new ArrayList<>();
        if (init.isPresent()) {
            header.add("init: " + init);
        }
        if (condition.isPresent()) {
            header.add("condition: " + condition);
        }
        if (step.isPresent()) {
            header.add("step: " + step);
        }

        return "for(" + String.join(", ", header) + ") = {\n" + bodyString + "\n}";
    }
}
