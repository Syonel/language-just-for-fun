package de.bzembrodt.parser.node;

import de.bzembrodt.interpreter.Interpreter;
import de.bzembrodt.interpreter.RuntimeType;
import de.bzembrodt.interpreter.RuntimeValue;
import de.bzembrodt.lexer.Token;

import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class VariableDeclarationNode extends AstNode {
    public final boolean isConst;
    public final String name;
    public final String type;
    public final Optional<AstNode> initializer;

    public VariableDeclarationNode(boolean isConst, String name, String type, Optional<AstNode> initializer, Token token) {
        super(token);
        this.isConst = isConst;
        this.name = name;
        this.type = type;
        this.initializer = initializer;
    }

    @Override
    public RuntimeValue evaluate(Interpreter interpreter) {
        RuntimeType runtimeType = interpreter.getRuntimeType(type);
        interpreter.declareVariable(name, runtimeType, isConst, initializer.map(i -> i.evaluate(interpreter)));
        return null;
    }

    @Override
    public String toString() {
        String init = "";
        if (initializer.isPresent()) {
            init = " = " + initializer.get();
        }
        return "decl_" + (isConst ? "c" : "v") + "['" + name + "']" + init;
    }
}
