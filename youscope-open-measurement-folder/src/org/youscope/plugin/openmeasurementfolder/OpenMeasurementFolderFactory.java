/**
 * 
 */
package org.youscope.plugin.openmeasurementfolder;

import java.awt.Desktop;

import org.youscope.addon.AddonException;
import org.youscope.addon.postprocessing.PostProcessorAddonFactory;
import org.youscope.addon.tool.ToolAddonUI;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * @author Moritz Lang
 *
 */
public class OpenMeasurementFolderFactory implements PostProcessorAddonFactory
{

	@Override
	public ToolAddonUI createPostProcessorUI(String ID, YouScopeClient client, YouScopeServer server, String measurementFolder) throws AddonException
	{
		if(OpenMeasurementFolder.TYPE_IDENTIFIER.equals(ID))
		{
			return new OpenMeasurementFolder(client, server, measurementFolder);
		}
		return null;
	}

	@Override
	public String[] getSupportedPostProcessorIDs()
	{
		if(Desktop.isDesktopSupported())
			return new String[]{OpenMeasurementFolder.TYPE_IDENTIFIER};
		return new String[0];
	}

	@Override
	public boolean supportsPostProcessorID(String ID)
	{
		if(OpenMeasurementFolder.TYPE_IDENTIFIER.equals(ID))
			return true;
		return false;
	}

	@Override
	public String getPostProcessorName(String ID)
	{
		if(OpenMeasurementFolder.TYPE_IDENTIFIER.equals(ID))
			return "Open Folder";
		return null;
	}

}
