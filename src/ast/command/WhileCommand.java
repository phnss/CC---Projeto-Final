package ast.command;

import ast.expression.Expression;
import java.util.List;

public class WhileCommand extends Command {

    private final Expression condition;
    private final List<Command> commands;

    public WhileCommand(
            Expression condition,
            List<Command> commands,
            int line,
            int column) {

        super(line, column);
        this.condition = condition;
        this.commands = List.copyOf(commands);
    }

    public Expression getCondition() {
        return condition;
    }

    public List<Command> getCommands() {
        return commands;
    }
}