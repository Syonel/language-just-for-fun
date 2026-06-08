package de.bzembrodt.parser;

import de.bzembrodt.lexer.Token;
import de.bzembrodt.lexer.TokenType;
import de.bzembrodt.parser.node.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Parser {

    private static final Set<String> RESERVED_WORDS = Stream.concat(Keyword.ALL_KEYWORDS.stream(), BuildinType.ALL_BUILDIN_TYPES.stream()).collect(Collectors.toSet());

    public AstNode parse(List<Token> tokens) {

        TokenList tokenList = new TokenList(tokens);
        return parseProgram(tokenList);
    }

    private AstNode parseProgram(TokenList tokenList) {
        List<AstNode> statements = new ArrayList<>();
        while (tokenList.getToken().type != TokenType.EOF) {
            AstNode statement = parseStatement(tokenList, true);
            if (statement != null) {
                statements.add(statement);
            }
        }
        assert tokenList.getToken().type == TokenType.EOF;
        return new StatementsNode(statements);
    }

    private AstNode parseStatement(TokenList tokenList, boolean needsSemicolon) {
        //Empty Statements are fine
        if (tokenList.getToken().type == TokenType.SEMICOLON) {
            tokenList.advance();
            return null;
        }
        AstNode statement;
        if (tokenList.getToken().type == TokenType.IDENTIFIER && (tokenList.getToken().value.equals(Keyword.CONST.name) || tokenList.getToken().value.equals(Keyword.VAR.name))) {
            statement = parseVariableDeclaration(tokenList);
        } else if (tokenList.getToken().type == TokenType.IDENTIFIER && tokenList.getToken().value.equals(Keyword.FN.name)) {
            statement = parseFunctionDefinition(tokenList);
            needsSemicolon = false;
        } else if (tokenList.getToken().type == TokenType.IDENTIFIER && tokenList.getToken().value.equals(Keyword.RETURN.name)) {
            //TODO Return is only valid within a function
            Token token = tokenList.getToken();
            tokenList.advance();
            if (tokenList.getToken().type == TokenType.SEMICOLON) {
                statement = new ReturnNode(Optional.empty(), token);
            } else {
                statement = new ReturnNode(Optional.of(parseArithmeticExpression(tokenList)), token);
            }
        } else if (tokenList.getToken().type == TokenType.IDENTIFIER && tokenList.getToken().value.equals(Keyword.IF.name)) {
            statement = parseIf(tokenList);
            needsSemicolon = false;

        } else if (tokenList.getToken().type == TokenType.IDENTIFIER && tokenList.getToken().value.equals(Keyword.FOR.name)) {
            statement = parseFor(tokenList);
            needsSemicolon = false;

        } else if (tokenList.getToken().type == TokenType.IDENTIFIER && tokenList.getToken().value.equals(Keyword.BREAK.name)) {
            statement = new BreakNode(tokenList.getToken());
            tokenList.advance();
        } else if (tokenList.getToken().type == TokenType.IDENTIFIER && tokenList.getToken().value.equals(Keyword.CONTINUE.name)) {
            statement = new ContinueNode(tokenList.getToken());
            tokenList.advance();
        } else {
            statement = parseArithmeticExpression(tokenList);
        }
        if (needsSemicolon) {
            assert tokenList.getToken().type == TokenType.SEMICOLON;
            tokenList.advance();
        }
        return statement;
    }

    private AstNode parseIf(TokenList tokenList) {
        Token token = tokenList.getToken();
        tokenList.advance();
        AstNode condition = parseArithmeticExpression(tokenList);
        List<AstNode> trueBlock = parseBlock(tokenList);
        Optional<List<AstNode>> falseBlock = Optional.empty();
        if (tokenList.getToken().type == TokenType.IDENTIFIER && tokenList.getToken().value.equals(Keyword.ELSE.name)) {
            tokenList.advance();
            falseBlock = Optional.of(parseBlock(tokenList));
        }
        return new IfNode(condition, new StatementsNode(trueBlock), falseBlock.map(StatementsNode::new), token);
    }

    private AstNode parseFor(TokenList tokenList) {
        Token token = tokenList.getToken();
        tokenList.advance();
        List<AstNode> header = new ArrayList<>();
        int nodesFound = 0;
        for (; nodesFound < 3; nodesFound++) {
            if (tokenList.getToken().type == TokenType.OPEN_CURLY_BRACKET) {
                break;
            }
            header.add(parseStatement(tokenList, false));
            assert tokenList.getToken().type == TokenType.OPEN_CURLY_BRACKET || tokenList.getToken().type == TokenType.SEMICOLON;
            if (tokenList.getToken().type == TokenType.SEMICOLON) {
                tokenList.advance();
            }
        }
        Optional<AstNode> init = Optional.empty();
        Optional<AstNode> condition = Optional.empty();
        Optional<AstNode> step = Optional.empty();

        if (nodesFound == 1) {
            condition = Optional.of(header.get(0));
        } else if (nodesFound == 2) {
            init = Optional.of(header.get(0));
            condition = Optional.of(header.get(1));
        } else if (nodesFound == 3) {
            init = Optional.of(header.get(0));
            condition = Optional.of(header.get(1));
            step = Optional.of(header.get(2));
        }

        List<AstNode> body = parseBlock(tokenList);
        return new ForNode(init, condition, step, new StatementsNode(body), token);
    }

    private AstNode parseFunctionDefinition(TokenList tokenList) {
        Token token = tokenList.getToken();
        assert token.type == TokenType.IDENTIFIER && token.value.equals(Keyword.FN.name);
        tokenList.advance();

        assert tokenList.getToken().type == TokenType.IDENTIFIER;
        String name = tokenList.getToken().value;
        tokenList.advance();

        assert tokenList.getToken().type == TokenType.OPEN_ROUND_BRACKET;
        tokenList.advance();

        List<FunctionDefinitionNode.Argument> arguments = new ArrayList<>();

        while (tokenList.getToken().type != TokenType.CLOSE_ROUND_BRACKET) {
            assert tokenList.getToken().type == TokenType.IDENTIFIER;
            String argName = tokenList.getToken().value;
            tokenList.advance();

            assert tokenList.getToken().type == TokenType.COLON;
            tokenList.advance();

            assert tokenList.getToken().type == TokenType.IDENTIFIER;
            String argType = tokenList.getToken().value;
            tokenList.advance();
            arguments.add(new FunctionDefinitionNode.Argument(argName, argType));

            if (tokenList.getToken().type == TokenType.COMMA) {
                tokenList.advance();
            }
        }

        assert tokenList.getToken().type == TokenType.CLOSE_ROUND_BRACKET;
        tokenList.advance();

        assert tokenList.getToken().type == TokenType.COLON;
        tokenList.advance();

        assert tokenList.getToken().type == TokenType.IDENTIFIER;
        String returnType = tokenList.getToken().value;
        tokenList.advance();

        List<AstNode> statements = parseBlock(tokenList);
        //Add an empty return statement in case the function did not have one
        statements.add(new ReturnNode(Optional.empty(), tokenList.getToken()));

        return new FunctionDefinitionNode(name, returnType, arguments, new StatementsNode(statements), token);
    }

    private List<AstNode> parseBlock(TokenList tokenList) {
        assert tokenList.getToken().type == TokenType.OPEN_CURLY_BRACKET;
        tokenList.advance();

        List<AstNode> statements = new ArrayList<>();
        while (tokenList.getToken().type != TokenType.EOF && tokenList.getToken().type != TokenType.CLOSE_CURLY_BRACKET) {
            AstNode statement = parseStatement(tokenList, true);
            if (statement != null) {
                statements.add(statement);
            }
        }
        assert tokenList.getToken().type == TokenType.CLOSE_CURLY_BRACKET;
        tokenList.advance();
        return statements;
    }

    private AstNode parseVariableDeclaration(TokenList tokenList) {
        Token token = tokenList.getToken();
        assert token.type == TokenType.IDENTIFIER && (token.value.equals(Keyword.CONST.name) || token.value.equals(Keyword.VAR.name));
        boolean isConst = token.value.equals(Keyword.CONST.name);
        tokenList.advance();

        assert tokenList.getToken().type == TokenType.IDENTIFIER;
        String name = tokenList.getToken().value;
        tokenList.advance();

        assert tokenList.getToken().type == TokenType.COLON;
        tokenList.advance();

        assert tokenList.getToken().type == TokenType.IDENTIFIER;
        String type = tokenList.getToken().value;
        tokenList.advance();

        boolean isArray = false;
        long arraySize = -1;
        if (tokenList.getToken().type == TokenType.OPEN_SQUARE_BRACKET) {
            tokenList.advance();
            isArray = true;
            if (tokenList.getToken().type == TokenType.NUMBER) {
                arraySize = Long.parseLong(tokenList.getToken().value);
                tokenList.advance();
            }
            assert tokenList.getToken().type == TokenType.CLOSE_SQUARE_BRACKET;
            tokenList.advance();
        }

        Optional<AstNode> initializer = Optional.empty();
        if (tokenList.getToken().type == TokenType.EQUALS) {
            tokenList.advance();
            initializer = Optional.of(parseArithmeticExpression(tokenList));
        }

        if (isArray && arraySize == -1) {
            assert initializer.isPresent() && initializer.get() instanceof ArrayNode;
            arraySize = ((ArrayNode) initializer.get()).values.size();
        }
        if (isArray) {
            type += "[" + arraySize + "]";
        }
        // Constants have to be initialized directly
        assert !isConst || initializer.isPresent();
        return new VariableDeclarationNode(isConst, name, type, initializer, token);
    }

    private record OperatorAndToken(BinaryOperation.Operator op, Token token) {
    }

    private static final Set<TokenType> OPERATORS = EnumSet.of(TokenType.PLUS, TokenType.MINUS, TokenType.MULTIPLY, TokenType.DIVIDE, TokenType.EQUALS, TokenType.AMPERSAND, TokenType.PIPE, TokenType.LESS_THAN, TokenType.GREATER_THAN, TokenType.EXCLAMATION_MARK);

    private AstNode parseArithmeticExpression(TokenList tokenList) {
        Stack<AstNode> operands = new Stack<>();
        Stack<OperatorAndToken> operators = new Stack<>();

        operands.push(parsePrimaryExpression(tokenList));

        while (OPERATORS.contains(tokenList.getToken().type)) {

            Token token = tokenList.getToken();
            BinaryOperation.Operator binOp = switch (token.type) {
                case PLUS -> BinaryOperation.Operator.PLUS;
                case MINUS -> BinaryOperation.Operator.MINUS;
                case MULTIPLY -> BinaryOperation.Operator.MULTIPLY;
                case DIVIDE -> BinaryOperation.Operator.DIVIDE;
                case AMPERSAND -> {
                    assert tokenList.peek().type == TokenType.AMPERSAND;
                    tokenList.advance();
                    yield BinaryOperation.Operator.AND;
                }
                case PIPE -> {
                    assert tokenList.peek().type == TokenType.PIPE;
                    tokenList.advance();
                    yield BinaryOperation.Operator.OR;
                }
                case LESS_THAN -> {
                    if (tokenList.peek().type == TokenType.EQUALS) {
                        tokenList.advance();
                        yield BinaryOperation.Operator.LESS_EQUAL;
                    }
                    yield BinaryOperation.Operator.LESS_THAN;
                }
                case GREATER_THAN -> {
                    if (tokenList.peek().type == TokenType.EQUALS) {
                        tokenList.advance();
                        yield BinaryOperation.Operator.GREATER_EQUAL;
                    }
                    yield BinaryOperation.Operator.GREATER_THAN;
                }
                case EQUALS -> {
                    if (tokenList.peek().type == TokenType.EQUALS) {
                        tokenList.advance();
                        yield BinaryOperation.Operator.EQUAL;
                    }
                    yield BinaryOperation.Operator.ASSIGN;
                }
                case EXCLAMATION_MARK -> {
                    assert tokenList.peek().type == TokenType.EQUALS;
                    tokenList.advance();
                    yield BinaryOperation.Operator.NOT_EQUAL;
                }
                default -> null;
            };
            assert binOp != null;
            tokenList.advance();

            while (!operators.isEmpty() && operators.peek().op.precedence > binOp.precedence) {
                joinBinaryOperation(operands, operators);
            }

            operators.push(new OperatorAndToken(binOp, token));

            operands.push(parsePrimaryExpression(tokenList));
        }

        while (!operators.isEmpty()) {
            joinBinaryOperation(operands, operators);
        }

        assert operands.size() == 1;
        return operands.pop();
    }

    private static void joinBinaryOperation(Stack<AstNode> operands, Stack<OperatorAndToken> operators) {
        AstNode rhs = operands.pop();
        AstNode lhs = operands.pop();
        OperatorAndToken opAndToken = operators.pop();
        if (opAndToken.op == BinaryOperation.Operator.ASSIGN) {
            assert lhs instanceof NameLookupNode || lhs instanceof ArrayAccessNode;

            if (lhs instanceof NameLookupNode lhsName) {
                operands.push(new VariableAssignmentNode(lhsName.name, rhs, opAndToken.token));
            } else if (lhs instanceof ArrayAccessNode lhsArray) {
                assert lhsArray.array instanceof NameLookupNode;
                operands.push(new ArrayIndexAssignmentNode(((NameLookupNode) lhsArray.array).name, lhsArray.index, rhs, opAndToken.token));
            }
        } else {
            operands.push(new BinaryOperation(lhs, opAndToken.op, rhs, opAndToken.token));
        }
    }

    private AstNode parsePrimaryExpression(TokenList tokenList) {
        AstNode expression = null;

        Token token = tokenList.getToken();
        switch (token.type) {
            case IDENTIFIER -> {
                if (token.value.equals(Keyword.TRUE.name) || token.value.equals(Keyword.FALSE.name)) {
                    expression = new BoolNode(token.value.equals(Keyword.TRUE.name), token);
                    tokenList.advance();
                    break;
                }
                assert !RESERVED_WORDS.contains(token.value);
                if (tokenList.peek().type == TokenType.OPEN_ROUND_BRACKET) {
                    expression = parseFunctionCall(tokenList);
                    break;
                }
                expression = new NameLookupNode(token.value, token);
                tokenList.advance();
            }
            case NUMBER -> {
                expression = new NumberNode(Long.parseLong(token.value), token);
                tokenList.advance();
            }
            case STRING -> {
                expression = new StringNode(parseString(token.value), token);
                tokenList.advance();
            }
            case EXCLAMATION_MARK -> {
                tokenList.advance();
                expression = new UnaryOperation(UnaryOperation.Operator.NOT, parsePrimaryExpression(tokenList), token);
            }
            case MINUS -> {
                tokenList.advance();
                expression = new UnaryOperation(UnaryOperation.Operator.NEGATE, parsePrimaryExpression(tokenList), token);
            }
            case OPEN_SQUARE_BRACKET -> {
                tokenList.advance();
                List<AstNode> nodes = new ArrayList<>();
                while (tokenList.getToken().type != TokenType.CLOSE_SQUARE_BRACKET) {
                    nodes.add(parseArithmeticExpression(tokenList));
                    assert tokenList.getToken().type == TokenType.CLOSE_SQUARE_BRACKET || tokenList.getToken().type == TokenType.COMMA;
                    if (tokenList.getToken().type == TokenType.COMMA) {
                        tokenList.advance();
                    }
                }
                tokenList.advance();
                expression = new ArrayNode(nodes, token);
            }
        }
        if (tokenList.getToken().type == TokenType.OPEN_SQUARE_BRACKET) {
            token = tokenList.getToken();
            tokenList.advance();
            expression = new ArrayAccessNode(expression, parseArithmeticExpression(tokenList), token);
            assert tokenList.getToken().type == TokenType.CLOSE_SQUARE_BRACKET;
            tokenList.advance();
        }

        assert expression != null;
        return expression;
    }

    private FunctionCallNode parseFunctionCall(TokenList tokenList) {
        assert tokenList.getToken().type == TokenType.IDENTIFIER;
        String name = tokenList.getToken().value;
        Token token = tokenList.getToken();
        tokenList.advance();
        assert tokenList.getToken().type == TokenType.OPEN_ROUND_BRACKET;
        tokenList.advance();

        List<AstNode> params = new ArrayList<>();
        while (tokenList.getToken().type != TokenType.CLOSE_ROUND_BRACKET) {
            params.add(parseArithmeticExpression(tokenList));

            if (tokenList.getToken().type == TokenType.COMMA) {
                tokenList.advance();
            }
        }

        assert tokenList.getToken().type == TokenType.CLOSE_ROUND_BRACKET;
        tokenList.advance();

        return new FunctionCallNode(name, params, token);
    }

    private String parseString(String in) {
        StringBuilder sb = new StringBuilder();
        boolean escaped = false;
        for (char c : in.toCharArray()) {
            if (escaped) {
                sb.append(switch (c) {
                    case 't' -> '\t';
                    case 'b' -> '\b';
                    case 'n' -> '\n';
                    case 'r' -> '\r';
                    case 'f' -> '\f';
                    case '\"' -> '\"';
                    case '\\' -> '\\';
                    default -> {
                        assert false;
                        yield '\0';
                    }
                });
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                continue;
            }
            sb.append(c);
        }
        String result = sb.toString();
        assert result.length() >= 2 && result.charAt(0) == '"' && result.charAt(result.length() - 1) == '"';
        return result.substring(1, result.length() - 1);
    }
}
