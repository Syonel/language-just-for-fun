package de.bzembrodt.parser.node;

import de.bzembrodt.interpreter.Interpreter;

import java.util.List;
import java.util.stream.Collectors;

public class StatementsNode extends AstNode {
    public final List<AstNode> statements;

    public StatementsNode(List<AstNode> statements) {
        //TODO We dont really have a token for the program. Think about if that is fine.
        super(null);
        this.statements = statements;
    }

    @Override
    public Object evaluate(Interpreter interpreter) {
        for (AstNode statement : statements) {
            statement.evaluate(interpreter);
            if (interpreter.shouldReturn()) {
                return interpreter.doReturn();
            }
        }
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
