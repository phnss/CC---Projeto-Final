package lexer;

import exception.LexicalException;
import java.util.ArrayList;
import java.util.List;
import model.Token;
import model.TokenType;

public class Lexer {

    private final String input;

    private int position;
    private int line;
    private int column;

    private final List<LexicalException> errors;

    public Lexer(String input) {

        this.input = input;
        this.position = 0;
        this.line = 1;
        this.column = 1;
        this.errors = new ArrayList<>();
    }

    public List<Token> tokenize() {

        List<Token> tokens = new ArrayList<>();

        while (!isAtEnd()) {

            char current = currentChar();

            if (Character.isWhitespace(current)) {

                advance();
                continue;
            }

            if (Character.isDigit(current)) {

                tokenizeNumber(tokens);
                continue;
            }

            if (Character.isLetter(current)) {

                tokenizeIdentifier(tokens);
                continue;
            }

            tokenizeSymbol(tokens);
        }

        tokens.add(new Token(
                TokenType.EOF,
                "",
                position,
                line,
                column));

        return tokens;
    }

    private void tokenizeNumber(
            List<Token> tokens) {

        int startPosition = position;
        int startLine = line;
        int startColumn = column;

        StringBuilder lexeme = new StringBuilder();

        while (!isAtEnd()
                && Character.isDigit(currentChar())) {

            lexeme.append(advance());
        }

        if (!isAtEnd()
                && Character.isLetter(currentChar())) {

            while (!isAtEnd()
                    && Character.isLetterOrDigit(currentChar())) {

                lexeme.append(advance());
            }

            tokens.add(new Token(
                    TokenType.ERRO,
                    lexeme.toString(),
                    startPosition,
                    startLine,
                    startColumn));

            errors.add(new LexicalException(
                    "Identificador iniciado por número.",
                    startLine,
                    startColumn));

            return;
        }

        tokens.add(new Token(
                TokenType.NUMERO,
                lexeme.toString(),
                startPosition,
                startLine,
                startColumn));
    }

    private void tokenizeIdentifier(
            List<Token> tokens) {

        int startPosition = position;
        int startLine = line;
        int startColumn = column;

        StringBuilder lexeme = new StringBuilder();

        while (!isAtEnd()
                && Character.isLetterOrDigit(currentChar())) {

            lexeme.append(advance());
        }

        String value = lexeme.toString();

        tokens.add(new Token(
                keywordType(value),
                value,
                startPosition,
                startLine,
                startColumn));
    }

    private void tokenizeSymbol(
            List<Token> tokens) {

        int startPosition = position;
        int startLine = line;
        int startColumn = column;

        char current = currentChar();

        switch (current) {

            case '+' -> tokenizePlus(
                    tokens,
                    startPosition,
                    startLine,
                    startColumn);

            case '-' -> tokenizeMinus(
                    tokens,
                    startPosition,
                    startLine,
                    startColumn);

            case '*' -> tokenizeMultiply(
                    tokens,
                    startPosition,
                    startLine,
                    startColumn);

            case '/' -> tokenizeDivide(
                    tokens,
                    startPosition,
                    startLine,
                    startColumn);

            case '%' -> tokenizeRemainder(
                    tokens,
                    startPosition,
                    startLine,
                    startColumn);

            case '<' -> tokenizeLessThan(
                    tokens,
                    startPosition,
                    startLine,
                    startColumn);

            case '>' -> tokenizeGreaterThan(
                    tokens,
                    startPosition,
                    startLine,
                    startColumn);

            case '!' -> tokenizeExclamation(
                    tokens,
                    startPosition,
                    startLine,
                    startColumn);

            case ';' -> addSingleCharacterToken(
                    tokens,
                    TokenType.PONTO_VIRGULA,
                    startPosition,
                    startLine,
                    startColumn);

            case ',' -> addSingleCharacterToken(
                    tokens,
                    TokenType.VIRGULA,
                    startPosition,
                    startLine,
                    startColumn);

            case '(' -> addSingleCharacterToken(
                    tokens,
                    TokenType.PAREN_ESQ,
                    startPosition,
                    startLine,
                    startColumn);

            case ')' -> addSingleCharacterToken(
                    tokens,
                    TokenType.PAREN_DIR,
                    startPosition,
                    startLine,
                    startColumn);

            case '{' -> addSingleCharacterToken(
                    tokens,
                    TokenType.CHAVE_ESQ,
                    startPosition,
                    startLine,
                    startColumn);

            case '}' -> addSingleCharacterToken(
                    tokens,
                    TokenType.CHAVE_DIR,
                    startPosition,
                    startLine,
                    startColumn);

            case '=' -> tokenizeEquals(
                    tokens,
                    startPosition,
                    startLine,
                    startColumn);

            default -> {

                advance();

                tokens.add(new Token(
                        TokenType.ERRO,
                        String.valueOf(current),
                        startPosition,
                        startLine,
                        startColumn));

                errors.add(new LexicalException(
                        "Caractere inválido '"
                                + current
                                + "'.",
                        startLine,
                        startColumn));
            }
        }
    }

    private void tokenizePlus(
            List<Token> tokens,
            int startPosition,
            int startLine,
            int startColumn) {

        advance();

        if (!isAtEnd() && currentChar() == '+') {

            advance();

            tokens.add(new Token(
                    TokenType.INCREMENTO,
                    "++",
                    startPosition,
                    startLine,
                    startColumn));

            return;
        }

        if (!isAtEnd() && currentChar() == '=') {

            advance();

            tokens.add(new Token(
                    TokenType.SOMA_ATRIB,
                    "+=",
                    startPosition,
                    startLine,
                    startColumn));

            return;
        }

        tokens.add(new Token(
                TokenType.SOMA,
                "+",
                startPosition,
                startLine,
                startColumn));
    }

    private void tokenizeMinus(
            List<Token> tokens,
            int startPosition,
            int startLine,
            int startColumn) {

        advance();

        if (!isAtEnd() && currentChar() == '-') {

            advance();

            tokens.add(new Token(
                    TokenType.DECREMENTO,
                    "--",
                    startPosition,
                    startLine,
                    startColumn));

            return;
        }

        if (!isAtEnd() && currentChar() == '=') {

            advance();

            tokens.add(new Token(
                    TokenType.SUB_ATRIB,
                    "-=",
                    startPosition,
                    startLine,
                    startColumn));

            return;
        }

        tokens.add(new Token(
                TokenType.SUB,
                "-",
                startPosition,
                startLine,
                startColumn));
    }

    private void tokenizeMultiply(
            List<Token> tokens,
            int startPosition,
            int startLine,
            int startColumn) {

        tokenizeSingleOrAssignment(
                tokens,
                TokenType.MULT,
                TokenType.MULT_ATRIB,
                "*",
                "*=",
                startPosition,
                startLine,
                startColumn);
    }

    private void tokenizeDivide(
            List<Token> tokens,
            int startPosition,
            int startLine,
            int startColumn) {

        tokenizeSingleOrAssignment(
                tokens,
                TokenType.DIV,
                TokenType.DIV_ATRIB,
                "/",
                "/=",
                startPosition,
                startLine,
                startColumn);
    }

    private void tokenizeRemainder(
            List<Token> tokens,
            int startPosition,
            int startLine,
            int startColumn) {

        tokenizeSingleOrAssignment(
                tokens,
                TokenType.RESTO,
                TokenType.RESTO_ATRIB,
                "%",
                "%=",
                startPosition,
                startLine,
                startColumn);
    }

    private void tokenizeSingleOrAssignment(
            List<Token> tokens,
            TokenType singleType,
            TokenType assignmentType,
            String singleLexeme,
            String assignmentLexeme,
            int startPosition,
            int startLine,
            int startColumn) {

        advance();

        if (!isAtEnd() && currentChar() == '=') {

            advance();

            tokens.add(new Token(
                    assignmentType,
                    assignmentLexeme,
                    startPosition,
                    startLine,
                    startColumn));

            return;
        }

        tokens.add(new Token(
                singleType,
                singleLexeme,
                startPosition,
                startLine,
                startColumn));
    }

    private void tokenizeLessThan(
            List<Token> tokens,
            int startPosition,
            int startLine,
            int startColumn) {

        advance();

        if (!isAtEnd() && currentChar() == '<') {

            advance();

            if (!isAtEnd() && currentChar() == '=') {

                advance();

                tokens.add(new Token(
                        TokenType.DESLOCAMENTO_ESQ_ATRIB,
                        "<<=",
                        startPosition,
                        startLine,
                        startColumn));

                return;
            }

            tokens.add(new Token(
                    TokenType.DESLOCAMENTO_ESQ,
                    "<<",
                    startPosition,
                    startLine,
                    startColumn));

            return;
        }

        if (!isAtEnd() && currentChar() == '=') {

            advance();

            tokens.add(new Token(
                    TokenType.MENOR_IGUAL,
                    "<=",
                    startPosition,
                    startLine,
                    startColumn));

            return;
        }

        tokens.add(new Token(
                TokenType.MENOR,
                "<",
                startPosition,
                startLine,
                startColumn));
    }

    private void tokenizeGreaterThan(
            List<Token> tokens,
            int startPosition,
            int startLine,
            int startColumn) {

        advance();

        if (!isAtEnd() && currentChar() == '>') {

            advance();

            if (!isAtEnd() && currentChar() == '=') {

                advance();

                tokens.add(new Token(
                        TokenType.DESLOCAMENTO_DIR_ATRIB,
                        ">>=",
                        startPosition,
                        startLine,
                        startColumn));

                return;
            }

            tokens.add(new Token(
                    TokenType.DESLOCAMENTO_DIR,
                    ">>",
                    startPosition,
                    startLine,
                    startColumn));

            return;
        }

        if (!isAtEnd() && currentChar() == '=') {

            advance();

            tokens.add(new Token(
                    TokenType.MAIOR_IGUAL,
                    ">=",
                    startPosition,
                    startLine,
                    startColumn));

            return;
        }

        tokens.add(new Token(
                TokenType.MAIOR,
                ">",
                startPosition,
                startLine,
                startColumn));
    }

    private void tokenizeExclamation(
            List<Token> tokens,
            int startPosition,
            int startLine,
            int startColumn) {

        advance();

        if (!isAtEnd() && currentChar() == '=') {

            advance();

            tokens.add(new Token(
                    TokenType.DIFERENTE,
                    "!=",
                    startPosition,
                    startLine,
                    startColumn));

            return;
        }

        tokens.add(new Token(
                TokenType.ERRO,
                "!",
                startPosition,
                startLine,
                startColumn));

        errors.add(new LexicalException(
                "Caractere inválido '!'. Esperado '=' após '!'.",
                startLine,
                startColumn));
    }

    private void tokenizeEquals(
            List<Token> tokens,
            int startPosition,
            int startLine,
            int startColumn) {

        advance();

        if (!isAtEnd() && currentChar() == '=') {

            advance();

            tokens.add(new Token(
                    TokenType.IGUALDADE,
                    "==",
                    startPosition,
                    startLine,
                    startColumn));

            return;
        }

        tokens.add(new Token(
                TokenType.ATRIB,
                "=",
                startPosition,
                startLine,
                startColumn));
    }

    private void addSingleCharacterToken(
            List<Token> tokens,
            TokenType type,
            int startPosition,
            int startLine,
            int startColumn) {

        String lexeme = String.valueOf(advance());

        tokens.add(new Token(
                type,
                lexeme,
                startPosition,
                startLine,
                startColumn));
    }

    private TokenType keywordType(String lexeme) {

        return switch (lexeme) {

            case "if" -> TokenType.IF;

            case "else" -> TokenType.ELSE;

            case "while" -> TokenType.WHILE;

            case "return" -> TokenType.RETURN;

            case "fun" -> TokenType.FUN;

            case "var" -> TokenType.VAR;

            case "main" -> TokenType.MAIN;

            default -> TokenType.IDENTIFICADOR;
        };
    }

    private char currentChar() {
        return input.charAt(position);
    }

    private char advance() {

        char current = input.charAt(position);

        position++;

        if (current == '\n') {

            line++;
            column = 1;

        } else {

            column++;
        }

        return current;
    }

    private boolean isAtEnd() {
        return position >= input.length();
    }

    public List<LexicalException> getErrors() {
        return List.copyOf(errors);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}