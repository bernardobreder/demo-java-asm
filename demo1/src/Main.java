import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Main {

	private static final int TIMEOUT = 2000;

	private static Map<String, List<Integer>> asmCache = new TreeMap<String, List<Integer>>();

	public static void main(String[] args) throws IOException, InterruptedException {
		executeAsmCode("cmp %rcx, %rax");
		executeAsmCode("cmp %rbx, %rax");
		executeAsmCode("cmp %rax, %rax");
		executeAsmCode("movq $1, %rax");
		executeAsmCode("movl $1, %eax");
		executeAsmCode("mov $1, %al");
		executeAsmCode("movq $1, %rbx");
		executeAsmCode("movl $1, %ebx");
		executeAsmCode("mov $1, %bl");
		executeAsmCode("movq $1, %rcx");
		executeAsmCode("movl $1, %ecx");
		executeAsmCode("mov $1, %cl");
		executeAsmCode("cmp %ecx, %eax");
		executeAsmCode("cmp %ebx, %eax");
		executeAsmCode("jne 1");
		executeAsmCode("je 1");
		executeAsmCode("sete %al");
		executeAsmCode("movzbl %al, %eax");
		executeAsmCode("movzbq %al, %rax");
		System.out.println(asmAsmCodes("movq $1, %rax"));
		System.out.println(executeAsmCodes("movq $1, %rax"));
	}

	public static int asmAsmCodes(String... asmCodes) throws IOException, InterruptedException {
		List<Integer> bytes = new ArrayList<Integer>();
		for (String asmCode : asmCodes) {
			bytes.addAll(getAsmCode(asmCode));
		}
		return executeBytes(bytes);
	}

	public static int executeBytes(List<Integer> bytes) throws IOException, InterruptedException {
		writeAsmSourceCode(bytes);
		File asmFile = compileSourceToAsm();
		File objFile = compileAsmToObj(asmFile);
		File exeFile = compileObjToExe(objFile);
		return executeExe(exeFile);
	}

	public static int executeAsmCodes(String... asmCodes) throws IOException, InterruptedException {
		File sourceFile = writeSourceCode();
		try {
			File asmFile = compileSourceToAsm();
			try {
				String asmContent = readFile(asmFile);
				asmContent = prepareAsmCode(asmContent);
				asmContent = asmContent.replace("${code}", buildAsmCodes(asmCodes));
				writeFile(asmFile, asmContent);
				File objFile = compileAsmToObj(asmFile);
				try {
//					String memory = readMemoryFromObj(objFile);
//					String asm = readAsmFromObj(objFile);
					// System.out.println(memory);
					// System.out.println(asm);
					// System.out.println(executeVm(objFile));
					File exeFile = compileObjToExe(objFile);
					try {
						return executeExe(exeFile);
					} finally {
						exeFile.delete();
					}
				} finally {
					objFile.delete();
				}
			} finally {
				asmFile.delete();
			}
		} finally {
			sourceFile.delete();
		}
	}

	public static void executeAsmCode(String asmCode) throws IOException, InterruptedException {
		List<Integer> bytes = getAsmCode(asmCode);
	}

	protected static void printAsmBytes(String asmCode, List<Integer> bytes) {
		System.out.print(asmCode + ": ");
		for (Integer asmItem : bytes) {
			System.out.print(Integer.toHexString(asmItem) + " ");
		}
		System.out.println();
	}

	public static List<Integer> getAsmCode(String asmCode) throws IOException, InterruptedException {
		List<Integer> bytes = asmCache.get(asmCode);
		if (bytes == null) {
			File sourceFile = writeSourceCode();
			try {
				File asmFile = compileSourceToAsm();
				try {
					String asmContent = readFile(asmFile);
					asmContent = prepareAsmCode(asmContent);
					asmContent = asmContent.replace("${code}", asmCode);
					writeFile(asmFile, asmContent);
					File objFile = compileAsmToObj(asmFile);
					try {
						String memory = readMemoryFromObj(objFile);
						bytes = readMemoryToData(memory);
					} finally {
						objFile.delete();
					}
				} finally {
					asmFile.delete();
				}
			} finally {
				sourceFile.delete();
			}
			printAsmBytes(asmCode, bytes);
			asmCache.put(asmCode, bytes);
		}
		return bytes;
	}

	public static String buildAsmCodes(String... asmCodes) {
		StringBuilder sb = new StringBuilder();
		for (int n = 0; n < asmCodes.length; n++) {
			sb.append(asmCodes[n]);
			if (n != asmCodes.length - 1) {
				sb.append("\n\t");
			}
		}
		String asmCode = sb.toString();
		return asmCode;
	}

	public static List<Integer> readMemoryToData(String memory) throws IOException {
		List<Integer> bytes = new ArrayList<Integer>();
		LineNumberReader reader = new LineNumberReader(new InputStreamReader(new ByteArrayInputStream(memory.getBytes())));
		try {
			reader.readLine();
			reader.readLine();
			String line = reader.readLine();
			while (line != null) {
				String[] split = line.split(" ");
				// int address = Integer.parseInt(split[0], 16);
				for (int n = 1; n < split.length; n++) {
					bytes.add(Integer.parseInt(split[n], 16));
				}
				line = reader.readLine();
			}
			bytes.remove(bytes.size() - 1);
			return bytes;
		} finally {
			reader.close();
		}
	}

	public static void writeFile(File asmFile, String asmContent) throws IOException {
		FileOutputStream fout = new FileOutputStream(asmFile);
		try {
			fout.write(asmContent.getBytes());
		} finally {
			fout.close();
		}
	}

	public static String readMemoryFromObj(File file) throws IOException, InterruptedException {
		Process process = new ProcessBuilder("otool", "-t", file.toString()).start();
		process.waitFor();
		return readStream(process.getInputStream());
	}

	public static String readAsmFromObj(File file) throws IOException, InterruptedException {
		Process process = new ProcessBuilder("otool", "-tv", file.toString()).start();
		process.waitFor();
		return readStream(process.getInputStream());
	}

	public static File compileSourceToAsm() throws InterruptedException, IOException {
		File file = new File("main.s");
		if (new ProcessBuilder("gcc", "-S", "main.c").start().waitFor() != 0) {
			file.delete();
			throw new RuntimeException("can not compile the source code to assembly code");
		}
		if (!file.exists()) {
			throw new RuntimeException("not found the assembly code file");
		}
		return file;
	}

	public static File writeSourceCode() throws IOException {
		File file = new File("main.c");
		FileOutputStream fout = new FileOutputStream(file);
		try {
			String code = "int main () { return 0; }";
			fout.write(code.getBytes());
		} catch (IOException e) {
			try {
				fout.close();
			} catch (IOException e1) {
			}
			file.delete();
			throw e;
		} finally {
			fout.close();
		}
		return file;
	}

	public static File writeAsmSourceCode(List<Integer> bytes) throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append("#ifdef __WIN32__\n");
		sb.append("#include <mman.h>\n");
		sb.append("#else\n");
		sb.append("#include <sys/mman.h>\n");
		sb.append("#endif\n");
		sb.append("typedef unsigned (*asmFunc)(void);\n");
		sb.append("int main () {\n");
		sb.append("\tunsigned char* data = mmap(0, 8*1024, PROT_READ | PROT_WRITE | PROT_EXEC, MAP_ANON | MAP_PRIVATE, 0, 0);\n");
		sb.append("\tunsigned char* bytes = data;\n");
		bytes.add(0xC3);
		for (int n = 0; n < bytes.size(); n++) {
			sb.append("\t*bytes++ = 0x" + Integer.toHexString(bytes.get(n)) + ";\n");
		}
		sb.append("\tasmFunc func = (asmFunc) data;\n");
		sb.append("\treturn func();\n");
		sb.append("}");
		File file = new File("main.c");
		FileOutputStream fout = new FileOutputStream(file);
		try {
			fout.write(sb.toString().getBytes());
		} catch (IOException e) {
			try {
				fout.close();
			} catch (IOException e1) {
			}
			file.delete();
			throw e;
		} finally {
			fout.close();
		}
		return file;
	}

	public static File compileAsmToObj(File asmFile) throws InterruptedException, IOException {
		File file = new File("main.o");
		if (new ProcessBuilder("gcc", "-c", asmFile.toString()).start().waitFor() != 0) {
			file.delete();
			throw new RuntimeException("can not compile the assembly code to object code");
		}
		if (!file.exists()) {
			throw new RuntimeException("not found the object code file");
		}
		return file;
	}

	public static File compileObjToExe(File objFile) throws InterruptedException, IOException {
		File file = new File("main");
		if (new ProcessBuilder("gcc", "-o", file.toString(), objFile.toString()).start().waitFor() != 0) {
			file.delete();
			throw new RuntimeException("can not compile the object code to executable code");
		}
		if (!file.exists()) {
			throw new RuntimeException("not found the executable code file");
		}
		new ProcessBuilder("chmod", "+x", file.toString()).start().waitFor();
		return file;
	}

	public static int executeExe(File file) throws InterruptedException, IOException {
		Process process = new ProcessBuilder(file.getAbsolutePath()).start();
		long time = System.currentTimeMillis();
		do {
			try {
				process.exitValue();
				break;
			} catch (IllegalThreadStateException e) {
				Thread.sleep(10);
			}
		} while (System.currentTimeMillis() - time < TIMEOUT);
		try {
			return process.exitValue();
		} catch (IllegalThreadStateException e) {
			process.destroy();
			throw new InterruptedException();
		}
	}

	public static String executeVm(File objFile) throws InterruptedException, IOException {
		List<Integer> bytes = readMemoryToData(readMemoryFromObj(objFile));
		bytes.add(Integer.parseInt("C3", 16));
		StringBuilder sb = new StringBuilder();
		for (int n = 0; n < bytes.size(); n++) {
			sb.append("*b++ = ");
			sb.append("0x");
			sb.append(Integer.toHexString(bytes.get(n)).toUpperCase());
			sb.append(";");
		}
		return sb.toString();
	}

	public static String prepareAsmCode(String asmContent) {
		// return asmContent.replace("movl\t$0, %eax", "${code}");
		// String beginStr = "_main:";
		// int beginIndex = asmContent.indexOf(beginStr);
		// String endStr = "ret";
		// int endIndex = asmContent.indexOf(endStr, beginIndex);
		// asmContent = asmContent.substring(0, beginIndex + beginStr.length())
		// + "\n\t${code}\n\t" + asmContent.substring(endIndex);
		// return asmContent;
		StringBuilder sb = new StringBuilder();
		sb.append("\t.globl\t_main\n");
		sb.append("_main:\n");
		sb.append("\t${code}\n");
		sb.append("\tret\n");
		return sb.toString();
	}

	public static String readFile(File file) throws IOException {
		return readStream(new FileInputStream(file));
	}

	public static String readStream(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			byte[] bytes = new byte[1024];
			for (int n = 0; (n = in.read(bytes)) != -1;) {
				out.write(bytes, 0, n);
			}
		} finally {
			in.close();
		}
		return new String(out.toByteArray());
	}
}
