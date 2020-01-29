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
package org.youscope.plugin.labelvisualizer;

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
@XStreamAlias("label-visualizer-configuration")
@YSConfigAlias("Label Visualizer")
@YSConfigClassification("cell visualization")
@YSConfigDescription("This visualizer creates an image in which the areas the cells occupy are painted in different colors, whereas the background is black.\nIf checked, information like the cell area and fluorescence intensities is displayed next to the detected cells.")
public class LabelVisualizerConfiguration extends CellVisualizationConfiguration
{

	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -1111201671110652226L;
	
	/**
	 * Constructor.
	 */
	public LabelVisualizerConfiguration()
	{
		// do nothing.
	}

	@YSConfigAlias("display properties next to cells")
	private boolean drawCellInformation = true;
	
	@YSConfigAlias("draw on top of original image")
	private boolean drawIntoOrgImage = true;
	
	/**
	 * The identifier for this configuration.
	 */
	public static final String	CONFIGURATION_ID	= "YouScope.LabelVisualizer";
	
	@Override
	public String getTypeIdentifier()
	{
		return CONFIGURATION_ID;
	}

	/**
	 * Set to true to draw information about each detected cell next to the cell.
	 * @param drawCellInformation True to draw information, false otherwise.
	 */
	public void setDrawCellInformation(boolean drawCellInformation)
	{
		this.drawCellInformation = drawCellInformation;
	}

	/**
	 * Returns true if information about each detected cell is drawn next to the cell.
	 * @return True if information is drawn.
	 */
	public boolean isDrawCellInformation()
	{
		return drawCellInformation;
	}

	/**
	 * Sets if the borders of the cells should be drawn into the original image (true), or the shape of the cell into a new image (false).
	 * @param drawIntoOrgImage True to draw into original image.
	 */
	public void setDrawIntoOrgImage(boolean drawIntoOrgImage)
	{
		this.drawIntoOrgImage = drawIntoOrgImage;
	}

	/**
	 * Returns if the borders of the cells should be drawn into the original image (true), or the shape of the cell into a new image (false).
	 * @return True to draw into original image.
	 */
	public boolean isDrawIntoOrgImage()
	{
		return drawIntoOrgImage;
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		super.checkConfiguration();
		// do nothing.
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
