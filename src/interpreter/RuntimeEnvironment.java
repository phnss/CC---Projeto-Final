package interpreter;

import ast.declaration.FunctionDeclaration;
import exception.InterpreterException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class RuntimeEnvironment {

    private final Map<String, Long> globalVariables = new HashMap<>();

    private final Map<String, FunctionDeclaration> functions = new HashMap<>();

    private final Deque<Map<String, Long>> localScopes = new ArrayDeque<>();

    public void declareGlobalVariable(
            String name,
            long value) {

        globalVariables.put(name, value);
    }

    public void declareFunction(
            String name,
            FunctionDeclaration declaration) {

        functions.put(name, declaration);
    }

    public void enterLocalScope() {

        localScopes.push(
                new HashMap<>());
    }

    public void exitLocalScope() {

        if (localScopes.isEmpty()) {

            throw new InterpreterException(
                    "Nenhum escopo local está ativo.",
                    0,
                    0);
        }

        localScopes.pop();
    }

    public void declareLocalVariable(
            String name,
            long value,
            int line,
            int column) {

        currentLocalScope(line, column)
                .put(name, value);
    }

    public long get(
            String name,
            int line,
            int column) {

        if (!localScopes.isEmpty()
                && localScopes.peek().containsKey(name)) {

            return localScopes.peek().get(name);
        }

        if (globalVariables.containsKey(name)) {

            return globalVariables.get(name);
        }

        throw new InterpreterException(
                "Variável '"
                        + name
                        + "' não declarada.",
                line,
                column);
    }

    public void set(
            String name,
            long value,
            int line,
            int column) {

        if (!localScopes.isEmpty()
                && localScopes.peek().containsKey(name)) {

            localScopes.peek().put(name, value);
            return;
        }

        if (globalVariables.containsKey(name)) {

            globalVariables.put(name, value);
            return;
        }

        throw new InterpreterException(
                "Variável '"
                        + name
                        + "' não declarada.",
                line,
                column);
    }

    public FunctionDeclaration getFunction(
            String name,
            int line,
            int column) {

        FunctionDeclaration function = functions.get(name);

        if (function == null) {

            throw new InterpreterException(
                    "Função '"
                            + name
                            + "' não declarada.",
                    line,
                    column);
        }

        return function;
    }

    public void clear() {

        globalVariables.clear();
        functions.clear();
        localScopes.clear();
    }

    private Map<String, Long> currentLocalScope(
            int line,
            int column) {

        if (localScopes.isEmpty()) {

            throw new InterpreterException(
                    "Nenhum escopo local está ativo.",
                    line,
                    column);
        }

        return localScopes.peek();
    }
}