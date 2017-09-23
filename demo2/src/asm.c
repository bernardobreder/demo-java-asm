#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <sys/mman.h>
#include "asm.h"

#ifdef __x86_64__
#define x86_64
#else
#define x86_32
#endif

struct asm_t* asm_new() {
	struct asm_t* self = (struct asm_t*) calloc(1, sizeof(struct asm_t));
	if (!self) {
		return 0;
	}
	self->bytes_max = 1024;
	self->bytes = self->bytes_next = (unsigned char*) calloc(self->bytes_max, 1);
	if (!self->bytes) {
		free(self);
		return 0;
	}
	self->labels_max = 1024;
	self->labels = self->labels_next = (unsigned long*) calloc(self->labels_max, sizeof(unsigned long));
	if (!self->bytes) {
		free(self->bytes);
		free(self);
		return 0;
	}
	return self;
}

void asm_free(struct asm_t* self) {
	if (self->func) {
		munmap(self->func, self->func_size);
	}
	free(self->bytes);
	free(self->labels);
	free(self);
}

void* asm_build(struct asm_t* self) {
	unsigned char* bytes = self->bytes;
	unsigned char* bytes_limit = self->bytes_next;
	while (bytes < bytes_limit) {
		switch (*bytes++) {
		case asm_opcode_prolog :
#ifdef x86_64
			self->func_size += 4;
#else
			self->func_size += 3;
#endif
			break;
		case asm_opcode_leave :
			self->func_size += 1;
			break;
		case asm_opcode_ret :
			self->func_size += 1;
			break;
		case asm_opcode_sum_ax_to_cx :
		case asm_opcode_sub_ax_to_cx :
			self->func_size += 2;
			break;
		case asm_opcode_mul_ax_to_cx :
		case asm_opcode_div_ax_to_cx :
			self->func_size += 3;
			break;
		case asm_opcode_mov_int_to_ax :
		case asm_opcode_mov_int_to_cx :
			self->func_size += 5;
			bytes += 4;
			break;
		case asm_opcode_bp_to_ax_byte :
		case asm_opcode_bp_to_cx_byte :
			self->func_size += 3;
			bytes++;
			break;
		case asm_opcode_ax_to_bp_byte :
			self->func_size += 3;
			bytes++;
			break;
		case asm_opcode_sum_sp_byte :
		case asm_opcode_sub_sp_byte :
			self->func_size += 4;
			bytes++;
			break;
		case asm_opcode_sum_sp_int :
		case asm_opcode_sub_sp_int :
#ifdef x86_64
			self->func_size += 7;
#else
			self->func_size += 6;
#endif
			bytes += 4;
			break;
		case asm_opcode_mov_sp_to_bp :
			self->func_size += 3;
			break;
		case asm_opcode_push_bp :
		case asm_opcode_pop_bp :
			self->func_size += 3;
			bytes++;
			break;
		case asm_opcode_shift_left_ax :
		case asm_opcode_shift_right_ax :
			self->func_size += 3;
			bytes++;
			break;
		case asm_opcode_bp_to_ax_int :
		case asm_opcode_bp_to_cx_int :
			self->func_size += 6;
			bytes += 4;
			break;
		case asm_opcode_ax_to_bp_int :
			break;
		default :
			assert(0);
		}
	}
	int size = self->func_size;
	void* func = mmap(0, size, PROT_READ | PROT_WRITE | PROT_EXEC, MAP_ANON | MAP_PRIVATE, 0, 0);
	if (!func) {
		return 0;
	}
	self->func = self->func_next = func;
	bytes = self->bytes;
	bytes_limit = self->bytes_next;
	int intValue;
	while (bytes < bytes_limit) {
		switch (*bytes++) {
		case asm_opcode_prolog :
#ifdef x86_64
			*self->func_next++ = 0x55;
			*self->func_next++ = 0x48;
			*self->func_next++ = 0x89;
			*self->func_next++ = 0xE5;
#else
			*self->func_next++ = 0x55;
			*self->func_next++ = 0x89;
			*self->func_next++ = 0xE5;
#endif
			break;
		case asm_opcode_leave :
			*self->func_next++ = 0x5D;
			break;
		case asm_opcode_ret :
			*self->func_next++ = 0xC3;
			break;
		case asm_opcode_mov_int_to_ax :
			intValue = (*bytes++) << 24;
			intValue += (*bytes++) << 16;
			intValue += (*bytes++) << 8;
			intValue += (*bytes++);
			*self->func_next++ = 0xB8;
			*self->func_next++ = (intValue) & 0xFF;
			*self->func_next++ = (intValue >> 8) & 0xFF;
			*self->func_next++ = (intValue >> 16) & 0xFF;
			*self->func_next++ = (intValue >> 24) & 0xFF;
			break;
		case asm_opcode_mov_int_to_cx :
			intValue = (*bytes++) << 24;
			intValue += (*bytes++) << 16;
			intValue += (*bytes++) << 8;
			intValue += (*bytes++);
			*self->func_next++ = 0xB9;
			*self->func_next++ = (intValue) & 0xFF;
			*self->func_next++ = (intValue >> 8) & 0xFF;
			*self->func_next++ = (intValue >> 16) & 0xFF;
			*self->func_next++ = (intValue >> 24) & 0xFF;
			break;
		case asm_opcode_sum_ax_to_cx :
			*self->func_next++ = 0x01;
			*self->func_next++ = 0xC8;
			break;
		case asm_opcode_push_bp :
			*self->func_next++ = 0x55;
			break;
		case asm_opcode_pop_bp :
			*self->func_next++ = 0x5D;
			break;
		case asm_opcode_mov_sp_to_bp :
			*self->func_next++ = 0x48;
			*self->func_next++ = 0x89;
			*self->func_next++ = 0xE5;
			break;
		case asm_opcode_shift_left_ax :
			*self->func_next++ = 0xC1;
			*self->func_next++ = 0xF8;
			*self->func_next++ = *bytes++;
			break;
		case asm_opcode_shift_right_ax :
			*self->func_next++ = 0xC1;
			*self->func_next++ = 0xE0;
			*self->func_next++ = *bytes++;
			break;
		case asm_opcode_sub_ax_to_cx :
			*self->func_next++ = 0x29;
			*self->func_next++ = 0xC8;
			break;
		case asm_opcode_mul_ax_to_cx :
			*self->func_next++ = 0x0F;
			*self->func_next++ = 0xAF;
			*self->func_next++ = 0xC1;
			break;
		case asm_opcode_div_ax_to_cx :
			*self->func_next++ = 0x99;
			*self->func_next++ = 0xF7;
			*self->func_next++ = 0xF9;
			break;
		case asm_opcode_sum_sp_byte :
			*self->func_next++ = 0x48;
			*self->func_next++ = 0x83;
			*self->func_next++ = 0xC4;
			*self->func_next++ = *bytes++;
			break;
		case asm_opcode_sub_sp_byte :
			*self->func_next++ = 0x48;
			*self->func_next++ = 0x83;
			*self->func_next++ = 0xEC;
			*self->func_next++ = *bytes++;
			break;
		case asm_opcode_sum_sp_int :
			intValue = (*bytes++) << 24;
			intValue += (*bytes++) << 16;
			intValue += (*bytes++) << 8;
			intValue += (*bytes++);
#ifdef x86_64
			*self->func_next++ = 0x48;
			*self->func_next++ = 0x81;
			*self->func_next++ = 0xC4;
#else
			*self->func_next++ = 0x81;
			*self->func_next++ = 0xC4;
#endif
			*self->func_next++ = (intValue) & 0xFF;
			*self->func_next++ = (intValue >> 8) & 0xFF;
			*self->func_next++ = (intValue >> 16) & 0xFF;
			*self->func_next++ = (intValue >> 24) & 0xFF;
			break;
		case asm_opcode_sub_sp_int :
			intValue = (*bytes++) << 24;
			intValue += (*bytes++) << 16;
			intValue += (*bytes++) << 8;
			intValue += (*bytes++);
#ifdef x86_64
			*self->func_next++ = 0x48;
			*self->func_next++ = 0x81;
			*self->func_next++ = 0xEC;
#else
			*self->func_next++ = 0x81;
			*self->func_next++ = 0xEC;
#endif
			*self->func_next++ = (intValue) & 0xFF;
			*self->func_next++ = (intValue >> 8) & 0xFF;
			*self->func_next++ = (intValue >> 16) & 0xFF;
			*self->func_next++ = (intValue >> 24) & 0xFF;
			break;
		case asm_opcode_bp_to_ax_byte :
			*self->func_next++ = 0x8B;
			*self->func_next++ = 0x45;
			*self->func_next++ = 0x100 + *bytes++;
			break;
		case asm_opcode_bp_to_cx_byte :
			*self->func_next++ = 0x8B;
			*self->func_next++ = 0x4D;
			*self->func_next++ = 0x100 + *bytes++;
			break;
		case asm_opcode_ax_to_bp_byte :
			*self->func_next++ = 0x89;
			*self->func_next++ = 0x45;
			*self->func_next++ = 0x100 + *bytes++;
			break;
		case asm_opcode_bp_to_ax_int :
			*self->func_next++ = 0x8B;
			*self->func_next++ = 0x85;
			*self->func_next++ = (intValue) & 0xFF;
			*self->func_next++ = (intValue >> 8) & 0xFF;
			*self->func_next++ = (intValue >> 16) & 0xFF;
			*self->func_next++ = (intValue >> 24) & 0xFF;
			break;
		case asm_opcode_bp_to_cx_int :
			*self->func_next++ = 0x8B;
			*self->func_next++ = 0x8D;
			*self->func_next++ = (intValue) & 0xFF;
			*self->func_next++ = (intValue >> 8) & 0xFF;
			*self->func_next++ = (intValue >> 16) & 0xFF;
			*self->func_next++ = (intValue >> 24) & 0xFF;
			break;
		case asm_opcode_ax_to_bp_int :
			*self->func_next++ = 0x89;
			*self->func_next++ = 0x85;
			*self->func_next++ = (intValue) & 0xFF;
			*self->func_next++ = (intValue >> 8) & 0xFF;
			*self->func_next++ = (intValue >> 16) & 0xFF;
			*self->func_next++ = (intValue >> 24) & 0xFF;
			break;
		default :
			assert(0);
		}
	}
	return func;
}

long asm_label(struct asm_t* self) {
	long result = self->labels_next - self->labels;
	self->labels_next++;
	*self->labels = self->func_next - self->func;
	self->labels_next++;
	return result;
}

void asm_prolog(struct asm_t* self) {
	*self->bytes_next++ = asm_opcode_prolog;
}

void asm_leave(struct asm_t* self) {
	*self->bytes_next++ = asm_opcode_leave;
}

void asm_ret(struct asm_t* self) {
	*self->bytes_next++ = asm_opcode_ret;
}

void asm_mov_int_to_ax(struct asm_t* self, int value) {
	*self->bytes_next++ = asm_opcode_mov_int_to_ax;
	*self->bytes_next++ = value >> 24;
	*self->bytes_next++ = (value >> 16) & 0xFF;
	*self->bytes_next++ = (value >> 8) & 0xFF;
	*self->bytes_next++ = value & 0xFF;
}

void asm_mov_int_to_cx(struct asm_t* self, int value) {
	*self->bytes_next++ = asm_opcode_mov_int_to_cx;
	*self->bytes_next++ = value >> 24;
	*self->bytes_next++ = (value >> 16) & 0xFF;
	*self->bytes_next++ = (value >> 8) & 0xFF;
	*self->bytes_next++ = value & 0xFF;
}

void asm_push_bp(struct asm_t* self) {
	*self->bytes_next++ = asm_opcode_sum_ax_to_cx;
}

void asm_pop_bp(struct asm_t* self) {
	*self->bytes_next++ = asm_opcode_pop_bp;
}

void asm_mov_sp_to_bp(struct asm_t* self) {
	*self->bytes_next++ = asm_opcode_mov_sp_to_bp;
}

void asm_sum_ax_to_cx(struct asm_t* self) {
	*self->bytes_next++ = asm_opcode_sum_ax_to_cx;
}

void asm_sub_ax_to_cx(struct asm_t* self) {
	*self->bytes_next++ = asm_opcode_sub_ax_to_cx;
}

void asm_mul_ax_to_cx(struct asm_t* self) {
	*self->bytes_next++ = asm_opcode_mul_ax_to_cx;
}

void asm_div_ax_to_cx(struct asm_t* self) {
	*self->bytes_next++ = asm_opcode_div_ax_to_cx;
}

void asm_shift_left_to_aux(struct asm_t* self, char value) {
	*self->bytes_next++ = asm_opcode_shift_left_ax;
	*self->bytes_next++ = value;
}

void asm_shift_right_to_aux(struct asm_t* self, char value) {
	*self->bytes_next++ = asm_opcode_shift_right_ax;
	*self->bytes_next++ = value;
}

void asm_sum_sp(struct asm_t* self, int value) {
	if (value > -0x80 && value < 0x80) {
		*self->bytes_next++ = asm_opcode_sum_sp_byte;
		*self->bytes_next++ = value;
	} else {
		*self->bytes_next++ = asm_opcode_sum_sp_int;
		*self->bytes_next++ = value >> 24;
		*self->bytes_next++ = (value >> 16) & 0xFF;
		*self->bytes_next++ = (value >> 8) & 0xFF;
		*self->bytes_next++ = value & 0xFF;
	}
}

void asm_sub_sp(struct asm_t* self, int value) {
	if (value > -0x80 && value < 0x80) {
		*self->bytes_next++ = asm_opcode_sub_sp_byte;
		*self->bytes_next++ = value;
	} else {
		*self->bytes_next++ = asm_opcode_sub_sp_int;
		*self->bytes_next++ = value >> 24;
		*self->bytes_next++ = (value >> 16) & 0xFF;
		*self->bytes_next++ = (value >> 8) & 0xFF;
		*self->bytes_next++ = value & 0xFF;
	}
}

void asm_mov_bp_to_ax(struct asm_t* self, int index) {
	if (index > -0x80 && index < 0x80) {
		*self->bytes_next++ = asm_opcode_bp_to_ax_byte;
		*self->bytes_next++ = index;
	} else {
		*self->bytes_next++ = asm_opcode_bp_to_ax_int;
		*self->bytes_next++ = index >> 24;
		*self->bytes_next++ = (index >> 16) & 0xFF;
		*self->bytes_next++ = (index >> 8) & 0xFF;
		*self->bytes_next++ = index & 0xFF;
	}
}

void asm_mov_bp_to_cx(struct asm_t* self, int index) {
	if (index > -0x80 && index < 0x80) {
		*self->bytes_next++ = asm_opcode_bp_to_cx_byte;
		*self->bytes_next++ = index;
	} else {
		*self->bytes_next++ = asm_opcode_bp_to_cx_int;
		*self->bytes_next++ = index >> 24;
		*self->bytes_next++ = (index >> 16) & 0xFF;
		*self->bytes_next++ = (index >> 8) & 0xFF;
		*self->bytes_next++ = index & 0xFF;
	}
}

void asm_mov_ax_to_bp(struct asm_t* self, int index) {
	if (index > -0x80 && index < 0x80) {
		*self->bytes_next++ = asm_opcode_ax_to_bp_byte;
		*self->bytes_next++ = index;
	} else {
		*self->bytes_next++ = asm_opcode_ax_to_bp_int;
		*self->bytes_next++ = index >> 24;
		*self->bytes_next++ = (index >> 16) & 0xFF;
		*self->bytes_next++ = (index >> 8) & 0xFF;
		*self->bytes_next++ = index & 0xFF;
	}
}
