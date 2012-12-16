package net.mikaboshi.gui;

import javax.swing.table.DefaultTableModel;

/**
 * 
 * @author Takuma Umezawa
 * @since 1.1.5
 */
public class CheckBoxTableModel extends DefaultTableModel {

	private static final long serialVersionUID = 1L;
	
	public CheckBoxTableModel(Object[][] data, Object[] columnNames) {
		super(data, columnNames);
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		Object value = getValueAt(0, columnIndex);
		return value != null ? value.getClass() : Object.class;
	}

}
