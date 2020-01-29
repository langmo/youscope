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
 * Base class of all exceptions thrown by measurement components.
 * @author Moritz Lang
 * 
 */
public abstract class ComponentException extends Exception
{
	/**
	 * Serial version UID.
	 */
	private static final long	serialVersionUID	= -5853816701125425656L;

	/**
	 * Constructor.
	 * 
	 * @param description 
	 */
	public ComponentException(String description)
	{
		super(description);
	}

	/**
	 * Constructor.
	 * 
	 * @param parent
	 */
	public ComponentException(Throwable parent)
	{
		super(parent);
	}

	/**
	 * Constructor.
	 * 
	 * @param description
	 * @param parent
	 */
	public ComponentException(String description, Throwable parent)
	{
		super(description, parent);
	}
}
