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
package org.youscope.plugin.openmeasurementfolder;

import java.awt.Desktop;

import org.youscope.addon.AddonException;
import org.youscope.addon.AddonMetadata;
import org.youscope.addon.AddonUI;
import org.youscope.addon.postprocessing.PostProcessorAddonFactory;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.saving.MeasurementFileLocations;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * @author Moritz Lang
 *
 */
public class OpenMeasurementFolderFactory implements PostProcessorAddonFactory
{

	@Override
	public AddonUI<? extends AddonMetadata> createPostProcessorUI(String typeIdentifier, YouScopeClient client,
			YouScopeServer server, MeasurementFileLocations measurementFileLocations) throws AddonException {
		if(OpenMeasurementFolder.TYPE_IDENTIFIER.equals(typeIdentifier))
		{
			return new OpenMeasurementFolder(client, server, measurementFileLocations.getMeasurementBaseFolder());
		}
		throw new AddonException("Type identifer "+typeIdentifier+" not supported by this factory.");
	}

	@Override
	public String[] getSupportedTypeIdentifiers()
	{
		if(Desktop.isDesktopSupported())
			return new String[]{OpenMeasurementFolder.TYPE_IDENTIFIER};
		return new String[0];
	}

	@Override
	public boolean isSupportingTypeIdentifier(String ID)
	{
		if(OpenMeasurementFolder.TYPE_IDENTIFIER.equals(ID))
			return true;
		return false;
	}

	@Override
	public AddonMetadata getPostProcessorMetadata(String typeIdentifier) throws AddonException {
		if(OpenMeasurementFolder.TYPE_IDENTIFIER.equals(typeIdentifier))
			return OpenMeasurementFolder.getMetadata();
		throw new AddonException("Type identifer "+typeIdentifier+" not supported by this factory.");
	}
}
