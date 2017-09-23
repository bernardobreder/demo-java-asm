package emuasm.task.util;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public abstract class FrameTask extends Task {

  private JFrame frame;

  private JLabel title;

  private JProgressBar progress;

  public void setTitle(String title) {
    this.title.setText(title);
  }

  @Override
  protected void createProgressFrame() {
    frame = new JFrame("...");
    JPanel panel = new JPanel(new BorderLayout(5, 5));
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    panel.add(title = new JLabel("..."), BorderLayout.CENTER);
    panel.add(progress = new JProgressBar(), BorderLayout.SOUTH);
    frame.setResizable(false);
    frame.setContentPane(panel);
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frame.setSize(200, 100);
    frame.setLocationRelativeTo(parentComponent);
  }

  @Override
  protected void showProgressFrame() {
    frame.setVisible(true);
  }

  @Override
  protected void closeProgressFrame() {
    frame.dispose();
  }

}
