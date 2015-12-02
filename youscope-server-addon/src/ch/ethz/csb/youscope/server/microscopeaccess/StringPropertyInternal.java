/**
 * 
 */
package ch.ethz.csb.youscope.server.microscopeaccess;

import ch.ethz.csb.youscope.shared.microscope.MicroscopeException;
import ch.ethz.csb.youscope.shared.microscope.MicroscopeLockedException;


/**
 * @author Moritz Lang
 *
 */
public interface StringPropertyInternal extends PropertyInternal
{
	@Override
	public void setValue(String value, int accessID) throws MicroscopeException, MicroscopeLockedException, InterruptedException;
}
