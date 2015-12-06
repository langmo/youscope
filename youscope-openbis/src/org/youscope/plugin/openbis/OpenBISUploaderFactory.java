/**
 * 
 */
package org.youscope.plugin.openbis;

import org.youscope.addon.AddonException;
import org.youscope.addon.postprocessing.PostProcessorAddonFactory;
import org.youscope.addon.tool.ToolAddonUI;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * @author Moritz Lang
 *
 */
public class OpenBISUploaderFactory implements PostProcessorAddonFactory
{

	@Override
	public ToolAddonUI createPostProcessorUI(String ID, YouScopeClient client, YouScopeServer server, String measurementFolder) throws AddonException
	{
		if(OpenBISUploader.TYPE_IDENTIFIER.equals(ID))
		{
			return new OpenBISUploader(client, server, measurementFolder);
		}
		return null;
	}

	@Override
	public String[] getSupportedPostProcessorIDs()
	{
		return new String[]{OpenBISUploader.TYPE_IDENTIFIER};
	}

	@Override
	public boolean supportsPostProcessorID(String ID)
	{
		if(OpenBISUploader.TYPE_IDENTIFIER.equals(ID))
			return true;
		return false;
	}

	@Override
	public String getPostProcessorName(String ID)
	{
		if(OpenBISUploader.TYPE_IDENTIFIER.equals(ID))
			return "Upload to OpenBIS";
		return null;
	}

}
