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
package org.youscope.common.job;

import org.youscope.common.ComponentException;

/**
 * Exception thrown by a job to indicate that its initialization, execution or uninitialization failed.
 * @author Moritz Lang
 * 
 */
public class JobException extends ComponentException
{
	/**
	 * Serial version UID.
	 */
	private static final long	serialVersionUID	= -5853816707725425656L;

	/**
	 * Constructor.
	 * 
	 * @param description
	 */
	public JobException(String description)
	{
		super(description);
	}

	/**
	 * Constructor.
	 * 
	 * @param parent
	 */
	public JobException(Throwable parent)
	{
		super(parent);
	}

	/**
	 * Constructor.
	 * 
	 * @param description
	 * @param parent
	 */
	public JobException(String description, Throwable parent)
	{
		super(description, parent);
	}
}
