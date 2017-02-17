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
package org.youscope.plugin.onix;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.rmi.RemoteException;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.youscope.addon.AddonException;
import org.youscope.addon.tool.ToolAddonUIAdapter;
import org.youscope.addon.tool.ToolMetadata;
import org.youscope.addon.tool.ToolMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrameListener;
import org.youscope.common.MessageListener;
import org.youscope.common.util.RMIReader;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DoubleTextField;
import org.youscope.uielements.ImageLoadingTools;
import org.youscope.uielements.IntegerTextField;
import org.youscope.uielements.StandardFormats;
import org.youscope.uielements.StateButton;
import org.youscope.uielements.scripteditor.ScriptEditor;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

/**
 * Controller tool for the CellAsic Onix microfluidic device.
 * @author Moritz Lang
 *
 */
class OnixController extends ToolAddonUIAdapter implements YouScopeFrameListener
{
	private OnixAddon onix = null;
	
	private final StateButton connectedField = new StateButton("Connection Status");
	private final StateButton onField = new StateButton("On/Off");
	private final StateButton plateSealedField = new StateButton("Plate sealing");
	private final StateButton vacuumReadyField = new StateButton("Vacuum ready");
	private final StateButton unknown1Field = new StateButton("Unknown 1");
	private final StateButton unknown2Field = new StateButton("Unknown 2");
	
	private final DoubleTextField xPressureSetpointField = new DoubleTextField(0.0);
	private final DoubleTextField yPressureSetpointField = new DoubleTextField(0.0);
	private final JTextField xPressureField = new JTextField("0.25");
	private final JTextField yPressureField = new JTextField("0.25");
	
	final static String PROPERTY_PROTOCOL = "YouScope.Onix.LastProtocol";
	
	private final JButton switchButton = new JButton("Switch");
	
	private final JButton runProtocolButton = new JButton("Run protocol");
	
	private final StateButton protocolRunningField = new StateButton("Protocol running");
	private final ScriptEditor protocolArea = new ScriptEditor();	
	
	private boolean everythingOK = false;
	
	private final JCheckBox[] valveButtons = new JCheckBox[8];
	
	private boolean continueQuery = true;
	
	private final StateButton pwmxRunningField = new StateButton("X-PWM Running");
	private final IntegerTextField pwmxPeriodField = new IntegerTextField(1000);
	private final DoubleTextField pwmxFractionField = new DoubleTextField(0.5);
	private final JButton pwmxStartField = new JButton("Start");
	private final JButton pwmxStopField = new JButton("Stop");
	
	private final StateButton pwmyRunningField = new StateButton("Y-PWM Running");
	private final IntegerTextField pwmyPeriodField = new IntegerTextField(1000);
	private final DoubleTextField pwmyFraction3Field = new DoubleTextField(0.5);
	private final DoubleTextField pwmyFraction4Field = new DoubleTextField(0.5);
	private final DoubleTextField pwmyFraction5Field = new DoubleTextField(0.0);
	private final DoubleTextField pwmyFraction6Field = new DoubleTextField(0.0);
	private final JButton pwmyStartField = new JButton("Start");
	private final JButton pwmyStopField = new JButton("Stop");
	
	public final static String TYPE_IDENTIFIER = "YouScope.OnixController";
	
	static ToolMetadata getMetadata()
	{
		return new ToolMetadataAdapter(TYPE_IDENTIFIER, "Onix Controller", new String[]{"microfluidics"}, 
				"Allows to direclty set the flow rates/pressures of an Onix microfluidic device. Provides an option for pulse-width modulation to continuously mix media.", "icons/beaker.png");
	}
	
	private final MessageListener onixListener = new MessageListener()
	{
		@Override
		public void sendMessage(String message) throws RemoteException 
		{
			OnixController.this.sendErrorMessage(message, null);
		}

		@Override
		public void sendErrorMessage(String message, Throwable exception) throws RemoteException
		{
			OnixController.this.sendErrorMessage(message, exception);
		}		
	};
	
	/**
	 * Constructor.
	 * @param client Interface to the YouScope client.
	 * @param server Interface to the YouScope server.
	 * @throws AddonException 
	 */
	public OnixController(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(), client, server);
	}
	/**
	 * Runnable to query the onix device for its current state.
	 */
	private Runnable onixStateActualizer = new Runnable()
	{
		private boolean actualizeAll = true;
		@Override
		public void run()
		{
			while(continueQuery)
			{
				boolean connected;
				boolean on;
				boolean plateSealed;
				boolean vacuumReady;
				boolean unknown1;
				boolean unknown2;
				float xPressure = 0f;
				float yPressure = 0f;
				boolean protocolRunning;
				boolean pwmxRunning;
				boolean pwmyRunning;
				boolean[] valveOn = new boolean[8];
				float xPressureSetpoint;
				float yPressureSetpoint;
				try
				{
					connected = onix.isConnected();
					on = onix.isOn();
					plateSealed = onix.isPlateSealed();
					vacuumReady = onix.isVacuumReady();
					protocolRunning = onix.isProtocolRunning();
					unknown1 = onix.isUnknown1Alright();
					unknown2 = onix.isUnknown2Alright();
					xPressure = onix.getXPressure();
					yPressure = onix.getYPressure();
					pwmxRunning = onix.isPWMX();
					pwmyRunning = onix.isPWMY();
					xPressureSetpoint = onix.getXPressureSetpoint();
					yPressureSetpoint = onix.getYPressureSetpoint();
					
					for(int i=0; i<valveOn.length; i++)
					{
						valveOn[i] = onix.isValve(i);
					}
				}
				catch(Exception e)
				{
					sendErrorMessage("Could not actualize state of Onix microfluidic device. Stoping querying.", e);
					continueQuery = false;
					connected = false;
					on = false;
					plateSealed = false;
					vacuumReady = false;
					unknown1 = false;
					unknown2 = false;
					protocolRunning = false;
					pwmxRunning = false;
					pwmyRunning = false;
					
					xPressureSetpoint = 3;
					yPressureSetpoint = 3;
					
					for(int i=0; i<valveOn.length; i++)
					{
						valveOn[i] = false;
					}
				}
				
				// Correct state
				if(!connected || !on)
				{
					plateSealed = false;
					vacuumReady = false;
					unknown1 = false;
					unknown2 = false;
				}
				else if(!plateSealed)
				{
					vacuumReady = false;
				}
				
				try
				{
					SwingUtilities.invokeAndWait(new DesignUpdater(actualizeAll, connected, on, plateSealed, vacuumReady, unknown1, unknown2, Float.toString(xPressure), Float.toString(yPressure), protocolRunning, pwmxRunning, pwmyRunning, valveOn, xPressureSetpoint, yPressureSetpoint));
				}
				catch(Exception e)
				{
					sendErrorMessage("Could not actualize window with new state. Stoping querying.", e);
					continueQuery = false;
				}
				
				actualizeAll = false;
				
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
		if(onix != null)
		{
			try
			{
				onix.removeMessageListener(onixListener);
			}
			catch(RemoteException e)
			{
				sendErrorMessage("Could not remove Onix message listener.", e);
			}
		}
	}

	@Override
	public void frameOpened()
	{
		// do nothing.
	}
	
	/**
	 * Runnable for the Swing thread to update UI elements, when state has changed.
	 * @author Moritz Lang
	 *
	 */
	private class DesignUpdater implements Runnable
	{
		private final boolean connected;
		private final boolean on;
		private final boolean plateSealed;
		private final boolean vacuumReady;
		private final boolean unknown1;
		private final boolean unknown2;
		private final boolean actualizeAll;
		private final String xPressure;
		private final String yPressure;
		private final boolean protocolRunning;
		private final boolean pwmxRunning;
		private final boolean pwmyRunning;
		private final boolean[] valveOn;
		
		private final float xPressureSetpoint;
		private final float yPressureSetpoint;
		public DesignUpdater(boolean actualizeAll, boolean connected, boolean on, boolean plateSealed, boolean vacuumReady, boolean unknown1, boolean unknown2, String xPressure, String yPressure, boolean protocolRunning, boolean pwmxRunning, boolean pwmyRunning, boolean[] valveOn, float xPressureSetpoint, float yPressureSetpoint)
		{
			this.pwmxRunning = pwmxRunning;
			this.protocolRunning = protocolRunning;
			this.xPressure = xPressure;
			this.yPressure = yPressure;
			this.actualizeAll = actualizeAll;
			this.connected = connected;
			this.on = on;
			this.plateSealed = plateSealed;
			this.vacuumReady = vacuumReady;
			this.unknown1 = unknown1;
			this.unknown2 = unknown2;
			this.pwmyRunning = pwmyRunning;
			this.valveOn = valveOn;
			
			this.xPressureSetpoint = xPressureSetpoint;
			this.yPressureSetpoint = yPressureSetpoint;
		}
		@Override
		public void run()
		{
			if(actualizeAll || protocolRunning != protocolRunningField.isActive())
			{
				runProtocolButton.setText(protocolRunning ? "Stop Protocol" : "Run Protocol");
			}
			
			if(actualizeAll || !xPressure.equals(xPressureField.getText()))
			{
				xPressureField.setText(xPressure);
			}
			if(actualizeAll || !yPressure.equals(yPressureField.getText()))
			{
				yPressureField.setText(yPressure);
			}
			
			connectedField.setActive(connected);
			onField.setActive(on);
			plateSealedField.setActive(plateSealed);
			vacuumReadyField.setActive(vacuumReady);
			unknown1Field.setActive(unknown1);
			unknown2Field.setActive(unknown2);
			protocolRunningField.setActive(protocolRunning);
			pwmxRunningField.setActive(pwmxRunning);
			pwmyRunningField.setActive(pwmyRunning);
			
			if(!xPressureSetpointField.isFocusOwner())
				xPressureSetpointField.setValue(xPressureSetpoint);
			if(!yPressureSetpointField.isFocusOwner())
				yPressureSetpointField.setValue(yPressureSetpoint);
			
			for(int i=0; i<valveOn.length; i++)
			{
				valveButtons[i].setSelected(valveOn[i]);
			}
			
			boolean everythingOK = connected && on && plateSealed && !protocolRunning;
			if(actualizeAll || everythingOK != OnixController.this.everythingOK)
			{
				OnixController.this.everythingOK = everythingOK;
				xPressureSetpointField.setEditable(everythingOK);
				yPressureSetpointField.setEditable(everythingOK);
				switchButton.setEnabled(everythingOK);
				pwmxStartField.setEnabled(everythingOK);
				pwmyStartField.setEnabled(everythingOK);
				for(int i=0; i<2; i++)
				{
					valveButtons[i].setEnabled(everythingOK);
				}
				for(int i=2; i<valveButtons.length; i++)
				{
					valveButtons[i].setEnabled(everythingOK);
				}
				
				runProtocolButton.setEnabled(protocolRunning || everythingOK);
			}
		}
	}
	
	private class ValveActionListener implements ActionListener
	{
		private final int valveNum;
		private final JCheckBox valveButton;
		public ValveActionListener(JCheckBox valveButton, int valveNum)
		{
			this.valveButton = valveButton;
			this.valveNum = valveNum;
		}
		@Override
		public void actionPerformed(ActionEvent e)
		{
			try
			{
				onix.setValve(valveNum, valveButton.isSelected());
			}
			catch(Exception e1)
			{
				sendErrorMessage("Could not set state of valve " + Integer.toString(valveNum+1) + " to " + (valveButton.isSelected() ? "on" : "off"), e1);
			}
		}
	}
	
	@Override
	public java.awt.Component createUI() throws AddonException
	{
		setMaximizable(true);
		setResizable(true);
		setTitle("Onix Controller");
		getContainingFrame().addFrameListener(this);
	
		// get onix addon.
		try
		{
			onix = getServer().getProperties().getServerAddon(OnixAddon.class);
			onix.addMessageListener(onixListener);
			onix.initialize();
		}
		catch(Exception e1)
		{
			throw new AddonException("Could not load onix control addon", e1);
		}
		if(onix == null)
		{
			throw new AddonException("Could not load onix control addon", null);
		}			
		
		// Grid Bag Layouts
		GridBagConstraints newLineConstr = StandardFormats.getNewLineConstraint();
		GridBagConstraints bottomConstr = StandardFormats.getBottomContstraint();
		
		// State Panel
        JPanel statePanel = new JPanel(new GridLayout(3,3,5,5));
        statePanel.add(connectedField);
        statePanel.add(onField);
        statePanel.add(plateSealedField);
        statePanel.add(vacuumReadyField);
        statePanel.add(unknown1Field);
        statePanel.add(unknown2Field);
        
        // Button panel
    	JPanel buttonPanel = new JPanel(new GridLayout(3,1,5,5));
        JButton reconnectButton = new JButton("Reconnect");
        reconnectButton.addActionListener(new ActionListener()
        {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					onix.reconnect();
				}
				catch(Exception e1)
				{
					sendErrorMessage("Could not reconnect to Onix.", e1);
				}
			}
        });
        buttonPanel.add(reconnectButton);
        
        switchButton.addActionListener(new ActionListener()
        {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					onix.setSwitch();
				}
				catch(Exception e1)
				{
					sendErrorMessage("Onix could not switch.", e1);
				}
			}
        });
        switchButton.setEnabled(false);
        buttonPanel.add(switchButton);
    	
        JPanel downPanel = new JPanel(new BorderLayout());
        downPanel.add(statePanel, BorderLayout.WEST);
        downPanel.add(buttonPanel, BorderLayout.EAST);
        downPanel.setBorder(new TitledBorder("System Status"));
        
        // X-PANEL
        GridBagLayout xLayout = new GridBagLayout();
        JPanel xPanel = new JPanel(xLayout);
        xPanel.setOpaque(false);
        
        JPanel xPressurePanel = new JPanel(new GridLayout(2,2,5,5));
        xPressurePanel.setOpaque(false);
        xPressurePanel.add(new JLabel("Setpoint (0.25-10.0 psi):"));
        xPressureSetpointField.setMaximalValue(10);
        xPressureSetpointField.setMinimalValue(0.25);
        xPressureSetpointField.addActionListener(new ActionListener()
        {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					onix.setXPressureSetpoint(xPressureSetpointField.getValue().floatValue());
				}
				catch(Exception e1)
				{
					sendErrorMessage("Could not set x-pressure to " + xPressureSetpointField.getValue().toString() + " psi.", e1);
				}
			}
        });
        xPressureSetpointField.setEditable(false);
        xPressureSetpointField.setOpaque(false);
        xPressurePanel.add(xPressureSetpointField);
        xPressurePanel.add(new JLabel("Current pressure (psi):"));
        xPressureField.setEditable(false);
        xPressureField.setOpaque(false);
        xPressurePanel.add(xPressureField);
    	xPressurePanel.setBorder(new TitledBorder("X-Pressure Settings"));
    	StandardFormats.addGridBagElement(xPressurePanel, xLayout, newLineConstr, xPanel);
    	
    	// Valve panel
    	for(int i=0; i<valveButtons.length; i++)
    	{
    		valveButtons[i] = new JCheckBox("Valve " + Integer.toString(i+1));
    		valveButtons[i].addActionListener(new ValveActionListener(valveButtons[i], i));
    		valveButtons[i].setEnabled(false);
    	}
    	
    	JPanel xValvesPanel = new JPanel(new GridLayout(1, 2, 5, 5));
    	xValvesPanel.setOpaque(false);
    	for(int i=0; i < 2; i++)
    	{
    		valveButtons[i].setOpaque(false);
    		xValvesPanel.add(valveButtons[i]);
    	}
    	xValvesPanel.setBorder(new TitledBorder("X-Valves"));
    	StandardFormats.addGridBagElement(xValvesPanel, xLayout, newLineConstr, xPanel);
    	JPanel tempPanel = new JPanel();
    	tempPanel.setOpaque(false);
    	StandardFormats.addGridBagElement(tempPanel, xLayout, bottomConstr, xPanel);
    	
    	// Y-PANEL
        GridBagLayout yLayout = new GridBagLayout();
        JPanel yPanel = new JPanel(yLayout);
        yPanel.setOpaque(false);
    	
        JPanel yPressurePanel = new JPanel(new GridLayout(2,2,5,5));
        yPressurePanel.setOpaque(false);
        yPressurePanel.add(new JLabel("Setpoint (0.25-10.0 psi):"));
        yPressureSetpointField.setOpaque(false);
        yPressureSetpointField.setMaximalValue(10);
        yPressureSetpointField.setMinimalValue(0.0);
        yPressureSetpointField.setEditable(false);
        yPressureSetpointField.addActionListener(new ActionListener()
        {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					onix.setYPressureSetpoint(yPressureSetpointField.getValue().floatValue());
				}
				catch(Exception e1)
				{
					sendErrorMessage("Could not set y-pressure to " + yPressureSetpointField.getValue().toString() + " psi.", e1);
				}
			}
        });
        yPressurePanel.add(yPressureSetpointField);
        yPressurePanel.add(new JLabel("Current pressure (psi):"));
        yPressureField.setOpaque(false);
        yPressureField.setEditable(false);
        yPressurePanel.add(yPressureField);
    	yPressurePanel.setBorder(new TitledBorder("Y-Pressure Settings"));
    	StandardFormats.addGridBagElement(yPressurePanel, yLayout, newLineConstr, yPanel);
        
    	JPanel yValvesPanel = new JPanel(new GridLayout(3, 2, 5, 5));
    	yValvesPanel.setOpaque(false);
    	for(int i=2; i < 8; i++)
    	{
    		valveButtons[i].setOpaque(false);
    		yValvesPanel.add(valveButtons[i]);
    	}
    	yValvesPanel.setBorder(new TitledBorder("Y-Valves"));
    	StandardFormats.addGridBagElement(yValvesPanel, yLayout, newLineConstr, yPanel);
    	tempPanel = new JPanel();
    	tempPanel.setOpaque(false);
    	StandardFormats.addGridBagElement(tempPanel, yLayout, bottomConstr, yPanel);
    	
    	
    	// PWM panel
    	GridBagLayout pwmLayout = new GridBagLayout();
        JPanel pwmPanel = new JPanel(pwmLayout);
        pwmPanel.setOpaque(false);
	        
    	JPanel pwmxPanel = new JPanel(new GridLayout(4,2,5,5));
    	pwmxPanel.setOpaque(false);
        pwmxPanel.add(new JLabel("Pulse Period (ms):"));
    	pwmxPeriodField.setMinimalValue(10);
    	pwmxPeriodField.setOpaque(false);
    	pwmxPanel.add(pwmxPeriodField);
    	pwmxPanel.add(new JLabel("Fraction Valve 1 (0-1):"));
    	pwmxFractionField.setMinimalValue(0);
    	pwmxFractionField.setMaximalValue(1);
    	pwmxFractionField.setOpaque(false);
    	pwmxPanel.add(pwmxFractionField);
    	pwmxPanel.add(pwmxRunningField);
    	tempPanel = new JPanel();
    	tempPanel.setOpaque(false);
    	pwmxPanel.add(tempPanel);
    	pwmxStartField.setOpaque(false);
    	pwmxStartField.addActionListener(new ActionListener()
    	{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					onix.startPWMX(pwmxPeriodField.getValue(), pwmxFractionField.getValue());
				}
				catch(Exception e1)
				{
					sendErrorMessage("Could not start pulse-width-modulation for valves 1 and 2.", e1);
				}
			}
    	});
    	pwmxPanel.add(pwmxStartField);
    	pwmxStopField.setOpaque(false);
    	pwmxStopField.addActionListener(new ActionListener()
    	{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					onix.stopPWMX();
				}
				catch(Exception e1)
				{
					sendErrorMessage("Could not stop pulse-width-modulation for valves 1 and 2.", e1);
				}
			}
    	});
    	pwmxPanel.add(pwmxStopField);
    	pwmxPanel.setBorder(new TitledBorder("X-Pulse-Width-Modulation"));
    	StandardFormats.addGridBagElement(pwmxPanel, pwmLayout, newLineConstr, pwmPanel);
    	
    	JPanel pwmyPanel = new JPanel(new GridLayout(7,2,5,5));
    	pwmyPanel.setOpaque(false);
        pwmyPanel.add(new JLabel("Pulse Period (ms):"));
        pwmyPeriodField.setOpaque(false);
        pwmyPeriodField.setMinimalValue(10);
    	pwmyPanel.add(pwmyPeriodField);
    	
    	pwmyPanel.add(new JLabel("Media 3 Fraction [0-1]:"));
    	pwmyFraction3Field.setOpaque(false);
    	pwmyFraction3Field.setMinimalValue(0);
    	pwmyFraction3Field.setMaximalValue(1);
    	pwmyPanel.add(pwmyFraction3Field);
    	
    	pwmyPanel.add(new JLabel("Media 4 Fraction [0-1]:"));
    	pwmyFraction4Field.setOpaque(false);
    	pwmyFraction4Field.setMinimalValue(0);
    	pwmyFraction4Field.setMaximalValue(1);
    	pwmyPanel.add(pwmyFraction4Field);
    	
    	pwmyPanel.add(new JLabel("Media 5 Fraction [0-1]:"));
    	pwmyFraction5Field.setOpaque(false);
    	pwmyFraction5Field.setMinimalValue(0);
    	pwmyFraction5Field.setMaximalValue(1);
    	pwmyPanel.add(pwmyFraction5Field);
    	
    	pwmyPanel.add(new JLabel("Media 6 Fraction [0-1]:"));
    	pwmyFraction6Field.setOpaque(false);
    	pwmyFraction6Field.setMinimalValue(0);
    	pwmyFraction6Field.setMaximalValue(1);
    	pwmyPanel.add(pwmyFraction6Field);
    	
    	pwmyPanel.add(pwmyRunningField);
    	tempPanel = new JPanel();
    	tempPanel.setOpaque(false);
    	pwmyPanel.add(tempPanel);
    	pwmyStartField.setOpaque(false);
    	pwmyStartField.addActionListener(new ActionListener()
    	{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					onix.startPWMY(pwmyPeriodField.getValue(), pwmyFraction3Field.getValue(), pwmyFraction4Field.getValue(), pwmyFraction5Field.getValue(), pwmyFraction6Field.getValue());
				}
				catch(Exception e1)
				{
					sendErrorMessage("Could not start pulse-width-modulation for Y-Valves.", e1);
				}
			}
    	});
    	pwmyPanel.add(pwmyStartField);
    	pwmyStopField.setOpaque(false);
    	pwmyStopField.addActionListener(new ActionListener()
    	{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					onix.stopPWMY();
				}
				catch(Exception e1)
				{
					sendErrorMessage("Could not stop pulse-width-modulation for Y-Valves.", e1);
				}
			}
    	});
    	pwmyPanel.add(pwmyStopField);
    	pwmyPanel.setBorder(new TitledBorder("Y-Pulse-Width-Modulation"));
    	StandardFormats.addGridBagElement(pwmyPanel, pwmLayout, newLineConstr, pwmPanel);
    	tempPanel = new JPanel();
    	tempPanel.setOpaque(false);
    	StandardFormats.addGridBagElement(tempPanel, pwmLayout, bottomConstr, pwmPanel);
    	
    	// north-west image panel
    	Icon onixIcon = ImageLoadingTools.getResourceIcon("org/youscope/plugin/onix/images/onix.jpg", "Onix Plate");
		JLabel imageLabel = null;
		if(onixIcon != null)
		{
			imageLabel = new JLabel(onixIcon, SwingConstants.CENTER);
			//imageLabel.setBackground(Color.WHITE);
			imageLabel.setOpaque(false);
			//imageLabel.setBorder(new LineBorder(Color.BLACK, 1));
		}
		
		//X/Y Panel
		JPanel xyPanel = new JPanel(new GridLayout(1,3,5,5));
		xyPanel.setOpaque(false);
		xyPanel.add(xPanel);
		xyPanel.add(yPanel);
		xyPanel.add(pwmPanel);
		
		// west panel
		JPanel westPanel = new JPanel(new BorderLayout());
		westPanel.setOpaque(false);
		if(imageLabel != null)
			westPanel.add(imageLabel, BorderLayout.NORTH);
		westPanel.add(xyPanel, BorderLayout.CENTER);
        
		// protocol panel
		GridBagLayout protocolLayout = new GridBagLayout();
        JPanel protocolPanel = new JPanel(protocolLayout);
        protocolPanel.setOpaque(false);
        protocolArea.setScriptStyleID("YouScope.ScriptStyle.Onix");
        protocolArea.setText("% Cell loading \n"
        		+ "close all\n"
        		+ "setflow Y 8\n"
        		+ "open V8\n" 
        		+ "wait 0.08\n"
        		+ "close V8\n"
        		+ "\n"
        		+ "end");
        
        StandardFormats.addGridBagElement(protocolArea, protocolLayout, bottomConstr, protocolPanel);
        
        JPanel loadSavePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        loadSavePanel.setOpaque(false);
        JButton loadButton = new JButton("Load");
        loadButton.setOpaque(false);
        loadSavePanel.add(loadButton);
        loadButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent arg0)
                {
                	String lastProtocol = getClient().getPropertyProvider().getProperty(PROPERTY_PROTOCOL, "onix/protocol.onix");
                    JFileChooser fileChooser = new JFileChooser(lastProtocol);
                    
                    String filterDesc = "ONIX Protocol (.onix)";
                    fileChooser.addChoosableFileFilter(new FileNameExtensionFilter(filterDesc, new String[]{".onix"}));
                    fileChooser.setSelectedFile(new File(lastProtocol));      
                    
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
                    
                    getClient().getPropertyProvider().setProperty(PROPERTY_PROTOCOL, file.toString());
                    
                    BufferedReader reader = null;
                    String protocol = "";
                    try
					{
						reader = new BufferedReader(new FileReader(file));
						while(true)
						{
							String line = reader.readLine();
							if(line == null)
								break;
							protocol += line + "\n";
						}
					}
					catch(Exception e1)
					{
						sendErrorMessage("Could not load Onix protocol " + file.toString()+ ".", e1);
						return;
					}
					finally
					{
						if(reader != null)
						{
							try
							{
								reader.close();
							}
							catch(Exception e1)
							{
								sendErrorMessage("Could not close protocol " + file.toString()+ ".", e1);
							}
						}						
					}
					
					protocolArea.setText(protocol);
                }
            });
        
        JButton saveButton = new JButton("Save");
        saveButton.setOpaque(false);
        loadSavePanel.add(saveButton);
        saveButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
            	String lastProtocol = getClient().getPropertyProvider().getProperty(PROPERTY_PROTOCOL, "onix/protocol.onix");
                JFileChooser fileChooser = new JFileChooser(lastProtocol);
                String filterDesc = "ONIX Protocol (.onix)";
                fileChooser.addChoosableFileFilter(new FileNameExtensionFilter(filterDesc, new String[]{".onix"}));
                fileChooser.setSelectedFile(new File(lastProtocol)); 
                                   
                File file;
                while(true)
                {
                	int returnVal = fileChooser.showDialog(null, "Save");
                	if (returnVal != JFileChooser.APPROVE_OPTION)
                	{
                		return;
                	}
                	file = fileChooser.getSelectedFile().getAbsoluteFile();
                	if(file.exists())
                	{
                		returnVal = JOptionPane.showConfirmDialog(null, "File " + file.toString() + " does already exist.\nOverwrite?", "File does already exist", JOptionPane.YES_NO_OPTION);
                		if(returnVal == JOptionPane.YES_OPTION)
                			break;
                	}
                	else
                		break;
                }
                
                getClient().getPropertyProvider().setProperty(PROPERTY_PROTOCOL, file.toString());
                
                String text = protocolArea.getText();
        		try
        		{
        			PrintStream fileStream = new PrintStream(file);
        			fileStream.print(text);
        			fileStream.close();
        		}
        		catch(Exception e)
        		{
        			sendErrorMessage("Could not save file.", e);
        			return;
        		}
            }
        });
        StandardFormats.addGridBagElement(loadSavePanel, protocolLayout, newLineConstr, protocolPanel);
        
        runProtocolButton.setEnabled(false);
        runProtocolButton.setOpaque(false);
        runProtocolButton.addActionListener(new ActionListener()
        {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(protocolRunningField.isActive())
				{
					try
					{
						onix.stopProtocol();
					}
					catch(Exception e1)
					{
						sendErrorMessage("Could not stop execution of Onix protocol.", e1);
					}
				}
				else
				{
					RMIReader rmiReader = null;
					try
					{
						rmiReader = new RMIReader(new StringReader(protocolArea.getText()));
						onix.runProtocol(rmiReader);
					}
					catch(Exception e1)
					{
						sendErrorMessage("Could not execute Onix protocol.", e1);
					}
					finally
					{
						if(rmiReader != null)
						{
							try
							{
								rmiReader.close();
							}
							catch(Exception e1)
							{
								sendErrorMessage("Could not close protocol stream.", e1);
							}
						}						
					}
				}
			}
        });
        protocolRunningField.setOpaque(false);
        StandardFormats.addGridBagElement(protocolRunningField, protocolLayout, newLineConstr, protocolPanel);
        StandardFormats.addGridBagElement(runProtocolButton, protocolLayout, newLineConstr, protocolPanel);
		
        // central panel
        JTabbedPane centralPanel = new JTabbedPane(JTabbedPane.TOP);
        centralPanel.addTab("Direct Control", westPanel);
        centralPanel.addTab("Protocol", protocolPanel);
        			
		// End initializing
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(downPanel, BorderLayout.SOUTH);
		contentPane.add(centralPanel, BorderLayout.CENTER);
		
		new Thread(onixStateActualizer).start();
		
		return contentPane;
	}
}
