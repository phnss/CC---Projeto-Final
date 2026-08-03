package main;

import ast.printer.ASTPrinter;
import ast.program.Program;
import codegen.CodeGenerator;
import exception.CompilerException;
import exception.LexicalException;
import interpreter.Interpreter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;
import lexer.Lexer;
import model.Token;
import parser.Parser;
import semantic.SemanticAnalyzer;

public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        try {

            System.out.println("==============================");
            System.out.println("      COMPILADOR FUN");
            System.out.println("==============================");

            String fileName;

            if (args.length > 0) {

                fileName = args[0];

                System.out.println(
                        "Arquivo: " + fileName);

            } else {

                System.out.print(
                        "Informe o nome do arquivo: ");

                fileName = scanner.nextLine();
            }

            Path inputFile = Path.of(
                    "../../input",
                    fileName);

            String source = Files.readString(
                    inputFile);

            Lexer lexer = new Lexer(source);

            List<Token> tokens = lexer.tokenize();

            System.out.println("\n===== TOKENS =====");

            tokens.forEach(System.out::println);

            if (lexer.hasErrors()) {

                System.out.println(
                        "\n===== ERROS LÉXICOS =====");

                for (LexicalException error : lexer.getErrors()) {

                    System.out.println(
                            error.getMessage());
                }

                return;
            }

            Parser parser = new Parser(tokens);

            Program program = parser.parse();

            System.out.println("\n===== AST =====");

            ASTPrinter printer = new ASTPrinter();

            printer.print(program);

            SemanticAnalyzer analyzer = new SemanticAnalyzer();

            analyzer.analyze(program);

            Interpreter interpreter = new Interpreter();

            long result = interpreter.evaluate(program);

            System.out.println(
                    "\nResultado = " + result);

            CodeGenerator generator = new CodeGenerator();

            String assembly = generator.generate(program);

            String outputName = fileName.replaceFirst(
                    "\\.[^.]+$",
                    "");

            Path output = Path.of(
                    "../../output",
                    outputName + ".s");

            Files.createDirectories(
                    output.getParent());

            Files.writeString(
                    output,
                    assembly);

            System.out.println(
                    "\nAssembly salvo em:");

            System.out.println(
                    output.toAbsolutePath());

        } catch (IOException exception) {

            System.out.println(
                    "Erro de entrada/saída: "
                            + exception.getMessage());

        } catch (CompilerException exception) {

            System.out.println(
                    exception.getMessage());

        } catch (RuntimeException exception) {

            System.out.println(
                    "Erro interno do compilador: "
                            + exception.getMessage());

        } finally {

            scanner.close();
        }
    }
}