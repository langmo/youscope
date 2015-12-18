/**
 * 
 */
package org.youscope.plugin.simplefocusscores;

import org.youscope.addon.focusscore.FocusScoreConfiguration;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.configuration.YSConfigAlias;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author Moritz Lang
 *
 */
@YSConfigAlias("variance")
@XStreamAlias("variance-focus-score")
public class VarianceFocusScoreConfiguration extends FocusScoreConfiguration
{

	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -1111201676111652236L;
	
	/**
	 * Constructor.
	 */
	public VarianceFocusScoreConfiguration()
	{
		// do nothing.
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}

	/**
	 * The identifier for this configuration.
	 */
	public static final String	CONFIGURATION_ID	= "YouScope.VarianceFocusScore";
	
	@Override
	public String getTypeIdentifier()
	{
		return CONFIGURATION_ID;
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		// nothing to check.
	}
}
