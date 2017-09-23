package emuasm.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import emuasm.model.Asm;
import emuasm.model.Asm.AsmCode;

public class AsmCodePane extends JPanel {

  private Asm asm;

  public AsmCodePane() {
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
    DefaultTableModel model = new DefaultTableModel();
    model.addColumn("Address");
    model.addColumn("Binary");
    model.addColumn("Opcode");
    model.addColumn("Param1");
    model.addColumn("Param2");
    AsmCode[] codes =
      asm.getAsmCodes().toArray(new Asm.AsmCode[asm.getAsmCodes().size()]);
    for (AsmCode code : codes) {
      StringBuilder binary = new StringBuilder();
      for (int n = 0; n < code.binary.length; n++) {
        binary.append(code.binary[n]);
        if (n != code.binary.length - 1) {
          binary.append(' ');
        }
      }
      String param1 =
        code.params != null && code.params.length > 0 ? code.params[0] : null;
        String param2 =
          code.params != null && code.params.length > 1 ? code.params[1] : null;
          String address = "0x" + Long.toHexString(code.address);
          model.addRow(new Object[] { address, binary.toString(), code.opcode,
            param1, param2 });
    }
    JTable table = new JTable(model);
    table.setFocusable(false);
    table.getSelectionModel().setSelectionInterval(asm.getAsmCodePc(),
      asm.getAsmCodePc());
    JScrollPane scroll = new JScrollPane(table);
    scroll.getViewport().setBackground(DesktopFrame.BLUE_LIGHT);
    scroll.setBorder(BorderFactory.createEmptyBorder());
    scroll.setFocusable(false);
    int y =
      Math.max(0, asm.getAsmCodePc() * table.getRowHeight()
        - scroll.getVisibleRect().height / 2);
    scroll.getViewport().setViewPosition(new Point(0, y));
    return scroll;
  }

}
