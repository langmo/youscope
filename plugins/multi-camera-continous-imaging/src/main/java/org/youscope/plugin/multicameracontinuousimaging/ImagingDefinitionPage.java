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
package org.youscope.plugin.multicameracontinuousimaging;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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

import org.youscope.addon.measurement.MeasurementAddonUIPage;
import org.youscope.clientinterfaces.StandardProperty;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.microscope.Channel;
import org.youscope.common.microscope.Device;
import org.youscope.common.microscope.DeviceType;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.StandardFormats;

class ImagingDefinitionPage extends MeasurementAddonUIPage<MultiCameraContinousImagingConfiguration>
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 2311117369656666466L;
	private final JLabel periodLabel = new JLabel("Imaging Period:");
	private final JLabel periodTypeLabel = new JLabel("Imaging Type:");
	private final JFormattedTextField					periodField			= new JFormattedTextField(StandardFormats.getIntegerFormat());
	private final JComboBox<String>									periodTypeField = new JComboBox<String>(new String[]{"Burst", "Given Period"});
	private JComboBox<String>								configGroupField		= new JComboBox<String>();

	private JComboBox<String>								channelField			= new JComboBox<String>();

	private Vector<CameraPanel>								cameraPanels					= new Vector<CameraPanel>();

	private JCheckBox								saveImagesField			= new JCheckBox("Save images", true);

	private JTextField									imageNameField				= new JTextField();

	private JLabel										imageNameLabel				= new JLabel(
																							"Image name used for saving:");
	private final YouScopeClient client; 
	private final YouScopeServer server; 
	ImagingDefinitionPage(YouScopeClient client, YouScopeServer server)
	{
		this.client = client;
		this.server = server;
	}
	
	@Override
	public void loadData(MultiCameraContinousImagingConfiguration configuration)
	{
		
		String configGroup = configuration.getChannelGroup();
		if(configGroup == null || configGroup.length() < 1)
			configGroup = (String) client.getPropertyProvider().getProperty(StandardProperty.PROPERTY_LAST_CHANNEL_GROUP);
		for(int i = 0; i < configGroupField.getItemCount(); i++)
		{
			if(configGroup.compareTo(configGroupField.getItemAt(i).toString()) == 0)
				configGroupField.setSelectedIndex(i);
		}

		
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
		
		saveImagesField.setSelected(configuration.isSaveImages());
		
		String[] cameras = configuration.getCameras();
		double[] exposures = configuration.getExposures();
		int imagingPeriod = configuration.getImagingPeriod();
		
		periodField.setValue(imagingPeriod);
		
		if(imagingPeriod <= 0)
		{
			periodField.setVisible(false);
			periodLabel.setVisible(false);
			periodTypeField.setSelectedIndex(0);
		}
		else
		{
			periodField.setVisible(true);
			periodLabel.setVisible(true);
			periodTypeField.setSelectedIndex(1);
		}
		
		if(cameras.length != 0 && exposures.length != 0)
		{
			for(CameraPanel cameraPanel : cameraPanels)
			{
				cameraPanel.setSelected(false);
			}
			for(int i=0; i<exposures.length && i < cameras.length; i++)
			{
				for(CameraPanel cameraPanel : cameraPanels)
				{
					if(cameraPanel.getDevice().compareTo(cameras[i]) == 0)
					{
						cameraPanel.setSelected(true);
						cameraPanel.setExposure(exposures[i]);
						break;
					}
				}
			}
		}
		else
		{
			for(CameraPanel cameraPanel : cameraPanels)
			{
				cameraPanel.setSelected(true);
				cameraPanel.setExposure(50);
			}
		}

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

	@Override
	public boolean saveData(MultiCameraContinousImagingConfiguration configuration)
	{
		configuration.setChannel((String)configGroupField.getSelectedItem(), (String)channelField.getSelectedItem());
		
		Vector<CameraPanel> selectedCameraPanels = new Vector<CameraPanel>();
		for(CameraPanel cameraPanel : cameraPanels)
		{
			if(cameraPanel.isSelected())
				selectedCameraPanels.addElement(cameraPanel);
		}
		String[] selectedCameras = new String[selectedCameraPanels.size()];
		double[] selectedExposures = new double[selectedCameraPanels.size()];
		for(int i=0; i<selectedCameraPanels.size(); i++)
		{
			CameraPanel selectedCameraPanel = selectedCameraPanels.elementAt(i);
			selectedCameras[i] = selectedCameraPanel.getDevice();
			selectedExposures[i] = selectedCameraPanel.getExposure();
		}
		
		int imagingPeriod;
		if(periodTypeField.getSelectedIndex() == 0)
			imagingPeriod = 0;
		else
			imagingPeriod = ((Number)periodField.getValue()).intValue();
		
		configuration.setCameras(selectedCameras);
		configuration.setExposures(selectedExposures);
		configuration.setImagingPeriod(imagingPeriod);
		configuration.setSaveImages(saveImagesField.isSelected());
		if(imageNameField.getText().length() > 3)
			configuration.setImageSaveName(imageNameField.getText().substring(0, 3));
		else
			configuration.setImageSaveName(imageNameField.getText());

		return true;
	}

	@Override
	public void setToDefault(MultiCameraContinousImagingConfiguration configuration)
	{
		// Do nothing.
	}

	@Override
	public String getPageName()
	{
		return "Imaging";
	}
	
	private class CameraPanel extends JPanel
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= 2734953846897939031L;
		private final JCheckBox			shouldUseBox;
		private final JFormattedTextField	exposureField			= new JFormattedTextField(StandardFormats.getDoubleFormat());
		
		private final JLabel exposureLabel;
		private final String				camera;

		CameraPanel(String camera)
		{
			GridBagConstraints newLineConstr = StandardFormats.getNewLineConstraint();
			this.camera = camera;
			exposureLabel = new JLabel("Exposure Camera \"" + camera + "\":");
			shouldUseBox = new JCheckBox("Image Camera \"" + camera + "\"");
			shouldUseBox.setSelected(false);
			shouldUseBox.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					exposureLabel.setVisible(shouldUseBox.isSelected());
					exposureField.setVisible(shouldUseBox.isSelected());
					fireSizeChanged();
				}
			});
			
			GridBagLayout elementsLayout = new GridBagLayout();
			setLayout(elementsLayout);
			StandardFormats.addGridBagElement(shouldUseBox, elementsLayout, newLineConstr, this);
			StandardFormats.addGridBagElement(exposureLabel, elementsLayout, newLineConstr, this);
			StandardFormats.addGridBagElement(exposureField, elementsLayout, newLineConstr, this);
			
			setSelected(true);
			setExposure(20.0);
		}

		public void setSelected(boolean selected)
		{
			shouldUseBox.setSelected(selected);
			exposureLabel.setVisible(shouldUseBox.isSelected());
			exposureField.setVisible(shouldUseBox.isSelected());
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
			this.exposureField.setValue(exposure);
		}

		public double getExposure()
		{
			return ((Number)exposureField.getValue()).doubleValue();
		}
	}

	@Override
	public void createUI(YouScopeFrame frame)
	{
		GridBagConstraints newLineConstr = StandardFormats.getNewLineConstraint();
		GridBagLayout elementsLayout = new GridBagLayout();
		setLayout(elementsLayout);
		
		StandardFormats.addGridBagElement(new JLabel("Channel Group:"), elementsLayout, newLineConstr, this);
		StandardFormats.addGridBagElement(configGroupField, elementsLayout, newLineConstr, this);

		StandardFormats.addGridBagElement(new JLabel("Channel:"), elementsLayout, newLineConstr, this);
		StandardFormats.addGridBagElement(channelField, elementsLayout, newLineConstr, this);

		periodTypeField.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent itemEvent)
			{
				if(periodTypeField.getSelectedIndex() == 0)
				{
					periodLabel.setVisible(false);
					periodField.setVisible(false);
				}
				else
				{
					periodLabel.setVisible(true);
					periodField.setVisible(true);
				}
				fireSizeChanged();
			}
		});
		
		StandardFormats.addGridBagElement(periodTypeLabel, elementsLayout, newLineConstr, this);
		StandardFormats.addGridBagElement(periodTypeField, elementsLayout, newLineConstr, this);
		StandardFormats.addGridBagElement(periodLabel, elementsLayout, newLineConstr, this);
		StandardFormats.addGridBagElement(periodField, elementsLayout, newLineConstr, this);
		
		String[] cameraDevices;
		try
		{
			Device[] cameras = server.getMicroscope().getDevices(DeviceType.CameraDevice);
			cameraDevices = new String[cameras.length];
			for(int i=0; i<cameras.length; i++)
			{
				cameraDevices[i] = cameras[i].getDeviceID();
			}
		}
		catch (Exception e2)
		{
			client.sendError("Could not detect installed cameras.", e2);
			cameraDevices = new String[0];
		}
		for (String camera : cameraDevices)
		{
			CameraPanel cameraPanel = new CameraPanel(camera);
			cameraPanels.addElement(cameraPanel);
			
			StandardFormats.addGridBagElement(cameraPanel, elementsLayout, newLineConstr, this);
		}
		
		StandardFormats.addGridBagElement(saveImagesField, elementsLayout, newLineConstr, this);
		StandardFormats.addGridBagElement(imageNameLabel, elementsLayout, newLineConstr,
				this);
		StandardFormats.addGridBagElement(imageNameField, elementsLayout, newLineConstr,
				this);
		
		StandardFormats.addGridBagElement(new JPanel(), elementsLayout, StandardFormats.getBottomContstraint(), this);

		setBorder(new TitledBorder("Imaging Properties"));
		
		loadConfigGroupNames();
		loadChannels();
		
		saveImagesField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				boolean selected = saveImagesField.isSelected();
				imageNameLabel.setVisible(selected);
				imageNameField.setVisible(selected);

				fireSizeChanged();
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
	}
}
