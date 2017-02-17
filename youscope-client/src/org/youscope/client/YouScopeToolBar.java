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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.youscope.addon.AddonException;
import org.youscope.addon.AddonMetadata;
import org.youscope.addon.component.ComponentAddonUI;
import org.youscope.addon.component.ComponentAddonUIListener;
import org.youscope.addon.component.ComponentMetadata;
import org.youscope.addon.tool.ToolAddonUI;
import org.youscope.addon.tool.ToolMetadata;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.measurement.MeasurementConfiguration;
import org.youscope.uielements.AddonButton;
import org.youscope.uielements.ImageLoadingTools;

/**
 * The main toolbar of the YouScope window
 * @author Moritz Lang
 *
 */
class YouScopeToolBar extends JToolBar
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 3692378888290252775L;
	private static final String CONFIG_FILE = "configuration/toolbar.cfg";
	
	YouScopeToolBar()
	{
		setFloatable(false);
		setRollover(true);
	}
		
	/**
	 * Button representing an addon. The text, tooltip and icon is automatically initialized to the information provided in the addon metadata.
	 * @author Moritz
	 *
	 */
	private static class ToolbarScriptButton extends JButton
	{
		/**
		 * Serial Version UID.
		 */
		private static final long serialVersionUID = 7754882737215008622L;
		ToolbarScriptButton(String text, Icon icon)
		{
			super(text);
			setOpaque(false);
			setFocusPainted(false);
			super.setBorder(new EmptyBorder(3,5,3,5));
			
			setToolTipText("Run script "+text+".");
			
			if(icon != null)
			{
				setIcon(icon);
				
			}
			setVerticalTextPosition(SwingConstants.BOTTOM);
			setHorizontalTextPosition(SwingConstants.CENTER);
		}
		
		@Override
		public void setBorder(Border border)
		{
			// forbid the look and feel to add a border.
			return;
		}
	}
	
	private static class ToolbarAddonButton extends AddonButton<AddonMetadata>
	{
		/**
		 * Serial Version UID.
		 */
		private static final long serialVersionUID = -6575470641890270160L;
		ToolbarAddonButton(AddonMetadata addonMetadata, Icon defaultIcon)
		{
			super(addonMetadata, defaultIcon);
			setOpaque(false);
			setFocusPainted(false);
			super.setBorder(new EmptyBorder(3,5,3,5));
		}
		@Override
		public void setBorder(Border border)
		{
			// forbid the look and feel to add a border.
			return;
		}
	}
	
	private void addButton(String addonID)
	{
		try
		{
			final ComponentMetadata<? extends MeasurementConfiguration> metadata = ClientAddonProviderImpl.getProvider().getComponentMetadata(addonID, MeasurementConfiguration.class);
			ToolbarAddonButton measurementButton = new ToolbarAddonButton(metadata, ImageLoadingTools.DEFAULT_MEASUREMENT_ICON);
			measurementButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent event)
				{
					ComponentAddonUI<? extends MeasurementConfiguration> addon;
					try {
						addon = ClientAddonProviderImpl.getProvider().createComponentUI(metadata);
					} catch (AddonException e1) {
						ClientSystem.err.println("Could not create measurement configuration UI.", e1);
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
					YouScopeFrame confFrame;
					try {
						confFrame = addon.toFrame();
					} catch (AddonException e) {
						ClientSystem.err.println("Could not initialize measurement configuration UI.", e);
						return;
					}
					confFrame.setVisible(true);
				}
			});
			add(measurementButton);
			return;
		}
		catch(@SuppressWarnings("unused") AddonException e)
		{
			// do nothing, probably not a measurement.
		}
		
		try
		{
			final ToolMetadata metadata = ClientAddonProviderImpl.getProvider().getToolMetadata(addonID);
			ToolbarAddonButton toolButton = new ToolbarAddonButton(metadata, ImageLoadingTools.DEFAULT_TOOL_ICON);
			toolButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent event)
				{
					ToolAddonUI addon;
					try
					{
						addon = ClientAddonProviderImpl.getProvider().createToolUI(metadata);
						YouScopeFrame toolFrame = addon.toFrame();
						toolFrame.setVisible(true);
					}
					catch (AddonException e)
					{
						ClientSystem.err.println("Error creating tool UI.", e);
						return;
					}
				}
			});
			add(toolButton);
			return;
		}
		catch(@SuppressWarnings("unused") AddonException e)
		{
			// do nothing, probably not a tool.
		}
		
		ScriptDefinition[] scriptDefinitions = ScriptDefinitionManager.getScriptDefinitions();
		for(final ScriptDefinition scriptDefinition : scriptDefinitions)
		{
			if(scriptDefinition.getName().equals(addonID))
			{
				ToolbarScriptButton scriptButton = new ToolbarScriptButton(addonID, ImageLoadingTools.DEFAULT_SCRIPT_ICON);
				scriptButton.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						runScript();
					}
					private void runScript()
					{
						ScriptDefinitionManager.runScript(scriptDefinition);
					}
				});
				add(scriptButton);
				return;
			}
		}
	}
	
	public void loadConfiguration()
	{
		this.removeAll();
		File configFile = new File(CONFIG_FILE);
		// Create default config file if it does not exist.
		if(!configFile.exists())
		{
			if(!createConfiguration())
				return;
		}
		
		BufferedReader reader = null;
        try
		{
			reader = new BufferedReader(new FileReader(configFile));
			while(true)
			{
				String line = reader.readLine();
				if(line == null)
					break;
				line = line.trim();
				if(line.length() == 0)
					this.addSeparator();
				else
					addButton(line);
			}
		}
		catch(Exception e1)
		{
			ClientSystem.err.println("Could not read toolbar configuration file " + configFile.toString()+ ".", e1);
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
					ClientSystem.err.println("Could not close toolbar configuration file " + configFile.toString()+ ".", e1);
				}
			}						
		}
	}
	
	private boolean createConfiguration()
	{
		File configFile = new File(CONFIG_FILE).getAbsoluteFile();
		File parentDir = configFile.getParentFile();
		if(!parentDir.exists())
		{
			if(!parentDir.mkdirs())
			{
				ClientSystem.err.println("Could not create toolbar configuration file's parent directory.");
				return false;
			}
		}
		try
		{
			PrintStream fileStream = new PrintStream(configFile);
			fileStream.println("YouScope.YouScopeLiveStream");
			fileStream.println("YouScope.YouScopePositionControl");
			fileStream.println();
			fileStream.println("YouScope.DeviceSettingManager");
			fileStream.println("YouScope.YouScopeMeasurementViewer");
			fileStream.println();
			fileStream.println("YouScope.SimpleMeasurement");
			fileStream.println("YouScope.MicroPlateMeasurement");
			fileStream.println();
			fileStream.close();
		}
		catch(Exception e)
		{
			ClientSystem.err.println("Could not create toolbar configuration file.", e);
			return false;
		}
		return true;
	}
}
