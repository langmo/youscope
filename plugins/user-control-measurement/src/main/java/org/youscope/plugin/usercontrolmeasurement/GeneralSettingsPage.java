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
package org.youscope.plugin.usercontrolmeasurement;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.border.TitledBorder;

import org.youscope.addon.measurement.MeasurementAddonUIPage;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.saving.SaveSettingsConfiguration;
import org.youscope.uielements.StandardFormats;
import org.youscope.uielements.SubConfigurationPanel;

class GeneralSettingsPage extends MeasurementAddonUIPage<UserControlMeasurementConfiguration>
{

	/**
	 * Serial Verision UID.
	 */
	private static final long				serialVersionUID		= 885352612109223078L;

	private final YouScopeClient	client;

	private SubConfigurationPanel<SaveSettingsConfiguration> saveSettingPanel = null;
	
	GeneralSettingsPage(YouScopeClient client)
	{
		this.client = client;
	}

	@Override
	public void loadData(UserControlMeasurementConfiguration configuration)
	{
		saveSettingPanel.setConfiguration(configuration.getSaveSettings());
	}

	@Override
	public boolean saveData(UserControlMeasurementConfiguration configuration)
	{
		
		configuration.setSaveSettings(saveSettingPanel.getConfiguration());

		return true;
	}

	@Override
	public void setToDefault(UserControlMeasurementConfiguration configuration)
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
		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);

		GridBagConstraints bottomConstr = StandardFormats.getBottomContstraint();

		// Panel to choose save settings
		saveSettingPanel = new SubConfigurationPanel<SaveSettingsConfiguration>("Save type:", null, SaveSettingsConfiguration.class, client, frame);
		StandardFormats.addGridBagElement(saveSettingPanel, layout, bottomConstr, this);
		setBorder(new TitledBorder("Measurement Properties"));
	}
}
