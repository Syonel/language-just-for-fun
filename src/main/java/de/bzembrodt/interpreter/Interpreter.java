package de.bzembrodt.interpreter;

import de.bzembrodt.parser.node.AstNode;

import java.util.List;
import java.util.function.Consumer;

public class Interpreter {

    private final Consumer<String> printer;

    public Interpreter() {
        printer = IO::print;
    }

    public Interpreter(StringBuilder sb) {
        this.printer = sb::append;
    }

    public Object interpret(AstNode programNode) {
        return programNode.evaluate(this);
    }

    public Object call(String name, List<Object> args) {
        assert name.equals("print");
        assert args.size() == 1;
        printer.accept(args.getFirst() + "\n");
        return null;
    }
}
