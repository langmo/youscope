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
package org.youscope.addon.component.generic;

/**
 * Error thrown when analyzing a configuration class with java reflection.
 * @author Moritz Lang
 *
 */
public class GenericException extends Exception 
{

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -498050681654341501L;

	/**
	 * Constructor.
	 * @param message Error message.
	 */
	public GenericException(String message) 
	{
		super(message);
	}

	/**
	 * Constructor.
	 * @param message Error message.
	 * @param cause Cause of the error.
	 */
	public GenericException(String message, Throwable cause) 
	{
		super(message, cause);
	}
}
