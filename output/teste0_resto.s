.section .bss
valor: .zero 8

.section .text
.globl _start
_start:
mov $17, %rax
mov %rax, valor(%rip)
mov $10, %rax
push %rax
call ehPar
add $8, %rsp
push %rax
mov $5, %rax
push %rax
mov valor(%rip), %rax
pop %rbx
cqto
idiv %rbx
mov %rdx, %rax
pop %rbx
add %rbx, %rax
call imprime_num
call sair

ehPar:
push %rbp
mov %rsp, %rbp
mov $0, %rax
push %rax
mov $2, %rax
push %rax
mov 16(%rbp), %rax
pop %rbx
cqto
idiv %rbx
mov %rdx, %rax
pop %rbx
xor %rcx, %rcx
cmp %rbx, %rax
setz %cl
mov %rcx, %rax
pop %rbp
ret
.include "runtime.s"
