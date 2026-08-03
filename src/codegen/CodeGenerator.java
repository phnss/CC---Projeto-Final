package codegen;

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
import exception.CodeGenerationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeGenerator {

    private final StringBuilder data = new StringBuilder();

    private final StringBuilder text = new StringBuilder();

    private final StringBuilder functions = new StringBuilder();

    private int labelCounter;

    private Map<String, Integer> frameOffsets = new HashMap<>();

    private boolean insideFunction;

    public String generate(Program program) {

        data.setLength(0);
        text.setLength(0);
        functions.setLength(0);
        labelCounter = 0;
        frameOffsets = new HashMap<>();
        insideFunction = false;

        data.append(".section .bss\n");

        for (TopLevelDeclaration declaration
                : program.getDeclarations()) {

            if (declaration instanceof VariableDeclaration variableDeclaration) {

                data.append(variableDeclaration.getVariable())
                        .append(": .zero 8\n");
            }
        }

        text.append(".section .text\n");
        text.append(".globl _start\n");
        text.append("_start:\n");

        for (TopLevelDeclaration declaration
                : program.getDeclarations()) {

            if (declaration instanceof VariableDeclaration variableDeclaration) {

                generateExpression(
                        variableDeclaration.getValue(),
                        text);

                generateVariableWrite(
                        variableDeclaration.getVariable(),
                        text);
            }
        }

        generateCommands(
                program.getCommands(),
                text);

        generateExpression(
                program.getExpression(),
                text);

        text.append("call imprime_num\n");
        text.append("call sair\n");

        for (TopLevelDeclaration declaration
                : program.getDeclarations()) {

            if (declaration instanceof FunctionDeclaration function) {

                generateFunction(function);
            }
        }

        text.append(functions);
        text.append(".include \"runtime.s\"\n");

        return data.toString()
                + "\n"
                + text.toString();
    }

    private void generateFunction(
            FunctionDeclaration function) {

        int localBytes
                = function.getLocalDeclarations().size() * 8;

        Map<String, Integer> previousOffsets = frameOffsets;
        boolean previousInsideFunction = insideFunction;

        frameOffsets = new HashMap<>();
        insideFunction = true;

        for (int i = 0;
                i < function.getLocalDeclarations().size();
                i++) {

            frameOffsets.put(
                    function.getLocalDeclarations()
                            .get(i)
                            .getVariable(),
                    i * 8);
        }

        for (int i = 0;
                i < function.getParameters().size();
                i++) {

            frameOffsets.put(
                    function.getParameters().get(i),
                    localBytes + 16 + i * 8);
        }

        functions.append("\n")
                .append(function.getName())
                .append(":\n");

        functions.append("push %rbp\n");

        if (localBytes > 0) {

            functions.append("sub $")
                    .append(localBytes)
                    .append(", %rsp\n");
        }

        functions.append("mov %rsp, %rbp\n");

        for (VariableDeclaration declaration
                : function.getLocalDeclarations()) {

            generateExpression(
                    declaration.getValue(),
                    functions);

            generateVariableWrite(
                    declaration.getVariable(),
                    functions);
        }

        generateCommands(
                function.getCommands(),
                functions);

        generateExpression(
                function.getExpression(),
                functions);

        if (localBytes > 0) {

            functions.append("add $")
                    .append(localBytes)
                    .append(", %rsp\n");
        }

        functions.append("pop %rbp\n");
        functions.append("ret\n");

        frameOffsets = previousOffsets;
        insideFunction = previousInsideFunction;
    }

    private void generateCommands(
            List<Command> commands,
            StringBuilder output) {

        for (Command command : commands) {

            generateCommand(command, output);
        }
    }

    private void generateCommand(
            Command command,
            StringBuilder output) {

        if (command instanceof AssignmentCommand assignment) {

            generateExpression(
                    assignment.getValue(),
                    output);

            generateVariableWrite(
                    assignment.getVariable(),
                    output);

            return;
        }

        if (command instanceof IfCommand ifCommand) {

            int label = labelCounter++;

            String falseLabel = "Lfalso" + label;
            String endLabel = "Lfim" + label;

            generateExpression(
                    ifCommand.getCondition(),
                    output);

            output.append("cmp $0, %rax\n");
            output.append("jz ")
                    .append(falseLabel)
                    .append("\n");

            generateCommands(
                    ifCommand.getThenCommands(),
                    output);

            output.append("jmp ")
                    .append(endLabel)
                    .append("\n");

            output.append(falseLabel)
                    .append(":\n");

            generateCommands(
                    ifCommand.getElseCommands(),
                    output);

            output.append(endLabel)
                    .append(":\n");

            return;
        }

        if (command instanceof WhileCommand whileCommand) {

            int label = labelCounter++;

            String startLabel = "Linicio" + label;
            String endLabel = "Lfim" + label;

            output.append(startLabel)
                    .append(":\n");

            generateExpression(
                    whileCommand.getCondition(),
                    output);

            output.append("cmp $0, %rax\n");
            output.append("jz ")
                    .append(endLabel)
                    .append("\n");

            generateCommands(
                    whileCommand.getCommands(),
                    output);

            output.append("jmp ")
                    .append(startLabel)
                    .append("\n");

            output.append(endLabel)
                    .append(":\n");

            return;
        }

        throw new CodeGenerationException(
                "Comando inválido na AST.",
                command.getLine(),
                command.getColumn());
    }

    private void generateExpression(
            Expression expression,
            StringBuilder output) {

        if (expression instanceof LiteralExpression literal) {

            output.append("mov $")
                    .append(literal.getValue())
                    .append(", %rax\n");

            return;
        }

        if (expression instanceof VariableExpression variable) {

            generateVariableRead(
                    variable.getName(),
                    output);

            return;
        }

        if (expression instanceof FunctionCallExpression call) {

            generateFunctionCall(call, output);

            return;
        }

        if (expression instanceof BinaryExpression binary) {

            generateExpression(
                    binary.getRight(),
                    output);

            output.append("push %rax\n");

            generateExpression(
                    binary.getLeft(),
                    output);

            output.append("pop %rbx\n");

            switch (binary.getOperator()) {

                case "+" ->
                    output.append("add %rbx, %rax\n");

                case "-" ->
                    output.append("sub %rbx, %rax\n");

                case "*" ->
                    output.append("imul %rbx, %rax\n");

                case "/" -> {

                    output.append("cqto\n");
                    output.append("idiv %rbx\n");
                }

                case "%" -> {

                    output.append("cqto\n");
                    output.append("idiv %rbx\n");
                    output.append("mov %rdx, %rax\n");
                }

                case "<<" -> {

                    output.append("mov %rbx, %rcx\n");
                    output.append("sal %cl, %rax\n");
                }

                case ">>" -> {

                    output.append("mov %rbx, %rcx\n");
                    output.append("sar %cl, %rax\n");
                }

                case "==" ->
                    generateComparison(
                            "setz",
                            output);

                case "<" ->
                    generateComparison(
                            "setl",
                            output);

                case ">" ->
                    generateComparison(
                            "setg",
                            output);

                case "<=" ->
                    generateComparison(
                            "setle",
                            output);

                case ">=" ->
                    generateComparison(
                            "setge",
                            output);

                case "!=" ->
                    generateComparison(
                            "setne",
                            output);

                default -> throw new CodeGenerationException(
                        "Operador '"
                                + binary.getOperator()
                                + "' inválido.",
                        binary.getLine(),
                        binary.getColumn());
            }

            return;
        }

        throw new CodeGenerationException(
                "Expressão inválida na AST.",
                expression.getLine(),
                expression.getColumn());
    }

    private void generateFunctionCall(
            FunctionCallExpression call,
            StringBuilder output) {

        for (int i = call.getArguments().size() - 1;
                i >= 0;
                i--) {

            generateExpression(
                    call.getArguments().get(i),
                    output);

            output.append("push %rax\n");
        }

        output.append("call ")
                .append(call.getFunction())
                .append("\n");

        int parameterBytes
                = call.getArguments().size() * 8;

        if (parameterBytes > 0) {

            output.append("add $")
                    .append(parameterBytes)
                    .append(", %rsp\n");
        }
    }

    private void generateVariableRead(
            String variable,
            StringBuilder output) {

        Integer offset = frameOffsets.get(variable);

        if (insideFunction && offset != null) {

            output.append("mov ")
                    .append(offset)
                    .append("(%rbp), %rax\n");

            return;
        }

        output.append("mov ")
                .append(variable)
                .append("(%rip), %rax\n");
    }

    private void generateVariableWrite(
            String variable,
            StringBuilder output) {

        Integer offset = frameOffsets.get(variable);

        if (insideFunction && offset != null) {

            output.append("mov %rax, ")
                    .append(offset)
                    .append("(%rbp)\n");

            return;
        }

        output.append("mov %rax, ")
                .append(variable)
                .append("(%rip)\n");
    }

    private void generateComparison(
            String instruction,
            StringBuilder output) {

        output.append("xor %rcx, %rcx\n");
        output.append("cmp %rbx, %rax\n");
        output.append(instruction)
                .append(" %cl\n");
        output.append("mov %rcx, %rax\n");
    }
}