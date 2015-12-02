/**
 * 
 */
package ch.ethz.csb.youscope.shared.configuration;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.basic.BooleanConverter;

/**
 * @author langmo
 */
@XStreamAlias("regular-period")
public class RegularPeriod extends Period
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 1308127813101097630L;

	/**
	 * The period time in milliseconds.
	 */
	@XStreamAlias("period")
	@XStreamAsAttribute
	private int					period				= 10000;

	/**
	 * TRUE if starts of executions should be exactly <period> ms away from each other. FALSE if the
	 * end of one execution should be <period> ms away from the start of the next one.
	 */
	@XStreamAlias("fixed")
	@XStreamAsAttribute
	@XStreamConverter(value = BooleanConverter.class, booleans = {false}, strings = {"yes", "no"})
	private boolean				fixedTimes			= false;

	@Override
	public String getTypeIdentifier() 
	{
		return "CSB::RegularPeriod";
	}
	
	/**
	 * @param fixedTimes the fixedTimes to set
	 */
	public void setFixedTimes(boolean fixedTimes)
	{
		this.fixedTimes = fixedTimes;
	}

	/**
	 * @return the fixedTimes
	 */
	public boolean isFixedTimes()
	{
		return fixedTimes;
	}

	/**
	 * @param period the period to set
	 */
	public void setPeriod(int period)
	{
		this.period = period;
	}

	/**
	 * @return the period
	 */
	public int getPeriod()
	{
		return period;
	}

	@Override
	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}

	@Override
	public void checkConfiguration() throws ConfigurationException 
	{
		super.checkConfiguration();
		if(period < 0)
			throw new ConfigurationException("Period length must be bigger or equal to zero.");
	}
}
