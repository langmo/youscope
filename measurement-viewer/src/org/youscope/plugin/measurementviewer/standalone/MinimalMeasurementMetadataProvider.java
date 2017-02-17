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
package org.youscope.plugin.measurementviewer.standalone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.youscope.clientinterfaces.MetadataDefinition;
import org.youscope.clientinterfaces.MetadataDefinitionManager;
import org.youscope.clientinterfaces.YouScopeClientException;

class MinimalMeasurementMetadataProvider implements MetadataDefinitionManager
{

	@Override
	public boolean isAllowCustomMetadata() {
		return false;
	}

	@Override
	public Iterator<MetadataDefinition> iterator() {
		return getMetadataDefinitions().iterator();
	}

	@Override
	public Collection<MetadataDefinition> getMetadataDefinitions() {
		return new ArrayList<MetadataDefinition>(0);
	}

	@Override
	public Collection<MetadataDefinition> getMandatoryMetadataDefinitions() {
		return new ArrayList<MetadataDefinition>(0);
	}

	@Override
	public MetadataDefinition getMetadataDefinition(String name) {
		return null;
	}

	@Override
	public void setMetadataDefinition(MetadataDefinition property) throws YouScopeClientException
	{
		throw new YouScopeClientException("Operation not supported.");
	}

	@Override
	public boolean deleteMetadataDefinition(String name) 
	{
		return false;
	}

	@Override
	public Collection<MetadataDefinition> getDefaultMetadataDefinitions() {
		return new ArrayList<MetadataDefinition>(0);
	}

	@Override
	public int getNumMetadataDefinitions() {
		return 0;
	}

}
