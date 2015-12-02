/**
 * 
 */
package ch.ethz.csb.youscope.addon.continousimaging;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.rmi.RemoteException;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JTextField;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.YouScopeFrame;
import ch.ethz.csb.youscope.client.addon.YouScopeProperties;
import ch.ethz.csb.youscope.client.addon.job.JobConfigurationAddon;
import ch.ethz.csb.youscope.client.addon.job.JobConfigurationAddonListener;
import ch.ethz.csb.youscope.client.uielements.DynamicPanel;
import ch.ethz.csb.youscope.client.uielements.ImagePanel;
import ch.ethz.csb.youscope.client.uielements.IntegerTextField;
import ch.ethz.csb.youscope.client.uielements.StandardFormats;
import ch.ethz.csb.youscope.shared.ImageEvent;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;
import ch.ethz.csb.youscope.shared.microscope.CameraDevice;
import ch.ethz.csb.youscope.shared.microscope.Channel;
import ch.ethz.csb.youscope.shared.tools.ImageConvertException;

/**
 * @author Moritz Lang
 */
class ShortContinuousImagingJobConfigurationAddon implements JobConfigurationAddon
{

	private YouScopeFrame									frame;
	private final YouScopeClient client; 
	private final YouScopeServer server;
	private final Vector<JobConfigurationAddonListener> configurationListeners = new Vector<JobConfigurationAddonListener>();

	private final JComboBox<String>								configGroupField		= new JComboBox<String>();

	private final JComboBox<String>								channelField			= new JComboBox<String>();

	private final JFormattedTextField					exposureField			= new JFormattedTextField(StandardFormats.getDoubleFormat());
	private final JFormattedTextField					imagingPeriodField			= new JFormattedTextField(StandardFormats.getIntegerFormat());
	private final JLabel imagingPeriodLabel = new JLabel("Imaging Period (ms):");

	private final JCheckBox								saveImagesField			= new JCheckBox("Save images", true);

	private final JTextField									imageNameField				= new JTextField();

	private final JLabel										imageNameLabel				= new JLabel(
																							"Image name used for saving:");
	private final JLabel 					cameraLabel = new JLabel("Camera:");
	private final JComboBox<String>					cameraField		= new JComboBox<String>();
	
	private final JComboBox<String>									imagingSpeedField = new JComboBox<String>(new String[]{"Burst", "Given Period"});

	private final IntegerTextField numImagesField = new IntegerTextField(10);
	
	private ShortContinuousImagingJobConfiguration	job = new ShortContinuousImagingJobConfiguration();
	
	ShortContinuousImagingJobConfigurationAddon(YouScopeClient client, YouScopeServer server)
	{
		this.client = client;
		this.server = server;
	}
	
	
	
	@Override
	public void createUI(YouScopeFrame frame)
	{
		this.frame = frame;
		frame.setTitle("Short Continuous Imaging");
		frame.setResizable(true);
		frame.setClosable(true);
		frame.setMaximizable(false);

		frame.startInitializing();
		(new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					initilizeFrame();
				}
				catch(RemoteException e)
				{
					ShortContinuousImagingJobConfigurationAddon.this.frame.setToErrorState("Could not initialize frame", e);
				}
				ShortContinuousImagingJobConfigurationAddon.this.frame.endLoading();
			}
		})).start();
	}
	private void initilizeFrame() throws RemoteException
	{
		
		saveImagesField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				boolean selected = saveImagesField.isSelected();
				imageNameLabel.setVisible(selected);
				imageNameField.setVisible(selected);

				ShortContinuousImagingJobConfigurationAddon.this.frame.pack();
			}
		});
		
		configGroupField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				loadChannels();
			}
		});
		channelField.addItemListener(new ItemListener()
		{
			private String	lastItem	= null;

			@Override
			public void itemStateChanged(ItemEvent itemEvent)
			{
				if(itemEvent.getStateChange() == ItemEvent.DESELECTED)
				{
					lastItem = itemEvent.getItem().toString();
					if(lastItem.length() > 3)
						lastItem = lastItem.substring(0, 3);
				}
				else
				{
					if(lastItem != null && lastItem.compareToIgnoreCase(imageNameField.getText()) == 0)
					{
						String newName = itemEvent.getItem().toString();
						if(newName.length() > 3)
							newName = newName.substring(0, 3);
						imageNameField.setText(newName);
					}
				}
			}
		});
		imagingSpeedField.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent itemEvent)
			{
				if(imagingSpeedField.getSelectedIndex() == 0)
				{
					imagingPeriodLabel.setVisible(false);
					imagingPeriodField.setVisible(false);
				}
				else
				{
					imagingPeriodLabel.setVisible(true);
					imagingPeriodField.setVisible(true);
				}
				frame.pack();
			}
		});

		// Load data from configuration
		ShortContinuousImagingJobConfiguration configuration = getConfigurationData();
		
		int numCams = loadCameras();
		if(numCams <= 0)
		{
			frame.setToErrorState("No camera device defined.\nAdd camera before proceeding.", new Exception("No camera device defined.\nAdd camera before proceeding."));
			return;
		}
		else if(numCams>1)
		{
			cameraField.setVisible(true);
			cameraLabel.setVisible(true);
			
			String camera = configuration.getCamera();
			if(camera == null || camera.length() < 1)
			{
				// get default camera
				camera = getDefaultCameraName();
			}
			if(camera != null)
			{
				cameraField.setSelectedItem(camera);
			}
		}
		else
		{
			cameraField.setVisible(false);
			cameraLabel.setVisible(false);
		}
		
		loadConfigGroupNames();
		String configGroup = configuration.getChannelGroup();
		if(configGroup == null || configGroup.length() < 1)
			configGroup = client.getProperties().getProperty(YouScopeProperties.PROPERTY_LAST_CHANNEL_GROUP, "");
		for(int i = 0; i < configGroupField.getItemCount(); i++)
		{
			if(configGroup.compareTo(configGroupField.getItemAt(i).toString()) == 0)
				configGroupField.setSelectedIndex(i);
		}

		loadChannels();
		for(int i = 0; i < channelField.getItemCount(); i++)
		{
			if(configuration.getChannel().compareTo(channelField.getItemAt(i).toString()) == 0)
				channelField.setSelectedIndex(i);
		}
		
		String imageName = configuration.getImageSaveName();
		if (imageName.length() < 1)
		{
			imageName = channelField.getSelectedItem().toString();
			if (imageName.length() > 3)
				imageName = imageName.substring(0, 3);
		}
		imageNameField.setText(imageName);
		
		exposureField.setValue(configuration.getExposure());
		if(configuration.getImagingPeriod() <= 0)
		{
			imagingPeriodField.setValue(1000);
			imagingPeriodField.setVisible(false);
			imagingPeriodLabel.setVisible(false);
			imagingSpeedField.setSelectedIndex(0);
		}
		else
		{
			imagingPeriodField.setValue(configuration.getImagingPeriod());
			imagingSpeedField.setSelectedIndex(1);
		}

		saveImagesField.setSelected(configuration.getSaveImages());
		
		numImagesField.setValue(configuration.getNumImages());
		numImagesField.setMinimalValue(1);
		
		DynamicPanel contentPane = new DynamicPanel();
		contentPane.add(cameraLabel);
		contentPane.add(cameraField);
				
		contentPane.add(new JLabel("Channel Group:"));
		contentPane.add(configGroupField);

		contentPane.add(new JLabel("Channel:"));
		contentPane.add(channelField);

		contentPane.add(new JLabel("Exposure (ms):"));
		contentPane.add(exposureField);
		
		JButton snapImageButton = new JButton("Snap Image");
		snapImageButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				String channel = (String)channelField.getSelectedItem();
				String channelGroup = (String)configGroupField.getSelectedItem();
				double exposure = ((Number)exposureField.getValue()).doubleValue();
				String camera = (String)cameraField.getSelectedItem();
				snapImage(camera, channelGroup, channel, exposure);
			}
		});
		contentPane.add(snapImageButton);
		
		contentPane.add(new JLabel("Imaging Type:"));
		contentPane.add(imagingSpeedField);
		
		contentPane.add(imagingPeriodLabel);
		contentPane.add(imagingPeriodField);
		
		contentPane.add(new JLabel("Number of Images:"));
		contentPane.add(numImagesField);

		contentPane.add(saveImagesField);
		contentPane.add(imageNameLabel);
		contentPane.add(imageNameField);
		
		contentPane.addFillEmpty();
		
		JButton addJobButton = new JButton("Add Job");
		addJobButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				commitChanges();
				for(JobConfigurationAddonListener listener : configurationListeners)
				{
					listener.jobConfigurationFinished(ShortContinuousImagingJobConfigurationAddon.this.job);
				}

				try
				{
					ShortContinuousImagingJobConfigurationAddon.this.frame.setVisible(false);
				}
				catch(Exception e1)
				{
					// Should not happen!
					client.sendError("Could not close window.", e1);
				}
			}
		});
		contentPane.add(addJobButton);
		
		frame.setContentPane(contentPane);
		frame.pack();
	}
	
	
	
	private int loadCameras()
	{
		String[] cameraNames = null;
		try
		{
			CameraDevice[] cameras = server.getMicroscope().getCameraDevices();
			cameraNames = new String[cameras.length];
			for(int i=0; i< cameras.length; i++)
			{
				cameraNames[i] = cameras[i].getDeviceID();
			}
		}
		catch(RemoteException e)
		{
			client.sendError("Could not obtain names of cameras.", e);
			cameraNames = new String[0];
		}
				
		cameraField.removeAllItems();
		for(String cameraName : cameraNames)
		{
			cameraField.addItem(cameraName);
		}
		return cameraNames.length;
	}
	
	private String getDefaultCameraName()
	{
		try
		{
			CameraDevice defaultCamera = server.getMicroscope().getCameraDevice();
			if(defaultCamera == null)
				return null;
			return defaultCamera.getDeviceID();
		}
		catch(Exception e)
		{
			client.sendError("Could not get name of default camera.", e);
			return null;
		}
	}
	
	private void commitChanges()
	{
		job.setChannel((String)configGroupField.getSelectedItem(), (String)channelField.getSelectedItem());
		job.setExposure(((Number)exposureField.getValue()).doubleValue());
		job.setImagingPeriod(((Number)exposureField.getValue()).intValue());
		job.setSaveImages(saveImagesField.isSelected());
		job.setCamera((String)cameraField.getSelectedItem());
		job.setNumImages(numImagesField.getValue());
		if(imageNameField.getText().length() > 3)
			job.setImageSaveName(imageNameField.getText().substring(0, 3));
		else
			job.setImageSaveName(imageNameField.getText());
		if(imagingSpeedField.getSelectedIndex() == 0)
		{
			job.setImagingPeriod(0);
		}
		else
		{
			job.setImagingPeriod(((Number)imagingPeriodField.getValue()).intValue());
		}
	}

	private void snapImage(String camera, String channelGroup, String channel, double exposure)
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
			private final String camera;
			ImageSnapper(YouScopeFrame childFrame, ImagePanel imagePanel, String camera, String channelGroup, String channel, double exposure)
			{
				this.channel = channel;
				this.channelGroup = channelGroup;
				this.childFrame = childFrame;
				this.imagePanel = imagePanel;
				this.exposure = exposure;
				this.camera = camera;
			}

			@Override
			public void run()
			{
				ImageEvent imageEvent;
				try
				{
					CameraDevice cameraDevice;
					if(camera != null && camera.length() > 0)
						cameraDevice = server.getMicroscope().getCameraDevice(camera);
					else
						cameraDevice = server.getMicroscope().getCameraDevice();
					if(cameraDevice == null)
						throw new Exception("Camera was not found.");
					
					
					imageEvent = cameraDevice.makeImage(channelGroup, channel, exposure);
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
		new Thread(new ImageSnapper(childFrame, imagePanel, camera, channelGroup, channel, exposure)).start();
	}
	
	@Override
	public void setConfigurationData(JobConfiguration job) throws ConfigurationException
	{
		if(!(job instanceof ShortContinuousImagingJobConfiguration))
			throw new ConfigurationException("Configuration not supported by this addon.");
		this.job = (ShortContinuousImagingJobConfiguration)job;
	}

	@Override
	public ShortContinuousImagingJobConfiguration getConfigurationData()
	{
		return job;
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
	public String getConfigurationID()
	{
		return ShortContinuousImagingJobConfiguration.TYPE_IDENTIFIER;
	}
	
	private void loadConfigGroupNames()
	{
		String[] configGroupNames = null;
		try
		{
			configGroupNames = server.getMicroscope().getChannelManager().getChannelGroupIDs();
		}
		catch(Exception e)
		{
			client.sendError("Could not obtain config group names.", e);
		}

		if(configGroupNames == null || configGroupNames.length <= 0)
		{
			configGroupNames = new String[] {""};
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
			catch(Exception e)
			{
				client.sendError("Could not obtain channel names of microscope.", e);
			}
		}

		if(channelNames == null || channelNames.length <= 0)
		{
			channelNames = new String[] {""};
		}

		channelField.removeAllItems();
		for(String channelName : channelNames)
		{
			channelField.addItem(channelName);
		}
	}
}
