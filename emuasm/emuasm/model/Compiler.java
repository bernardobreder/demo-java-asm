package emuasm.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.LinkedList;
import java.util.List;

public class Compiler {

  private final String code;
  private StreamThread err;
  private StreamThread in;
  private List<StreamListener> inList;
  private List<StreamListener> errList;
  private File binaryResult;

  public Compiler(String code) {
    this.code = code;
    this.inList = new LinkedList<Compiler.StreamListener>();
    this.errList = new LinkedList<Compiler.StreamListener>();
  }

  public void clean() {
    String ext = getExtensionBinaryFile();
    new File("main" + ext).delete();
  }

  private String getExtensionBinaryFile() {
    return System.getProperty("os.name").toLowerCase().contains("win") ? ".exe"
      : "";
  }

  public void addInputListener(StreamListener listener) {
    inList.add(listener);
  }

  public void addErrorListener(StreamListener listener) {
    errList.add(listener);
  }

  public void execute() throws IOException {
    File mainCSource = new File("main.c");
    String ext = getExtensionBinaryFile();
    try {
      FileOutputStream out = new FileOutputStream(mainCSource);
      out.write(code.getBytes("utf-8"));
      out.close();
      executeCommand(new String[] { "gcc", "-g", "main.c", "-o", "main" + ext });
      binaryResult = new File("main" + ext);
    }
    finally {
      mainCSource.delete();
    }
  }

  private void executeCommand(String[] cmds) throws IOException {
    Process process = new ProcessBuilder(cmds).start();
    try {
      in = new StreamThread(process.getInputStream());
      err = new StreamThread(process.getErrorStream());
      for (StreamListener listener : inList) {
        in.list.add(listener);
      }
      for (StreamListener listener : errList) {
        err.list.add(listener);
      }
      in.start();
      err.start();
    }
    finally {
      if (process != null) {
        try {
          int status = process.waitFor();
          if (status != 0) {
            throw new IOException("exit with code: " + status);
          }
        }
        catch (InterruptedException e) {
        }
      }
      if (in != null) {
        try {
          in.join();
        }
        catch (InterruptedException e) {
        }
      }
      if (err != null) {
        try {
          err.join();
        }
        catch (InterruptedException e) {
        }
      }
    }
  }

  public File getBinaryFile() {
    return binaryResult;
  }

  public static class StreamThread extends Thread {

    private final InputStream in;

    private List<StreamListener> list;

    public StreamThread(InputStream in) {
      this.in = in;
      list = new LinkedList<StreamListener>();
    }

    @Override
    public void run() {
      try {
        LineNumberReader reader =
          new LineNumberReader(new InputStreamReader(in, "utf-8"));
        try {
          String line = reader.readLine();
          while (line != null) {
            fireLineListener(line);
            line = reader.readLine();
          }
        }
        finally {
          reader.close();
        }
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }

    public void fireLineListener(String line) {
      for (StreamListener listener : list) {
        listener.lineReaded(line);
      }
    }

  }

  public static interface StreamListener {

    public void lineReaded(String line);

  }

  public static void main(String[] args) throws IOException {
    Compiler compiler =
      new Compiler(
        "int main() {\nint a = 5;\nint b= 3;\nint c = a + b;\nreturn c;\n}");
    compiler.addInputListener(new StreamListener() {
      @Override
      public void lineReaded(String line) {
        System.out.println(line);
      }
    });
    compiler.addErrorListener(new StreamListener() {
      @Override
      public void lineReaded(String line) {
        System.out.println(line);
      }
    });
    compiler.execute();

  }
}
