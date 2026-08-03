package ast.declaration;

import ast.command.Command;
import ast.expression.Expression;
import java.util.List;

public class FunctionDeclaration extends TopLevelDeclaration {

    private final String name;
    private final List<String> parameters;
    private final List<VariableDeclaration> localDeclarations;
    private final List<Command> commands;
    private final Expression expression;

    public FunctionDeclaration(
            String name,
            List<String> parameters,
            List<VariableDeclaration> localDeclarations,
            List<Command> commands,
            Expression expression,
            int line,
            int column) {

        super(line, column);
        this.name = name;
        this.parameters = List.copyOf(parameters);
        this.localDeclarations = List.copyOf(localDeclarations);
        this.commands = List.copyOf(commands);
        this.expression = expression;
    }

    public String getName() {
        return name;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public List<VariableDeclaration> getLocalDeclarations() {
        return localDeclarations;
    }

    public List<Command> getCommands() {
        return commands;
    }

    public Expression getExpression() {
        return expression;
    }
}