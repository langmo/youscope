package org.youscope.common.measurement;

/**
 * Exception thrown if creation of a measurement component failed
 * @author Moritz Lang
 *
 */
public class ComponentCreationException extends Exception {

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 7512230465385081443L;

	/**
	 * Constructor.
	 * @param message The error message.
	 */
	public ComponentCreationException(String message) {
		super(message);
	}

	/**
	 * Constructor.
	 * @param message the error message.
	 * @param cause The more detailed cause of the exception
	 */
	public ComponentCreationException(String message, Throwable cause) {
		super(message, cause);
	}

}
