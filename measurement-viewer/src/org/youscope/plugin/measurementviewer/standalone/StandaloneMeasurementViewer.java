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

import javax.imageio.ImageIO;
import javax.swing.UIManager;

import org.youscope.addon.AddonException;
import org.youscope.addon.tool.ToolAddonUI;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.clientinterfaces.YouScopeFrameListener;
import org.youscope.plugin.measurementviewer.MeasurementViewerFactory;

/**
 * Standalone implementation of the measurement viewer.
 * @author mlang
 *
 */
public class StandaloneMeasurementViewer
{

	/**
	 * Main method of standalone viewer.
	 * @param args
	 */
	public static void main(String[] args)
	{
		// Set system look and feel.
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(@SuppressWarnings("unused") Exception e)
		{
			// Don't care, take standard L&F...
		}
		
		ImageIO.scanForPlugins();
		MeasurementViewerFactory factory = new MeasurementViewerFactory();
		final MinimalClientImpl client = new MinimalClientImpl();
		ToolAddonUI ui;
		YouScopeFrame frame;
		try {
			ui = factory.createToolUI("YouScope.YouScopeMeasurementViewer", client, null);
			frame = ui.toFrame();
			 
		} catch (AddonException e) {
			e.printStackTrace();
			System.exit(1);
			return;
		}
		frame.setVisible(true);
		frame.addFrameListener(new YouScopeFrameListener() {
			
			@Override
			public void frameOpened() {
				// do nothing.
				
			}
			
			@Override
			public void frameClosed() {
				System.out.println("Closing Measurement Viewer...");
				client.close();
				System.exit(0);
			}
		});
	}

}
