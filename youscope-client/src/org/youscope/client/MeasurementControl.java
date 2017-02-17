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
package org.youscope.client;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.EventListener;
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;

import org.youscope.addon.AddonException;
import org.youscope.addon.AddonMetadata;
import org.youscope.addon.AddonUI;
import org.youscope.addon.component.ComponentAddonUI;
import org.youscope.addon.component.ComponentAddonUIListener;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.clientinterfaces.YouScopeFrameListener;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.measurement.MeasurementConfiguration;
import org.youscope.common.measurement.MeasurementException;
import org.youscope.common.measurement.MeasurementListener;
import org.youscope.common.measurement.MeasurementState;
import org.youscope.common.saving.MeasurementFileLocations;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.ImageLoadingTools;
import org.youscope.uielements.LinkLabel;

/**
 * @author Moritz Lang
 */
class MeasurementControl
{
	private MeasurementConfiguration	configuration				= null;

	private final Measurement		measurement;

	private JTextField					measurementField;

	private MeasurementStateField					stateField;
	
	private JTextField runTimeField = new JTextField("0d 0h 0m 0s"); 
	
	private final MeasurementListenerImpl measurementListener = new MeasurementListenerImpl();
	private volatile long measurementStartTime = -1;
	private volatile long measurementPauseDuration = 0;
	
	private final Timer			runTimeTimer				= new Timer(1000, new ActionListener()
	{
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			if(getState() == MeasurementState.RUNNING && measurementStartTime>=0 && measurementPauseDuration>=0)
				setDuration(System.currentTimeMillis() - measurementStartTime - measurementPauseDuration);
			else
				runTimeTimer.stop();
		}
	});

	private final static Icon START_MEASUREMENT_ICON = ImageLoadingTools.getResourceIcon("icons/control.png", "start measurement");
	private final static String START_MEASUREMENT_TEXT = "Start";
	private final JButton startMeasurementButton;
	
	private final static Icon STOP_MEASUREMENT_ICON = ImageLoadingTools.getResourceIcon("icons/control-stop.png", "stop measurement");
	private final static String STOP_MEASUREMENT_TEXT = "Stop";
	private final JButton stopMeasurementButton;
	
	private final static Icon QUICK_STOP_MEASUREMENT_ICON = ImageLoadingTools.getResourceIcon("icons/control-skip.png", "quick stop measurement");
	private final static String QUICK_STOP_MEASUREMENT_TEXT = "Quick Stop";
	private final JButton quickStopMeasurementButton;
	
	private final static Icon SAVE_MEASUREMENT_ICON = ImageLoadingTools.getResourceIcon("icons/disk.png", "save measurement");
	private final static String SAVE_MEASUREMENT_TEXT = "Save";
	private final JButton saveMeasurementButton;
	
	private final static Icon EDIT_MEASUREMENT_ICON = ImageLoadingTools.getResourceIcon("icons/block--pencil.png", "edit measurement");
	private final static String EDIT_MEASUREMENT_TEXT = "Edit";
	private final JButton editMeasurementButton;
	
	private final static Icon PAUSE_MEASUREMENT_ICON = ImageLoadingTools.getResourceIcon("icons/control-pause.png", "pause measurement");
	private final static String PAUSE_MEASUREMENT_TEXT = "Pause";
	private final JButton pauseMeasurementButton;
	
	private final static Icon PROCESS_MEASUREMENT_ICON = ImageLoadingTools.getResourceIcon("icons/images-stack.png", "view measurement results");
	private final static String PROCESS_MEASUREMENT_TEXT = "View Results";
	private final JButton processMeasurementButton;
	
	private final static Icon EMERGENCY_STOP_ICON = ImageLoadingTools.getResourceIcon("icons/cross-button.png", "emergency stop");
	private final static String EMERGENCY_STOP_TEXT = "Emergency Stop";
	private final JButton emergencyStopButton;
	
	private volatile MeasurementState				state						= MeasurementState.READY;

	private MeasurementTree measurementTree = new MeasurementTree(this);
	
	private Vector<YouScopeFrame>		childFrames			= new Vector<YouScopeFrame>();
	private final MeasurementControlListener controlListener;
	
	private final DynamicPanel controlPanel = new DynamicPanel();
	private final DynamicPanel  informationPanel = new DynamicPanel();
	private JPanel imagingJobsPanel;
	
	private JPopupMenu measurementProcessorChooser;
	
	private String name;
	
	private boolean isDocked = true;

	MeasurementControl(MeasurementControlListener controlListener, Measurement measurement) throws RemoteException
	{
		startMeasurementButton = START_MEASUREMENT_ICON == null ? new JButton(START_MEASUREMENT_TEXT) : new JButton(START_MEASUREMENT_TEXT, START_MEASUREMENT_ICON);
		startMeasurementButton.setHorizontalAlignment(SwingConstants.LEFT);
		startMeasurementButton.setOpaque(false);
		startMeasurementButton.setToolTipText("Starts the measurement. If another measurement is already running, the measurement is queued and started as soon as the other measurement finished.");
		
		stopMeasurementButton = STOP_MEASUREMENT_ICON == null ? new JButton(STOP_MEASUREMENT_TEXT) : new JButton(STOP_MEASUREMENT_TEXT, STOP_MEASUREMENT_ICON);
		stopMeasurementButton.setHorizontalAlignment(SwingConstants.LEFT);
		stopMeasurementButton.setOpaque(false);
		stopMeasurementButton.setToolTipText("Stops the measurement. All jobs which are already due are executed before the measurement stops.");
		
		quickStopMeasurementButton = QUICK_STOP_MEASUREMENT_ICON == null ? new JButton(QUICK_STOP_MEASUREMENT_TEXT) : new JButton(QUICK_STOP_MEASUREMENT_TEXT, QUICK_STOP_MEASUREMENT_ICON);
		quickStopMeasurementButton.setHorizontalAlignment(SwingConstants.LEFT);
		quickStopMeasurementButton.setOpaque(false);
		quickStopMeasurementButton.setToolTipText("Stops the measurement. Only the currently executed job is finished. All other jobs which are due but not yet executed are discarded.");
		
		saveMeasurementButton = SAVE_MEASUREMENT_ICON == null ? new JButton(SAVE_MEASUREMENT_TEXT) : new JButton(SAVE_MEASUREMENT_TEXT, SAVE_MEASUREMENT_ICON);
		saveMeasurementButton.setHorizontalAlignment(SwingConstants.LEFT);
		saveMeasurementButton.setOpaque(false);
		saveMeasurementButton.setToolTipText("Saves the measurement configuration to the file system.");
		
		editMeasurementButton = EDIT_MEASUREMENT_ICON == null ? new JButton(EDIT_MEASUREMENT_TEXT) : new JButton(EDIT_MEASUREMENT_TEXT, EDIT_MEASUREMENT_ICON);
		editMeasurementButton.setHorizontalAlignment(SwingConstants.LEFT);
		editMeasurementButton.setOpaque(false);
		editMeasurementButton.setToolTipText("Edits the measurement configuration. The control for this measurement is closed when editing.");
		
		processMeasurementButton = PROCESS_MEASUREMENT_ICON == null ? new JButton(PROCESS_MEASUREMENT_TEXT) : new JButton(PROCESS_MEASUREMENT_TEXT, PROCESS_MEASUREMENT_ICON);
		processMeasurementButton.setHorizontalAlignment(SwingConstants.LEFT);
		processMeasurementButton.setOpaque(false);
		processMeasurementButton.setToolTipText("Allows to view or edit the images made during the current or the last execution of the measurement.");
		
		pauseMeasurementButton = PAUSE_MEASUREMENT_ICON == null ? new JButton(PAUSE_MEASUREMENT_TEXT) : new JButton(PAUSE_MEASUREMENT_TEXT, PAUSE_MEASUREMENT_ICON);
		pauseMeasurementButton.setHorizontalAlignment(SwingConstants.LEFT);
		pauseMeasurementButton.setOpaque(false);
		pauseMeasurementButton.setToolTipText("Pauses the current measurement. The currently running job is executed before pause. After pausing, the measurement can be resumed.");
		
		emergencyStopButton = EMERGENCY_STOP_ICON == null ? new JButton(EMERGENCY_STOP_TEXT) : new JButton(EMERGENCY_STOP_TEXT, EMERGENCY_STOP_ICON);
		emergencyStopButton.setHorizontalAlignment(SwingConstants.LEFT);
		emergencyStopButton.setOpaque(false);
		emergencyStopButton.setToolTipText("Tries to interrupt the measurement as quick as possible without caring about finishing jobs or applying shutdown settings. Furthermore, locks the microscope such that all attempts to control it via YouScope fail until the emergency stop is manually resetted.");
		
		this.measurement = measurement;
		try {
			this.configuration = measurement.getMetadata().getConfiguration();
		} catch (@SuppressWarnings("unused") ConfigurationException e) {
			this.configuration = null;
		}
		this.controlListener = controlListener;
		
		// Add measurement listener
		measurement.addMeasurementListener(measurementListener);
		// Setup appearance
		setupUIElements();
	}
	
	public String getName()
	{
		return name;
	}
	public void initializeWideLayout(Container container)
	{	
		JPanel dockLabelPanel = new JPanel(new BorderLayout());
		dockLabelPanel.setOpaque(false);
		LinkLabel dockLabel = new LinkLabel("dock window");
		dockLabel.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					if(controlListener != null)
						controlListener.dockWindow();
				}
			});
		dockLabelPanel.add(dockLabel, BorderLayout.EAST);
		
		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.setOpaque(false);
		centerPanel.add(imagingJobsPanel, BorderLayout.CENTER);
		centerPanel.add(informationPanel, BorderLayout.NORTH);
		
		container.setLayout(new BorderLayout());
		container.add(dockLabelPanel, BorderLayout.NORTH);
		container.add(controlPanel, BorderLayout.EAST);
		container.add(centerPanel, BorderLayout.CENTER);
		
		isDocked = false;
	}
	public void initializeTightLayout(JPanel container)
	{
		JPanel undockLabelPanel = new JPanel(new BorderLayout());
		undockLabelPanel.setOpaque(false);
		LinkLabel undockLabel = new LinkLabel("undock window");
		undockLabel.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					if(controlListener != null)
						controlListener.undockWindow();
				}
			});
		undockLabelPanel.add(undockLabel, BorderLayout.EAST);
		
		DynamicPanel mainPanel = new DynamicPanel();
		mainPanel.add(informationPanel);
		mainPanel.add(controlPanel);
		mainPanel.addFill(imagingJobsPanel);
		
		container.setLayout(new BorderLayout());
		container.add(undockLabelPanel, BorderLayout.NORTH);
		container.add(mainPanel, BorderLayout.CENTER);
		container.setPreferredSize(new Dimension(200, 200));
		
		isDocked = true;
	}
	
	public void setClosed()
	{
		if((state == MeasurementState.QUEUED || state == MeasurementState.RUNNING) && configuration != null)
		{
			JOptionPane.showMessageDialog(null, "The measurement is currently running or queued and will not be stopped from excecution although this window is closed.\nYou can reopen the window with the help of the measurement manager (Tools -> Measurement Manager).", "Closing active measurement", JOptionPane.INFORMATION_MESSAGE);
		}
		while(childFrames.size() > 0)
		{
			childFrames.get(0).setVisible(false);
		}
		if(controlListener != null)
			controlListener.measurementControlClosed();
		try {
			measurement.removeMeasurementListener(measurementListener);
		} catch (@SuppressWarnings("unused") RemoteException e) {
			// do nothing, just more work for the garbage collector...
		}
	}
	public interface MeasurementControlListener extends EventListener
	{
		public void measurementControlClosed();
		public void dockWindow();
		public void undockWindow();
	}
	
	public YouScopeFrame createChildFrame()
	{
		final YouScopeFrame childFrame = YouScopeFrameImpl.createTopLevelFrame();
		childFrame.addFrameListener(new YouScopeFrameListener()
		{
			@Override
			public void frameClosed()
			{
				childFrames.remove(childFrame);
			}

			@Override
			public void frameOpened()
			{
				childFrames.add(childFrame);
			}

		});
		return childFrame;
	}
	
	public void addChildFrame(final YouScopeFrame childFrame)
	{
		childFrame.addFrameListener(new YouScopeFrameListener()
		{
			@Override
			public void frameClosed()
			{
				childFrames.remove(childFrame);
			}

			@Override
			public void frameOpened()
			{
				childFrames.add(childFrame);
			}

		});
	}

	private void setupUIElements() throws RemoteException
	{
		if(configuration != null)
		{
			saveMeasurementButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					YouScopeClientImpl.saveMeasurement(configuration);
				}
			});
			
			editMeasurementButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					if(state == MeasurementState.READY || state == MeasurementState.UNINITIALIZED || state == MeasurementState.ERROR)
					{
						ComponentAddonUI<? extends MeasurementConfiguration> addon;
						try {
							addon = ClientAddonProviderImpl.getProvider().createComponentUI(configuration.getTypeIdentifier(), MeasurementConfiguration.class);
						} catch (AddonException e1) {
							ClientSystem.err.println("Cannot create measurement configuration UI.", e1);
							return;
						}
						addon.addUIListener(new ComponentAddonUIListener<MeasurementConfiguration>()
							{
								@Override
								public void configurationFinished(MeasurementConfiguration configuration) 
								{
									YouScopeClientImpl.addMeasurement(configuration);
								}
							});
						try
						{
							addon.setConfiguration(configuration);
						}
						catch(Exception e)
						{
							ClientSystem.err.println("Cannot load measurement configuration.", e);
							return;
						}
						YouScopeFrame newFrame;
						try {
							newFrame = addon.toFrame();
						} catch (AddonException e) {
							ClientSystem.err.println("Cannot create measurement configuration UI.", e);
							return;
						}
						newFrame.setVisible(true);
						setClosed();
					}
				}
			});
		}
		else
		{
			editMeasurementButton.setEnabled(false);
			saveMeasurementButton.setEnabled(false);
		}
		
		informationPanel.setBorder(new TitledBorder("Measurement Information"));
		informationPanel.setOpaque(false);
		name = measurement.getName();
		informationPanel.add(new JLabel("Name:"));
		measurementField = new JTextField(name);
		measurementField.setEditable(false);
		informationPanel.add(measurementField);
		informationPanel.add(new JLabel("Status:"));
		stateField = new MeasurementStateField();
		this.state = measurement.getState();
		actualizeStateInternal();
		informationPanel.add(stateField);
		informationPanel.add(new JLabel("Duration:"));
		runTimeField.setEditable(false);
		informationPanel.add(runTimeField);

		// Initialize Buttons
		startMeasurementButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				startMeasurement();
			}
		});
		
		
		measurementProcessorChooser = new JPopupMenu();
		processMeasurementButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	if(isDocked)
                		measurementProcessorChooser.show(processMeasurementButton, -measurementProcessorChooser.getPreferredSize().width, 0);
                	else
                		measurementProcessorChooser.show(processMeasurementButton, processMeasurementButton.getWidth(), 0);
                }
            });
		
		List<AddonMetadata> postProcessorMetadata = ClientAddonProviderImpl.getProvider().getPostProcessorMetadata();
		
		for(AddonMetadata postProcessorMetadate : postProcessorMetadata)
		{
			try {
				measurementProcessorChooser.add(new StartProcessorMenuItem(postProcessorMetadate));
			} catch (@SuppressWarnings("unused") AddonException e1) {
				continue;
			}
		
		}
		
		stopMeasurementButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				stopMeasurement(true);
			}
		});
		
		pauseMeasurementButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				pauseMeasurement();
			}
		});
		quickStopMeasurementButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				stopMeasurement(false);
			}
		});
		emergencyStopButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				emergencyStop();
			}
		});
		
		controlPanel.setBorder(new TitledBorder("Measurement Control"));
		controlPanel.setOpaque(false);
		controlPanel.add(startMeasurementButton);
		controlPanel.add(pauseMeasurementButton);
		controlPanel.add(stopMeasurementButton);
		controlPanel.add(quickStopMeasurementButton);
		controlPanel.addEmpty();
		controlPanel.add(processMeasurementButton);
		controlPanel.add(editMeasurementButton);
		controlPanel.add(saveMeasurementButton);
		controlPanel.addEmpty();
		controlPanel.add(emergencyStopButton);		
		controlPanel.addFillEmpty();

		// Initialize imaging jobs / measurement tree
		imagingJobsPanel = new JPanel(new BorderLayout());
		imagingJobsPanel.setBorder(new TitledBorder("Produced Images:"));
		imagingJobsPanel.setOpaque(false);
		imagingJobsPanel.add(new JScrollPane(measurementTree), BorderLayout.CENTER);
		measurementTree.setMeasurement(measurement);
	}
	private MeasurementState getState()
	{
		return state;
	}
	private synchronized void setState(final MeasurementState state)
	{
		if(state == this.state)
			return;
		this.state = state;
		if(SwingUtilities.isEventDispatchThread())
			actualizeStateInternal();
		else
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				
				@Override
				public void run()
				{
					actualizeStateInternal();
				}
			});
		}
	}
	private boolean showMeasurementProcessors()
	{
		return measurementProcessorChooser.getComponentCount() > 0 && ClientSystem.isLocalServer();
	}
	private void actualizeStateInternal()
	{
		MeasurementState state = this.state;
		// Set behavior.
		switch(state)
		{
			case UNINITIALIZED:
				if(showMeasurementProcessors())
					processMeasurementButton.setVisible(true);
				else
					processMeasurementButton.setVisible(false);
				startMeasurementButton.setVisible(true);
				editMeasurementButton.setVisible(true);
				stopMeasurementButton.setVisible(false);
				quickStopMeasurementButton.setVisible(false);
				pauseMeasurementButton.setVisible(false);
				saveMeasurementButton.setVisible(true);
				break;
			case READY:
				processMeasurementButton.setVisible(false);
				startMeasurementButton.setVisible(true);
				editMeasurementButton.setVisible(true);
				stopMeasurementButton.setVisible(false);
				quickStopMeasurementButton.setVisible(false);
				pauseMeasurementButton.setVisible(false);
				saveMeasurementButton.setVisible(true);
				break;
			case PAUSED:
				if(showMeasurementProcessors())
					processMeasurementButton.setVisible(true);
				else
					processMeasurementButton.setVisible(false);
				startMeasurementButton.setVisible(true);
				editMeasurementButton.setVisible(false);
				stopMeasurementButton.setVisible(false);
				quickStopMeasurementButton.setVisible(false);
				pauseMeasurementButton.setVisible(false);
				saveMeasurementButton.setVisible(false);
				break;
			case RUNNING:
				if(showMeasurementProcessors())
					processMeasurementButton.setVisible(true);
				else
					processMeasurementButton.setVisible(false);
				startMeasurementButton.setVisible(false);
				editMeasurementButton.setVisible(false);
				stopMeasurementButton.setVisible(true);
				quickStopMeasurementButton.setVisible(true);
				pauseMeasurementButton.setVisible(true);
				saveMeasurementButton.setVisible(false);
				break;
			case ERROR:
				processMeasurementButton.setVisible(false);
				startMeasurementButton.setVisible(false);
				editMeasurementButton.setVisible(true);
				stopMeasurementButton.setVisible(false);
				quickStopMeasurementButton.setVisible(false);
				pauseMeasurementButton.setVisible(false);
				saveMeasurementButton.setVisible(true);
				break;
			case QUEUED:
				processMeasurementButton.setVisible(false);
				startMeasurementButton.setVisible(false);
				editMeasurementButton.setVisible(false);
				stopMeasurementButton.setVisible(true);
				quickStopMeasurementButton.setVisible(false);
				pauseMeasurementButton.setVisible(false);
				saveMeasurementButton.setVisible(false);
				break;
			default:
				// INITIALIZING, INITIALIZED, STOPPING, STOPPED, UNINITIALIZING
				processMeasurementButton.setVisible(false);
				startMeasurementButton.setVisible(false);
				editMeasurementButton.setVisible(false);
				stopMeasurementButton.setVisible(false);
				quickStopMeasurementButton.setVisible(false);
				pauseMeasurementButton.setVisible(false);
				saveMeasurementButton.setVisible(false);
				break;
		}
		// Set state information field.
		stateField.setState(state);
		
		// Set duration updating
		if(state == MeasurementState.RUNNING)
		{
			try
			{
				measurementStartTime = measurement.getStartTime();
				measurementPauseDuration = measurement.getPauseDuration();
				if(measurementStartTime >= 0)
					runTimeTimer.restart();
				else
					ClientSystem.err.println("Could not get measurement start time. Won't update measurement runtime.");
			}
			catch(RemoteException e)
			{
				ClientSystem.err.println("Could not get measurement start time and pause duration. Won't update measurement runtime.", e);
			}
		}
		else
		{
			try
			{
				runTimeTimer.stop();
				long runtime =measurement.getRuntime();
				if(runtime >= 0)
					setDuration(runtime);
			}
			catch(RemoteException e)
			{
				ClientSystem.err.println("Could not get measurement run time", e);
			}
		}
	}

	private void setDuration(long duration)
	{
		long seconds = duration / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;
		long days = hours / 24;
		
		seconds = seconds % 60;
		minutes = minutes % 60;
		hours = hours % 24;
		runTimeField.setText(Long.toString(days)+"d "+Long.toString(hours)+"h " + Long.toString(minutes)+"m " + Long.toString(seconds)+"s");
	}

	private void stopMeasurement(final boolean processJobQueue)
	{
		setState(MeasurementState.STOPPING);
		
		try
		{
			measurement.stopMeasurement(processJobQueue);
		}
		catch(RemoteException | MeasurementException e)
		{
			ClientSystem.err.println("Could not stop measurement.", e);
			setState(MeasurementState.ERROR);
		}
	
	}
	
	private void pauseMeasurement()
	{
		setState(MeasurementState.PAUSING);
		
		try
		{
			measurement.pauseMeasurement();
		}
		catch(RemoteException | MeasurementException e)
		{
			ClientSystem.err.println("Could not pause measurement.", e);
			setState(MeasurementState.ERROR);
		}
	
	}

	private void emergencyStop()
	{
		try
		{
			YouScopeClientImpl.getServer().emergencyStop();
		}
		catch(RemoteException e)
		{
			ClientSystem.err.println("Could not set microscope to emergency-stop state. Stop microscope manually!", e);
		}
		setState(MeasurementState.ERROR);
	}

	

	private void startMeasurement()
	{
		try
		{
			// Start measurement
			measurement.startMeasurement();
		}
		catch(RemoteException | MeasurementException e)
		{
			ClientSystem.err.println("Could not start measurement.", e);
			setState(MeasurementState.ERROR);
		}

	}

	private class MeasurementListenerImpl extends UnicastRemoteObject implements MeasurementListener
	{

		/**
		 * Serial version UID.
		 */
		private static final long	serialVersionUID	= -3162707630761616684L;

		/**
		 * Constructor.
		 * 
		 * @throws RemoteException
		 */
		private MeasurementListenerImpl() throws RemoteException
		{
			super();
		}		

		@Override
		public void measurementStructureModified() throws RemoteException 
		{
			refreshMeasurementTree();
		}

		@Override
		public void measurementStateChanged(MeasurementState oldState, MeasurementState newState)
				throws RemoteException {
			setState(newState);
		}

		@Override
		public void measurementError(Exception e) throws RemoteException {
			ClientSystem.err.println("Error in measurement " + measurement.getName() + " occured.", e);
			
		}
	}
	
	public void refreshMeasurementTree() {
        if(measurement != null)
            measurementTree.setMeasurement(measurement);
    }
	
	private class StartProcessorMenuItem extends JMenuItem implements ActionListener
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -1089398454827661984L;
		private final AddonMetadata addonMetadata;
		StartProcessorMenuItem(AddonMetadata addonMetadata) throws AddonException
		{
			super(addonMetadata.getName());
			this.addonMetadata = addonMetadata;
			addActionListener(this);
		}
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			if(measurement == null)
				return;
			
			MeasurementFileLocations measurementFileLocations;
			try
			{
				measurementFileLocations = measurement.getSaver().getLastMeasurementFileLocations();
			}
			catch(RemoteException e)
			{
				ClientSystem.err.println("Could not obtain measurement save options from measurement.", e);
				return;
			}
			try
			{
				AddonUI<?> addon = ClientAddonProviderImpl.getProvider().createPostProcessorUI(addonMetadata, measurementFileLocations);
				YouScopeFrame frame = addon.toFrame();
				frame.setVisible(true);
			}
			catch(AddonException e)
			{
				ClientSystem.err.println("Could not start measurement post processor.", e);
			}
		}
	}
}
