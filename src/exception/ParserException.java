package exception;

public class ParserException extends CompilerException {

    private static final long serialVersionUID = 1L;

    public ParserException(
            String message,
            int line,
            int column) {

        super(
                "sintático",
                message,
                line,
                column);
    }
}