/**
 * 
 */
package org.youscope.common.microscope;

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
