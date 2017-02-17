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
package org.youscope.plugin.cellx;

import org.youscope.addon.celldetection.CellDetectionConfiguration;
import org.youscope.common.table.TableDefinition;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.basic.BooleanConverter;

/**
 * @author Moritz Lang
 *
 */
@XStreamAlias("cellx")
public class CellXConfiguration extends CellDetectionConfiguration
{

	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -8155501676160652226L;
	
	/**
	 * Constructor.
	 */
	public CellXConfiguration()
	{
		// do nothing.
	}
		
	@XStreamAlias("generate-detection-image")
	@XStreamAsAttribute
	@XStreamConverter(value = BooleanConverter.class, booleans = {false}, strings = {"yes", "no"})
	private boolean generateDetectionImage = true;
	
	@XStreamAlias("track-cells")
	@XStreamAsAttribute
	@XStreamConverter(value = BooleanConverter.class, booleans = {false}, strings = {"yes", "no"})
	private boolean trackCells = true;
	
	/**
	 * The identifier for this configuration.
	 */
	public static final String	TYPE_IDENTIFIER	= "YouScope.CellX";
	
	@XStreamAlias("configuration-file")
	private String configurationFile = null;
	
	@Override
	public String getTypeIdentifier()
	{
		return TYPE_IDENTIFIER;
	}
	
	/**
	 * Set to true if a label image should be generated.
	 * @param generateDetectionImage True if detection image should be generated, otherwise false.
	 */
	public void setGenerateLabelImage(boolean generateDetectionImage)
	{
		this.generateDetectionImage = generateDetectionImage;
	}

	/**
	 * Returns true if a label image should be generated.
	 * @return True if detection image should be generated, otherwise false.
	 */
	@Override
	public boolean isGenerateLabelImage()
	{
		return generateDetectionImage;
	}

	/**
	 * Sets the path to the file which contains the configuration settings for the CellX algorithm.
	 * @param configurationFile Absolute path to the configuration file (*.xml).
	 */
	public void setConfigurationFile(String configurationFile)
	{
		this.configurationFile = configurationFile;
	}

	/**
	 * Returns the path to the file which contains the configuration settings for the CellX algorithm.
	 * @return Absolute path to the configuration file (*.xml), or null.
	 */
	public String getConfigurationFile()
	{
		return configurationFile;
	}

	@Override
	public TableDefinition getProducedTableDefinition() {
		return CellXTable.getTableDefinition();
	}

	/**
	 * Set to true if cells should not only be segmented, but also be tracked between adjacent frames.
	 * @param trackCells True if cells should be tracked.
	 */
	public void setTrackCells(boolean trackCells)
	{
		this.trackCells = trackCells;
	}

	/**
	 * Returns true if cells should not only be segmented, but also be tracked between adjacent frames.
	 * @return True if cells should be tracked.
	 */
	public boolean isTrackCells()
	{
		return trackCells;
	}
}
