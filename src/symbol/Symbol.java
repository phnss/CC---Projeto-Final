package symbol;

import ast.declaration.FunctionDeclaration;

public class Symbol {

    private final String name;
    private final SymbolKind kind;
    private final int parameterCount;
    private final FunctionDeclaration functionDeclaration;

    private Symbol(
            String name,
            SymbolKind kind,
            int parameterCount,
            FunctionDeclaration functionDeclaration) {

        this.name = name;
        this.kind = kind;
        this.parameterCount = parameterCount;
        this.functionDeclaration = functionDeclaration;
    }

    public static Symbol variable(
            String name,
            SymbolKind kind) {

        return new Symbol(
                name,
                kind,
                0,
                null);
    }

    public static Symbol function(
            String name,
            int parameterCount,
            FunctionDeclaration functionDeclaration) {

        return new Symbol(
                name,
                SymbolKind.FUNCTION,
                parameterCount,
                functionDeclaration);
    }

    public String getName() {
        return name;
    }

    public SymbolKind getKind() {
        return kind;
    }

    public int getParameterCount() {
        return parameterCount;
    }

    public FunctionDeclaration getFunctionDeclaration() {
        return functionDeclaration;
    }

    public boolean isVariable() {

        return kind == SymbolKind.GLOBAL_VARIABLE
                || kind == SymbolKind.LOCAL_VARIABLE
                || kind == SymbolKind.PARAMETER;
    }
}