package de.bzembrodt.parser.node;

import de.bzembrodt.interpreter.Interpreter;
import de.bzembrodt.lexer.Token;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FunctionDefinitionNode extends AstNode {
    public final String name;
    public final String returnType;
    public final List<Argument> arguments;
    public final StatementsNode body;

    public FunctionDefinitionNode(String name, String returnType, List<Argument> arguments, StatementsNode body, Token token) {
        super(token);
        this.name = name;
        this.returnType = returnType;
        this.arguments = arguments;
        this.body = body;
    }

    @Override
    public Object evaluate(Interpreter interpreter) {
        //TODO get correct type for function
        interpreter.declareVariable(name, "func", true, Optional.of(this));
        return null;
    }

    @Override
    public String toString() {
        String argumentsString = arguments.stream().map(Argument::name).collect(Collectors.joining(", "));
        String bodyString = body.statements.stream().map(s -> "\t" + s.toString()).collect(Collectors.joining("\n"));
        return "decl_f['" + name + "'](" + argumentsString + ") = {\n" + bodyString + "\n}";
    }


    public record Argument(String name, String type) {
    }
}
