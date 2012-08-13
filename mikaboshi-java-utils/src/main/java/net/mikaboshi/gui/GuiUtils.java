package net.mikaboshi.gui;

import java.awt.Component;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * ビジュアルクラスに関するユーティリティ
 * @author Takuma Umezawa
 * @since 1.1.5
 */
public final class GuiUtils {

	private GuiUtils() {}
	
	/**
	 * ESCキーでウィンドウを閉じる。
	 * @param window
	 * @param panel
	 */
	public static void closeByESC(final Window window, JPanel panel) {
		panel.getActionMap().put(
				"close", 
				new AbstractAction() {
					private static final long serialVersionUID = 1L;
					public void actionPerformed(ActionEvent e) {
						window.dispose();
					}
				}
		);
		
		panel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
			.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
		
	}
	
	/**
	 * テキストエリアの一番上にスクロールを合わせる。
	 * @param textArea
	 */
	public static void setCeil(final JTextArea textArea) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JViewport v = (JViewport) textArea.getParent();
				v.setViewPosition(new Point(0, 0));
			}
		});
	}
	
	/**
	 * テーブルの列の幅をヘッダサイズに合わせる。
	 * @param table
	 */
	public static void adjustHeader(JTable table) {
		
		TableCellRenderer headerRenderer = 
				table.getTableHeader().getDefaultRenderer();
		
		for (int i = 0; i < table.getColumnCount(); i++) {
			
			TableColumn column = table.getColumnModel().getColumn(i);
			
			Component header =
				headerRenderer.getTableCellRendererComponent(
					table, 
					column.getHeaderValue(), false, 
					false,
					0,
					i);
			
			column.setPreferredWidth(header.getPreferredSize().width + 8);
		}
	}
	
}
