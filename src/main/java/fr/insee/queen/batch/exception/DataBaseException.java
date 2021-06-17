package fr.insee.queen.batch.exception;

/**
 * Class to throw a FolderException
 * @author scorcaud
 *
 */
public class DataBaseException extends Exception {
	
	private static final long serialVersionUID = 1L;

	/**
	 * Defaut constructor of a FolderException
	 */
	public DataBaseException() {
		super();
	}

	/**
	 * Constructor for a FolderException
	 * @param message
	 */
	public DataBaseException(String s) {
		super(s);
	}
}
