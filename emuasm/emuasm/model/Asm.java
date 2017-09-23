package emuasm.model;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import emuasm.util.StringMaker;

public class Asm {

  private static final char[] HELP_GDB = new char[6];
  private static final String HELP_BEGIN = "(gdb) List of classes of commands:";
  private static final String HELP_END =
    "Command name abbreviations are allowed if unambiguous.";

  private final File binaryFile;
  private final List<AsmCode> asmCodes;
  private final Map<String, Long> registers;
  private Process process;
  private InputStreamReader in;
  private PrintWriter out;
  private int asmCodePc;
  private StringMaker sb = new StringMaker();

  public Asm(File binaryFile) {
    this.asmCodes = new ArrayList<AsmCode>();
    this.registers = new TreeMap<String, Long>();
    this.binaryFile = binaryFile;
  }

  public void start() throws IOException {
    process = new ProcessBuilder("gdb", "main.exe").start();
    in = new InputStreamReader(process.getInputStream(), getCharset());
    out =
      new PrintWriter(new OutputStreamWriter(process.getOutputStream(),
        getCharset()), true);
    readLines();
    out.println("break main");
    readLines();
    out.println("run");
    readLines();
    readStatus();
  }

  private String[] readLines() throws IOException {
    sb.clear();
    while (!sb.endsWith("(gdb) ")) {
      int c = in.read();
      if (c < 0) {
        throw new EOFException();
      }
      sb.append((char) c);
    }
    return sb.deleteFromEnd(6).trim().toString().split("\n");
  }

  public void next() throws IOException {
    out.println("stepi");
    readLines();
    readStatus();
  }

  protected void readStatus() throws IOException {
    readDisassemble();
    readInfoRegisters();
  }

  protected void readDisassemble() throws IOException {
    out.println("disassemble /r");
    String[] lines = readLines();
    asmCodes.clear();
    for (int n = 1; n < lines.length - 1; n++) {
      parseDisassemble(lines[n].trim());
    }
  }

  protected void parseDisassemble(String line) {
    if (line.startsWith("=>")) {
      asmCodePc = asmCodes.size();
      line = line.substring(2).trim();
    }
    String addressStr = line.substring(0, line.indexOf(':')).trim();
    addressStr = addressStr.substring(0, addressStr.indexOf(' ')).trim();
    int addressInt = Integer.parseInt(addressStr.substring(2), 16);
    String code = line.substring(line.indexOf(':') + 1).trim();
    String binaryStr = code.substring(0, code.indexOf('\t'));
    code = code.substring(binaryStr.length()).trim();
    String[] binary = binaryStr.split(" ");
    String opcode =
      code.indexOf(' ') < 0 ? code : code.substring(0, code.indexOf(' '))
        .trim();
    String[] params = null;
    if (code.indexOf(' ') >= 0) {
      String param = code.substring(code.indexOf(' ')).trim();
      params = param.split(",");
      for (int n = 0; n < params.length; n++) {
        params[n] = params[n].trim();
      }
    }
    asmCodes.add(new AsmCode(addressInt, binary, code, opcode, params));
  }

  protected void readInfoRegisters() throws IOException {
    out.println("info registers");
    String[] lines = readLines();
    registers.clear();
    for (String line : lines) {
      parseInfoRegister(line.trim());
    }
  }

  protected void parseInfoRegister(String line) {
    String name = line.substring(0, line.indexOf(' ')).trim();
    line = line.substring(line.indexOf(' ')).trim();
    String valueStr = line.substring(0, line.indexOf('\t')).trim();
    Long value = Long.parseLong(valueStr.substring(2), 16);
    registers.put(name, value);
  }

  protected String getCharset() {
    return "utf-8";
  }

  public List<AsmCode> getAsmCodes() {
    return asmCodes;
  }

  public Map<String, Long> getRegisters() {
    return registers;
  }

  public int getAsmCodePc() {
    return asmCodePc;
  }

  public void close() {
    out.println("quit");
    try {
      process.waitFor();
    }
    catch (InterruptedException e) {
    }
    process = null;
    in = null;
    out = null;
    asmCodePc = -1;
    registers.clear();
    asmCodes.clear();
  }

  public static class AsmCode {

    public final int address;
    public final String[] binary;
    public final String code;
    public final String opcode;
    public final String[] params;

    public AsmCode(int address, String[] binary, String code, String opcode,
      String[] params) {
      super();
      this.address = address;
      this.binary = binary;
      this.code = code;
      this.opcode = opcode;
      this.params = params;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(address);
      sb.append(": ");
      sb.append(opcode);
      if (params != null) {
        sb.append(' ');
        for (int n = 0; n < params.length; n++) {
          sb.append(params[n]);
          if (n != params.length - 1) {
            sb.append(", ");
          }
        }
      }
      return sb.toString();
    }
  }

  public static void main(String[] args) throws IOException {
    Compiler compiler =
      new Compiler(
        "int main() {\nint a = 5;\nint b= 3;\nint c = a + b;\nreturn c;\n}");
    try {
      compiler.execute();
      Asm asm = new Asm(compiler.getBinaryFile());
      asm.start();
      asm.next();
      asm.next();
      asm.next();
      asm.next();
      asm.next();
      asm.next();
      asm.next();
      asm.close();
    }
    finally {
      compiler.clean();
    }
  }

}
