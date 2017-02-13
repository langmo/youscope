package org.youscope.clientinterfaces;

/**
 * Exception thrown by the {@link YouScopeClient} to indicate that a requested operation failed due to an exception on the client side.
 * @author Moritz Lang
 */
public class YouScopeClientException extends Exception { 

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -6285854028085158122L;

	/**
	 * Constructor.
	 * @param message The error message.
	 */
	public YouScopeClientException(String message) {
		super(message);
	}

	/**
	 * Constructor.
	 * @param message the error message.
	 * @param cause The more detailed cause of the exception
	 */
	public YouScopeClientException(String message, Throwable cause) {
		super(message, cause);
	}
}
