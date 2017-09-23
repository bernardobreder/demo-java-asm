#include <stdio.h>
#include <assert.h>
#include <sys/mman.h>
#include "asm.h"
#include "main.h"

void* jit_function_new(unsigned int size) {
	return mmap(0, size, PROT_READ | PROT_WRITE | PROT_EXEC, MAP_ANON | MAP_PRIVATE, 0, 0);
}

void jit_function_free(void* self, unsigned int size) {
	munmap(self, size);
}

#define CGL_OPCODE_SYSTEM_HALF 255

#define CGL_OPCODE_STACK_PROLOG 1
#define CGL_OPCODE_STACK_LEAVE 2
#define CGL_OPCODE_STACK_RET 3
#define CGL_OPCODE_STACK_PUSH 4
#define CGL_OPCODE_STACK_POP 5
#define CGL_OPCODE_STACK_INT 6

#define CGL_OPCODE_INT_LOAD_A 10
#define CGL_OPCODE_INT_LOAD_C 11
#define CGL_OPCODE_INT_SUM_AC 12
#define CGL_OPCODE_INT_SUB_AC 13
#define CGL_OPCODE_INT_MUL_AC 14
#define CGL_OPCODE_INT_DIV_AC 15

void jit_asm_call_address(unsigned char** bytes, void* next) {
	unsigned int address = (unsigned int) next;
	**bytes = 0xE8;
	(*bytes)++;
	**bytes = address & 0xFF;
	(*bytes)++;
	**bytes = (address >> 8) & 0xFF;
	(*bytes)++;
	**bytes = (address >> 16) & 0xFF;
	(*bytes)++;
	**bytes = address >> 24;
	(*bytes)++;
}

void jit_asm_call_int(unsigned char** bytes, int next) {
	next -= 5;
	**bytes = 0xE8;
	(*bytes)++;
	**bytes = next & 0xFF;
	(*bytes)++;
	**bytes = (next >> 8) & 0xFF;
	(*bytes)++;
	**bytes = (next >> 16) & 0xFF;
	(*bytes)++;
	**bytes = next >> 24;
	(*bytes)++;
}

void jit_asm_cmp_int_with_eax(unsigned char** bytes, char value) {
	**bytes = 0x83;
	(*bytes)++;
	**bytes = 0xF8;
	(*bytes)++;
	**bytes = value;
	(*bytes)++;
}

unsigned int jit_asm_jmp_neq_size(unsigned char* bytes, unsigned char* address) {
	int c = (void*) address - (void*) bytes - 1;
	if (c < 256) {
		return 2;
	} else {
		assert(0);
		return 0;
	}
}

/**
 * O valor de next deve ser o número de bytes que irá pular depois da instrução jmp.
 */
void jit_asm_jmp_neq(unsigned char** bytes, char next) {
	**bytes = 0x75;
	(*bytes)++;
	**bytes = next;
	(*bytes)++;
}

void jit_asm_jmp_eq(unsigned char** bytes, int next) {
	if (next > -0x80 && next < 0x80) {
		**bytes = 0x74;
		(*bytes)++;
		**bytes = next & 0xFF;
		(*bytes)++;
	} else {
		**bytes = 0x0F;
		(*bytes)++;
		**bytes = 0x84;
		(*bytes)++;
		**bytes = next & 0xFF;
		(*bytes)++;
		**bytes = (next >> 8) & 0xFF;
		(*bytes)++;
		**bytes = (next >> 16) & 0xFF;
		(*bytes)++;
		**bytes = next >> 24;
		(*bytes)++;
	}
}

void jit_asm_mov_int_to_irbp(unsigned char** bytes, char index, int value) {
	**bytes = 0xC7;
	(*bytes)++;
	**bytes = 0x45;
	(*bytes)++;
	**bytes = 0x100 - index;
	(*bytes)++;
	**bytes = value & 0xFF;
	(*bytes)++;
	**bytes = (value >> 8) & 0xFF;
	(*bytes)++;
	**bytes = (value >> 16) & 0xFF;
	(*bytes)++;
	**bytes = value >> 24;
	(*bytes)++;
}

void jit_asm_mov_irbp_to_eax(unsigned char** bytes, char index) {
	**bytes = 0x8B;
	(*bytes)++;
	**bytes = 0x45;
	(*bytes)++;
	**bytes = 0x100 - index;
	(*bytes)++;
}

void jit_asm_mov_irbp_to_ecx(unsigned char** bytes, char index) {
	**bytes = 0x8B;
	(*bytes)++;
	**bytes = 0x4D;
	(*bytes)++;
	**bytes = 0x100 - index;
	(*bytes)++;
}

void jit_asm_mov_eax_to_irbp(unsigned char** bytes, char index) {
	**bytes = 0x89;
	(*bytes)++;
	**bytes = 0x45;
	(*bytes)++;
	**bytes = 0x100 - index;
	(*bytes)++;
}

void jit_vm_opcode_system_half(unsigned char** bytecodes) {
	**bytecodes = CGL_OPCODE_SYSTEM_HALF;
	(*bytecodes)++;
}

void jit_vm_opcode_stack_prolog(unsigned char** bytecodes) {
	**bytecodes = CGL_OPCODE_STACK_PROLOG;
	(*bytecodes)++;
}

void jit_vm_opcode_stack_leave(unsigned char** bytecodes) {
	**bytecodes = CGL_OPCODE_STACK_LEAVE;
	(*bytecodes)++;
}

void jit_vm_opcode_stack_return(unsigned char** bytecodes) {
	**bytecodes = CGL_OPCODE_STACK_RET;
	(*bytecodes)++;
}

void jit_vm_opcode_stack_push(unsigned char** bytecodes, unsigned char count) {
	**bytecodes = CGL_OPCODE_STACK_PUSH;
	(*bytecodes)++;
	**bytecodes = count & 0x7F;
	(*bytecodes)++;
}

void jit_vm_opcode_stack_pop(unsigned char** bytecodes, unsigned char count) {
	**bytecodes = CGL_OPCODE_STACK_POP;
	(*bytecodes)++;
	**bytecodes = count & 0x7F;
	(*bytecodes)++;
}

void jit_vm_opcode_stack_integer(unsigned char** bytecodes) {
	**bytecodes = CGL_OPCODE_STACK_INT;
	(*bytecodes)++;
}

void jit_vm_opcode_integer_load_a(unsigned char** bytecodes, int value) {
	**bytecodes = CGL_OPCODE_INT_LOAD_A;
	(*bytecodes)++;
	**bytecodes = value < 0 ? 0x80 : 0;
	value = value < 0 ? -value : value;
	**bytecodes = (value & 0x7F) >> 24;
	(*bytecodes)++;
	**bytecodes = (value >> 16) & 0xFF;
	(*bytecodes)++;
	**bytecodes = (value >> 8) & 0xFF;
	(*bytecodes)++;
	**bytecodes = value & 0xFF;
	(*bytecodes)++;
}

void jit_vm_opcode_integer_load_c(unsigned char** bytecodes, int value) {
	**bytecodes = CGL_OPCODE_INT_LOAD_C;
	(*bytecodes)++;
	**bytecodes = value < 0 ? 0x80 : 0;
	value = value < 0 ? -value : value;
	**bytecodes = (value & 0x7F) >> 24;
	(*bytecodes)++;
	**bytecodes = (value >> 16) & 0xFF;
	(*bytecodes)++;
	**bytecodes = (value >> 8) & 0xFF;
	(*bytecodes)++;
	**bytecodes = value & 0xFF;
	(*bytecodes)++;
}

void jit_vm_opcode_integer_sum_ac(unsigned char** bytecodes) {
	**bytecodes = CGL_OPCODE_INT_SUM_AC;
	(*bytecodes)++;
}

void jit_vm_opcode_integer_sub_ac(unsigned char** bytecodes) {
	**bytecodes = CGL_OPCODE_INT_SUB_AC;
	(*bytecodes)++;
}

void jit_vm_opcode_integer_mul_ac(unsigned char** bytecodes) {
	**bytecodes = CGL_OPCODE_INT_MUL_AC;
	(*bytecodes)++;
}

void jit_vm_opcode_integer_div_ac(unsigned char** bytecodes) {
	**bytecodes = CGL_OPCODE_INT_DIV_AC;
	(*bytecodes)++;
}

void* jit_vm_bytecode(unsigned char* bytecodes) {
	unsigned int size = 1024;
	unsigned char* address = (unsigned char *) jit_function_new(size);
	unsigned char* bytes = address;
	int intValueA;
	unsigned char signal;
	for (;;) {
		switch (*(bytecodes++)) {
		case CGL_OPCODE_STACK_PROLOG :
			jit_asm_push_bp(bytes)
			jit_asm_mov_sp_to_bp(bytes)
			break;
		case CGL_OPCODE_STACK_LEAVE :
			jit_asm_pop_bp(bytes)
			break;
		case CGL_OPCODE_STACK_RET :
			jit_asm_ret(bytes)
			break;
		case CGL_OPCODE_STACK_PUSH :
			intValueA = *(bytecodes++) & 0x7F;
			jit_asm_sub_sp(bytes, intValueA)
			break;
		case CGL_OPCODE_STACK_POP :
			intValueA = *(bytecodes++) & 0x7F;
			jit_asm_sum_sp(bytes, intValueA)
			break;
		case CGL_OPCODE_STACK_INT :
			break;
		case CGL_OPCODE_INT_LOAD_A :
			signal = (*bytecodes & 0x80) == 0x80 ? -1 : 1;
			intValueA = 0;
			intValueA += *(bytecodes++) & 0x7F;
			intValueA += *(bytecodes++) & 0xFF;
			intValueA += *(bytecodes++) & 0xFF;
			intValueA += *(bytecodes++) & 0xFF;
			intValueA *= signal;
			jit_asm_int_mov_eax(bytes, intValueA)
			break;
		case CGL_OPCODE_INT_LOAD_C :
			signal = (*bytecodes & 0x80) == 0x80 ? -1 : 1;
			intValueA = 0;
			intValueA += *(bytecodes++) & 0x7F;
			intValueA += *(bytecodes++) & 0xFF;
			intValueA += *(bytecodes++) & 0xFF;
			intValueA += *(bytecodes++) & 0xFF;
			intValueA *= signal;
			jit_asm_int_mov_ecx(bytes, intValueA)
			break;
		case CGL_OPCODE_INT_SUM_AC :
			jit_asm_int_sum_eax_ecx(bytes)
			break;
		case CGL_OPCODE_INT_SUB_AC :
			jit_asm_int_sub_eax_ecx(bytes)
			break;
		case CGL_OPCODE_INT_MUL_AC :
			jit_asm_int_mul_eax_ecx(bytes)
			break;
		case CGL_OPCODE_INT_DIV_AC :
			jit_asm_int_div_eax_ecx(bytes)
			break;
		case CGL_OPCODE_SYSTEM_HALF :
			return address;
		default :
			jit_function_free(address, size);
			return 0;
		}
	}
	return address;
}

typedef unsigned (*asmFunc)(void);

void jit_jit_asm_test() {
	{
		unsigned char* address = (unsigned char *) jit_function_new(1024);
		unsigned char* bytes = address;
		jit_asm_int_mov_eax(bytes, 10);
		jit_asm_ret(bytes);
		assert(10 == ((asmFunc ) address)());
		jit_function_free(address, 1024);
	}
	{
		unsigned char* address = (unsigned char *) jit_function_new(1024);
		unsigned char* bytes = address;
		jit_asm_push_bp(bytes);
		jit_asm_mov_sp_to_bp(bytes);
		jit_asm_int_mov_eax(bytes, 10);
		jit_asm_int_mov_ecx(bytes, 5);
		jit_asm_int_sum_eax_ecx(bytes);
		jit_asm_pop_bp(bytes);
		jit_asm_ret(bytes);
		assert(15 == ((asmFunc ) address)());
		jit_function_free(address, 1024);
	}
	{
		unsigned char* address = (unsigned char *) jit_function_new(1024);
		unsigned char* bytes = address;
		jit_asm_push_bp(bytes);
		jit_asm_mov_sp_to_bp(bytes);
		jit_asm_sub_sp(bytes, 8);
		jit_asm_int_mov_eax(bytes, 10);
		jit_asm_byte_sum_ax(bytes, 2);
		jit_asm_sum_sp(bytes, 8);
		jit_asm_pop_bp(bytes);
		jit_asm_ret(bytes);
		assert(12 == ((asmFunc ) address)());
		jit_function_free(address, 1024);
	}
	{
		unsigned char* address = (unsigned char *) jit_function_new(1024);
		unsigned char* bytes = address;
		jit_asm_push_bp(bytes);
		jit_asm_mov_sp_to_bp(bytes);
		jit_asm_sub_sp(bytes, 8);
		jit_asm_int_mov_eax(bytes, 10);
		jit_asm_byte_sub_ax(bytes, 2);
		jit_asm_sum_sp(bytes, 8);
		jit_asm_pop_bp(bytes);
		jit_asm_ret(bytes);
		assert(8 == ((asmFunc ) address)());
		jit_function_free(address, 1024);
	}
	{
		unsigned char* address = (unsigned char *) jit_function_new(1024);
		unsigned char* bytes = address;
		jit_asm_push_bp(bytes);
		jit_asm_mov_sp_to_bp(bytes);
		jit_asm_sub_sp(bytes, 8);
		jit_asm_int_mov_eax(bytes, 10);
		jit_asm_byte_mul_ax(bytes, 2);
		jit_asm_sum_sp(bytes, 8);
		jit_asm_pop_bp(bytes);
		jit_asm_ret(bytes);
		assert(20 == ((asmFunc ) address)());
		jit_function_free(address, 1024);
	}
	{
		unsigned char* address = (unsigned char *) jit_function_new(1024);
		unsigned char* bytes = address;
		jit_asm_push_bp(bytes);
		jit_asm_mov_sp_to_bp(bytes);
		jit_asm_sub_sp(bytes, 8);
		jit_asm_int_mov_eax(bytes, 10);
		jit_asm_int_sum_ax(bytes, 305419896);
		jit_asm_sum_sp(bytes, 8);
		jit_asm_pop_bp(bytes);
		jit_asm_ret(bytes);
		assert((305419896 + 10) == ((asmFunc ) address)());
		jit_function_free(address, 1024);
	}
	{
		unsigned char* address = (unsigned char *) jit_function_new(1024);
		unsigned char* bytes = address;
		jit_asm_push_bp(bytes);
		jit_asm_mov_sp_to_bp(bytes);
		jit_asm_sub_sp(bytes, 8);
		jit_asm_int_mov_eax(bytes, 10);
		jit_asm_int_sum_ax(bytes, 305419896);
		jit_asm_sum_sp(bytes, 8);
		jit_asm_pop_bp(bytes);
		jit_asm_ret(bytes);
		assert((305419896 + 10) == ((asmFunc ) address)());
		jit_function_free(address, 1024);
	}
	{
		unsigned char* address = (unsigned char *) jit_function_new(1024);
		unsigned char* bytes = address;
		jit_asm_push_bp(bytes);
		jit_asm_mov_sp_to_bp(bytes);
		jit_asm_sub_sp(bytes, 8);
		jit_asm_int_mov_eax(bytes, 1000);
		jit_asm_int_mul_ax(bytes, 1000);
		jit_asm_sum_sp(bytes, 8);
		jit_asm_pop_bp(bytes);
		jit_asm_ret(bytes);
		assert((1000 * 1000) == ((asmFunc ) address)());
		jit_function_free(address, 1024);
	}
	{
		unsigned char* address = (unsigned char *) jit_function_new(1024);
		unsigned char* bytes = address;
		jit_asm_int_mov_eax(bytes, 10);
		jit_asm_byte_shift_left_ax(bytes, 1);
		jit_asm_ret(bytes);
		assert(20 == ((asmFunc ) address)());
		jit_function_free(address, 1024);
	}
	{
		unsigned char* address = (unsigned char *) jit_function_new(1024);
		unsigned char* bytes = address;
		jit_asm_int_mov_eax(bytes, 10);
		jit_asm_byte_shift_left_ax(bytes, 6);
		jit_asm_ret(bytes);
		assert(640 == ((asmFunc ) address)());
		jit_function_free(address, 1024);
	}
	{
		unsigned char* address = (unsigned char *) jit_function_new(1024);
		unsigned char* bytes = address;
		jit_asm_int_mov_eax(bytes, 10);
		jit_asm_byte_shift_right_ax(bytes, 1);
		jit_asm_ret(bytes);
		assert(5 == ((asmFunc ) address)());
		jit_function_free(address, 1024);
	}
	{
		unsigned char* address = (unsigned char *) jit_function_new(1024);
		unsigned char* bytes = address;
		jit_asm_int_mov_eax(bytes, 640);
		jit_asm_byte_shift_right_ax(bytes, 6);
		jit_asm_ret(bytes);
		assert(10 == ((asmFunc ) address)());
		jit_function_free(address, 1024);
	}
	{
		unsigned char* address = (unsigned char *) jit_function_new(1024);
		unsigned char* bytes = address;
		jit_asm_int_mov_eax(bytes, 10);
		jit_asm_int_mov_ecx(bytes, 2);
		jit_asm_int_div_eax_ecx(bytes);
		jit_asm_ret(bytes);
		assert(5 == ((asmFunc ) address)());
		jit_function_free(address, 1024);
	}
	{
		unsigned char* address = (unsigned char *) jit_function_new(1024);
		unsigned char* bytes = address;
		jit_asm_int_mov_eax(bytes, 10);
		jit_asm_int_mov_ecx(bytes, 4);
		jit_asm_int_div_eax_ecx(bytes);
		jit_asm_ret(bytes);
		assert(2 == ((asmFunc ) address)());
		jit_function_free(address, 1024);
	}
	{
		unsigned char* address = (unsigned char *) jit_function_new(1024);
		unsigned char* bytes = address;
		jit_asm_mov_int_to_irbp(&bytes, 12, 10);
		jit_asm_mov_irbp_to_eax(&bytes, 12);
		jit_asm_ret(bytes);
		assert(10 == ((asmFunc ) address)());
		jit_function_free(address, 1024);
	}
	{
		unsigned char* address = (unsigned char *) jit_function_new(1024);
		unsigned char* bytes = address;
		jit_asm_push_bp(bytes);
		jit_asm_mov_sp_to_bp(bytes);
		jit_asm_sub_sp(bytes, 12);
		jit_asm_mov_int_to_irbp(&bytes, 12, 10);
		jit_asm_mov_irbp_to_eax(&bytes, 12);
		jit_asm_sum_sp(bytes, 12);
		jit_asm_pop_bp(bytes);
		jit_asm_ret(bytes);
		assert(10 == ((asmFunc ) address)());
		jit_function_free(address, 1024);
	}
	{
		unsigned char* address = (unsigned char *) jit_function_new(1024);
		unsigned char* bytes = address;
		jit_asm_push_bp(bytes);
		jit_asm_mov_sp_to_bp(bytes);
		jit_asm_sub_sp(bytes, 12);
		jit_asm_mov_int_to_irbp(&bytes, 12, -10);
		jit_asm_mov_irbp_to_eax(&bytes, 12);
		jit_asm_sum_sp(bytes, 12);
		jit_asm_pop_bp(bytes);
		jit_asm_ret(bytes);
		assert(-10 == ((asmFunc ) address)());
		jit_function_free(address, 1024);
	}
	{
		unsigned char* address = (unsigned char *) jit_function_new(1024);
		unsigned char* bytes = address;
		jit_asm_push_bp(bytes);
		jit_asm_mov_sp_to_bp(bytes);
		jit_asm_sub_sp(bytes, 16);
		jit_asm_mov_int_to_irbp(&bytes, 12, 10);
		jit_asm_mov_int_to_irbp(&bytes, 16, 20);
		jit_asm_mov_irbp_to_eax(&bytes, 12);
		jit_asm_mov_irbp_to_ecx(&bytes, 16);
		jit_asm_int_sum_eax_ecx(bytes);
		jit_asm_sum_sp(bytes, 16);
		jit_asm_pop_bp(bytes);
		jit_asm_ret(bytes);
		assert(30 == ((asmFunc ) address)());
		jit_function_free(address, 1024);
	}
	{
		unsigned char* address = (unsigned char *) jit_function_new(1024);
		unsigned char* bytes = address;
		jit_asm_push_bp(bytes);
		jit_asm_mov_sp_to_bp(bytes);
		jit_asm_sub_sp(bytes, 16);
		jit_asm_int_mov_eax(bytes, 10);
		jit_asm_cmp_int_with_eax(&bytes, 15);
		jit_asm_jmp_neq(&bytes, jit_asm_int_mov_eax_size);
		jit_asm_int_mov_eax(bytes, 15);
		jit_asm_sum_sp(bytes, 16);
		jit_asm_pop_bp(bytes);
		jit_asm_ret(bytes);
		assert(10 == ((asmFunc ) address)());
		jit_function_free(address, 1024);
	}
	{
		unsigned char* address = (unsigned char *) jit_function_new(1024);
		unsigned char* bytes = address;
		jit_asm_push_bp(bytes);
		jit_asm_mov_sp_to_bp(bytes);
		jit_asm_sub_sp(bytes, 16);
		jit_asm_int_mov_eax(bytes, 10);
		jit_asm_cmp_int_with_eax(&bytes, 10);
		jit_asm_jmp_eq(&bytes, jit_asm_int_mov_eax_size);
		jit_asm_int_mov_eax(bytes, 15);
		jit_asm_sum_sp(bytes, 16);
		jit_asm_pop_bp(bytes);
		jit_asm_ret(bytes);
		assert(10 == ((asmFunc ) address)());
		jit_function_free(address, 1024);
	}
	{
		unsigned char* address = (unsigned char *) jit_function_new(1024);
		unsigned char* bytes = address;
		jit_asm_push_bp(bytes);
		jit_asm_mov_sp_to_bp(bytes);
		jit_asm_sub_sp(bytes, 16);
		jit_asm_int_mov_eax(bytes, 10);
		jit_asm_cmp_int_with_eax(&bytes, 10);
		jit_asm_jmp_eq(&bytes, 256);
		int n;
		for (n = 0; n < 256; n++) {
			jit_asm_ret(bytes);
		}
		jit_asm_int_mov_eax(bytes, 15);
		jit_asm_sum_sp(bytes, 16);
		jit_asm_pop_bp(bytes);
		jit_asm_ret(bytes);
		assert(15 == ((asmFunc ) address)());
		jit_function_free(address, 1024);
	}
	{
		unsigned char* address = (unsigned char *) jit_function_new(1024);
		unsigned char* bytes = address;
		jit_asm_push_bp(bytes);
		jit_asm_mov_sp_to_bp(bytes);
		jit_asm_sub_sp(bytes, 16);
		jit_asm_int_mov_eax(bytes, 10);
		jit_asm_cmp_int_with_eax(&bytes, 10);
		jit_asm_jmp_eq(&bytes, 32);
		int n;
		for (n = 0; n < 32; n++) {
			jit_asm_ret(bytes);
		}
		jit_asm_int_mov_eax(bytes, 15);
		jit_asm_sum_sp(bytes, 16);
		jit_asm_pop_bp(bytes);
		jit_asm_ret(bytes);
		assert(15 == ((asmFunc ) address)());
		jit_function_free(address, 1024);
	}
	{
		unsigned char* address = (unsigned char *) jit_function_new(1024);
		unsigned char* bytes = address;
		jit_asm_push_bp(bytes);
		jit_asm_mov_sp_to_bp(bytes);
		jit_asm_sub_sp(bytes, 16);
		jit_asm_int_mov_eax(bytes, 10);
		jit_asm_cmp_int_with_eax(&bytes, 10);
		jit_asm_jmp_eq(&bytes, 127);
		int n;
		for (n = 0; n < 127; n++) {
			jit_asm_ret(bytes);
		}
		jit_asm_int_mov_eax(bytes, 15);
		jit_asm_sum_sp(bytes, 16);
		jit_asm_pop_bp(bytes);
		jit_asm_ret(bytes);
		assert(15 == ((asmFunc ) address)());
		jit_function_free(address, 1024);
	}
	{
		unsigned char* address = (unsigned char *) jit_function_new(1024);
		unsigned char* bytes = address;
		jit_asm_push_bp(bytes);
		jit_asm_mov_sp_to_bp(bytes);
		jit_asm_sub_sp(bytes, 16);
		jit_asm_int_mov_eax(bytes, 10);
		jit_asm_cmp_int_with_eax(&bytes, 10);
		jit_asm_jmp_eq(&bytes, 128);
		int n;
		for (n = 0; n < 128; n++) {
			jit_asm_ret(bytes);
		}
		jit_asm_int_mov_eax(bytes, 15);
		jit_asm_sum_sp(bytes, 16);
		jit_asm_pop_bp(bytes);
		jit_asm_ret(bytes);
		assert(15 == ((asmFunc ) address)());
		jit_function_free(address, 1024);
	}
	{
		unsigned char* address = (unsigned char *) jit_function_new(1024);
		unsigned char* bytes = address;
		jit_asm_push_bp(bytes);
		jit_asm_mov_sp_to_bp(bytes);
		jit_asm_int_mov_eax(bytes, 15);
		jit_asm_pop_bp(bytes);
		jit_asm_ret(bytes);
		unsigned char* func_main = bytes;
		jit_asm_push_bp(bytes);
		jit_asm_mov_sp_to_bp(bytes);
		jit_asm_call_int(&bytes, address - bytes);
		jit_asm_pop_bp(bytes);
		jit_asm_ret(bytes);
		assert(15 == ((asmFunc ) func_main)());
		jit_function_free(address, 1024);
	}
}

void jit_vm_test() {
	{
		unsigned char bytecodes[1024];
		unsigned char* b = bytecodes;
		jit_vm_opcode_stack_prolog(&b);
		jit_vm_opcode_integer_load_a(&b, 10);
		jit_vm_opcode_integer_load_c(&b, 5);
		jit_vm_opcode_integer_sum_ac(&b);
		jit_vm_opcode_stack_leave(&b);
		jit_vm_opcode_stack_return(&b);
		jit_vm_opcode_system_half(&b);
		asmFunc func = (asmFunc) jit_vm_bytecode(bytecodes);
		assert(15 == func());
		jit_function_free(func, 1024);
	}
	{
		unsigned char bytecodes[1024];
		unsigned char* b = bytecodes;
		jit_vm_opcode_stack_prolog(&b);
		jit_vm_opcode_integer_load_a(&b, 10);
		jit_vm_opcode_integer_load_c(&b, 5);
		jit_vm_opcode_integer_sub_ac(&b);
		jit_vm_opcode_stack_leave(&b);
		jit_vm_opcode_stack_return(&b);
		jit_vm_opcode_system_half(&b);
		asmFunc func = (asmFunc) jit_vm_bytecode(bytecodes);
		assert(5 == func());
		jit_function_free(func, 1024);
	}
	{
		unsigned char bytecodes[1024];
		unsigned char* b = bytecodes;
		jit_vm_opcode_stack_prolog(&b);
		jit_vm_opcode_integer_load_a(&b, 10);
		jit_vm_opcode_integer_load_c(&b, 5);
		jit_vm_opcode_integer_mul_ac(&b);
		jit_vm_opcode_stack_leave(&b);
		jit_vm_opcode_stack_return(&b);
		jit_vm_opcode_system_half(&b);
		asmFunc func = (asmFunc) jit_vm_bytecode(bytecodes);
		assert(50 == func());
		jit_function_free(func, 1024);
	}
	{
		unsigned char bytecodes[1024];
		unsigned char* b = bytecodes;
		jit_vm_opcode_stack_prolog(&b);
		jit_vm_opcode_integer_load_a(&b, 10);
		jit_vm_opcode_integer_load_c(&b, 5);
		jit_vm_opcode_integer_div_ac(&b);
		jit_vm_opcode_stack_leave(&b);
		jit_vm_opcode_stack_return(&b);
		jit_vm_opcode_system_half(&b);
		asmFunc func = (asmFunc) jit_vm_bytecode(bytecodes);
		assert(2 == func());
		jit_function_free(func, 1024);
	}
	{
		unsigned char bytecodes[1024];
		unsigned char* b = bytecodes;
		jit_vm_opcode_stack_prolog(&b);
		jit_vm_opcode_integer_load_a(&b, 10);
		jit_vm_opcode_integer_load_c(&b, 5);
		jit_vm_opcode_integer_sum_ac(&b);
		jit_vm_opcode_integer_load_c(&b, 3);
		jit_vm_opcode_integer_sub_ac(&b);
		jit_vm_opcode_integer_load_c(&b, 3);
		jit_vm_opcode_integer_mul_ac(&b);
		jit_vm_opcode_integer_load_c(&b, 4);
		jit_vm_opcode_integer_div_ac(&b);
		jit_vm_opcode_stack_leave(&b);
		jit_vm_opcode_stack_return(&b);
		jit_vm_opcode_system_half(&b);
		asmFunc func = (asmFunc) jit_vm_bytecode(bytecodes);
		assert(9 == func());
		jit_function_free(func, 1024);
	}
}

void jit_asm_test() {
	{
		struct asm_t* code = asm_new();
		asm_mov_int_to_ax(code, 10);
		asm_ret(code);
		asmFunc func = (asmFunc) asm_build(code);
		assert(10 == func());
		asm_free(code);
	}
	{
		struct asm_t* code = asm_new();
		asm_mov_int_to_ax(code, 10);
		asm_mov_int_to_cx(code, 20);
		asm_sum_ax_to_cx(code);
		asm_ret(code);
		asmFunc func = (asmFunc) asm_build(code);
		assert(30 == func());
		asm_free(code);
	}
	{
		struct asm_t* code = asm_new();
		asm_mov_int_to_ax(code, 10);
		asm_mov_int_to_cx(code, 20);
		asm_sub_ax_to_cx(code);
		asm_ret(code);
		asmFunc func = (asmFunc) asm_build(code);
		assert(-10 == func());
		asm_free(code);
	}
	{
		struct asm_t* code = asm_new();
		asm_mov_int_to_ax(code, 10);
		asm_mov_int_to_cx(code, 20);
		asm_mul_ax_to_cx(code);
		asm_ret(code);
		asmFunc func = (asmFunc) asm_build(code);
		assert(200 == func());
		asm_free(code);
	}
	{
		struct asm_t* code = asm_new();
		asm_mov_int_to_ax(code, 20);
		asm_mov_int_to_cx(code, 10);
		asm_div_ax_to_cx(code);
		asm_ret(code);
		asmFunc func = (asmFunc) asm_build(code);
		assert(2 == func());
		asm_free(code);
	}
	{
		struct asm_t* code = asm_new();
		asm_prolog(code);
		asm_mov_int_to_ax(code, 10);
		asm_mov_int_to_cx(code, 20);
		asm_sum_ax_to_cx(code);
		asm_mov_int_to_cx(code, 25);
		asm_sub_ax_to_cx(code);
		asm_leave(code);
		asm_ret(code);
		asmFunc func = (asmFunc) asm_build(code);
		assert(5 == func());
		asm_free(code);
	}
	{
		struct asm_t* code = asm_new();
		asm_prolog(code);
		asm_sub_sp(code, 12);
		asm_mov_int_to_ax(code, 10);
		asm_mov_ax_to_bp(code, -12);
		asm_mov_int_to_ax(code, 15);
		asm_mov_bp_to_ax(code, -12);
		asm_sum_sp(code, 12);
		asm_leave(code);
		asm_ret(code);
		asmFunc func = (asmFunc) asm_build(code);
		assert(10 == func());
		asm_free(code);
	}
}

int main(int argc, char *argv[]) {
	jit_jit_asm_test();
	jit_vm_test();
	jit_asm_test();
	printf("Finished\n");
	return 0;
}
