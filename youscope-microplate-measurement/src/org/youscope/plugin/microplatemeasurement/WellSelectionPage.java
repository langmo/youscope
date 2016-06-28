/**
 * 
 */
package org.youscope.plugin.microplatemeasurement;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.ServiceLoader;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import org.youscope.addon.AddonException;
import org.youscope.addon.measurement.MeasurementAddonUIPage;
import org.youscope.addon.pathoptimizer.PathOptimizer;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.clientinterfaces.YouScopeFrameListener;
import org.youscope.common.configuration.FocusConfiguration;
import org.youscope.common.microscope.Device;
import org.youscope.common.microscope.DeviceException;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.StandardFormats;

/**
 * @author Moritz Lang
 *
 */
class WellSelectionPage extends MeasurementAddonUIPage<MicroplateMeasurementConfiguration>
{

	/**
	 * Serial Verision UID.
	 */
	private static final long	serialVersionUID	= -4078877503793485077L;

	private JLabel			focusAdjustmentLabel	= new JLabel("Adjustment time for focusing device (ms):");
		
	private WellTable								wellSelection;

	private WellPositionsTable					positionSelection;

	private JCheckBox 							moreThanOnePositionInWell = new JCheckBox("Measure multiple positions per well.");
	private JLabel								wellSelectionLabel			= new JLabel("Define wells to measure:");										
	private JLabel								positionSelectionLabel		= new JLabel("Define positions in a well to measure:");							

	
	private JFormattedTextField					focusAdjustmentField		= new JFormattedTextField(StandardFormats.getIntegerFormat());

	private JRadioButton							focusNotStore				= new JRadioButton("Focus/Autofocus not set.", false);

	private JRadioButton							focusNormalStore			= new JRadioButton("Store focus for every well/position.", false);

	private final JLabel			focusDeviceLabel		= new JLabel("Focus device:");
	private JComboBox<String>								focusDeviceField;
	
	private JComboBox<String>								stageDeviceField;
	
	private JButton newFineConfiguration = new JButton("Run Fine-Configuration");
	private JButton editFineConfiguration = new JButton("Edit Fine-Configuration");
	
	private MicroplatePositionConfiguration positionConfiguration = null;
	private final YouScopeClient	client;
	private final YouScopeServer			server;
	private YouScopeFrame frame;
	
	private final JButton showOptimizedPathButton = new JButton("Show"); 
	
	private final JComboBox<ComparableOptimizer>						pathOptimizerField	= new JComboBox<ComparableOptimizer>();
	
	private final static String DEFAULT_PATH_OPTIMIZER = "YouScope.ZigZagPathOptimizer";
	
	private class ComparableOptimizer implements Comparable<ComparableOptimizer>
	{
		private final PathOptimizer optimizer;
		private final MicroplatePositionConfiguration positionConfiguration;
		ComparableOptimizer(PathOptimizer optimizer, MicroplatePositionConfiguration positionConfiguration)
		{
			this.optimizer = optimizer;
			this.positionConfiguration = positionConfiguration;
		}
		
		@Override
		public int compareTo(ComparableOptimizer arg0)
		{
			if(arg0 == null)
				return -1;
			return arg0.optimizer.getSpecificity(positionConfiguration) - optimizer.getSpecificity(positionConfiguration) > 0 ? 1 : -1; 
		}
		
		@Override
		public String toString()
		{
			return optimizer.getName();
		}
		
		public String getOptimizerID()
		{
			return optimizer.getOptimizerID();
		}
	}
	private static Iterable<PathOptimizer> getPathOptimizers()
    {
        ServiceLoader<PathOptimizer> pathOptimizers =
                ServiceLoader.load(PathOptimizer.class,
                		MicroplateMeasurementInitializer.class.getClassLoader());
        return pathOptimizers;
    }
	private void loadPathOptimizers(MicroplateMeasurementConfiguration configuration)
	{
		ComparableOptimizer lastOptimizer = (ComparableOptimizer) pathOptimizerField.getSelectedItem();
		pathOptimizerField.removeAllItems();
		Vector<ComparableOptimizer> optimizers = new Vector<ComparableOptimizer>();
		for(PathOptimizer optimizer : getPathOptimizers())
		{
			if(!optimizer.isApplicable(positionConfiguration))
				continue;
			
			optimizers.addElement(new ComparableOptimizer(optimizer, positionConfiguration));
		}
		Collections.sort(optimizers);
		
		for(ComparableOptimizer optimizer : optimizers)
		{
			pathOptimizerField.addItem(optimizer);
		}
		
		if(configuration != null && configuration.getPathOptimizerID() != null)
		{
			for(ComparableOptimizer optimizer : optimizers)
			{
				if(optimizer.getOptimizerID().equals(configuration.getPathOptimizerID()))
				{
					pathOptimizerField.setSelectedItem(optimizer);
					break;
				}
			}
		}
		else if(lastOptimizer != null)
		{
			for(ComparableOptimizer optimizer : optimizers)
			{
				if(optimizer.getOptimizerID().equals(lastOptimizer.getOptimizerID()))
				{
					pathOptimizerField.setSelectedItem(optimizer);
					break;
				}
			}
		}
		else
		{
			for(ComparableOptimizer optimizer : optimizers)
			{
				if(optimizer.getOptimizerID().equals(DEFAULT_PATH_OPTIMIZER))
				{
					pathOptimizerField.setSelectedItem(optimizer);
					break;
				}
			}
		}
		
		if(pathOptimizerField.getItemCount() == 0)
			client.sendError("No path optimizer found. Check installation.");
	}
	
	WellSelectionPage(YouScopeClient client, YouScopeServer server)
	{
		this.server = server;
		this.client = client;
	}
	
	@Override
	public void loadData(MicroplateMeasurementConfiguration configuration)
	{
		positionConfiguration = configuration.getMicroplatePositions();
		if(positionConfiguration.isNoneSelected())
		{
			editFineConfiguration.setEnabled(false);
			newFineConfiguration.setEnabled(false);
		}
		else
		{
			newFineConfiguration.setEnabled(true);
			if(positionConfiguration.isInitialized())
				editFineConfiguration.setEnabled(true);
			else
				editFineConfiguration.setEnabled(false);
		}
		boolean isMultiplePositions = configuration.getMicroplatePositions().getWellNumPositionsX() > 1 || configuration.getMicroplatePositions().getWellNumPositionsY() > 1;
		
		wellSelection.loadFromConfiguration(configuration.getMicroplatePositions());
		positionSelection.loadFromConfiguration(configuration.getMicroplatePositions());
		
		if(configuration.getFocusConfiguration() != null)
			focusAdjustmentField.setValue(configuration.getFocusConfiguration().getAdjustmentTime());
		else
			focusAdjustmentField.setValue(0);
		
		if(configuration.getFocusConfiguration() != null && configuration.getFocusConfiguration().getFocusDevice() != null)
		{
			focusDeviceField.setSelectedItem(configuration.getFocusConfiguration().getFocusDevice());
		}
		
		String stageDevice = configuration.getStageDevice();
		if(stageDevice == null)
		{
			try
			{
				stageDevice = server.getMicroscope().getStageDevice().getDeviceID();
			}
			catch(Exception e)
			{
				client.sendError("Could not determine standard stage device.", e);
			}
		}
		stageDeviceField.setSelectedItem(stageDevice);

		if(configuration.getFocusConfiguration() != null)
		{
			focusNormalStore.doClick();
		}
		else
		{
			focusNotStore.doClick();
		}
		
		if(configuration.getMicroplatePositions().isAliasMicroplate())
		{
			wellSelection.setVisible(false);
			wellSelectionLabel.setVisible(false);
			moreThanOnePositionInWell.setVisible(false);
			positionSelection.setVisible(false);
			positionSelectionLabel.setVisible(false);
		}
		else
		{
			wellSelection.setVisible(true);
			wellSelectionLabel.setVisible(true);
			if(isMultiplePositions)
			{
				moreThanOnePositionInWell.setSelected(true);
				positionSelection.setVisible(true);
				positionSelectionLabel.setVisible(true);
			}
			else
			{
				moreThanOnePositionInWell.setSelected(false);
				positionSelection.setVisible(false);
				positionSelectionLabel.setVisible(false);
			}	
		}
		
		if(positionConfiguration==null || positionConfiguration.isInitialized() == false || positionConfiguration.isNoneSelected())
		{
			showOptimizedPathButton.setEnabled(false);
		}
		else
		{
			showOptimizedPathButton.setEnabled(true);
		}
		
		loadPathOptimizers(configuration);
	}
	
	private void selectionChanged()
	{
		if(positionConfiguration.isNoneSelected())
		{
			editFineConfiguration.setEnabled(false);
			newFineConfiguration.setEnabled(false);
		}
		else
		{
			newFineConfiguration.setEnabled(true);
			if(positionConfiguration.isInitialized())
				editFineConfiguration.setEnabled(true);
			else
				editFineConfiguration.setEnabled(false);
		}
		
		if(positionConfiguration==null || positionConfiguration.isInitialized() == false || positionConfiguration.isNoneSelected())
		{
			loadPathOptimizers(null);
			showOptimizedPathButton.setEnabled(false);
		}
		else
		{
			loadPathOptimizers(null);
			showOptimizedPathButton.setEnabled(true);
		}
	}
	
	private String[] getFocusDevices() throws RemoteException, InterruptedException, DeviceException, MicroscopeException
	{
		Device[] devices = server.getMicroscope().getFocusDevices();
		String[] deviceNames = new String[devices.length];
		for(int i=0; i < devices.length; i++)
		{
			deviceNames[i] = devices[i].getDeviceID();
		}
		
		return deviceNames;
	}
	
	private String[] getStageDevices() throws RemoteException, InterruptedException, DeviceException, MicroscopeException
	{
		Device[] devices = server.getMicroscope().getStageDevices();
		String[] deviceNames = new String[devices.length];
		for(int i=0; i < devices.length; i++)
		{
			deviceNames[i] = devices[i].getDeviceID();
		}
		
		return deviceNames;
	}

	@Override
	public boolean saveData(MicroplateMeasurementConfiguration configuration)
	{
		wellSelection.saveToConfiguration(positionConfiguration);
		positionSelection.saveToConfiguration(positionConfiguration);
		configuration.setMicroplatePositions(positionConfiguration);
		
		if(focusNotStore.isSelected())
		{
			configuration.setFocusConfiguration(null);
		}
		else
		{
			FocusConfiguration focusConfig = new FocusConfiguration();
			focusConfig.setFocusDevice(focusDeviceField.getSelectedItem().toString());
			focusConfig.setAdjustmentTime(((Number)focusAdjustmentField.getValue()).intValue());
			configuration.setFocusConfiguration(focusConfig);
		}
		
		configuration.setStageDevice(stageDeviceField.getSelectedItem().toString());
		configuration.setPathOptimizerID(((ComparableOptimizer)pathOptimizerField.getSelectedItem()).getOptimizerID());
		return true;
	}

	@Override
	public void setToDefault(MicroplateMeasurementConfiguration configuration)
	{
		try
		{	
			String focusDevice = server.getMicroscope().getFocusDevice().getDeviceID();
			FocusConfiguration focusConfiguration = new FocusConfiguration();
			focusConfiguration.setFocusDevice(focusDevice);
			configuration.setFocusConfiguration(focusConfiguration);
		}
		catch(Exception e)
		{
			client.sendError("Could not pre-initialize focus device. Letting these settings empty and continuing.", e);
		}
	}

	@Override
	public String getPageName()
	{
		return "Measured Positions";
	}

	@Override
	public void createUI(final YouScopeFrame frame)
	{
		this.frame = frame;
		GridBagConstraints newLineConstr = StandardFormats.getNewLineConstraint();
		GridBagConstraints bottomConstr = StandardFormats.getBottomContstraint();

		GridBagLayout elementsLayout = new GridBagLayout();
		setLayout(elementsLayout);

		try
		{
			focusDeviceField = new JComboBox<String>(getFocusDevices());
		}
		catch(Exception e1)
		{
			client.sendError("Could not load focus device list. Initializing with empty list", e1);
			focusDeviceField = new JComboBox<String>();
		}
		
		try
		{
			stageDeviceField = new JComboBox<String>(getStageDevices());
		}
		catch(Exception e1)
		{
			client.sendError("Could not load stage device list. Initializing with empty list", e1);
			stageDeviceField = new JComboBox<String>();
		}
		
		wellSelection = new WellTable();
		wellSelection.addWellsChangeListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				wellSelection.saveToConfiguration(positionConfiguration);
				selectionChanged();
			}
		});
		positionSelection = new WellPositionsTable();
		positionSelection.addWellPositionsChangeListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				positionSelection.saveToConfiguration(positionConfiguration);
				selectionChanged();
			}
		});
		
		if(stageDeviceField.getItemCount() > 1)
		{
			StandardFormats.addGridBagElement(new JLabel("Stage Device:"), elementsLayout, newLineConstr, this);
			StandardFormats.addGridBagElement(stageDeviceField, elementsLayout, newLineConstr, this);
		}
		
		StandardFormats.addGridBagElement(wellSelectionLabel, elementsLayout, newLineConstr, this);
		StandardFormats.addGridBagElement(wellSelection, elementsLayout, newLineConstr, this);
		StandardFormats.addGridBagElement(moreThanOnePositionInWell, elementsLayout, newLineConstr, this);
		StandardFormats.addGridBagElement(positionSelectionLabel, elementsLayout, newLineConstr, this);
		StandardFormats.addGridBagElement(positionSelection, elementsLayout, newLineConstr, this);

		StandardFormats.addGridBagElement(new JLabel("Focussing options for every position:"), elementsLayout, newLineConstr, this);
		StandardFormats.addGridBagElement(focusNotStore, elementsLayout, newLineConstr, this);
		StandardFormats.addGridBagElement(focusNormalStore, elementsLayout, newLineConstr, this);
		ButtonGroup zPositionButtonGroup = new ButtonGroup();
		zPositionButtonGroup.add(focusNotStore);
		zPositionButtonGroup.add(focusNormalStore);
		class ZPositionStoreChangedListener implements ActionListener
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(focusNotStore.isSelected())
				{
					focusAdjustmentLabel.setVisible(false);
					focusAdjustmentField.setVisible(false);
					focusDeviceField.setVisible(false);
					focusDeviceLabel.setVisible(false);
				}
				else if(focusNormalStore.isSelected())
				{
					focusAdjustmentLabel.setVisible(true);
					focusAdjustmentField.setVisible(true);
					if(focusDeviceField.getItemCount() > 1)
					{
						focusDeviceField.setVisible(true);
						focusDeviceLabel.setVisible(true);
					}
					else
					{
						focusDeviceField.setVisible(false);
						focusDeviceLabel.setVisible(false);
					}
				}
				fireSizeChanged();
				selectionChanged();
			}
		}
		focusNotStore.addActionListener(new ZPositionStoreChangedListener());
		focusNormalStore.addActionListener(new ZPositionStoreChangedListener());
		
		StandardFormats.addGridBagElement(focusDeviceLabel, elementsLayout, newLineConstr, this);
		StandardFormats.addGridBagElement(focusDeviceField, elementsLayout, newLineConstr, this);
		focusDeviceField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				selectionChanged();
			}
		});
		StandardFormats.addGridBagElement(focusAdjustmentLabel, elementsLayout, newLineConstr, this);
		StandardFormats.addGridBagElement(focusAdjustmentField, elementsLayout, newLineConstr, this);

		// Path optimizer
		StandardFormats.addGridBagElement(new JLabel("Path through microplate"), elementsLayout, newLineConstr, this);
		JPanel pathPanel = new JPanel(new BorderLayout());
		pathPanel.setOpaque(false);
		pathPanel.add(pathOptimizerField, BorderLayout.CENTER);
		pathPanel.add(showOptimizedPathButton, BorderLayout.EAST);
		StandardFormats.addGridBagElement(pathPanel, elementsLayout, newLineConstr, this);		
		showOptimizedPathButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				PathOptimizer optimizer = ((ComparableOptimizer)pathOptimizerField.getSelectedItem()).optimizer;
				if(positionConfiguration==null || positionConfiguration.isInitialized() == false || positionConfiguration.isNoneSelected())
				{
					return;
				}
				
				try
				{
					PathDisplayUI content = new PathDisplayUI(client, server);
					YouScopeFrame childFrame = content.toFrame();
					frame.addChildFrame(childFrame);
					childFrame.setVisible(true);
					content.calculatePath(optimizer, positionConfiguration);
				}
				catch(AddonException e1)
				{
					client.sendError("Could not display path.", e1);
				}
			}
		});
		
		StandardFormats.addGridBagElement(new JPanel(), elementsLayout, bottomConstr, this);

		JTextArea remarkLabel = new JTextArea("Remark: The fine configuration must be rerun every time the measurement type, the selected wells or positions have been changed.");
		remarkLabel.setEditable(false);
		remarkLabel.setOpaque(false);
		remarkLabel.setLineWrap(true);
		remarkLabel.setFont((new JLabel()).getFont().deriveFont(Font.BOLD));
		StandardFormats.addGridBagElement(remarkLabel, elementsLayout, newLineConstr, this);
		newFineConfiguration.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				newFineConfiguration();
			}
		});
		editFineConfiguration.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				editFineConfiguration();
			}
		});
		JPanel fineConfigurationPanel = new JPanel(new GridLayout(1, 2, 5, 0));
		fineConfigurationPanel.add(editFineConfiguration);
		fineConfigurationPanel.add(newFineConfiguration);
		StandardFormats.addGridBagElement(fineConfigurationPanel, elementsLayout, newLineConstr, this);

		moreThanOnePositionInWell.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(moreThanOnePositionInWell.isSelected())
				{
					if(positionConfiguration.isSinglePosition())
					{
						positionConfiguration.setWellNumPositionsX(3);
						positionConfiguration.setWellNumPositionsY(3);
						positionSelection.loadFromConfiguration(positionConfiguration);
						selectionChanged();
					}
					
					moreThanOnePositionInWell.setSelected(true);
					positionSelection.setVisible(true);
					positionSelectionLabel.setVisible(true);
				}
				else
				{
					if(!positionConfiguration.isSinglePosition())
					{
						positionConfiguration.setWellNumPositionsX(1);
						positionConfiguration.setWellNumPositionsY(1);
						positionSelection.loadFromConfiguration(positionConfiguration);
						selectionChanged();
					}
					
					moreThanOnePositionInWell.setSelected(false);
					positionSelection.setVisible(false);
					positionSelectionLabel.setVisible(false);
				}	
				editFineConfiguration.setEnabled(false);
				fireSizeChanged();
			}
		});
		
		setBorder(new TitledBorder("Measured Positions"));
	}
	
	private void newFineConfiguration()
	{
		if(positionConfiguration == null)
			return;
		
		// Run fine configuration
		YouScopeFrame fineConfigFrame = frame.createModalChildFrame();
		fineConfigFrame.addFrameListener(new YouScopeFrameListener()
				{
					@Override
					public void frameClosed()
					{
						selectionChanged();
					}

					@Override
					public void frameOpened()
					{
						// Do nothing.
					}
				});
		String focusDevice = focusNotStore.isSelected() ? null : focusDeviceField.getSelectedItem().toString();
		String stageDevice = stageDeviceField.getSelectedItem().toString();
		
		@SuppressWarnings("unused")
		PositionFineConfigurationFrame positionFineConfigurationFrame = new PositionFineConfigurationFrame(client, server, fineConfigFrame, positionConfiguration, focusDevice, stageDevice, true);
		fineConfigFrame.setVisible(true);
	}
	private void editFineConfiguration()
	{
		if(positionConfiguration == null)
			return;
		
		// Edit fine configuration
		YouScopeFrame fineConfigFrame = frame.createModalChildFrame();
		fineConfigFrame.addFrameListener(new YouScopeFrameListener()
		{
			@Override
			public void frameClosed()
			{
				selectionChanged();
			}

			@Override
			public void frameOpened()
			{
				// Do nothing.
			}
		});
		
		String focusDevice = focusNotStore.isSelected() ? null : focusDeviceField.getSelectedItem().toString();
		String stageDevice = stageDeviceField.getSelectedItem().toString();
		
		@SuppressWarnings("unused")
		PositionFineConfigurationFrame positionFineConfigurationFrame = new PositionFineConfigurationFrame(client, server, fineConfigFrame, positionConfiguration, focusDevice, stageDevice, false);
		fineConfigFrame.setVisible(true);
	}

}
