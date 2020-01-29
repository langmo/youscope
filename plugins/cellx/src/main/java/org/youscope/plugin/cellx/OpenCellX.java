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

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.youscope.addon.AddonException;
import org.youscope.addon.ConfigurationManagement;
import org.youscope.addon.tool.ToolAddonUIAdapter;
import org.youscope.addon.tool.ToolMetadata;
import org.youscope.addon.tool.ToolMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.measurement.MeasurementConfiguration;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.StandardFormats;

/**
 * @author Moritz Lang
 *
 */
class OpenCellX extends ToolAddonUIAdapter
{
	public static final String TYPE_IDENTIFIER = "YouScope.OpenCellX";
	
	private final JTextField measurementIDField = new JTextField();
	private final JTextField measurementFolderField = new JTextField();
	
	private JButton openCellXButton = new JButton("Open CellX");
	
	private final static GridBagConstraints newLineCnstr = StandardFormats.getNewLineConstraint();

	private final String measurementFolder;
	
	static ToolMetadata getMetadata()
	{
		return new ToolMetadataAdapter(TYPE_IDENTIFIER, "Open with CellX", new String[]{"Cell Detection"},
				"Opens the meeasurement for cell segmentation in CellX.",
				"icons/smiley-mr-green.png");
	}
	
	/**
	 * Constructor.
	 * @param client
	 * @param server
	 * @param measurementFolder
	 * @throws AddonException 
	 */
	OpenCellX(YouScopeClient client, YouScopeServer server, String measurementFolder) throws AddonException
	{
		super(getMetadata(), client, server);
		this.measurementFolder = measurementFolder;	
	}
	@Override
	public java.awt.Component createUI() throws AddonException
	{
		// Initialize fields.
		String measurementName;
		boolean localServer;
		measurementFolderField.setText(measurementFolder);
		try
		{
			MeasurementConfiguration configuration = (MeasurementConfiguration) ConfigurationManagement.loadConfiguration(measurementFolder + File.separator + "configuration.csb");
			measurementName = configuration.getName();
			measurementIDField.setText(measurementName);
			localServer = getClient().isLocalServer();
		}
		catch(Throwable e)
		{
			throw new AddonException("Could not get measurement information.",e);
		}
		
		// Measurement identification
		final GridBagLayout elementsLayout = new GridBagLayout();
		JPanel elementsPanel = new JPanel(elementsLayout);
		if(!localServer)
		{
			JEditorPane descriptionPane = new JEditorPane();
			descriptionPane.setEditable(false);
			descriptionPane.setContentType("text/html");
			descriptionPane.setText("<html><p style=\"font-size:small;margin-top:0px;\"><b>Measurement folder located at YouScope server.</b></p>" +
					"<p style=\"font-size:small;margin-top:4px;margin-bottom:0px\">The requested measurement "+measurementName+" is located at the YouScope server.<br />Please open the respective folder directly on the server side,<br />or using a remote desktop/shell.</p>" +
					"</html>");
			JScrollPane descriptionScrollPane = new JScrollPane(descriptionPane);
			StandardFormats.addGridBagElement(descriptionScrollPane, elementsLayout, newLineCnstr, elementsPanel);
		}
		else
		{
			JEditorPane descriptionPane = new JEditorPane();
			descriptionPane.setEditable(false);
			descriptionPane.setContentType("text/html");
			descriptionPane.setText("<html><p style=\"font-size:small;margin-top:0px;\"><b>CellX Gui is starting.</b></p>" +
					"<p style=\"font-size:small;margin-top:4px;margin-bottom:0px\">CellX is starting automatically.<br />This may take several seconds. Thank you for waiting!<br />You can close this window.</p>" +
					"</html>");
			JScrollPane descriptionScrollPane = new JScrollPane(descriptionPane);
			StandardFormats.addGridBagElement(descriptionScrollPane, elementsLayout, newLineCnstr, elementsPanel);
		}
		StandardFormats.addGridBagElement(new JLabel("Measurement Name:"), elementsLayout, newLineCnstr, elementsPanel);
		measurementIDField.setEditable(false);
		StandardFormats.addGridBagElement(measurementIDField, elementsLayout, newLineCnstr, elementsPanel);
		StandardFormats.addGridBagElement(new JLabel("Measurement Folder:"), elementsLayout, newLineCnstr, elementsPanel);
		measurementFolderField.setEditable(false);
		StandardFormats.addGridBagElement(measurementFolderField, elementsLayout, newLineCnstr, elementsPanel);
		
			
		openCellXButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				openCellX();
			}
		});
		
		// Set frame properties
		if(localServer)
			setTitle("CellX is starting");
		else
			setTitle("Cannot start CellX");
		setResizable(false);
		setMaximizable(false);
		
		// Create content pane
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(elementsPanel, BorderLayout.CENTER);
		if(localServer)
			contentPane.add(openCellXButton, BorderLayout.SOUTH);
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
				openCellX();
			}
		})).start();
		return contentPane;
	}
	
	private void openCellX()
	{
		// Check if all necessary files exist
		File cellxDirectory = new File("cellx");
		if(!cellxDirectory.exists() || !cellxDirectory.isDirectory())
		{
			sendErrorMessage("Directory " + cellxDirectory.getAbsolutePath() + " does not exist. Check the CellX addon installation for consistency.", null);
			return;
		}
		
		File cellxFile = new File(cellxDirectory, "CellXGui.jar");
		if(!cellxFile.exists() || !cellxFile.isFile())
		{
			sendErrorMessage("CellX Gui " + cellxFile.getAbsolutePath() + " does not exist. Check the CellX addon installation for consistency.", null);
			return;
		}
    	try
		{
			Runtime.getRuntime().exec(new String[]{"javaw", "-jar", "CellXGui.jar"}, null, cellxDirectory);
		}
		catch(IOException e)
		{
			sendErrorMessage("Error while starting CellX Gui (" + cellxFile.getAbsolutePath() + ").", e);
			return;
		}
	}
}
