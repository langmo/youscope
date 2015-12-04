/**
 * 
 */
package org.youscope.plugin.devicesettingmanager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.youscope.addon.tool.ToolAddon;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * @author langmo
 *
 */
class DeviceSettingManager implements ToolAddon
{
	private final DevicesPanel devicesPanel;
	private final PropertiesPanel proertiesPanel;
	
	/**
	 * Constructor.
	 * @param client Interface to the YouScope client.
	 * @param server Interface to the YouScope server.
	 */
	public DeviceSettingManager(YouScopeClient client, YouScopeServer server)
	{
		devicesPanel = new DevicesPanel(client, server);
		proertiesPanel = new PropertiesPanel(client, server);
		devicesPanel.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				proertiesPanel.setSelectedDevice(devicesPanel.getSelectedDevice());
			}
		});
		
	}
	@Override
	public void createUI(YouScopeFrame frame)
	{
		frame.setTitle("Device Setting Manager");
		frame.setResizable(true);
		frame.setClosable(true);
		frame.setMaximizable(true);
		
		JButton refreshButton = new JButton("Refresh");
		refreshButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				//proertiesPanel.setSelectedDevice(null);
				devicesPanel.actualize();
			}
		});
		
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(proertiesPanel, BorderLayout.CENTER);
		contentPane.add(devicesPanel, BorderLayout.WEST);
		contentPane.add(refreshButton, BorderLayout.SOUTH);

		proertiesPanel.setSelectedDevice(null);
		devicesPanel.actualize();
		
		frame.setContentPane(contentPane);
		frame.setSize(new Dimension(600, 600));
	}
}
