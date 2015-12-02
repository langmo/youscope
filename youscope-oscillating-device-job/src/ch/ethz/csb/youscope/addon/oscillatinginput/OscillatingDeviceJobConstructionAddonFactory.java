/**
 * 
 */
package ch.ethz.csb.youscope.addon.oscillatinginput;

import ch.ethz.csb.youscope.server.addon.job.JobConstructionAddon;
import ch.ethz.csb.youscope.server.addon.job.JobConstructionAddonFactory;
import ch.ethz.csb.youscope.shared.measurement.job.Job;

/**
 * @author langmo
 * 
 */
public class OscillatingDeviceJobConstructionAddonFactory implements JobConstructionAddonFactory
{

	@Override
	public JobConstructionAddon createJobConstructionAddon(String ID)
	{
		if(OscillatingDeviceJobConfigurationDTO.TYPE_IDENTIFIER.compareToIgnoreCase(ID) == 0)
			return new OscillatingDeviceJobConstructionAddon();
		return null;
	}

	@Override
	public String[] getSupportedConfigurationIDs()
	{
		String[] supportedTypes = new String[] {OscillatingDeviceJobConfigurationDTO.TYPE_IDENTIFIER};
		return supportedTypes;
	}

	@Override
	public boolean supportsConfigurationID(String ID)
	{
		if(OscillatingDeviceJobConfigurationDTO.TYPE_IDENTIFIER.compareToIgnoreCase(ID) == 0)
			return true;
		return false;
	}

	@Override
	public Iterable<Class<? extends Job>> getJobImplementations()
	{
		// An oscillating device job is just a set of other jobs and does not have an own interface.
		return null;
	}

}
