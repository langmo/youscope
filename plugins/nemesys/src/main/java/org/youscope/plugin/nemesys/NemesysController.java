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
/**
 * 
 */
package org.youscope.plugin.nemesys;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import org.youscope.addon.AddonException;
import org.youscope.addon.tool.ToolAddonUIAdapter;
import org.youscope.addon.tool.ToolMetadata;
import org.youscope.addon.tool.ToolMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrameListener;
import org.youscope.common.microscope.Device;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DoubleTextField;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.StateButton;

/**
 * Controller tool for the Nemesys syringe control device.
 * @author Moritz Lang
 *
 */
class NemesysController extends ToolAddonUIAdapter implements YouScopeFrameListener
{
	private Device nemesysDevice = null;
	private final JComboBox<String> nemesysDeviceField = new JComboBox<String>();	
	private final JPanel syringeFieldsContainer = new JPanel();
	private boolean continueQuery = true;
	private SyringeField[] syringeFields = new SyringeField[0]; 
	
	public final static String TYPE_IDENTIFIER = "YouScope.NemesysController";
	
	static ToolMetadata getMetadata()
	{
		return new ToolMetadataAdapter(TYPE_IDENTIFIER, "Nemesys Controller", new String[]{"microfluidics"}, "Allows to directly set the flow rates of a Nemesys syringe pump system.", "icons/beaker.png");
	}
	
	private class SyringeField extends DynamicPanel
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -7754207045299953183L;
		private final StateButton stateField = new StateButton("Initializing...");
		private final JTextField nameField = new JTextField(); 
		private final DoubleTextField flowRateField = new DoubleTextField(0.0);
		private final JLabel currentFlowRateLabel = new JLabel("unknown");
		private final JLabel maxFlowRateLabel = new JLabel("unknown");
		SyringeField(final int syringeID, String name)
		{
			JPanel elementsPanel = new JPanel(new GridLayout(4,2));
			
			elementsPanel.add(new JLabel("Identifier:"));
			nameField.setText(name);
			nameField.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					try
					{
						nemesysDevice.getProperty("syringe" + Integer.toString(syringeID+1) + ".identifier").setValue(nameField.getText());
					}
					catch(Exception e1)
					{
						sendErrorMessage("Could not set syringe name.", e1);
					}
				}
			});
			elementsPanel.add(nameField);
			
			elementsPanel.add(new JLabel("Current flow-rate:"));
			elementsPanel.add(currentFlowRateLabel);
			
			elementsPanel.add(new JLabel("Max. flow-rate:"));
			elementsPanel.add(maxFlowRateLabel);
			
			elementsPanel.add(new JLabel("Flow-rate setpoint:"));
			elementsPanel.add(flowRateField);
			
			add(elementsPanel);
			addFillEmpty();
			
			setBorder(new TitledBorder("Syringe " + Integer.toString(syringeID + 1)));
			
			flowRateField.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					setFlowRate(syringeID, flowRateField.getValue());
				}
			});
			add(stateField);	
		}
		private void actualizeFields(final String flowUnit, final double currentFlowRate, final double maxFlowRate, final boolean state)
		{
			if(SwingUtilities.isEventDispatchThread())
			{
				currentFlowRateLabel.setText(Double.toString(currentFlowRate) + " " + flowUnit);
				maxFlowRateLabel.setText(Double.toString(maxFlowRate) + " " + flowUnit);
				stateField.setActive(state);
				stateField.setText(state ? "Syringe ready" : "Syringe disabled/in error state");
			}
			else
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						currentFlowRateLabel.setText(Double.toString(currentFlowRate) + " " + flowUnit);
						maxFlowRateLabel.setText(Double.toString(maxFlowRate) + " " + flowUnit);
						stateField.setActive(state);
						stateField.setText(state ? "Syringe ready" : "Syringe disabled/in error state");
					}
				});
			}
		}
	}
	
	private void setFlowRate(int dosingUnit, double flowRate)
	{
		Device nemesysDevice = this.nemesysDevice;
		if(nemesysDevice == null)
		{
			sendErrorMessage("Cannot set flow rate since Nemesys device not set.", null);
			return;
		}
		try
		{
			nemesysDevice.getProperty("syringe" + Integer.toString(dosingUnit+1) + ".flowRate").setValue(Double.toString(flowRate));
		}
		catch(Exception e)
		{
			sendErrorMessage("Could not set flow rate.", e);
		}
	}
	
	/**
	 * Constructor.
	 * @param client Interface to the YouScope client.
	 * @param server Interface to the YouScope server.
	 * @throws AddonException 
	 */
	public NemesysController(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(), client, server);
	}
	/**
	 * Runnable to query the Nemesys device for its current state.
	 */
	private Runnable nemesysStateActualizer = new Runnable()
	{
		@Override
		public void run()
		{
			SyringeField[] syringeFields;
			Device nemesysDevice;
			outerActualizationLoop: while(continueQuery)
			{
				// make local copy of variables
				synchronized(NemesysController.this)
				{
					syringeFields = NemesysController.this.syringeFields;
					nemesysDevice = NemesysController.this.nemesysDevice;
				}
				if(nemesysDevice != null)
				{
					for(int i=0; i<syringeFields.length; i++)
					{
						double currentFlowRate;
						String flowUnit;
						double maxFlowRate;
						boolean state;
						try
						{
							currentFlowRate = Double.parseDouble(nemesysDevice.getProperty("syringe" + Integer.toString(i+1) + ".flowRate").getValue());
							flowUnit = nemesysDevice.getProperty("syringe" + Integer.toString(i+1) + ".flowUnit").getValue();
							maxFlowRate = Double.parseDouble(nemesysDevice.getProperty("syringe" + Integer.toString(i+1) + ".flowRateMax").getValue());
							state = nemesysDevice.getProperty("syringe" + Integer.toString(i+1) + ".state").getValue().equals("1");
						}
						catch(Exception e)
						{
							sendErrorMessage("Error while obtaining Nemesys state. Stoping actualizing fields", e);
							continueQuery = false;
							break outerActualizationLoop;
						}
						syringeFields[i].actualizeFields(flowUnit, currentFlowRate, maxFlowRate, state);
					}
				}
				
				try
				{
					Thread.sleep(500);
				}
				catch(InterruptedException e)
				{
					sendErrorMessage("State updater interrupted. Stoping querying.", e);
					continueQuery = false;
				}
			}
		}
		
	};
	
	@Override
	public void frameClosed()
	{
		continueQuery=false;
	}

	@Override
	public void frameOpened()
	{
		new Thread(nemesysStateActualizer).start();
	}
	
	@Override
	public java.awt.Component createUI()
	{
		setMaximizable(false);
		setResizable(false);
		setTitle("Nemesys Controller");
		getContainingFrame().addFrameListener(this);
	
		DynamicPanel mainPanel = new DynamicPanel();
		try
		{
			Device[] devices = getMicroscope().getDevices();
			for(Device device : devices)
			{
				if(device.getDriverID().equals("Nemesys") && device.getLibraryID().equals("NemesysPump"))
				{
					nemesysDeviceField.addItem(device.getDeviceID());
				}
			}
		}
		catch(Exception e)
		{
			sendErrorMessage("Could not load Nemesys device IDs.", e);
		}
		nemesysDeviceField.setOpaque(false);
		nemesysDeviceField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				nemesysDeviceChanged();
				notifyLayoutChanged();
			}
		});
		mainPanel.add(new JLabel("Nemesys Device:"));
		mainPanel.add(nemesysDeviceField);
		mainPanel.add(syringeFieldsContainer);
		
		nemesysDeviceChanged();
		return mainPanel;
	}
	
	private synchronized void nemesysDeviceChanged()
	{
		String nemesysDeviceName = nemesysDeviceField.getSelectedItem() == null ? null : nemesysDeviceField.getSelectedItem().toString();
		int numDosingUnits;
		if(nemesysDeviceName != null)
		{
			try
			{
				nemesysDevice = getMicroscope().getDevice(nemesysDeviceName);
				numDosingUnits = Integer.parseInt(nemesysDevice.getProperty("numDosingUnits").getValue());
			}
			catch(NumberFormatException e)
			{
				nemesysDevice = null;
				numDosingUnits = 0;
				sendErrorMessage("Could not parse number of volume or flow rate units.", e);
				return;
			}
			catch(Exception e)
			{
				nemesysDevice = null;
				numDosingUnits = 0;
				sendErrorMessage("Could not obtain number of dosing units.", e);
				return;
			}
		}
		else
		{
			nemesysDevice = null;
			numDosingUnits = 0;
		}
		syringeFields = new SyringeField[numDosingUnits];
		syringeFieldsContainer.removeAll();
		syringeFieldsContainer.setLayout(new GridLayout(1, numDosingUnits));
		for(int i=0; i<numDosingUnits; i++)
		{
			String syringeName = "";
			try
			{
				syringeName = nemesysDevice.getProperty("syringe" + Integer.toString(i+1) + ".identifier").getValue();
			}
			catch(Exception e)
			{
				sendErrorMessage("Could not get name of syringe " + Integer.toString(i+1) + ".", e);
			}
			syringeFields[i] = new SyringeField(i, syringeName);
			syringeFieldsContainer.add(syringeFields[i]);
		}
		syringeFieldsContainer.revalidate();
	}
}
