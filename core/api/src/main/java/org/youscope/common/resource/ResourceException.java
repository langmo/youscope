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
package org.youscope.common.resource;

import org.youscope.common.ComponentException;

/**
 * Exception thrown by resources.
 * @author Moritz Lang
 *
 */
public class ResourceException extends ComponentException {

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 1111823544103386723L;

	/**
	 * Constructor.
	 * 
	 * @param description
	 */
	public ResourceException(String description)
	{
		super(description);
	}

	/**
	 * Constructor.
	 * 
	 * @param description
	 * @param parent
	 */
	public ResourceException(String description, Throwable parent)
	{
		super(description, parent);
	}

}
