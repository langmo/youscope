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
package org.youscope.starter;

/**
 * @author langmo
 */
class ConnectionFailedException extends Exception
{
    private static final long serialVersionUID = -7514053310291595981L;

    ConnectionFailedException(String cause)
    {
        super(cause);
    }

    ConnectionFailedException(String cause, Exception e)
    {
        super(cause, e);
    }
}
