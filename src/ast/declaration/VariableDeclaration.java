package ast.declaration;

import ast.expression.Expression;

public class VariableDeclaration extends TopLevelDeclaration {

    private final String variable;
    private final Expression value;

    public VariableDeclaration(
            String variable,
            Expression value,
            int line,
            int column) {

        super(line, column);
        this.variable = variable;
        this.value = value;
    }

    public String getVariable() {
        return variable;
    }

    public Expression getValue() {
        return value;
    }
}