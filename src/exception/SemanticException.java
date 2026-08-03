package exception;

public class SemanticException extends CompilerException {

    private static final long serialVersionUID = 1L;

    public SemanticException(
            String message,
            int line,
            int column) {

        super(
                "semântico",
                message,
                line,
                column);
    }
}