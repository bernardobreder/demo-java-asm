import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Asm {

	private List<Integer> bytes;

	public Asm() {
		this.bytes = new ArrayList<Integer>();
	}

	public Asm mov_i32_a64(int value) {
		add(get("movq $" + value + ", %rax"));
		return this;
	}

	public Asm mov_i32_b64(int value) {
		add(get("movq $" + value + ", %rbx"));
		return this;
	}

	public Asm mov_i32_c64(int value) {
		add(get("movq $" + value + ", %rcx"));
		return this;
	}

	public Asm mov_a64_b64() {
		add(get("movq %rax, %rbx"));
		return this;
	}

	public Asm mov_a32_b32() {
		add(get("movl %eax, %ebx"));
		return this;
	}

	public Asm mov_a8_b8() {
		add(get("mov %al, %bl"));
		return this;
	}

	public Asm mov_a64_c64() {
		add(get("movq %rax, %rcx"));
		return this;
	}

	public Asm mov_a32_c32() {
		add(get("movl %eax, %ecx"));
		return this;
	}

	public Asm mov_a8_c8() {
		add(get("mov %al, %cl"));
		return this;
	}

	public Asm mov_b64_c64() {
		add(get("movq %rbx, %rcx"));
		return this;
	}

	public Asm mov_b32_c32() {
		add(get("movl %ebx, %ecx"));
		return this;
	}

	public Asm mov_b8_c8() {
		add(get("mov %bl, %cl"));
		return this;
	}

	public Asm mov_b64_a64() {
		add(get("movq %rbx, %rax"));
		return this;
	}

	public Asm mov_b32_a32() {
		add(get("movl %ebx, %eax"));
		return this;
	}

	public Asm mov_b8_a8() {
		add(get("mov %bl, %al"));
		return this;
	}
	

	public Asm mov_c64_a64() {
		add(get("movq %rcx, %rax"));
		return this;
	}

	public Asm mov_c32_a32() {
		add(get("movl %ecx, %eax"));
		return this;
	}

	public Asm mov_c8_a8() {
		add(get("mov %cl, %cl"));
		return this;
	}

	private void add(List<Integer> list) {
		this.bytes.addAll(list);
	}

	protected List<Integer> get(String code) {
		try {
			return Main.getAsmCode(code);
		} catch (Exception e) {
			throw new Error(e);
		}
	}

	public int execute() throws IOException, InterruptedException {
		return Main.executeBytes(bytes);
	}

}
