/**
 * 
 */
package ch.ethz.csb.youscope.addon.outoffocus;

import java.util.ArrayList;

import ch.ethz.csb.youscope.server.addon.job.JobConstructionAddon;
import ch.ethz.csb.youscope.server.addon.job.JobConstructionAddonFactory;
import ch.ethz.csb.youscope.shared.measurement.job.Job;

/**
 * @author langmo
 * 
 */
public class OutOfFocusJobConstructionAddonFactory implements JobConstructionAddonFactory
{

	@Override
	public JobConstructionAddon createJobConstructionAddon(String ID)
	{
		if(OutOfFocusJobConfigurationDTO.TYPE_IDENTIFIER.compareToIgnoreCase(ID) == 0)
			return new OutOfFocusJobConstructionAddon();
		return null;
	}

	@Override
	public String[] getSupportedConfigurationIDs()
	{
		String[] supportedTypes = new String[] {OutOfFocusJobConfigurationDTO.TYPE_IDENTIFIER};
		return supportedTypes;
	}

	@Override
	public boolean supportsConfigurationID(String ID)
	{
		if(OutOfFocusJobConfigurationDTO.TYPE_IDENTIFIER.compareToIgnoreCase(ID) == 0)
			return true;
		return false;
	}

	@Override
	public Iterable<Class<? extends Job>> getJobImplementations()
	{
		ArrayList<Class<? extends Job>> classes = new ArrayList<Class<? extends Job>>();
		return classes;
	}

}
