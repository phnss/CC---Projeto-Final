package ast.program;

import ast.ASTNode;
import ast.command.Command;
import ast.declaration.TopLevelDeclaration;
import ast.expression.Expression;
import java.util.List;

public class Program extends ASTNode {

    private final List<TopLevelDeclaration> declarations;
    private final List<Command> commands;
    private final Expression expression;

    public Program(
            List<TopLevelDeclaration> declarations,
            List<Command> commands,
            Expression expression,
            int line,
            int column) {

        super(line, column);
        this.declarations = List.copyOf(declarations);
        this.commands = List.copyOf(commands);
        this.expression = expression;
    }

    public List<TopLevelDeclaration> getDeclarations() {
        return declarations;
    }

    public List<Command> getCommands() {
        return commands;
    }

    public Expression getExpression() {
        return expression;
    }
}