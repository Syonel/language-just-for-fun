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
        TEST_CASES.add(new TestCase(
                "006_constants.ljff",
                "[IDENTIFIER(const)[1:1], IDENTIFIER(a)[1:7], COLON[1:8], IDENTIFIER(int)[1:10], EQUALS[1:14], NUMBER(5)[1:16], SEMICOLON[1:17], IDENTIFIER(print)[2:1], OPEN_ROUND_BRACKET[2:6], IDENTIFIER(a)[2:7], CLOSE_ROUND_BRACKET[2:8], SEMICOLON[2:9], EOF[2:10]]",
                "{\n\tdecl_c['a'] = #5\n\tcall['print'](@a)\n}",
                "5\n",
                null
        ));
        TEST_CASES.add(new TestCase(
                "007_multiple_constants.ljff",
                "[IDENTIFIER(const)[1:1], IDENTIFIER(a)[1:7], COLON[1:8], IDENTIFIER(int)[1:10], EQUALS[1:14], NUMBER(1)[1:16], SEMICOLON[1:17], IDENTIFIER(const)[2:1], IDENTIFIER(b)[2:7], COLON[2:8], IDENTIFIER(int)[2:10], EQUALS[2:14], NUMBER(2)[2:16], SEMICOLON[2:17], IDENTIFIER(const)[3:1], IDENTIFIER(c)[3:7], COLON[3:8], IDENTIFIER(int)[3:10], EQUALS[3:14], IDENTIFIER(a)[3:16], PLUS[3:18], NUMBER(2)[3:20], MULTIPLY[3:22], IDENTIFIER(b)[3:24], SEMICOLON[3:25], IDENTIFIER(print)[4:1], OPEN_ROUND_BRACKET[4:6], IDENTIFIER(c)[4:7], CLOSE_ROUND_BRACKET[4:8], SEMICOLON[4:9], EOF[4:10]]",
                "{\n\tdecl_c['a'] = #1\n\tdecl_c['b'] = #2\n\tdecl_c['c'] = (@a + (#2 * @b))\n\tcall['print'](@c)\n}",
                "5\n",
                null
        ));
        TEST_CASES.add(new TestCase(
                "008_variables.ljff",
                "[IDENTIFIER(var)[1:1], IDENTIFIER(a)[1:5], COLON[1:6], IDENTIFIER(int)[1:8], EQUALS[1:12], NUMBER(1)[1:14], SEMICOLON[1:15], IDENTIFIER(a)[2:1], EQUALS[2:3], IDENTIFIER(a)[2:5], PLUS[2:7], NUMBER(2)[2:9], SEMICOLON[2:10], IDENTIFIER(print)[3:1], OPEN_ROUND_BRACKET[3:6], IDENTIFIER(a)[3:7], CLOSE_ROUND_BRACKET[3:8], SEMICOLON[3:9], EOF[3:10]]",
                "{\n\tdecl_v['a'] = #1\n\tset['a'] = (@a + #2)\n\tcall['print'](@a)\n}",
                "3\n",
                null
        ));
        TEST_CASES.add(new TestCase(
                "009_functions.ljff",
                "[IDENTIFIER(fn)[1:1], IDENTIFIER(add)[1:4], OPEN_ROUND_BRACKET[1:7], IDENTIFIER(a)[1:8], COLON[1:9], IDENTIFIER(int)[1:11], COMMA[1:14], IDENTIFIER(b)[1:16], COLON[1:17], IDENTIFIER(int)[1:19], CLOSE_ROUND_BRACKET[1:22], COLON[1:23], IDENTIFIER(int)[1:25], OPEN_CURLY_BRACKET[1:29], IDENTIFIER(return)[2:5], IDENTIFIER(a)[2:12], PLUS[2:14], IDENTIFIER(b)[2:16], SEMICOLON[2:17], CLOSE_CURLY_BRACKET[3:1], IDENTIFIER(print)[4:1], OPEN_ROUND_BRACKET[4:6], IDENTIFIER(add)[4:7], OPEN_ROUND_BRACKET[4:10], NUMBER(1)[4:11], COMMA[4:12], NUMBER(2)[4:14], CLOSE_ROUND_BRACKET[4:15], CLOSE_ROUND_BRACKET[4:16], SEMICOLON[4:17], EOF[4:18]]",
                "{\n\tdecl_f['add'](a, b) = {\n\treturn (@a + @b)\n\treturn\n}\n\tcall['print'](call['add'](#1, #2))\n}",
                "3\n",
                null
        ));
        TEST_CASES.add(new TestCase(
                "010_nested_functions.ljff",
                "[IDENTIFIER(fn)[1:1], IDENTIFIER(add)[1:4], OPEN_ROUND_BRACKET[1:7], IDENTIFIER(a)[1:8], COLON[1:9], IDENTIFIER(int)[1:11], COMMA[1:14], IDENTIFIER(b)[1:16], COLON[1:17], IDENTIFIER(int)[1:19], CLOSE_ROUND_BRACKET[1:22], COLON[1:23], IDENTIFIER(int)[1:25], OPEN_CURLY_BRACKET[1:29], IDENTIFIER(return)[2:5], IDENTIFIER(double)[2:12], OPEN_ROUND_BRACKET[2:18], IDENTIFIER(a)[2:19], CLOSE_ROUND_BRACKET[2:20], PLUS[2:22], IDENTIFIER(double)[2:24], OPEN_ROUND_BRACKET[2:30], IDENTIFIER(b)[2:31], CLOSE_ROUND_BRACKET[2:32], SEMICOLON[2:33], CLOSE_CURLY_BRACKET[3:1], IDENTIFIER(fn)[4:1], IDENTIFIER(double)[4:4], OPEN_ROUND_BRACKET[4:10], IDENTIFIER(a)[4:11], COLON[4:12], IDENTIFIER(int)[4:14], CLOSE_ROUND_BRACKET[4:17], COLON[4:18], IDENTIFIER(int)[4:20], OPEN_CURLY_BRACKET[4:24], IDENTIFIER(return)[5:5], IDENTIFIER(a)[5:12], MULTIPLY[5:14], NUMBER(2)[5:16], SEMICOLON[5:17], CLOSE_CURLY_BRACKET[6:1], IDENTIFIER(print)[7:1], OPEN_ROUND_BRACKET[7:6], IDENTIFIER(add)[7:7], OPEN_ROUND_BRACKET[7:10], NUMBER(1)[7:11], COMMA[7:12], NUMBER(2)[7:14], CLOSE_ROUND_BRACKET[7:15], CLOSE_ROUND_BRACKET[7:16], SEMICOLON[7:17], EOF[7:18]]",
                "{\n\tdecl_f['add'](a, b) = {\n\treturn (call['double'](@a) + call['double'](@b))\n\treturn\n}\n\tdecl_f['double'](a) = {\n\treturn (@a * #2)\n\treturn\n}\n\tcall['print'](call['add'](#1, #2))\n}",
                "6\n",
                null
        ));
        TEST_CASES.add(new TestCase(
                "011_boolean.ljff",
                "[IDENTIFIER(const)[1:1], IDENTIFIER(t)[1:7], COLON[1:8], IDENTIFIER(bool)[1:10], EQUALS[1:15], IDENTIFIER(true)[1:17], SEMICOLON[1:21], IDENTIFIER(const)[2:1], IDENTIFIER(f)[2:7], COLON[2:8], IDENTIFIER(bool)[2:10], EQUALS[2:15], IDENTIFIER(false)[2:17], SEMICOLON[2:22], IDENTIFIER(print)[3:1], OPEN_ROUND_BRACKET[3:6], IDENTIFIER(t)[3:7], CLOSE_ROUND_BRACKET[3:8], SEMICOLON[3:9], IDENTIFIER(print)[4:1], OPEN_ROUND_BRACKET[4:6], IDENTIFIER(f)[4:7], CLOSE_ROUND_BRACKET[4:8], SEMICOLON[4:9], EOF[4:10]]",
                "{\n\tdecl_c['t'] = #true\n\tdecl_c['f'] = #false\n\tcall['print'](@t)\n\tcall['print'](@f)\n}",
                "true\nfalse\n",
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