package semantic;

import ast.command.AssignmentCommand;
import ast.command.Command;
import ast.command.IfCommand;
import ast.command.WhileCommand;
import ast.declaration.FunctionDeclaration;
import ast.declaration.TopLevelDeclaration;
import ast.declaration.VariableDeclaration;
import ast.expression.BinaryExpression;
import ast.expression.Expression;
import ast.expression.FunctionCallExpression;
import ast.expression.LiteralExpression;
import ast.expression.VariableExpression;
import ast.program.Program;
import exception.SemanticException;
import java.util.List;
import symbol.Symbol;
import symbol.SymbolKind;
import symbol.SymbolTable;

public class SemanticAnalyzer {

    private final SymbolTable table = new SymbolTable();

    private FunctionDeclaration currentFunction;

    public void analyze(Program program) {

        table.clear();
        currentFunction = null;

        for (TopLevelDeclaration declaration
                : program.getDeclarations()) {

            analyzeTopLevelDeclaration(declaration);
        }

        analyzeCommands(
                program.getCommands());

        analyzeExpression(
                program.getExpression());
    }

    private void analyzeTopLevelDeclaration(
            TopLevelDeclaration declaration) {

        if (declaration instanceof VariableDeclaration variableDeclaration) {

            analyzeGlobalVariableDeclaration(
                    variableDeclaration);

            return;
        }

        if (declaration instanceof FunctionDeclaration function) {

            analyzeFunctionDeclaration(function);

            return;
        }

        throw new SemanticException(
                "Declaração inválida na AST.",
                declaration.getLine(),
                declaration.getColumn());
    }

    private void analyzeGlobalVariableDeclaration(
            VariableDeclaration declaration) {

        if (table.existsGlobal(
                declaration.getVariable())) {

            Symbol symbol = table.getGlobalSymbol(
                    declaration.getVariable());

            if (symbol.getKind()
                    == SymbolKind.GLOBAL_VARIABLE) {

                throw new SemanticException(
                        "Variável '"
                                + declaration.getVariable()
                                + "' já foi declarada.",
                        declaration.getLine(),
                        declaration.getColumn());
            }

            throw new SemanticException(
                    "Identificador '"
                            + declaration.getVariable()
                            + "' já foi declarado como função.",
                    declaration.getLine(),
                    declaration.getColumn());
        }

        analyzeExpression(
                declaration.getValue());

        table.declareGlobalVariable(
                declaration.getVariable());
    }

    private void analyzeFunctionDeclaration(
            FunctionDeclaration function) {

        if (table.existsGlobal(
                function.getName())) {

            throw new SemanticException(
                    "Identificador '"
                            + function.getName()
                            + "' já foi declarado.",
                    function.getLine(),
                    function.getColumn());
        }

        /*
         * A função é inserida antes da análise de seu corpo.
         * Isso permite chamadas diretamente recursivas.
         */
        table.declareFunction(
                function.getName(),
                function.getParameters().size(),
                function);

        table.enterLocalScope();
        currentFunction = function;

        try {

            for (String parameter : function.getParameters()) {

                ensureLocalNameAvailable(
                        parameter,
                        function.getLine(),
                        function.getColumn());

                table.declareParameter(parameter);
            }

            for (VariableDeclaration declaration
                    : function.getLocalDeclarations()) {

                ensureLocalNameAvailable(
                        declaration.getVariable(),
                        declaration.getLine(),
                        declaration.getColumn());

                /*
                 * O inicializador é analisado antes da variável
                 * ser inserida no escopo local.
                 */
                analyzeExpression(
                        declaration.getValue());

                table.declareLocalVariable(
                        declaration.getVariable());
            }

            analyzeCommands(
                    function.getCommands());

            analyzeExpression(
                    function.getExpression());

        } finally {

            table.exitLocalScope();
            currentFunction = null;
        }
    }

    private void ensureLocalNameAvailable(
            String name,
            int line,
            int column) {

        if (table.existsInCurrentLocalScope(name)) {

            throw new SemanticException(
                    "Variável local '"
                            + name
                            + "' já foi declarada na função '"
                            + currentFunction.getName()
                            + "'.",
                    line,
                    column);
        }
    }

    private void analyzeCommands(
            List<Command> commands) {

        for (Command command : commands) {

            analyzeCommand(command);
        }
    }

    private void analyzeCommand(Command command) {

        if (command instanceof AssignmentCommand assignment) {

            analyzeVariableReference(
                    assignment.getVariable(),
                    assignment.getLine(),
                    assignment.getColumn());

            analyzeExpression(
                    assignment.getValue());

            return;
        }

        if (command instanceof IfCommand ifCommand) {

            analyzeExpression(
                    ifCommand.getCondition());

            analyzeCommands(
                    ifCommand.getThenCommands());

            analyzeCommands(
                    ifCommand.getElseCommands());

            return;
        }

        if (command instanceof WhileCommand whileCommand) {

            analyzeExpression(
                    whileCommand.getCondition());

            analyzeCommands(
                    whileCommand.getCommands());

            return;
        }

        throw new SemanticException(
                "Comando inválido na AST.",
                command.getLine(),
                command.getColumn());
    }

    private void analyzeExpression(Expression expression) {

        if (expression instanceof LiteralExpression) {
            return;
        }

        if (expression instanceof VariableExpression variable) {

            analyzeVariableReference(
                    variable.getName(),
                    variable.getLine(),
                    variable.getColumn());

            return;
        }

        if (expression instanceof FunctionCallExpression call) {

            analyzeFunctionCall(call);

            return;
        }

        if (expression instanceof BinaryExpression binary) {

            analyzeExpression(
                    binary.getLeft());

            analyzeExpression(
                    binary.getRight());

            return;
        }

        throw new SemanticException(
                "Expressão inválida na AST.",
                expression.getLine(),
                expression.getColumn());
    }

    private void analyzeVariableReference(
            String name,
            int line,
            int column) {

        Symbol symbol = table.resolveSymbol(name);

        if (symbol == null) {

            throw new SemanticException(
                    "Variável '"
                            + name
                            + "' não declarada.",
                    line,
                    column);
        }

        if (!symbol.isVariable()) {

            throw new SemanticException(
                    "Identificador '"
                            + name
                            + "' não representa uma variável.",
                    line,
                    column);
        }
    }

    private void analyzeFunctionCall(
            FunctionCallExpression call) {

        /*
         * Funções somente podem ser globais.
         */
        Symbol symbol = table.getGlobalSymbol(
                call.getFunction());

        if (symbol == null) {

            throw new SemanticException(
                    "Função '"
                            + call.getFunction()
                            + "' não declarada.",
                    call.getLine(),
                    call.getColumn());
        }

        if (symbol.getKind() != SymbolKind.FUNCTION) {

            throw new SemanticException(
                    "Identificador '"
                            + call.getFunction()
                            + "' não representa uma função.",
                    call.getLine(),
                    call.getColumn());
        }

        int expected = symbol.getParameterCount();
        int received = call.getArguments().size();

        if (expected != received) {

            throw new SemanticException(
                    "Função '"
                            + call.getFunction()
                            + "' esperava "
                            + expected
                            + " argumento(s), mas recebeu "
                            + received
                            + ".",
                    call.getLine(),
                    call.getColumn());
        }

        for (Expression argument : call.getArguments()) {

            analyzeExpression(argument);
        }
    }

    public SymbolTable getSymbolTable() {
        return table;
    }
}