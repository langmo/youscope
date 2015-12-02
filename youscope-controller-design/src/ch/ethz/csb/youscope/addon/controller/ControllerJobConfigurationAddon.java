/**
 * 
 */
package ch.ethz.csb.youscope.addon.controller;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTabbedPane;

import ch.ethz.csb.youscope.client.addon.ConfigurationAddonAdapter;
import ch.ethz.csb.youscope.client.addon.ConfigurationMetadataAdapter;
import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.addon.AddonException;

/**
 * @author Moritz Lang
 */
public class ControllerJobConfigurationAddon extends ConfigurationAddonAdapter<ControllerJobConfiguration>
{
    private OutputConfigurationPanel outputJobConfigurationPanel = null;
    private InputConfigurationPanel inputJobConfigurationPanel = null;
    private AlgorithmConfigurationPanel algorithmConfigurationPanel = null;
	private MiscConfigurationPanel miscConfigurationPanel = null;
    
	/**
	 * Constructor.
	 * @param client Interface to the client.
	 * @param server Interface to the server.
	 * @throws AddonException 
	 */
	public ControllerJobConfigurationAddon(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(),  client, server);
	}
    
	static ConfigurationMetadataAdapter<ControllerJobConfiguration> getMetadata()
	{
		return new ConfigurationMetadataAdapter<ControllerJobConfiguration>(ControllerJobConfiguration.TYPE_IDENTIFIER, 
				ControllerJobConfiguration.class, 
				ControllerJob.class, 
				"controller", 
				new String[]{"feedback"});
	}
    
	@Override
	protected Component createUI(ControllerJobConfiguration configuration) throws AddonException 
	{
		setTitle("Controller Job");
		setResizable(true);
		setMaximizable(true);
		setPreferredSize(new Dimension(800, 600));
		
		outputJobConfigurationPanel = new OutputConfigurationPanel(configuration, getClient(), getContainingFrame());
	    inputJobConfigurationPanel = new InputConfigurationPanel(configuration, getClient(), getContainingFrame());
	    algorithmConfigurationPanel = new AlgorithmConfigurationPanel(configuration, getClient(), getServer());
	    miscConfigurationPanel = new MiscConfigurationPanel(configuration, getContainingFrame());
	    
	    outputJobConfigurationPanel.addActionListener(new ActionListener()
	    		{
					@Override
					public void actionPerformed(ActionEvent e) {
						algorithmConfigurationPanel.setOutputColumns(outputJobConfigurationPanel.getOutputColumns());
					}
	    		});
	    inputJobConfigurationPanel.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) {
				algorithmConfigurationPanel.setInputColumns(inputJobConfigurationPanel.getInputColumns());
			}
		});
	    
		JTabbedPane centralPanel = new JTabbedPane(JTabbedPane.TOP);
        centralPanel.addTab("Input", inputJobConfigurationPanel);
        centralPanel.addTab("Output", outputJobConfigurationPanel);
        centralPanel.addTab("Control Algorithm", algorithmConfigurationPanel);
        centralPanel.addTab("Misc", miscConfigurationPanel);
        
		return centralPanel;
    }
	
	@Override
	protected void commitChanges(ControllerJobConfiguration configuration)
	{
		inputJobConfigurationPanel.commitChanges(configuration);
		outputJobConfigurationPanel.commitChanges(configuration);
		miscConfigurationPanel.commitChanges(configuration);
		algorithmConfigurationPanel.commitChanges(configuration);
    }
}
