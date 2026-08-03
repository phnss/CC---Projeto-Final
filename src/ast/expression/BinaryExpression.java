package ast.expression;

public class BinaryExpression extends Expression {

    private final Expression left;
    private final Expression right;
    private final String operator;

    public BinaryExpression(
            Expression left,
            Expression right,
            String operator,
            int line,
            int column) {

        super(line, column);
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    public Expression getLeft() {
        return left;
    }

    public Expression getRight() {
        return right;
    }

    public String getOperator() {
        return operator;
    }
}