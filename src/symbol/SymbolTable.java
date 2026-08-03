package symbol;

import ast.declaration.FunctionDeclaration;
import exception.SemanticException;
import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

    private final Map<String, Symbol> globalSymbols = new HashMap<>();

    private Map<String, Symbol> localSymbols;

    public void declareGlobalVariable(String name) {

        globalSymbols.put(
                name,
                Symbol.variable(
                        name,
                        SymbolKind.GLOBAL_VARIABLE));
    }

    public void declareFunction(
            String name,
            int parameterCount,
            FunctionDeclaration declaration) {

        globalSymbols.put(
                name,
                Symbol.function(
                        name,
                        parameterCount,
                        declaration));
    }

    public void enterLocalScope() {

        localSymbols = new HashMap<>();
    }

    public void exitLocalScope() {

        localSymbols = null;
    }

    public boolean hasLocalScope() {
        return localSymbols != null;
    }

    public void declareLocalVariable(String name) {

        requireLocalScope();

        localSymbols.put(
                name,
                Symbol.variable(
                        name,
                        SymbolKind.LOCAL_VARIABLE));
    }

    public void declareParameter(String name) {

        requireLocalScope();

        localSymbols.put(
                name,
                Symbol.variable(
                        name,
                        SymbolKind.PARAMETER));
    }

    public boolean existsGlobal(String name) {

        return globalSymbols.containsKey(name);
    }

    public boolean existsInCurrentLocalScope(
            String name) {

        return hasLocalScope()
                && localSymbols.containsKey(name);
    }

    public Symbol getGlobalSymbol(String name) {

        return globalSymbols.get(name);
    }

    public Symbol resolveSymbol(String name) {

        if (hasLocalScope()) {

            Symbol local = localSymbols.get(name);

            if (local != null) {
                return local;
            }
        }

        return globalSymbols.get(name);
    }

    public void clear() {

        globalSymbols.clear();
        localSymbols = null;
    }

    private void requireLocalScope() {

        if (!hasLocalScope()) {

            throw new SemanticException(
                    "Nenhum escopo local está ativo.",
                    0,
                    0);
        }
    }
}