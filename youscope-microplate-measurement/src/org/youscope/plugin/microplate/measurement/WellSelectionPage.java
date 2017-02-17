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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;

import org.youscope.addon.AddonException;
import org.youscope.addon.measurement.MeasurementAddonUIPage;
import org.youscope.addon.microplate.MicroplateResource;
import org.youscope.addon.microplate.MicroplateWellSelectionUI;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.FocusConfiguration;
import org.youscope.common.measurement.SimpleMeasurementContext;
import org.youscope.common.Well;
import org.youscope.common.microplate.MicroplateLayout;
import org.youscope.common.microplate.WellLayout;
import org.youscope.plugin.microplate.measurement.MicroplateMeasurementConfiguration;
import org.youscope.plugin.microplate.measurement.TileConfiguration;
import org.youscope.plugin.microplate.measurement.TileDefinitionUI;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DynamicPanel;


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

	private MicroplateLayout microplateLayout = null;
	private JLabel microplateSelectionLabel = new JLabel("Select wells to measure:");				
	private MicroplateWellSelectionUI microplateSelectionUI = null;
	private Component microplateSelectionUIComponent = null;
	
	private JLabel tileSelectionLabel = new JLabel("Select tiles in a well to measure:");	
	private TileDefinitionUI	tileSelectionUI;
	private Component tileSelectionUIComponent;

	private JCheckBox 							tileMeasurementField = new JCheckBox("Image multiple tiles per well.");
	
	private final YouScopeClient	client;
	private final YouScopeServer			server;
	
	WellSelectionPage(YouScopeClient client, YouScopeServer server)
	{
		this.server = server;
		this.client = client;
	}
	
	@Override
	public void loadData(MicroplateMeasurementConfiguration configuration)
	{
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
		microplateSelectionUI.setMicroplateLayout(microplateLayout);
		microplateSelectionUI.setSelectedWells(configuration.getSelectedWells());
		
		boolean tileMeasurement = false;
		if(microplateLayout != null)
		{
			double width;
			double height;
			if(microplateLayout.getNumWells()>=1)
			{
				WellLayout firstWell = microplateLayout.getWell(0);
				width = firstWell.getWidth();
				height = firstWell.getHeight();
			}
			else
			{
				width = 9000.;
				height = 9000.;
			}
			tileSelectionUI.setWellSize(width, height);
			
			TileConfiguration tileConfiguration = configuration.getTileConfiguration();		
			if(tileConfiguration == null)
			{
				tileSelectionUI.setTileLayout(3,3);
			}
			else
			{
				tileMeasurement = true;
				tileSelectionUI.setTileLayout(tileConfiguration.getNumTilesX(), tileConfiguration.getNumTilesY());
				tileSelectionUI.setSelectedTiles(configuration.getSelectedTiles());
			}
		}
		
		
		
		if(microplateLayout == null)
		{
			microplateSelectionLabel.setVisible(false);
			microplateSelectionUIComponent.setVisible(false);
			
			tileMeasurementField.setVisible(false);
			
			tileSelectionUIComponent.setVisible(false);
			tileSelectionLabel.setVisible(false);
		}
		else
		{
			microplateSelectionLabel.setVisible(true);
			microplateSelectionUIComponent.setVisible(true);
			
			tileMeasurementField.setVisible(true);
			
			if(tileMeasurement)
			{
				tileMeasurementField.setSelected(true);
				tileSelectionUIComponent.setVisible(true);
				tileSelectionLabel.setVisible(true);
			}
			else
			{
				tileMeasurementField.setSelected(false);
				tileSelectionUIComponent.setVisible(false);
				tileSelectionLabel.setVisible(false);
			}	
		}
	}

	@Override
	public boolean saveData(MicroplateMeasurementConfiguration configuration)
	{
		if(microplateLayout != null)
		{
			configuration.setSelectedWells(microplateSelectionUI.getSelectedWells());
			if(tileMeasurementField.isSelected())
			{
				TileConfiguration tileConfiguration = new TileConfiguration();
				tileConfiguration.setNumTilesX(tileSelectionUI.getNumTilesX());
				tileConfiguration.setNumTilesY(tileSelectionUI.getNumTilesY());
				configuration.setTileConfiguration(tileConfiguration);
				configuration.setSelectedTiles(tileSelectionUI.getSelectedTiles());
			}
			else
			{
				configuration.setTileConfiguration(null);
				configuration.setSelectedTiles(new HashSet<Well>(0));
			}
		}
		else
		{
			configuration.setSelectedWells(new HashSet<Well>(0));
			configuration.setTileConfiguration(null);
			configuration.setSelectedTiles(new HashSet<Well>(0));
		}
		
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
	protected boolean isAppear(MicroplateMeasurementConfiguration configuration) {
		return configuration.getMicroplateConfiguration() != null;
	}

	@Override
	public void createUI(final YouScopeFrame frame)
	{
		setLayout(new BorderLayout());
		
		microplateSelectionUI = new MicroplateWellSelectionUI(client, server);
		tileSelectionUI = new TileDefinitionUI(client, server);
		
		DynamicPanel mainPanel = new DynamicPanel();
		mainPanel.add(microplateSelectionLabel);
		try {
			microplateSelectionUIComponent = microplateSelectionUI.toPanel(frame);
			
		} catch (AddonException e2) {
			client.sendError("Cannot display well selection UI.", e2);
			microplateSelectionUIComponent = new JLabel("Cannot display well selection UI.");
		}
		mainPanel.addFill(microplateSelectionUIComponent);
		
		mainPanel.add(tileMeasurementField);
		mainPanel.add(tileSelectionLabel);
		try {
			tileSelectionUIComponent = tileSelectionUI.toPanel(frame);
			
		} catch (AddonException e2) {
			client.sendError("Cannot display tile selection UI.", e2);
			microplateSelectionUIComponent = new JLabel("Cannot display tile selection UI.");
		}
		mainPanel.add(tileSelectionUIComponent);
		
		

		tileMeasurementField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(tileMeasurementField.isSelected())
				{
					tileMeasurementField.setSelected(true);
					tileSelectionUIComponent.setVisible(true);
					tileSelectionLabel.setVisible(true);
				}
				else
				{
					tileMeasurementField.setSelected(false);
					tileSelectionUIComponent.setVisible(false);
					tileSelectionLabel.setVisible(false);
				}	
				fireSizeChanged();
			}
		});
		add(mainPanel, BorderLayout.CENTER);
		setBorder(new TitledBorder("Wells and Tiles"));
	}
}
