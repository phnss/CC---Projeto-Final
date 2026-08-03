package ast.expression;

import ast.ASTNode;

public abstract class Expression extends ASTNode {

    protected Expression(
            int line,
            int column) {

        super(line, column);
    }
}