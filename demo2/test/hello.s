	.section	__TEXT,__text,regular,pure_instructions
	.globl	_main
	.align	4, 0x90
_main:
	pushl	%ebp
	movl	%esp, %ebp
	subl	$28, %esp
	movl	12(%ebp), %eax
	movl	8(%ebp), %ecx
	movl	%ecx, -4(%ebp)
	movl	%eax, -8(%ebp)
	movl	$-1, -20(%ebp)
	movl	$-286331154, -24(%ebp)
	movl	-20(%ebp), %eax
	movl	-24(%ebp), %ecx
	addl	%ecx, %eax
	movl	%eax, -28(%ebp)
	movl	-28(%ebp), %eax
	movl	%eax, -16(%ebp)
	movl	-16(%ebp), %eax
	movl	%eax, -12(%ebp)
	movl	-12(%ebp), %eax
	addl	$28, %esp
	popl	%ebp
	ret


.subsections_via_symbols
