/**
 * 
 */
package ch.ethz.csb.youscope.addon.outoffocus;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.YouScopeFrame;
import ch.ethz.csb.youscope.client.addon.job.JobConfigurationAddon;
import ch.ethz.csb.youscope.client.addon.job.JobConfigurationAddonListener;
import ch.ethz.csb.youscope.client.uielements.ImagePanel;
import ch.ethz.csb.youscope.client.uielements.StandardFormats;
import ch.ethz.csb.youscope.shared.ImageEvent;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.FocusConfiguration;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;
import ch.ethz.csb.youscope.shared.microscope.Channel;
import ch.ethz.csb.youscope.shared.microscope.Device;
import ch.ethz.csb.youscope.shared.microscope.DeviceType;
import ch.ethz.csb.youscope.shared.tools.ImageConvertException;

/**
 * @author langmo
 */
class OutOfFocusJobConfigurationAddon implements JobConfigurationAddon
{

    private OutOfFocusJobConfigurationDTO job = new OutOfFocusJobConfigurationDTO();

    // UI Elements
    private JComboBox<String> configGroupField = new JComboBox<String>();
    private JComboBox<String> channelField  = new JComboBox<String>();
    private JComboBox<String> focusDevicesField = new JComboBox<String>();

    private JFormattedTextField exposureField = new JFormattedTextField(
            StandardFormats.getDoubleFormat());
    
    private JFormattedTextField adjustmentTimeField = new JFormattedTextField(
            StandardFormats.getIntegerFormat());

    private JTextField imageNameField = new JTextField("OOF");

    private JCheckBox saveImagesField = new JCheckBox("Save images", true);

    private JFormattedTextField offsetField = new JFormattedTextField(
            StandardFormats.getDoubleFormat());

    private YouScopeFrame									frame;
	
	private YouScopeClient client; 
	private YouScopeServer server;
	private Vector<JobConfigurationAddonListener> configurationListeners = new Vector<JobConfigurationAddonListener>();

	/**
	 * Constructor.
	 * @param client Interface to the client.
	 * @param server Interface to the server.
	 */
	public OutOfFocusJobConfigurationAddon(YouScopeClient client, YouScopeServer server)
	{
		this.client = client;
		this.server = server;
		job.setImageSaveName("BFout");
	}
    
	@Override
	public void createUI(YouScopeFrame frame)
	{
		this.frame = frame;
		frame.setTitle("Out-of-Focus Job");
		frame.setResizable(false);
		frame.setClosable(true);
		frame.setMaximizable(false);

		GridBagLayout elementsLayout = new GridBagLayout();
		JPanel elementsPanel = new JPanel(elementsLayout);
        GridBagConstraints newLineConstr = StandardFormats.getNewLineConstraint();
        
        StandardFormats.addGridBagElement(new JLabel("Channel Group:"), elementsLayout, newLineConstr,
        		elementsPanel);
        StandardFormats.addGridBagElement(configGroupField, elementsLayout, newLineConstr, elementsPanel);
        StandardFormats.addGridBagElement(new JLabel("Channel:"), elementsLayout, newLineConstr, elementsPanel);
        StandardFormats.addGridBagElement(channelField, elementsLayout, newLineConstr, elementsPanel);

        configGroupField.addActionListener(new ActionListener()
    	{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				loadChannels();
			}
    	});
        
        StandardFormats.addGridBagElement(new JLabel("Exposure (ms):"), elementsLayout, newLineConstr, elementsPanel);
        StandardFormats.addGridBagElement(exposureField, elementsLayout, newLineConstr, elementsPanel);

        StandardFormats.addGridBagElement(new JLabel("Focus Device:"), elementsLayout, newLineConstr, elementsPanel);
        StandardFormats.addGridBagElement(focusDevicesField, elementsLayout, newLineConstr, elementsPanel);
        
        StandardFormats.addGridBagElement(new JLabel("Offset:"), elementsLayout, newLineConstr, elementsPanel);
        StandardFormats.addGridBagElement(offsetField, elementsLayout, newLineConstr, elementsPanel);
        
        StandardFormats.addGridBagElement(new JLabel("Adjustment Time:"), elementsLayout, newLineConstr, elementsPanel);
        StandardFormats.addGridBagElement(adjustmentTimeField, elementsLayout, newLineConstr, elementsPanel);

        StandardFormats.addGridBagElement(new JLabel("Image Name:"), elementsLayout, newLineConstr, elementsPanel);
        StandardFormats.addGridBagElement(imageNameField, elementsLayout, newLineConstr, elementsPanel);
        
        StandardFormats.addGridBagElement(saveImagesField, elementsLayout, newLineConstr, elementsPanel);
        
        StandardFormats.addGridBagElement(new JPanel(), elementsLayout, StandardFormats.getBottomContstraint(), elementsPanel);
        
        JButton snapImageButton = new JButton("Snap Image");
		snapImageButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				String channel = (String)channelField.getSelectedItem();
				String channelGroup = (String)configGroupField.getSelectedItem();
				double exposure = ((Number)exposureField.getValue()).doubleValue();
				String focusDevice = focusDevicesField.getSelectedItem().toString();
				double offset = ((Number) offsetField.getValue()).doubleValue();
				snapImage(channelGroup, channel, exposure, focusDevice, offset);
			}
		});
		StandardFormats.addGridBagElement(snapImageButton, elementsLayout, newLineConstr, elementsPanel);
        
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
				OutOfFocusJobConfigurationAddon.this.frame.setVisible(false);
			}
		});
		
		loadSettingsIntoLayout();
		
		Container contentPane = new JPanel(new BorderLayout());
		contentPane.add(elementsPanel, BorderLayout.NORTH);
		contentPane.add(addJobButton, BorderLayout.SOUTH);
        
		frame.setContentPane(contentPane);
        frame.pack();
    }

	private void snapImage(String channelGroup, String channel, double exposure, String focusDevice, double offset)
	{
		// Create snap image window
		ImagePanel imagePanel = new ImagePanel(client);
		YouScopeFrame childFrame = imagePanel.toFrame();
		frame.addModalChildFrame(childFrame);
		childFrame.setVisible(true);
		childFrame.startLoading();
		
		// Make image
		class ImageSnapper implements Runnable
		{
			private final YouScopeFrame childFrame;
			private final ImagePanel imagePanel;
			private final String channelGroup;
			private final String channel;
			private final double exposure;
			private final String focusDevice;
			private final double offset;
			ImageSnapper(YouScopeFrame childFrame, ImagePanel imagePanel, String channelGroup, String channel, double exposure, String focusDevice, double offset)
			{
				this.channel = channel;
				this.channelGroup = channelGroup;
				this.childFrame = childFrame;
				this.imagePanel = imagePanel;
				this.exposure = exposure;
				this.focusDevice = focusDevice;
				this.offset = offset;
			}
			@Override
			public void run()
			{
				ImageEvent imageEvent;
				try
				{
					server.getMicroscope().getFocusDevice(focusDevice).setRelativeFocusPosition(offset);
					imageEvent = server.getMicroscope().getCameraDevice().makeImage(channelGroup, channel, exposure);
					server.getMicroscope().getFocusDevice(focusDevice).setRelativeFocusPosition(-offset);
				}
				catch(Exception e1)
				{
					childFrame.setToErrorState("Error occured while taking image.", e1);
					return;
				}
				if(imageEvent == null)
				{
					childFrame.setToErrorState("No image was returned by the microscope.", null);
					return;
				}
				
				// Show image
				childFrame.endLoading();
				imagePanel.setImage(imageEvent);
			}
		}
		new Thread(new ImageSnapper(childFrame, imagePanel, channelGroup, channel, exposure, focusDevice, offset)).start();
	}
	
    private void loadConfigGroupNames()
    {
    	String[] configGroupNames = null;
    	try
		{
    		configGroupNames = server.getMicroscope().getChannelManager().getChannelGroupIDs();
		}
		catch (Exception e)
		{
			client.sendError("Could not obtain config group names.", e);
		}
		
		if (configGroupNames == null || configGroupNames.length <= 0)
        {
			configGroupNames = new String[]{""};
        }
		
		configGroupField.removeAllItems();
		for(String configGroupName : configGroupNames)
		{
			configGroupField.addItem(configGroupName);
		}
    }
    private void loadChannels()
    {
    	String[] channelNames = null;
    	
    	Object selectedGroup = configGroupField.getSelectedItem();
    	if(selectedGroup != null && selectedGroup.toString().length() > 0)
    	{
	        try
	        {
	        	Channel[] channels = server.getMicroscope().getChannelManager().getChannels(selectedGroup.toString()); 
				channelNames = new String[channels.length];
				for(int i=0; i<channels.length; i++)
				{
					channelNames[i] = channels[i].getChannelID();
				}
	        } 
	        catch (Exception e)
	        {
	        	client.sendError("Could not obtain channel names of microscope.", e);
	        } 
    	}
    	
        if (channelNames == null || channelNames.length <= 0)
        {
            channelNames  = new String[]{""};
        }
        
        channelField.removeAllItems();
		for(String channelName : channelNames)
		{
			channelField.addItem(channelName);
		}
    }
    private void loadSettingsIntoLayout()
    {
        // Set state
        loadFocusDevices();
        if(job.getFocusConfiguration() != null && job.getFocusConfiguration().getFocusDevice() != null)
        {
        	String focusDevice = job.getFocusConfiguration().getFocusDevice();
        	for (int i = 0; i < focusDevicesField.getItemCount(); i++)
            {
                if (focusDevice.compareTo(focusDevicesField.getItemAt(i).toString()) == 0)
                	focusDevicesField.setSelectedIndex(i);
            }
        }
        
        loadConfigGroupNames();
        if(job.getChannelGroup() != null)
        {
        	for (int i = 0; i < configGroupField.getItemCount(); i++)
            {
                if (job.getChannelGroup().compareTo(configGroupField.getItemAt(i).toString()) == 0)
                	configGroupField.setSelectedIndex(i);
            }
        }
        else
        {
        	loadChannels();
        }
        if (job.getChannel() != null)
        {
            for (int i = 0; i < channelField.getItemCount(); i++)
            {
                if (job.getChannel().compareTo(channelField.getItemAt(i).toString()) == 0)
                    channelField.setSelectedIndex(i);
            }
        }
        exposureField.setValue(job.getExposure());
        imageNameField.setText(job.getImageSaveName());
        if(job.getFocusConfiguration() != null)
        	adjustmentTimeField.setValue(job.getFocusConfiguration().getAdjustmentTime());
        else
        	adjustmentTimeField.setValue(0);

        saveImagesField.setSelected(job.isSaveImages());
        offsetField.setValue(job.getPosition());
    }

    @Override
	public void setConfigurationData(JobConfiguration job) throws ConfigurationException
	{
		if(!(job instanceof OutOfFocusJobConfigurationDTO))
			throw new ConfigurationException("Configuration not supported by this addon.");
		this.job = (OutOfFocusJobConfigurationDTO)job;
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
        String channel = channelField.getSelectedItem().toString();
        String configGroup = configGroupField.getSelectedItem().toString();
        double exposure = ((Number) exposureField.getValue()).doubleValue();
        boolean saveImages = saveImagesField.isSelected();
        String imageName = imageNameField.getText();
        double offset = ((Number) offsetField.getValue()).doubleValue();
        String focusDevice = focusDevicesField.getSelectedItem().toString();
        int adjustmentTime = ((Number)adjustmentTimeField.getValue()).intValue();

        // Store parameters
        job.setChannel(configGroup, channel);
        job.setExposure(exposure);
        FocusConfiguration focusConfiguration = new FocusConfiguration();
        focusConfiguration.setAdjustmentTime(adjustmentTime);
        focusConfiguration.setFocusDevice(focusDevice);
        job.setFocusConfiguration(focusConfiguration);
        job.setImageSaveName(imageName);
        job.setPosition(offset);
        job.setSaveImages(saveImages);

        return job;
    }
    
    private void loadFocusDevices()
	{
    	String[] focusDevices;
    	try
		{
    		Device[] devices = server.getMicroscope().getDevices(DeviceType.StageDevice);
    		focusDevices = new String[devices.length]; 
    		for(int i=0; i<devices.length; i++)
    		{
    			focusDevices[i] = devices[i].getDeviceID();
    		}
		}
		catch (Exception e)
		{
			client.sendError("Could not obtain focus device names.", e);
			focusDevices = new String[0];
		}
		
		focusDevicesField.removeAllItems();
		for(String focusDevice : focusDevices)
		{
			focusDevicesField.addItem(focusDevice);
		}
	}
    @Override
	public String getConfigurationID()
	{
		return OutOfFocusJobConfigurationDTO.TYPE_IDENTIFIER;
	}
}
