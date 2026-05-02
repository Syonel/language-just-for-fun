package de.bzembrodt.interpreter;

import de.bzembrodt.parser.node.AstNode;
import de.bzembrodt.parser.node.FunctionDefinitionNode;
import de.bzembrodt.parser.node.ReturnNode;
import de.bzembrodt.parser.node.StatementsNode;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class Interpreter {

    private final Consumer<String> printer;
    private final Scope globalScope = new Scope(Optional.empty(), Optional.empty());
    private Scope currentScope = globalScope;
    private boolean shouldReturn = false;
    private Object returnValue;

    public Interpreter() {
        printer = IO::print;
        init();
    }

    public Interpreter(StringBuilder sb) {
        this.printer = sb::append;
        init();
    }

    private void init() {
        FunctionDefinitionNode printNode = new FunctionDefinitionNode("print", "void", List.of(new FunctionDefinitionNode.Argument("value", "string")), new StatementsNode(List.of(new AstNode(null) {
            @Override
            public Object evaluate(Interpreter interpreter) {
                printer.accept(interpreter.lookupVariable("value") + "\n");
                return null;
            }

            @Override
            public String toString() {
                return "__intrinsic_print__";
            }
        }, new ReturnNode(Optional.empty(), null))), null);
        printNode.evaluate(this);
    }

    public Object interpret(AstNode programNode) {
        return programNode.evaluate(this);
    }

    public Object call(String name, List<Object> args) {
        assert currentScope.hasVariable(name, true);

        RuntimeValue runtimeValue = currentScope.getVariable(name);
        assert runtimeValue.value instanceof FunctionDefinitionNode;

        FunctionDefinitionNode func = (FunctionDefinitionNode) runtimeValue.value;
        assert func.arguments.size() == args.size();
        Scope functionScope = new Scope(Optional.of(globalScope), Optional.of(currentScope));
        currentScope = functionScope;
        for (int i = 0; i < args.size(); i++) {
            Object argValue = args.get(i);
            String argName = func.arguments.get(i).name();
            //TODO Check for matching types
            String argType = func.arguments.get(i).type();

            declareVariable(argName, argType, false, Optional.of(argValue));
        }

        return func.body.evaluate(this);
    }

    public void declareVariable(String name, String type, boolean isConst, Optional<Object> value) {
        assert !currentScope.hasVariable(name, false);
        currentScope.setVariable(name, new RuntimeValue(type, isConst, value.isPresent(), value.orElse(null)));
    }

    public void assignVariable(String name, Object value) {
        assert currentScope.hasVariable(name, true);

        RuntimeValue runtimeValue = currentScope.getVariable(name);
        assert !runtimeValue.isConst;

        runtimeValue.value = value;
        runtimeValue.isInitialized = true;
    }

    public Object lookupVariable(String name) {
        assert currentScope.hasVariable(name, true);
        RuntimeValue value = currentScope.getVariable(name);
        assert value != null;
        assert value.isInitialized;

        return value.value;
    }

    public void funcReturn(Object o) {
        shouldReturn = true;
        returnValue = o;
    }

    public boolean shouldReturn() {
        return shouldReturn;
    }

    public Object doReturn() {
        Object result = returnValue;
        shouldReturn = false;
        returnValue = null;
        assert currentScope.callerScope.isPresent();
        currentScope = currentScope.callerScope.get();

        return result;
    }
}
