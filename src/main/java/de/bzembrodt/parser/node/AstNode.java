package de.bzembrodt.parser.node;

import de.bzembrodt.interpreter.Interpreter;

public abstract class AstNode {

    public abstract Object evaluate(Interpreter interpreter);
    public abstract String toString();
}
