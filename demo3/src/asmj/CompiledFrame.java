package asmj;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import asmj.swing.pane.AsmPane;

public class CompiledFrame extends JFrame {

	private JTabbedPane tab;

	private JPanel codePanel;

	private AsmPane asm32Panel;

	private AsmPane asmO32Panel;

	private AsmPane asm64Panel;

	private AsmPane asmO64Panel;

	public CompiledFrame() {
		this.setTitle("Compiled");
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.add(this.buildCenter(), BorderLayout.CENTER);
		this.setSize(640, 480);
		this.setLocationRelativeTo(null);
	}

	private Component buildCenter() {
		this.tab = new JTabbedPane();
		tab.setFocusable(false);
		this.codePanel = new JPanel(new VerticalLayout());
		JScrollPane scroll = new JScrollPane(codePanel);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		this.tab.addTab("Código", scroll);
		return tab;
	}

	public void addCode(String code) {
		EditorText text = new EditorText();
		text.setText(code);
		text.setEditable(false);
		this.codePanel.add(text);
	}

	public void setAsm32(List<AsmCommand> asms) {
		this.asm32Panel = new AsmPane(asms);
		JScrollPane scroll = new JScrollPane(asm32Panel);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		this.tab.addTab("32 bits", scroll);
	}

	public void setAsmO32(List<AsmCommand> asms) {
		this.asmO32Panel = new AsmPane(asms);
		JScrollPane scroll = new JScrollPane(asmO32Panel);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		this.tab.addTab("32 bits Otimized", scroll);
	}

	public void setAsm64(List<AsmCommand> asms) {
		this.asm64Panel = new AsmPane(asms);
		JScrollPane scroll = new JScrollPane(asm64Panel);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		this.tab.addTab("64 bits", scroll);
	}

	public void setAsmO64(List<AsmCommand> asms) {
		this.asmO64Panel = new AsmPane(asms);
		JScrollPane scroll = new JScrollPane(asmO64Panel);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		this.tab.addTab("64 bits Otimized", scroll);
	}

}
