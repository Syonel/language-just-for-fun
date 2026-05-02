package de.bzembrodt;


import de.bzembrodt.lexer.Lexer;
import de.bzembrodt.lexer.Token;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {
    static void main() throws IOException {

        String fileContent = Files.readString(Path.of("tests/001_print_number.ljff"));

        Lexer lexer = new Lexer();
        List<Token> tokens = lexer.lex(fileContent);

        IO.println(tokens);
    }
}
