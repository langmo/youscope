/**
 * 
 */
package ch.ethz.csb.youscope.addon.brentfocussearch;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.annotations.YSConfigAlias;
import ch.ethz.csb.youscope.shared.configuration.annotations.YSConfigDoubleRange;
import ch.ethz.csb.youscope.shared.configuration.annotations.YSConfigIntegerRange;
import ch.ethz.csb.youscope.shared.resource.focussearch.FocusSearchConfiguration;

/**
 * @author Moritz Lang
 *
 */
@YSConfigAlias("Brent optimization")
@XStreamAlias("brent-focus-search")
public class BrentFocusSearchConfiguration extends FocusSearchConfiguration
{

	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -1111111676111652234L;
	
	@YSConfigAlias("relative focus lower bound (um)")
	@XStreamAlias("focus-lower-bound")
	private double focusLowerBound = -10;
	@YSConfigAlias("relative focus upper bound (um)")
	@XStreamAlias("focus-upper-bound")
	private double focusUpperBound = 10;
	@YSConfigAlias("maximal number of search steps")
	@YSConfigIntegerRange(minValue=2)
	@XStreamAlias("max-focus-search-steps")
	private int maxSearchSteps = 100;
	@YSConfigAlias("focus tolerance (um)")
	@YSConfigDoubleRange(minValue=0.0)
	@XStreamAlias("tolerance")
	private double tolerance = 0.1;
	/**
	 * Constructor.
	 */
	public BrentFocusSearchConfiguration()
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
	public static final String	CONFIGURATION_ID	= "CSB::BrentFocusSearch";
	
	@Override
	public String getTypeIdentifier()
	{
		return CONFIGURATION_ID;
	}
	
	/**
	 * Sets the minimal relative focus. Must be smaller than the upper bound.
	 * @param focusLowerBound lower relative focus bound
	 */
	public void setFocusLowerBound(double focusLowerBound)
	{
		this.focusLowerBound = focusLowerBound;
	}

	/**
	 * Returns the minimal relative focus.
	 * @return lower relative focus bound
	 */
	public double getFocusLowerBound()
	{
		return focusLowerBound;
	}

	/**
	 * Sets the maximal relative focus. Must be bigger than the lower bound.
	 * @param focusUpperBound upper relative focus bound
	 */
	public void setFocusUpperBound(double focusUpperBound)
	{
		this.focusUpperBound = focusUpperBound;
	}

	/**
	 * Returns the maximal relative focus.
	 * @return upper relative focus bound
	 */
	public double getFocusUpperBound()
	{
		return focusUpperBound;
	}

	/**
	 * Sets the maximal number of images which should be taken and analyzed to find the optional focal plane
	 * @param maxSearchSteps maximal number of focus search steps. Must be larger than one.
	 */
	public void setMaxSearchSteps(int maxSearchSteps)
	{
		this.maxSearchSteps = maxSearchSteps;
	}
	/**
	 * Returns the maximal number of images which should be taken and analyzed to find the optional focal plane
	 * @return maximal number of focus search steps.
	 */
	public int getMaxSearchSteps()
	{
		return maxSearchSteps;
	}

	/**
	 * Returns the tolerance, in um, for the focal plane position.
	 * @return Tolerance
	 */
	public double getTolerance() {
		return tolerance;
	}

	/**
	 * Sets the tolerance, in um, for the focal plane position.
	 * @param tolerance tolerance in um. Must be bigger than 0.
	 */
	public void setTolerance(double tolerance) {
		this.tolerance = tolerance;
	}

	@Override
	public void checkConfiguration() throws ConfigurationException 
	{
		if(focusLowerBound >= focusUpperBound)
			throw new ConfigurationException("Lower bound of focus search space must be lower than upper bound.");
		else if(maxSearchSteps < 2)
			throw new ConfigurationException("There must be at least two focus search steps.");
	}
}
