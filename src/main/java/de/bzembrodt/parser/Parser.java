package de.bzembrodt.parser;

import de.bzembrodt.lexer.Token;
import de.bzembrodt.lexer.TokenType;
import de.bzembrodt.parser.node.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Parser {

    private static final Set<String> RESERVED_WORDS = Stream.concat(Keywords.ALL_KEYWORDS.stream(), BuildinTypes.ALL_BUILDIN_TYPES.stream()).collect(Collectors.toSet());

    public AstNode parse(List<Token> tokens) {

        TokenList tokenList = new TokenList(tokens);
        return parseProgram(tokenList);
    }

    private AstNode parseProgram(TokenList tokenList) {
        List<AstNode> statements = new ArrayList<>();
        while (tokenList.getToken().type != TokenType.EOF) {
            AstNode statement = parseStatement(tokenList);
            if (statement != null) {
                statements.add(statement);
            }
        }
        assert tokenList.getToken().type == TokenType.EOF;
        return new StatementsNode(statements);
    }

    private AstNode parseStatement(TokenList tokenList) {
        //Empty Statements are fine
        if (tokenList.getToken().type == TokenType.SEMICOLON) {
            tokenList.advance();
            return null;
        }
        boolean needsSemicolon = true;
        AstNode statement;
        if (tokenList.getToken().type == TokenType.IDENTIFIER && (tokenList.getToken().value.equals(Keywords.CONST.name) || tokenList.getToken().value.equals(Keywords.VAR.name))) {
            statement = parseVariableDeclaration(tokenList);
        } else if (tokenList.getToken().type == TokenType.IDENTIFIER && tokenList.getToken().value.equals(Keywords.FN.name)) {
            statement = parseFunctionDefinition(tokenList);
            needsSemicolon = false;
        } else if (tokenList.getToken().type == TokenType.IDENTIFIER && tokenList.getToken().value.equals(Keywords.RETURN.name)) {
            //TODO Return is only valid within a function
            Token token = tokenList.getToken();
            tokenList.advance();
            if (tokenList.getToken().type == TokenType.SEMICOLON) {
                statement = new ReturnNode(Optional.empty(), token);
            } else {
                statement = new ReturnNode(Optional.of(parseArithmeticExpression(tokenList)), token);
            }
        } else {
            statement = parseArithmeticExpression(tokenList);
        }
        if (needsSemicolon) {
            assert tokenList.getToken().type == TokenType.SEMICOLON;
            tokenList.advance();
        }
        return statement;
    }

    private AstNode parseFunctionDefinition(TokenList tokenList) {
        Token token = tokenList.getToken();
        assert token.type == TokenType.IDENTIFIER && token.value.equals(Keywords.FN.name);
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

        assert tokenList.getToken().type == TokenType.OPEN_CURLY_BRACKET;
        tokenList.advance();

        List<AstNode> statements = new ArrayList<>();
        while (tokenList.getToken().type != TokenType.EOF && tokenList.getToken().type != TokenType.CLOSE_CURLY_BRACKET) {
            AstNode statement = parseStatement(tokenList);
            if (statement != null) {
                statements.add(statement);
            }
        }
        assert tokenList.getToken().type == TokenType.CLOSE_CURLY_BRACKET;
        //Add an empty return statement in case the function did not have one
        statements.add(new ReturnNode(Optional.empty(), tokenList.getToken()));
        tokenList.advance();

        return new FunctionDefinitionNode(name, returnType, arguments, new StatementsNode(statements), token);
    }

    private AstNode parseVariableDeclaration(TokenList tokenList) {
        Token token = tokenList.getToken();
        assert token.type == TokenType.IDENTIFIER && (token.value.equals(Keywords.CONST.name) || token.value.equals(Keywords.VAR.name));
        boolean isConst = token.value.equals(Keywords.CONST.name);
        tokenList.advance();

        assert tokenList.getToken().type == TokenType.IDENTIFIER;
        String name = tokenList.getToken().value;
        tokenList.advance();

        assert tokenList.getToken().type == TokenType.COLON;
        tokenList.advance();

        assert tokenList.getToken().type == TokenType.IDENTIFIER;
        String type = tokenList.getToken().value;
        tokenList.advance();

        Optional<AstNode> initializer = Optional.empty();
        if (tokenList.getToken().type == TokenType.EQUALS) {
            tokenList.advance();
            initializer = Optional.of(parseArithmeticExpression(tokenList));
        }
        // Constants have to be initialized directly
        assert !isConst || initializer.isPresent();
        return new VariableDeclarationNode(isConst, name, type, initializer, token);
    }

    private record OperatorAndToken(BinaryOperation.Operator op, Token token) {
    }

    private static final Set<TokenType> OPERATORS = EnumSet.of(TokenType.PLUS, TokenType.MINUS, TokenType.MULTIPLY, TokenType.DIVIDE, TokenType.EQUALS);

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
                case EQUALS -> BinaryOperation.Operator.EQUALS;
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
        if (opAndToken.op == BinaryOperation.Operator.EQUALS) {
            assert lhs instanceof NameLookupNode;
            NameLookupNode lhsName = (NameLookupNode) lhs;
            operands.push(new VariableAssignmentNode(lhsName.name, rhs, opAndToken.token));
        } else {
            operands.push(new BinaryOperation(lhs, opAndToken.op, rhs, opAndToken.token));
        }
    }

    private AstNode parsePrimaryExpression(TokenList tokenList) {
        AstNode expression = null;

        Token token = tokenList.getToken();
        switch (token.type) {
            case IDENTIFIER -> {
                if (token.value.equals(Keywords.TRUE.name) || token.value.equals(Keywords.FALSE.name)) {
                    expression = new BoolNode(token.value.equals(Keywords.TRUE.name), token);
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
}
