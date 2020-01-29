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
import java.io.File;
import java.io.IOException;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

import org.youscope.addon.AddonException;
import org.youscope.addon.tool.ToolAddonUIAdapter;
import org.youscope.addon.tool.ToolMetadata;
import org.youscope.addon.tool.ToolMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * @author Moritz Lang
 *
 */
class OpenMeasurementFolder extends ToolAddonUIAdapter
{
	public static final String TYPE_IDENTIFIER = "YouScope.OpenMeasurementFolder";
	
	private final String measurementFolder;
	
	static ToolMetadata getMetadata()
	{
		return new ToolMetadataAdapter(TYPE_IDENTIFIER, "Open Folder", new String[0],
				"Opens the folder containing the measurement data.",
				"icons/folder-open-image.png");
	}
	
	/**
	 * Constructor.
	 * @param client
	 * @param server
	 * @param measurementFolder
	 * @throws AddonException 
	 */
	OpenMeasurementFolder(YouScopeClient client, YouScopeServer server, String measurementFolder) throws AddonException
	{
		super(getMetadata(), client, server);
		this.measurementFolder = measurementFolder;
	}
	@Override
	public java.awt.Component createUI() throws AddonException
	{
		setTitle("Folder is opening");
		setResizable(false);
		setMaximizable(false);
		
		JEditorPane descriptionPane = new JEditorPane();
		descriptionPane.setEditable(false);
		descriptionPane.setContentType("text/html");
		descriptionPane.setText("<html><p style=\"font-size:small;margin-top:0px;\"><b>Folder<br />"+measurementFolder+"<br />is opening.</b></p>" +
				"<p style=\"font-size:small;margin-top:4px;margin-bottom:0px\">The requested folder should open automatically.<br />If it is not opening, manually surf to the respective path.<br />You can close this window.</p>" +
				"</html>");
		JScrollPane descriptionScrollPane = new JScrollPane(descriptionPane);
		
		this.setShowCloseButton(true);
		this.setCloseButtonLabel("Close");

		(new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				// open folder with a minimal delay.
				try
				{
					Thread.sleep(100);
				}
				catch(@SuppressWarnings("unused") InterruptedException e)
				{
					// do nothing.
				}
				openFolder();
			}
		})).start();
		
		return descriptionScrollPane;
	}
	
	private void openFolder()
	{
		try
		{
			Desktop.getDesktop().open(new File(measurementFolder));
			closeAddon();
		}
		catch(IOException e1)
		{
			sendErrorMessage("Could not open folder " + measurementFolder + ".", e1);
		}
	}
}
