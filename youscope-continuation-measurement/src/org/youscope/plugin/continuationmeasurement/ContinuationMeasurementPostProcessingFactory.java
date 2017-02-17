/*******************************************************************************
 * Copyright (c) 2017 Moritz Lang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Moritz Lang - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package org.youscope.plugin.continuationmeasurement;

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
public class ContinuationMeasurementPostProcessingFactory implements PostProcessorAddonFactory
{

	@Override
	public ToolAddonUI createPostProcessorUI(String ID, YouScopeClient client, YouScopeServer server, MeasurementFileLocations measurementFileLocations) throws AddonException
	{
		if(ContinuationMeasurementPostProcessing.TYPE_IDENTIFIER.equals(ID))
		{
			return new ContinuationMeasurementPostProcessing(client, server, measurementFileLocations);
		}
		throw new AddonException("Type identifer "+ID+" not supported by this factory.");
	}

	@Override
	public String[] getSupportedTypeIdentifiers()
	{
		return new String[]{ContinuationMeasurementPostProcessing.TYPE_IDENTIFIER};		
	}

	@Override
	public boolean isSupportingTypeIdentifier(String ID)
	{
		if(ContinuationMeasurementPostProcessing.TYPE_IDENTIFIER.equals(ID))
			return true;
		return false;
	}

	@Override
	public AddonMetadata getPostProcessorMetadata(String typeIdentifier) throws AddonException {
		if(ContinuationMeasurementPostProcessing.TYPE_IDENTIFIER.equals(typeIdentifier))
			return ContinuationMeasurementPostProcessing.getMetadata();
		throw new AddonException("Type identifer "+typeIdentifier+" not supported by this factory.");
	}

}
