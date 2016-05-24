/**
 * 
 */
package org.youscope.plugin.measurementviewer;

import org.youscope.addon.tool.ToolAddonFactoryAdapter;

/**
 * @author Moritz Lang
 *
 */
public class MeasurementViewerFactory extends ToolAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */ 
	public MeasurementViewerFactory()
	{
		super(MeasurementViewer.class, MeasurementViewer.getMetadata());
	}
}
