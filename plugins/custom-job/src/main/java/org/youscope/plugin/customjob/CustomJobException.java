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
package org.youscope.plugin.customjob;

/**
 * Exception thrown by a job to indicate that initializing a custom job, creating or deleting it, or similar, failed.
 * @author Moritz Lang
 * 
 */
public class CustomJobException extends Exception
{
	/**
	 * Serial version UID.
	 */
	private static final long	serialVersionUID	= -5888816707725425656L;

	/**
	 * Constructor.
	 * 
	 * @param description
	 */
	public CustomJobException(String description)
	{
		super(description);
	}

	/**
	 * Constructor.
	 * 
	 * @param parent
	 */
	public CustomJobException(Throwable parent)
	{
		super(parent);
	}

	/**
	 * Constructor.
	 * 
	 * @param description
	 * @param parent
	 */
	public CustomJobException(String description, Throwable parent)
	{
		super(description, parent);
	}
}
