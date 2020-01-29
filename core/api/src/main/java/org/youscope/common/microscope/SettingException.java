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
 * An exception thrown if a requested setting is undefined or a provided setting is invalid.
 * @author Moritz Lang
 * 
 */
public class SettingException extends Exception
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 1177656230830798767L;

	/**
	 * Constructor.
	 * 
	 * @param description Human readable description of the exception.
	 */
	public SettingException(String description)
	{
		super(description);
	}

	/**
	 * Constructor.
	 * 
	 * @param description Human readable description of the exception.
	 * @param cause The cause of the exception.
	 */
	public SettingException(String description, Exception cause)
	{
		super(description, cause);
	}
}
