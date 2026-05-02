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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestExamples {

    private static final List<TestCase> TEST_CASES = new ArrayList<>();

    @BeforeAll
    static void setUp() {
        TEST_CASES.add(new TestCase(
                "001_print_number.ljff",
                "[IDENTIFIER(print)[1:1], OPEN_ROUND_BRACKET[1:6], NUMBER(1)[1:7], CLOSE_ROUND_BRACKET[1:8], SEMICOLON[1:9], EOF[1:10]]",
                "call['print'](#1)",
                "1\n",
                null
        ));
        TEST_CASES.add(new TestCase(
                "002_addition.ljff",
                "[IDENTIFIER(print)[1:1], OPEN_ROUND_BRACKET[1:6], NUMBER(1)[1:7], PLUS[1:9], NUMBER(1)[1:11], CLOSE_ROUND_BRACKET[1:12], SEMICOLON[1:13], EOF[1:14]]",
                "call['print']((#1 + #1))",
                "2\n",
                null
        ));
        TEST_CASES.add(new TestCase(
                "003_simple_math.ljff",
                "[IDENTIFIER(print)[1:1], OPEN_ROUND_BRACKET[1:6], NUMBER(1)[1:7], PLUS[1:9], NUMBER(1)[1:11], MULTIPLY[1:13], NUMBER(2)[1:15], MINUS[1:17], NUMBER(5)[1:19], DIVIDE[1:21], NUMBER(5)[1:23], CLOSE_ROUND_BRACKET[1:24], SEMICOLON[1:25], EOF[1:26]]",
                "call['print']((#1 + ((#1 * #2) - (#5 / #5))))",
                "2\n",
                null
        ));
        TEST_CASES.add(new TestCase(
                "004_comments.ljff",
                "[IDENTIFIER(print)[2:1], OPEN_ROUND_BRACKET[2:6], NUMBER(1)[2:7], PLUS[2:8], NUMBER(1)[2:20], CLOSE_ROUND_BRACKET[2:21], SEMICOLON[2:22], EOF[2:23]]",
                "call['print']((#1 + #1))",
                "2\n",
                null
        ));
        TEST_CASES.add(new TestCase(
                "005_multiple_statments.ljff",
                "[IDENTIFIER(print)[1:1], OPEN_ROUND_BRACKET[1:6], NUMBER(1)[1:7], CLOSE_ROUND_BRACKET[1:8], SEMICOLON[1:9], IDENTIFIER(print)[2:1], OPEN_ROUND_BRACKET[2:6], NUMBER(2)[2:7], CLOSE_ROUND_BRACKET[2:8], SEMICOLON[2:9], EOF[2:10]]",
                "{\n\tcall['print'](#1)\n\tcall['print'](#2)\n}",
                "1\n2\n",
                null
        ));
    }

    @Test
    void testAllFilesAreTested() throws IOException {
        try (Stream<Path> fileStream = Files.list(Path.of("tests"))) {
            Set<String> files = fileStream.map(Path::getFileName).map(Path::toString).collect(Collectors.toSet());
            Set<String> tests = TEST_CASES.stream().map(t -> t.fileName).collect(Collectors.toSet());
            assertEquals(files, tests, "All files should have a test case.");
        }
    }

    @ParameterizedTest(name = "{0}")
    @FieldSource("TEST_CASES")
    void testLexer(TestCase t) throws IOException {

        String file = Files.readString(Path.of("tests", t.fileName));
        List<Token> tokens = new Lexer().lex(file);
        assertEquals(t.expectedTokens, tokens.toString());
    }

    @ParameterizedTest(name = "{0}")
    @FieldSource("TEST_CASES")
    void testAst(TestCase t) throws IOException {

        String file = Files.readString(Path.of("tests", t.fileName));
        AstNode ast = new Parser().parse(new Lexer().lex(file));
        assertEquals(t.expectedAst, ast.toString());
    }

    @ParameterizedTest(name = "{0}")
    @FieldSource("TEST_CASES")
    void testExecution(TestCase t) throws IOException {

        String file = Files.readString(Path.of("tests", t.fileName));
        StringBuilder output = new StringBuilder();
        Object result = new Interpreter(output).interpret(new Parser().parse(new Lexer().lex(file)));
        assertEquals(t.expectedOutput, output.toString());
        assertEquals(t.expectedReturn, result);
    }


    private record TestCase(String fileName, String expectedTokens, String expectedAst, String expectedOutput,
                            Object expectedReturn) {

        @Override
        @NonNull
        public String toString() {
            return fileName;
        }
    }

}