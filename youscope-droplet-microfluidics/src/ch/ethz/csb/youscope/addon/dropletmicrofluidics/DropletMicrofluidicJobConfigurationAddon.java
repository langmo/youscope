/**
 * 
 */
package ch.ethz.csb.youscope.addon.dropletmicrofluidics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import ch.ethz.csb.youscope.addon.autofocus.AutoFocusJobConfiguration;
import ch.ethz.csb.youscope.client.addon.ConfigurationAddon;
import ch.ethz.csb.youscope.client.addon.ConfigurationAddonAdapter;
import ch.ethz.csb.youscope.client.addon.ConfigurationMetadataAdapter;
import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.uielements.ComponentComboBox;
import ch.ethz.csb.youscope.client.uielements.DynamicPanel;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.addon.AddonException;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationMetadata;
import ch.ethz.csb.youscope.shared.measurement.job.Job;
import ch.ethz.csb.youscope.shared.microscope.Device;
import ch.ethz.csb.youscope.shared.resource.dropletmicrofluidics.DropletControllerConfiguration;
import ch.ethz.csb.youscope.shared.resource.dropletmicrofluidics.DropletControllerConfigurationAddon;
import ch.ethz.csb.youscope.shared.resource.dropletmicrofluidics.DropletObserverConfiguration;

/**
 * @author langmo
 */
class DropletMicrofluidicJobConfigurationAddon extends ConfigurationAddonAdapter<DropletMicrofluidicJobConfiguration>
{
    private ConfigurationAddon<? extends AutoFocusJobConfiguration> autofocusAddon = null;
    private ControllerConfigurationPanel controllerConfigurationPanel;
    private ObserverConfigurationPanel observerConfigurationPanel;
	/**
	 * Constructor.
	 * @param client Interface to the client.
	 * @param server Interface to the server.
	 * @throws AddonException 
	 */
	public DropletMicrofluidicJobConfigurationAddon(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(),  client, server);
	}
    
	static ConfigurationMetadataAdapter<DropletMicrofluidicJobConfiguration> getMetadata()
	{
		return new ConfigurationMetadataAdapter<DropletMicrofluidicJobConfiguration>(DropletMicrofluidicJobConfiguration.TYPE_IDENTIFIER, 
				DropletMicrofluidicJobConfiguration.class, 
				Job.class, 
				"droplet-based microfluidics", 
				new String[]{"feedback"});
	}
	
	@Override
	protected Component createUI(DropletMicrofluidicJobConfiguration configuration) throws AddonException
	{
		setTitle("Droplet-based microfluidics");
		setResizable(true);
		setMaximizable(false);
		
		autofocusAddon = getClient().getAddonProvider().createConfigurationAddon(AutoFocusJobConfiguration.TYPE_IDENTIFIER, AutoFocusJobConfiguration.class);
		AutoFocusJobConfiguration autofocusConfiguration = configuration.getAutofocusConfiguration();
		if(autofocusConfiguration != null)
			try {
				autofocusAddon.setConfiguration(autofocusConfiguration);
			} catch (ConfigurationException e) {
				throw new AddonException("Autofocus configuration is invalid.", e);
			}
		Component autofocusPanel = autofocusAddon.toPanel(getContainingFrame());
		
		JTabbedPane contentPane = new JTabbedPane(JTabbedPane.TOP);
		contentPane.addTab("Autofocus Configuration", autofocusPanel);
		observerConfigurationPanel = new ObserverConfigurationPanel(configuration);
		contentPane.addTab("Observer Configuration", observerConfigurationPanel);
		controllerConfigurationPanel = new ControllerConfigurationPanel(configuration);
		contentPane.addTab("Controller Configuration", controllerConfigurationPanel);
		return contentPane; 
    }
	
	private class ControllerConfigurationPanel extends DynamicPanel
	{
		/**
		 * Serial Version UID.
		 */
		private static final long serialVersionUID = -4969819162642361145L;
		private final ComponentComboBox<DropletControllerConfiguration>	controllerTypeField;
		private Component addonConfigurationPanel = null;
		private ConfigurationAddon<? extends DropletControllerConfiguration> currentAddon = null;
		private final JComboBox<String> nemesysDeviceField = new JComboBox<String>();
		
		ControllerConfigurationPanel(DropletMicrofluidicJobConfiguration configuration) throws AddonException
		{
			String[] nemesysDeviceIds = getNemesysDevices();
			for(String nemesysDeviceId : nemesysDeviceIds)
			{
				nemesysDeviceField.addItem(nemesysDeviceId);
			}
			if(configuration.getNemesysDevice() != null)
				nemesysDeviceField.setSelectedItem(configuration.getNemesysDevice());
			nemesysDeviceField.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0) 
				{
					if(currentAddon != null && currentAddon instanceof DropletControllerConfigurationAddon<?>)
					{
						int numSyringes = getNumSyringes((String)nemesysDeviceField.getSelectedItem());
						((DropletControllerConfigurationAddon<?>)currentAddon).setNumFlowUnits(numSyringes);
					}
				}
			});
			add(new JLabel("Nemesys device:"));
			add(nemesysDeviceField);
			
			DropletControllerConfiguration lastConfiguration = configuration.getControllerConfiguration();
			controllerTypeField = new ComponentComboBox<DropletControllerConfiguration>(getClient(), DropletControllerConfiguration.class);
			if(lastConfiguration != null)
				controllerTypeField.setSelectedElement(lastConfiguration.getTypeIdentifier());
			controllerTypeField.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e) {
					displayAddonConfiguration(controllerTypeField.getSelectedElement());
					
				}
			});
			add(new JLabel("Controller stategy:"));
			add(controllerTypeField);
			displayAddonConfiguration(controllerTypeField.getSelectedElement());
		}
		private Component createErrorUI(String message, Exception exception)
		{
			if(exception != null)
				message += "\n\n"+exception.getMessage();
			JTextArea textArea = new JTextArea(message);
			textArea.setEditable(false);
			JPanel errorPane = new JPanel(new BorderLayout());
			errorPane.add(new JLabel("An error occured:"), BorderLayout.NORTH);
			errorPane.add(new JScrollPane(textArea), BorderLayout.CENTER);
			return errorPane;
		}
		private void displayAddonConfiguration(ConfigurationMetadata<? extends DropletControllerConfiguration> controllerOption)
		{
			if(currentAddon != null)
			{
				ConfigurationMetadata<?> metadata = currentAddon.getConfigurationMetadata();
				if(metadata!= null && controllerOption!= null && metadata.getTypeIdentifier().equals(controllerOption.getTypeIdentifier()))
					return;
				currentAddon = null;
			}
			if(addonConfigurationPanel != null)
				remove(addonConfigurationPanel);
			if(controllerOption == null)
			{
				addonConfigurationPanel = createErrorUI("No droplet based microfluidics controller algorithms available.", null);
				addFill(addonConfigurationPanel);
				revalidate();
				if(getContainingFrame().isVisible())
					getContainingFrame().pack();
				return;
			}
			try 
			{
				currentAddon = getClient().getAddonProvider().createConfigurationAddon(controllerOption);
				if(currentAddon instanceof DropletControllerConfigurationAddon<?>)
				{
					int numSyringes = getNumSyringes((String)nemesysDeviceField.getSelectedItem());
					((DropletControllerConfigurationAddon<?>)currentAddon).setNumFlowUnits(numSyringes);
				}
				
				DropletControllerConfiguration currentConfiguration = getConfiguration().getControllerConfiguration();
				
				if(currentConfiguration != null && currentConfiguration.getTypeIdentifier().equals(controllerOption.getTypeIdentifier()))
				{
					currentAddon.setConfiguration(currentConfiguration);
				}
				addonConfigurationPanel = currentAddon.toPanel(getContainingFrame());
			} 
			catch (Exception e) 
			{
				addonConfigurationPanel = createErrorUI("Error loading droplet based microfluidics controller configuration interface.", e);
				addFill(addonConfigurationPanel);
				sendErrorMessage("Error loading droplet based microfluidics controller configuration interface.",  e);
				revalidate();
				if(getContainingFrame().isVisible())
					getContainingFrame().pack();
				return;
			}
			
			addFill(addonConfigurationPanel);
			revalidate();
			revalidate();
			if(getContainingFrame().isVisible())
				getContainingFrame().pack();
		}
		
		void commitChanges(DropletMicrofluidicJobConfiguration configuration)
		{
			String nemesysDevice = (String)nemesysDeviceField.getSelectedItem();
	    	configuration.setNemesysDevice(nemesysDevice);
	    	
			configuration.setControllerConfiguration(currentAddon==null ? null : currentAddon.getConfiguration());
		}
	}
	
	private class ObserverConfigurationPanel extends DynamicPanel
	{
		/**
		 * Serial Version UID.
		 */
		private static final long serialVersionUID = -4969119162642361145L;
		private Component addonConfigurationPanel = null;
		private ConfigurationAddon<? extends DropletObserverConfiguration> currentAddon = null;
		private final ComponentComboBox<DropletObserverConfiguration>	observerTypeField;
		
		ObserverConfigurationPanel(DropletMicrofluidicJobConfiguration configuration) throws AddonException
		{
			DropletObserverConfiguration lastConfiguration = configuration.getObserverConfiguration();
			observerTypeField = new ComponentComboBox<DropletObserverConfiguration>(getClient(), DropletObserverConfiguration.class);
			if(lastConfiguration != null)
				observerTypeField.setSelectedElement(lastConfiguration.getTypeIdentifier());
			observerTypeField.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e) {
					displayAddonConfiguration(observerTypeField.getSelectedElement());
					
				}
			});
			add(new JLabel("Observer stategy:"));
			add(observerTypeField);
			displayAddonConfiguration(observerTypeField.getSelectedElement());
		}
		private Component createErrorUI(String message, Exception exception)
		{
			if(exception != null)
				message += "\n\n"+exception.getMessage();
			JTextArea textArea = new JTextArea(message);
			textArea.setEditable(false);
			JPanel errorPane = new JPanel(new BorderLayout());
			errorPane.add(new JLabel("An error occured:"), BorderLayout.NORTH);
			errorPane.add(new JScrollPane(textArea), BorderLayout.CENTER);
			return errorPane;
		}
		private void displayAddonConfiguration(ConfigurationMetadata<? extends DropletObserverConfiguration> observerOption)
		{
			if(currentAddon != null)
			{
				ConfigurationMetadata<?> metadata = currentAddon.getConfigurationMetadata();
				if(metadata!= null && observerOption!= null && metadata.getTypeIdentifier().equals(observerOption.getTypeIdentifier()))
					return;
				currentAddon = null;
			}
			if(addonConfigurationPanel != null)
				remove(addonConfigurationPanel);
			if(observerOption == null)
			{
				addonConfigurationPanel = createErrorUI("No droplet based microfluidics observer algorithms available.", null);
				addFill(addonConfigurationPanel);
				revalidate();
				if(getContainingFrame().isVisible())
					getContainingFrame().pack();
				return;
			}
			try 
			{
				currentAddon = getClient().getAddonProvider().createConfigurationAddon(observerOption.getTypeIdentifier(), DropletObserverConfiguration.class);
				
				DropletObserverConfiguration currentConfiguration = getConfiguration().getObserverConfiguration();
				
				if(currentConfiguration != null && currentConfiguration.getTypeIdentifier().equals(observerOption.getTypeIdentifier()))
				{
					currentAddon.setConfiguration(currentConfiguration);
				}
				addonConfigurationPanel = currentAddon.toPanel(getContainingFrame());
			} 
			catch (Exception e) 
			{
				addonConfigurationPanel = createErrorUI("Error loading droplet based microfluidics observer configuration interface.", e);
				addFill(addonConfigurationPanel);
				sendErrorMessage("Error loading droplet based microfluidics observer configuration interface.",  e);
				revalidate();
				if(getContainingFrame().isVisible())
					getContainingFrame().pack();
				return;
			}
			
			addFill(addonConfigurationPanel);
			revalidate();
			revalidate();
			if(getContainingFrame().isVisible())
				getContainingFrame().pack();
		}
		
		void commitChanges(DropletMicrofluidicJobConfiguration configuration)
		{
			configuration.setObserverConfiguration(currentAddon == null ? null : currentAddon.getConfiguration());
		}
	}

	private int getNumSyringes(String nemesysDeviceID)
	{
		if(nemesysDeviceID == null)
			return 0;
		
		try
		{
			Device nemesysDevice = getServer().getMicroscope().getDevice(nemesysDeviceID);
			return Integer.parseInt(nemesysDevice.getProperty("numDosingUnits").getValue());
		}
		catch(NumberFormatException e)
		{
			sendErrorMessage("Could not parse number of flow units.", e);
			return 0;
		}
		catch(Exception e)
		{
			sendErrorMessage("Could not obtain number of dosing units.", e);
			return 0;
		}
	}
	
	private String[] getNemesysDevices()
	{
		ArrayList<String> nemesysDevices = new ArrayList<String>(1);
		try
		{
			Device[] devices = getServer().getMicroscope().getDevices();
			for(Device device : devices)
			{
				if(device.getDriverID().equals("Nemesys") && device.getLibraryID().equals("NemesysPump"))
				{
					nemesysDevices.add(device.getDeviceID());
				}
			}
		}
		catch(Exception e)
		{
			getClient().sendError("Could not load Nemesys device IDs.", e);
			return new String[0];
		}
		
		return nemesysDevices.toArray(new String[nemesysDevices.size()]);
	}
	
	@Override
	protected void commitChanges(DropletMicrofluidicJobConfiguration configuration)
	{
		configuration.setAutofocusConfiguration(autofocusAddon.getConfiguration());
		controllerConfigurationPanel.commitChanges(configuration);
		observerConfigurationPanel.commitChanges(configuration);
	}
}
