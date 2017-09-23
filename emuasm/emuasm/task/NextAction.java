package emuasm.task;

import javax.swing.JOptionPane;

import emuasm.gui.DesktopFrame;
import emuasm.model.Asm;
import emuasm.task.util.FrameTask;

public class NextAction extends FrameTask {

  private final DesktopFrame frame;

  private final Asm asm;

  public NextAction(DesktopFrame frame, Asm asm) {
    this.frame = frame;
    this.asm = asm;
    this.setParentComponent(frame);
  }

  @Override
  public void perform() throws Throwable {
    this.setTitle("Nexting...");
    asm.next();
  }

  @Override
  public void updateUI() {
    frame.updateAsm(asm);
  }

  @Override
  public void handlerUI(Throwable e) {
    JOptionPane.showMessageDialog(frame, "Error the next code", "Error",
      JOptionPane.ERROR_MESSAGE);
  }

}
