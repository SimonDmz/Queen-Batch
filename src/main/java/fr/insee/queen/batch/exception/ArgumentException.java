package fr.insee.queen.batch.exception;

/**
 * Class to throw a ArgumentException during the execution of the batch
 * @author scorcaud
 *
 */
public class ArgumentException extends Exception {
	 
	private static final long serialVersionUID = 1L;

	/**
	 * Defaut constructor of a ArgumentException
	 */
	public ArgumentException() {
	    super();
	}

	/**
	 * Constructor for a ArgumentException
	 * @param message
	 */
	public ArgumentException(String s) {
		super(s);
	}
}
