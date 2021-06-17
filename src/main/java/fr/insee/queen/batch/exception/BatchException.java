package fr.insee.queen.batch.exception;

/**
 * Class to throw a BatchException during the execution of the batch
 * @author scorcaud
 *
 */
public class BatchException extends Exception {
	 
	private static final long serialVersionUID = 1L;

	/**
	 * Defaut constructor of a BatchException
	 */
	public BatchException() {
	    super();
	}

	/**
	 * Constructor for a BatchExcpetion
	 * @param message
	 */
	public BatchException(String s) {
		super(s);
	}
}
