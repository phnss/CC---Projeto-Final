package ast.expression;

public class LiteralExpression extends Expression {

    private final long value;

    public LiteralExpression(
            long value,
            int line,
            int column) {

        super(line, column);
        this.value = value;
    }

    public long getValue() {
        return value;
    }
}