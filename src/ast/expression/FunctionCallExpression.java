package ast.expression;

import java.util.List;

public class FunctionCallExpression extends Expression {

    private final String function;
    private final List<Expression> arguments;

    public FunctionCallExpression(
            String function,
            List<Expression> arguments,
            int line,
            int column) {

        super(line, column);
        this.function = function;
        this.arguments = List.copyOf(arguments);
    }

    public String getFunction() {
        return function;
    }

    public List<Expression> getArguments() {
        return arguments;
    }
}