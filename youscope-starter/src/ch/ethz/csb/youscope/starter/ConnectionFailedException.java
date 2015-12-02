/**
 * 
 */
package ch.ethz.csb.youscope.starter;

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
