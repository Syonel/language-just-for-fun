package de.bzembrodt.interpreter;

import de.bzembrodt.parser.node.AstNode;

import java.util.List;

public class Interpreter {

    public Object interpret(AstNode programNode) {
        return programNode.evaluate(this);
    }

    public Object call(String name, List<Object> args) {
        assert name.equals("print");
        assert args.size() == 1;
        IO.println(args.getFirst());
        return null;
    }
}
