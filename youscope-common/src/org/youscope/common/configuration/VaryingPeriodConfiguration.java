/**
 * 
 */
package org.youscope.common.configuration;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * This class represents a varying period length. The actual period length for a given iteration is a member of an array of predefined period lengths.
 * @author Moritz Lang
 */
@XStreamAlias("varying-period")
public class VaryingPeriodConfiguration extends PeriodConfiguration
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 5382800416891354802L;

	/**
	 * Times between single executions (in milliseconds).
	 */
	@XStreamImplicit(itemFieldName = "period")
	private int[]				periods				= new int[0];

	/**
	 * After all periods are run through, the job will pause for <breakTime> ms and then start with
	 * periods[0] again.
	 */
	@XStreamAlias("break")
	@XStreamAsAttribute
	private int					breakTime			= 0;

	/**
	 * Sets an additional time which should be waited after iterating through all periods. Default is 0.
	 * @param breakTime Wait time in ms.
	 */
	public void setBreakTime(int breakTime)
	{
		this.breakTime = breakTime;
	}
	
	@Override
	public String getTypeIdentifier() 
	{
		return "CSB::VaryingPeriod";
	}

	/**
	 * Returns the additional time which should be waited after iterating through all periods. Default is 0.
	 * @return Wait time in ms.
	 */
	public int getBreakTime()
	{
		return breakTime;
	}

	/**
	 * Sets the periods. Setting it to null has no effect.
	 * @param periods the periods to set (in ms).
	 */
	public void setPeriods(int[] periods)
	{
		if(periods == null)
			return;
		this.periods = new int[periods.length];
		System.arraycopy(periods, 0, this.periods, 0, periods.length);
	}

	/**
	 * Returns the periods.
	 * @return the periods to set (in ms).
	 */
	public int[] getPeriods()
	{
		int[] periods = new int[this.periods.length];
		System.arraycopy(this.periods, 0, periods, 0, this.periods.length);
		return periods;
	}

	@Override
	public Object clone() throws CloneNotSupportedException
	{
		VaryingPeriodConfiguration clone = (VaryingPeriodConfiguration)super.clone();
		clone.periods = new int[periods.length];
		System.arraycopy(periods, 0, clone.periods, 0, periods.length);
		return clone;
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		super.checkConfiguration();
		if(periods == null || periods.length == 0)
			throw new ConfigurationException("There must be at least one period defined.");
	}
}
