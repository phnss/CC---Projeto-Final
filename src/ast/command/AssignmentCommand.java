package ast.command;

import ast.expression.Expression;

public class AssignmentCommand extends Command {

    private final String variable;
    private final Expression value;

    public AssignmentCommand(
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