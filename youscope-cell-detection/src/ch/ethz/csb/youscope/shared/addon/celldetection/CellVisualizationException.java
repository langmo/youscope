/**
 * 
 */
package ch.ethz.csb.youscope.shared.addon.celldetection;

import ch.ethz.csb.youscope.shared.measurement.resource.ResourceException;

/**
 * Exception thrown by cell visualization algorithms.
 * @author Moritz Lang
 *
 */
public class CellVisualizationException extends ResourceException
{

	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 1411110243352359988L;

	/**
	 * @param message
	 * @param cause
	 */
	public CellVisualizationException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public CellVisualizationException(String message)
	{
		super(message);
	}
}
