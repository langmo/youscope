/**
 * 
 */
package org.youscope.plugin.waitforuser;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonUIAdapter;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DynamicPanel;

/**
 * @author Moritz Lang
 */
class WaitForUserJobConfigurationAddon extends ComponentAddonUIAdapter<WaitForUserJobConfiguration>
{
	private JTextArea							messageField				= new JTextArea(6, 30);
	
	/**
	 * Constructor.
	 * @param client Interface to the client.
	 * @param server Interface to the server.
	 * @throws AddonException 
	 */
	public WaitForUserJobConfigurationAddon(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(),  client, server);
	}
    
	static ComponentMetadataAdapter<WaitForUserJobConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<WaitForUserJobConfiguration>(WaitForUserJobConfiguration.TYPE_IDENTIFIER, 
				WaitForUserJobConfiguration.class, 
				WaitForUserJob.class, 
				"Wait for user", 
				new String[]{"misc"}, "icons/user--exclamation.png");
	}

	@Override
	protected Component createUI(WaitForUserJobConfiguration configuration) throws AddonException
	{
		setTitle("Wait For User");
		setResizable(true);
		setMaximizable(false);
		
		DynamicPanel elementsPanel = new DynamicPanel();
		elementsPanel.add(new JLabel("User message:"));
		elementsPanel.add(new JScrollPane(messageField));

		// Load state
		messageField.setText(configuration.getMessage());

		return elementsPanel;
	}

	@Override
	protected void commitChanges(WaitForUserJobConfiguration configuration) {
		configuration.setMessage(messageField.getText());
	}

	@Override
	protected void initializeDefaultConfiguration(WaitForUserJobConfiguration configuration) throws AddonException {
		// do nothing.
	}
}
