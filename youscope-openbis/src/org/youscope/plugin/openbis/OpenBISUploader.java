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
package org.youscope.plugin.openbis;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.youscope.addon.AddonException;
import org.youscope.addon.ConfigurationManagement;
import org.youscope.addon.tool.ToolAddonUIAdapter;
import org.youscope.addon.tool.ToolMetadata;
import org.youscope.addon.tool.ToolMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.measurement.MeasurementConfiguration;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.StandardFormats;

/**
 * @author Moritz Lang
 *
 */
class OpenBISUploader extends ToolAddonUIAdapter
{
	public static final String TYPE_IDENTIFIER = "YouScope.OpenBISUploader";
	
	private final String measurementFolder;
	
	private final JTextField measurementIDField = new JTextField();
	private final JButton configButton = new JButton("Configuration");
	private final JButton commitButton = new JButton("Upload to OpenBIS");
	private final JTextField measurementFolderField = new JTextField();
	private final JTextField userIDField = new JTextField();
	private final JTextField projectIDField = new JTextField();
	
	private final static GridBagConstraints newLineCnstr = StandardFormats.getNewLineConstraint();
	private final TransferSettings settings = new TransferSettings();
	
	public static final String OPEN_BIS_USER_PROPERTY = "YouScope.OpenBIS.OpenBISUserID";
	public static final String OPEN_BIS_PROJECT_PROPERTY = "YouScope.OpenBIS.OpenBISProjectID";
	
	static ToolMetadata getMetadata()
	{
		return new ToolMetadataAdapter(TYPE_IDENTIFIER, "Send to OpenBIS", new String[0],
				"Exports the measurement data into the OpenBIS database.", 
				"icons/database-import.png");
	}
	
	OpenBISUploader(YouScopeClient client, YouScopeServer server, String measurementFolder) throws AddonException
	{
		super(getMetadata(), client, server);
		this.measurementFolder = measurementFolder;
	}
	@Override
	public java.awt.Component createUI()
	{
		// Initialize fields.
		try
		{
			MeasurementConfiguration configuration = (MeasurementConfiguration) ConfigurationManagement.loadConfiguration(measurementFolder + File.separator + "configuration.csb");
			measurementIDField.setText(configuration.getName());
		}
		catch(Throwable e)
		{
			sendErrorMessage("Could not load measurement configuration.\nLeaving measurement ID field empty.",e);
		}
		measurementFolderField.setText(measurementFolder);
		
		// User identification
		final GridBagLayout userIdentLayout = new GridBagLayout();
		JPanel userIdentPanel = new JPanel(userIdentLayout);
		StandardFormats.addGridBagElement(new JLabel("OpenBIS User Name:"), userIdentLayout, newLineCnstr, userIdentPanel);
		StandardFormats.addGridBagElement(userIDField, userIdentLayout, newLineCnstr, userIdentPanel);
		userIdentPanel.setBorder(new TitledBorder("User Identification"));
		
		// Upload options
		final GridBagLayout uploadOptionsLayout = new GridBagLayout();
		JPanel uploadOptionsPanel = new JPanel(uploadOptionsLayout);
		StandardFormats.addGridBagElement(new JLabel("Project Name:"), uploadOptionsLayout, newLineCnstr, uploadOptionsPanel);
		StandardFormats.addGridBagElement(projectIDField, uploadOptionsLayout, newLineCnstr, uploadOptionsPanel);
		StandardFormats.addGridBagElement(new JLabel("Measurement Name (unique):"), uploadOptionsLayout, newLineCnstr, uploadOptionsPanel);
		StandardFormats.addGridBagElement(measurementIDField, uploadOptionsLayout, newLineCnstr, uploadOptionsPanel);
		StandardFormats.addGridBagElement(new JLabel("Local Measurement Location:"), uploadOptionsLayout, newLineCnstr, uploadOptionsPanel);
		measurementFolderField.setEditable(false);
		StandardFormats.addGridBagElement(measurementFolderField, uploadOptionsLayout, newLineCnstr, uploadOptionsPanel);
		uploadOptionsPanel.setBorder(new TitledBorder("Upload Settings"));
		
		// Create central layout
		final GridBagLayout elementsLayout = new GridBagLayout();
		final JPanel elementsPanel = new JPanel(elementsLayout);
		StandardFormats.addGridBagElement(userIdentPanel, elementsLayout, newLineCnstr, elementsPanel);
		StandardFormats.addGridBagElement(uploadOptionsPanel, elementsLayout, newLineCnstr, elementsPanel);
		
		
		commitButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				// Read out settings
				settings.measurementFolder = measurementFolderField.getText();
				settings.measurementID = measurementIDField.getText();
				settings.projectID = projectIDField.getText();
				settings.userID = userIDField.getText();
				
				// Save settings
				getClient().getPropertyProvider().setProperty(OPEN_BIS_USER_PROPERTY, settings.userID);
				getClient().getPropertyProvider().setProperty(OPEN_BIS_PROJECT_PROPERTY, settings.projectID);
				
				// Start up transfer and show waitbar frame
				YouScopeFrame childFrame = getContainingFrame().createModalChildFrame();
				@SuppressWarnings("unused")
				TransferStateFrame transferStateFrame = new TransferStateFrame(childFrame, getServer(), getClient(), settings);
				childFrame.setVisible(true);
				closeAddon();
			}
		});
		
		configButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				YouScopeFrame childFrame = getContainingFrame().createModalChildFrame();
				ConnectionConfigurationFrame config = new ConnectionConfigurationFrame(childFrame, getClient());
				config.addConfigurationChangeListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						loadSSHSettings();
					}
				});
				childFrame.setVisible(true);
			}
		});
		
		JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 2, 2));
		buttonPanel.add(configButton);
		buttonPanel.add(commitButton);
		
		// Set frame properties
		setTitle("OpenBIS Uploader");
		setResizable(false);
		setMaximizable(false);
		
		// Load SSH settings
		loadSSHSettings();
		
		// Load last settings
		userIDField.setText(getClient().getPropertyProvider().getProperty(OPEN_BIS_USER_PROPERTY, ""));
		projectIDField.setText(getClient().getPropertyProvider().getProperty(OPEN_BIS_PROJECT_PROPERTY, ""));
		
		// Create content pane
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(elementsPanel, BorderLayout.CENTER);
		contentPane.add(buttonPanel, BorderLayout.SOUTH);

		return contentPane;

	}
	private void loadSSHSettings()
	{
		settings.sshUser = getClient().getPropertyProvider().getProperty(ConnectionConfigurationFrame.SSH_USER_PROPERTY, "");
		settings.sshServer = getClient().getPropertyProvider().getProperty(ConnectionConfigurationFrame.SSH_SERVER_PROPERTY, "");
		settings.sshDirectory = getClient().getPropertyProvider().getProperty(ConnectionConfigurationFrame.SSH_PATH_PROPERTY, "");
		
		if(settings.sshDirectory.length() > 0 && settings.sshUser.length() > 0  && settings.sshServer.length() > 0)
		{
			commitButton.setEnabled(true);
		}
		else
		{
			commitButton.setEnabled(false);
		}
	}
}
