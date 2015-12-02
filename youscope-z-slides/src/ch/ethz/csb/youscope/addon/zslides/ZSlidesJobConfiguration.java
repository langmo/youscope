/**
 * 
 */
package ch.ethz.csb.youscope.addon.zslides;

import java.util.Arrays;
import java.util.Formatter;

import ch.ethz.csb.youscope.shared.configuration.FocusConfiguration;
import ch.ethz.csb.youscope.shared.configuration.JobContainerConfiguration;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * @author langmo
 */
@XStreamAlias("z-slides-job")
public class ZSlidesJobConfiguration extends JobConfiguration implements JobContainerConfiguration
{

	/**
	 * Serial Version UID.
	 */
	private static final long						serialVersionUID		= 8198191383459721511L;

	/**
	 * The jobs which should be run at every position.
	 */
	@XStreamAlias("jobs")
	private JobConfiguration[]	jobs					= new JobConfiguration[0];

	@XStreamImplicit(itemFieldName = "relative-focus-position-um")
	private double[] slideZPositions = new double[0];

	/**
	 * Configuration of the focus device used for focusing.
	 */
	@XStreamAlias("focus-configuration")
	private FocusConfiguration	focusConfiguration	= null;
	
	/**
	 * @param focusConfiguration
	 *            The configuration of the focus .
	 */
	public void setFocusConfiguration(FocusConfiguration focusConfiguration)
	{
		this.focusConfiguration = focusConfiguration;
	}

	/**
	 * @return The configuration of the focus.
	 */
	public FocusConfiguration getFocusConfiguration()
	{
		return focusConfiguration;
	}

	
	
	@Override
	public String getDescription()
	{
		String description;
		if(slideZPositions.length > 0)
		{
			description = "<p>" + "for z = [";
			for(int i=0; i < slideZPositions.length; i++)
			{
				if(i > 0)
					description += ", ";
				description += (new Formatter()).format("%2.2f", slideZPositions[i]);
			}
			description += "]</p><ul style=\"margin-top:0px;margin-bottom:0px;margin-left:12px;list-style-type:none\"><li>";
			if(focusConfiguration == null || focusConfiguration.getFocusDevice() == null)
				description += "focus";
			else
				description += focusConfiguration.getFocusDevice();

			description += ".position += z";
			description += "</li>";
			
			for(JobConfiguration job : jobs)
			{
				description += "<li>" + job.getDescription() + "</li>";
			}
			description += "</ul><p>end</p>";
		}
		else
		{
			description = "<p>Empty Z-Slides</p>";
		}
		return description;
	}

	@Override
	public Object clone() throws CloneNotSupportedException
	{
		ZSlidesJobConfiguration clone = (ZSlidesJobConfiguration)super.clone();
		clone.jobs = new JobConfiguration[jobs.length];
		for(int i = 0; i < jobs.length; i++)
		{
			clone.jobs[i] = (JobConfiguration)jobs[i].clone();
		}

		clone.slideZPositions = Arrays.copyOf(slideZPositions, slideZPositions.length);
		if(focusConfiguration != null)
			clone.focusConfiguration = focusConfiguration.clone();
		return clone;
	}

	@Override
	public JobConfiguration[] getJobs()
	{
		return jobs;
	}

	@Override
	public void setJobs(JobConfiguration[] jobs)
	{
		this.jobs = jobs;
	}

	@Override
	public void addJob(JobConfiguration job)
	{
		if(job == null)
			return;
		jobs = Arrays.copyOf(jobs, jobs.length+1);
		jobs[jobs.length - 1] = job;
	}

	@Override
	public void clearJobs()
	{
		jobs = new JobConfiguration[0];
	}

	@Override
	public void removeJobAt(int index)
	{
		JobConfiguration[] newJobs = new JobConfiguration[jobs.length - 1];
		for(int i=0; i < newJobs.length; i++)
		{
			newJobs[i] = jobs[i >= index? i+1 : i];
		}
		jobs = newJobs;
	}

	@Override
	public void addJob(JobConfiguration job, int index)
	{
		JobConfiguration[] newJobs = new JobConfiguration[jobs.length + 1];
		for(int i=0; i < newJobs.length; i++)
		{
			if(i < index)
				newJobs[i] = jobs[i];
			else if(i == index)
				newJobs[i] = job;
			else
				newJobs[i] = jobs[i-1];
		}
		jobs = newJobs;
	}

	/**
	 * The identifier for this job type.
	 */
	public static final String	TYPE_IDENTIFIER	= "CSB::ZSlidesJob";

	@Override
	public String getTypeIdentifier()
	{
		return TYPE_IDENTIFIER;
	}

	/**
	 * Sets the z positions where the jobs should be executed.
	 * @param slideZPositions
	 */
	public void setSlideZPositions(double[] slideZPositions)
	{
		if(slideZPositions == null)
			this.slideZPositions = new double[0];
		else
			this.slideZPositions = slideZPositions;
	}

	/**
	 * Returns the z-positions where the jobs should be executed.
	 * @return z-positions.
	 */
	public double[] getSlideZPositions()
	{
		return slideZPositions;
	}
}
