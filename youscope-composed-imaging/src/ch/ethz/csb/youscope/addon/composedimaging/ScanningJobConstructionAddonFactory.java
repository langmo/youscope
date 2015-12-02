/**
 * 
 */
package ch.ethz.csb.youscope.addon.composedimaging;

import java.util.Vector;

import ch.ethz.csb.youscope.server.addon.job.JobConstructionAddon;
import ch.ethz.csb.youscope.server.addon.job.JobConstructionAddonFactory;
import ch.ethz.csb.youscope.shared.measurement.job.Job;

/**
 * @author langmo
 * 
 */
public class ScanningJobConstructionAddonFactory implements JobConstructionAddonFactory
{

	@Override
	public JobConstructionAddon createJobConstructionAddon(String ID)
	{
		if(supportsConfigurationID(ID))
			return new ScanningJobConstructionAddon();
		return null;
	}

	@Override
	public String[] getSupportedConfigurationIDs()
	{
		String[] supportedTypes = new String[] {StaggeringJobConfiguration.TYPE_IDENTIFIER, ComposedImagingJobConfiguration.TYPE_IDENTIFIER, PlateScanningJobConfiguration.TYPE_IDENTIFIER};
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
		// The jobs should not be used by anybody else, since in their configuration one can do a lot of mistakes.
		Vector<Class<? extends Job>> implementations = new Vector<Class<? extends Job>>();
		return implementations;
	}

}
