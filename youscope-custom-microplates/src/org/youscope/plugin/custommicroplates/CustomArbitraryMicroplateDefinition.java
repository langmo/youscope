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
package org.youscope.plugin.custommicroplates;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.youscope.common.Well;
import org.youscope.common.configuration.Configuration;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.microplate.MicroplateLayout;
import org.youscope.common.microplate.WellLayout;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("custom-arbitrary-microplate")
class CustomArbitraryMicroplateDefinition implements Serializable, Cloneable, MicroplateLayout, CustomMicroplateDefinition, Configuration
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -5358378772356674069L;
	@Override
	protected Object clone()
	{
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("Clone not supported.", e);
		}
	}
	@XStreamAlias("well-layouts")
	private final ArrayList<WellLayout> wellLayouts = new ArrayList<>();
	@XStreamAlias("microplate-name")
	private String customMicroplateName = "unnamed";
	public List<WellLayout> getWellLayouts()
	{
		return new ArrayList<WellLayout>(wellLayouts);
	}
	
	public void setWellLayouts(List<WellLayout> wellLayouts)
	{
		this.wellLayouts.clear();
		this.wellLayouts.addAll(wellLayouts);
	}

	@Override
	public void setCustomMicroplateName(String customMicroplateName)
	{
		this.customMicroplateName = customMicroplateName;
	}

	@Override
	public String getCustomMicroplateName()
	{
		return customMicroplateName;
	}

	@Override
	public Iterator<WellLayout> iterator() {
		return new Iterator<WellLayout>()
				{
					int currentPos = 0;
					@Override
					public boolean hasNext() {
						return currentPos < wellLayouts.size();
					}

					@Override
					public WellLayout next() {
						return wellLayouts.get(currentPos++);
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
			
				};
	}

	@Override
	public int getNumWells() {
		return wellLayouts.size();
	}

	@Override
	public WellLayout getWell(int index) 
	{
		if(index < 0 || index>=wellLayouts.size())
			return null;
		return wellLayouts.get(index);
	}

	@Override
	public WellLayout getWell(Well well) 
	{
		for(WellLayout wellLayout : wellLayouts)
		{
			if(wellLayout.getWell().equals(well))
				return wellLayout;
		}
		return null;
	}

	@Override
	public String getTypeIdentifier() 
	{
		return CustomMicroplateManager.getCustomMicroplateTypeIdentifier(customMicroplateName);
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		// do nothing.
	}
}
