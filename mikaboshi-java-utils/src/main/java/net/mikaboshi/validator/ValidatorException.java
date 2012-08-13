package net.mikaboshi.validator;

/**
 * Validator のチェック失敗時に throw される実行時例外クラス。
 * @author Takuma Umezawa
 */
public class ValidatorException extends RuntimeException {

	private static final long serialVersionUID = 6970191766846753061L;

	public ValidatorException(String message) {
		super(message);
	}
	
}
