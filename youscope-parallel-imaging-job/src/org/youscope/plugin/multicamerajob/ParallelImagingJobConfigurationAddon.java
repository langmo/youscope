/*******************************************************************************
 * Copyright (c) 2017 Moritz Lang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Moritz Lang - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package org.youscope.plugin.multicamerajob;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonUIAdapter;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.clientinterfaces.StandardProperty;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.job.basicjobs.ImagingJob;
import org.youscope.common.microscope.Channel;
import org.youscope.common.microscope.Device;
import org.youscope.common.microscope.DeviceType;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.StandardFormats;

/**
 * @author langmo
 * 
 */
class ParallelImagingJobConfigurationAddon extends ComponentAddonUIAdapter<ParallelImagingJobConfiguration>
{
	private JTextField									nameField				= new JTextField();

	private JLabel										nameLabel				= new JLabel(
																							"Image name used for saving:");
	private JCheckBox										saveImagesField			= new JCheckBox(
																							"Save images",
																							true);

	private JComboBox<String>								configGroupField		= new JComboBox<String>();

	private JComboBox<String>								channelField			= new JComboBox<String>();
	
	private boolean newJob = true;


	private static GridBagConstraints						newLineConstr			= StandardFormats
																							.getNewLineConstraint();

	private Vector<CameraPanel>								cameras					= new Vector<CameraPanel>();

	private String[]										cameraDevices			= new String[0];

	/**
	 * Constructor.
	 * @param client Interface to the client.
	 * @param server Interface to the server.
	 * @throws AddonException 
	 */
	public ParallelImagingJobConfigurationAddon(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(),  client, server);
	}
    
	static ComponentMetadataAdapter<ParallelImagingJobConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<ParallelImagingJobConfiguration>(ParallelImagingJobConfiguration.TYPE_IDENTIFIER, 
				ParallelImagingJobConfiguration.class, 
				ImagingJob.class, 
				"Multi-Camera Imaging", 
				new String[]{"Multi-Camera"}, 
				"Uses several cameras to simultanuously take several images. Requires that the number of pixels per image and the number of bytes per pixel are equal for each camera. Experimental.",
				"icons/images-stack.png");
	}
	@Override
	protected Component createUI(ParallelImagingJobConfiguration configuration) throws AddonException
	{
		setTitle("Multi-Camera Imaging Job");
		setResizable(true);
		setMaximizable(false);
		
		try
		{
			Device[] cameras = getServer().getMicroscope().getDevices(DeviceType.CameraDevice);
			cameraDevices = new String[cameras.length];
			for(int i=0; i<cameras.length; i++)
			{
				cameraDevices[i] = cameras[i].getDeviceID();
			}
		}
		catch (Exception e2)
		{
			sendErrorMessage("Could not detect installed cameras.", e2);
			cameraDevices = new String[0];
		}

		// Cameras panel
		GridBagLayout camerasLayout = new GridBagLayout();
		JPanel camerasPanel = new JPanel(camerasLayout);
		for (String camera : cameraDevices)
		{
			CameraPanel cameraPanel = new CameraPanel(camera);
			cameras.addElement(cameraPanel);
			StandardFormats.addGridBagElement(cameraPanel, camerasLayout, newLineConstr,
					camerasPanel);
		}
		StandardFormats.addGridBagElement(new JPanel(), camerasLayout, StandardFormats.getBottomContstraint(), camerasPanel);
		camerasPanel.setBorder(new TitledBorder("Cameras"));

		// Image saving panel
		GridBagLayout imageSavingLayout = new GridBagLayout();
		JPanel imageSavingPanel = new JPanel(imageSavingLayout);
		imageSavingPanel.setBorder(new TitledBorder("Image Saving"));
		StandardFormats.addGridBagElement(saveImagesField, imageSavingLayout, newLineConstr,
				imageSavingPanel);
		saveImagesField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				boolean selected = saveImagesField.isSelected();
				nameLabel.setVisible(selected);
				nameField.setVisible(selected);

				getContainingFrame().pack();
			}
		});
		StandardFormats.addGridBagElement(nameLabel, imageSavingLayout, newLineConstr,
				imageSavingPanel);
		StandardFormats.addGridBagElement(nameField, imageSavingLayout, newLineConstr,
				imageSavingPanel);
		
		StandardFormats.addGridBagElement(new JPanel(), imageSavingLayout, StandardFormats.getBottomContstraint(), imageSavingPanel);
		
		// Channel panel
		GridBagLayout channelLayout = new GridBagLayout();
		JPanel channelPanel = new JPanel(channelLayout);
		channelPanel.setBorder(new TitledBorder("Channel Configuration:"));
		StandardFormats.addGridBagElement(new JLabel("Channel Group:"), channelLayout, newLineConstr, channelPanel);
		StandardFormats.addGridBagElement(configGroupField, channelLayout, newLineConstr, channelPanel);
		StandardFormats.addGridBagElement(new JLabel("Channel:"), channelLayout, newLineConstr, channelPanel);
		StandardFormats.addGridBagElement(channelField, channelLayout, newLineConstr, channelPanel);
		
		
		// Central Panel
		JPanel elementsPanel = new JPanel(new GridLayout(1,2));
		elementsPanel.add(camerasPanel);
		GridBagLayout rightLayout = new GridBagLayout();
		JPanel rightPanel = new JPanel(rightLayout);
		StandardFormats.addGridBagElement(channelPanel, rightLayout, newLineConstr, rightPanel);
		StandardFormats.addGridBagElement(imageSavingPanel, rightLayout, newLineConstr, rightPanel);
		elementsPanel.add(rightPanel);
		
		// Load state
		if (!newJob)
		{
			String[] selectedCameras = configuration.getCameras();
			double[] selectedExposures = configuration.getExposures();
			for (int i = 0; i < selectedCameras.length
					&& i < selectedExposures.length; i++)
			{
				for (CameraPanel camera : cameras)
				{
					if (camera.getDevice().compareToIgnoreCase(
							selectedCameras[i]) == 0)
					{
						camera.setSelected(true);
						camera.setExposure(selectedExposures[i]);
						break;
					}
				}
			}
		}
		else
		{
			for (CameraPanel camera : cameras)
			{
				camera.setSelected(true);
			}
		}
		
		loadConfigGroupNames();

		// Load state
		String configGroup = configuration.getChannelGroup();
		if(configGroup == null || configGroup.length() < 1)
			configGroup = (String) getClient().getPropertyProvider().getProperty(StandardProperty.PROPERTY_LAST_CHANNEL_GROUP);
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
					if(lastItem != null && lastItem.compareToIgnoreCase(nameField.getText()) == 0)
					{
						String newName = itemEvent.getItem().toString();
						if(newName.length() > 3)
							newName = newName.substring(0, 3);
						nameField.setText(newName);
					}
				}
			}
		});

		saveImagesField.setSelected(configuration.isSaveImages());

		String name = configuration.getImageSaveName();
		if (name.length() < 1)
		{
			name = channelField.getSelectedItem().toString();
			if(name.length() > 3)
				name = name.substring(0, 3);
		}
		nameField.setText(name);

		return elementsPanel;
	}

	private class CameraPanel extends JPanel
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= 2734953846897939033L;
		private final JCheckBox			shouldUseBox;
		private final JFormattedTextField	exposure			= new JFormattedTextField(
																StandardFormats.getDoubleFormat());
		private final JLabel exposureLabel;
		private final String				camera;

		CameraPanel(String camera)
		{
			this.camera = camera;
			
			exposureLabel = new JLabel("Exposure Camera \"" + camera + "\":");
			
			shouldUseBox = new JCheckBox("Image Camera \"" + camera + "\"");
			shouldUseBox.setSelected(false);
			exposure.setEditable(false);
			shouldUseBox.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					exposureLabel.setVisible(shouldUseBox.isSelected());
					exposure.setVisible(shouldUseBox.isSelected());
				}
			});
			GridBagLayout elementsLayout = new GridBagLayout();
			setLayout(elementsLayout);
			StandardFormats.addGridBagElement(shouldUseBox, elementsLayout, newLineConstr, this);
			StandardFormats.addGridBagElement(exposureLabel, elementsLayout,
					newLineConstr, this);
			StandardFormats.addGridBagElement(exposure, elementsLayout, newLineConstr, this);
			
			setExposure(20.0);
		}

		public void setSelected(boolean selected)
		{
			shouldUseBox.setSelected(selected);
			exposure.setEditable(selected);
		}

		public boolean isSelected()
		{
			return shouldUseBox.isSelected();
		}

		public String getDevice()
		{
			return camera;
		}

		public void setExposure(double exposure)
		{
			this.exposure.setValue(exposure);
		}

		public double getExposure()
		{
			return ((Number)exposure.getValue()).doubleValue();
		}
	}
	
	private void loadConfigGroupNames()
	{
		String[] configGroupNames = null;
		try
		{
			configGroupNames = getServer().getMicroscope().getChannelManager().getChannelGroupIDs();
		}
		catch(Exception e)
		{
			sendErrorMessage("Could not obtain config group names.", e);
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
				Channel[] channels = getServer().getMicroscope().getChannelManager().getChannels(selectedGroup.toString()); 
				channelNames = new String[channels.length];
				for(int i=0; i<channels.length; i++)
				{
					channelNames[i] = channels[i].getChannelID();
				}
			}
			catch(Exception e)
			{
				sendErrorMessage("Could not obtain channel names of microscope.", e);
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

	@Override
	protected void commitChanges(ParallelImagingJobConfiguration configuration) {
		Vector<String> cameraDevicesVector = new Vector<String>();
		Vector<Double> exposuresVector = new Vector<Double>();
		for (int i = 0; i < cameras.size(); i++)
		{
			CameraPanel camera = cameras.elementAt(i);
			if (!camera.isSelected())
				continue;
			cameraDevicesVector.addElement(camera.getDevice());
			exposuresVector.addElement(camera.getExposure());
		}
		String[] cameraDevices = cameraDevicesVector
				.toArray(new String[cameraDevicesVector.size()]);

		double[] exposures = new double[cameraDevicesVector.size()];
		for (int i = 0; i < exposuresVector.size(); i++)
		{
			exposures[i] = exposuresVector.elementAt(i);
		}

		configuration.setCameras(cameraDevices);
		configuration.setExposures(exposures);
		configuration.setSaveImages(saveImagesField
				.isSelected());
		configuration.setImageSaveName(nameField
				.getText());
		configuration.setChannel(configGroupField.getSelectedItem().toString(), channelField.getSelectedItem().toString());
		
	}

	@Override
	protected void initializeDefaultConfiguration(ParallelImagingJobConfiguration configuration) throws AddonException {
		// do nothing.
	}
}
