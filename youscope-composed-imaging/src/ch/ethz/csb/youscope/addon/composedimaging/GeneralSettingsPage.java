/**
 * 
 */
package ch.ethz.csb.youscope.addon.composedimaging;

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

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.YouScopeProperties;
import ch.ethz.csb.youscope.client.uielements.PeriodVaryingPanel;
import ch.ethz.csb.youscope.client.uielements.StandardFormats;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.configuration.ImageFolderStructure;
import ch.ethz.csb.youscope.shared.configuration.RegularPeriod;
import ch.ethz.csb.youscope.shared.configuration.VaryingPeriodDTO;
import ch.ethz.csb.youscope.shared.measurement.MeasurementSaveSettings;

/**
 * @author langmo
 * 
 */
class GeneralSettingsPage extends AbstractConfigurationPage
{

	/**
	 * Serial Version UID.
	 */
	private static final long				serialVersionUID		= -8833466993053293407L;

	private GridBagLayout					layout					= new GridBagLayout();

	private JLabel							fixedPeriodLabel		= new JLabel("Fixed period length (seconds):");

	private JTextField						nameField				= new JTextField("composed imaging");

	private JLabel							runtimeFieldLabel		= new JLabel("Measurement Total Runtime (seconds):");

	private JLabel							numExecutionsFieldLabel	= new JLabel("Number of Executions:");

	private JFormattedTextField				runtimeField			= new JFormattedTextField(StandardFormats.getIntegerFormat());

	private JFormattedTextField				numExecutionsField		= new JFormattedTextField(StandardFormats.getIntegerFormat());

	private JTextField						folderField				= new JTextField();

	private JRadioButton					stopByUser				= new JRadioButton("When stopped manually / after tasks finished.", false);

	private JRadioButton					stopByExecutions		= new JRadioButton("After a given number of executions.", false);

	private JRadioButton					stopByRuntime			= new JRadioButton("After a given time.", false);

	private JFormattedTextField				periodField				= new JFormattedTextField(StandardFormats.getIntegerFormat());

	private JRadioButton					periodAFAP				= new JRadioButton("As fast as possible.", false);

	private JRadioButton					periodFixed				= new JRadioButton("Every fixed period.", false);

	private JRadioButton					periodVarying			= new JRadioButton("Varying periods.", false);

	private PeriodVaryingPanel				periodVaryingDataPanel	= new PeriodVaryingPanel();

	private JComboBox<String>						imageTypeField;

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
	public void createUI()
	{
		// Get supported image types
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
		
		setLayout(layout);
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

		StandardFormats.addGridBagElement(new JLabel("Repeat composed imaging:"), layout, newLineConstr, this);
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

		// Panel to choose image file type
		StandardFormats.addGridBagElement(new JLabel("Image File Type:"), layout, newLineConstr, this);
		StandardFormats.addGridBagElement(imageTypeField, layout, newLineConstr, this);
		StandardFormats.addGridBagElement(new JPanel(), layout, StandardFormats.getBottomContstraint(), this);

		setBorder(new TitledBorder("Measurement Properties"));
	}

	@Override
	public void loadData(ComposedImagingMeasurementConfiguration configuration)
	{
		if(configuration.getPeriod().getNumExecutions() >= 0)
			numExecutionsField.setValue(configuration.getPeriod().getNumExecutions());
		else
			numExecutionsField.setValue(10);

		

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

		if(configuration.getPeriod() instanceof RegularPeriod && !((RegularPeriod)configuration.getPeriod()).isFixedTimes())
		{
			periodField.setValue(60);
			periodAFAP.doClick();
		}
		else if(configuration.getPeriod() instanceof RegularPeriod)
		{
			periodField.setValue(((RegularPeriod)configuration.getPeriod()).getPeriod() / 1000);
			periodFixed.doClick();
		}
		else if(configuration.getPeriod() instanceof VaryingPeriodDTO)
		{
			VaryingPeriodDTO period = (VaryingPeriodDTO)configuration.getPeriod();
			periodVaryingDataPanel.setPeriod(period);
			periodField.setValue(60);
			periodVarying.doClick();
		}
	}

	@Override
	public void saveData(ComposedImagingMeasurementConfiguration configuration)
	{
		configuration.setName(nameField.getText());
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
			period.setPeriod(((Number)periodField.getValue()).intValue() * 1000);
			configuration.setPeriod(period);
		}
		else
		{
			// PeriodVarying
			configuration.setPeriod(periodVaryingDataPanel.getPeriod());
		}
		if (stopByRuntime.isSelected())
			configuration.setMeasurementRuntime(((Number) runtimeField.getValue()).intValue() * 1000);
		else
			configuration.setMeasurementRuntime(-1);
		if(stopByExecutions.isSelected())
			configuration.getPeriod().setNumExecutions(((Number)numExecutionsField.getValue()).intValue());
		else
			configuration.getPeriod().setNumExecutions(-1);
		
		MeasurementSaveSettings saveSettings = new MeasurementSaveSettings();
		saveSettings.setFolder(folderField.getText());
		saveSettings.setImageFileType((String) imageTypeField.getSelectedItem());
		configuration.setSaveSettings(saveSettings);
		
		client.getProperties().setProperty(YouScopeProperties.PROPERTY_LAST_MEASUREMENT_SAVE_FOLDER, saveSettings.getFolder());
	}

	@Override
	public void setToDefault(ComposedImagingMeasurementConfiguration configuration)
	{
		MeasurementSaveSettings saveSettings = new MeasurementSaveSettings();
		saveSettings.setFolder(client.getProperties().getProperty(YouScopeProperties.PROPERTY_LAST_MEASUREMENT_SAVE_FOLDER, ""));
		saveSettings.setImageFolderStructure(ImageFolderStructure.ALL_IN_ONE_FOLDER);
		saveSettings.setImageFileName("%N_position%4p_time%n");
		configuration.setSaveSettings(saveSettings);
		
		if(configuration.getPeriod() == null)
		{
			// Set to AFAP
			RegularPeriod period = new RegularPeriod();
			period.setFixedTimes(false);
			period.setStartTime(0);
			period.setPeriod(0);
			configuration.setPeriod(period);
		}
	}

	@Override
	public String getPageName()
	{
		return "General Settings";
	}

}
