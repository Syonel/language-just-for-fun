package de.bzembrodt.parser.node;

import de.bzembrodt.interpreter.Interpreter;
import de.bzembrodt.lexer.Token;

import java.util.List;
import java.util.stream.Collectors;

public class FunctionCallNode extends AstNode {
    public final String name;
    public final List<AstNode> arguments;

    public FunctionCallNode(String name, List<AstNode> arguments, Token token) {
        super(token);
        this.name = name;
        this.arguments = arguments;
    }

    @Override
    public Object evaluate(Interpreter interpreter) {
        List<Object> args = arguments.stream().map(a -> a.evaluate(interpreter)).toList();

        return interpreter.call(name, args);
    }

    @Override
    public String toString() {
        return "call['" + name + "'](" + arguments.stream().map(AstNode::toString).collect(Collectors.joining(", ")) + ")";
    }
}
