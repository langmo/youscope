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
package org.youscope.plugin.fluigent;

/**
 * Exception thrown if error occurs in the communication with the Fluigent device.
 * @author Moritz Lang
 *
 */
public class ResourceException extends Exception
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 4109397143111065911L;
	
	/**
     * Constructor.
     * 
     * @param description Human readable description.
     */
    public ResourceException(String description)
    {
        super(description);
    }

    /**
     * Constructor.
     * 
     * @param parent The parent exception.
     */
    public ResourceException(Throwable parent)
    {
        super(parent);
    }

    /**
     * Constructor.
     * 
     * @param description Human readable description.
     * @param parent The parent exception.
     */
    public ResourceException(String description, Throwable parent)
    {
        super(description, parent);
    }
}
