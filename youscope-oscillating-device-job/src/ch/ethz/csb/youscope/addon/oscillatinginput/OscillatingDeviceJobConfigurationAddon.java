/**
 * 
 */
package ch.ethz.csb.youscope.addon.oscillatinginput;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.YouScopeFrame;
import ch.ethz.csb.youscope.client.addon.job.JobConfigurationAddon;
import ch.ethz.csb.youscope.client.addon.job.JobConfigurationAddonListener;
import ch.ethz.csb.youscope.client.uielements.StandardFormats;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;
import ch.ethz.csb.youscope.shared.microscope.Device;
import ch.ethz.csb.youscope.shared.microscope.Property;

/**
 * @author langmo
 */
class OscillatingDeviceJobConfigurationAddon implements JobConfigurationAddon
{
    private OscillatingDeviceJobConfigurationDTO job = new OscillatingDeviceJobConfigurationDTO();

    private JComboBox<String> deviceField;

    private JComboBox<String> propertyField;

    private JFormattedTextField minValueField = new JFormattedTextField(
            StandardFormats.getDoubleFormat());

    private JFormattedTextField maxValueField = new JFormattedTextField(
            StandardFormats.getDoubleFormat());

    private JFormattedTextField periodLengthField = new JFormattedTextField(
            StandardFormats.getDoubleFormat());

    private JFormattedTextField phaseField = new JFormattedTextField(
            StandardFormats.getDoubleFormat());

    private String[] devices;

    private String[] deviceProperties;

    private YouScopeFrame									frame;
    private YouScopeClient client; 
	private YouScopeServer server;
	private Vector<JobConfigurationAddonListener> configurationListeners = new Vector<JobConfigurationAddonListener>();
	
	/**
	 * Constructor.
	 * @param client Interface to the client.
	 * @param server Interface to the server.
	 */
    public OscillatingDeviceJobConfigurationAddon(YouScopeClient client, YouScopeServer server)
    {
    	this.client = client;
		this.server = server;
    }
    
    @Override
	public void createUI(YouScopeFrame frame)
	{
		this.frame = frame;
		frame.setTitle("Oscillating Input Job");
		frame.setResizable(false);
		frame.setClosable(true);
		frame.setMaximizable(false);
		
		GridBagLayout elementsLayout = new GridBagLayout();
		JPanel elementsPanel = new JPanel(elementsLayout);
        GridBagConstraints newLineConstr = StandardFormats.getNewLineConstraint();

        StandardFormats.addGridBagElement(new JLabel("Device:"), elementsLayout, newLineConstr, elementsPanel);
        deviceField = new JComboBox<String>();
        loadDevices();
        class DeviceFieldActionListener implements ActionListener
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                loadDevicePropertyNames();
            }

        }
        deviceField.addActionListener(new DeviceFieldActionListener());
        StandardFormats.addGridBagElement(deviceField, elementsLayout, newLineConstr, elementsPanel);
        StandardFormats.addGridBagElement(new JLabel("Property:"), elementsLayout, newLineConstr, elementsPanel);
        propertyField = new JComboBox<String>();
        loadDevicePropertyNames();
        StandardFormats.addGridBagElement(propertyField, elementsLayout, newLineConstr, elementsPanel);

        // Minimal and maximal value of the device's property during oscillation.
        StandardFormats.addGridBagElement(new JLabel("Minimal value during oscillation:"), elementsLayout,
                newLineConstr, elementsPanel);
        StandardFormats.addGridBagElement(minValueField, elementsLayout, newLineConstr, elementsPanel);
        StandardFormats.addGridBagElement(new JLabel("Maximal value during oscillation:"), elementsLayout,
                newLineConstr, elementsPanel);
        StandardFormats.addGridBagElement(maxValueField, elementsLayout, newLineConstr, elementsPanel);

        // Period length of the oscillation in s
        StandardFormats.addGridBagElement(new JLabel("Period length (ms):"), elementsLayout, newLineConstr, elementsPanel);
        StandardFormats.addGridBagElement(periodLengthField, elementsLayout, newLineConstr, elementsPanel);

        // Initial phase at t=0s in rad (Signal is a sine, not a cosine).
        StandardFormats.addGridBagElement(new JLabel("Initial phase (rad):"), elementsLayout, newLineConstr, elementsPanel);
        StandardFormats.addGridBagElement(phaseField, elementsLayout, newLineConstr, elementsPanel);
        
        JButton addJobButton = new JButton("Add Job");
		addJobButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				JobConfiguration job = getConfigurationData();
				for(JobConfigurationAddonListener listener : configurationListeners)
				{
					listener.jobConfigurationFinished(job);
				}
				OscillatingDeviceJobConfigurationAddon.this.frame.setVisible(false);
			}
		});
		        
        loadSettingsIntoLayout();
        
        JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(elementsPanel, BorderLayout.NORTH);
		contentPane.add(addJobButton, BorderLayout.SOUTH);
		frame.setContentPane(contentPane);
        frame.pack();
    }

    private void loadSettingsIntoLayout()
    {
        // Set state
    	String device = job.getDevice();
    	String property = job.getProperty();
        if (device != null)
        {
            for (int i = 0; i < devices.length; i++)
            {
                if (devices[i].compareTo(device) == 0)
                    deviceField.setSelectedIndex(i);
            }
            if (property != null)
            {
                for (int i = 0; i < deviceProperties.length; i++)
                {
                    if (deviceProperties[i].compareTo(property) == 0)
                        propertyField.setSelectedIndex(i);
                }
            }
        }
        minValueField.setValue(job.getMinValue());
        maxValueField.setValue(job.getMaxValue());
        periodLengthField.setValue(job.getPeriodLength());
        phaseField.setValue(job.getInitialPhase());
    }

    
    @Override
	public void setConfigurationData(JobConfiguration job) throws ConfigurationException
	{
		if(!(job instanceof OscillatingDeviceJobConfigurationDTO))
			throw new ConfigurationException("Configuration not supported by this addon.");
		this.job = (OscillatingDeviceJobConfigurationDTO)job;
	}

	@Override
	public void addConfigurationListener(JobConfigurationAddonListener listener)
	{
		configurationListeners.add(listener);
	}

	@Override
	public void removeConfigurationListener(JobConfigurationAddonListener listener)
	{
		configurationListeners.remove(listener);
	}

    @Override
    public JobConfiguration getConfigurationData()
    {
        // Get parameters
        String device = deviceField.getSelectedItem().toString();
        String property = propertyField.getSelectedItem().toString();
        double minValue = ((Number)minValueField.getValue()).doubleValue();
        double maxValue = ((Number)maxValueField.getValue()).doubleValue();
        int periodLength = ((Number)periodLengthField.getValue()).intValue();
        double initialPhase = ((Number)phaseField.getValue()).doubleValue();

        // Store parameters
        job.setDevice(device);
        job.setInitialPhase(initialPhase);
        job.setMaxValue(maxValue);
        job.setMinValue(minValue);
        job.setPeriodLength(periodLength);
        job.setProperty(property);
        
        return job;
    }

    private void loadDevices()
    {
        deviceField.removeAllItems();

        try
        {
        	Device[] deviceH = server.getMicroscope().getDevices();
            devices = new String[deviceH.length];
            for(int i=0; i<deviceH.length; i++)
            {
            	devices[i] = deviceH[i].getDeviceID();
            }
        } 
        catch (Exception e)
        {
        	client.sendError("Could not load device names.", e);
            devices = new String[0];
        } 
        if (devices == null)
        {
            devices = new String[0];
        }
        for (int i = 0; i < devices.length; i++)
        {
            deviceField.addItem(devices[i]);
        }
    }

    private void loadDevicePropertyNames()
    {
        String device = (String) deviceField.getSelectedItem();
        if (device == null)
            return;

        propertyField.removeAllItems();
        try
        {
        	Property[] properties = server.getMicroscope().getDevice(device).getEditableProperties();        	
            deviceProperties = new String[properties.length];
            for(int i=0; i<properties.length; i++)
            {
            	deviceProperties[i] = properties[i].getPropertyID();
            }
        } 
        catch (Exception e)
        {
        	client.sendError("Could not load device property names for device "+device+".", e);
            deviceProperties = new String[0];
        } 
        if (deviceProperties == null)
        {
            deviceProperties = new String[0];
        }
        for (int i = 0; i < deviceProperties.length; i++)
        {
            propertyField.addItem(deviceProperties[i]);
        }
    }
    
    @Override
	public String getConfigurationID()
	{
		return OscillatingDeviceJobConfigurationDTO.TYPE_IDENTIFIER;
	}
}
