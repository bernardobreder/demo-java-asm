package asmj.swing.pane;

import java.awt.Component;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import asmj.AsmCommand;

public class AsmPane extends JTable {

	public AsmPane(List<AsmCommand> asms) {
		super(new MyTableModel(asms));
		this.setDefaultRenderer(String[].class, new MyStringArrayRenderer());
		this.setAutoCreateColumnsFromModel(true);
		int width = 75;
		for (int n : new int[] { 0, 1 }) {
			this.getColumnModel().getColumn(n).setMinWidth(width);
			this.getColumnModel().getColumn(n).setMaxWidth(width);
			this.getColumnModel().getColumn(n).setPreferredWidth(width);
			this.getColumnModel().getColumn(n).setWidth(width);
		}
	}

	private static class MyStringArrayRenderer extends DefaultTableCellRenderer {

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			StringBuilder sb = new StringBuilder();
			if (value != null) {
				String[] array = (String[]) value;
				for (int n = 0; n < array.length; n++) {
					sb.append(array[n]);
					if (n != array.length - 1) {
						sb.append(", ");
					}
				}
			}
			return super.getTableCellRendererComponent(table, sb.toString(),
					isSelected, hasFocus, row, column);
		}

	}

	private static class MyTableModel extends AbstractTableModel {

		private static final String[] COLUMNS = new String[] { "Posição",
				"Opcode", "Argumentos", "Bytes" };

		private static final Class<?>[] COLUMNS_CLASS = new Class<?>[] {
				Long.class, String.class, String[].class, String[].class };

		private List<AsmCommand> asms;

		public MyTableModel(List<AsmCommand> asms) {
			this.asms = asms;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return COLUMNS_CLASS[columnIndex];
		}

		@Override
		public String getColumnName(int column) {
			return COLUMNS[column];
		}

		@Override
		public int getColumnCount() {
			return 4;
		}

		@Override
		public int getRowCount() {
			return asms.size();
		}

		@Override
		public Object getValueAt(int row, int column) {
			AsmCommand asm = asms.get(row);
			switch (column) {
			case 0:
				return asm.getPosition()
						+ " (0x"
						+ Integer.toHexString((int) asm.getPosition())
								.toLowerCase() + ")";
			case 1:
				return asm.getOpcode();
			case 2:
				return asm.getArguments();
			case 3:
				return asm.getBytes();
			default:
				return null;
			}
		}

	}

}
