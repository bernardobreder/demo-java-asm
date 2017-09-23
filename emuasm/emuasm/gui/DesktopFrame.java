package emuasm.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import emuasm.model.Asm;
import emuasm.task.CompileAction;
import emuasm.task.NextAction;

public class DesktopFrame extends JFrame {

  private CEditor editor;
  private AsmRegisterPane asmRegisterPane;
  private AsmRegisterPane asmVariablePane;
  private AsmCodePane asmCodePane;
  public static final Color BLUE_LIGHT = new Color(245, 245, 255);
  private Asm asm;
  private JMenuItem nextProgramMenuItem;

  public DesktopFrame() {
    this.setTitle("Desktop");
    this.setJMenuBar(createMenuBar());
    this.setContentPane(createContentPane());
    this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    this.setSize(600 + 500, 700);
    this.setLocationRelativeTo(null);
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        editor.requestFocusInWindow();
      }
    });
  }

  private JMenuBar createMenuBar() {
    JMenuBar bar = new JMenuBar();
    bar.add(createProgramMenu());
    return bar;
  }

  private JMenu createProgramMenu() {
    JMenu menu = new JMenu("Program");
    menu.add(createCompileProgramMenu());
    menu.add(createNextProgramMenu());
    return menu;
  }

  private JMenuItem createCompileProgramMenu() {
    JMenuItem c = new JMenuItem("Compile");
    c.setAccelerator(KeyStroke.getKeyStroke("F5"));
    c.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        onCompileAction();
      }
    });
    return c;
  }

  private JMenuItem createNextProgramMenu() {
    nextProgramMenuItem = new JMenuItem("Next");
    nextProgramMenuItem.setEnabled(false);
    nextProgramMenuItem.setAccelerator(KeyStroke.getKeyStroke("F6"));
    nextProgramMenuItem.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        onNextAction();
      }
    });
    return nextProgramMenuItem;
  }

  private Container createContentPane() {
    JPanel panel = new JPanel(new BorderLayout(1, 1));
    panel.setBackground(Color.BLACK);
    panel.add(buildAsmPane(), BorderLayout.WEST);
    panel.add(buildCenterPane(), BorderLayout.CENTER);
    panel.add(buildVariablePane(), BorderLayout.EAST);
    return panel;
  }

  private Component buildCenterPane() {
    JPanel panel = new JPanel(new GridLayout(2, 1, 1, 1));
    panel.setOpaque(false);
    panel.add(buildTextEditor());
    panel.add(buildAsmCodePane());
    return panel;
  }

  private Component buildTextEditor() {
    editor = new CEditor();
    return editor;
  }

  private Component buildAsmPane() {
    asmRegisterPane = new AsmRegisterPane();
    asmRegisterPane.setPreferredSize(new Dimension(300, 300));
    return asmRegisterPane;
  }

  private Component buildAsmCodePane() {
    asmCodePane = new AsmCodePane();
    return asmCodePane;
  }

  private Component buildVariablePane() {
    asmVariablePane = new AsmRegisterPane();
    asmVariablePane.setPreferredSize(new Dimension(300, 300));
    return asmVariablePane;
  }

  protected void onCompileAction() {
    new CompileAction(this, editor.getText()).start();
  }

  protected void onNextAction() {
    new NextAction(this, asm).start();
  }

  public void updateAsm(Asm asm) {
    this.asm = asm;
    asmRegisterPane.setAsm(asm);
    asmVariablePane.setAsm(asm);
    asmCodePane.setAsm(asm);
    asmRegisterPane.fireAsmChanged();
    asmVariablePane.fireAsmChanged();
    asmCodePane.fireAsmChanged();
    nextProgramMenuItem.setEnabled(true);
  }

}
