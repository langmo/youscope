/**
 * 
 */
package org.youscope.plugin.microplatejob;

import java.util.Vector;

import org.youscope.common.configuration.FocusConfiguration;
import org.youscope.common.configuration.JobConfiguration;
import org.youscope.common.configuration.JobContainerConfiguration;
import org.youscope.plugin.microplatemeasurement.MicroplatePositionConfigurationDTO;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This class represents the configuration of a user configurable microplate job.
 * 
 * @author Moritz Lang
 */
@XStreamAlias("microplate-job")
public class MicroplateJobConfigurationDTO extends JobConfiguration implements JobContainerConfiguration
{
	/**
	 * Serial version UID.
	 */
	private static final long						serialVersionUID	= 3413810800791783902L;

	/**
	 * A list of all the jobs which should be done during the measurement.
	 */
	private Vector<JobConfiguration>	jobs				= new Vector<JobConfiguration>();

	@XStreamAlias("path-optimizer")
	private String pathOptimizerID = null;
	
	@XStreamAlias("stage")
	private String stageDevice = null;
	
	@Override
	public JobConfiguration[] getJobs()
	{
		return jobs.toArray(new JobConfiguration[jobs.size()]);
	}

	@Override
	public void setJobs(JobConfiguration[] jobs)
	{
		this.jobs.clear();
		for(JobConfiguration job:jobs)
		{
			this.jobs.add(job);
		}
	}

	@Override
	public void addJob(JobConfiguration job)
	{
		jobs.add(job);
	}

	@Override
	public void clearJobs()
	{
		jobs.clear();
	}

	/**
	 * The identifier for this measurement type.
	 */
	public static final String								TYPE_IDENTIFIER		= "CSB::MicroPlateJob";

	/**
	 * Type of plate.
	 */
	@XStreamAlias("microplate")
	private MicroplatePositionConfigurationDTO	microplatePositions	= new MicroplatePositionConfigurationDTO();

	/**
	 * Configuration of the focus device used for focussing the wells. Set to
	 * null to not set focus in wells.
	 */
	@XStreamAlias("focus")
	private FocusConfiguration		 			focusConfiguration	= null;

	@Override
	public String getTypeIdentifier()
	{
		return TYPE_IDENTIFIER;
	}

	@Override
	public void removeJobAt(int index)
	{
		jobs.removeElementAt(index);
	}

	@Override
	public void addJob(JobConfiguration job, int index)
	{
		jobs.insertElementAt(job, index);

	}

	/**
	 * @param focusConfiguration
	 *            The configuration of the focus used when entering a well. Set
	 *            to NULL to not set focus.
	 */
	public void setFocusConfiguration(FocusConfiguration focusConfiguration)
	{
		this.focusConfiguration = focusConfiguration;
	}

	/**
	 * @return The configuration of the focus used when entering a well. NULL if
	 *         focus is not set.
	 */
	public FocusConfiguration getFocusConfiguration()
	{
		return focusConfiguration;
	}

	/**
	 * Sets the used microplate and the measured positions therein.
	 * @param microplatePositions the configuration of the used microplate and the measured positions therein.
	 */
	public void setMicroplatePositions(MicroplatePositionConfigurationDTO microplatePositions)
	{
		this.microplatePositions = microplatePositions;
	}

	/**
	 * Returns the used microplate and the measured positions therein.
	 * @return the configuration of the used microplate and the measured positions therein.
	 */
	public MicroplatePositionConfigurationDTO getMicroplatePositions()
	{
		return microplatePositions;
	}

	/**
	 * Sets the ID of the optimizer which should be used to minimize the distances between two wells/positions measured.
	 * Set to null to not use any optimized path.
	 * @param pathOptimizerID The ID of the path optimizer used, or null
	 */
	public void setPathOptimizerID(String pathOptimizerID)
	{
		this.pathOptimizerID = pathOptimizerID;
	}

	/**
	 * Returns the ID of the optimizer which should be used to minimize the distances between two wells/positions measured.
	 * Returns null if not uses any optimized path.
	 * @return The ID of the path optimizer used, or null
	 */
	public String getPathOptimizerID()
	{
		return pathOptimizerID;
	}

	/**
	 * Sets the ID of the stage device which should be used to change between wells and positions. Set to NULL to use default stage device.
	 * @param stageDevice The ID of the stage device, or NULL.
	 */
	public void setStageDevice(String stageDevice)
	{
		this.stageDevice = stageDevice;
	}

	/**
	 * Returns the ID of the stage device which should be used to change between wells and positions. Returns NULL to use default stage device.
	 * @return The ID of the stage device, or NULL.
	 */
	public String getStageDevice()
	{
		return stageDevice;
	}

	@Override
	public String getDescription()
	{
		String description = "<p>Microplate Job</p>";
		if(microplatePositions != null && microplatePositions.getNumMeasuredWells() > 0)
		{
			description += "<p>for well=1:" + Integer.toString(microplatePositions.getNumMeasuredWells()) + "</p>"+
			"<ul style=\"margin-top:0px;margin-bottom:0px;margin-left:12px;list-style-type:none\">";
			for(JobConfiguration job : jobs)
			{
				description += "<li>" + job.getDescription() + "</li>";
			}
			description += "</ul><p>end</p>";
		}
		else
		{
			description += "<p>No well specified.</p>";
		}
		return description;
	}
}
