package de.bzembrodt.interpreter;

import de.bzembrodt.parser.BuildinType;
import de.bzembrodt.parser.node.AstNode;
import de.bzembrodt.parser.node.FunctionDefinitionNode;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class Interpreter {

    private final Consumer<String> printer;
    private final Scope globalScope = new Scope(Optional.empty(), Optional.empty());
    private Scope currentScope = globalScope;
    private boolean shouldReturn = false;
    private boolean shouldBreak = false;
    private boolean shouldContinue = false;
    private RuntimeValue returnValue;

    public Interpreter() {
        printer = IO::print;
        init();
    }

    public Interpreter(StringBuilder sb) {
        this.printer = sb::append;
        init();
    }

    private void init() {
        globalScope.setVariable("print", new RuntimeVariable(null, true, true, new RuntimeValue(null, new IntrinsicFunction(this::intrinsicPrint))));
    }

    public RuntimeValue interpret(AstNode programNode) {
        return programNode.evaluate(this);
    }

    public RuntimeValue call(String name, List<RuntimeValue> args) {
        assert currentScope.hasVariable(name, true);

        RuntimeVariable variable = currentScope.getVariable(name);
        if (variable.value.value instanceof IntrinsicFunction) {
            return ((IntrinsicFunction) variable.value.value).call(args);
        }
        assert variable.type instanceof RuntimeFunctionType;
        assert variable.value.value instanceof FunctionDefinitionNode;

        FunctionDefinitionNode func = (FunctionDefinitionNode) variable.value.value;
        assert func.arguments.size() == args.size();
        currentScope = new Scope(Optional.of(globalScope), Optional.of(currentScope));
        for (int i = 0; i < args.size(); i++) {
            RuntimeValue argValue = args.get(i);
            String argName = func.arguments.get(i).name();

            RuntimeType argType = getRuntimeType(func.arguments.get(i).type());
            assert argType.equals(argValue.type);

            declareVariable(argName, argType, false, Optional.of(argValue));
        }

        return func.body.evaluate(this);
    }

    public void enterScope() {
        currentScope = new Scope(Optional.of(currentScope), currentScope.callerScope);
    }

    public void exitScope() {
        assert currentScope.parentScope.isPresent();
        currentScope = currentScope.parentScope.get();
    }

    private RuntimeValue intrinsicPrint(List<RuntimeValue> args) {
        assert args.size() == 1;
        printer.accept(args.getFirst().value + "\n");
        return null;
    }

    public RuntimeType getRuntimeType(String type) {
        int arraySize = -1;
        if (type.contains("[")) {
            String[] split = type.split("\\[");
            type = split[0];
            arraySize = Integer.parseInt(split[1].substring(0, split[1].length() - 1));
        }
        RuntimeType runtimeType = null;
        for (BuildinType value : BuildinType.values()) {
            if (value.name.equals(type)) {
                runtimeType = RuntimeBuildinType.forType(value);
            }
        }
        //Only buildin types for now
        assert runtimeType != null;

        if (arraySize != -1) {
            runtimeType = new RuntimeArrayType(runtimeType, arraySize);
        }
        return runtimeType;
    }

    public void declareVariable(String name, RuntimeType type, boolean isConst, Optional<RuntimeValue> value) {
        assert !currentScope.hasVariable(name, false);
        if (value.isPresent()) {
            assert type.equals(value.get().type);
        }
        currentScope.setVariable(name, new RuntimeVariable(type, isConst, value.isPresent(), value.orElse(null)));
    }

    public void assignVariable(String name, RuntimeValue value) {
        assert currentScope.hasVariable(name, true);

        RuntimeVariable variable = currentScope.getVariable(name);
        assert !variable.isConst;
        assert variable.type.equals(value.type);

        variable.value = value;
        variable.isInitialized = true;
    }

    public void assignArrayIndexVariable(String name, long index, RuntimeValue value) {
        assert currentScope.hasVariable(name, true);

        RuntimeVariable variable = currentScope.getVariable(name);
        assert !variable.isConst;
        assert variable.isInitialized;
        assert variable.type instanceof RuntimeArrayType;
        RuntimeArrayType arrayType = (RuntimeArrayType) variable.type;
        assert arrayType.elementsType.equals(value.type);

        List<RuntimeValue> arrayValue = (List<RuntimeValue>) variable.value.value;
        assert arrayValue.size() > index;
        arrayValue.set((int) index, value);
    }

    public RuntimeValue lookupVariable(String name) {
        assert currentScope.hasVariable(name, true);
        RuntimeVariable value = currentScope.getVariable(name);
        assert value != null;
        assert value.isInitialized;

        return value.value;
    }

    public void funcReturn(RuntimeValue value) {
        shouldReturn = true;
        returnValue = value;
    }

    public boolean shouldReturn() {
        return shouldReturn;
    }

    public boolean shouldBreak() {
        return shouldBreak;
    }

    public boolean shouldContinue() {
        return shouldContinue;
    }

    public RuntimeValue doReturn() {
        RuntimeValue result = returnValue;
        shouldReturn = false;
        returnValue = null;
        assert currentScope.callerScope.isPresent();
        currentScope = currentScope.callerScope.get();

        return result;
    }

    public void doBreak() {
        shouldBreak = true;
    }

    public void doContinue() {
        shouldContinue = true;
    }

    public void clearBreak() {
        shouldBreak = false;
    }

    public void clearContinue() {
        shouldContinue = false;
    }
}
