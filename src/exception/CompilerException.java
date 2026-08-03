package exception;

public abstract class CompilerException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String phase;
    private final int line;
    private final int column;

    protected CompilerException(
            String phase,
            String message,
            int line,
            int column) {

        super(message);
        this.phase = phase;
        this.line = line;
        this.column = column;
    }

    public String getPhase() {
        return phase;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public String getMessage() {

        if (line > 0 && column > 0) {

            return "Erro "
                    + phase
                    + " na linha "
                    + line
                    + ", coluna "
                    + column
                    + ": "
                    + super.getMessage();
        }

        return "Erro "
                + phase
                + ": "
                + super.getMessage();
    }
}