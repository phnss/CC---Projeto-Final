.section .bss
contador: .zero 8

.section .text
.globl _start
_start:
mov $0, %rax
mov %rax, contador(%rip)
Linicio0:
mov $5, %rax
push %rax
mov contador(%rip), %rax
pop %rbx
xor %rcx, %rcx
cmp %rbx, %rax
setl %cl
mov %rcx, %rax
cmp $0, %rax
jz Lfim0
mov $1, %rax
push %rax
mov contador(%rip), %rax
pop %rbx
add %rbx, %rax
mov %rax, contador(%rip)
jmp Linicio0
Lfim0:
mov $1, %rax
push %rax
mov contador(%rip), %rax
pop %rbx
sub %rbx, %rax
mov %rax, contador(%rip)
mov contador(%rip), %rax
push %rax
call ajustar
add $8, %rsp
call imprime_num
call sair

ajustar:
push %rbp
sub $8, %rsp
mov %rsp, %rbp
mov 24(%rbp), %rax
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
add %rbx, %rax
mov %rax, 0(%rbp)
mov $1, %rax
push %rax
mov 0(%rbp), %rax
pop %rbx
sub %rbx, %rax
mov %rax, 0(%rbp)
mov 0(%rbp), %rax
add $8, %rsp
pop %rbp
ret
.include "runtime.s"
