package net.mikaboshi.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import sun.swing.table.DefaultTableCellHeaderRenderer;

/**
 * ヘッダが縦のテーブル。
 * 
 * 参考：　http://dev.ariel-networks.com/Members/nagai/swing30c630fc30eb306e30d830c330927e2665b95411-884c30d830c3-3078590966f43059308b
 * 
 * @since 1.1.5
 * @author Takuma Umezawa
 *
 */
public class VerticalHeaderTable extends JTable {

	private static final long serialVersionUID = 1L;

	public VerticalHeaderTable(Object[] rowheaderNames, Integer columnCount) {
		super(
			new DefaultTableModel(
					rowheaderNamesToData(rowheaderNames),
					columnCountToColumnNames(columnCount))
		);
	}

	@Override
	protected void initializeLocalVars() {
		super.initializeLocalVars();
		TableColumn tableColumn = super.getColumnModel().getColumn(0);
		tableColumn.setCellRenderer(new DefaultTableCellHeaderRenderer());
	}

	private static Object[][] rowheaderNamesToData(Object[] rowheaderNames) {
		List<Object[]> data = new ArrayList<Object[]>();
		for (Object rowheaderName : rowheaderNames) {
			data.add(new Object[] { rowheaderName });
		}
		return data.toArray(new Object[data.size()][]);
	}

	private static Object[] columnCountToColumnNames(Integer dataLength) {
		List<Object> columnNames = new ArrayList<Object>();
		for (Integer i = 0; i < dataLength; i++) {
			columnNames.add("");
		}
		return columnNames.toArray();
	}
	
	@Override
	public boolean isCellEditable(int row, int column) {
		
		// 1列目は編集不可とする
		if (column == 0) {
			return false;
		}
		
		return super.isCellEditable(row, column);
	}
	
	
	/**
	 * ヘッダ列のセルの幅を調整する。
	 * @param table
	 */
	public void adjustHeaderWidth() {
		
		int maxWidth = 0;
		
		for (int row = 0; row < getRowCount(); row++) {
			
			int width = getCellRenderer(row, 0).
				getTableCellRendererComponent(
					this,
					getValueAt(row, 0),
					false,
					false,
					row,
					0).getPreferredSize().width;
			
			maxWidth = Math.max(maxWidth, width);
		}
		
		getColumnModel().getColumn(0).setPreferredWidth(maxWidth + 20);
	}
}
