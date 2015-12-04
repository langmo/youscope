/**
 * 
 */
package org.youscope.common.configuration;


/**
 * The abstract superclass of all configurable measurement jobs/tasks which should be activated in
 * regular intervals..
 * 
 * @author Moritz Lang
 */
public abstract class JobConfiguration implements Configuration
{
	/**
	 * Serial Version UID
	 */
	private static final long	serialVersionUID	= 76205644694553051L;

	/**
	 * Returns a short description of this job.
	 * 
	 * @return Short description of the job.
	 */
	public abstract String getDescription();

	@Override
	public String toString()
	{
		return getDescription();
	}

	@Override
	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		// do nothing.
	}
}
