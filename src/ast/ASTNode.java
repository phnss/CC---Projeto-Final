package ast;

public abstract class ASTNode {

    private final int line;
    private final int column;

    protected ASTNode(
            int line,
            int column) {

        this.line = line;
        this.column = column;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }
}
