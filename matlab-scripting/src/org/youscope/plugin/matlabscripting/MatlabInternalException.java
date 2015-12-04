package org.youscope.plugin.matlabscripting;

import com.mathworks.jmi.MatlabException;

/**
 * A wrapper around com.mathworks.jmi.MatlabException so that the exception can be sent over RMI
 * without needing the jmi.jar to be included by the developer, but still prints identically.
 * 
 * @author <a href="mailto:jak2@cs.brown.edu">Joshua Kaplan</a>
 */
public class MatlabInternalException extends Exception
{
    private static final long serialVersionUID = 1L;

    /**
     * Creates a wrapper around <code>innerException</code> so that when the stack trace is printed
     * it is the same to the developer, but can be easily sent over RMI.
     * 
     * @param innerException
     */
    MatlabInternalException(MatlabException innerException)
    {
        super(innerException.toString());

        // Set this stack trace to that of the innerException
        this.setStackTrace(innerException.getStackTrace());
    }
}