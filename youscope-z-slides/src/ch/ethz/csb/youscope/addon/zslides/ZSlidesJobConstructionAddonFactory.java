/**
 * 
 */
package ch.ethz.csb.youscope.addon.zslides;

import ch.ethz.csb.youscope.server.addon.job.JobConstructionAddon;
import ch.ethz.csb.youscope.server.addon.job.JobConstructionAddonFactory;
import ch.ethz.csb.youscope.shared.measurement.job.Job;

/**
 * @author langmo
 * 
 */
public class ZSlidesJobConstructionAddonFactory implements JobConstructionAddonFactory
{

	@Override
	public JobConstructionAddon createJobConstructionAddon(String ID)
	{
		if(ZSlidesJobConfiguration.TYPE_IDENTIFIER.compareToIgnoreCase(ID) == 0)
			return new ZSlidesJobJobConstructionAddon();
		return null;
	}

	@Override
	public String[] getSupportedConfigurationIDs()
	{
		String[] supportedTypes = new String[] {ZSlidesJobConfiguration.TYPE_IDENTIFIER};
		return supportedTypes;
	}

	@Override
	public boolean supportsConfigurationID(String ID)
	{
		if(ZSlidesJobConfiguration.TYPE_IDENTIFIER.compareToIgnoreCase(ID) == 0)
			return true;
		return false;
	}

	@Override
	public Iterable<Class<? extends Job>> getJobImplementations()
	{
		return null;
	}

}
