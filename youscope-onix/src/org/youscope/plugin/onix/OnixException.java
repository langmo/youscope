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
package org.youscope.plugin.onix;

/**
 * General purpose exception thrown by the Onix microfluidic device.
 * @author Moritz Lang
 *
 */
public class OnixException extends Exception
{

	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 4209397143032065915L;
	
	/**
     * Constructor.
     * 
     * @param description Human readable description.
     */
    public OnixException(String description)
    {
        super(description);
    }

    /**
     * Constructor.
     * 
     * @param parent The parent exception.
     */
    public OnixException(Throwable parent)
    {
        super(parent);
    }

    /**
     * Constructor.
     * 
     * @param description Human readable description.
     * @param parent The parent exception.
     */
    public OnixException(String description, Throwable parent)
    {
        super(description, parent);
    }

}
