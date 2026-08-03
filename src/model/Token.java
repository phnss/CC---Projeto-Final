package model;

public class Token {

    private final TokenType type;
    private final String lexeme;
    private final int position;
    private final int line;
    private final int column;

    public Token(
            TokenType type,
            String lexeme,
            int position,
            int line,
            int column) {

        this.type = type;
        this.lexeme = lexeme;
        this.position = position;
        this.line = line;
        this.column = column;
    }

    public TokenType getType() {
        return type;
    }

    public String getLexeme() {
        return lexeme;
    }

    public int getPosition() {
        return position;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public String toString() {

        return "<"
                + type
                + ", \""
                + lexeme
                + "\", posição "
                + position
                + ", linha "
                + line
                + ", coluna "
                + column
                + ">";
    }
}