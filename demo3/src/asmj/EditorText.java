package asmj;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JTextArea;

public class EditorText extends JTextArea {

	public EditorText() {
		this.setTabSize(4);
		this.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				onTextReleased(e);
			}

		});
	}

	protected void onTextReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			String content = this.getText();
			int selectionStart = this.getSelectionStart();
			int begin = selectionStart - 2;
			while (begin > 0 && content.charAt(begin) != '\n') {
				begin--;
			}
			if (begin > 0) {
				begin++;
			}
			int end = begin;
			while (end < content.length() && content.charAt(end) != '\n'
					&& content.charAt(end) <= ' ') {
				end++;
			}
			String tab = content.substring(begin, end);
			this.setText(content.substring(0, selectionStart) + tab
					+ content.substring(selectionStart));
		}
	}

}
