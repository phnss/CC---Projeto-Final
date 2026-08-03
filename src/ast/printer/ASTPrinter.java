package ast.printer;

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
import java.util.List;

public class ASTPrinter {

    public void print(Program program) {

        System.out.println("Program");

        System.out.println("├── Declarações");

        printTopLevelDeclarations(
                program.getDeclarations(),
                "│   ");

        System.out.println("└── main");

        System.out.println("    ├── Comandos");

        printCommands(
                program.getCommands(),
                "    │   ");

        System.out.println("    └── return");

        printExpression(
                program.getExpression(),
                "        ",
                true);
    }

    private void printTopLevelDeclarations(
            List<TopLevelDeclaration> declarations,
            String prefix) {

        if (declarations.isEmpty()) {

            System.out.println(prefix + "└── <vazio>");
            return;
        }

        for (int i = 0; i < declarations.size(); i++) {

            printTopLevelDeclaration(
                    declarations.get(i),
                    prefix,
                    i == declarations.size() - 1);
        }
    }

    private void printTopLevelDeclaration(
            TopLevelDeclaration declaration,
            String prefix,
            boolean isLast) {

        if (declaration instanceof VariableDeclaration variableDeclaration) {

            printVariableDeclaration(
                    variableDeclaration,
                    prefix,
                    isLast);

            return;
        }

        if (declaration instanceof FunctionDeclaration function) {

            printNodePrefix(prefix, isLast);

            System.out.println(
                    "fun " + function.getName());

            String childPrefix = childPrefix(
                    prefix,
                    isLast);

            printParameters(
                    function.getParameters(),
                    childPrefix,
                    false);

            printLocalDeclarations(
                    function.getLocalDeclarations(),
                    childPrefix,
                    false);

            printCommandBlock(
                    "Comandos",
                    function.getCommands(),
                    childPrefix,
                    false);

            printNamedExpression(
                    "return",
                    function.getExpression(),
                    childPrefix,
                    true);

            return;
        }

        throw new IllegalStateException(
                "Declaração inválida na AST.");
    }

    private void printVariableDeclaration(
            VariableDeclaration declaration,
            String prefix,
            boolean isLast) {

        printNodePrefix(prefix, isLast);

        System.out.println(
                "var " + declaration.getVariable() + " =");

        printExpression(
                declaration.getValue(),
                childPrefix(prefix, isLast),
                true);
    }

    private void printParameters(
            List<String> parameters,
            String prefix,
            boolean isLast) {

        printNodePrefix(prefix, isLast);

        System.out.println("Parâmetros");

        String childPrefix = childPrefix(
                prefix,
                isLast);

        if (parameters.isEmpty()) {

            System.out.println(
                    childPrefix + "└── <vazio>");

            return;
        }

        for (int i = 0; i < parameters.size(); i++) {

            printNodePrefix(
                    childPrefix,
                    i == parameters.size() - 1);

            System.out.println(parameters.get(i));
        }
    }

    private void printLocalDeclarations(
            List<VariableDeclaration> declarations,
            String prefix,
            boolean isLast) {

        printNodePrefix(prefix, isLast);

        System.out.println("Variáveis Locais");

        String childPrefix = childPrefix(
                prefix,
                isLast);

        if (declarations.isEmpty()) {

            System.out.println(
                    childPrefix + "└── <vazio>");

            return;
        }

        for (int i = 0; i < declarations.size(); i++) {

            printVariableDeclaration(
                    declarations.get(i),
                    childPrefix,
                    i == declarations.size() - 1);
        }
    }

    private void printCommands(
            List<Command> commands,
            String prefix) {

        if (commands.isEmpty()) {

            System.out.println(prefix + "└── <vazio>");
            return;
        }

        for (int i = 0; i < commands.size(); i++) {

            printCommand(
                    commands.get(i),
                    prefix,
                    i == commands.size() - 1);
        }
    }

    private void printCommand(
            Command command,
            String prefix,
            boolean isLast) {

        if (command instanceof AssignmentCommand assignment) {

            printNodePrefix(prefix, isLast);

            System.out.println(
                    assignment.getVariable() + " =");

            printExpression(
                    assignment.getValue(),
                    childPrefix(prefix, isLast),
                    true);

            return;
        }

        if (command instanceof IfCommand ifCommand) {

            printNodePrefix(prefix, isLast);

            System.out.println("if");

            String childPrefix = childPrefix(
                    prefix,
                    isLast);

            printNamedExpression(
                    "Condição",
                    ifCommand.getCondition(),
                    childPrefix,
                    false);

            printCommandBlock(
                    "Então",
                    ifCommand.getThenCommands(),
                    childPrefix,
                    false);

            printCommandBlock(
                    "Senão",
                    ifCommand.getElseCommands(),
                    childPrefix,
                    true);

            return;
        }

        if (command instanceof WhileCommand whileCommand) {

            printNodePrefix(prefix, isLast);

            System.out.println("while");

            String childPrefix = childPrefix(
                    prefix,
                    isLast);

            printNamedExpression(
                    "Condição",
                    whileCommand.getCondition(),
                    childPrefix,
                    false);

            printCommandBlock(
                    "Corpo",
                    whileCommand.getCommands(),
                    childPrefix,
                    true);

            return;
        }

        throw new IllegalStateException(
                "Comando inválido na AST.");
    }

    private void printNamedExpression(
            String name,
            Expression expression,
            String prefix,
            boolean isLast) {

        printNodePrefix(prefix, isLast);

        System.out.println(name);

        printExpression(
                expression,
                childPrefix(prefix, isLast),
                true);
    }

    private void printCommandBlock(
            String name,
            List<Command> commands,
            String prefix,
            boolean isLast) {

        printNodePrefix(prefix, isLast);

        System.out.println(name);

        printCommands(
                commands,
                childPrefix(prefix, isLast));
    }

    private void printExpression(
            Expression expression,
            String prefix,
            boolean isLast) {

        printNodePrefix(prefix, isLast);

        if (expression instanceof LiteralExpression literal) {

            System.out.println(
                    literal.getValue());

            return;
        }

        if (expression instanceof VariableExpression variable) {

            System.out.println(
                    variable.getName());

            return;
        }

        if (expression instanceof FunctionCallExpression call) {

            System.out.println(
                    "call " + call.getFunction());

            String childPrefix = childPrefix(
                    prefix,
                    isLast);

            if (call.getArguments().isEmpty()) {

                System.out.println(
                        childPrefix + "└── <sem argumentos>");

                return;
            }

            for (int i = 0; i < call.getArguments().size(); i++) {

                printExpression(
                        call.getArguments().get(i),
                        childPrefix,
                        i == call.getArguments().size() - 1);
            }

            return;
        }

        if (expression instanceof BinaryExpression binary) {

            System.out.println(
                    binary.getOperator());

            String childPrefix = childPrefix(
                    prefix,
                    isLast);

            printExpression(
                    binary.getLeft(),
                    childPrefix,
                    false);

            printExpression(
                    binary.getRight(),
                    childPrefix,
                    true);

            return;
        }

        throw new IllegalStateException(
                "Expressão inválida na AST.");
    }

    private void printNodePrefix(
            String prefix,
            boolean isLast) {

        System.out.print(prefix);

        System.out.print(
                isLast
                        ? "└── "
                        : "├── ");
    }

    private String childPrefix(
            String prefix,
            boolean isLast) {

        return prefix
                + (isLast
                        ? "    "
                        : "│   ");
    }
}