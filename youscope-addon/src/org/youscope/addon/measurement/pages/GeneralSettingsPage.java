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
package org.youscope.addon.measurement.pages;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.ButtonGroup;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import org.youscope.addon.measurement.MeasurementAddonUIPage;
import org.youscope.clientinterfaces.StandardProperty;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.measurement.MeasurementConfiguration;
import org.youscope.common.saving.SaveSettingsConfiguration;
import org.youscope.common.task.PeriodConfiguration;
import org.youscope.common.task.RegularPeriodConfiguration;
import org.youscope.common.task.VaryingPeriodConfiguration;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.PeriodField;
import org.youscope.uielements.PeriodVaryingPanel;
import org.youscope.uielements.StandardFormats;
import org.youscope.uielements.SubConfigurationPanel;

/**
 * A page with which the measurement name, save setting and runtime can be set. Setting save settings is omitted when the 
 * configuration class does not have the functions with signatures
 * <code>PeriodConfiguration getPeriod()</code> and <code>void setPeriod(PeriodConfiguration)</code>, which is determined using reflection.
 * @author mlang
 *
 * @param <T> Measurement configuration type
 */
public class GeneralSettingsPage<T extends MeasurementConfiguration> extends MeasurementAddonUIPage<T>
{

	/**
	 * Serial Verision UID.
	 */
	private static final long				serialVersionUID		= 885352612109223078L;

	private final YouScopeClient	client;

	private final JLabel					fixedPeriodLabel		= new JLabel("Fixed period length:");

	private final JRadioButton					stopByUser				= new JRadioButton("When stopped manually.", false);

	private final JRadioButton					stopByExecutions		= new JRadioButton("After a given number of executions.", false);

	private final JRadioButton					stopByRuntime			= new JRadioButton("After a given time.", false);

	private final JLabel							runtimeFieldLabel		= new JLabel("Measurement Total Runtime:");

	private final JLabel							numExecutionsFieldLabel	= new JLabel("Number of Executions:");

	private final PeriodField				runtimeField			= new PeriodField();

	private final JFormattedTextField				numExecutionsField		= new JFormattedTextField(StandardFormats.getIntegerFormat());

	private final PeriodField				periodField				= new PeriodField();

	private final JRadioButton					periodAFAP				= new JRadioButton("As fast as possible.", false);

	private final JRadioButton					periodFixed				= new JRadioButton("Every fixed period.", false);

	private final JRadioButton					periodVarying			= new JRadioButton("Varying periods.", false);

	private final PeriodVaryingPanel				periodVaryingDataPanel	= new PeriodVaryingPanel();

	private SubConfigurationPanel<SaveSettingsConfiguration> saveSettingPanel = null;
	
	private final Method getPeriod;
	private final Method setPeriod;
	
	/**
	 * Constructor.
	 * @param client YouScope client.
	 * @param configurationClass Specific measurement configuration class. If this class implements getPeriod and setPeriod as described in the class description, Period set choices are displayed, otherwise not.
	 */
	public GeneralSettingsPage(YouScopeClient client, Class<T> configurationClass)
	{
		this.client = client;

		//initialize functions
		Method method;
		try {
			method = configurationClass.getDeclaredMethod("getPeriod");
			if(!PeriodConfiguration.class.isAssignableFrom(method.getReturnType()))
				method = null;
		} catch (@SuppressWarnings("unused") NoSuchMethodException | SecurityException e) {
			method = null;
		}
		getPeriod = method;
		try {
			method = configurationClass.getDeclaredMethod("setPeriod", PeriodConfiguration.class);
		} catch (@SuppressWarnings("unused") NoSuchMethodException | SecurityException e) {
			method = null;
		}
		setPeriod = method;
	}
	private boolean isPeriod()
	{
		return getPeriod != null && setPeriod != null;
	}
	private PeriodConfiguration getPeriod(MeasurementConfiguration configuration)
	{
		if(getPeriod == null)
			return null;
		try {
			return (PeriodConfiguration) getPeriod.invoke(configuration);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			client.sendError("Could not get period.", e);
			return null;
		}
	}
	private void setPeriod(MeasurementConfiguration configuration, PeriodConfiguration period)
	{
		if(setPeriod == null)
			return;
		try {
			setPeriod.invoke(configuration, period);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			client.sendError("Could not get period.", e);
			return;
		}
	}

	@Override
	public void loadData(MeasurementConfiguration configuration)
	{
		if(configuration.getMaxRuntime() >= 0)
			runtimeField.setDuration(configuration.getMaxRuntime());
		else
			runtimeField.setDuration(60*60*1000);
		if(configuration.getSaveSettings() == null)
			saveSettingPanel.setConfiguration(client.getPropertyProvider().getProperty(StandardProperty.PROPERTY_MEASUREMENT_STANDARD_SAVE_SETTINGS_TYPE).toString());
		else
			saveSettingPanel.setConfiguration(configuration.getSaveSettings());
		PeriodConfiguration period;
		if(isPeriod())
		{
			period = getPeriod(configuration);
			if(period == null)
			{
				period = new RegularPeriodConfiguration();
				setPeriod(configuration, period);
			}
				
			if(period.getNumExecutions() >= 0)
				numExecutionsField.setValue(period.getNumExecutions());
			else
				numExecutionsField.setValue(1);
			if(period instanceof RegularPeriodConfiguration && !((RegularPeriodConfiguration)period).isFixedTimes())
			{
				periodField.setDuration(10 * 60 * 1000);
				periodAFAP.doClick();
			}
			else if(period instanceof RegularPeriodConfiguration)
			{
				periodField.setDuration(((RegularPeriodConfiguration)period).getPeriod());
				periodFixed.doClick();
			}
			else if(period instanceof VaryingPeriodConfiguration)
			{
				periodVaryingDataPanel.setPeriod((VaryingPeriodConfiguration)period);
				periodField.setDuration(10 * 60 * 1000);
				periodVarying.doClick();
			}
		}
		else
			period = null;
		if(configuration.getMaxRuntime() >= 0)
		{
			stopByRuntime.doClick();
		}
		else if(period != null && period.getNumExecutions() >= 0)
		{
			stopByExecutions.doClick();
		}
		else
		{
			stopByUser.doClick();
		}
	
	}

	@Override
	public boolean saveData(MeasurementConfiguration configuration)
	{
		if(isPeriod())
		{
			PeriodConfiguration period;
			if(periodAFAP.isSelected())
			{
				RegularPeriodConfiguration regPeriod = new RegularPeriodConfiguration();
				regPeriod.setFixedTimes(false);
				regPeriod.setStartTime(0);
				regPeriod.setPeriod(0);
				period = regPeriod;
			}
			else if(periodFixed.isSelected())
			{
				RegularPeriodConfiguration regPeriod = new RegularPeriodConfiguration();
				regPeriod.setFixedTimes(true);
				regPeriod.setStartTime(0);
				regPeriod.setPeriod(periodField.getDuration());
				period = regPeriod;
			}
			else
			{
				// PeriodVarying
				period = periodVaryingDataPanel.getPeriod();
			}
			if(stopByExecutions.isSelected())
				period.setNumExecutions(((Number)numExecutionsField.getValue()).intValue());
			else
				period.setNumExecutions(-1);
			setPeriod(configuration, period);
		}
		if(stopByRuntime.isSelected())
			configuration.setMaxRuntime(runtimeField.getDuration());
		else
			configuration.setMaxRuntime(-1);
		
		
		configuration.setSaveSettings(saveSettingPanel.getConfiguration());

		return true;
	}

	@Override
	public void setToDefault(MeasurementConfiguration configuration)
	{
		// do nothing
	}

	@Override
	public String getPageName()
	{
		return "Measurement Properties";
	}

	@Override
	public void createUI(YouScopeFrame frame)
	{
		DynamicPanel mainPanel = new DynamicPanel();
		
		mainPanel.add(new JLabel("Measurement finishes:"));
		ButtonGroup stopConditionGroup = new ButtonGroup();
		stopConditionGroup.add(stopByUser);
		if(isPeriod())
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
				else if(isPeriod() && stopByExecutions.isSelected())
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

		mainPanel.add(stopByUser);
		if(isPeriod())
			mainPanel.add(stopByExecutions);
		mainPanel.add(stopByRuntime);

		mainPanel.add(runtimeFieldLabel);
		mainPanel.add(runtimeField);

		if(isPeriod())
		{
			mainPanel.add(numExecutionsFieldLabel);
			mainPanel.add(numExecutionsField);

			mainPanel.add(new JLabel("Repeat Imaging Protocol:"));
			mainPanel.add(periodAFAP);
			mainPanel.add(periodFixed);
			mainPanel.add(periodVarying);
			mainPanel.add(fixedPeriodLabel);
			mainPanel.add(periodField);
			mainPanel.add(periodVaryingDataPanel);
	
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
		}
		// Panel to choose save settings
		saveSettingPanel = new SubConfigurationPanel<SaveSettingsConfiguration>("Save type:", null, SaveSettingsConfiguration.class, client, frame);
		mainPanel.addFill(saveSettingPanel);
		setLayout(new BorderLayout());
		add(mainPanel, BorderLayout.CENTER);
		setBorder(new TitledBorder("Measurement Properties"));
	}
}
