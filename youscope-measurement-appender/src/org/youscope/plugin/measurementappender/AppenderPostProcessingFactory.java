/**
 * 
 */
package org.youscope.plugin.measurementappender;

import org.youscope.addon.AddonException;
import org.youscope.addon.AddonMetadata;
import org.youscope.addon.postprocessing.PostProcessorAddonFactory;
import org.youscope.addon.tool.ToolAddonUI;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.saving.MeasurementFileLocations;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * @author Moritz Lang
 *
 */
public class AppenderPostProcessingFactory implements PostProcessorAddonFactory
{

	@Override
	public ToolAddonUI createPostProcessorUI(String ID, YouScopeClient client, YouScopeServer server, MeasurementFileLocations measurementFileLocations) throws AddonException
	{
		if(AppenderTool.TYPE_IDENTIFIER.equals(ID))
		{
			return new AppenderTool(client, server, measurementFileLocations);
		}
		throw new AddonException("Type identifer "+ID+" not supported by this factory.");
	}

	@Override
	public String[] getSupportedTypeIdentifiers()
	{
		return new String[]{AppenderTool.TYPE_IDENTIFIER};		
	}

	@Override
	public boolean isSupportingTypeIdentifier(String ID)
	{
		if(AppenderTool.TYPE_IDENTIFIER.equals(ID))
			return true;
		return false;
	}

	@Override
	public AddonMetadata getPostProcessorMetadata(String typeIdentifier) throws AddonException {
		if(AppenderTool.TYPE_IDENTIFIER.equals(typeIdentifier))
			return AppenderTool.getMetadata();
		throw new AddonException("Type identifer "+typeIdentifier+" not supported by this factory.");
	}

}
