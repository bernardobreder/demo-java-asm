	.section	__TEXT,__text,regular,pure_instructions
	.globl	_main
	.align	4, 0x90
_main:                                  ## @main
	.cfi_startproc
## BB#0:
	pushq	%rbp
Ltmp2:
	.cfi_def_cfa_offset 16
Ltmp3:
	.cfi_offset %rbp, -16
	movq	%rsp, %rbp
Ltmp4:
	.cfi_def_cfa_register %rbp
	subq	$32, %rsp
	movabsq	$0, %rax
	movabsq	$8192, %rsi             ## imm = 0x2000
	movl	$7, %edx
	movl	$4098, %ecx             ## imm = 0x1002
	movl	$0, %r8d
	movl	$0, -4(%rbp)
	movq	%rax, %rdi
	movq	%rax, %r9
	callq	_mmap
	movq	%rax, -16(%rbp)
	movq	-16(%rbp), %rax
	movq	%rax, -24(%rbp)
	movq	-24(%rbp), %rax
	movq	%rax, %rsi
	addq	$1, %rsi
	movq	%rsi, -24(%rbp)
	movb	$-80, (%rax)
	movq	-24(%rbp), %rax
	movq	%rax, %rsi
	addq	$1, %rsi
	movq	%rsi, -24(%rbp)
	movb	$1, (%rax)
	movq	-24(%rbp), %rax
	movq	%rax, %rsi
	addq	$1, %rsi
	movq	%rsi, -24(%rbp)
	movb	$-79, (%rax)
	movq	-24(%rbp), %rax
	movq	%rax, %rsi
	addq	$1, %rsi
	movq	%rsi, -24(%rbp)
	movb	$2, (%rax)
	movq	-24(%rbp), %rax
	movq	%rax, %rsi
	addq	$1, %rsi
	movq	%rsi, -24(%rbp)
	movb	$0, (%rax)
	movq	-24(%rbp), %rax
	movq	%rax, %rsi
	addq	$1, %rsi
	movq	%rsi, -24(%rbp)
	movb	$-56, (%rax)
	movq	-24(%rbp), %rax
	movq	%rax, %rsi
	addq	$1, %rsi
	movq	%rsi, -24(%rbp)
	movb	$-61, (%rax)
	movq	-16(%rbp), %rax
	movq	%rax, -32(%rbp)
	callq	*-32(%rbp)
	addq	$32, %rsp
	popq	%rbp
	ret
	.cfi_endproc


.subsections_via_symbols
