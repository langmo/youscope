/**
 * 
 */
package ch.ethz.csb.youscope.addon.livemodifiablejob;

import java.util.Vector;

import ch.ethz.csb.youscope.server.addon.job.JobConstructionAddon;
import ch.ethz.csb.youscope.server.addon.job.JobConstructionAddonFactory;
import ch.ethz.csb.youscope.shared.measurement.job.Job;

/**
 * @author langmo
 */
public class LiveModifiableJobConstructionAddonFactory implements JobConstructionAddonFactory
{

    @Override
    public JobConstructionAddon createJobConstructionAddon(String ID)
    {
        if (supportsConfigurationID(ID))
            return new LiveModifiableJobConstructionAddon();
		return null;
    }

    @Override
    public String[] getSupportedConfigurationIDs()
    {
        String[] supportedTypes = new String[] { LiveModifiableJobConfigurationDTO.TYPE_IDENTIFIER };
        return supportedTypes;
    }

    @Override
    public boolean supportsConfigurationID(String ID)
    {
        for (String supportedType : getSupportedConfigurationIDs())
        {
            if (supportedType.compareToIgnoreCase(ID) == 0)
                return true;
        }
        return false;
    }

    @Override
    public Iterable<Class<? extends Job>> getJobImplementations()
    {
        Vector<Class<? extends Job>> implementations = new Vector<Class<? extends Job>>();
        implementations.addElement(LiveModifiableJobImpl.class);
        return implementations;
    }
}
