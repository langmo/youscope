package org.youscope.plugin.simplemeasurement;

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
import org.youscope.common.configuration.RegularPeriod;
import org.youscope.common.configuration.VaryingPeriod;
import org.youscope.common.measurement.MeasurementSaveSettings;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.FileNameComboBox;
import org.youscope.uielements.PeriodField;
import org.youscope.uielements.PeriodVaryingPanel;
import org.youscope.uielements.StandardFormats;

class GeneralSettingsPage extends MeasurementAddonUIPage<SimpleMeasurementConfiguration>
{

	/**
	 * Serial Verision UID.
	 */
	private static final long				serialVersionUID		= 885352612109223078L;

	private final YouScopeClient	client;
	private final YouScopeServer			server;

	private final JLabel					fixedPeriodLabel		= new JLabel("Fixed period length:");

	private JTextField						nameField				= new JTextField("unnamed");

	private JRadioButton					stopByUser				= new JRadioButton("When stopped manually.", false);

	private JRadioButton					stopByExecutions		= new JRadioButton("After a given number of executions.", false);

	private JRadioButton					stopByRuntime			= new JRadioButton("After a given time.", false);

	private JLabel							runtimeFieldLabel		= new JLabel("Measurement Total Runtime:");

	private JLabel							numExecutionsFieldLabel	= new JLabel("Number of Executions:");

	private PeriodField				runtimeField			= new PeriodField();

	private JFormattedTextField				numExecutionsField		= new JFormattedTextField(StandardFormats.getIntegerFormat());

	private PeriodField				periodField				= new PeriodField();

	private JRadioButton					periodAFAP				= new JRadioButton("As fast as possible.", false);

	private JRadioButton					periodFixed				= new JRadioButton("Every fixed period.", false);

	private JRadioButton					periodVarying			= new JRadioButton("Varying periods.", false);

	private PeriodVaryingPanel				periodVaryingDataPanel	= new PeriodVaryingPanel();

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
	public void loadData(SimpleMeasurementConfiguration configuration)
	{
		nameField.setText(configuration.getName());
		if(configuration.getMeasurementRuntime() >= 0)
			runtimeField.setDuration(configuration.getMeasurementRuntime());
		else
			runtimeField.setDuration(60*60*1000);
		
		MeasurementSaveSettings saveSettings = configuration.getSaveSettings();
		if(saveSettings != null)
		{
			folderField.setText(saveSettings.getFolder());
			imageFolderTypeField.setSelectedItem(saveSettings.getImageFolderStructure());
			imageFileField.setSelectedItem(saveSettings.getImageFileName());
			imageTypeField.setSelectedItem(saveSettings.getImageFileType());
		}
		
		if(configuration.getPeriod().getNumExecutions() >= 0)
			numExecutionsField.setValue(configuration.getPeriod().getNumExecutions());
		else
			numExecutionsField.setValue(1);
		if(configuration.getPeriod() instanceof RegularPeriod && !((RegularPeriod)configuration.getPeriod()).isFixedTimes())
		{
			periodField.setDuration(10 * 60 * 1000);
			periodAFAP.doClick();
		}
		else if(configuration.getPeriod() instanceof RegularPeriod)
		{
			periodField.setDuration(((RegularPeriod)configuration.getPeriod()).getPeriod());
			periodFixed.doClick();
		}
		else if(configuration.getPeriod() instanceof VaryingPeriod)
		{
			VaryingPeriod period = (VaryingPeriod)configuration.getPeriod();
			periodVaryingDataPanel.setPeriod(period);
			periodField.setDuration(10 * 60 * 1000);
			periodVarying.doClick();
		}

		if(configuration.getMeasurementRuntime() >= 0)
		{
			stopByRuntime.doClick();
		}
		else if(configuration.getPeriod().getNumExecutions() >= 0)
		{
			stopByExecutions.doClick();
		}
		else
		{
			stopByUser.doClick();
		}
	}

	@Override
	public boolean saveData(SimpleMeasurementConfiguration configuration)
	{
		if(periodAFAP.isSelected())
		{
			RegularPeriod period = new RegularPeriod();
			period.setFixedTimes(false);
			period.setStartTime(0);
			period.setPeriod(0);
			configuration.setPeriod(period);
		}
		else if(periodFixed.isSelected())
		{
			RegularPeriod period = new RegularPeriod();
			period.setFixedTimes(true);
			period.setStartTime(0);
			period.setPeriod(periodField.getDuration());
			configuration.setPeriod(period);
		}
		else
		{
			// PeriodVarying
			configuration.setPeriod(periodVaryingDataPanel.getPeriod());
		}
		configuration.setName(nameField.getText());
		if(stopByRuntime.isSelected())
			configuration.setMeasurementRuntime(runtimeField.getDuration());
		else
			configuration.setMeasurementRuntime(-1);
		if(stopByExecutions.isSelected())
			configuration.getPeriod().setNumExecutions(((Number)numExecutionsField.getValue()).intValue());
		else
			configuration.getPeriod().setNumExecutions(-1);
		
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
	public void setToDefault(SimpleMeasurementConfiguration configuration)
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

		StandardFormats.addGridBagElement(new JLabel("Measurement finishes:"), layout, newLineConstr, this);
		ButtonGroup stopConditionGroup = new ButtonGroup();
		stopConditionGroup.add(stopByUser);
		stopConditionGroup.add(stopByExecutions);
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
					numExecutionsFieldLabel.setVisible(false);
					numExecutionsField.setVisible(false);
					fireSizeChanged();
				}
				else if(stopByExecutions.isSelected())
				{
					runtimeFieldLabel.setVisible(false);
					runtimeField.setVisible(false);
					numExecutionsFieldLabel.setVisible(true);
					numExecutionsField.setVisible(true);
					fireSizeChanged();
				}
				else
				{
					// stopByRuntime
					runtimeFieldLabel.setVisible(true);
					runtimeField.setVisible(true);
					numExecutionsFieldLabel.setVisible(false);
					numExecutionsField.setVisible(false);
					fireSizeChanged();
				}
			}
		}
		stopByUser.addActionListener(new StopTypeChangedListener());
		stopByExecutions.addActionListener(new StopTypeChangedListener());
		stopByRuntime.addActionListener(new StopTypeChangedListener());

		StandardFormats.addGridBagElement(stopByUser, layout, newLineConstr, this);
		StandardFormats.addGridBagElement(stopByExecutions, layout, newLineConstr, this);
		StandardFormats.addGridBagElement(stopByRuntime, layout, newLineConstr, this);

		StandardFormats.addGridBagElement(runtimeFieldLabel, layout, newLineConstr, this);
		StandardFormats.addGridBagElement(runtimeField, layout, newLineConstr, this);

		StandardFormats.addGridBagElement(numExecutionsFieldLabel, layout, newLineConstr, this);
		StandardFormats.addGridBagElement(numExecutionsField, layout, newLineConstr, this);

		StandardFormats.addGridBagElement(new JLabel("Repeat Imaging Protocol:"), layout, newLineConstr, this);
		StandardFormats.addGridBagElement(periodAFAP, layout, newLineConstr, this);
		StandardFormats.addGridBagElement(periodFixed, layout, newLineConstr, this);
		StandardFormats.addGridBagElement(periodVarying, layout, newLineConstr, this);
		StandardFormats.addGridBagElement(fixedPeriodLabel, layout, newLineConstr, this);
		StandardFormats.addGridBagElement(periodField, layout, newLineConstr, this);
		StandardFormats.addGridBagElement(periodVaryingDataPanel, layout, newLineConstr, this);

		ButtonGroup periodGroup = new ButtonGroup();
		periodGroup.add(periodAFAP);
		periodGroup.add(periodFixed);
		periodGroup.add(periodVarying);
		class PeriodTypeChangedListener implements ActionListener
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(periodAFAP.isSelected())
				{
					periodField.setVisible(false);
					fixedPeriodLabel.setVisible(false);
					periodVaryingDataPanel.setVisible(false);
					fireSizeChanged();
				}
				else if(periodFixed.isSelected())
				{
					periodField.setVisible(true);
					fixedPeriodLabel.setVisible(true);
					periodVaryingDataPanel.setVisible(false);
					fireSizeChanged();
				}
				else
				{
					// PeriodVarying
					periodField.setVisible(false);
					fixedPeriodLabel.setVisible(false);
					periodVaryingDataPanel.setVisible(true);
					fireSizeChanged();
				}
			}
		}
		periodAFAP.addActionListener(new PeriodTypeChangedListener());
		periodFixed.addActionListener(new PeriodTypeChangedListener());
		periodVarying.addActionListener(new PeriodTypeChangedListener());

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
