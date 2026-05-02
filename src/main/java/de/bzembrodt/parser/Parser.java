package de.bzembrodt.parser;

import de.bzembrodt.lexer.Token;
import de.bzembrodt.lexer.TokenType;
import de.bzembrodt.parser.node.AstNode;
import de.bzembrodt.parser.node.BinaryOperation;
import de.bzembrodt.parser.node.FunctionCallNode;
import de.bzembrodt.parser.node.NumberNode;

import java.util.List;

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

    private AstNode parseArithmeticExpression(TokenList tokenList) {
        AstNode lhs = parsePrimaryExpression(tokenList);
        Token token = tokenList.getToken();
        if (token.type == TokenType.PLUS) {
            tokenList.advance();

            AstNode rhs = parsePrimaryExpression(tokenList);
            return new BinaryOperation(lhs, BinaryOperation.Operator.PLUS, rhs, token);
        }
        return lhs;
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
