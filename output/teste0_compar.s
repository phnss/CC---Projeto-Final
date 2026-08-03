.section .bss
x: .zero 8

.section .text
.globl _start
_start:
mov $100, %rax
mov %rax, x(%rip)
mov $100, %rax
push %rax
mov x(%rip), %rax
pop %rbx
xor %rcx, %rcx
cmp %rbx, %rax
setge %cl
mov %rcx, %rax
cmp $0, %rax
jz Lfalso0
mov $50, %rax
push %rax
mov x(%rip), %rax
pop %rbx
sub %rbx, %rax
mov %rax, x(%rip)
jmp Lfim0
Lfalso0:
Lfim0:
mov x(%rip), %rax
call imprime_num
call sair
.include "runtime.s"
