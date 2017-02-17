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
package org.youscope.common.microscope;

/**
 * @author langmo
 */
public class MicroscopeLockedException extends Exception
{

	/**
	 * Serial version UID.
	 */
	private static final long	serialVersionUID	= -7193119368940382198L;

	/**
	 * Default constructor, indicating that the microscope is locked.
	 */
	public MicroscopeLockedException()
	{
		super("Microscope is locked and, thus, its state can not be changed (however, reading its state is allowed).");
	}

	/**
	 * Constructor.
	 * @param message Human readable message.
	 */
	public MicroscopeLockedException(String message)
	{
		super(message);
	}
}
