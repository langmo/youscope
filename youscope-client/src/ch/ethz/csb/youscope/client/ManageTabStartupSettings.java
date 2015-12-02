/**
 * 
 */
package ch.ethz.csb.youscope.client;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import ch.ethz.csb.youscope.client.uielements.DescriptionPanel;
import ch.ethz.csb.youscope.client.uielements.DeviceSettingsPanel;
import ch.ethz.csb.youscope.shared.microscope.DeviceSettingDTO;

/**
 * @author Moritz Lang
 *
 */
class ManageTabStartupSettings extends ManageTabElement
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 1730251748743397942L;
	private DeviceSettingsPanel deviceSettingPanel = null;
	private boolean somethingChanged = false;
	ManageTabStartupSettings()
	{
		setOpaque(false);
		
		deviceSettingPanel= new DeviceSettingsPanel(new YouScopeClientConnectionImpl(), YouScopeClientImpl.getServer(), true);
		deviceSettingPanel.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				somethingChanged = true;
			}
		});
		
		DescriptionPanel descriptionPanel = new DescriptionPanel("Startup settings are applied by YouScope whenever loading a given microscope configuration, typically during startup of YouScope.\nYouScope can then automatically apply a set of device settings, e.g. turn on lights or change the binning of a camera.");
		
		setLayout(new BorderLayout(5, 5));
		add(descriptionPanel, BorderLayout.NORTH);
		add(deviceSettingPanel, BorderLayout.CENTER);
	}
	
	@Override
	public void initializeContent()
	{
		if(deviceSettingPanel == null)
			return;
		try
		{
			deviceSettingPanel.setSettings(YouScopeClientImpl.getMicroscope().getMicroscopeConfiguration().getSystemStartupSettings());
		}
		catch(Exception e)
		{
			ClientSystem.err.println("Could not obtain current startup settings.", e);
		}
	}

	@Override
	public boolean storeContent()
	{
		if(somethingChanged)
		{
			// Send settings to server.
			DeviceSettingDTO[] startupSettings = deviceSettingPanel.getSettings();
			try
			{
				YouScopeClientImpl.getMicroscope().getMicroscopeConfiguration().setSystemStartupSettings(startupSettings);
			}
			catch(Exception e1)
			{
				ClientSystem.err.println("Could not set startup settings.", e1);
			}
		}
		return somethingChanged;
	}
}
