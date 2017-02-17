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
package org.youscope.plugin.glowvisualizer;

import org.youscope.addon.celldetection.CellVisualizationConfiguration;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.configuration.YSConfigAlias;
import org.youscope.common.configuration.YSConfigClassification;
import org.youscope.common.configuration.YSConfigDescription;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author Moritz Lang
 *
 */
@YSConfigAlias("Glow Visualizer")
@YSConfigClassification("cell visualization")
@YSConfigDescription("This visualizer displays the original image, and lets the detected cells glow.\nThe glow intensity defines how strong the cells glow: as stronger they glow, as better are they visible by eye. However, the background becomes less and less visible.")
@XStreamAlias("glow-visualizer-configuration")
public class GlowVisualizerConfiguration extends CellVisualizationConfiguration
{

	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -1111201676160652226L;
	
	/**
	 * Constructor.
	 */
	public GlowVisualizerConfiguration()
	{
		// do nothing.
	}

	@YSConfigAlias("Glow strength")
	@XStreamAlias("glow-strength")
	private double glowStrength = 0.5;
	
	/**
	 * The identifier for this configuration.
	 */
	public static final String	CONFIGURATION_ID	= "YouScope.GlowVisualizer";
	
	@Override
	public String getTypeIdentifier()
	{
		return CONFIGURATION_ID;
	}

	/**
	 * Sets the strength in which the cells should be glowing.
	 * 0 means no glowing at all (returns original image), one means only glowing (original image not visible anymore).
	 * @param glowStrength
	 */
	public void setGlowStrength(double glowStrength)
	{
		if(glowStrength < 0)
			glowStrength = 0;
		if(glowStrength > 1)
			glowStrength = 1;
		this.glowStrength = glowStrength;
	}

	/**
	 * Returns the strength in which the cells should be glowing.
	 * 0 means no glowing at all (returns original image), one means only glowing (original image not visible anymore).
	 * @return the glow strength.
	 */
	public double getGlowStrength()
	{
		return glowStrength;
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		super.checkConfiguration();
		if(glowStrength < 0 || glowStrength > 1)
			throw new ConfigurationException("Glow strength must be between zero and one.");
		
	}

	@Override
	public String[] getImageSaveNames() {
		return new String[0];
	}

	@Override
	public int getNumberOfImages() {
		return 1;
	}
}
