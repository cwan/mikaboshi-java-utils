package net.mikaboshi.validator;

/**
 * {@code net.mikaboshi.validator.*} の処理において致命的なエラーが発生した場合に
 * throw される。
 * 例えば、リフレクションに失敗した場合など。
 * 
 * @author Takuma Umezawa
 */
public class ValidatorError extends Error {

	private static final long serialVersionUID = -2850808660885082315L;

	public ValidatorError(Throwable cause) {
		super(cause);
	}
	
}
