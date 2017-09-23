package asmj;

import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class CompileTask extends Thread {

	private static boolean is64Bits;

	private String code;

	private CompiledFrame frame;

	private Component component;

	static {
		is64Bits = false;
		try {
			Process process = new ProcessBuilder("gcc", "-version").start();
			process.waitFor();
			InputStream input = process.getErrorStream();
			StringBuilder sb = new StringBuilder();
			byte[] bytes = new byte[1024];
			for (int n; (n = input.read(bytes)) != -1;) {
				sb.append(new String(bytes, 0, n));
			}
			if (sb.toString().contains("i686")) {
				is64Bits = true;
			}
		} catch (Exception e) {
		}
	}

	public CompileTask(Component component, String code) {
		this.component = component;
		this.code = code;
		this.frame = new CompiledFrame();
		this.frame.addCode(code);
		this.frame.setVisible(true);
	}

	@Override
	public void run() {
		try {
			final File sourceFile = File.createTempFile("asmj.", ".compile.c");
			try {
				FileOutputStream foutput = new FileOutputStream(sourceFile);
				try {
					foutput.write(code.getBytes("utf-8"));
				} finally {
					foutput.close();
				}
				Thread asm32Thread = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							asm32(sourceFile);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				});
				Thread asm64Thread = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							asm64(sourceFile);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				Thread asm32OThread = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							asm32O(sourceFile);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				});
				Thread asm64OThread = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							asm64O(sourceFile);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				asm32Thread.start();
				asm32OThread.start();
				if (is64Bits) {
					asm64Thread.start();
					asm64OThread.start();
				}
				asm32Thread.join();
				asm32OThread.join();
				if (is64Bits) {
					asm64Thread.join();
					asm64OThread.join();
				}
			} finally {
				sourceFile.delete();
			}
		} catch (Exception e) {
			showError("Erro de compilação", e.getClass().getSimpleName() + ": "
					+ e.getMessage());
		}
	}

	private void asm32(File sourceFile) throws Exception {
		String mArg = "-m32";
		File asmFile = this.asm(mArg, false, sourceFile);
		try {
			File objFile = compile(mArg, asmFile);
			try {
				final List<AsmCommand> asms = otool(objFile);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						frame.setAsm32(asms);
					}
				});
			} finally {
				objFile.delete();
			}
		} finally {
			asmFile.delete();
		}
	}

	private void asm64(File sourceFile) throws Exception {
		String mArg = "-m64";
		File asmFile = this.asm(mArg, false, sourceFile);
		try {
			File objFile = compile(mArg, asmFile);
			try {
				final List<AsmCommand> asms = otool(objFile);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						frame.setAsm64(asms);
					}
				});
			} finally {
				objFile.delete();
			}
		} finally {
			asmFile.delete();
		}
	}

	private void asm32O(File sourceFile) throws Exception {
		String mArg = "-m32";
		File asmFile = this.asm(mArg, true, sourceFile);
		try {
			File objFile = compile(mArg, asmFile);
			try {
				final List<AsmCommand> asms = otool(objFile);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						frame.setAsmO32(asms);
					}
				});
			} finally {
				objFile.delete();
			}
		} finally {
			asmFile.delete();
		}
	}

	private void asm64O(File sourceFile) throws Exception {
		String mArg = "-m64";
		File asmFile = this.asm(mArg, true, sourceFile);
		try {
			File objFile = compile(mArg, asmFile);
			try {
				final List<AsmCommand> asms = otool(objFile);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						frame.setAsmO64(asms);
					}
				});
			} finally {
				objFile.delete();
			}
		} finally {
			asmFile.delete();
		}
	}

	private File asm(String mArg, boolean otimize, File sourceFile)
			throws Exception {
		File asmFile = File.createTempFile("asmj.", ".compile.s");
		List<String> list = new ArrayList<String>();
		list.add("gcc");
		list.add(mArg);
		if (otimize) {
			list.add("-Os");
			list.add("-fomit-frame-pointer");
		}
		list.add("-S");
		list.add(sourceFile.toString());
		list.add("-o");
		list.add(asmFile.toString());
		Process process = new ProcessBuilder(list.toArray(new String[0]))
				.start();
		if (process.waitFor() != 0) {
			showError("Erro na Compilação para Assemble", list.toString(),
					process);
			throw new IOException("code: " + process.exitValue());
		}
		return asmFile;
	}

	private File compile(String mArg, File asmFile) throws Exception {
		File objFile = File.createTempFile("asmj.", "compile.o");
		Process process = new ProcessBuilder("gcc", mArg, "-c",
				asmFile.toString(), "-o", objFile.toString()).start();
		if (process.waitFor() != 0) {
			showError("Erro na Compilação para Assemble",
					"gcc -c <source.s> -o <target>", process);
			throw new IOException("code: " + process.exitValue());
		}
		return objFile;
	}

	private List<AsmCommand> otool(File objFile) throws Exception {
		List<String> textLines = new ArrayList<String>();
		List<String> disassembleLines = new ArrayList<String>();
		{
			Process textProcess = new ProcessBuilder("otool", "-t",
					objFile.toString()).start();
			Process disassembleProcess = new ProcessBuilder("otool", "-tv",
					objFile.toString()).start();
//			if (textProcess.waitFor() != 0 || disassembleProcess.waitFor() != 0) {
//				showError("Erro no Dessassembler do Objeto",
//						"otool -t[v] <object>", textProcess);
//				throw new IOException("code: " + textProcess.exitValue() + " "
//						+ disassembleProcess.exitValue());
//			}
			{
				StringBuilder sb = new StringBuilder();
				InputStream input = textProcess.getInputStream();
				byte[] bytes = new byte[1024];
				for (int n; (n = input.read(bytes)) != -1;) {
					sb.append(new String(bytes, 0, n));
				}
				LineNumberReader reader = new LineNumberReader(
						new StringReader(sb.toString()));
				String line = reader.readLine();
				while (line != null && !line.startsWith("00000000")) {
					line = reader.readLine();
				}
				textLines.add(line);
				line = reader.readLine();
				while (line != null) {
					textLines.add(line);
					line = reader.readLine();
				}
			}
			{
				StringBuilder sb = new StringBuilder();
				InputStream input = disassembleProcess.getInputStream();
				byte[] bytes = new byte[1024];
				for (int n; (n = input.read(bytes)) != -1;) {
					sb.append(new String(bytes, 0, n));
				}
				LineNumberReader reader = new LineNumberReader(
						new StringReader(sb.toString()));
				String line = reader.readLine();
				while (line != null && !line.startsWith("00000000")) {
					line = reader.readLine();
				}
				disassembleLines.add(line);
				line = reader.readLine();
				while (line != null) {
					disassembleLines.add(line);
					line = reader.readLine();
				}
			}
		}
		List<AsmCommand> list = new ArrayList<AsmCommand>();
		{
			int byteSize = 0;
			String[] bytes = new String[textLines.size() * 16];
			for (int n = 0; n < textLines.size(); n++) {
				String line = textLines.get(n);
				String[] split = line.split(" ");
				for (int m = 1; m < split.length; m++) {
					bytes[byteSize++] = split[m];
				}
			}
			int size = disassembleLines.size();
			for (int n = 0; n < size; n++) {
				String line = disassembleLines.get(n);
				if (line.startsWith("0")) {
					String[] split = line.split("\t");
					String[] arguments = null;
					if (split.length > 2) {
						arguments = split[2].split(", ");
					}
					String[] asms = null;
					long begin = Long.valueOf(split[0], 16);
					for (int p = n + 1; p < size; p++) {
						String nextLine = disassembleLines.get(p);
						if (nextLine.startsWith("0")) {
							String[] nextSplit = nextLine.split("\t");
							long end = Long.valueOf(nextSplit[0], 16);
							asms = new String[(int) (end - begin)];
							for (int m = 0; m < asms.length; m++) {
								asms[m] = bytes[(int) (begin + m)];
							}
							break;
						}
					}
					if (asms == null) {
						asms = new String[(int) (byteSize - begin)];
						for (int m = 0; m < asms.length; m++) {
							asms[m] = bytes[(int) (begin + m)];
						}
					}
					list.add(new AsmCommand(begin, split[1], arguments, asms));
				}
			}
		}
		return list;
	}

	private List<String> otoolText(File objFile) throws Exception {
		Process process = new ProcessBuilder("otool", "-t", objFile.toString())
				.start();
		if (process.waitFor() != 0) {
			showError("Erro no Dessassembler do Objeto", "otool -t <object>",
					process);
			throw new IOException("code: " + process.exitValue());
		}
		StringBuilder sb = new StringBuilder();
		InputStream input = process.getInputStream();
		byte[] bytes = new byte[1024];
		for (int n; (n = input.read(bytes)) != -1;) {
			sb.append(new String(bytes, 0, n));
		}
		List<String> lines = new ArrayList<String>();
		LineNumberReader reader = new LineNumberReader(new StringReader(
				sb.toString()));
		String line = reader.readLine();
		while (!line.startsWith("00000000")) {
			line = reader.readLine();
		}
		lines.add(line);
		line = reader.readLine();
		while (line != null) {
			lines.add(line);
			line = reader.readLine();
		}
		return lines;
	}

	private List<String> otoolDisassemble(File objFile) throws Exception {
		Process process = new ProcessBuilder("otool", "-tv", objFile.toString())
				.start();
		if (process.waitFor() != 0) {
			showError("Erro no Dessassembler do Objeto", "otool -t <object>",
					process);
			throw new IOException("code: " + process.exitValue());
		}
		StringBuilder sb = new StringBuilder();
		InputStream input = process.getInputStream();
		byte[] bytes = new byte[1024];
		for (int n; (n = input.read(bytes)) != -1;) {
			sb.append(new String(bytes, 0, n));
		}
		List<String> lines = new ArrayList<String>();
		LineNumberReader reader = new LineNumberReader(new StringReader(
				sb.toString()));
		String line = reader.readLine();
		while (!line.startsWith("00000000")) {
			line = reader.readLine();
		}
		lines.add(line);
		line = reader.readLine();
		while (line != null) {
			lines.add(line);
			line = reader.readLine();
		}
		return lines;
	}

	private void showError(String title, String cmd, Process process) {
		StringBuilder sb = new StringBuilder();
		sb.append(cmd);
		sb.append("\n\n");
		InputStream input = process.getErrorStream();
		byte[] bytes = new byte[1024];
		try {
			for (int n; (n = input.read(bytes)) != -1;) {
				sb.append(new String(bytes, 0, n));
			}
		} catch (IOException e) {
		}
		showError(title, sb.toString());
	}

	private void showError(String title, String msg) {
		JOptionPane.showMessageDialog(this.component, msg, title,
				JOptionPane.ERROR_MESSAGE);
	}

}
