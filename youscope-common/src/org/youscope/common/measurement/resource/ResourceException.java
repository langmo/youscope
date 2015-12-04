package org.youscope.common.measurement.resource;

import org.youscope.common.measurement.ComponentException;

/**
 * Exception thrown by resources.
 * @author Moritz Lang
 *
 */
public class ResourceException extends ComponentException {

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 1111823544103386723L;

	/**
	 * Constructor.
	 * 
	 * @param description
	 */
	public ResourceException(String description)
	{
		super(description);
	}

	/**
	 * Constructor.
	 * 
	 * @param description
	 * @param parent
	 */
	public ResourceException(String description, Throwable parent)
	{
		super(description, parent);
	}

}
