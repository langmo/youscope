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
package org.youscope.plugin.microplate.measurement;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentCreationException;
import org.youscope.addon.component.ComponentMetadata;
import org.youscope.addon.measurement.MeasurementAddonUIPage;
import org.youscope.addon.microplate.MicroplateResource;
import org.youscope.addon.pathoptimizer.PathOptimizerConfiguration;
import org.youscope.addon.pathoptimizer.PathOptimizerResource;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.configuration.FocusConfiguration;
import org.youscope.common.measurement.SimpleMeasurementContext;
import org.youscope.common.Well;
import org.youscope.common.microplate.MicroplateLayout;
import org.youscope.common.microscope.Device;
import org.youscope.common.microscope.DeviceException;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.plugin.microplate.measurement.MicroplateMeasurementConfiguration;
import org.youscope.plugin.microplate.measurement.PathDisplayUI;
import org.youscope.plugin.microplate.measurement.PathTable;
import org.youscope.plugin.microplate.measurement.PositionFineConfigurationAddon;
import org.youscope.plugin.microplate.measurement.TileConfiguration;
import org.youscope.plugin.microplate.measurement.XYAndFocusPosition;
import org.youscope.plugin.microplate.measurement.PathTable.Column;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DescriptionPanel;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.FocusField;
import org.youscope.uielements.SubConfigurationPanel;
/**
 * @author Moritz Lang
 *
 */
class PathPage extends MeasurementAddonUIPage<MicroplateMeasurementConfiguration>
{

	/**
	 * Serial Verision UID.
	 */
	private static final long	serialVersionUID	= -771185914335388563L;
	
	private final YouScopeClient	client;
	private final YouScopeServer server;
	private FocusConfiguration focusConfiguration;
	private String stageDevice;
	private final HashMap<PositionInformation, XYAndFocusPosition> configuredPositions= new HashMap<>(200);
	private MicroplateLayout microplateLayout = null;
	private TileConfiguration tileConfiguration = null;
	private Set<Well> selectedWells;
	private Set<Well> selectedTiles;
	private YouScopeFrame frame;
	private PathOptimizerConfiguration pathOptimizerConfiguration;
	private final static String DEFAULT_PATH_OPTIMIZER = "YouScope.path.ZigZagPath";
	private SubPage lastSubPage = null;
	PathPage(YouScopeClient client, YouScopeServer server)
	{
		this.client = client;
		this.server = server;
	}
	private interface SubPage
	{
		void close();
	}
	private class NoPositionSelectedPanel extends DynamicPanel implements SubPage
	{
		/**
		 * Serial Versio UID.
		 */
		private static final long serialVersionUID = -1528365488715327529L;
		NoPositionSelectedPanel()
		{
			DescriptionPanel descriptionPanel = new DescriptionPanel("No Wells/Tiles Selected",
					"At least one well/tile has to be selected. If the option to image multiple tiles per well was selected, additionally at least one tile has to be selected.\n"
					+ "Go to the previous page and select at least one well/tile."
					);
			JScrollPane scrollPane = new JScrollPane(descriptionPanel);
			scrollPane.setPreferredSize(new Dimension(400, 150));
			addFill(scrollPane);
		}
		@Override
		public void close() {
			// do nothing.
		}
		
	}
	private class NoPositionsConfiguredPanel extends DynamicPanel implements SubPage
	{
		/**
		 * Serial Version UID.
		 */
		private static final long serialVersionUID = -5287492043333512642L;
		private final JButton newFineConfiguration = new JButton("Configure Positions");
		private JCheckBox							focusStoreField			= new JCheckBox("Store focus position for every well/tile.", false);

		private final JLabel			focusDeviceLabel		= new JLabel("Focus device:");
		private FocusField								focusDeviceField;
		private JComboBox<String> stageDeviceField;
		NoPositionsConfiguredPanel()
		{
			DescriptionPanel descriptionPanel = new DescriptionPanel("Microplate Positions not Configured",
					"Press <i>"+newFineConfiguration.getText()+"</i> to define the exact positions where images are taken. "
					+ "In the first mandatory step, the stage has to be manually moved to the center of the first well/tile indicated at the top-left. Based on this information, YouScope automatically calculates the positions of all other wells/tiles."
					+ "In the second optional step, the positions of all other wells/tiles can be adjusted. Alternatively, press <i>Save Positions</i> to accept the automatically calculated positions.\n"
					+"<b>Warning: Incorrectly providing the position of the first well/tile can damage the microscope (e.g. stage colliding with objectives)!</b>"
					);
			JScrollPane scrollPane = new JScrollPane(descriptionPanel);
			scrollPane.setPreferredSize(new Dimension(400, 150));
			addFill(scrollPane);
			
			try
			{
				stageDeviceField = new JComboBox<String>(getStageDevices());
			}
			catch(Exception e1)
			{
				client.sendError("Could not load stage device list. Initializing with empty list", e1);
				stageDeviceField = new JComboBox<String>();
			}
			if(stageDeviceField.getItemCount() > 1)
			{
				add(new JLabel("Stage Device:"));
				add(stageDeviceField);
			}
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
			
			add(focusStoreField);
			focusDeviceField = new FocusField(focusConfiguration, client, server);
			focusStoreField.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					if(!focusStoreField.isSelected())
					{
						focusDeviceField.setVisible(false);
						focusDeviceLabel.setVisible(false);
					}
					else
					{
						focusDeviceField.setVisible(true);
						focusDeviceLabel.setVisible(true);
					}
					fireSizeChanged();
				}
			});
			add(focusDeviceLabel);
			add(focusDeviceField);
			if(focusConfiguration != null)
			{
				focusStoreField.doClick();
			}
			
			add(newFineConfiguration);
			newFineConfiguration.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					try {
						runFineConfiguration(true);
					} catch (AddonException e) {
						client.sendError("Could not start microplate well/tile position configuration.", e);
					}
				}
			});
		}
		@Override
		public void close() 
		{
			if(!focusStoreField.isSelected())
			{
				focusConfiguration = null;
			}
			else
			{
				focusConfiguration = focusDeviceField.getFocusConfiguration();
			}
			stageDevice = stageDeviceField.getSelectedItem().toString();
		}
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
	private class PositionsConfiguredPanel extends DynamicPanel implements SubPage
	{
		/**
		 * Serial Version UID.
		 */
		private static final long serialVersionUID = 6258765419338706296L;
		private SubConfigurationPanel<PathOptimizerConfiguration> pathOptimizerPanel = null;
		private final JButton editFineConfiguration = new JButton("Edit Positions");
		private final JButton deleteFineConfiguration = new JButton("Delete Positions");
		private final JButton showOptimizedPathButton = new JButton("Visualize Path");
		private final PathTable pathTable;
		PositionsConfiguredPanel()
		{
			add(new JLabel("Configured Positions:"));
			ArrayList<PathTable.Column> columns = new ArrayList<>();
			if(microplateLayout != null)
				columns.add(Column.WELL);
			else
				columns.add(Column.MAIN_POSITION);
			if(tileConfiguration != null)
				columns.add(Column.TILE);
			columns.add(Column.X);
			columns.add(Column.Y);
			if(focusConfiguration != null)
				columns.add(Column.FOCUS);
			
			pathTable = new PathTable(configuredPositions, frame, columns.toArray(new Column[columns.size()]));
			pathTable.addLayoutChangedListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					editFineConfiguration.setEnabled(false);
				}
			});
			addFill(pathTable);
			
			pathOptimizerPanel = new SubConfigurationPanel<PathOptimizerConfiguration>("Path through microplate:", null, PathOptimizerConfiguration.class, client, frame);
			pathOptimizerPanel.setConfiguration(pathOptimizerConfiguration);
			add(pathOptimizerPanel);

			showOptimizedPathButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					PathOptimizerConfiguration pathOptimizerConfiguration = pathOptimizerPanel.getConfiguration();
					if(pathOptimizerConfiguration == null)
					{
						client.sendError("No path optimizer selected.");
						return;
					}
					PathOptimizerResource optimizer;
					try {
						optimizer = server.getComponentProvider(null).createComponent(new PositionInformation(), pathOptimizerConfiguration, PathOptimizerResource.class);
					} catch (RemoteException | ComponentCreationException | ConfigurationException e2) {
						client.sendError("Could not create path optimizer with ID "+pathOptimizerConfiguration.getTypeIdentifier()+".", e2);
						return;
					}
					
					
					try
					{
						PathDisplayUI content = new PathDisplayUI(client, server);
						YouScopeFrame childFrame = content.toFrame();
						frame.addChildFrame(childFrame);
						childFrame.setVisible(true);
						content.calculatePath(optimizer, configuredPositions);
					}
					catch(AddonException e1)
					{
						client.sendError("Could not display path.", e1);
					} 
				}
			});			
			deleteFineConfiguration.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					if(!configuredPositions.isEmpty())
					{
						if(JOptionPane.showConfirmDialog(PathPage.this, "Really delete all configured positions?", "Confirm Deleteion", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
							return;
					}
					configuredPositions.clear();
					updateLayout();
				}
			});
			editFineConfiguration.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					try {
						runFineConfiguration(false);
					} catch (AddonException e) {
						client.sendError("Could not start microplate well/tile position configuration.", e);
					}
				}
			});
			
			if(isCompatible())
			{
				editFineConfiguration.setEnabled(true);
			}
			else
			{
				editFineConfiguration.setEnabled(false);
			}
			showOptimizedPathButton.setEnabled(pathOptimizerPanel.getConfiguration()!=null);
			JPanel buttonsPanel = new JPanel(new GridLayout(1,3));
			buttonsPanel.add(showOptimizedPathButton);
			buttonsPanel.add(deleteFineConfiguration);
			buttonsPanel.add(editFineConfiguration);
			
			add(buttonsPanel);
		}
		@Override
		public void close() {
			pathOptimizerConfiguration = pathOptimizerPanel.getConfiguration();
		}
	}

	@Override
	public void loadData(MicroplateMeasurementConfiguration configuration)
	{
		pathOptimizerConfiguration = configuration.getPathOptimizerConfiguration();
		focusConfiguration = configuration.getFocusConfiguration();
		stageDevice = configuration.getStageDevice();
		configuredPositions.clear();
		configuredPositions.putAll(configuration.getPositions());
		
		selectedWells = configuration.getSelectedWells();
		selectedTiles = configuration.getSelectedTiles();
		try
		{
			if(configuration.getMicroplateConfiguration() != null)
			{
				MicroplateResource microplateResource = server.getComponentProvider(null).createComponent(new PositionInformation(), configuration.getMicroplateConfiguration(), MicroplateResource.class);
				SimpleMeasurementContext measurementContext = new SimpleMeasurementContext();
				microplateResource.initialize(measurementContext);
				microplateLayout = microplateResource.getMicroplateLayout();
				microplateResource.uninitialize(measurementContext);
			}
			else
				microplateLayout = null;
		} catch (Exception e) 
		{
			client.sendError("Could not get information on microscope layout.", e);
			microplateLayout = null;
		}	
		tileConfiguration = configuration.getTileConfiguration();
		
		updateLayout();
	}

	@Override
	public boolean saveData(MicroplateMeasurementConfiguration configuration)
	{
		if(lastSubPage != null)
			lastSubPage.close();
		
		configuration.setFocusConfiguration(focusConfiguration);
		configuration.setStageDevice(stageDevice);
		configuration.setPathOptimizerConfiguration(pathOptimizerConfiguration);
		configuration.setPositions(configuredPositions);
		return true;
	}

	@Override
	public void setToDefault(MicroplateMeasurementConfiguration configuration)
	{
		try 
		{
			ComponentMetadata<? extends PathOptimizerConfiguration> metadata = client.getAddonProvider().getComponentMetadata(DEFAULT_PATH_OPTIMIZER, PathOptimizerConfiguration.class);
			PathOptimizerConfiguration pathConfiguration = metadata.getConfigurationClass().newInstance();
			configuration.setPathOptimizerConfiguration(pathConfiguration);
			
		} catch (@SuppressWarnings("unused") Exception e) {
			// do nothing. Default only not set.
		}
	}

	private void updateLayout()
	{
		Runnable runner = new Runnable()
		{
			@Override
			public void run() 
			{
				removeAll();
				setLayout(new BorderLayout());
				if(lastSubPage != null)
					lastSubPage.close();
				if(configuredPositions.isEmpty())
				{
					if((microplateLayout != null && selectedWells.isEmpty())||(tileConfiguration != null && selectedTiles.isEmpty()))
					{
						NoPositionSelectedPanel panel = new NoPositionSelectedPanel();
						add(panel, BorderLayout.CENTER);
						lastSubPage = panel;
					}
					else
					{
						NoPositionsConfiguredPanel panel = new NoPositionsConfiguredPanel();
						add(panel, BorderLayout.CENTER);
						lastSubPage = panel;
					}
				}
				else
				{
					PositionsConfiguredPanel panel = new PositionsConfiguredPanel();
					add(panel, BorderLayout.CENTER);
					lastSubPage = panel;
				}
				revalidate();
				frame.pack();
			}
		};
		if(SwingUtilities.isEventDispatchThread())
			runner.run();
		else
			SwingUtilities.invokeLater(runner);
	}
	
	@Override
	public String getPageName()
	{
		return "Microplate Type";
	}
	
	@Override
	public void createUI(final YouScopeFrame frame)
	{
		setLayout(new BorderLayout());
		this.frame = frame;
		
		setBorder(new TitledBorder("Position Configuration"));
	}
	
	private void runFineConfiguration(boolean forceNew) throws AddonException
	{
		String focusDevice = focusConfiguration == null ? null : focusConfiguration.getFocusDevice();
		
		final PositionFineConfigurationAddon positionFineConfiguration = new PositionFineConfigurationAddon(client, server);
		positionFineConfiguration.setStageDevice(stageDevice);
		positionFineConfiguration.setFocusDevice(focusDevice);
		if(microplateLayout != null)
			positionFineConfiguration.setSelectedWells(microplateLayout, selectedWells);
		if(microplateLayout != null && tileConfiguration != null)
			positionFineConfiguration.setSelectedTiles(tileConfiguration, selectedTiles);
		if(!forceNew)
		{
			positionFineConfiguration.setPositions(configuredPositions);
		}
		positionFineConfiguration.addSaveListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Map<PositionInformation, XYAndFocusPosition> newPositions = positionFineConfiguration.getConfiguredPositions();
				if(newPositions == null || newPositions.isEmpty())
					return;
				configuredPositions.clear();
				configuredPositions.putAll(newPositions);
				updateLayout();
			}
		});
		
		YouScopeFrame fineConfigFrame = positionFineConfiguration.toFrame();
		fineConfigFrame.setVisible(true);
	}
	private boolean isCompatible()
	{
		if(microplateLayout == null && tileConfiguration != null)
			return false;
		else if(microplateLayout == null)
		{
			for(Entry<PositionInformation, XYAndFocusPosition> entry : configuredPositions.entrySet())
			{
				PositionInformation posInfo = entry.getKey();
				double focus = entry.getValue().getFocus();
				if(posInfo.getWell() != null)
					return false;
				if(posInfo.getNumPositions() != 1)
					return false;
				if(!PositionInformation.POSITION_TYPE_MAIN_POSITION.equals(posInfo.getPositionType(0)))
					return false;
				if((Double.isNaN(focus) && focusConfiguration != null) || (!Double.isNaN(focus) && focusConfiguration == null))
					return false;
			}
			// Check if positions in right order
			for(int pos = 0; pos < configuredPositions.size(); pos++)
			{
				if(!configuredPositions.containsKey(new PositionInformation(PositionInformation.POSITION_TYPE_MAIN_POSITION, pos)))
					return false;
			}
			return true;
		}
		// check if all entries valid
		for(Entry<PositionInformation, XYAndFocusPosition> entry : configuredPositions.entrySet())
		{
			PositionInformation posInfo = entry.getKey();
			double focus = entry.getValue().getFocus();
			if(posInfo.getWell() == null)
			{
				return false;
			}
			if(!selectedWells.contains(posInfo.getWell()))
			{
				return false;
			}
			if(posInfo.getNumPositions() == 2 && tileConfiguration != null)
			{
				if(!PositionInformation.POSITION_TYPE_YTILE.equals(posInfo.getPositionType(0)))
				{
					return false;
				}
				if(!PositionInformation.POSITION_TYPE_XTILE.equals(posInfo.getPositionType(1)))
				{
					return false;
				}
				if(!selectedTiles.contains(new Well(posInfo.getPosition(0), posInfo.getPosition(1))))
				{
					return false;
				}
			}
			else if(posInfo.getNumPositions() != 0 || tileConfiguration != null)
			{
				return false;
			}
			if((Double.isNaN(focus) && focusConfiguration != null) || (!Double.isNaN(focus) && focusConfiguration == null))
			{
				return false;
			}
		}
		// check if all entries there
		if(tileConfiguration == null)
		{
			for(Well well : selectedWells)
			{
				if(!configuredPositions.containsKey(new PositionInformation(well)))
				{
					return false;
				}
			}
		}
		else
		{
			for(Well well : selectedWells)
			{
				PositionInformation wellPos = new PositionInformation(well);
				for(Well tile : selectedTiles)
				{
					if(!configuredPositions.containsKey(new PositionInformation(new PositionInformation(wellPos, PositionInformation.POSITION_TYPE_YTILE, tile.getWellY()), PositionInformation.POSITION_TYPE_XTILE, tile.getWellX())))
					{
						return false;
					}
				}
				
			}
		}
		return true;
	}
}
