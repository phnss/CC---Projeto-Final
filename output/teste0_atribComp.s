.section .bss

.section .text
.globl _start
_start:
mov $10, %rax
push %rax
call calcular
add $8, %rsp
call imprime_num
call sair

calcular:
push %rbp
sub $8, %rsp
mov %rsp, %rbp
mov 24(%rbp), %rax
mov %rax, 0(%rbp)
mov $6, %rax
push %rax
mov 0(%rbp), %rax
pop %rbx
add %rbx, %rax
mov %rax, 0(%rbp)
mov $2, %rax
push %rax
mov 0(%rbp), %rax
pop %rbx
sub %rbx, %rax
mov %rax, 0(%rbp)
mov $3, %rax
push %rax
mov 0(%rbp), %rax
pop %rbx
imul %rbx, %rax
mov %rax, 0(%rbp)
mov $2, %rax
push %rax
mov 0(%rbp), %rax
pop %rbx
cqto
idiv %rbx
mov %rax, 0(%rbp)
mov $8, %rax
push %rax
mov 0(%rbp), %rax
pop %rbx
cqto
idiv %rbx
mov %rdx, %rax
mov %rax, 0(%rbp)
mov $2, %rax
push %rax
mov 0(%rbp), %rax
pop %rbx
mov %rbx, %rcx
sal %cl, %rax
mov %rax, 0(%rbp)
mov $1, %rax
push %rax
mov 0(%rbp), %rax
pop %rbx
mov %rbx, %rcx
sar %cl, %rax
mov %rax, 0(%rbp)
mov 0(%rbp), %rax
add $8, %rsp
pop %rbp
ret
.include "runtime.s"
