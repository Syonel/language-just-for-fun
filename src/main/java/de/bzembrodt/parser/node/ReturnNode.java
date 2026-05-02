package de.bzembrodt.parser.node;

import de.bzembrodt.interpreter.Interpreter;
import de.bzembrodt.lexer.Token;

import java.util.Optional;

public class ReturnNode extends AstNode {
    public final Optional<AstNode> value;

    public ReturnNode(Optional<AstNode> value, Token token) {
        super(token);
        this.value = value;
    }

    @Override
    public Object evaluate(Interpreter interpreter) {
        interpreter.funcReturn(value.map(v -> v.evaluate(interpreter)).orElse(null));
        return null;
    }

    @Override
    public String toString() {
        String ret = "";
        if (value.isPresent()) {
            ret = " " + value.get();
        }
        return "return" + ret;
    }
}
