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
package org.youscope.addon.celldetection;

import org.youscope.common.resource.ResourceException;

/**
 * Exception thrown by cell visualization algorithms.
 * @author Moritz Lang
 *
 */
public class CellVisualizationException extends ResourceException
{

	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 1411110243352359988L;

	/**
	 * @param message
	 * @param cause
	 */
	public CellVisualizationException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public CellVisualizationException(String message)
	{
		super(message);
	}
}
