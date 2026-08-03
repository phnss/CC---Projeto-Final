package ast.expression;

public class VariableExpression extends Expression {

    private final String name;

    public VariableExpression(
            String name,
            int line,
            int column) {

        super(line, column);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}