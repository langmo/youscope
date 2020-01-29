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
package org.youscope.plugin.custommicroplates;

/**
 * Exception thrown to indicate that initializing a custom microplate, creating or deleting it, or similar, failed.
 * @author Moritz Lang
 * 
 */
public class CustomMicroplateException extends Exception
{

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 1834665729033073553L;

	/**
	 * Constructor.
	 * 
	 * @param description
	 */
	public CustomMicroplateException(String description)
	{
		super(description);
	}

	/**
	 * Constructor.
	 * 
	 * @param parent
	 */
	public CustomMicroplateException(Throwable parent)
	{
		super(parent);
	}

	/**
	 * Constructor.
	 * 
	 * @param description
	 * @param parent
	 */
	public CustomMicroplateException(String description, Throwable parent)
	{
		super(description, parent);
	}
}
