/**
 * 
 */
package ch.ethz.csb.youscope.addon.customjob;

import java.util.Vector;

import ch.ethz.csb.youscope.server.addon.job.JobConstructionAddon;
import ch.ethz.csb.youscope.server.addon.job.JobConstructionAddonFactory;
import ch.ethz.csb.youscope.shared.measurement.job.Job;

/**
 * @author langmo
 * 
 */
public class CustomJobConstructionAddonFactory implements JobConstructionAddonFactory
{

	@Override
	public JobConstructionAddon createJobConstructionAddon(String ID)
	{
		if(supportsConfigurationID(ID))
			return new CustomJobConstructionAddon();
		return null;
	}

	@Override
	public String[] getSupportedConfigurationIDs()
	{
		CustomJobConfigurationDTO[] jobs;
		try
		{
			jobs = CustomJobManager.loadCustomJobs();
		}
		catch(@SuppressWarnings("unused") CustomJobException e)
		{
			return new String[0];
		}
		String[] supportedTypes = new String[jobs.length];
		for(int i=0; i<jobs.length; i++)
		{
			supportedTypes[i] = jobs[i].getTypeIdentifier();
		}
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
		Vector<Class<? extends Job>> implementations = new Vector<Class<? extends Job>>();
		implementations.addElement(CustomJobImpl.class);
		return implementations;
	}
}
