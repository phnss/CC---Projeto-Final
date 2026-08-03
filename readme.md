# Documentação — Compilador Fun

Pedro Henrique Nogueira da Silva Santos - 20190023610

## 1. Objetivo

Este projeto implementa, em Java, um compilador acadêmico para a linguagem **Fun**.

A linguagem Fun adiciona funções, parâmetros, variáveis locais, chamadas de função e recursão direta.

O compilador implementa as seguintes etapas:

- análise léxica;
- análise sintática descendente recursiva;
- construção da Árvore Sintática Abstrata (AST);
- análise semântica;
- interpretação do programa;
- geração de código Assembly x86-64.

O fluxo geral é:

```text
Programa Fun
      ↓
     Lexer
      ↓
    Tokens
      ↓
     Parser
      ↓
   AST (Program)
      ↓
SemanticAnalyzer
      ↓
 Interpreter
      ↓
 Resultado
      ↓
CodeGenerator
      ↓
 Assembly x86-64
```

A linguagem Fun suporta:

- números inteiros do tipo `long`;
- variáveis globais;
- variáveis locais;
- parâmetros de funções;
- declarações de funções;
- chamadas de função;
- funções sem parâmetros;
- funções com múltiplos parâmetros;
- funções que chamam outras funções;
- recursão direta;
- comandos de atribuição;
- comando condicional `if`/`else`;
- comando de repetição `while`;
- expressão final com `return`;
- soma (`+`);
- subtração (`-`);
- multiplicação (`*`);
- divisão inteira (`/`);
- menor que (`<`);
- maior que (`>`);
- igualdade (`==`);
- expressões com parênteses;
- precedência entre operadores;
- associatividade à esquerda;
- blocos delimitados por chaves;
- localização por linha e coluna nos tokens e nos nós da AST;
- exceções específicas para cada fase do compilador.

A linguagem não possui um tipo booleano separado. O valor `0` representa falso, enquanto qualquer valor diferente de `0` representa verdadeiro. Os operadores de comparação sempre produzem `0` ou `1`.

---

# 2. Estrutura do Projeto

```text
Projeto/
│
├── input/
│   ├── programa.fun
│   ├── teste_funcao.fun
│   ├── teste_recursao.fun
│   └── ...
│
├── output/
│
├── runtime.s
│
├── bin/
│
└── src/
    ├── ast/
    │   ├── ASTNode.java
    │   │
    │   ├── command/
    │   │   ├── AssignmentCommand.java
    │   │   ├── Command.java
    │   │   ├── IfCommand.java
    │   │   └── WhileCommand.java
    │   │
    │   ├── declaration/
    │   │   ├── FunctionDeclaration.java
    │   │   ├── TopLevelDeclaration.java
    │   │   └── VariableDeclaration.java
    │   │
    │   ├── expression/
    │   │   ├── BinaryExpression.java
    │   │   ├── Expression.java
    │   │   ├── FunctionCallExpression.java
    │   │   ├── LiteralExpression.java
    │   │   └── VariableExpression.java
    │   │
    │   ├── printer/
    │   │   └── ASTPrinter.java
    │   │
    │   └── program/
    │       └── Program.java
    │
    ├── codegen/
    │   └── CodeGenerator.java
    │
    ├── exception/
    │   ├── CodeGenerationException.java
    │   ├── CompilerException.java
    │   ├── InterpreterException.java
    │   ├── LexicalException.java
    │   ├── ParserException.java
    │   └── SemanticException.java
    │
    ├── interpreter/
    │   ├── Interpreter.java
    │   └── RuntimeEnvironment.java
    │
    ├── lexer/
    │   └── Lexer.java
    │
    ├── main/
    │   └── Main.java
    │
    ├── model/
    │   ├── Token.java
    │   └── TokenType.java
    │
    ├── parser/
    │   └── Parser.java
    │
    ├── semantic/
    │   └── SemanticAnalyzer.java
    │
    └── symbol/
        ├── Symbol.java
        ├── SymbolKind.java
        └── SymbolTable.java
```

## 2.1 Responsabilidades dos pacotes

- `ast`: classes que representam a Árvore Sintática Abstrata;
- `ast.command`: comandos da linguagem;
- `ast.declaration`: declarações globais, locais e de funções;
- `ast.expression`: expressões da linguagem;
- `ast.printer`: impressão textual da AST;
- `ast.program`: raiz da AST;
- `codegen`: geração do código Assembly x86-64;
- `exception`: exceções específicas de cada fase;
- `interpreter`: execução direta do programa e ambiente de valores;
- `lexer`: transformação do código-fonte em tokens;
- `main`: ponto de entrada e coordenação das etapas;
- `model`: representação dos tokens;
- `parser`: validação sintática e construção da AST;
- `semantic`: validação das regras semânticas;
- `symbol`: símbolos e tabela utilizada exclusivamente pela análise semântica.

---

# 3. Linguagem Fun

Um programa Fun possui uma sequência de declarações globais, seguida pelo bloco principal iniciado pela palavra-chave `main`.

As declarações globais podem ser:

- declarações de variáveis, iniciadas por `var`;
- declarações de funções, iniciadas por `fun`.

Exemplo:

```text
var valorGlobal = 10;

fun dobro(x) {
    return x * 2;
}

main {
    return dobro(valorGlobal);
}
```

Resultado:

```text
20
```

## 3.1 Gramática

```text
<programa>  ::= <decl>* 'main' '{' <cmd>* 'return' <exp> ';' '}'

<decl>      ::= <vardecl> | <fundecl>

<fundecl>   ::= 'fun' <ident> '(' <arglist>? ')'
                '{' <vardecl>* <cmd>* 'return' <exp> ';' '}'

<arglist>   ::= <ident> | <ident> ',' <arglist>

<vardecl>   ::= 'var' <ident> '=' <exp> ';'

<ident>     ::= <letra><letra_digito>*

<cmd>       ::= <if> | <while> | <atrib>

<if>        ::= 'if' <exp> '{' <cmd>* '}' 'else' '{' <cmd>* '}'

<while>     ::= 'while' <exp> '{' <cmd>* '}'

<atrib>     ::= <ident> '=' <exp> ';'

<exp>       ::= <exp_a> (('<' | '>' | '==') <exp_a>)*

<exp_a>     ::= <exp_m> (('+' | '-') <exp_m>)*

<exp_m>     ::= <prim> (('*' | '/') <prim>)*

<prim>      ::= <num> | <ident> | '(' <exp> ')' | <fun>

<fun>       ::= <ident> '(' <params>? ')'

<params>    ::= <exp> | <exp> , <params>

<num>       ::= <digito><digito>*
```

## 3.2 Regras principais

A gramática e a implementação estabelecem que:

- um programa é formado por zero ou mais declarações, seguidas obrigatoriamente pelo bloco principal iniciado pela palavra-chave `main`;
- o bloco `main` é delimitado por chaves;
- o bloco principal pode possuir zero ou mais comandos;
- o bloco principal termina obrigatoriamente com `return`, seguido de uma expressão e de um ponto e vírgula;
- uma declaração pode ser uma declaração de variável ou uma declaração de função;
- declarações de variáveis são iniciadas pela palavra-chave `var`;
- declarações de funções são iniciadas pela palavra-chave `fun`;
- toda função possui um nome identificador, uma lista opcional de parâmetros formais e um corpo delimitado por chaves;
- uma função pode não possuir parâmetros;
- quando uma função possui mais de um parâmetro formal, os identificadores são separados por vírgulas;
- o corpo de uma função pode possuir zero ou mais declarações de variáveis locais;
- as declarações de variáveis locais aparecem antes dos comandos da função;
- depois das declarações locais, uma função pode possuir zero ou mais comandos;
- toda função termina obrigatoriamente com `return`, seguido de uma expressão e de um ponto e vírgula;
- uma declaração de variável possui um identificador, uma expressão de inicialização e um ponto e vírgula;
- um identificador começa obrigatoriamente com uma letra;
- depois da primeira letra, um identificador pode possuir zero ou mais letras ou dígitos;
- um comando pode ser um comando condicional, um comando de repetição ou uma atribuição;
- o comando `if` recebe uma expressão como condição;
- o comando `if` possui obrigatoriamente um bloco `else`;
- os blocos de `if` e `else` são delimitados por chaves;
- os blocos de `if` e `else` podem possuir zero ou mais comandos;
- o comando `while` recebe uma expressão como condição;
- o corpo de um `while` é delimitado por chaves e pode possuir zero ou mais comandos;
- uma atribuição modifica o valor associado a um identificador;
- toda atribuição termina com ponto e vírgula;
- uma expressão pode conter operadores de comparação, aritméticos, valores numéricos, identificadores, expressões entre parênteses ou chamadas de função;
- os operadores de comparação suportados são `<`, `>` e `==`;
- os operadores de comparação possuem precedência menor que os operadores aritméticos;
- os operadores de soma e subtração possuem precedência menor que os operadores de multiplicação e divisão;
- operadores pertencentes ao mesmo nível de precedência são associados da esquerda para a direita;
- uma expressão primária pode ser um número, um identificador, uma expressão entre parênteses ou uma chamada de função;
- uma chamada de função é formada por um identificador seguido de parênteses;
- uma chamada de função pode não possuir parâmetros reais;
- quando uma chamada possui mais de um parâmetro real, as expressões são separadas por vírgulas;
- cada parâmetro real de uma chamada pode ser uma expressão completa; - um número é formado por um ou mais dígitos;
- declarações de funções só aparecem no nível global, por meio da produção `<decl>`;
- declarações de variáveis podem aparecer no nível global ou no início do corpo de uma função;
- declarações de variáveis não aparecem dentro dos blocos de `if`, `else` ou `while`;
- `return` não é definido como um comando pela produção `<cmd>`;
- consequentemente, `return` aparece apenas no final do bloco `main` ou no final do corpo de uma função.

As palavras abaixo são reservadas:

```text
if
else
while
return
fun
var
main
```

Elas não podem ser utilizadas como nomes de variáveis ou funções.

## 3.3 Escopo das variáveis

Existem dois níveis principais de escopo:

```text
Escopo global
├── variáveis globais
└── funções

Escopo local de uma chamada
├── parâmetros
└── variáveis locais
```

Dentro de uma função, a resolução de uma variável ocorre nesta ordem:

1. procura no escopo local da chamada;
2. caso não seja encontrada, procura no escopo global.

Uma variável local ou um parâmetro pode possuir o mesmo nome de uma variável global. Nesse caso, o nome local esconde o nome global dentro da função.

Exemplo:

```text
var x = 100;

fun incrementar(x) {
    var resultado = x + 1;

    return resultado;
}

main {
    return incrementar(9) + x;
}
```

Resultado:

```text
110
```

O parâmetro `x` da função possui valor `9`, enquanto a variável global `x` continua com valor `100`.

## 3.4 Recursão

A implementação permite recursão direta. A função é inserida na tabela de símbolos antes de seu corpo ser analisado.

Exemplo:

```text
fun fib(n) {
    var resultado = 0;

    if n < 2 {
        resultado = 1;
    } else {
        resultado = fib(n - 1) + fib(n - 2);
    }

    return resultado;
}

main {
    return fib(6);
}
```

Resultado:

```text
13
```

Como as declarações globais são processadas sequencialmente, funções mutuamente recursivas não são garantidas pela implementação atual.

---

# 4. Como Executar

A implementação atual utiliza os caminhos relativos:

```java
Path.of("../../input", fileName)
Path.of("../../output", outputName + ".s")
```

Por isso, o diretório de trabalho usado para executar o programa deve ser:

```text
src/main
```

## 4.1 Criar um arquivo de entrada

Crie um arquivo com extensão `.fun` dentro da pasta `input`.

Exemplo:

```text
input/programa.fun
```

Conteúdo:

```text
fun dobro(x) {
    return x * 2;
}

main {
    return dobro(21);
}
```

## 4.4 Versão do Java

A implementação foi validada com Java 21. Recomenda-se utilizar Java 17 ou superior, pois o código utiliza recursos modernos da linguagem, como:

- expressões `switch`;
- regras de `switch` com `->`;
- pattern matching com `instanceof`.

---

# 5. Saída Esperada

Durante uma compilação bem-sucedida, o programa apresenta:

1. os tokens produzidos pelo Lexer;
2. a AST completa;
3. o resultado calculado pelo interpretador;
4. o caminho do arquivo Assembly gerado.

## 5.1 Tokens

Entrada:

```text
main {
    return 42;
}
```

Saída simplificada:

```text
<MAIN, "main", posição 0, linha 1, coluna 1>
<CHAVE_ESQ, "{", posição 5, linha 1, coluna 6>
<RETURN, "return", posição 11, linha 2, coluna 5>
<NUMERO, "42", posição 18, linha 2, coluna 12>
<PONTO_VIRGULA, ";", posição 20, linha 2, coluna 14>
<CHAVE_DIR, "}", posição 22, linha 3, coluna 1>
<EOF, "", posição 23, linha 3, coluna 2>
```

O campo `posição` representa o deslocamento absoluto no texto. Os campos `linha` e `coluna` começam em `1`.

## 5.2 AST

Para o programa:

```text
fun dobro(x) {
    var resultado = x * 2;

    return resultado;
}

main {
    return dobro(21);
}
```

A árvore possui formato semelhante a:

```text
Program
├── Declarações
│   └── fun dobro
│       ├── Parâmetros
│       │   └── x
│       ├── Variáveis Locais
│       │   └── var resultado =
│       │       └── *
│       │           ├── x
│       │           └── 2
│       ├── Comandos
│       │   └── <vazio>
│       └── return
│           └── resultado
└── main
    ├── Comandos
    │   └── <vazio>
    └── return
        └── call dobro
            └── 21
```

O `ASTPrinter` também apresenta `<vazio>` quando uma lista de declarações ou comandos está vazia e `<sem argumentos>` para chamadas sem argumentos.

## 5.3 Resultado da interpretação

Entrada:

```text
fun dobro(x) {
    return x * 2;
}

main {
    return dobro(21);
}
```

Saída:

```text
Resultado = 42
```

## 5.4 Código Assembly

Ao final da compilação, é gerado automaticamente:

```text
output/programa.s
```

O arquivo contém o código Assembly x86-64 correspondente ao programa Fun.

---

# 6. Funcionamento do Compilador

## 6.1 Análise Léxica

A análise léxica percorre o código-fonte caractere por caractere e produz uma lista de tokens.

Os tipos de token atuais são:

```text
NUMERO
IDENTIFICADOR

SOMA
SUB
MULT
DIV

MENOR
MAIOR
IGUALDADE

ATRIB
PONTO_VIRGULA
VIRGULA

PAREN_ESQ
PAREN_DIR
CHAVE_ESQ
CHAVE_DIR

IF
ELSE
WHILE
RETURN
FUN
VAR
MAIN

ERRO
EOF
```

Correspondência principal:

```text
+       SOMA
-       SUB
*       MULT
/       DIV
<       MENOR
>       MAIOR
==      IGUALDADE
=       ATRIB
;       PONTO_VIRGULA
,       VIRGULA
(       PAREN_ESQ
)       PAREN_DIR
{       CHAVE_ESQ
}       CHAVE_DIR
```

O Lexer:

- ignora espaços, tabulações e quebras de linha;
- atualiza linha e coluna durante o avanço;
- reconhece números inteiros;
- reconhece identificadores;
- reconhece palavras reservadas;
- diferencia `=` de `==`;
- reconhece vírgulas, operadores, parênteses, chaves e ponto e vírgula;
- adiciona `EOF` ao final;
- acumula os erros léxicos encontrados.

A lista de erros do Lexer contém objetos:

```java
List<LexicalException>
```

Caso existam erros léxicos, o `Main` apresenta todos eles e interrompe a compilação antes do Parser.

## 6.2 Análise Sintática

O Parser utiliza análise descendente recursiva.

Entre os métodos responsáveis pelo reconhecimento da linguagem estão:

```text
parse
parseTopLevelDeclaration
parseVariableDeclaration
parseFunctionDeclaration
parseFormalParameters
parseCommand
parseIf
parseWhile
parseAssignment
parseCommandBlock
parseExpression
parseAdditive
parseMultiplicative
parsePrimary
parseFunctionCall
parseRealParameters
consume
currentToken
advance
```

O método principal:

1. reconhece declarações globais iniciadas por `var` ou `fun`;
2. consome a palavra-chave `main`;
3. consome a chave de abertura;
4. reconhece comandos até `return`;
5. reconhece a expressão final do `main`;
6. consome ponto e vírgula, chave de fechamento e `EOF`;
7. constrói um objeto `Program`.

### Diferenciação entre variável e chamada

Uma referência a variável e uma chamada de função começam com `IDENTIFICADOR`.

O Parser consome o identificador e verifica o token seguinte:

- se for `PAREN_ESQ`, cria `FunctionCallExpression`;
- caso contrário, cria `VariableExpression`.

Exemplo:

```text
resultado
```

Produz uma referência a variável.

Exemplo:

```text
dobro(21)
```

Produz uma chamada de função.

### Localização dos nós

O Parser utiliza a linha e a coluna dos tokens para construir os nós da AST. Dessa forma, erros posteriores podem indicar a localização da estrutura que causou o problema.

## 6.3 Árvore Sintática Abstrata

Todos os nós atuais herdam de:

```text
ASTNode
```

A classe `ASTNode` armazena:

```java
int line
int column
```

Hierarquia principal:

```text
ASTNode
│
├── Program
│
├── TopLevelDeclaration
│   ├── VariableDeclaration
│   └── FunctionDeclaration
│
├── Command
│   ├── AssignmentCommand
│   ├── IfCommand
│   └── WhileCommand
│
└── Expression
    ├── LiteralExpression
    ├── VariableExpression
    ├── BinaryExpression
    └── FunctionCallExpression
```

### `Program`

A raiz da AST contém:

```text
Program
├── List<TopLevelDeclaration>
├── List<Command>
└── Expression
```

A expressão armazenada diretamente no `Program` corresponde ao `return` final do `main`.

### `VariableDeclaration`

Substitui a antiga classe `Declaration`.

Representa declarações globais e locais:

```text
var x = 10;
```

Campos principais:

```java
String variable
Expression value
```

### `FunctionDeclaration`

Representa uma função completa.

Campos principais:

```java
String name
List<String> parameters
List<VariableDeclaration> localDeclarations
List<Command> commands
Expression expression
```

O campo `expression` corresponde ao resultado final da função.

### `FunctionCallExpression`

Representa uma chamada que produz um valor.

Campos principais:

```java
String function
List<Expression> arguments
```

Exemplo:

```text
soma(2 + 3, dobro(4))
```

## 6.4 Análise Semântica

A classe responsável é:

```text
semantic.SemanticAnalyzer
```

A análise semântica utiliza:

```text
symbol.SymbolTable
```

A tabela semântica não armazena valores de execução. Ela guarda apenas informações sobre os símbolos conhecidos.

Os tipos de símbolo são:

```text
GLOBAL_VARIABLE
FUNCTION
LOCAL_VARIABLE
PARAMETER
```

A análise verifica:

- declaração global duplicada;
- conflito entre nome de variável global e função;
- declaração local duplicada;
- parâmetro duplicado;
- uso de variável não declarada;
- atribuição para variável não declarada;
- uso de um nome de função como variável;
- chamada de função não declarada;
- uso de uma variável como se fosse função;
- quantidade incorreta de argumentos;
- variáveis usadas em condições;
- variáveis usadas em comandos aninhados;
- variáveis usadas no `return`;
- escopo local e sombreamento.

### Ordem das declarações

As declarações globais são processadas na ordem em que aparecem.

Uma variável ou função pode referenciar símbolos globais declarados anteriormente.

A função atual é inserida na tabela antes de seu corpo ser analisado, permitindo recursão direta.

### Análise de funções

Durante a análise de uma função:

1. cria-se um escopo local;
2. inserem-se os parâmetros;
3. analisam-se e inserem-se as variáveis locais;
4. analisam-se os comandos;
5. analisa-se a expressão de retorno;
6. encerra-se o escopo local.

O inicializador de uma variável local é analisado antes de a própria variável ser inserida no escopo.

## 6.5 Ambiente de execução

A execução não utiliza a `SymbolTable` semântica.

O interpretador utiliza:

```text
interpreter.RuntimeEnvironment
```

Essa separação evita misturar:

- informações estáticas da compilação;
- valores mutáveis existentes durante a execução.

O `RuntimeEnvironment` armazena:

```text
Map<String, Long> globalVariables
Map<String, FunctionDeclaration> functions
Deque<Map<String, Long>> localScopes
```

Cada chamada de função cria um novo mapa local no topo da pilha de escopos.

Isso permite que chamadas recursivas possuam cópias independentes de:

- parâmetros;
- variáveis locais.

Ao final da chamada, o escopo correspondente é removido.

## 6.6 Interpretação

A classe responsável é:

```text
interpreter.Interpreter
```

A interpretação do programa ocorre nesta ordem:

1. limpa o ambiente de execução;
2. processa variáveis globais e funções;
3. executa os comandos do `main`;
4. avalia a expressão final;
5. retorna um valor `long`.

### Chamada de função

Ao interpretar uma chamada:

1. localiza a declaração da função;
2. confirma a quantidade de argumentos;
3. avalia os argumentos da direita para a esquerda;
4. cria um novo escopo local;
5. associa os valores aos parâmetros;
6. inicializa as variáveis locais em ordem;
7. executa os comandos;
8. avalia a expressão de retorno;
9. remove o escopo local;
10. devolve o valor produzido.

### Divisão por zero

O interpretador detecta explicitamente divisão por zero e lança `InterpreterException`.

Exemplo de mensagem:

```text
Erro de interpretação na linha 4, coluna 14: Divisão por zero.
```

### Condições

O `if` e o `while` consideram:

```text
0                    → falso
qualquer outro valor → verdadeiro
```

## 6.7 Geração de Código Assembly

A classe responsável é:

```text
codegen.CodeGenerator
```

O Assembly utiliza:

- arquitetura x86-64;
- sintaxe AT&T;
- ponto de entrada `_start`;
- endereçamento relativo ao RIP;
- pilha do sistema para argumentos e variáveis locais;
- `%rbp` como referência do registro de ativação;
- `%rax` como registrador de resultado.

### Variáveis globais

As variáveis globais são reservadas na seção `.bss`:

```asm
.section .bss

x: .zero 8
y: .zero 8
```

Leitura:

```asm
mov x(%rip), %rax
```

Escrita:

```asm
mov %rax, x(%rip)
```

### Programa principal

O código principal começa com:

```asm
.section .text
.globl _start
_start:
```

Depois:

1. inicializa as variáveis globais;
2. executa os comandos do `main`;
3. gera a expressão final;
4. chama `imprime_num`;
5. chama `sair`.

### Operações binárias

O gerador avalia primeiro o lado direito:

```text
generateExpression(right)
push %rax
generateExpression(left)
pop %rbx
```

Após isso:

```text
%rax = lado esquerdo
%rbx = lado direito
```

Operações:

```asm
add %rbx, %rax
sub %rbx, %rax
imul %rbx, %rax
cqto
idiv %rbx
```

Comparações:

```asm
xor %rcx, %rcx
cmp %rbx, %rax
setz/setl/setg %cl
mov %rcx, %rax
```

Correspondência:

```text
== → setz
<  → setl
>  → setg
```

### Chamada de função

Os argumentos são avaliados e empilhados em ordem inversa.

Exemplo:

```text
f(11, 202)
```

Modelo:

```asm
mov $202, %rax
push %rax

mov $11, %rax
push %rax

call f
add $16, %rsp
```

O chamador remove os argumentos da pilha depois da instrução `call`.

### Registro de ativação

Cada função utiliza um registro de ativação na pilha.

Prólogo:

```asm
funcao:
push %rbp
sub $<bytes_locais>, %rsp
mov %rsp, %rbp
```

Epílogo:

```asm
add $<bytes_locais>, %rsp
pop %rbp
ret
```

As variáveis locais são acessadas por deslocamentos a partir de `%rbp`.

Exemplo:

```asm
mov 0(%rbp), %rax
mov %rax, 8(%rbp)
```

Os parâmetros ficam depois:

- das variáveis locais;
- do `%rbp` anterior;
- do endereço de retorno.

Para uma função com `L` variáveis locais, o deslocamento do parâmetro de índice `i` é calculado por:

```text
L × 8 + 16 + i × 8
```

### Funções no arquivo Assembly

As funções são emitidas na seção `.text`, junto ao programa principal, antes da inclusão de `runtime.s`.

Modelo geral:

```asm
.section .bss
# variáveis globais

.section .text
.globl _start

_start:
# programa principal
call imprime_num
call sair

# funções geradas

.include "runtime.s"
```

---

# 7. Exceções e Tratamento de Erros

O pacote `exception` define uma hierarquia específica:

```text
CompilerException
├── LexicalException
├── ParserException
├── SemanticException
├── InterpreterException
└── CodeGenerationException
```

`CompilerException` armazena:

```text
fase
linha
coluna
mensagem
```

Formato geral:

```text
Erro <fase> na linha <linha>, coluna <coluna>: <mensagem>
```

O `Main` trata:

- `IOException` como erro de entrada/saída;
- `CompilerException` como erro esperado de uma fase do compilador;
- outras `RuntimeException` como erro interno do compilador.

## 7.1 Erro léxico

Entrada:

```text
main {
    return @;
}
```

Saída:

```text
Erro léxico na linha 2, coluna 12: Caractere inválido '@'.
```

## 7.2 Identificador iniciado por número

Entrada:

```text
var 123abc = 10;

main {
    return 0;
}
```

Saída:

```text
Erro léxico na linha 1, coluna 5: Identificador iniciado por número.
```

## 7.3 Erro sintático

Entrada:

```text
main {
    return 42
}
```

Saída semelhante a:

```text
Erro sintático na linha 3, coluna 1: Esperado PONTO_VIRGULA mas encontrado CHAVE_DIR.
```

## 7.4 Variável não declarada

Entrada:

```text
main {
    return x;
}
```

Saída:

```text
Erro semântico na linha 2, coluna 12: Variável 'x' não declarada.
```

## 7.5 Função não declarada

Entrada:

```text
main {
    return calcular(10);
}
```

Saída:

```text
Erro semântico na linha 2, coluna 12: Função 'calcular' não declarada.
```

## 7.6 Quantidade incorreta de argumentos

Entrada:

```text
fun soma(a, b) {
    return a + b;
}

main {
    return soma(10);
}
```

Saída:

```text
Erro semântico na linha 6, coluna 12: Função 'soma' esperava 2 argumento(s), mas recebeu 1.
```

## 7.7 Declaração local duplicada

Entrada:

```text
fun f(x) {
    var y = 1;
    var y = 2;

    return y;
}

main {
    return f(0);
}
```

Saída semelhante a:

```text
Erro semântico na linha 3, coluna 5: Variável local 'y' já foi declarada na função 'f'.
```

---

# 8. Testes Recomendados

## Teste 1 — Programa mínimo

Arquivo:

```text
teste1.fun
```

Entrada:

```text
main {
    return 42;
}
```

Resultado esperado:

```text
42
```

## Teste 2 — Variável global

```text
var x = 10;

main {
    return x;
}
```

Resultado esperado:

```text
10
```

## Teste 3 — Função sem parâmetros

```text
fun resposta() {
    return 42;
}

main {
    return resposta();
}
```

Resultado esperado:

```text
42
```

## Teste 4 — Função com parâmetros

```text
fun soma(a, b) {
    return a + b;
}

main {
    return soma(10, 5);
}
```

Resultado esperado:

```text
15
```

## Teste 5 — Variáveis locais

```text
fun somaQuadrados(a, b) {
    var quadradoA = a * a;
    var quadradoB = b * b;

    return quadradoA + quadradoB;
}

main {
    return somaQuadrados(3, 4);
}
```

Resultado esperado:

```text
25
```

## Teste 6 — Função chamando outra função

```text
fun quadrado(x) {
    return x * x;
}

fun somaQuadrados(a, b) {
    return quadrado(a) + quadrado(b);
}

main {
    return somaQuadrados(3, 4);
}
```

Resultado esperado:

```text
25
```

## Teste 7 — Sombreamento

```text
var x = 100;

fun incrementar(x) {
    var resultado = x + 1;

    return resultado;
}

main {
    return incrementar(9) + x;
}
```

Resultado esperado:

```text
110
```

## Teste 8 — `if` dentro de função

```text
fun absoluto(x) {
    var resultado = 0;

    if x < 0 {
        resultado = 0 - x;
    } else {
        resultado = x;
    }

    return resultado;
}

main {
    return absoluto(0 - 15);
}
```

Resultado esperado:

```text
15
```

## Teste 9 — `while` dentro de função

```text
fun somatorio(limite) {
    var n = 1;
    var soma = 0;

    while n < limite {
        soma = soma + n;
        n = n + 1;
    }

    return soma;
}

main {
    return somatorio(10);
}
```

Resultado esperado:

```text
45
```

## Teste 10 — Recursão direta

```text
fun fib(n) {
    var resultado = 0;

    if n < 2 {
        resultado = 1;
    } else {
        resultado = fib(n - 1) + fib(n - 2);
    }

    return resultado;
}

main {
    return fib(6);
}
```

Resultado esperado:

```text
13
```

## Teste 11 — Fatorial recursivo

```text
fun fatorial(n) {
    var resultado = 1;

    if n > 1 {
        resultado = n * fatorial(n - 1);
    } else {
    }

    return resultado;
}

main {
    return fatorial(5);
}
```

Resultado esperado:

```text
120
```

## Teste 12 — Erro de quantidade de argumentos

```text
fun dobro(x) {
    return x * 2;
}

main {
    return dobro(10, 20);
}
```

Resultado esperado:

```text
Erro semântico na linha 6, coluna 12: Função 'dobro' esperava 1 argumento(s), mas recebeu 2.
```

## Teste 13 — Erro de variável local

```text
fun f(x) {
    return x + y;
}

main {
    return f(10);
}
```

Resultado esperado:

```text
Erro semântico na linha 2, coluna 16: Variável 'y' não declarada.
```

## Teste 14 — Erro léxico com localização

```text
main {
    return @;
}
```

Resultado esperado:

```text
Erro léxico na linha 2, coluna 12: Caractere inválido '@'.
```

---

# 9. Arquivos Gerados

Ao final de uma compilação bem-sucedida, o compilador gera um arquivo Assembly dentro da pasta `output`.

Exemplo:

```text
input/programa.fun
```

gera:

```text
output/programa.s
```

O nome do arquivo de saída é derivado do nome do arquivo de entrada, removendo sua extensão original.

---

# 10. Montagem e Execução do Assembly

O código Assembly gerado utiliza sintaxe AT&T e foi projetado para Linux x86-64 ou ambiente compatível, como WSL.

O arquivo gerado contém:

```asm
.include "runtime.s"
```

O montador precisa encontrar `runtime.s` no diretório de trabalho.

Uma possibilidade é copiar `runtime.s` para `output`:

```text
Projeto/
└── output/
    ├── programa.s
    └── runtime.s
```

Depois, entre em `output`:

```bash
cd output
```

Monte:

```bash
as --64 programa.s -o programa.o
```

Faça a ligação:

```bash
ld programa.o -o programa
```

Execute:

```bash
./programa
```

A rotina `imprime_num`, fornecida pelo `runtime.s`, imprime o valor armazenado em `%rax`. A rotina `sair` encerra o processo.

---

# 11. Evolução das Linguagens

| Recurso                              | EV  | Cmd | Fun |
|--------------------------------------|:---:|:---:|:---:|
| Números inteiros                     | Sim | Sim | Sim |
| Declarações de variáveis             | Sim | Sim | Sim |
| Expressões aritméticas               | Sim | Sim | Sim |
| Atribuição após declaração           | Não | Sim | Sim |
| Comparações `<`, `>` e `==`          | Não | Sim | Sim |
| Blocos com `{` e `}`                 | Não | Sim | Sim |
| Comando `if`/`else`                  | Não | Sim | Sim |
| Comando `while`                      | Não | Sim | Sim |
| Expressão final com `return`         | Não | Sim | Sim |
| Declarações com palavra-chave `var`  | Não | Não | Sim |
| Bloco principal com `main`           | Não | Não | Sim |
| Declarações de função                | Não | Não | Sim |
| Parâmetros                           | Não | Não | Sim |
| Variáveis locais                     | Não | Não | Sim |
| Chamadas em expressões               | Não | Não | Sim |
| Recursão direta                      | Não | Não | Sim |
| Registro de ativação na pilha        | Não | Não | Sim |
| Localização por linha e coluna       | Não | Não | Sim |
| Exceções específicas por fase        | Não | Não | Sim |

---
