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
package org.youscope.common.callback;

import org.youscope.common.ComponentException;

/**
 * Exception thrown by measurement callbacks.
 * @author Moritz Lang
 * 
 */
public class CallbackException extends ComponentException
{
	/**
	 * Serial version UID.
	 */
	private static final long	serialVersionUID				= -2814438972668943211L;

	/**
	 * Constructor.
	 * 
	 * @param description Human readable description of the exception.
	 */
	public CallbackException(String description)
	{
		super(description);
	}


	/**
	 * Constructor.
	 * 
	 * @param description Human readable description of the exception.
	 * @param cause the parent exception.
	 */
	public CallbackException(String description, Throwable cause)
	{
		super(description, cause);
	}
}
