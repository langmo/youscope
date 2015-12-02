/**
 * 
 */
package ch.ethz.csb.youscope.addon.livemodifiablejob;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.csb.youscope.shared.addon.XMLConfigurationProvider;
import ch.ethz.csb.youscope.shared.configuration.Configuration;

/**
 * @author Moritz Lang
 */
public class LiveModifiableJobXMLConfigurationProvider implements XMLConfigurationProvider
{

    @Override
    public List<Class<? extends Configuration>> getConfigurationClasses()
    {
        List<Class<? extends Configuration>> classes = new ArrayList<Class<? extends Configuration>>();
        classes.add(LiveModifiableJobConfigurationDTO.class);
        return classes;
    }

}
