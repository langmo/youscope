/**
 * 
 */
package ch.ethz.csb.youscope.shared.microscope;

/**
 * @author langmo
 */
public class MicroscopeLockedException extends Exception
{

	/**
	 * Serial version UID.
	 */
	private static final long	serialVersionUID	= -7193119368940382198L;

	/**
	 * Default constructor, indicating that the microscope is locked.
	 */
	public MicroscopeLockedException()
	{
		super("Microscope is locked and, thus, its state can not be changed (however, reading its state is allowed).");
	}

	/**
	 * Constructor.
	 * @param message Human readable message.
	 */
	public MicroscopeLockedException(String message)
	{
		super(message);
	}
}
