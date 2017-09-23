package emuasm.gui;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class CEditor extends JScrollPane {

  private TextEditor editor;

  public CEditor() {
    editor = new TextEditor();
    this.getViewport().setView(editor);
    this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    this.setBackground(Color.WHITE);
  }

  public String getText() {
    return editor.getText();
  }

  public class TextEditor extends JTextArea {

    public TextEditor() {
      this.setTabSize(4);
      this.setText("int main(int argc, char** argv) {\n\t\n}");
      this.setSelectionStart(35);
      this.setSelectionEnd(35);
    }

  }

}
