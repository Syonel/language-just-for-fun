package de.bzembrodt.parser.node;

import de.bzembrodt.interpreter.Interpreter;
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
    public Object evaluate(Interpreter interpreter) {
        interpreter.declareVariable(name, type, isConst, initializer.map(i -> i.evaluate(interpreter)));
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
