/**
 * 
 */
package ch.ethz.csb.youscope.addon.simplefocusscores;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.annotations.YSConfigAlias;
import ch.ethz.csb.youscope.shared.configuration.annotations.YSConfigIntegerRange;
import ch.ethz.csb.youscope.shared.resource.focusscore.FocusScoreConfiguration;

/**
 * @author Moritz Lang
 *
 */
@YSConfigAlias("autocorrelation")
@XStreamAlias("autocorrelation-focus-score")
public class AutocorrelationFocusScoreConfiguration extends FocusScoreConfiguration
{

	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -1111101676111652236L;
	
	@YSConfigAlias("lag (pixels)")
	@YSConfigIntegerRange(minValue=1)
	private int lag = 1;
	/**
	 * Constructor.
	 */
	public AutocorrelationFocusScoreConfiguration()
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
	public static final String	CONFIGURATION_ID	= "CSB::AutocorrelationFocusScore";
	
	@Override
	public String getTypeIdentifier()
	{
		return CONFIGURATION_ID;
	}

	/**
	 * Returns the lag between the two autocorrelations (in pixels).
	 * @return Lag in pixels.
	 */
	public int getLag() {
		return lag;
	}

	/**
	 * Sets the lag between the two autocorrelations (in pixels).
	 * @param lag Lag in pixels.
	 */
	public void setLag(int lag) {
		this.lag = lag;
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		if(lag < 1)
			throw new ConfigurationException("Autocorrelation lag must be at least one.");
		
	}
}
