package de.bzembrodt;

import de.bzembrodt.interpreter.Interpreter;
import de.bzembrodt.lexer.Lexer;
import de.bzembrodt.lexer.Token;
import de.bzembrodt.parser.Parser;
import de.bzembrodt.parser.node.AstNode;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestExamples {

    public static final Path RESULTS_PATH = Path.of("tests", "results.json");

    public record Result(String fileName, String expectedTokens, String expectedAst, String expectedOutput,
                         Object expectedReturn) {

        @Override
        @NonNull
        public String toString() {
            return fileName;
        }
    }

    private static final List<Result> expectedResults = new ArrayList<>();

    @BeforeAll
    static void loadExpected() throws IOException {
        String results = Files.readString(RESULTS_PATH);
        expectedResults.addAll(new JsonMapper().readValue(results, new TypeReference<List<Result>>() {
        }));
    }

    @Test
    void testAllFilesAreTested() throws IOException {
        try (Stream<Path> fileStream = Files.list(Path.of("tests")).filter(p -> !p.equals(RESULTS_PATH))) {
            Set<String> files = fileStream.map(Path::getFileName).map(Path::toString).collect(Collectors.toSet());
            Set<String> tests = expectedResults.stream().map(t -> t.fileName).collect(Collectors.toSet());
            assertEquals(files, tests, "All files should have a test case.");
        }
    }

    @ParameterizedTest(name = "{0}")
    @FieldSource("expectedResults")
    void testLexer(Result expectedResult) throws IOException {

        String file = Files.readString(Path.of("tests", expectedResult.fileName));
        List<Token> tokens = new Lexer().lex(file);
        assertEquals(expectedResult.expectedTokens, tokens.toString());
    }

    @ParameterizedTest(name = "{0}")
    @FieldSource("expectedResults")
    void testAst(Result expectedResult) throws IOException {

        String file = Files.readString(Path.of("tests", expectedResult.fileName));
        AstNode ast = new Parser().parse(new Lexer().lex(file));
        assertEquals(expectedResult.expectedAst, ast.toString());
    }

    @ParameterizedTest(name = "{0}")
    @FieldSource("expectedResults")
    void testExecution(Result expectedResult) throws IOException {

        String file = Files.readString(Path.of("tests", expectedResult.fileName));
        StringBuilder output = new StringBuilder();
        Object result = new Interpreter(output).interpret(new Parser().parse(new Lexer().lex(file)));
        assertEquals(expectedResult.expectedOutput, output.toString());
        assertEquals(expectedResult.expectedReturn, Objects.toString(result));
    }

}