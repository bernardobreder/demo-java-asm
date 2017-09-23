#ifndef MAIN_H
#define MAIN_H

void* jit_function_new(unsigned int size);

void jit_function_free(void* self, unsigned int size);

#ifdef __x86_64__

// 55 - pushq %rbp
#define jit_asm_push_bp_size 1
#define jit_asm_push_bp(b) *(b++) = 0x55;
// 5d - popq %rbp
#define jit_asm_pop_bp_size 1
#define jit_asm_pop_bp(b) *(b++) = 0x5D;
// 48 89 e5 - movq %rsp, %rbp
#define jit_asm_mov_sp_to_bp_size 3
#define jit_asm_mov_sp_to_bp(b) *(b++) = 0x48; *(b++) = 0x89; *(b++) = 0xE5;
// 83 c0 v0 - addl $v, %eax
#define jit_asm_byte_sum_ax_size 3
#define jit_asm_byte_sum_ax(b,v) *(b++) = 0x83; *(b++) = 0xC0; *(b++) = v & 0xFF;
// 83 e8 v0 - subl $v, %eax
#define jit_asm_byte_sub_ax_size 3
#define jit_asm_byte_sub_ax(b,v) *(b++) = 0x83; *(b++) = 0xE8; *(b++) = v & 0xFF;
// 6b c0 v0 - imull $v, %eax
#define jit_asm_byte_mul_ax_size 3
#define jit_asm_byte_mul_ax(b,v) *(b++) = 0x6B; *(b++) = 0xC0; *(b++) = v & 0xFF;
// c1 f8 v0 - sarl	$v, %eax
#define jit_asm_byte_shift_right_ax_size 3
#define jit_asm_byte_shift_right_ax(b,v) *(b++) = 0xC1; *(b++) = 0xF8; *(b++) = v & 0xFF;
// c1 e0 v0 - shll	$v, %eax
#define jit_asm_byte_shift_left_ax_size 3
#define jit_asm_byte_shift_left_ax(b,v) *(b++) = 0xC1; *(b++) = 0xE0; *(b++) = v & 0xFF;
// b8 v0 v1 v2 v3 - movl $v, %eax
#define jit_asm_int_mov_eax_size 5
#define jit_asm_int_mov_eax(b,v) *(b++) = 0xB8; *(b++) = ((v)&0xFF); *(b++) = (((v)>>8)&0xFF); *(b++) = (((v)>>16)&0xFF); *(b++) = (((v)>>24)&0xFF);
// b9 v0 v1 v2 v3 - movl $v, %eax
#define jit_asm_int_mov_ecx_size 5
#define jit_asm_int_mov_ecx(b,v) *(b++) = 0xB9; *(b++) = ((v)&0xFF); *(b++) = (((v)>>8)&0xFF); *(b++) = (((v)>>16)&0xFF); *(b++) = (((v)>>24)&0xFF);
// 05 v0 v1 v2 v3 - addl $v, %eax - eax = isum(v, eax)
#define jit_asm_int_sum_ax_size 5
#define jit_asm_int_sum_ax(b,v) *(b++) = 0x05; *(b++) = ((v)&0xFF); *(b++) = (((v)>>8)&0xFF); *(b++) = (((v)>>16)&0xFF); *(b++) = (((v)>>24)&0xFF);
// 05 v0 v1 v2 v3 - addl $v, %eax - eax = isum(v, eax)
#define jit_asm_int_sum_eax_ecx_size 2
#define jit_asm_int_sum_eax_ecx(b) *(b++) = 0x01; *(b++) = 0xC8;
// 2d v0 v1 v2 v3 - subl $v, %eax - eax = isub(v, eax)
#define jit_asm_int_sub_ax_size 5
#define jit_asm_int_sub_ax(b,v) *(b++) = 0x2D; *(b++) = ((v)&0xFF); *(b++) = (((v)>>8)&0xFF); *(b++) = (((v)>>16)&0xFF); *(b++) = (((v)>>24)&0xFF);
// 05 v0 v1 v2 v3 - addl $v, %eax - eax = isum(v, eax)
#define jit_asm_int_sub_eax_ecx_size 2
#define jit_asm_int_sub_eax_ecx(b) *(b++) = 0x29; *(b++) = 0xC8;
// 69 c0 v0 v1 v2 v3 - eax = imul(v, eax)
#define jit_asm_int_mul_ax_size 6
#define jit_asm_int_mul_ax(b,v) *(b++) = 0x69; *(b++) = 0xC0; *(b++) = ((v)&0xFF); *(b++) = (((v)>>8)&0xFF); *(b++) = (((v)>>16)&0xFF); *(b++) = (((v)>>24)&0xFF);
// 05 v0 v1 v2 v3 - addl $v, %eax - eax = isum(v, eax)
#define jit_asm_int_mul_eax_ecx_size 3
#define jit_asm_int_mul_eax_ecx(b) *(b++) = 0x0F; *(b++) = 0xAF; *(b++) = 0xC1;
// 99 f7 f9 - eax = idiv(eax, ecx)
#define jit_asm_int_div_eax_ecx_size 3
#define jit_asm_int_div_eax_ecx(b) *(b++) = 0x99; *(b++) = 0xF7; *(b++) = 0xF9;
// 48 83 c4 v0 - addq $v, %rsp
#define jit_asm_sum_sp_size 4
#define jit_asm_sum_sp(b,v) *(b++) = 0x48; *(b++) = 0x83; *(b++) = 0xC4; *(b++) = v & 0xFF;
// 48 83 ec v0 - subq $v, %rsp
#define jit_asm_sub_sp_size 4
#define jit_asm_sub_sp(b,v) *(b++) = 0x48; *(b++) = 0x83; *(b++) = 0xEC; *(b++) = v & 0xFF;

#define jit_asm_mov_bp_int32_size 4
void jit_asm_mov_irbp_to_eax(unsigned char** bytes, char index) ;
// c3 - ret
#define jit_asm_ret_size 1
#define jit_asm_ret(b) *(b++) = 0xC3;

#else

// 55 - pushl %ebp
#define jit_asm_push_bp_size 1
#define jit_asm_push_bp(b) *(b++) = 0x55
// 5d - popl %ebp
#define jit_asm_pop_bp_size 1
#define jit_asm_pop_bp(b) *(b++) = 0x5D
// 89 e5 - movl %esp, %ebp
#define jit_asm_mov_sp_to_bp_size 2
#define jit_asm_mov_sp_to_bp(b) *(b++) = 0x89; *(b++) = 0xE5;
// 83 c0 v0 - addl $v, %eax
#define jit_asm_byte_sum_ax_size 3
#define jit_asm_byte_sum_ax(b,v) *(b++) = 0x83; *(b++) = 0xC0; *(b++) = v & 0xFF;
// 83 e8 v0 - subl $v, %eax
#define jit_asm_byte_sub_ax_size 3
#define jit_asm_byte_sub_ax(b,v) *(b++) = 0x83; *(b++) = 0xE8; *(b++) = v & 0xFF;
// 6b c0 v0 - imull $v, %eax
#define jit_asm_byte_mul_ax_size 3
#define jit_asm_byte_mul_ax(b,v) *(b++) = 0x6B; *(b++) = 0xC0; *(b++) = v & 0xFF;
// c1 f8 v0 - sarl	$v, %eax
#define jit_asm_byte_shift_right_ax_size 3
#define jit_asm_byte_shift_right_ax(b,v) *(b++) = 0xC1; *(b++) = 0xF8; *(b++) = v & 0xFF;
// c1 e0 v0 - shll	$v, %eax
#define jit_asm_byte_shift_left_ax_size 3
#define jit_asm_byte_shift_left_ax(b,v) *(b++) = 0xC1; *(b++) = 0xE0; *(b++) = v & 0xFF;
// b8 02 00 00 00 - movl $v, %eax
#define jit_asm_int_mov_eax_size 5
#define jit_asm_int_mov_eax(b,v) *(b++) = 0xB8; *(b++) = ((v)&0xFF); *(b++) = (((v)>>8)&0xFF); *(b++) = (((v)>>16)&0xFF); *(b++) = (((v)>>24)&0xFF);
// 05 v0 v1 v2 v3 - addl $v, %eax
#define jit_asm_int_sum_ax_size 5
#define jit_asm_int_sum_ax(b,v) *(b++) = 0x05; *(b++) = ((v)&0xFF); *(b++) = (((v)>>8)&0xFF); *(b++) = (((v)>>16)&0xFF); *(b++) = (((v)>>24)&0xFF);
// 05 v0 v1 v2 v3 - addl $v, %eax - eax = isum(v, eax)
#define jit_asm_int_sum_eax_ecx_size 2
#define jit_asm_int_sum_eax_ecx(b) *(b++) = 0x01; *(b++) = 0xC8;
// 2d v0 v1 v2 v3 - addl $v, %eax
#define jit_asm_int_sub_ax_size 5
#define jit_asm_int_sub_ax(b,v) *(b++) = 0x2D; *(b++) = ((v)&0xFF); *(b++) = (((v)>>8)&0xFF); *(b++) = (((v)>>16)&0xFF); *(b++) = (((v)>>24)&0xFF);
// 05 v0 v1 v2 v3 - addl $v, %eax - eax = isum(v, eax)
#define jit_asm_int_sub_eax_ecx_size 2
#define jit_asm_int_sub_eax_ecx(b) *(b++) = 0x29; *(b++) = 0xC8;
// 69 c0 v0 v1 v2 v3 - addl $v, %eax
#define jit_asm_int_mul_ax_size 6
#define jit_asm_int_mul_ax(b,v) *(b++) = 0x69; *(b++) = 0xC0; *(b++) = ((v)&0xFF); *(b++) = (((v)>>8)&0xFF); *(b++) = (((v)>>16)&0xFF); *(b++) = (((v)>>24)&0xFF);
// 05 v0 v1 v2 v3 - addl $v, %eax - eax = isum(v, eax)
#define jit_asm_int_mul_eax_ecx_size 3
#define jit_asm_int_mul_eax_ecx(b) *(b++) = 0x0F; *(b++) = 0xAF; *(b++) = 0xC1;
// 99 f7 f9 - eax = idiv(eax, ecx)
#define jit_asm_int_div_eax_ecx_size 3
#define jit_asm_int_div_eax_ecx(b) *(b++) = 0x99; *(b++) = 0xF7; *(b++) = 0xF9;
// 83 c4 v0 - addl $24, %esp
#define jit_asm_sum_sp_size 3
#define jit_asm_sum_sp(b,v) *(b++) = 0x48; *(b++) = 0x83; *(b++) = 0xC4; *(b++) = v & 0xFF;
// 83 ec v0 - subl	$v, %esp
#define jit_asm_sub_sp_size 3
#define jit_asm_sub_sp(b,v) *(b++) = 0x83; *(b++) = 0xEC; *(b++) = v & 0xFF;
// c3 - ret
#define jit_asm_ret_size 1
#define jit_asm_ret(b) *(b++) = 0xC3;

#endif

#endif
