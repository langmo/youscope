/*******************************************************************************
 * Copyright (c) 2017 Moritz Lang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Moritz Lang - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package org.youscope.common;

/**
 * Exception thrown if trying to change the configuration of a {@link Component} while it is executed.
 * 
 * @author Moritz Lang
 */
public class ComponentRunningException extends ComponentException
{

	/**
	 * Serial version UID.
	 */
	private static final long	serialVersionUID	= -3628890068985604340L;

	/**
	 * Constructor.
	 */
	public ComponentRunningException()
	{
		this("Measurement component cannot be modified while component is executed.");
	}
	/**
	 * Constructor.
	 * @param message Error message.
	 */
	public ComponentRunningException(String message)
	{
		super(message);
	}
}
