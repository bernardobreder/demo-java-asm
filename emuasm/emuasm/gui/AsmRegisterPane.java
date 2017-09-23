package emuasm.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import emuasm.model.Asm;

public class AsmRegisterPane extends JPanel {

  private Asm asm;

  public AsmRegisterPane() {
    this.setBackground(DesktopFrame.BLUE_LIGHT);
    this.setLayout(new BorderLayout());
    this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
  }

  public Asm getAsm() {
    return asm;
  }

  public void setAsm(Asm asm) {
    this.asm = asm;
  }

  public void fireAsmChanged() {
    this.removeAll();
    this.add(createContentPane(), BorderLayout.CENTER);
    this.validate();
  }

  private Component createContentPane() {
    JPanel panel = new JPanel(new BorderLayout(5, 5));
    panel.setOpaque(false);
    panel.add(createRegisterPane(), BorderLayout.NORTH);
    panel.add(createMemoryPane(), BorderLayout.CENTER);
    return panel;
  }

  private Component createRegisterPane() {
    String[] regs =
      asm.getRegisters().keySet()
      .toArray(new String[asm.getRegisters().size()]);
    Arrays.sort(regs, new RegisterComparator());
    DefaultTableModel model = new DefaultTableModel();
    model.addColumn("Register");
    model.addColumn("Value");
    model.addColumn("Hex Value");
    for (int n = 0; n < regs.length; n++) {
      String reg = regs[n];
      Long value = asm.getRegisters().get(reg);
      model.addRow(new Object[] { reg, value, "0x" + Long.toHexString(value) });
    }
    JTable table = new JTable(model);
    JScrollPane scroll = new JScrollPane(table);
    scroll.setBorder(BorderFactory.createEmptyBorder());
    table.setPreferredSize(new Dimension(300, regs.length
      * table.getRowHeight()));
    return table;
  }

  private Component createMemoryPane() {
    DefaultTableModel model = new DefaultTableModel();
    JTable table = new JTable(model);
    JScrollPane scroll = new JScrollPane(table);
    scroll.setBorder(BorderFactory.createEmptyBorder());
    scroll.getViewport().setBackground(DesktopFrame.BLUE_LIGHT);
    return scroll;
  }

}
