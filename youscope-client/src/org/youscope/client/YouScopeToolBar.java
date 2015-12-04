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
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonUI;
import org.youscope.addon.component.ComponentAddonUIListener;
import org.youscope.addon.component.ComponentMetadata;
import org.youscope.addon.measurement.MeasurementAddonFactory;
import org.youscope.addon.tool.ToolAddon;
import org.youscope.addon.tool.ToolAddonFactory;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.configuration.MeasurementConfiguration;
import org.youscope.common.measurement.Measurement;
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
		MeasurementAddonFactory measurementAddon = ClientSystem.getMeasurementAddon(addonID);
		if(measurementAddon != null)
		{
			ComponentMetadata<? extends MeasurementConfiguration> metadata;
			try {
				metadata = measurementAddon.getComponentMetadata(addonID);
			} catch (AddonException e) {
				ClientSystem.err.println("Could not load measurement metadata.", e);
				return;
			}
			
			String addonName = metadata.getTypeName();
			if(addonName == null || addonName.length() <= 0)
				addonName = "Unnamed Measurement";
			String[] addonFolder = addonName.split("/");
			
			JButton measurementButton = new JButton(addonFolder[addonFolder.length - 1]);
			measurementButton.setOpaque(false);
			measurementButton.setFocusPainted(false); 
			ImageIcon measurementIcon = metadata.getIcon();
			if(measurementIcon == null)
				measurementIcon = ImageLoadingTools.getResourceIcon("icons/receipt--plus.png", "new measurement");
			if(measurementIcon != null)
			{
				measurementButton.setIcon(measurementIcon);
				measurementButton.setVerticalTextPosition(SwingConstants.BOTTOM);
				measurementButton.setHorizontalTextPosition(SwingConstants.CENTER);
			}
			class NewMeasurementListener implements ActionListener
			{
				private MeasurementAddonFactory addonFactory;
				private String addonID;
				NewMeasurementListener(MeasurementAddonFactory addonFactory, String addonID)
				{
					this.addonFactory = addonFactory;
					this.addonID = addonID;
				}
				@Override
				public void actionPerformed(ActionEvent e)
				{
					openAddon();
				}
				private void openAddon()
				{
					ComponentAddonUI<? extends MeasurementConfiguration> addon;
					try {
						addon = addonFactory.createMeasurementUI(addonID, new YouScopeClientConnectionImpl(), YouScopeClientImpl.getServer());
					} catch (AddonException e1) {
						ClientSystem.err.println("Could not create measurement configuration UI.", e1);
						return;
					}
					addon.addUIListener(new ComponentAddonUIListener<MeasurementConfiguration>()
					{

						@Override
						public void configurationFinished(MeasurementConfiguration configuration)
						{
							Measurement measurement = YouScopeClientImpl.addMeasurement(configuration);
							if(measurement == null)
							{
								openAddon();
								return;
							}
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
			}
			measurementButton.addActionListener(new NewMeasurementListener(measurementAddon, addonID));
			add(measurementButton);
			return;
		}
		
		ToolAddonFactory toolAddon = ClientSystem.getToolAddon(addonID);
		if(toolAddon != null)
		{
			String addonName = toolAddon.getToolName(addonID);
			if(addonName == null || addonName.length() <= 0)
				addonName = "Unknown Tool";
			String[] addonFolder = addonName.split("/");
			JButton toolButton = new JButton(addonFolder[addonFolder.length-1]);
			toolButton.setOpaque(false);
			toolButton.setFocusPainted(false);
			ImageIcon toolIcon = toolAddon.getToolIcon(addonID);
			if(toolIcon == null)
				toolIcon = ImageLoadingTools.getResourceIcon("icons/application-form.png", "New Tool");
			if(toolIcon != null)
			{
				toolButton.setIcon(toolIcon);
				toolButton.setVerticalTextPosition(SwingConstants.BOTTOM);
				toolButton.setHorizontalTextPosition(SwingConstants.CENTER);
			}
			class NewToolListener implements ActionListener
			{
				private ToolAddonFactory addonFactory;
				private String addonID;
				NewToolListener(ToolAddonFactory addonFactory, String addonID)
				{
					this.addonFactory = addonFactory;
					this.addonID = addonID;
				}
				@Override
				public void actionPerformed(ActionEvent e)
				{
					openAddon();
				}
				private void openAddon()
				{
					ToolAddon addon = addonFactory.createToolAddon(addonID, new YouScopeClientConnectionImpl(), YouScopeClientImpl.getServer());
					YouScopeFrame toolFrame = YouScopeFrameImpl.createTopLevelFrame();
					addon.createUI(toolFrame);
					toolFrame.setVisible(true);
				}
			}
			toolButton.addActionListener(new NewToolListener(toolAddon, addonID));
			add(toolButton);
			return;
		}
		
		ScriptDefinitionDTO[] scriptDefinitions = ScriptDefinitionManager.getScriptDefinitions();
		for(ScriptDefinitionDTO scriptDefinition : scriptDefinitions)
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
					private final ScriptDefinitionDTO scriptDefinition;
					NewScriptListener(ScriptDefinitionDTO scriptDefinition)
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
