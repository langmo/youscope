package org.youscope.plugin.usercontrolmeasurement;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.youscope.addon.measurement.MeasurementAddonUIPage;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.clientinterfaces.YouScopeProperties;
import org.youscope.common.configuration.ImageFolderStructure;
import org.youscope.common.measurement.MeasurementSaveSettings;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.FileNameComboBox;
import org.youscope.uielements.StandardFormats;

class GeneralSettingsPage extends MeasurementAddonUIPage<UserControlMeasurementConfiguration>
{

	/**
	 * Serial Verision UID.
	 */
	private static final long				serialVersionUID		= 885352612109223078L;

	private final YouScopeClient	client;
	private final YouScopeServer			server;

	private JTextField						nameField				= new JTextField("unnamed");

	private JTextField						folderField				= new JTextField();

	private JComboBox<String>						imageTypeField;

	private JComboBox<ImageFolderStructure>						imageFolderTypeField	= new JComboBox<ImageFolderStructure>(ImageFolderStructure.values());

	private FileNameComboBox						imageFileField			= new FileNameComboBox(FileNameComboBox.Type.FILE_NAME);
	
	GeneralSettingsPage(YouScopeClient client, YouScopeServer server)
	{
		this.server = server;
		this.client = client;
	}

	@Override
	public void loadData(UserControlMeasurementConfiguration configuration)
	{
		nameField.setText(configuration.getName());
		
		MeasurementSaveSettings saveSettings = configuration.getSaveSettings();
		if(saveSettings != null)
		{
			folderField.setText(saveSettings.getFolder());
			imageFolderTypeField.setSelectedItem(saveSettings.getImageFolderStructure());
			imageFileField.setSelectedItem(saveSettings.getImageFileName());
			imageTypeField.setSelectedItem(saveSettings.getImageFileType());
		}	
	}

	@Override
	public boolean saveData(UserControlMeasurementConfiguration configuration)
	{
		
		configuration.setName(nameField.getText());
		
		MeasurementSaveSettings saveSettings = new MeasurementSaveSettings();
		saveSettings.setFolder(folderField.getText());
		saveSettings.setImageFileType((String) imageTypeField.getSelectedItem());
		saveSettings.setImageFolderStructure((ImageFolderStructure) imageFolderTypeField.getSelectedItem());
		saveSettings.setImageFileName(imageFileField.getSelectedItem().toString());
		configuration.setSaveSettings(saveSettings);
			
		// Save some of the configurations for next time.
		client.getProperties().setProperty(YouScopeProperties.PROPERTY_LAST_MEASUREMENT_SAVE_FOLDER, saveSettings.getFolder());

		return true;
	}

	@Override
	public void setToDefault(UserControlMeasurementConfiguration configuration)
	{
		MeasurementSaveSettings saveSettings = new MeasurementSaveSettings();
		saveSettings.setFolder(client.getProperties().getProperty(YouScopeProperties.PROPERTY_LAST_MEASUREMENT_SAVE_FOLDER, ""));
		saveSettings.setImageFileName(FileNameComboBox.PRE_DEFINED_FILE_NAMES[0][0]);
		configuration.setSaveSettings(saveSettings);
	}

	@Override
	public String getPageName()
	{
		return "Measurement Properties";
	}

	@Override
	public void createUI(YouScopeFrame frame)
	{
		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);

		GridBagConstraints newLineConstr = StandardFormats.getNewLineConstraint();
		GridBagConstraints bottomConstr = StandardFormats.getBottomContstraint();
		
		// Get supported image types
		String[] imageTypes;
		try
		{
			imageTypes = server.getConfiguration().getSupportedImageFormats();
		}
		catch(RemoteException e1)
		{
			client.sendError("Could not obtain supported image file types from server.", e1);
			imageTypes = new String[0];
		}
		imageTypeField = new JComboBox<String>(imageTypes);
		
		StandardFormats.addGridBagElement(new JLabel("Name:"), layout, newLineConstr, this);
		StandardFormats.addGridBagElement(nameField, layout, newLineConstr, this);

		// Panel to choose files
		StandardFormats.addGridBagElement(new JLabel("Output Directory:"), layout, newLineConstr, this);
		JPanel folderPanel = new JPanel(new BorderLayout(5, 0));
		folderPanel.add(folderField, BorderLayout.CENTER);

		if(client.isLocalServer())
		{
			JButton openFolderChooser = new JButton("Edit");
			openFolderChooser.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					JFileChooser fileChooser = new JFileChooser(folderField.getText());
					fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					int returnVal = fileChooser.showDialog(null, "Open");
					if(returnVal == JFileChooser.APPROVE_OPTION)
					{
						folderField.setText(fileChooser.getSelectedFile().getAbsolutePath());
					}
				}
			});
			folderPanel.add(openFolderChooser, BorderLayout.EAST);
		}
		StandardFormats.addGridBagElement(folderPanel, layout, newLineConstr, this);

		StandardFormats.addGridBagElement(new JLabel("Image folder structure:"), layout, newLineConstr, this);
		StandardFormats.addGridBagElement(imageFolderTypeField, layout, newLineConstr, this);

		StandardFormats.addGridBagElement(new JLabel("Image filename:"), layout, newLineConstr, this);
		StandardFormats.addGridBagElement(imageFileField, layout, newLineConstr, this);

		// Panel to choose image file type
		StandardFormats.addGridBagElement(new JLabel("Image File Type:"), layout, newLineConstr, this);
		StandardFormats.addGridBagElement(imageTypeField, layout, newLineConstr, this);
		
		StandardFormats.addGridBagElement(new JPanel(), layout, bottomConstr, this);
		setBorder(new TitledBorder("Measurement Properties"));
	}
}
