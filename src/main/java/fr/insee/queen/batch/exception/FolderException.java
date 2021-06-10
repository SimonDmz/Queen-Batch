package fr.insee.queen.batch.exception;

/**
 * Class to throw a FolderException
 * @author scorcaud
 *
 */
public class FolderException extends Exception {
	
	private static final long serialVersionUID = 1L;

	/**
	 * Defaut constructor of a FolderException
	 */
	public FolderException() {
		super();
	}

	/**
	 * Constructor for a FolderException
	 * @param message
	 */
	public FolderException(String s) {
		super(s);
	}
}
