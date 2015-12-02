/**
 * 
 */
package ch.ethz.csb.youscope.addon.repeatjob;

import java.util.Vector;

import ch.ethz.csb.youscope.server.addon.job.JobConstructionAddon;
import ch.ethz.csb.youscope.server.addon.job.JobConstructionAddonFactory;
import ch.ethz.csb.youscope.shared.measurement.job.Job;

/**
 * Factory to create repeat jobs.
 * @author Moritz Lang
 * 
 */
public class RepeatJobConstructionAddonFactory implements JobConstructionAddonFactory
{

	@Override
	public JobConstructionAddon createJobConstructionAddon(String ID)
	{
		if(supportsConfigurationID(ID))
			return new RepeatJobConstructionAddon();
		return null;
	}

	@Override
	public String[] getSupportedConfigurationIDs()
	{
		String[] supportedTypes = new String[] {RepeatJobConfigurationDTO.TYPE_IDENTIFIER};
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
		implementations.addElement(RepeatJobImpl.class);
		return implementations;
	}
}
