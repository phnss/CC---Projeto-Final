package exception;

public class LexicalException extends CompilerException {

    private static final long serialVersionUID = 1L;

    public LexicalException(
            String message,
            int line,
            int column) {

        super(
                "léxico",
                message,
                line,
                column);
    }
}