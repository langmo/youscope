/**
 * 
 */
package org.youscope.addon.microscopeaccess;

import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.MicroscopeLockedException;


/**
 * @author Moritz Lang
 *
 */
public interface StringPropertyInternal extends PropertyInternal
{
	@Override
	public void setValue(String value, int accessID) throws MicroscopeException, MicroscopeLockedException, InterruptedException;
}
