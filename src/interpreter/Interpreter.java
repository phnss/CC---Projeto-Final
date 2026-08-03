package interpreter;

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
import exception.InterpreterException;
import java.util.ArrayList;
import java.util.List;

public class Interpreter {

    private final RuntimeEnvironment environment
            = new RuntimeEnvironment();

    public long evaluate(Program program) {

        environment.clear();

        for (TopLevelDeclaration declaration
                : program.getDeclarations()) {

            processTopLevelDeclaration(declaration);
        }

        executeCommands(
                program.getCommands());

        return evaluateExpression(
                program.getExpression());
    }

    private void processTopLevelDeclaration(
            TopLevelDeclaration declaration) {

        if (declaration instanceof VariableDeclaration variableDeclaration) {

            long value = evaluateExpression(
                    variableDeclaration.getValue());

            environment.declareGlobalVariable(
                    variableDeclaration.getVariable(),
                    value);

            return;
        }

        if (declaration instanceof FunctionDeclaration function) {

            environment.declareFunction(
                    function.getName(),
                    function);

            return;
        }

        throw new InterpreterException(
                "Declaração inválida na AST.",
                declaration.getLine(),
                declaration.getColumn());
    }

    private void executeCommands(
            List<Command> commands) {

        for (Command command : commands) {

            executeCommand(command);
        }
    }

    private void executeCommand(Command command) {

        if (command instanceof AssignmentCommand assignment) {

            long value = evaluateExpression(
                    assignment.getValue());

            environment.set(
                    assignment.getVariable(),
                    value,
                    assignment.getLine(),
                    assignment.getColumn());

            return;
        }

        if (command instanceof IfCommand ifCommand) {

            long condition = evaluateExpression(
                    ifCommand.getCondition());

            if (condition != 0) {

                executeCommands(
                        ifCommand.getThenCommands());

            } else {

                executeCommands(
                        ifCommand.getElseCommands());
            }

            return;
        }

        if (command instanceof WhileCommand whileCommand) {

            while (evaluateExpression(
                    whileCommand.getCondition()) != 0) {

                executeCommands(
                        whileCommand.getCommands());
            }

            return;
        }

        throw new InterpreterException(
                "Comando inválido na AST.",
                command.getLine(),
                command.getColumn());
    }

    private long evaluateExpression(Expression expression) {

        if (expression instanceof LiteralExpression literal) {

            return literal.getValue();
        }

        if (expression instanceof VariableExpression variable) {

            return environment.get(
                    variable.getName(),
                    variable.getLine(),
                    variable.getColumn());
        }

        if (expression instanceof FunctionCallExpression call) {

            return evaluateFunctionCall(call);
        }

        if (expression instanceof BinaryExpression binary) {

            /*
             * Mantém a mesma ordem utilizada pelo gerador:
             * primeiro o lado direito e depois o esquerdo.
             */
            long right = evaluateExpression(
                    binary.getRight());

            long left = evaluateExpression(
                    binary.getLeft());

            if (right == 0) {

                if (binary.getOperator().equals("/")) {

                    throw new InterpreterException(
                            "Divisão por zero.",
                            binary.getLine(),
                            binary.getColumn());
                }

                if (binary.getOperator().equals("%")) {

                    throw new InterpreterException(
                            "Resto de divisão por zero.",
                            binary.getLine(),
                            binary.getColumn());
                }
            }

            return switch (binary.getOperator()) {

                case "+" -> left + right;

                case "-" -> left - right;

                case "*" -> left * right;

                case "/" -> left / right;

                case "%" -> left % right;

                case "<<" -> left << right;

                case ">>" -> left >> right;

                case "<" -> left < right ? 1 : 0;

                case ">" -> left > right ? 1 : 0;

                case "<=" -> left <= right ? 1 : 0;

                case ">=" -> left >= right ? 1 : 0;

                case "==" -> left == right ? 1 : 0;

                case "!=" -> left != right ? 1 : 0;

                default -> throw new InterpreterException(
                        "Operador '"
                                + binary.getOperator()
                                + "' inválido.",
                        binary.getLine(),
                        binary.getColumn());
            };
        }

        throw new InterpreterException(
                "Expressão inválida na AST.",
                expression.getLine(),
                expression.getColumn());
    }

    private long evaluateFunctionCall(
            FunctionCallExpression call) {

        FunctionDeclaration function = environment.getFunction(
                call.getFunction(),
                call.getLine(),
                call.getColumn());

        int expected = function.getParameters().size();
        int received = call.getArguments().size();

        if (expected != received) {

            throw new InterpreterException(
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

        List<Long> argumentValues = new ArrayList<>();

        for (int i = 0; i < received; i++) {
            argumentValues.add(0L);
        }

        /*
         * Avaliação em ordem inversa, acompanhando a convenção
         * adotada pelo gerador de código.
         */
        for (int i = received - 1; i >= 0; i--) {

            long value = evaluateExpression(
                    call.getArguments().get(i));

            argumentValues.set(i, value);
        }

        environment.enterLocalScope();

        try {

            for (int i = 0; i < expected; i++) {

                environment.declareLocalVariable(
                        function.getParameters().get(i),
                        argumentValues.get(i),
                        call.getLine(),
                        call.getColumn());
            }

            for (VariableDeclaration declaration
                    : function.getLocalDeclarations()) {

                long value = evaluateExpression(
                        declaration.getValue());

                environment.declareLocalVariable(
                        declaration.getVariable(),
                        value,
                        declaration.getLine(),
                        declaration.getColumn());
            }

            executeCommands(
                    function.getCommands());

            return evaluateExpression(
                    function.getExpression());

        } finally {

            environment.exitLocalScope();
        }
    }
}