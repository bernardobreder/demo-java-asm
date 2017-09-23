package asmj;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class Main {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(MetalLookAndFeel.class.getName());
				} catch (Exception e) {
				}
				new MainFrame().setVisible(true);
			}
		});
	}
}
