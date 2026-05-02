package de.bzembrodt.parser;

import de.bzembrodt.lexer.Token;
import de.bzembrodt.lexer.TokenType;
import de.bzembrodt.parser.node.*;

import java.util.*;

public class Parser {

    public AstNode parse(List<Token> tokens) {

        TokenList tokenList = new TokenList(tokens);
        return parseProgram(tokenList);
    }

    private AstNode parseProgram(TokenList tokenList) {
        List<AstNode> statements = new ArrayList<>();
        while (tokenList.getToken().type != TokenType.EOF) {
            statements.add(parseStatement(tokenList));
        }
        assert tokenList.getToken().type == TokenType.EOF;
        return new ProgramNode(statements);
    }

    private AstNode parseStatement(TokenList tokenList) {
        AstNode statement;
        if (tokenList.getToken().type == TokenType.IDENTIFIER && (tokenList.getToken().value.equals(Keywords.CONST.name) || tokenList.getToken().value.equals(Keywords.VAR.name))) {
            statement = parseVariableDeclaration(tokenList);
        } else {
            statement = parseArithmeticExpression(tokenList);
        }
        assert tokenList.getToken().type == TokenType.SEMICOLON;
        tokenList.advance();
        return statement;
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
        //TODO For now we only support ints, extend to other types later
        assert type.equals("int");
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
                assert !Keywords.ALL_KEYWORDS.contains(token.value);
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

        AstNode param = parseArithmeticExpression(tokenList);

        assert tokenList.getToken().type == TokenType.CLOSE_ROUND_BRACKET;
        tokenList.advance();

        return new FunctionCallNode(name, List.of(param), token);
    }
}
