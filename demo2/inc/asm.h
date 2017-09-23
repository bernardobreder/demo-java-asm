#ifndef ASM_H
#define ASM_H

struct asm_t {
	unsigned char* bytes;
	unsigned char* bytes_next;
	unsigned long bytes_size;
	unsigned long bytes_max;
	unsigned char* func;
	unsigned char* func_next;
	unsigned long func_size;
	unsigned long* labels;
	unsigned long* labels_next;
	unsigned long labels_size;
	unsigned long labels_max;
};

#define asm_opcode_prolog 1
#define asm_opcode_leave 2
#define asm_opcode_ret 3
#define asm_opcode_mov_int_to_ax 4
#define asm_opcode_mov_int_to_cx 5
#define asm_opcode_sum_ax_to_cx 6
#define asm_opcode_sub_ax_to_cx 7
#define asm_opcode_mul_ax_to_cx 8
#define asm_opcode_div_ax_to_cx 9
#define asm_opcode_shift_left_ax 10
#define asm_opcode_shift_right_ax 11
#define asm_opcode_sum_sp_byte 12
#define asm_opcode_sub_sp_byte 13
#define asm_opcode_sum_sp_int 14
#define asm_opcode_sub_sp_int 15
#define asm_opcode_push_bp 16
#define asm_opcode_pop_bp 17
#define asm_opcode_mov_sp_to_bp 18
#define asm_opcode_bp_to_ax_byte 19
#define asm_opcode_bp_to_cx_byte 20
#define asm_opcode_ax_to_bp_byte 21
#define asm_opcode_bp_to_ax_int 22
#define asm_opcode_bp_to_cx_int 23
#define asm_opcode_ax_to_bp_int 24

struct asm_t* asm_new();

void asm_free(struct asm_t* self);

void* asm_build(struct asm_t* self);

long asm_label(struct asm_t* self);

void asm_prolog(struct asm_t* self);

void asm_leave(struct asm_t* self);

void asm_ret(struct asm_t* self);

void asm_mov_ax_to_bx(struct asm_t* self);

void asm_mov_int_to_ax(struct asm_t* self, int value);

void asm_mov_int_to_cx(struct asm_t* self, int value);

void asm_push_bp(struct asm_t* self);

void asm_pop_bp(struct asm_t* self);

void asm_mov_sp_to_bp(struct asm_t* self);

void asm_sum_ax_to_cx(struct asm_t* self);

void asm_sub_ax_to_cx(struct asm_t* self);

void asm_mul_ax_to_cx(struct asm_t* self);

void asm_div_ax_to_cx(struct asm_t* self);

void asm_shift_left_to_aux(struct asm_t* self, char value);

void asm_shift_right_to_aux(struct asm_t* self, char value);

void asm_sum_sp(struct asm_t* self, int value);

void asm_sub_sp(struct asm_t* self, int value);

void asm_mov_bp_to_ax(struct asm_t* self, int index);

void asm_mov_bp_to_cx(struct asm_t* self, int index);

void asm_mov_ax_to_bp(struct asm_t* self, int index);

#endif
