package asmj;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

public class MainFrame extends JFrame {

	private JTextArea text;

	public MainFrame() {
		this.setTitle("Assemble");
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.add(this.buildToolbar(), BorderLayout.NORTH);
		this.add(this.buildCenter(), BorderLayout.CENTER);
		this.setSize(800, 600);
		this.setLocationRelativeTo(null);
	}

	private Component buildToolbar() {
		JToolBar bar = new JToolBar();
		bar.setFloatable(false);
		bar.add(buildCompileButton());
		return bar;
	}

	private JButton buildCompileButton() {
		JButton c = new JButton("Compile");
		c.setFocusable(false);
		c.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onCompileAction();
			}
		});
		return c;
	}

	protected void onCompileAction() {
		new CompileTask(this, text.getText().trim()).start();
	}

	private Component buildCenter() {
		text = new EditorText();
		JScrollPane scroll = new JScrollPane(text);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		return scroll;
	}

}
