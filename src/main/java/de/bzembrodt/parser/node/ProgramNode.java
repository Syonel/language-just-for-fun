package de.bzembrodt.parser.node;

import de.bzembrodt.interpreter.Interpreter;

import java.util.List;
import java.util.stream.Collectors;

public class ProgramNode extends AstNode {
    public final List<AstNode> statements;

    public ProgramNode(List<AstNode> statements) {
        //TODO We dont really have a token for the program. Think about if that is fine.
        super(null);
        this.statements = statements;
    }

    @Override
    public Object evaluate(Interpreter interpreter) {
        statements.forEach(statement -> statement.evaluate(interpreter));
        return null;
    }

    @Override
    public String toString() {
        if (statements.size() == 1) {
            return statements.getFirst().toString();
        }
        return "{\n" + statements.stream().map(s -> "\t" + s.toString()).collect(Collectors.joining("\n")) + "\n}";
    }
}
