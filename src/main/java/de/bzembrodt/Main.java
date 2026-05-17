package de.bzembrodt;


import de.bzembrodt.interpreter.Interpreter;
import de.bzembrodt.lexer.Lexer;
import de.bzembrodt.lexer.Token;
import de.bzembrodt.parser.Parser;
import de.bzembrodt.parser.node.AstNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {
    static void main() throws IOException {

        String fileContent = Files.readString(Path.of("tests/014_comparative_operators.ljff"));

        Lexer lexer = new Lexer();
        List<Token> tokens = lexer.lex(fileContent);
        IO.println(tokens);

        Parser parser = new Parser();
        AstNode programNode = parser.parse(tokens);
        IO.println(programNode);

        Interpreter interpreter = new Interpreter();
        Object result = interpreter.interpret(programNode);
        IO.println("Result: " + result);

    }
}
