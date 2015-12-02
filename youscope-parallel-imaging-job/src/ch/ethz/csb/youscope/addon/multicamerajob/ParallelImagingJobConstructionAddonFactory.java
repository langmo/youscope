/**
 * 
 */
package ch.ethz.csb.youscope.addon.multicamerajob;

import ch.ethz.csb.youscope.server.addon.job.JobConstructionAddon;
import ch.ethz.csb.youscope.server.addon.job.JobConstructionAddonFactory;
import ch.ethz.csb.youscope.shared.measurement.job.Job;

/**
 * @author langmo
 * 
 */
public class ParallelImagingJobConstructionAddonFactory implements JobConstructionAddonFactory
{

	@Override
	public JobConstructionAddon createJobConstructionAddon(String ID)
	{
		if(supportsConfigurationID(ID))
			return new ParallelImagingJobConstructionAddon();
		return null;
	}

	@Override
	public String[] getSupportedConfigurationIDs()
	{
		String[] supportedTypes = new String[] {ParallelImagingJobConfigurationDTO.TYPE_IDENTIFIER};
		return supportedTypes;
	}

	@Override
	public boolean supportsConfigurationID(String ID)
	{
		for(String supportedType : getSupportedConfigurationIDs())
		{
			if(supportedType.compareToIgnoreCase(ID) == 0)
				return true;
		}
		return false;
	}

	@Override
	public Iterable<Class<? extends Job>> getJobImplementations()
	{
		return null;
	}

}
