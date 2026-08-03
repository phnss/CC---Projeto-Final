package exception;

public class CodeGenerationException extends CompilerException {

    private static final long serialVersionUID = 1L;

    public CodeGenerationException(
            String message,
            int line,
            int column) {

        super(
                "de geração de código",
                message,
                line,
                column);
    }
}
