/**
 * 
 */
package ch.ethz.csb.youscope.addon.matlabfocusscores;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.annotations.YSConfigAlias;
import ch.ethz.csb.youscope.shared.resource.focusscore.FocusScoreConfiguration;

/**
 * @author Moritz Lang
 *
 */
@YSConfigAlias("Matlab based score")
@XStreamAlias("matlab-focus-score")
public class MatlabScoresConfiguration extends FocusScoreConfiguration
{

	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -1111201676160652236L;
	
	@YSConfigAlias("Matlab algorithm")
	private MatlabScoreType scoreAlgorithm = MatlabScoreType.SOBEL3;
	
	/**
	 * Constructor.
	 */
	public MatlabScoresConfiguration()
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
	public static final String	CONFIGURATION_ID	= "CSB::MatlabFocusScore";
	
	@Override
	public String getTypeIdentifier()
	{
		return CONFIGURATION_ID;
	}

	/**
	 * Sets the algorithm for the focus score calculation.
	 * @param scoreAlgorithm
	 */
	public void setScoreAlgorithm(MatlabScoreType scoreAlgorithm)
	{
		this.scoreAlgorithm = scoreAlgorithm;
	}

	/**
	 * @return algorithm to calculate the score.
	 */
	public MatlabScoreType getScoreAlgorithm()
	{
		return scoreAlgorithm;
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		// nothing to check.
		
	}
}
