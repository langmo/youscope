/**
 * 
 */
package ch.ethz.csb.youscope.addon.openbis;

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

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.YouScopeFrame;
import ch.ethz.csb.youscope.client.addon.postprocessing.MeasurementPostProcessorAddon;
import ch.ethz.csb.youscope.client.uielements.StandardFormats;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.configuration.MeasurementConfiguration;
import ch.ethz.csb.youscope.shared.tools.ConfigurationManagement;

/**
 * @author Moritz Lang
 *
 */
class OpenBISUploader implements MeasurementPostProcessorAddon
{
	public static final String ADDON_ID = "CSB::OpenBISUploader::1.0";
	
	private final YouScopeClient client;
	private final YouScopeServer server;
	private final String measurementFolder;
	private YouScopeFrame frame = null;
	
	private final JTextField measurementIDField = new JTextField();
	private final JButton configButton = new JButton("Configuration");
	private final JButton commitButton = new JButton("Upload to OpenBIS");
	private final JTextField measurementFolderField = new JTextField();
	private final JTextField userIDField = new JTextField();
	private final JTextField projectIDField = new JTextField();
	
	private final static GridBagConstraints newLineCnstr = StandardFormats.getNewLineConstraint();
	private final TransferSettings settings = new TransferSettings();
	
	public static final String OPEN_BIS_USER_PROPERTY = "CSB_CISD::OpenBIS::OpenBISUserID";
	public static final String OPEN_BIS_PROJECT_PROPERTY = "CSB_CISD::OpenBIS::OpenBISProjectID";
	
	OpenBISUploader(YouScopeClient client, YouScopeServer server, String measurementFolder)
	{
		this.client = client;
		this.server = server;
		this.measurementFolder = measurementFolder;
	}
	@Override
	public void createUI(YouScopeFrame frame)
	{
		this.frame = frame;
		
		// Initialize fields.
		try
		{
			MeasurementConfiguration configuration = ConfigurationManagement.loadConfiguration(measurementFolder + File.separator + "configuration.csb");
			measurementIDField.setText(configuration.getName());
		}
		catch(Exception e)
		{
			client.sendError("Could not load measurement configuration.\nLeaving measurement ID field empty.",e);
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
				if(OpenBISUploader.this.frame == null)
					return;
				
				// Read out settings
				settings.measurementFolder = measurementFolderField.getText();
				settings.measurementID = measurementIDField.getText();
				settings.projectID = projectIDField.getText();
				settings.userID = userIDField.getText();
				
				// Save settings
				client.getProperties().setProperty(OPEN_BIS_USER_PROPERTY, settings.userID);
				client.getProperties().setProperty(OPEN_BIS_PROJECT_PROPERTY, settings.projectID);
				
				// Start up transfer and show waitbar frame
				YouScopeFrame childFrame = OpenBISUploader.this.frame.createModalChildFrame();
				@SuppressWarnings("unused")
				TransferStateFrame transferStateFrame = new TransferStateFrame(childFrame, server, client, settings);
				childFrame.setVisible(true);
			}
		});
		
		configButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(OpenBISUploader.this.frame == null)
					return;
				
				YouScopeFrame childFrame = OpenBISUploader.this.frame.createModalChildFrame();
				ConnectionConfigurationFrame config = new ConnectionConfigurationFrame(childFrame, client);
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
		frame.setTitle("OpenBIS Uploader");
		frame.setResizable(false);
		frame.setClosable(true);
		frame.setMaximizable(false);
		
		// Load SSH settings
		loadSSHSettings();
		
		// Load last settings
		userIDField.setText(client.getProperties().getProperty(OPEN_BIS_USER_PROPERTY, ""));
		projectIDField.setText(client.getProperties().getProperty(OPEN_BIS_PROJECT_PROPERTY, ""));
		
		// Create content pane
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(elementsPanel, BorderLayout.CENTER);
		contentPane.add(buttonPanel, BorderLayout.SOUTH);
		frame.setContentPane(contentPane);
		frame.pack();

	}
	private void loadSSHSettings()
	{
		settings.sshUser = client.getProperties().getProperty(ConnectionConfigurationFrame.SSH_USER_PROPERTY, "");
		settings.sshServer = client.getProperties().getProperty(ConnectionConfigurationFrame.SSH_SERVER_PROPERTY, "");
		settings.sshDirectory = client.getProperties().getProperty(ConnectionConfigurationFrame.SSH_PATH_PROPERTY, "");
		
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
