/**
 * 
 */
package ch.ethz.csb.youscope.addon.simplefocusscores;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.annotations.YSConfigAlias;
import ch.ethz.csb.youscope.shared.resource.focusscore.FocusScoreConfiguration;

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
	public static final String	CONFIGURATION_ID	= "CSB::VarianceFocusScore";
	
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
