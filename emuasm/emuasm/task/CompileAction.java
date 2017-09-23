package emuasm.task;

import javax.swing.JOptionPane;

import emuasm.gui.DesktopFrame;
import emuasm.model.Asm;
import emuasm.model.Compiler;
import emuasm.task.util.FrameTask;

public class CompileAction extends FrameTask {

  private final DesktopFrame frame;

  private final String code;

  private Compiler compiler;

  private Asm asm;

  public CompileAction(DesktopFrame frame, String code) {
    this.frame = frame;
    this.code = code;
    this.setParentComponent(frame);
  }

  @Override
  public void perform() throws Throwable {
    this.setTitle("Compiling...");
    compiler = new Compiler(code);
    compiler.clean();
    compiler.execute();
    this.setTitle("Disassembling...");
    asm = new Asm(compiler.getBinaryFile());
    asm.start();
  }

  @Override
  public void updateUI() {
    frame.updateAsm(asm);
  }

  @Override
  public void handlerUI(Throwable e) {
    JOptionPane.showMessageDialog(frame, "Error in the code", "Error",
      JOptionPane.ERROR_MESSAGE);
  }

  @Override
  public void doneUI() {
  }

}
