package net.mikaboshi.gui;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;

/**
 * JTextField　のフォーカスが移ったときに、1以上の整数ではない場合に警告ダイアログを表示する。
 * 
 * @author Takuma Umezawa
 * @since 1.1.5
 */
public class PositiveIntegerInputVerifier extends InputVerifier {

	private String message;
	
	/**
	 * 
	 * @param message エラーメッセージ
	 */
	public PositiveIntegerInputVerifier(String message) {
		super();
		this.message = message;
	}

	@Override
	public boolean verify(JComponent input) {
		String text = ((JTextField) input).getText();
		
		try {
			if ( StringUtils.isBlank(text) ) {
				return true;
			}
			
			int i = Integer.parseInt(text);
			
			if (i > 0) {
				return true;
			}
			
		} catch (NumberFormatException e) {
		}
		
		JOptionPane.showMessageDialog(null,
				this.message,
				"",
				JOptionPane.ERROR_MESSAGE);
		
		return false;
	}

}
