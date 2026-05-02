package de.bzembrodt.parser.node;

import de.bzembrodt.lexer.Token;

import java.util.List;
import java.util.stream.Collectors;

public class FunctionCallNode extends AstNode {
    public final String name;
    public final List<AstNode> arguments;
    public final Token token;

    public FunctionCallNode(String name, List<AstNode> arguments, Token token) {
        this.name = name;
        this.arguments = arguments;
        this.token = token;
    }

    @Override
    public String toString() {
        return "call['" + name + "'](" + arguments.stream().map(AstNode::toString).collect(Collectors.joining(", ")) + ")";
    }
}
