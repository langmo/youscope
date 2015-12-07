/**
 * 
 */
package org.youscope.client;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;
import java.util.EventListener;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;

import org.youscope.addon.AddonException;
import org.youscope.addon.AddonUI;
import org.youscope.addon.component.ComponentAddonUI;
import org.youscope.addon.component.ComponentAddonUIListener;
import org.youscope.addon.measurement.MeasurementAddonFactory;
import org.youscope.addon.postprocessing.PostProcessorAddonFactory;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.clientinterfaces.YouScopeFrameListener;
import org.youscope.common.configuration.MeasurementConfiguration;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.measurement.MeasurementListener;
import org.youscope.common.measurement.MeasurementState;
import org.youscope.uielements.LinkLabel;
import org.youscope.uielements.StandardFormats;

/**
 * @author Moritz Lang
 */
class MeasurementControl
{
	private MeasurementConfiguration	configuration				= null;

	private Measurement		measurement;

	private JTextField					measurementField;

	private MeasurementStateField					stateField;
	
	private JTextField runTimeField = new JTextField("0d 0h 0m 0s"); 
	private Date startTime = null;
	private volatile Timer			runTimeTimer				= null;

	private JButton						measurementResultsButton		= new JButton("<html><head></head><body><p style=\"text-align:center;font-weight:bold;color:#008800\">Measurement<br>Results</p></body></html>");
	
	private JButton						startMeasurementButton		= new JButton("<html><head></head><body><p style=\"text-align:center;font-weight:bold;color:#008800\">Start<br>Measurement</p></body></html>");

	private JButton						stopMeasurementButton		= new JButton("<html><head></head><body><p style=\"text-align:center;font-weight:bold;color:#555500\">Stop<br>Measurement</p></body></html>");

	private JButton						quickStopMeasurementButton	= new JButton("<html><head></head><body><p style=\"text-align:center;font-weight:bold;color:#555500\">Quick Stop<br>Measurement</p></body></html>");

	private JButton						emergencyStopButton			= new JButton("<html><head></head><body><p style=\"text-align:center;font-weight:bold;color:#AA0000\">Emergency<br>Stop</p></body></html>");

	private JButton						saveMeasurementButton		= new JButton("<html><head></head><body><p style=\"text-align:center\">Save<br>Measurement</p></body></html>");

	private JButton						editMeasurementButton		= new JButton("<html><head></head><body><p style=\"text-align:center\">Edit<br>Measurement</p></body></html>");

	private volatile MeasurementState				state						= MeasurementState.READY;

	private MeasurementTree measurementTree = new MeasurementTree(this);
	
	private Vector<YouScopeFrame>		childFrames			= new Vector<YouScopeFrame>();
	private final MeasurementControlListener controlListener;
	
	private JPanel controlPanel;
	private JPanel informationPanel;
	private JPanel imagingJobsPanel;
	
	private JPopupMenu measurementProcessorChooser;
	
	private String name;
	
	private boolean isDocked = true;

	MeasurementControl(MeasurementControlListener controlListener, Measurement measurement) throws RemoteException
	{
		this.measurement = measurement;
		this.configuration = measurement.getSaver().getConfiguration();
		this.controlListener = controlListener;
		
		// Add measurement listener
		measurement.addMeasurementListener(new MeasurementListenerImpl());
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
		
		GridBagLayout containerLayout = new GridBagLayout();
		container.setLayout(containerLayout);
		GridBagConstraints newLineConstr = StandardFormats.getNewLineConstraint();
		GridBagConstraints bottomConstr = StandardFormats.getBottomContstraint();
		
		StandardFormats.addGridBagElement(undockLabelPanel, containerLayout, newLineConstr, container);
		StandardFormats.addGridBagElement(informationPanel, containerLayout, newLineConstr, container);
		StandardFormats.addGridBagElement(controlPanel, containerLayout, newLineConstr, container);
		StandardFormats.addGridBagElement(imagingJobsPanel, containerLayout, bottomConstr, container);
		
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
		GridBagLayout informationLayout = new GridBagLayout();
		informationPanel = new JPanel(informationLayout);
		informationPanel.setBorder(new TitledBorder("Measurement Information"));
		informationPanel.setOpaque(false);
		GridBagConstraints newLineConstr = StandardFormats.getNewLineConstraint();
		GridBagConstraints bottomConstr = StandardFormats.getBottomContstraint();
		
		name = measurement.getName();
		
		StandardFormats.addGridBagElement(new JLabel("Name:"), informationLayout, newLineConstr, informationPanel);
		measurementField = new JTextField(name);
		measurementField.setEditable(false);
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
					if(state == MeasurementState.READY || state == MeasurementState.FINISHED || state == MeasurementState.ERROR || state == MeasurementState.UNQUEUED)
					{
						MeasurementAddonFactory addonFactory = ClientSystem.getMeasurementAddon(configuration.getTypeIdentifier());
						if(addonFactory == null)
						{
							ClientSystem.err.println("No measurement configuration addon installed for measurements of type \"" + configuration.getTypeIdentifier() + "\".");
							return;
						}
						ComponentAddonUI<? extends MeasurementConfiguration> addon;
						try {
							addon = addonFactory.createMeasurementUI(configuration.getTypeIdentifier(), new YouScopeClientConnectionImpl(), YouScopeClientImpl.getServer());
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
		StandardFormats.addGridBagElement(measurementField, informationLayout, newLineConstr, informationPanel);

		// Initialize status field
		StandardFormats.addGridBagElement(new JLabel("Status:"), informationLayout, newLineConstr, informationPanel);
		stateField = new MeasurementStateField();
		setState(measurement.getState());
		StandardFormats.addGridBagElement(stateField, informationLayout, newLineConstr, informationPanel);
		
		StandardFormats.addGridBagElement(new JLabel("Duration:"), informationLayout, newLineConstr, informationPanel);
		runTimeField.setEditable(false);
		StandardFormats.addGridBagElement(runTimeField, informationLayout, newLineConstr, informationPanel);

		// Initialize Buttons
		startMeasurementButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				switch(state)
				{
					case READY:
					case FINISHED:
					case UNQUEUED:
						startMeasurement();
						break;
					default:
						break;
				}
			}
		});
		
		
		measurementProcessorChooser = new JPopupMenu();
		measurementResultsButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	if(isDocked)
                		measurementProcessorChooser.show(measurementResultsButton, -measurementProcessorChooser.getPreferredSize().width, 0);
                	else
                		measurementProcessorChooser.show(measurementResultsButton, measurementResultsButton.getWidth(), 0);
                }
            });
		
		Iterator<PostProcessorAddonFactory> factories = ClientSystem.getMeasurementPostProcessorAddons().iterator();
		
		for(;factories.hasNext();)
		{
			PostProcessorAddonFactory factory = factories.next();
			for(String addonID : factory.getSupportedTypeIdentifiers())
			{
				try {
					measurementProcessorChooser.add(new StartProcessorMenuItem(factory, addonID));
				} catch (@SuppressWarnings("unused") AddonException e1) {
					continue;
				}
			}
		}
		
		stopMeasurementButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				stopMeasurement();
			}
		});
		quickStopMeasurementButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				quickStopMeasurement();
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

		startMeasurementButton.setOpaque(false);
		measurementResultsButton.setOpaque(false);
		stopMeasurementButton.setOpaque(false);
		quickStopMeasurementButton.setOpaque(false);
		editMeasurementButton.setOpaque(false);
		saveMeasurementButton.setOpaque(false);
		emergencyStopButton.setOpaque(false);
		
		GridBagLayout buttonsLayout = new GridBagLayout();
		controlPanel = new JPanel(buttonsLayout);
		controlPanel.setBorder(new TitledBorder("Measurement Control"));
		controlPanel.setOpaque(false);
		StandardFormats.addGridBagElement(measurementResultsButton, buttonsLayout, newLineConstr, controlPanel);
		StandardFormats.addGridBagElement(startMeasurementButton, buttonsLayout, newLineConstr, controlPanel);
		StandardFormats.addGridBagElement(stopMeasurementButton, buttonsLayout, newLineConstr, controlPanel);
		StandardFormats.addGridBagElement(quickStopMeasurementButton, buttonsLayout, newLineConstr, controlPanel);
		JPanel emptyPanel1 = new JPanel();
		emptyPanel1.setOpaque(false);
		StandardFormats.addGridBagElement(emptyPanel1, buttonsLayout, newLineConstr, controlPanel);
		StandardFormats.addGridBagElement(editMeasurementButton, buttonsLayout, newLineConstr, controlPanel);
		StandardFormats.addGridBagElement(saveMeasurementButton, buttonsLayout, newLineConstr, controlPanel);
		JPanel emptyPanel2 = new JPanel();
		emptyPanel2.setOpaque(false);
		StandardFormats.addGridBagElement(emptyPanel2, buttonsLayout, bottomConstr, controlPanel);
		StandardFormats.addGridBagElement(emergencyStopButton, buttonsLayout, newLineConstr, controlPanel);
		

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
	private void setState(MeasurementState state)
	{
		if(SwingUtilities.isEventDispatchThread())
			actualizeStateInternal(state);
		else
		{
			class Runner implements Runnable
			{
				private final MeasurementState state;
				Runner(MeasurementState state)
				{
					this.state = state;
				}
				@Override
				public void run()
				{
					actualizeStateInternal(state);
				}
			}
			SwingUtilities.invokeLater(new Runner(state));
		}
	}
	private synchronized void actualizeStateInternal(MeasurementState state)
	{
		this.state = state;
		// Set behavior.
		switch(state)
		{
			case FINISHED:
				if(measurementProcessorChooser.getComponentCount() > 0)
					measurementResultsButton.setVisible(true);
				else
					measurementResultsButton.setVisible(false);
				startMeasurementButton.setVisible(true);
				editMeasurementButton.setVisible(true);
				stopMeasurementButton.setVisible(false);
				quickStopMeasurementButton.setVisible(false);
				break;
			case READY:
			case UNQUEUED:
				measurementResultsButton.setVisible(false);
				startMeasurementButton.setVisible(true);
				editMeasurementButton.setVisible(true);
				stopMeasurementButton.setVisible(false);
				quickStopMeasurementButton.setVisible(false);
				break;
			case QUEUED:
			case RUNNING:
			case STOPPING:
			case INTERRUPTING:
			case INITIALIZING:
			case UNINITIALIZING:
				measurementResultsButton.setVisible(false);
				startMeasurementButton.setVisible(false);
				editMeasurementButton.setVisible(false);
				stopMeasurementButton.setVisible(true);
				quickStopMeasurementButton.setVisible(true);
				break;
			case ERROR:
				measurementResultsButton.setVisible(false);
				startMeasurementButton.setVisible(false);
				editMeasurementButton.setVisible(true);
				stopMeasurementButton.setVisible(true);
				quickStopMeasurementButton.setVisible(true);
				break;
			default:
				// do nothing.
				break;
		}
		// Set state information field.
		stateField.setState(state);
		
		// Set duration updating
		if(state == MeasurementState.RUNNING)
		{
			try
			{
				startTime = measurement.getStartTime();
				runTimeTimer = new Timer(1000, new ActionListener()
					{
						@Override
						public void actionPerformed(ActionEvent arg0)
						{
							if(getState() == MeasurementState.RUNNING && startTime != null)
							{
								long duration = new Date().getTime() - startTime.getTime();
								setDuration(duration);
							}
						}
					});
				runTimeTimer.start();
			}
			catch(RemoteException e)
			{
				ClientSystem.err.println("Could not get measurement start time", e);
			}	
		}
		else if(state == MeasurementState.FINISHED)
		{
			try
			{
				if(runTimeTimer != null)
				{
					runTimeTimer.stop();
					runTimeTimer = null;
				}
				startTime = measurement.getStartTime();
				Date endTime = measurement.getEndTime();
				if(startTime != null && endTime != null)
				{
					long duration = endTime.getTime() - startTime.getTime();
					setDuration(duration);
				}
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

	private void stopMeasurement()
	{
		setState(MeasurementState.STOPPING);
		if(measurement != null)
		{
			try
			{
				measurement.stopMeasurement();
			}
			catch(RemoteException e)
			{
				ClientSystem.err.println("Could not stop measurement.", e);
				setState(MeasurementState.ERROR);
			}
		}
	}

	private void quickStopMeasurement()
	{
		if(measurement != null)
		{
			setState(MeasurementState.STOPPING);
			try
			{
				measurement.quickStopMeasurement();
			}
			catch(RemoteException e)
			{
				ClientSystem.err.println("Could not quick stop measurement.", e);
				setState(MeasurementState.ERROR);
				return;
			}
			setState(MeasurementState.FINISHED);
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
		catch(RemoteException e)
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
		public void errorOccured(Exception e) throws RemoteException
		{
			setState(MeasurementState.ERROR);
			ClientSystem.err.println("Error in measurement \"" + measurement.getName() + "\" occured.", e);
		}

		@Override
		public void measurementFinished() throws RemoteException
		{
			setState(MeasurementState.FINISHED);
		}

		@Override
		public void measurementQueued() throws RemoteException
		{
			setState(MeasurementState.QUEUED);
		}

		@Override
		public void measurementStarted() throws RemoteException
		{
			setState(MeasurementState.RUNNING);
		}

		@Override
		public void measurementUnqueued() throws RemoteException
		{
			setState(MeasurementState.UNQUEUED);
		}

		@Override
		public void measurementInitializing() throws RemoteException
		{
			setState(MeasurementState.INITIALIZING);
		}

		@Override
		public void measurementUninitializing() throws RemoteException
		{
			setState(MeasurementState.UNINITIALIZING);
		}

		@Override
		public void measurementStructureModified() throws RemoteException 
		{
			refreshMeasurementTree();
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
		private final PostProcessorAddonFactory addonFactory;
		private final String addonID;
		StartProcessorMenuItem(PostProcessorAddonFactory addonFactory, String addonID) throws AddonException
		{
			super(addonFactory.getPostProcessorMetadata(addonID).getTypeName());
			this.addonFactory = addonFactory;
			this.addonID = addonID;
			addActionListener(this);
		}
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			if(measurement == null)
				return;
			
			String measurementFolder;
			try
			{
				measurementFolder = measurement.getSaver().getLastMeasurementFolder();
			}
			catch(RemoteException e)
			{
				ClientSystem.err.println("Could not obtain measurement save options from measurement.", e);
				return;
			}
			try
			{
				AddonUI<?> addon = addonFactory.createPostProcessorUI(addonID, new YouScopeClientConnectionImpl(), YouScopeClientImpl.getServer(), measurementFolder);
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
