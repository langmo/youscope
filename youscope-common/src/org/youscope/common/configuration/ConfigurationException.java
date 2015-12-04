/**
 * 
 */
package org.youscope.common.configuration;

/**
 * @author langmo
 */
public class ConfigurationException extends Exception
{

    /**
     * Serial Version UID.
     */
    private static final long serialVersionUID = -6409165571340277838L;

    /**
     * Constructor.
     * 
     * @param description
     */
    public ConfigurationException(String description)
    {
        super(description);
    }

    /**
     * Constructor.
     * 
     * @param parent
     */
    public ConfigurationException(Throwable parent)
    {
        super(parent);
    }

    /**
     * Constructor.
     * 
     * @param description
     * @param parent
     */
    public ConfigurationException(String description, Throwable parent)
    {
        super(description, parent);
    }
}
