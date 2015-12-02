/**
 * 
 */
package ch.ethz.csb.youscope.addon.continousimaging;

import java.util.Vector;

import ch.ethz.csb.youscope.server.addon.job.JobConstructionAddon;
import ch.ethz.csb.youscope.server.addon.job.JobConstructionAddonFactory;
import ch.ethz.csb.youscope.shared.measurement.job.Job;

/**
 * This implemenatation by purpose does not allow to create a continuous imaging job over a configuration.
 * This would allow to add such a job to any kind of measurement, at any position, which would lead to unwanted effects.
 * Instead, it only allows to create the job directly, which is typically only done on purpose...
 * @author Moritz Lang
 * 
 */
public class AllJobConstructionAddonFactory implements JobConstructionAddonFactory
{

	@Override
	public JobConstructionAddon createJobConstructionAddon(String ID)
	{
		if(ID.equals(ShortContinuousImagingJobConfiguration.TYPE_IDENTIFIER))
			return new ShortContinuousImagingJobConstructionAddon();
		return null;
	}

	@Override
	public String[] getSupportedConfigurationIDs()
	{
		return new String[]{ShortContinuousImagingJobConfiguration.TYPE_IDENTIFIER};
	}

	@Override
	public boolean supportsConfigurationID(String ID)
	{
		if(ID.equals(ShortContinuousImagingJobConfiguration.TYPE_IDENTIFIER))
			return true;
		return false;
	}
	@Override
	public Iterable<Class<? extends Job>> getJobImplementations()
	{
		Vector<Class<? extends Job>> implementations = new Vector<Class<? extends Job>>();
		implementations.addElement(ContinuousImagingJobImpl.class);
		implementations.addElement(ShortContinuousImagingJobImpl.class);
		return implementations;
	}
}
