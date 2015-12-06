package org.youscope.plugin.multicameracontinuousimaging;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.youscope.addon.measurement.MeasurementAddonUIPage;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.clientinterfaces.YouScopeProperties;
import org.youscope.common.configuration.ImageFolderStructure;
import org.youscope.common.measurement.MeasurementSaveSettings;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.StandardFormats;

class GeneralSettingsPage extends MeasurementAddonUIPage<MultiCameraContinousImagingConfiguration>
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 2311111169656492466L;
	private JTextField								nameField				= new JTextField(
			"unnamed");

	private JLabel										runtimeFieldLabel		= new JLabel(
				"Measurement Total Runtime (seconds):");
	
	private JFormattedTextField						runtimeField			= new JFormattedTextField(
				StandardFormats
						.getIntegerFormat());
	
	private JTextField								folderField				= new JTextField();
	
	private JComboBox<String>									imageTypeField;
	
	private JRadioButton								stopByUser				= new JRadioButton(
			"When stopped manually.",
			false);

	private JRadioButton								stopByRuntime			= new JRadioButton(
			"After a given time.",
			false);

	private final YouScopeClient client; 
	private final YouScopeServer server; 
	GeneralSettingsPage(YouScopeClient client, YouScopeServer server)
	{
		this.client = client;
		this.server = server;
	}
	
	@Override
	public void loadData(MultiCameraContinousImagingConfiguration configuration)
	{
		nameField.setText(configuration.getName());
		if (configuration.getMeasurementRuntime() >= 0)
			runtimeField.setValue(configuration.getMeasurementRuntime() / 1000);
		else
			runtimeField.setValue(3600);
		
		MeasurementSaveSettings saveSettings = configuration.getSaveSettings();
		if(saveSettings != null)
		{
			folderField.setText(saveSettings.getFolder());
			imageTypeField.setSelectedItem(saveSettings.getImageFileType());
		}
		if (configuration.getMeasurementRuntime() >= 0)
		{
			stopByRuntime.doClick();
		}
		else
		{
			stopByUser.doClick();
		}
	}

	@Override
	public boolean saveData(MultiCameraContinousImagingConfiguration configuration)
	{
		configuration.setName(nameField.getText());
		if (stopByRuntime.isSelected())
			configuration.setMeasurementRuntime(((Number) runtimeField.getValue()).intValue() * 1000);
		else
			configuration.setMeasurementRuntime(-1);
		
		MeasurementSaveSettings saveSettings = new MeasurementSaveSettings();
		saveSettings.setFolder(folderField.getText());
		saveSettings.setImageFileType((String) imageTypeField.getSelectedItem());
		configuration.setSaveSettings(saveSettings);
		
		client.getProperties().setProperty(YouScopeProperties.PROPERTY_LAST_MEASUREMENT_SAVE_FOLDER, saveSettings.getFolder());
		return true;
	}

	@Override
	public void setToDefault(MultiCameraContinousImagingConfiguration configuration)
	{
		MeasurementSaveSettings saveSettings = new MeasurementSaveSettings();
		saveSettings.setFolder(client.getProperties().getProperty(YouScopeProperties.PROPERTY_LAST_MEASUREMENT_SAVE_FOLDER, ""));
		saveSettings.setImageFolderStructure(ImageFolderStructure.ALL_IN_ONE_FOLDER);
		saveSettings.setImageFileName("%N_time%n");
		configuration.setSaveSettings(saveSettings);
	}

	@Override
	public String getPageName()
	{
		return "General";
	}

	@Override
	public void createUI(final YouScopeFrame frame)
	{
		String[] imageTypes;
		try
		{
			imageTypes = server.getConfiguration().getSupportedImageFormats();
		}
		catch (RemoteException e1)
		{
			client.sendError("Could not obtain supported image file types from server.", e1);
			imageTypes = new String[0];
		}
		imageTypeField = new JComboBox<String>(imageTypes);

		
		GridBagLayout				layout				= new GridBagLayout();
		GridBagConstraints newLineConstr = StandardFormats.getNewLineConstraint();
		
		setLayout(layout);
		StandardFormats.addGridBagElement(new JLabel("Name:"), layout, newLineConstr, this);
		StandardFormats.addGridBagElement(nameField, layout, newLineConstr, this);

		StandardFormats.addGridBagElement(new JLabel("Measurement finishes:"), layout,
				newLineConstr, this);
		ButtonGroup stopConditionGroup = new ButtonGroup();
		stopConditionGroup.add(stopByUser);
		stopConditionGroup.add(stopByRuntime);
		class StopTypeChangedListener implements ActionListener
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (stopByUser.isSelected())
				{
					runtimeFieldLabel.setVisible(false);
					runtimeField.setVisible(false);
					frame.pack();
				}
				else
				{
					// stopByRuntime
					runtimeFieldLabel.setVisible(true);
					runtimeField.setVisible(true);
					frame.pack();
				}
			}
		}
		stopByUser.addActionListener(new StopTypeChangedListener());
		stopByRuntime.addActionListener(new StopTypeChangedListener());

		StandardFormats.addGridBagElement(stopByUser, layout, newLineConstr, this);
		StandardFormats.addGridBagElement(stopByRuntime, layout, newLineConstr, this);

		StandardFormats.addGridBagElement(runtimeFieldLabel, layout, newLineConstr, this);
		StandardFormats.addGridBagElement(runtimeField, layout, newLineConstr, this);

		// Panel to choose files
		StandardFormats.addGridBagElement(new JLabel("Output Directory:"), layout,
				newLineConstr, this);
		JPanel folderPanel = new JPanel(new BorderLayout(5, 0));
		folderPanel.add(folderField, BorderLayout.CENTER);

		if (client.isLocalServer())
		{
			JButton openFolderChooser = new JButton("Edit");
			openFolderChooser.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					JFileChooser fileChooser = new JFileChooser(folderField
							.getText());
					fileChooser
							.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					int returnVal = fileChooser.showDialog(
							null,
							"Open");
					if (returnVal == JFileChooser.APPROVE_OPTION)
					{
						folderField.setText(fileChooser.getSelectedFile()
								.getAbsolutePath());
					}
				}
			});
			folderPanel.add(openFolderChooser, BorderLayout.EAST);
		}
		StandardFormats.addGridBagElement(folderPanel, layout, newLineConstr, this);

		// Panel to choose image file type
		StandardFormats.addGridBagElement(new JLabel("Image File Type:"), layout,
				newLineConstr, this);
		StandardFormats.addGridBagElement(imageTypeField, layout, newLineConstr, this);
		StandardFormats.addGridBagElement(new JPanel(), layout, StandardFormats.getBottomContstraint(), this);

		setBorder(new TitledBorder("Measurement Properties"));
	}
}
