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
        TEST_CASES.add(new TestCase(
                "012_boolean_operators.ljff",
                "[IDENTIFIER(fn)[1:1], IDENTIFIER(trueFn)[1:4], OPEN_ROUND_BRACKET[1:10], CLOSE_ROUND_BRACKET[1:11], COLON[1:12], IDENTIFIER(bool)[1:14], OPEN_CURLY_BRACKET[1:19], IDENTIFIER(print)[2:5], OPEN_ROUND_BRACKET[2:10], NUMBER(1)[2:11], CLOSE_ROUND_BRACKET[2:12], SEMICOLON[2:13], IDENTIFIER(return)[3:5], IDENTIFIER(true)[3:12], SEMICOLON[3:16], CLOSE_CURLY_BRACKET[4:1], IDENTIFIER(fn)[5:1], IDENTIFIER(falseFn)[5:4], OPEN_ROUND_BRACKET[5:11], CLOSE_ROUND_BRACKET[5:12], COLON[5:13], IDENTIFIER(bool)[5:15], OPEN_CURLY_BRACKET[5:20], IDENTIFIER(print)[6:5], OPEN_ROUND_BRACKET[6:10], NUMBER(2)[6:11], CLOSE_ROUND_BRACKET[6:12], SEMICOLON[6:13], IDENTIFIER(return)[7:5], IDENTIFIER(false)[7:12], SEMICOLON[7:17], CLOSE_CURLY_BRACKET[8:1], IDENTIFIER(print)[10:1], OPEN_ROUND_BRACKET[10:6], IDENTIFIER(trueFn)[10:7], OPEN_ROUND_BRACKET[10:13], CLOSE_ROUND_BRACKET[10:14], AMPERSAND[10:16], AMPERSAND[10:17], IDENTIFIER(falseFn)[10:19], OPEN_ROUND_BRACKET[10:26], CLOSE_ROUND_BRACKET[10:27], CLOSE_ROUND_BRACKET[10:28], SEMICOLON[10:29], IDENTIFIER(print)[11:1], OPEN_ROUND_BRACKET[11:6], IDENTIFIER(falseFn)[11:7], OPEN_ROUND_BRACKET[11:14], CLOSE_ROUND_BRACKET[11:15], AMPERSAND[11:17], AMPERSAND[11:18], IDENTIFIER(trueFn)[11:20], OPEN_ROUND_BRACKET[11:26], CLOSE_ROUND_BRACKET[11:27], CLOSE_ROUND_BRACKET[11:28], SEMICOLON[11:29], IDENTIFIER(print)[12:1], OPEN_ROUND_BRACKET[12:6], IDENTIFIER(falseFn)[12:7], OPEN_ROUND_BRACKET[12:14], CLOSE_ROUND_BRACKET[12:15], AMPERSAND[12:17], AMPERSAND[12:18], IDENTIFIER(falseFn)[12:20], OPEN_ROUND_BRACKET[12:27], CLOSE_ROUND_BRACKET[12:28], CLOSE_ROUND_BRACKET[12:29], SEMICOLON[12:30], IDENTIFIER(print)[13:1], OPEN_ROUND_BRACKET[13:6], IDENTIFIER(trueFn)[13:7], OPEN_ROUND_BRACKET[13:13], CLOSE_ROUND_BRACKET[13:14], AMPERSAND[13:16], AMPERSAND[13:17], IDENTIFIER(trueFn)[13:19], OPEN_ROUND_BRACKET[13:25], CLOSE_ROUND_BRACKET[13:26], CLOSE_ROUND_BRACKET[13:27], SEMICOLON[13:28], IDENTIFIER(print)[15:1], OPEN_ROUND_BRACKET[15:6], IDENTIFIER(trueFn)[15:7], OPEN_ROUND_BRACKET[15:13], CLOSE_ROUND_BRACKET[15:14], PIPE[15:16], PIPE[15:17], IDENTIFIER(falseFn)[15:19], OPEN_ROUND_BRACKET[15:26], CLOSE_ROUND_BRACKET[15:27], CLOSE_ROUND_BRACKET[15:28], SEMICOLON[15:29], IDENTIFIER(print)[16:1], OPEN_ROUND_BRACKET[16:6], IDENTIFIER(falseFn)[16:7], OPEN_ROUND_BRACKET[16:14], CLOSE_ROUND_BRACKET[16:15], PIPE[16:17], PIPE[16:18], IDENTIFIER(trueFn)[16:20], OPEN_ROUND_BRACKET[16:26], CLOSE_ROUND_BRACKET[16:27], CLOSE_ROUND_BRACKET[16:28], SEMICOLON[16:29], IDENTIFIER(print)[17:1], OPEN_ROUND_BRACKET[17:6], IDENTIFIER(falseFn)[17:7], OPEN_ROUND_BRACKET[17:14], CLOSE_ROUND_BRACKET[17:15], PIPE[17:17], PIPE[17:18], IDENTIFIER(falseFn)[17:20], OPEN_ROUND_BRACKET[17:27], CLOSE_ROUND_BRACKET[17:28], CLOSE_ROUND_BRACKET[17:29], SEMICOLON[17:30], IDENTIFIER(print)[18:1], OPEN_ROUND_BRACKET[18:6], IDENTIFIER(trueFn)[18:7], OPEN_ROUND_BRACKET[18:13], CLOSE_ROUND_BRACKET[18:14], PIPE[18:16], PIPE[18:17], IDENTIFIER(trueFn)[18:19], OPEN_ROUND_BRACKET[18:25], CLOSE_ROUND_BRACKET[18:26], CLOSE_ROUND_BRACKET[18:27], SEMICOLON[18:28], EOF[18:29]]",
                "{\n\tdecl_f['trueFn']() = {\n\tcall['print'](#1)\n\treturn #true\n\treturn\n}\n\tdecl_f['falseFn']() = {\n\tcall['print'](#2)\n\treturn #false\n\treturn\n}\n\tcall['print']((call['trueFn']() && call['falseFn']()))\n\tcall['print']((call['falseFn']() && call['trueFn']()))\n\tcall['print']((call['falseFn']() && call['falseFn']()))\n\tcall['print']((call['trueFn']() && call['trueFn']()))\n\tcall['print']((call['trueFn']() || call['falseFn']()))\n\tcall['print']((call['falseFn']() || call['trueFn']()))\n\tcall['print']((call['falseFn']() || call['falseFn']()))\n\tcall['print']((call['trueFn']() || call['trueFn']()))\n}",
                "1\n2\nfalse\n2\nfalse\n2\nfalse\n1\n1\ntrue\n1\ntrue\n2\n1\ntrue\n2\n2\nfalse\n1\ntrue\n",
                null
        ));
        TEST_CASES.add(new TestCase(
                "013_unary_operators.ljff",
                "[IDENTIFIER(print)[1:1], OPEN_ROUND_BRACKET[1:6], EXCLAMATION_MARK[1:7], IDENTIFIER(true)[1:8], CLOSE_ROUND_BRACKET[1:12], SEMICOLON[1:13], IDENTIFIER(print)[2:1], OPEN_ROUND_BRACKET[2:6], EXCLAMATION_MARK[2:7], IDENTIFIER(false)[2:8], CLOSE_ROUND_BRACKET[2:13], SEMICOLON[2:14], IDENTIFIER(print)[3:1], OPEN_ROUND_BRACKET[3:6], EXCLAMATION_MARK[3:7], EXCLAMATION_MARK[3:8], IDENTIFIER(true)[3:9], CLOSE_ROUND_BRACKET[3:13], SEMICOLON[3:14], IDENTIFIER(print)[4:1], OPEN_ROUND_BRACKET[4:6], MINUS[4:7], NUMBER(1)[4:8], CLOSE_ROUND_BRACKET[4:9], SEMICOLON[4:10], IDENTIFIER(print)[5:1], OPEN_ROUND_BRACKET[5:6], MINUS[5:7], MINUS[5:8], NUMBER(1)[5:9], CLOSE_ROUND_BRACKET[5:10], SEMICOLON[5:11], EOF[5:12]]",
                "{\n\tcall['print']((! #true))\n\tcall['print']((! #false))\n\tcall['print']((! (! #true)))\n\tcall['print']((- #1))\n\tcall['print']((- (- #1)))\n}",
                "false\ntrue\ntrue\n-1\n1\n",
                null
        ));
        TEST_CASES.add(new TestCase(
                "014_comparative_operators.ljff",
                "[IDENTIFIER(print)[1:1], OPEN_ROUND_BRACKET[1:6], NUMBER(1)[1:7], LESS_THAN[1:9], NUMBER(2)[1:11], CLOSE_ROUND_BRACKET[1:12], SEMICOLON[1:13], IDENTIFIER(print)[2:1], OPEN_ROUND_BRACKET[2:6], NUMBER(1)[2:7], LESS_THAN[2:9], NUMBER(1)[2:11], CLOSE_ROUND_BRACKET[2:12], SEMICOLON[2:13], IDENTIFIER(print)[3:1], OPEN_ROUND_BRACKET[3:6], NUMBER(2)[3:7], LESS_THAN[3:9], NUMBER(1)[3:11], CLOSE_ROUND_BRACKET[3:12], SEMICOLON[3:13], IDENTIFIER(print)[5:1], OPEN_ROUND_BRACKET[5:6], NUMBER(1)[5:7], GREATER_THAN[5:9], NUMBER(2)[5:11], CLOSE_ROUND_BRACKET[5:12], SEMICOLON[5:13], IDENTIFIER(print)[6:1], OPEN_ROUND_BRACKET[6:6], NUMBER(1)[6:7], GREATER_THAN[6:9], NUMBER(1)[6:11], CLOSE_ROUND_BRACKET[6:12], SEMICOLON[6:13], IDENTIFIER(print)[7:1], OPEN_ROUND_BRACKET[7:6], NUMBER(2)[7:7], GREATER_THAN[7:9], NUMBER(1)[7:11], CLOSE_ROUND_BRACKET[7:12], SEMICOLON[7:13], IDENTIFIER(print)[9:1], OPEN_ROUND_BRACKET[9:6], NUMBER(1)[9:7], LESS_THAN[9:9], EQUALS[9:10], NUMBER(2)[9:12], CLOSE_ROUND_BRACKET[9:13], SEMICOLON[9:14], IDENTIFIER(print)[10:1], OPEN_ROUND_BRACKET[10:6], NUMBER(1)[10:7], LESS_THAN[10:9], EQUALS[10:10], NUMBER(1)[10:12], CLOSE_ROUND_BRACKET[10:13], SEMICOLON[10:14], IDENTIFIER(print)[11:1], OPEN_ROUND_BRACKET[11:6], NUMBER(2)[11:7], LESS_THAN[11:9], EQUALS[11:10], NUMBER(1)[11:12], CLOSE_ROUND_BRACKET[11:13], SEMICOLON[11:14], IDENTIFIER(print)[13:1], OPEN_ROUND_BRACKET[13:6], NUMBER(1)[13:7], GREATER_THAN[13:9], EQUALS[13:10], NUMBER(2)[13:12], CLOSE_ROUND_BRACKET[13:13], SEMICOLON[13:14], IDENTIFIER(print)[14:1], OPEN_ROUND_BRACKET[14:6], NUMBER(1)[14:7], GREATER_THAN[14:9], EQUALS[14:10], NUMBER(1)[14:12], CLOSE_ROUND_BRACKET[14:13], SEMICOLON[14:14], IDENTIFIER(print)[15:1], OPEN_ROUND_BRACKET[15:6], NUMBER(2)[15:7], GREATER_THAN[15:9], EQUALS[15:10], NUMBER(1)[15:12], CLOSE_ROUND_BRACKET[15:13], SEMICOLON[15:14], IDENTIFIER(print)[17:1], OPEN_ROUND_BRACKET[17:6], NUMBER(1)[17:7], EQUALS[17:9], EQUALS[17:10], NUMBER(2)[17:12], CLOSE_ROUND_BRACKET[17:13], SEMICOLON[17:14], IDENTIFIER(print)[18:1], OPEN_ROUND_BRACKET[18:6], NUMBER(1)[18:7], EQUALS[18:9], EQUALS[18:10], NUMBER(1)[18:12], CLOSE_ROUND_BRACKET[18:13], SEMICOLON[18:14], IDENTIFIER(print)[19:1], OPEN_ROUND_BRACKET[19:6], NUMBER(2)[19:7], EQUALS[19:9], EQUALS[19:10], NUMBER(1)[19:12], CLOSE_ROUND_BRACKET[19:13], SEMICOLON[19:14], IDENTIFIER(print)[21:1], OPEN_ROUND_BRACKET[21:6], NUMBER(1)[21:7], EXCLAMATION_MARK[21:9], EQUALS[21:10], NUMBER(2)[21:12], CLOSE_ROUND_BRACKET[21:13], SEMICOLON[21:14], IDENTIFIER(print)[22:1], OPEN_ROUND_BRACKET[22:6], NUMBER(1)[22:7], EXCLAMATION_MARK[22:9], EQUALS[22:10], NUMBER(1)[22:12], CLOSE_ROUND_BRACKET[22:13], SEMICOLON[22:14], IDENTIFIER(print)[23:1], OPEN_ROUND_BRACKET[23:6], NUMBER(2)[23:7], EXCLAMATION_MARK[23:9], EQUALS[23:10], NUMBER(1)[23:12], CLOSE_ROUND_BRACKET[23:13], SEMICOLON[23:14], EOF[23:15]]",
                "{\n\tcall['print']((#1 < #2))\n\tcall['print']((#1 < #1))\n\tcall['print']((#2 < #1))\n\tcall['print']((#1 > #2))\n\tcall['print']((#1 > #1))\n\tcall['print']((#2 > #1))\n\tcall['print']((#1 <= #2))\n\tcall['print']((#1 <= #1))\n\tcall['print']((#2 <= #1))\n\tcall['print']((#1 >= #2))\n\tcall['print']((#1 >= #1))\n\tcall['print']((#2 >= #1))\n\tcall['print']((#1 == #2))\n\tcall['print']((#1 == #1))\n\tcall['print']((#2 == #1))\n\tcall['print']((#1 != #2))\n\tcall['print']((#1 != #1))\n\tcall['print']((#2 != #1))\n}",
                "true\nfalse\nfalse\nfalse\nfalse\ntrue\ntrue\ntrue\nfalse\nfalse\ntrue\ntrue\nfalse\ntrue\nfalse\ntrue\nfalse\ntrue\n",
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