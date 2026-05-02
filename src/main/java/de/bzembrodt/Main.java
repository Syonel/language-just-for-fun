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

        //String fileContent = Files.readString(Path.of("tests/001_print_number.ljff"));
        //String fileContent = Files.readString(Path.of("tests/002_addition.ljff"));
        //String fileContent = Files.readString(Path.of("tests/003_simple_math.ljff"));
        String fileContent = Files.readString(Path.of("tests/004_comments.ljff"));

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
