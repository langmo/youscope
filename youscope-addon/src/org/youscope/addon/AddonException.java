package org.youscope.addon;

/**
 * Exception thrown by any addon to signal an error in the addon.
 * @author Moritz Lang
 *
 */
public class AddonException extends Exception {

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 7512230411385081443L;

	/**
	 * Constructor.
	 * @param message The error message.
	 */
	public AddonException(String message) {
		super(message);
	}

	/**
	 * Constructor.
	 * @param message the error message.
	 * @param cause The more detailed cause of the exception
	 */
	public AddonException(String message, Throwable cause) {
		super(message, cause);
	}

}
