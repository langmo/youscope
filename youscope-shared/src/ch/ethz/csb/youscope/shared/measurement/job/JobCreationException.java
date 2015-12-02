/**
 * 
 */
package ch.ethz.csb.youscope.shared.measurement.job;

import ch.ethz.csb.youscope.shared.addon.AddonException;

/**
 * Thrown when tried to construct a job which is not known or illegal.
 * @author Moritz Lang
 * 
 */
public class JobCreationException extends AddonException
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -6409165571340211838L;

	/**
	 * Constructor.
	 * @param message The error message.
	 */
	public JobCreationException(String message) {
		super(message);
	}

	/**
	 * Constructor.
	 * @param message the error message.
	 * @param cause The more detailed cause of the exception
	 */
	public JobCreationException(String message, Throwable cause) {
		super(message, cause);
	}
}
