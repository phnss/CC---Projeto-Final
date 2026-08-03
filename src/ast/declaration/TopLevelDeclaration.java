package ast.declaration;

import ast.ASTNode;

public abstract class TopLevelDeclaration extends ASTNode {

    protected TopLevelDeclaration(
            int line,
            int column) {

        super(line, column);
    }
}