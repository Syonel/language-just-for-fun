package de.bzembrodt.parser.node;

import de.bzembrodt.interpreter.Interpreter;
import de.bzembrodt.interpreter.RuntimeArrayType;
import de.bzembrodt.interpreter.RuntimeType;
import de.bzembrodt.interpreter.RuntimeValue;
import de.bzembrodt.lexer.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ArrayNode extends AstNode {
    public final List<AstNode> values;

    public ArrayNode(List<AstNode> values, Token token) {
        super(token);
        this.values = values;
    }

    @Override
    public RuntimeValue evaluate(Interpreter interpreter) {
        List<RuntimeValue> interpreted = values.stream().map(interpreter::interpret).toList();
        //TODO what to do for type of empty arrays?
        assert !interpreted.isEmpty();
        RuntimeType firstType = interpreted.getFirst().type;
        assert interpreted.stream().allMatch(i -> i.type.equals(firstType));
        return new RuntimeValue(new RuntimeArrayType(firstType, interpreted.size()), new ArrayList<>(interpreted));
    }

    @Override
    public String toString() {
        return "[" + values.stream().map(Object::toString).collect(Collectors.joining(", ")) + "]";
    }
}
