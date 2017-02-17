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
package org.youscope.common.task;

import org.youscope.common.ComponentException;

/**
 * Exception thrown by a task to indicate that its initialization, execution or deinitialization failed.
 * @author Moritz Lang
 * 
 */
public class TaskException extends ComponentException
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 5218835175494966494L;

	/**
	 * Constructor.
	 * 
	 * @param description Description of the error.
	 */
	public TaskException(String description)
	{
		super(description);
	}

	/**
	 * Constructor.
	 * 
	 * @param parent Parent exception.
	 */
	public TaskException(Throwable parent)
	{
		super(parent);
	}

	/**
	 * Constructor.
	 * 
	 * @param description Description of the error.
	 * @param parent Parent exception.
	 */
	public TaskException(String description, Throwable parent)
	{
		super(description, parent);
	}
}
