package de.bzembrodt;

import de.bzembrodt.interpreter.Interpreter;
import de.bzembrodt.lexer.Lexer;
import de.bzembrodt.lexer.Token;
import de.bzembrodt.parser.Parser;
import de.bzembrodt.parser.node.AstNode;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class RecordResults {

    //Record output
    static void main() throws Exception {
        List<TestExamples.Result> results = new ArrayList<>();
        try (Stream<Path> fileStream = Files.list(Path.of("tests"))) {
            for (Path path : fileStream.toList()) {
                if (path.getFileName().toString().endsWith(".json")) {
                    continue;
                }
                String file = Files.readString(path);
                StringBuilder output = new StringBuilder();
                List<Token> tokens = new Lexer().lex(file);
                AstNode ast = new Parser().parse(tokens);
                Object result = new Interpreter(output).interpret(ast);
                results.add(new TestExamples.Result(path.getFileName().toString(), tokens.toString(), ast.toString(), output.toString(), Objects.toString(result)));
            }
        }
        String resultsString = new JsonMapper().writerWithDefaultPrettyPrinter().writeValueAsString(results);
        Files.writeString(TestExamples.RESULTS_PATH, resultsString);
    }
}
