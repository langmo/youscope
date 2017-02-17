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
package org.youscope.addon.microscopeaccess;

/**
 * @author langmo
 */
public class MicroscopeConnectionException extends Exception
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = -2633690413777862118L;

    /**
     * Constructor.
     * 
     * @param description Human readable description of the exception.
     */
    public MicroscopeConnectionException(String description)
    {
        super(description);
    }

    /**
     * Constructor.
     * 
     * @param description Human readable description of the exception.
     * @param cause The cause of the exception.
     */
    public MicroscopeConnectionException(String description, Throwable cause)
    {
        super(description, cause);
    }
}
