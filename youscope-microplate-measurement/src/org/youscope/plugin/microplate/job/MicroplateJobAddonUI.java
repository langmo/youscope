/**
 * 
 */
package org.youscope.plugin.microplate.job;



import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.job.Job;
import org.youscope.serverinterfaces.YouScopeServer;


/**
 * @author Moritz Lang
 *
 */
class MicroplateJobAddonUI extends JobAddonUIAdapter<MicroplateJobConfiguration>
{
	/**
	 * Constructor.
	 * @param server YouScope server.
	 * @param YouScope client.
	 * @throws AddonException 
	 */
	MicroplateJobAddonUI(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(), client, server);
		
		setTitle("Microplate Job");
		addPage(new MicroplatePage(client));
		addPage(new WellSelectionPage(client, server));  
		addPage(new PathPage(client, server));
		addPage(new ImagingProtocolPage(client, server));
	}
	static ComponentMetadataAdapter<MicroplateJobConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<MicroplateJobConfiguration>(MicroplateJobConfiguration.TYPE_IDENTIFIER, 
				MicroplateJobConfiguration.class, 
				Job.class, 
				"Microplate", 
				new String[]{"containers"},
				"icons/map.png");
	}
}
