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
package org.youscope.plugin.showmeasurementinformation;

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
 * 
 * @author Moritz Lang
 *
 */
class ShowMeasurementInformation extends ToolAddonUIAdapter
{
	public static final String TYPE_IDENTIFIER = "YouScope.ShowMeasurementInformation";
	
	private final String measurementInformation;
	
	static ToolMetadata getMetadata()
	{
		return new ToolMetadataAdapter(TYPE_IDENTIFIER, "Show Information", new String[0],
				"Opens the information html file for the measurement.",
				"icons/folder-open-image.png");
	}
	
	/**
	 * Constructor.
	 * @param client
	 * @param server
	 * @param measurementInformation
	 * @throws AddonException 
	 */
	ShowMeasurementInformation(YouScopeClient client, YouScopeServer server, String measurementInformation) throws AddonException
	{
		super(getMetadata(), client, server);
		this.measurementInformation = measurementInformation;
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
		descriptionPane.setText("<html><p style=\"font-size:small;margin-top:0px;\"><b>File<br />"+measurementInformation+"<br />is opening.</b></p>" +
				"<p style=\"font-size:small;margin-top:4px;margin-bottom:0px\">The requested file should open automatically.<br />If it is not opening, manually surf to the respective path.<br />You can close this window.</p>" +
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
			Process p = Runtime.getRuntime().exec("cmd.exe /c start iexplore \""+measurementInformation+"\"");
			closeAddon();
		}
		catch(IOException e1)
		{
			sendErrorMessage("Could not open " + measurementInformation + ".", e1);
		}
	}
}
