package org.youscope.plugin.travelingsalesman.blossom;

/**
 * Exception thrown in blossom algorithm.
 * @author Moritz Lang
 *
 */
public class BlossomException extends Exception {

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -885010991662839919L;
	/**
	 * Constructor.
	 * @param description error description.
	 */
	public BlossomException(String description) {
		super(description);
	}
	/**
	 * Constructor.
	 * @param description error description.
	 * @param cause Cause of the exception.
	 */
	public BlossomException(String description, Throwable cause) {
		super(description, cause);
	}
}
