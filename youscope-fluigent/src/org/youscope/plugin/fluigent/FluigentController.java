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
package org.youscope.plugin.fluigent;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
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
 * Controller tool for the Fluigent pump device.
 * @author Moritz Lang
 *
 */
class FluigentController extends ToolAddonUIAdapter implements YouScopeFrameListener
{
	private Device fluigentDevice = null;
	private final JComboBox<String> fluigentDeviceField = new JComboBox<String>();	
	private boolean continueQuery = true;
	private PumpControl pumpControl = null;
	private PressureControl pressureControl = null;
	private JTabbedPane centralPanel = null;
	
	public final static String TYPE_IDENTIFIER = "YouScope.FluigentController";
	
	static ToolMetadata getMetadata()
	{
		return new ToolMetadataAdapter(TYPE_IDENTIFIER, "Fluigent Controller", new String[]{"microfluidics"}, 
				"User interface to change medium flow rates of a Fluigent pump system.",
				"icons/beaker.png");
	}
	
	private class PressureControl extends DynamicPanel
	{

		/**
		 * Serial Version UID.
		 */
		private static final long serialVersionUID = 4730441367404452391L;
		private final PressureField[] pressureFields; 
		
		PressureControl(int numEZPressureUnits)
		{
			JPanel pressureFieldsContainer = new JPanel(new GridLayout((numEZPressureUnits+1)/2, numEZPressureUnits>1?2:1));
			pressureFieldsContainer.setOpaque(false);
			pressureFields = new PressureField[numEZPressureUnits];
			for(int i=0; i<numEZPressureUnits; i++)
			{
				String pressureUnitName = "";
				String pressureUnitUnit = "AU";
				try
				{
					pressureUnitName = fluigentDevice.getProperty("ezPressureUnit" + Integer.toString(i+1) + ".identifier").getValue();
				}
				catch(Exception e)
				{
					sendErrorMessage("Could not get name of flow unit " + Integer.toString(i+1) + ".", e);
				}
				pressureFields[i] = new PressureField(i, pressureUnitName, pressureUnitUnit);
				pressureFieldsContainer.add(pressureFields[i]);
			}
			addFill(pressureFieldsContainer);
		}
		
		private class PressureField extends DynamicPanel
		{
			/**
			 * Serial Version UID.
			 */
			private static final long	serialVersionUID	= -7754217045211153181L;
			private final JTextField nameField = new JTextField(); 
			private final DoubleTextField pressureField = new DoubleTextField(0.0);
			private final JLabel currentPressureLabel = new JLabel("unknown");
			PressureField(final int pressureID, String name, String pressureUnitpressureUnit)
			{
				JPanel elementsPanel = new JPanel(new GridLayout(3,2));
				elementsPanel.setOpaque(false);
				elementsPanel.add(new JLabel("Identifier:"));
				nameField.setText(name);
				nameField.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						try
						{
							fluigentDevice.getProperty("ezPressureUnit" + Integer.toString(pressureID+1) + ".identifier").setValue(nameField.getText());
						}
						catch(Exception e1)
						{
							sendErrorMessage("Could not set ez pressure unit name.", e1);
						}
					}
				});
				elementsPanel.add(nameField);
				
				elementsPanel.add(new JLabel("Current pressure (" + pressureUnitpressureUnit + "):"));
				elementsPanel.add(currentPressureLabel);
				
				elementsPanel.add(new JLabel("Pressure setpoint  (" + pressureUnitpressureUnit + "):"));
				elementsPanel.add(pressureField);
				
				add(elementsPanel);
				addFillEmpty();
				
				setBorder(new TitledBorder("EZ Pressure Unit " + Integer.toString(pressureID + 1)));
				
				pressureField.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent arg0)
					{
						setPressure(pressureID, pressureField.getValue());
					}
				});
			}
			private void actualizeFields(final double currentPressure)
			{
				if(SwingUtilities.isEventDispatchThread())
				{
					currentPressureLabel.setText(Double.toString(currentPressure));
				}
				else
				{
					SwingUtilities.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							currentPressureLabel.setText(Double.toString(currentPressure));
						}
					});
				}
			}
		}
		public void actualizePressures(double[] pressures)
		{
			for(int i=0; i<pressures.length && i<pressureFields.length;i++)
			{
				pressureFields[i].actualizeFields(pressures[i]);
			}
		}
		private void setPressure(int pressureUnit, double pressure)
		{
			Device fluigentDevice = FluigentController.this.fluigentDevice;
			if(fluigentDevice == null)
			{
				sendErrorMessage("Cannot set pressure since Fluigent device not set.", null);
				return;
			}
			try
			{
				fluigentDevice.getProperty("ezPressureUnit" + Integer.toString(pressureUnit+1) + ".pressureSetPoint").setValue(Double.toString(pressure));
			}
			catch(Exception e)
			{
				sendErrorMessage("Could not set pressure.", e);
			}
		}
		public int getNumPressureUnits()
		{
			return pressureFields.length;
		}
	}
	
	private class PumpControl extends DynamicPanel
	{
		/**
		 * Serial Version UID.
		 */
		private static final long serialVersionUID = 8786005680193510544L;
		private final PumpField[] pumpFields; 
		private final StateButton flowReachableField = new StateButton("Initializing...");
		
		private static final String PROPERTY_LAST_IDENTIFICATION_FILE ="YouScope.Fluigent.LastIdentificationFile";
		
		PumpControl(int numFlowUnits)
		{
			JPanel pumpFieldsContainer = new JPanel(new GridLayout(1, numFlowUnits));
			pumpFieldsContainer.setOpaque(false);
			pumpFields = new PumpField[numFlowUnits];
			for(int i=0; i<numFlowUnits; i++)
			{
				String flowUnitName = "";
				String flowUnitRateUnit = "unknown";
				try
				{
					flowUnitName = fluigentDevice.getProperty("flowUnit" + Integer.toString(i+1) + ".identifier").getValue();
					flowUnitRateUnit = fluigentDevice.getProperty("flowUnit" + Integer.toString(i+1) + ".flowRateUnit").getValue();
				}
				catch(Exception e)
				{
					sendErrorMessage("Could not get name of flow unit " + Integer.toString(i+1) + ".", e);
				}
				pumpFields[i] = new PumpField(i, flowUnitName, flowUnitRateUnit);
				pumpFieldsContainer.add(pumpFields[i]);
			}
			addFill(pumpFieldsContainer);
			add(flowReachableField);
			
			JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			buttonsPanel.setOpaque(false);
			JButton runWizard = new JButton("Run Identification Wizard");
			runWizard.setOpaque(false);
			runWizard.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0) 
				{
					Device fluigentDevice = FluigentController.this.fluigentDevice;
					if(fluigentDevice == null)
						sendErrorMessage("No Fluigent device selected", null);
					try {
						fluigentDevice.getProperty("startIdentificationWizard").setValue("start");
					} 
					catch (Exception  e) {
						sendErrorMessage("Could not start Fluigent advanced identification wizard.",e);
					}
				}
			});
			buttonsPanel.add(runWizard);
			
			JButton loadIdentification = new JButton("Load Identification");
			loadIdentification.setOpaque(false);
			loadIdentification.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0) 
				{
					Device fluigentDevice = FluigentController.this.fluigentDevice;
					if(fluigentDevice == null)
						sendErrorMessage("No Fluigent device selected", null);

					String lastIdentFile = getClient().getPropertyProvider().getProperty(PROPERTY_LAST_IDENTIFICATION_FILE, "");
                    JFileChooser fileChooser = new JFileChooser(lastIdentFile);
                    fileChooser.setSelectedFile(new File(lastIdentFile)); 
                    
                    File file;
                    while(true)
                    {
                    	int returnVal = fileChooser.showDialog(null, "Load");
                    	if (returnVal != JFileChooser.APPROVE_OPTION)
                    	{
                    		return;
                    	}
                    	file = fileChooser.getSelectedFile().getAbsoluteFile();
                    	if(!file.exists())
                    	{
                    		JOptionPane.showMessageDialog(null, "File " + file.toString() + " does not exist.\nPlease select an existing file.", "File does not exist", JOptionPane. INFORMATION_MESSAGE);
                    	}
                    	else
                    		break;
                    }
                    String path;
					try {
						path = file.getCanonicalPath();
					} catch (@SuppressWarnings("unused") IOException e) {
						path = file.getAbsolutePath();
					}
					getClient().getPropertyProvider().setProperty(PROPERTY_LAST_IDENTIFICATION_FILE, path);
                    
                    try {
						fluigentDevice.getProperty("IdentificationFilePath").setValue(path);
					} catch (Exception e) {
						sendErrorMessage("Could not load Fluigent identification file.",e);
					}
				}
			});
			buttonsPanel.add(loadIdentification);
			
			add(buttonsPanel);
			
		}
		public void actualizeReachable(boolean reachable)
		{
			flowReachableField.setActive(reachable);
			flowReachableField.setText(reachable ? "Flow reachable" : "Flow not reachable");
		}
		public void actualizeFlowRates(double[] flowRates)
		{
			for(int i=0; i<flowRates.length && i<pumpFields.length;i++)
			{
				pumpFields[i].actualizeFields(flowRates[i]);
			}
		}
		public int getNumFlowUnits()
		{
			return pumpFields.length;
		}
		
		private class PumpField extends DynamicPanel
		{
			/**
			 * Serial Version UID.
			 */
			private static final long	serialVersionUID	= -7754207045299953181L;
			private final JTextField nameField = new JTextField(); 
			private final DoubleTextField flowRateField = new DoubleTextField(0.0);
			private final JLabel currentFlowRateLabel = new JLabel("unknown");
			PumpField(final int syringeID, String name, String flowUnitRateUnit)
			{
				JPanel elementsPanel = new JPanel(new GridLayout(3,2));
				elementsPanel.setOpaque(false);
				elementsPanel.add(new JLabel("Identifier:"));
				nameField.setText(name);
				nameField.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						try
						{
							fluigentDevice.getProperty("flowUnit" + Integer.toString(syringeID+1) + ".identifier").setValue(nameField.getText());
						}
						catch(Exception e1)
						{
							sendErrorMessage("Could not set pump name.", e1);
						}
					}
				});
				elementsPanel.add(nameField);
				
				elementsPanel.add(new JLabel("Current flow-rate (" + flowUnitRateUnit + "):"));
				elementsPanel.add(currentFlowRateLabel);
				
				elementsPanel.add(new JLabel("Flow-rate setpoint  (" + flowUnitRateUnit + "):"));
				elementsPanel.add(flowRateField);
				
				add(elementsPanel);
				addFillEmpty();
				
				setBorder(new TitledBorder("Flow Rate Unit " + Integer.toString(syringeID + 1)));
				
				flowRateField.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent arg0)
					{
						setFlowRate(syringeID, flowRateField.getValue());
					}
				});
			}
			private void actualizeFields(final double currentFlowRate)
			{
				if(SwingUtilities.isEventDispatchThread())
				{
					currentFlowRateLabel.setText(Double.toString(currentFlowRate));
				}
				else
				{
					SwingUtilities.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							currentFlowRateLabel.setText(Double.toString(currentFlowRate));
						}
					});
				}
			}
		}
		
		private void setFlowRate(int dosingUnit, double flowRate)
		{
			Device fluigentDevice = FluigentController.this.fluigentDevice;
			if(fluigentDevice == null)
			{
				sendErrorMessage("Cannot set flow rate since Fluigent device not set.", null);
				return;
			}
			try
			{
				fluigentDevice.getProperty("flowUnit" + Integer.toString(dosingUnit+1) + ".flowRateSetPoint").setValue(Double.toString(flowRate));
			}
			catch(Exception e)
			{
				sendErrorMessage("Could not set flow rate.", e);
			}
		}
	}
	
	/**
	 * Constructor.
	 * @param client Interface to the YouScope client.
	 * @param server Interface to the YouScope server.
	 * @throws AddonException 
	 */
	public FluigentController(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(), client, server);
	}
	/**
	 * Runnable to query the Fluigent device for its current state.
	 */
	private Runnable fluigentStateActualizer = new Runnable()
	{
		@Override
		public void run()
		{
			PumpControl pumpControl;
			PressureControl pressureControl;
			Device fluigentDevice;
			outerActualizationLoop: while(continueQuery)
			{
				// make local copy of variables
				synchronized(FluigentController.this)
				{
					pumpControl = FluigentController.this.pumpControl;
					fluigentDevice = FluigentController.this.fluigentDevice;
					pressureControl = FluigentController.this.pressureControl;
				}
				if(fluigentDevice != null)
				{
					if(pumpControl != null)
					{
						double[] flowRates = new double[pumpControl.getNumFlowUnits()];
						for(int i=0; i<flowRates.length; i++)
						{
							try
							{
								flowRates[i] = Double.parseDouble(fluigentDevice.getProperty("flowUnit" + Integer.toString(i+1) + ".currentFlowRate").getValue());
							}
							catch(Exception e)
							{
								sendErrorMessage("Error while obtaining Fluigent device state. Stoping actualizing fields", e);
								continueQuery = false;
								break outerActualizationLoop;
							}
						}
						pumpControl.actualizeFlowRates(flowRates);
						
						boolean reachable;
						try {
							reachable = fluigentDevice.getProperty("flowReachable").getValue().equals("1");
						}
						catch(Exception e)
						{
							sendErrorMessage("Error while querrying if Fluigent flow is reachable. Stoping actualizing fields", e);
							continueQuery = false;
							break outerActualizationLoop;
						}
						pumpControl.actualizeReachable(reachable);
					}	
				}
				if(pressureControl != null)
				{
					double[] pressures = new double[pressureControl.getNumPressureUnits()];
					for(int i=0; i<pressures.length; i++)
					{
						try
						{
							pressures[i] = Double.parseDouble(fluigentDevice.getProperty("ezPressureUnit" + Integer.toString(i+1) + ".currentPressure").getValue());
						}
						catch(Exception e)
						{
							sendErrorMessage("Error while obtaining Fluigent device state. Stoping actualizing fields", e);
							continueQuery = false;
							break outerActualizationLoop;
						}
					}
					pressureControl.actualizePressures(pressures);
				}	
			
				try
				{
					Thread.sleep(500);
				}
				catch(InterruptedException e)
				{
					sendErrorMessage("Fluigent state updater interrupted. Stoping querying.", e);
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
		new Thread(fluigentStateActualizer).start();
	}
	
	@Override
	public java.awt.Component createUI()
	{
		setMaximizable(false);
		setResizable(false);
		setTitle("Fluigent Controller");
		getContainingFrame().addFrameListener(this);
	
		DynamicPanel mainPanel = new DynamicPanel();
		try
		{
			Device[] devices = getMicroscope().getDevices();
			for(Device device : devices)
			{
				if(device.getDriverID().equals("Fluigent") && device.getLibraryID().equals("FluigentPump"))
				{
					fluigentDeviceField.addItem(device.getDeviceID());
				}
			}
		}
		catch(Exception e)
		{
			sendErrorMessage("Could not load Fluigent device IDs.", e);
		}
		fluigentDeviceField.setOpaque(false);
		fluigentDeviceField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				fluigentDeviceChanged();
				centralPanel.setComponentAt(0, pumpControl);
				centralPanel.setComponentAt(1, pressureControl);
				centralPanel.revalidate();
				notifyLayoutChanged();
			}
		});
		if(fluigentDeviceField.getItemCount() > 1)
		{
			mainPanel.add(new JLabel("Fluigent Device:"));
			mainPanel.add(fluigentDeviceField);
		}
		fluigentDeviceChanged();
		centralPanel = new JTabbedPane(JTabbedPane.TOP);
        centralPanel.addTab("Flow Unit Control", pumpControl);
        centralPanel.addTab("EZ Pressure Unit Control", pressureControl);
		
		mainPanel.addFill(centralPanel);
		return mainPanel;
	}
	
	private synchronized void fluigentDeviceChanged()
	{
		String fluigentDeviceName = fluigentDeviceField.getSelectedItem() == null ? null : fluigentDeviceField.getSelectedItem().toString();
		int numFlowUnits;
		int numEZPressureUnits;
		if(fluigentDeviceName != null)
		{
			try
			{
				fluigentDevice = getMicroscope().getDevice(fluigentDeviceName);
				numFlowUnits = Integer.parseInt(fluigentDevice.getProperty("numFlowUnits").getValue());
				numEZPressureUnits = Integer.parseInt(fluigentDevice.getProperty("numEZPressureUnits").getValue());
				
			}
			catch(NumberFormatException e)
			{
				fluigentDevice = null;
				numFlowUnits = 0;
				numEZPressureUnits = 0;
				sendErrorMessage("Could not parse number of flow units.", e);
				return;
			}
			catch(Exception e)
			{
				fluigentDevice = null;
				numFlowUnits = 0;
				numEZPressureUnits = 0;
				sendErrorMessage("Could not obtain number of flow units.", e);
				return;
			}
		}
		else
		{
			fluigentDevice = null;
			numFlowUnits = 0;
			numEZPressureUnits = 0;
		}
		
		pumpControl = new PumpControl(numFlowUnits);
		pressureControl = new PressureControl(numEZPressureUnits);
	}
}
