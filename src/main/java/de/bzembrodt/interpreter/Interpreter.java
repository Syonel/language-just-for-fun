package de.bzembrodt.interpreter;

import de.bzembrodt.parser.node.AstNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class Interpreter {

    private final Consumer<String> printer;
    private final Map<String, RuntimeValue> variables = new HashMap<>();

    public Interpreter() {
        printer = IO::print;
    }

    public Interpreter(StringBuilder sb) {
        this.printer = sb::append;
    }

    public Object interpret(AstNode programNode) {
        return programNode.evaluate(this);
    }

    public Object call(String name, List<Object> args) {
        assert name.equals("print");
        assert args.size() == 1;
        printer.accept(args.getFirst() + "\n");
        return null;
    }

    public void declareVariable(String name, String type, boolean isConst, Optional<Object> value) {
        assert !variables.containsKey(name);
        variables.put(name, new RuntimeValue(type, isConst, value.isPresent(), value.orElse(null)));
    }

    public Object lookupVariable(String name) {
        RuntimeValue value = variables.get(name);
        assert value != null;
        assert value.isInitialized;

        return value.value;
    }
}
