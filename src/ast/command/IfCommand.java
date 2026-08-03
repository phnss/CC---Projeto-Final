package ast.command;

import ast.expression.Expression;
import java.util.List;

public class IfCommand extends Command {

    private final Expression condition;
    private final List<Command> thenCommands;
    private final List<Command> elseCommands;

    public IfCommand(
            Expression condition,
            List<Command> thenCommands,
            List<Command> elseCommands,
            int line,
            int column) {

        super(line, column);
        this.condition = condition;
        this.thenCommands = List.copyOf(thenCommands);
        this.elseCommands = List.copyOf(elseCommands);
    }

    public Expression getCondition() {
        return condition;
    }

    public List<Command> getThenCommands() {
        return thenCommands;
    }

    public List<Command> getElseCommands() {
        return elseCommands;
    }
}