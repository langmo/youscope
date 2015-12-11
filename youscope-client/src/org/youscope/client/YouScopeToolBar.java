/**
 * 
 */
package org.youscope.client;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonUI;
import org.youscope.addon.component.ComponentAddonUIListener;
import org.youscope.addon.component.ComponentMetadata;
import org.youscope.addon.tool.ToolAddonUI;
import org.youscope.addon.tool.ToolMetadata;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.configuration.MeasurementConfiguration;
import org.youscope.common.tools.TextTools;
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
		setBackground(Color.WHITE);
		setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0.3f,0.3f,0.3f)));
	}
	
	private void addButton(String addonID)
	{
		try
		{
			final ComponentMetadata<? extends MeasurementConfiguration> metadata = ClientAddonProviderImpl.getProvider().getComponentMetadata(addonID, MeasurementConfiguration.class);
			String addonName = metadata.getTypeName();
			if(addonName == null || addonName.length() <= 0)
				addonName = "Unnamed Measurement";
			
			JButton measurementButton = new JButton(TextTools.capitalize(addonName));
			measurementButton.setOpaque(false);
			measurementButton.setFocusPainted(false); 
			Icon measurementIcon = metadata.getIcon();
			if(measurementIcon == null)
				measurementIcon = ImageLoadingTools.getResourceIcon("icons/receipt--plus.png", "new measurement");
			if(measurementIcon != null)
			{
				measurementButton.setIcon(measurementIcon);
				measurementButton.setVerticalTextPosition(SwingConstants.BOTTOM);
				measurementButton.setHorizontalTextPosition(SwingConstants.CENTER);
			}
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
			String addonName = metadata.getTypeName();
			if(addonName == null || addonName.length() <= 0)
				addonName = "Unknown Tool";
			JButton toolButton = new JButton(TextTools.capitalize(addonName));
			toolButton.setOpaque(false);
			toolButton.setFocusPainted(false);
			Icon toolIcon = metadata.getIcon();
			if(toolIcon == null)
				toolIcon = ImageLoadingTools.getResourceIcon("icons/application-form.png", "New Tool");
			if(toolIcon != null)
			{
				toolButton.setIcon(toolIcon);
				toolButton.setVerticalTextPosition(SwingConstants.BOTTOM);
				toolButton.setHorizontalTextPosition(SwingConstants.CENTER);
			}
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
			// do nothing, probably not a measurement.
		}
		
		ScriptDefinition[] scriptDefinitions = ScriptDefinitionManager.getScriptDefinitions();
		for(ScriptDefinition scriptDefinition : scriptDefinitions)
		{
			if(scriptDefinition.getName().equals(addonID))
			{
				JButton scriptButton = new JButton(addonID);
				scriptButton.setOpaque(false);
				scriptButton.setFocusPainted(false);
				ImageIcon scriptsIcon = ImageLoadingTools.getResourceIcon("icons/script--arrow.png", "execute script");
				if(scriptsIcon != null)
				{
					scriptButton.setIcon(scriptsIcon);
					scriptButton.setVerticalTextPosition(SwingConstants.BOTTOM);
					scriptButton.setHorizontalTextPosition(SwingConstants.CENTER);
				}
				class NewScriptListener implements ActionListener
				{
					private final ScriptDefinition scriptDefinition;
					NewScriptListener(ScriptDefinition scriptDefinition)
					{
						this.scriptDefinition = scriptDefinition;
					}
					@Override
					public void actionPerformed(ActionEvent e)
					{
						runScript();
					}
					private void runScript()
					{
						ScriptDefinitionManager.runScript(scriptDefinition);
					}
				}
				scriptButton.addActionListener(new NewScriptListener(scriptDefinition));
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
			fileStream.println("CSB::YouScopeLiveStream");
			fileStream.println("CSB::YouScopePositionControl");
			fileStream.println();
			fileStream.println("CSB::DeviceSettingManager");
			fileStream.println("CSB::YouScopeMeasurementViewer::1.0");
			fileStream.println();
			fileStream.println("CSB::SimpleMeasurement");
			fileStream.println("CSB::MicroPlateMeasurement");
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
