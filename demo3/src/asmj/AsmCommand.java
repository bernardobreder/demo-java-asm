package asmj;

import java.util.Arrays;

public class AsmCommand {

	private long position;

	private String opcode;

	private String[] arguments;

	private String[] bytes;

	public AsmCommand(long position, String opcode, String[] arguments,
			String[] bytes) {
		super();
		this.position = position;
		this.opcode = opcode;
		this.arguments = arguments;
		this.bytes = bytes;
	}

	public String[] getBytes() {
		return bytes;
	}

	public void setBytes(String[] bytes) {
		this.bytes = bytes;
	}

	public long getPosition() {
		return position;
	}

	public void setPosition(long position) {
		this.position = position;
	}

	public String getOpcode() {
		return opcode;
	}

	public void setOpcode(String opcode) {
		this.opcode = opcode;
	}

	public String[] getArguments() {
		return arguments;
	}

	public void setArguments(String[] arguments) {
		this.arguments = arguments;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(arguments);
		result = prime * result + ((opcode == null) ? 0 : opcode.hashCode());
		result = prime * result + (int) (position ^ (position >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AsmCommand other = (AsmCommand) obj;
		if (!Arrays.equals(arguments, other.arguments))
			return false;
		if (opcode == null) {
			if (other.opcode != null)
				return false;
		} else if (!opcode.equals(other.opcode))
			return false;
		if (position != other.position)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return position + " " + opcode
				+ (arguments == null ? "" : " " + Arrays.toString(arguments))
				+ (bytes == null ? "" : " " + Arrays.toString(bytes));
	}

}
