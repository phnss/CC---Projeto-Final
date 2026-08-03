.section .bss

.section .text
.globl _start
_start:
mov $5, %rax
push %rax
mov $17, %rax
pop %rbx
cqto
idiv %rbx
mov %rdx, %rax
push %rax
mov $1, %rax
push %rax
mov $2, %rax
pop %rbx
add %rbx, %rax
push %rax
mov $3, %rax
pop %rbx
mov %rbx, %rcx
sal %cl, %rax
push %rax
mov $3, %rax
push %rax
mov $64, %rax
pop %rbx
mov %rbx, %rcx
sar %cl, %rax
push %rax
mov $3, %rax
push %rax
mov $5, %rax
push %rax
call ajustar
add $16, %rsp
pop %rbx
add %rbx, %rax
pop %rbx
add %rbx, %rax
pop %rbx
add %rbx, %rax
call imprime_num
call sair

ajustar:
push %rbp
sub $8, %rsp
mov %rsp, %rbp
mov 32(%rbp), %rax
push %rax
mov 24(%rbp), %rax
pop %rbx
mov %rbx, %rcx
sal %cl, %rax
mov %rax, 0(%rbp)
mov $1, %rax
push %rax
mov 0(%rbp), %rax
pop %rbx
add %rbx, %rax
mov %rax, 0(%rbp)
mov $1, %rax
push %rax
mov 0(%rbp), %rax
pop %rbx
mov %rbx, %rcx
sar %cl, %rax
add $8, %rsp
pop %rbp
ret
.include "runtime.s"
