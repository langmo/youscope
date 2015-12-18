package org.youscope.common.callback;

/**
 * Exception thrown if creation of a measurement callback failed.
 * @author Moritz Lang
 *
 */
public class CallbackCreationException extends Exception {

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 7512222265385081443L;

	/**
	 * Constructor.
	 * @param message The error message.
	 */
	public CallbackCreationException(String message) {
		super(message);
	}

	/**
	 * Constructor.
	 * @param message the error message.
	 * @param cause The more detailed cause of the exception
	 */
	public CallbackCreationException(String message, Throwable cause) {
		super(message, cause);
	}

}
