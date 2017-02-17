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
package org.youscope.common.table;

/**
 * Exception thrown by table entries.
 * @author Moritz Lang
 */
public class TableException extends Exception
{
    /**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -1654663380102330692L;

	/**
     * Constructor.
     * 
     * @param description Description of the exception.
     */
    public TableException(String description)
    {
        super(description);
    }

    /**
     * Constructor.
     * 
     * @param parent Parent exception.
     */
    public TableException(Throwable parent)
    {
        super(parent);
    }

    /**
     * Constructor.
     * 
     * @param description Description of the exception.
     * @param parent Parent exception.
     */
    public TableException(String description, Throwable parent)
    {
        super(description, parent);
    }
}
