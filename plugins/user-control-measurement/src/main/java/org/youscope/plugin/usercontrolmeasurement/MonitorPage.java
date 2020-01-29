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

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.youscope.addon.measurement.MeasurementAddonUIPage;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.microscope.Device;
import org.youscope.common.microscope.StageDevice;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.StandardFormats;

class MonitorPage extends MeasurementAddonUIPage<UserControlMeasurementConfiguration>
{

	/**
	 * Serial Verision UID.
	 */
	private static final long				serialVersionUID		= 885352612109223078L;

	private final YouScopeClient	client;
	private final YouScopeServer			server;

	private final JCheckBox monitorField = new JCheckBox("Detect successive images made at the same position");
	
	private final JLabel stageDeviceLabel = new JLabel("Stage to monitor:");
	private final JComboBox<String> stageDeviceField = new JComboBox<String>();

	private final JLabel toleranceLabel = new JLabel("Maximal positioning tolerance of stage:");
    private final JFormattedTextField toleranceField = new JFormattedTextField(StandardFormats.getDoubleFormat());
	
	MonitorPage(YouScopeClient client, YouScopeServer server)
	{
		this.server = server;
		this.client = client;
	}

	@Override
	public void loadData(UserControlMeasurementConfiguration configuration)
	{
		loadStageDevices();
		
		if(configuration.getStageTolerance()>=0)
		{
			monitorField.setSelected(true);
			if(configuration.getStageDevice() != null)
				stageDeviceField.setSelectedItem(configuration.getStageDevice());
			toleranceField.setValue(configuration.getStageTolerance());
			
			stageDeviceLabel.setVisible(true);
			stageDeviceField.setVisible(true);
			toleranceLabel.setVisible(true);
			toleranceField.setVisible(true);
		}
		else
		{
			monitorField.setSelected(false);
			if(configuration.getStageDevice() != null)
				stageDeviceField.setSelectedItem(configuration.getStageDevice());
			toleranceField.setValue(10);
			
			stageDeviceLabel.setVisible(false);
			stageDeviceField.setVisible(false);
			toleranceLabel.setVisible(false);
			toleranceField.setVisible(false);
		}
	}

	private void loadStageDevices()
	{
    	String[] stageDevices;
    	try
		{
    		Device[] devices = server.getMicroscope().getStageDevices();
    		stageDevices = new String[devices.length]; 
    		for(int i=0; i<devices.length; i++)
    		{
    			stageDevices[i] = devices[i].getDeviceID();
    		}
		}
		catch (Exception e)
		{
			client.sendError("Could not obtain stage device names.", e);
			stageDevices = new String[0];
		}
		
		stageDeviceField.removeAllItems();
		for(String focusDevice : stageDevices)
		{
			stageDeviceField.addItem(focusDevice);
		}
	}
	
	private String getDefaultStage()
	{
		try
		{
			StageDevice device = server.getMicroscope().getStageDevice();
			if(device == null)
				return null;
			return device.getDeviceID();
		}
		catch(Exception e)
		{
			client.sendError("Could not determine default stage device.", e);
			return null;
		}
	}
	
	@Override
	public boolean saveData(UserControlMeasurementConfiguration configuration)
	{
		double tolerance = ((Number)toleranceField.getValue()).doubleValue();
		if(!monitorField.isSelected() || tolerance < 0)
		{
			configuration.setStageTolerance(-1);
		}
		else
		{
			configuration.setStageTolerance(tolerance);
			configuration.setStageDevice(stageDeviceField.getSelectedItem().toString());
		}
		
		return true;
	}

	@Override
	public void setToDefault(UserControlMeasurementConfiguration configuration)
	{
		configuration.setStageDevice(getDefaultStage());
	}

	@Override
	public String getPageName()
	{
		return "Stage Monitoring";
	}

	@Override
	public void createUI(YouScopeFrame frame)
	{
		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);

		GridBagConstraints newLineConstr = StandardFormats.getNewLineConstraint();
		GridBagConstraints bottomConstr = StandardFormats.getBottomContstraint();
		
		StandardFormats.addGridBagElement(monitorField, layout, newLineConstr, this);
		StandardFormats.addGridBagElement(stageDeviceLabel, layout, newLineConstr, this);
		StandardFormats.addGridBagElement(stageDeviceField, layout, newLineConstr, this);
		StandardFormats.addGridBagElement(toleranceLabel, layout, newLineConstr, this);
		StandardFormats.addGridBagElement(toleranceField, layout, newLineConstr, this);

		
		StandardFormats.addGridBagElement(new JPanel(), layout, bottomConstr, this);
		setBorder(new TitledBorder("Stage Monitoring"));
	}
}
