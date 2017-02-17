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
package org.youscope.common.scripting;

/**
 * @author langmo
 */
public class MicroscopeScriptException extends Exception
{

    /**
     * Constructor.
     * 
     * @param description
     * @param parent
     */
    public MicroscopeScriptException(String description, Exception parent)
    {
        super(description, parent);
    }

    /**
     * Constructor.
     * 
     * @param description
     */
    public MicroscopeScriptException(String description)
    {
        super(description);
    }

    /**
     * Constructor.
     */
    public MicroscopeScriptException()
    {
        super();
    }

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = -5489520364040481252L;

}
