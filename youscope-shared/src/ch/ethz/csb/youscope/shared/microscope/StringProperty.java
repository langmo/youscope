/**
 * 
 */
package ch.ethz.csb.youscope.shared.microscope;

import java.rmi.RemoteException;

/**
 * @author Moritz Lang
 * 
 */
public interface StringProperty extends Property
{
	@Override
	public void setValue(String value) throws MicroscopeException, MicroscopeLockedException, InterruptedException, RemoteException;
}
