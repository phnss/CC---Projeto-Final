package ast.command;

import ast.ASTNode;

public abstract class Command extends ASTNode {

    protected Command(
            int line,
            int column) {

        super(line, column);
    }
}