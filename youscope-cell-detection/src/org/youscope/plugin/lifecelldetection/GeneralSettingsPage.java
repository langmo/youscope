/**
 * 
 */
package org.youscope.plugin.lifecelldetection;

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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.youscope.addon.measurement.MeasurementAddonUIPage;
import org.youscope.clientinterfaces.StandardProperty;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.configuration.FolderStructureConfiguration;
import org.youscope.common.measurement.MeasurementSaveSettings;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.PeriodField;
import org.youscope.uielements.StandardFormats;

/**
 * @author langmo
 * 
 */
class GeneralSettingsPage extends MeasurementAddonUIPage<CellDetectionMeasurementConfiguration>
{

	/**
	 * Serial Version UID.
	 */
	private static final long				serialVersionUID		= -8833466993053293407L;

	private GridBagLayout					layout					= new GridBagLayout();

	private JLabel							fixedPeriodLabel		= new JLabel("Fixed period length (seconds):");

	private JTextField						nameField				= new JTextField("CellDetection");

	private JLabel							runtimeFieldLabel		= new JLabel("Measurement Total Runtime:");

	private PeriodField						runtimeField			= new PeriodField();

	private JTextField						folderField				= new JTextField();

	private JRadioButton					stopByUser				= new JRadioButton("When stopped manually / after tasks finished.", false);

	private JRadioButton					stopByRuntime			= new JRadioButton("After a given time.", false);

	private JRadioButton					periodAFAP				= new JRadioButton("As fast as possible.", false);

	private JRadioButton					periodFixed				= new JRadioButton("Every fixed period.", false);

	private JComboBox<String>						imageTypeField;
	
	private PeriodField				periodField				= new PeriodField();

	private final YouScopeClient	client;
	private final YouScopeServer			server;

	private GridBagConstraints				newLineConstr			= StandardFormats.getNewLineConstraint();

	// Instance Initializer
	GeneralSettingsPage(YouScopeClient client, YouScopeServer server)
	{
		this.server = server;
		this.client = client;
	}
	@Override
	public void createUI(YouScopeFrame parentFrame)
	{
		// Get supported image types
		String[] imageTypes;
		try
		{
			imageTypes = server.getProperties().getSupportedImageFormats();
		}
		catch (RemoteException e1)
		{
			client.sendError("Could not obtain supported image file types from server.", e1);
			imageTypes = new String[0];
		}
		imageTypeField = new JComboBox<String>(imageTypes);
		
		setLayout(layout);
		StandardFormats.addGridBagElement(new JLabel("Name:"), layout, newLineConstr, this);
		StandardFormats.addGridBagElement(nameField, layout, newLineConstr, this);

		StandardFormats.addGridBagElement(new JLabel("Measurement finishes:"), layout, newLineConstr, this);
		ButtonGroup stopConditionGroup = new ButtonGroup();
		stopConditionGroup.add(stopByUser);
		stopConditionGroup.add(stopByRuntime);
		class StopTypeChangedListener implements ActionListener
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(stopByUser.isSelected())
				{
					runtimeFieldLabel.setVisible(false);
					runtimeField.setVisible(false);
					fireSizeChanged();
				}
				else
				{
					// stopByRuntime
					runtimeFieldLabel.setVisible(true);
					runtimeField.setVisible(true);
					fireSizeChanged();
				}
			}
		}
		stopByUser.addActionListener(new StopTypeChangedListener());
		stopByRuntime.addActionListener(new StopTypeChangedListener());

		StandardFormats.addGridBagElement(stopByUser, layout, newLineConstr, this);
		StandardFormats.addGridBagElement(stopByRuntime, layout, newLineConstr, this);

		StandardFormats.addGridBagElement(runtimeFieldLabel, layout, newLineConstr, this);
		StandardFormats.addGridBagElement(runtimeField, layout, newLineConstr, this);

		StandardFormats.addGridBagElement(new JLabel("Imaging Period:"), layout, newLineConstr, this);
		StandardFormats.addGridBagElement(periodAFAP, layout, newLineConstr, this);
		StandardFormats.addGridBagElement(periodFixed, layout, newLineConstr, this);
		StandardFormats.addGridBagElement(fixedPeriodLabel, layout, newLineConstr, this);
		StandardFormats.addGridBagElement(periodField, layout, newLineConstr, this);

		ButtonGroup periodGroup = new ButtonGroup();
		periodGroup.add(periodAFAP);
		periodGroup.add(periodFixed);
		class PeriodTypeChangedListener implements ActionListener
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(periodAFAP.isSelected())
				{
					periodField.setVisible(false);
					fixedPeriodLabel.setVisible(false);
					fireSizeChanged();
				}
				else if(periodFixed.isSelected())
				{
					periodField.setVisible(true);
					fixedPeriodLabel.setVisible(true);
					fireSizeChanged();
				}
			}
		}
		periodAFAP.addActionListener(new PeriodTypeChangedListener());
		periodFixed.addActionListener(new PeriodTypeChangedListener());
		
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

		// Panel to choose image file type
		StandardFormats.addGridBagElement(new JLabel("Image File Type:"), layout, newLineConstr, this);
		StandardFormats.addGridBagElement(imageTypeField, layout, newLineConstr, this);
		StandardFormats.addGridBagElement(new JPanel(), layout, StandardFormats.getBottomContstraint(), this);

		setBorder(new TitledBorder("Measurement Properties"));
	}

	@Override
	public void loadData(CellDetectionMeasurementConfiguration configuration)
	{
		nameField.setText(configuration.getName());
		if (configuration.getMeasurementRuntime() >= 0)
			runtimeField.setDuration(configuration.getMeasurementRuntime());
		else
			runtimeField.setDuration(3600000);
		
		MeasurementSaveSettings saveSettings = configuration.getSaveSettings();
		if(saveSettings != null)
		{
			folderField.setText(saveSettings.getFolder());
			imageTypeField.setSelectedItem(saveSettings.getImageFileType());
		}
		
		if(configuration.getMeasurementRuntime() >= 0)
		{
			stopByRuntime.doClick();
		}
		else
		{
			stopByUser.doClick();
		}

		if(configuration.getImagingPeriod() <= 0)
		{
			periodField.setDuration(5000);
			periodAFAP.doClick();
		}
		else
		{
			periodField.setDuration(configuration.getImagingPeriod());
			periodFixed.doClick();
		}
	}

	@Override
	public boolean saveData(CellDetectionMeasurementConfiguration configuration)
	{
		configuration.setName(nameField.getText());
		if(periodAFAP.isSelected())
		{
			configuration.setImagingPeriod(0);
		}
		else
		{
			configuration.setImagingPeriod(periodField.getDuration());
		}
		if (stopByRuntime.isSelected())
			configuration.setMeasurementRuntime(runtimeField.getDuration());
		else
			configuration.setMeasurementRuntime(-1);
		
		MeasurementSaveSettings saveSettings = new MeasurementSaveSettings();
		saveSettings.setFolder(folderField.getText());
		saveSettings.setImageFileType((String) imageTypeField.getSelectedItem());
		configuration.setSaveSettings(saveSettings);
		
		client.getProperties().setProperty(StandardProperty.PROPERTY_LAST_MEASUREMENT_SAVE_FOLDER, saveSettings.getFolder());
		
		return true;
	}

	@Override
	public void setToDefault(CellDetectionMeasurementConfiguration configuration)
	{
		MeasurementSaveSettings saveSettings = new MeasurementSaveSettings();
		saveSettings.setFolder((String) client.getProperties().getProperty(StandardProperty.PROPERTY_LAST_MEASUREMENT_SAVE_FOLDER));
		saveSettings.setImageFolderStructure(FolderStructureConfiguration.ALL_IN_ONE_FOLDER);
		saveSettings.setImageFileName("%N_position%4p_time%n");
		configuration.setSaveSettings(saveSettings);
	}

	@Override
	public String getPageName()
	{
		return "General Settings";
	}

}
