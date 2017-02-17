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
package org.youscope.plugin.microplate.measurement;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import org.youscope.addon.measurement.MeasurementAddonUIPage;
import org.youscope.clientinterfaces.StandardProperty;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.saving.SaveSettingsConfiguration;
import org.youscope.common.task.RegularPeriodConfiguration;
import org.youscope.common.task.VaryingPeriodConfiguration;
import org.youscope.uielements.PeriodField;
import org.youscope.uielements.PeriodVaryingPanel;
import org.youscope.uielements.StandardFormats;
import org.youscope.uielements.SubConfigurationPanel;

class GeneralSettingsPage extends MeasurementAddonUIPage<MicroplateMeasurementConfiguration>
{

	/**
	 * Serial Verision UID.
	 */
	private static final long				serialVersionUID		= 885352612109223078L;

	private final YouScopeClient	client; 

	private final JLabel					fixedPeriodLabel		= new JLabel("Fixed period length:");

	private final JLabel					fixedWellTimeLabel		= new JLabel("Fixed Runtime per Well:");

	private JRadioButton					stopByUser				= new JRadioButton("When stopped manually.", false);

	private JRadioButton					stopByExecutions		= new JRadioButton("After a given number of executions.", false);

	private JRadioButton					stopByRuntime			= new JRadioButton("After a given time.", false);

	private JLabel							runtimeFieldLabel		= new JLabel("Measurement Total Runtime:");

	private JLabel							numExecutionsFieldLabel	= new JLabel("Number of Executions:");

	private PeriodField				runtimeField			= new PeriodField();

	private JFormattedTextField				numExecutionsField		= new JFormattedTextField(StandardFormats.getIntegerFormat());

	private JRadioButton					wellTimeAFAP			= new JRadioButton("As long as it needs.", false);

	private JRadioButton					wellTimeFixed			= new JRadioButton("Exactly a given time.", false);

	private PeriodField				wellTimeField			= new PeriodField();

	private PeriodField				periodField				= new PeriodField();

	private JRadioButton					periodAFAP				= new JRadioButton("As fast as possible.", false);

	private JRadioButton					periodFixed				= new JRadioButton("Every fixed period.", false);

	private JRadioButton					periodVarying			= new JRadioButton("Varying periods.", false);

	private PeriodVaryingPanel				periodVaryingDataPanel	= new PeriodVaryingPanel();

	private SubConfigurationPanel<SaveSettingsConfiguration> saveSettingPanel = null;
	
	GeneralSettingsPage(YouScopeClient client)
	{
		this.client = client;
	}

	@Override
	public void loadData(MicroplateMeasurementConfiguration configuration)
	{
		if(configuration.getMaxRuntime() >= 0)
			runtimeField.setDuration(configuration.getMaxRuntime());
		else
			runtimeField.setDuration(60*60*1000);
		if(configuration.getSaveSettings() == null)
			saveSettingPanel.setConfiguration(client.getPropertyProvider().getProperty(StandardProperty.PROPERTY_MEASUREMENT_STANDARD_SAVE_SETTINGS_TYPE).toString());
		else
			saveSettingPanel.setConfiguration(configuration.getSaveSettings());
		
		if(configuration.getPeriod() == null)
			configuration.setPeriod(new RegularPeriodConfiguration());
		
		if(configuration.getPeriod().getNumExecutions() >= 0)
			numExecutionsField.setValue(configuration.getPeriod().getNumExecutions());
		else
			numExecutionsField.setValue(1);
		if(configuration.getPeriod() instanceof RegularPeriodConfiguration && !((RegularPeriodConfiguration)configuration.getPeriod()).isFixedTimes())
		{
			periodField.setDuration(10 * 60 * 1000);
			periodAFAP.doClick();
		}
		else if(configuration.getPeriod() instanceof RegularPeriodConfiguration)
		{
			periodField.setDuration(((RegularPeriodConfiguration)configuration.getPeriod()).getPeriod());
			periodFixed.doClick();
		}
		else if(configuration.getPeriod() instanceof VaryingPeriodConfiguration)
		{
			VaryingPeriodConfiguration period = (VaryingPeriodConfiguration)configuration.getPeriod();
			periodVaryingDataPanel.setPeriod(period);
			periodField.setDuration(10 * 60 * 1000);
			periodVarying.doClick();
		}

		if(configuration.getTimePerWell() == -1)
		{
			wellTimeField.setDuration(5000);
			wellTimeAFAP.doClick();
		}
		else
		{
			wellTimeField.setDuration(configuration.getTimePerWell());
			wellTimeFixed.doClick();
		}
		
		if(configuration.getMaxRuntime() >= 0)
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
	public boolean saveData(MicroplateMeasurementConfiguration configuration)
	{
		if(periodAFAP.isSelected())
		{
			RegularPeriodConfiguration period = new RegularPeriodConfiguration();
			period.setFixedTimes(false);
			period.setStartTime(0);
			period.setPeriod(0);
			configuration.setPeriod(period);
		}
		else if(periodFixed.isSelected())
		{
			RegularPeriodConfiguration period = new RegularPeriodConfiguration();
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
		if(wellTimeAFAP.isSelected())
			configuration.setTimePerWell(-1);
		else
			configuration.setTimePerWell(wellTimeField.getDuration());
		if(stopByRuntime.isSelected())
			configuration.setMaxRuntime(runtimeField.getDuration());
		else
			configuration.setMaxRuntime(-1);
		if(stopByExecutions.isSelected())
			configuration.getPeriod().setNumExecutions(((Number)numExecutionsField.getValue()).intValue());
		else
			configuration.getPeriod().setNumExecutions(-1);
		
		
		configuration.setSaveSettings(saveSettingPanel.getConfiguration());

		return true;
	}

	@Override
	public void setToDefault(MicroplateMeasurementConfiguration configuration)
	{
		// do nothing.
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

		StandardFormats.addGridBagElement(new JLabel("Iterate through all wells:"), layout, newLineConstr, this);
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

		StandardFormats.addGridBagElement(new JLabel("Stay in one Well:"), layout, newLineConstr, this);
		StandardFormats.addGridBagElement(wellTimeAFAP, layout, newLineConstr, this);
		StandardFormats.addGridBagElement(wellTimeFixed, layout, newLineConstr, this);
		StandardFormats.addGridBagElement(fixedWellTimeLabel, layout, newLineConstr, this);
		StandardFormats.addGridBagElement(wellTimeField, layout, newLineConstr, this);
		ButtonGroup wellTimeGroup = new ButtonGroup();
		wellTimeGroup.add(wellTimeAFAP);
		wellTimeGroup.add(wellTimeFixed);
		class WellTimeTypeChangedListener implements ActionListener
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(wellTimeAFAP.isSelected())
				{
					wellTimeField.setVisible(false);
					fixedWellTimeLabel.setVisible(false);
					fireSizeChanged();
				}
				else
				{
					wellTimeField.setVisible(true);
					fixedWellTimeLabel.setVisible(true);
					fireSizeChanged();
				}
			}
		}
		wellTimeAFAP.addActionListener(new WellTimeTypeChangedListener());
		wellTimeFixed.addActionListener(new WellTimeTypeChangedListener());

		// Panel to choose save settings
		saveSettingPanel = new SubConfigurationPanel<SaveSettingsConfiguration>("Save type:", null, SaveSettingsConfiguration.class, client, frame);
		StandardFormats.addGridBagElement(saveSettingPanel, layout, bottomConstr, this);
		
		setBorder(new TitledBorder("Measurement Properties"));
	}
}
