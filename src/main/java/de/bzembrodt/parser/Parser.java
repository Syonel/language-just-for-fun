package de.bzembrodt.parser;

import de.bzembrodt.lexer.Token;
import de.bzembrodt.lexer.TokenType;
import de.bzembrodt.parser.node.AstNode;
import de.bzembrodt.parser.node.BinaryOperation;
import de.bzembrodt.parser.node.FunctionCallNode;
import de.bzembrodt.parser.node.NumberNode;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class Parser {

    public AstNode parse(List<Token> tokens) {

        TokenList tokenList = new TokenList(tokens);
        AstNode statement = parseStatement(tokenList);
        assert tokenList.getToken().type == TokenType.EOF;
        return statement;
    }

    private AstNode parseStatement(TokenList tokenList) {
        AstNode statement = parseArithmeticExpression(tokenList);
        assert tokenList.getToken().type == TokenType.SEMICOLON;
        tokenList.advance();
        return statement;
    }

    private record OperatorAndToken(BinaryOperation.Operator op, Token token) {
    }

    private static final Set<TokenType> OPERATORS = EnumSet.of(TokenType.PLUS, TokenType.MINUS, TokenType.MULTIPLY, TokenType.DIVIDE);

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
                default -> null;
            };
            assert binOp != null;
            tokenList.advance();

            while (!operators.isEmpty() && operators.peek().op.precedence > binOp.precedence) {
                AstNode rhs = operands.pop();
                AstNode lhs = operands.pop();
                OperatorAndToken opAndToken = operators.pop();
                operands.push(new BinaryOperation(lhs, opAndToken.op, rhs, opAndToken.token));
            }

            operators.push(new OperatorAndToken(binOp, token));

            operands.push(parsePrimaryExpression(tokenList));
        }

        while (!operators.isEmpty()) {
            AstNode rhs = operands.pop();
            AstNode lhs = operands.pop();
            OperatorAndToken opAndToken = operators.pop();
            operands.push(new BinaryOperation(lhs, opAndToken.op, rhs, opAndToken.token));
        }

        assert operands.size() == 1;
        return operands.pop();
    }

    private AstNode parsePrimaryExpression(TokenList tokenList) {
        AstNode expression = null;

        Token token = tokenList.getToken();
        switch (token.type) {
            case IDENTIFIER -> {
                if (tokenList.peek().type == TokenType.OPEN_ROUND_BRACKET) {
                    expression = parseFunctionCall(tokenList);
                }
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

        AstNode param = parseArithmeticExpression(tokenList);

        assert tokenList.getToken().type == TokenType.CLOSE_ROUND_BRACKET;
        tokenList.advance();

        return new FunctionCallNode(name, List.of(param), token);
    }
}
