package parser;

import ast.command.AssignmentCommand;
import ast.command.Command;
import ast.command.IfCommand;
import ast.command.WhileCommand;
import ast.declaration.FunctionDeclaration;
import ast.declaration.TopLevelDeclaration;
import ast.declaration.VariableDeclaration;
import ast.expression.BinaryExpression;
import ast.expression.Expression;
import ast.expression.FunctionCallExpression;
import ast.expression.LiteralExpression;
import ast.expression.VariableExpression;
import ast.program.Program;
import exception.ParserException;
import java.util.ArrayList;
import java.util.List;
import model.Token;
import model.TokenType;

public class Parser {

    private final List<Token> tokens;

    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    // programa ::= decl* 'main' '{' cmd* 'return' exp ';' '}' EOF

    public Program parse() {

        List<TopLevelDeclaration> declarations = new ArrayList<>();

        while (check(TokenType.VAR)
                || check(TokenType.FUN)) {

            declarations.add(
                    parseTopLevelDeclaration());
        }

        Token mainToken = consume(TokenType.MAIN);

        consume(TokenType.CHAVE_ESQ);

        List<Command> commands = new ArrayList<>();

        while (!check(TokenType.RETURN)) {

            ensureNotAtEnd(
                    "Esperado 'return' no bloco principal.");

            if (check(TokenType.CHAVE_DIR)) {

                throw error(
                        currentToken(),
                        "Esperado 'return' antes de '}'.");
            }

            commands.add(
                    parseCommand());
        }

        consume(TokenType.RETURN);

        Expression expression = parseExpression();

        consume(TokenType.PONTO_VIRGULA);
        consume(TokenType.CHAVE_DIR);
        consume(TokenType.EOF);

        return new Program(
                declarations,
                commands,
                expression,
                mainToken.getLine(),
                mainToken.getColumn());
    }

    // decl ::= vardecl | fundecl

    private TopLevelDeclaration parseTopLevelDeclaration() {

        return switch (currentToken().getType()) {

            case VAR -> parseVariableDeclaration();

            case FUN -> parseFunctionDeclaration();

            default -> throw error(
                    currentToken(),
                    "Declaração esperada.");
        };
    }

    // vardecl ::= 'var' IDENTIFICADOR '=' exp ';'

    private VariableDeclaration parseVariableDeclaration() {

        Token varToken = consume(TokenType.VAR);
        Token identifier = consume(TokenType.IDENTIFICADOR);

        consume(TokenType.ATRIB);

        Expression value = parseExpression();

        consume(TokenType.PONTO_VIRGULA);

        return new VariableDeclaration(
                identifier.getLexeme(),
                value,
                varToken.getLine(),
                varToken.getColumn());
    }

    // fundecl ::= 'fun' IDENTIFICADOR '(' arglist? ')'
    //             '{' vardecl* cmd* 'return' exp ';' '}'

    private FunctionDeclaration parseFunctionDeclaration() {

        Token funToken = consume(TokenType.FUN);
        Token identifier = consume(TokenType.IDENTIFICADOR);

        consume(TokenType.PAREN_ESQ);

        List<String> parameters = parseFormalParameters();

        consume(TokenType.PAREN_DIR);
        consume(TokenType.CHAVE_ESQ);

        List<VariableDeclaration> localDeclarations = new ArrayList<>();

        while (check(TokenType.VAR)) {

            localDeclarations.add(
                    parseVariableDeclaration());
        }

        List<Command> commands = new ArrayList<>();

        while (!check(TokenType.RETURN)) {

            ensureNotAtEnd(
                    "Esperado 'return' na função '"
                            + identifier.getLexeme()
                            + "'.");

            if (check(TokenType.CHAVE_DIR)) {

                throw error(
                        currentToken(),
                        "Esperado 'return' antes do fim da função '"
                                + identifier.getLexeme()
                                + "'.");
            }

            commands.add(
                    parseCommand());
        }

        consume(TokenType.RETURN);

        Expression expression = parseExpression();

        consume(TokenType.PONTO_VIRGULA);
        consume(TokenType.CHAVE_DIR);

        return new FunctionDeclaration(
                identifier.getLexeme(),
                parameters,
                localDeclarations,
                commands,
                expression,
                funToken.getLine(),
                funToken.getColumn());
    }

    // arglist ::= IDENTIFICADOR (',' IDENTIFICADOR)*

    private List<String> parseFormalParameters() {

        List<String> parameters = new ArrayList<>();

        if (check(TokenType.PAREN_DIR)) {
            return parameters;
        }

        parameters.add(
                consume(TokenType.IDENTIFICADOR)
                        .getLexeme());

        while (match(TokenType.VIRGULA)) {

            parameters.add(
                    consume(TokenType.IDENTIFICADOR)
                            .getLexeme());
        }

        return parameters;
    }

    // cmd ::= if | while | atrib | atrib_composta
    //       | incremento | decremento

    private Command parseCommand() {

        return switch (currentToken().getType()) {

            case IF -> parseIf();

            case WHILE -> parseWhile();

            case IDENTIFICADOR -> parseVariableCommand();

            default -> throw error(
                    currentToken(),
                    "Comando esperado.");
        };
    }

    // if ::= 'if' exp '{' cmd* '}' 'else' '{' cmd* '}'

    private IfCommand parseIf() {

        Token ifToken = consume(TokenType.IF);

        Expression condition = parseExpression();

        consume(TokenType.CHAVE_ESQ);

        List<Command> thenCommands = parseCommandBlock();

        consume(TokenType.CHAVE_DIR);
        consume(TokenType.ELSE);
        consume(TokenType.CHAVE_ESQ);

        List<Command> elseCommands = parseCommandBlock();

        consume(TokenType.CHAVE_DIR);

        return new IfCommand(
                condition,
                thenCommands,
                elseCommands,
                ifToken.getLine(),
                ifToken.getColumn());
    }

    // while ::= 'while' exp '{' cmd* '}'

    private WhileCommand parseWhile() {

        Token whileToken = consume(TokenType.WHILE);

        Expression condition = parseExpression();

        consume(TokenType.CHAVE_ESQ);

        List<Command> commands = parseCommandBlock();

        consume(TokenType.CHAVE_DIR);

        return new WhileCommand(
                condition,
                commands,
                whileToken.getLine(),
                whileToken.getColumn());
    }

    // atrib ::= IDENTIFICADOR '=' exp ';'
    //
    // atrib_composta ::= IDENTIFICADOR op_atrib exp ';'
    // op_atrib ::= '+=' | '-=' | '*=' | '/=' | '%=' | '<<=' | '>>='
    //
    // incremento ::= IDENTIFICADOR '++' ';'
    // decremento ::= IDENTIFICADOR '--' ';'

    private AssignmentCommand parseVariableCommand() {

        Token identifier = consume(TokenType.IDENTIFICADOR);

        if (match(TokenType.ATRIB)) {

            Expression value = parseExpression();

            consume(TokenType.PONTO_VIRGULA);

            return new AssignmentCommand(
                    identifier.getLexeme(),
                    value,
                    identifier.getLine(),
                    identifier.getColumn());
        }

        if (isCompoundAssignment(
                currentToken().getType())) {

            Token assignmentOperator = advance();

            Expression value = parseExpression();

            consume(TokenType.PONTO_VIRGULA);

            return createCompoundAssignmentCommand(
                    identifier,
                    assignmentOperator,
                    value);
        }

        if (check(TokenType.INCREMENTO)
                || check(TokenType.DECREMENTO)) {

            Token updateOperator = advance();

            consume(TokenType.PONTO_VIRGULA);

            return createUpdateCommand(
                    identifier,
                    updateOperator);
        }

        throw error(
                currentToken(),
                "Esperado '=', um operador de atribuição composta, "
                        + "'++' ou '--' após o identificador '"
                        + identifier.getLexeme()
                        + "'.");
    }

    private boolean isCompoundAssignment(
            TokenType type) {

        return type == TokenType.SOMA_ATRIB
                || type == TokenType.SUB_ATRIB
                || type == TokenType.MULT_ATRIB
                || type == TokenType.DIV_ATRIB
                || type == TokenType.RESTO_ATRIB
                || type == TokenType.DESLOCAMENTO_ESQ_ATRIB
                || type == TokenType.DESLOCAMENTO_DIR_ATRIB;
    }

    private AssignmentCommand createCompoundAssignmentCommand(
            Token identifier,
            Token assignmentOperator,
            Expression value) {

        String binaryOperator = switch (
                assignmentOperator.getType()) {

            case SOMA_ATRIB -> "+";
            case SUB_ATRIB -> "-";
            case MULT_ATRIB -> "*";
            case DIV_ATRIB -> "/";
            case RESTO_ATRIB -> "%";
            case DESLOCAMENTO_ESQ_ATRIB -> "<<";
            case DESLOCAMENTO_DIR_ATRIB -> ">>";

            default -> throw error(
                    assignmentOperator,
                    "Operador de atribuição composta inválido.");
        };

        Expression currentValue = new VariableExpression(
                identifier.getLexeme(),
                identifier.getLine(),
                identifier.getColumn());

        Expression updatedValue = new BinaryExpression(
                currentValue,
                value,
                binaryOperator,
                assignmentOperator.getLine(),
                assignmentOperator.getColumn());

        return new AssignmentCommand(
                identifier.getLexeme(),
                updatedValue,
                identifier.getLine(),
                identifier.getColumn());
    }

    private AssignmentCommand createUpdateCommand(
            Token identifier,
            Token updateOperator) {

        String arithmeticOperator
                = updateOperator.getType() == TokenType.INCREMENTO
                        ? "+"
                        : "-";

        Expression currentValue = new VariableExpression(
                identifier.getLexeme(),
                identifier.getLine(),
                identifier.getColumn());

        Expression one = new LiteralExpression(
                1,
                updateOperator.getLine(),
                updateOperator.getColumn());

        Expression updatedValue = new BinaryExpression(
                currentValue,
                one,
                arithmeticOperator,
                updateOperator.getLine(),
                updateOperator.getColumn());

        return new AssignmentCommand(
                identifier.getLexeme(),
                updatedValue,
                identifier.getLine(),
                identifier.getColumn());
    }

    private List<Command> parseCommandBlock() {

        List<Command> commands = new ArrayList<>();

        while (!check(TokenType.CHAVE_DIR)) {

            ensureNotAtEnd(
                    "Esperado '}' para encerrar o bloco de comandos.");

            if (check(TokenType.RETURN)) {

                throw error(
                        currentToken(),
                        "'return' somente pode aparecer no final de 'main' ou de uma função.");
            }

            commands.add(
                    parseCommand());
        }

        return commands;
    }

    // exp ::= exp_d (('<' | '>' | '<=' | '>=' | '==' | '!=') exp_d)*

    private Expression parseExpression() {

        Expression left = parseShift();

        while (check(TokenType.MENOR)
                || check(TokenType.MAIOR)
                || check(TokenType.MENOR_IGUAL)
                || check(TokenType.MAIOR_IGUAL)
                || check(TokenType.IGUALDADE)
                || check(TokenType.DIFERENTE)) {

            Token operator = advance();

            Expression right = parseShift();

            left = new BinaryExpression(
                    left,
                    right,
                    operator.getLexeme(),
                    operator.getLine(),
                    operator.getColumn());
        }

        return left;
    }


    // exp_d ::= exp_a (('<<' | '>>') exp_a)*

    private Expression parseShift() {

        Expression left = parseAdditive();

        while (check(TokenType.DESLOCAMENTO_ESQ)
                || check(TokenType.DESLOCAMENTO_DIR)) {

            Token operator = advance();

            Expression right = parseAdditive();

            left = new BinaryExpression(
                    left,
                    right,
                    operator.getLexeme(),
                    operator.getLine(),
                    operator.getColumn());
        }

        return left;
    }

    // exp_a ::= exp_m (('+' | '-') exp_m)*

    private Expression parseAdditive() {

        Expression left = parseMultiplicative();

        while (check(TokenType.SOMA)
                || check(TokenType.SUB)) {

            Token operator = advance();

            Expression right = parseMultiplicative();

            left = new BinaryExpression(
                    left,
                    right,
                    operator.getLexeme(),
                    operator.getLine(),
                    operator.getColumn());
        }

        return left;
    }

    // exp_m ::= prim (('*' | '/' | '%') prim)*

    private Expression parseMultiplicative() {

        Expression left = parsePrimary();

        while (check(TokenType.MULT)
                || check(TokenType.DIV)
                || check(TokenType.RESTO)) {

            Token operator = advance();

            Expression right = parsePrimary();

            left = new BinaryExpression(
                    left,
                    right,
                    operator.getLexeme(),
                    operator.getLine(),
                    operator.getColumn());
        }

        return left;
    }

    // prim ::= NUMERO | IDENTIFICADOR | '(' exp ')' | chamada

    private Expression parsePrimary() {

        if (check(TokenType.NUMERO)) {

            Token number = advance();

            try {

                return new LiteralExpression(
                        Long.parseLong(number.getLexeme()),
                        number.getLine(),
                        number.getColumn());

            } catch (NumberFormatException exception) {

                throw error(
                        number,
                        "Número inteiro fora do intervalo de 'long'.");
            }
        }

        if (check(TokenType.IDENTIFICADOR)) {

            Token identifier = advance();

            if (check(TokenType.PAREN_ESQ)) {

                return parseFunctionCall(identifier);
            }

            return new VariableExpression(
                    identifier.getLexeme(),
                    identifier.getLine(),
                    identifier.getColumn());
        }

        if (match(TokenType.PAREN_ESQ)) {

            Expression expression = parseExpression();

            consume(TokenType.PAREN_DIR);

            return expression;
        }

        throw error(
                currentToken(),
                "Expressão esperada.");
    }

    // fun ::= IDENTIFICADOR '(' params? ')'

    private FunctionCallExpression parseFunctionCall(
            Token identifier) {

        consume(TokenType.PAREN_ESQ);

        List<Expression> arguments = parseRealParameters();

        consume(TokenType.PAREN_DIR);

        return new FunctionCallExpression(
                identifier.getLexeme(),
                arguments,
                identifier.getLine(),
                identifier.getColumn());
    }

    // params ::= exp (',' exp)*

    private List<Expression> parseRealParameters() {

        List<Expression> arguments = new ArrayList<>();

        if (check(TokenType.PAREN_DIR)) {
            return arguments;
        }

        arguments.add(
                parseExpression());

        while (match(TokenType.VIRGULA)) {

            arguments.add(
                    parseExpression());
        }

        return arguments;
    }

    private Token consume(TokenType expected) {

        if (!check(expected)) {

            throw error(
                    currentToken(),
                    "Esperado "
                            + tokenDescription(expected)
                            + " mas encontrado "
                            + tokenDescription(currentToken().getType())
                            + ".");
        }

        return advance();
    }

    private boolean match(TokenType type) {

        if (!check(type)) {
            return false;
        }

        advance();
        return true;
    }

    private boolean check(TokenType type) {
        return currentToken().getType() == type;
    }

    private Token currentToken() {
        return tokens.get(current);
    }

    private Token advance() {

        Token token = currentToken();

        if (current < tokens.size() - 1) {
            current++;
        }

        return token;
    }

    private void ensureNotAtEnd(String message) {

        if (check(TokenType.EOF)) {

            throw error(
                    currentToken(),
                    message);
        }
    }

    private ParserException error(
            Token token,
            String message) {

        return new ParserException(
                message,
                token.getLine(),
                token.getColumn());
    }

    private String tokenDescription(TokenType type) {

        return switch (type) {

            case PONTO_VIRGULA -> "';'";
            case VIRGULA -> "','";
            case PAREN_ESQ -> "'('";
            case PAREN_DIR -> "')'";
            case CHAVE_ESQ -> "'{'";
            case CHAVE_DIR -> "'}'";
            case ATRIB -> "'='";
            case SOMA_ATRIB -> "'+='";
            case SUB_ATRIB -> "'-='";
            case MULT_ATRIB -> "'*='";
            case DIV_ATRIB -> "'/='";
            case RESTO_ATRIB -> "'%='";
            case DESLOCAMENTO_ESQ_ATRIB -> "'<<='";
            case DESLOCAMENTO_DIR_ATRIB -> "'>>='";
            case IGUALDADE -> "'=='";
            case DIFERENTE -> "'!='";
            case SOMA -> "'+'";
            case SUB -> "'-'";
            case MULT -> "'*'";
            case DIV -> "'/'";
            case RESTO -> "'%'";
            case INCREMENTO -> "'++'";
            case DECREMENTO -> "'--'";
            case DESLOCAMENTO_ESQ -> "'<<'";
            case DESLOCAMENTO_DIR -> "'>>'";
            case MENOR -> "'<'";
            case MAIOR -> "'>'";
            case MENOR_IGUAL -> "'<='";
            case MAIOR_IGUAL -> "'>='";
            case IF -> "'if'";
            case ELSE -> "'else'";
            case WHILE -> "'while'";
            case RETURN -> "'return'";
            case FUN -> "'fun'";
            case VAR -> "'var'";
            case MAIN -> "'main'";
            case IDENTIFICADOR -> "um identificador";
            case NUMERO -> "um número";
            case EOF -> "fim do arquivo";
            case ERRO -> "um token inválido";
        };
    }
}