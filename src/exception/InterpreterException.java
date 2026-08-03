package exception;

public class InterpreterException extends CompilerException {

    private static final long serialVersionUID = 1L;

    public InterpreterException(
            String message,
            int line,
            int column) {

        super(
                "de interpretação",
                message,
                line,
                column);
    }
}