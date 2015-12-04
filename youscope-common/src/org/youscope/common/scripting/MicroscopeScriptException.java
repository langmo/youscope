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
