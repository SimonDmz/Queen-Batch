package fr.insee.queen.batch.exception;

/**
 * Class to throw a ValidateException during the step of validation
 * @author scorcaud
 *
 */
public class ValidateException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Defaut constructor of a ValidateException
	 */
	public ValidateException() {
		super();
	}

	/**
	 * Constructor for a ValidateException
	 * @param message
	 */
	public ValidateException(String s) {
		super(s);
	}
}
