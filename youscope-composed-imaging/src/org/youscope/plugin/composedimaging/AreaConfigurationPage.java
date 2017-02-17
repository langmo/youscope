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
package org.youscope.plugin.composedimaging;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import org.youscope.addon.measurement.MeasurementAddonUIPage;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.microscope.CameraDevice;
import org.youscope.common.microscope.PixelSize;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.StandardFormats;

/**
 * @author langmo
 * 
 */
class AreaConfigurationPage extends MeasurementAddonUIPage<ComposedImagingMeasurementConfiguration>
{
	/**
	 * Serial Version UID.
	 */
	private static final long				serialVersionUID	= -1833466993051293407L;

	private JFormattedTextField				overlapField		= new JFormattedTextField(StandardFormats.getDoubleFormat());

	private JFormattedTextField				nxField				= new JFormattedTextField(StandardFormats.getIntegerFormat());

	private JFormattedTextField				nyField				= new JFormattedTextField(StandardFormats.getIntegerFormat());

	private JFormattedTextField				pixelSizeField		= new JFormattedTextField(StandardFormats.getDoubleFormat());

	private JFormattedTextField				numPixelXField		= new JFormattedTextField(StandardFormats.getIntegerFormat());

	private JFormattedTextField				numPixelYField		= new JFormattedTextField(StandardFormats.getIntegerFormat());

	private JFormattedTextField				areaWidthField		= new JFormattedTextField(StandardFormats.getDoubleFormat());

	private JFormattedTextField				areaHeightField		= new JFormattedTextField(StandardFormats.getDoubleFormat());

	private JFormattedTextField				pictureWidthField	= new JFormattedTextField(StandardFormats.getDoubleFormat());

	private JFormattedTextField				pictureHeightField	= new JFormattedTextField(StandardFormats.getDoubleFormat());

	private GridBagConstraints				newLineConstr		= StandardFormats.getNewLineConstraint();
	
	private JRadioButton					pixelsFromCamera	= new JRadioButton("From camera configuration.", false);

	private JRadioButton					pixelsFromUser		= new JRadioButton("Enter manually.", false);
	
	private JComboBox<String>						cameraNamesField;
	
	private JComboBox<String>						pixelSizeIDsField;
	
	private JPanel pixelsFromCameraPanel = null;
	private JPanel pixelsFromUserPanel = null;
	
	private JRadioButton					pixelSizeFromConfig	= new JRadioButton("From pixel-size configuration.", false);

	private JRadioButton					pixelSizeFromUser		= new JRadioButton("Enter manually.", false);
	
	private JPanel pixelSizeFromConfigPanel = null;
	private JPanel pixelSizeFromUserPanel = null;

	private final YouScopeClient	client;
	private final YouScopeServer			server;

	//Names for properties which are saved to the config file
	private static final String PROPERTY_OVERLAP = "YouScope.ComposedImaging.overlap";
	private static final String PROPERTY_NX = "YouScope.ComposedImaging.nx";
	private static final String PROPERTY_NY = "YouScope.ComposedImaging.ny";
	
	AreaConfigurationPage(YouScopeClient client, YouScopeServer server)
	{
		this.server = server;
		this.client = client;
	}
	@Override
	public void createUI(YouScopeFrame frame)
	{
		// Image area panel
		GridBagLayout partLayout = new GridBagLayout();
		JPanel partPanel = new JPanel(partLayout);
		partPanel.setBorder(new TitledBorder("Spatial Properties"));

		// Number of pixels
		StandardFormats.addGridBagElement(new JLabel("Get number of pixels:"), partLayout, newLineConstr, partPanel);
		ButtonGroup pixelsFromGroup = new ButtonGroup();
		pixelsFromGroup.add(pixelsFromCamera);
		pixelsFromGroup.add(pixelsFromUser);
		class PixelsFromListener implements ActionListener
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(pixelsFromCamera.isSelected())
				{
					pixelsFromCameraPanel.setVisible(true);
					pixelsFromUserPanel.setVisible(false);
					if(cameraNamesField.getSelectedItem() != null)
						setCameraPixels(cameraNamesField.getSelectedItem().toString());
					fireSizeChanged();
				}
				else if(pixelsFromUser.isSelected())
				{
					pixelsFromCameraPanel.setVisible(false);
					pixelsFromUserPanel.setVisible(true);
					fireSizeChanged();
				}
				updateAreaConfig();
			}
		}
		pixelsFromCamera.addActionListener(new PixelsFromListener());
		pixelsFromUser.addActionListener(new PixelsFromListener());
		StandardFormats.addGridBagElement(pixelsFromCamera, partLayout, newLineConstr, partPanel);
		StandardFormats.addGridBagElement(pixelsFromUser, partLayout, newLineConstr, partPanel);
		
		// Pixels from camera.
		GridBagLayout pixelsFromCameraLayout = new GridBagLayout();
		pixelsFromCameraPanel = new JPanel(pixelsFromCameraLayout);
		StandardFormats.addGridBagElement(new JLabel("Select Camera:"), pixelsFromCameraLayout, newLineConstr, pixelsFromCameraPanel);
		cameraNamesField = new JComboBox<String>(getCameraNames());
		cameraNamesField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(cameraNamesField.getSelectedItem() == null)
					return;
				String cameraName = cameraNamesField.getSelectedItem().toString();
				setCameraPixels(cameraName);
				updateAreaConfig();
			}
		});
		StandardFormats.addGridBagElement(cameraNamesField, pixelsFromCameraLayout, newLineConstr, pixelsFromCameraPanel);
		StandardFormats.addGridBagElement(pixelsFromCameraPanel, partLayout, newLineConstr, partPanel);
		
		// Pixels from user
		GridBagLayout pixelsFromUserLayout  = new GridBagLayout();
		pixelsFromUserPanel = new JPanel(pixelsFromUserLayout);
		StandardFormats.addGridBagElement(new JLabel("Number of pixels in an image (width / height):"), pixelsFromUserLayout, newLineConstr, pixelsFromUserPanel);
		JPanel numPixelPanel = new JPanel(new GridLayout(1, 2));
		numPixelXField.addActionListener(new AreaConfigChangedListener());
		numPixelYField.addActionListener(new AreaConfigChangedListener());
		numPixelPanel.add(numPixelXField);
		numPixelPanel.add(numPixelYField);
		StandardFormats.addGridBagElement(numPixelPanel, pixelsFromUserLayout, newLineConstr, pixelsFromUserPanel);
		StandardFormats.addGridBagElement(pixelsFromUserPanel, partLayout, newLineConstr, partPanel);
		
		// Pixel Size
		StandardFormats.addGridBagElement(new JLabel("Get pixel-size:"), partLayout, newLineConstr, partPanel);
		ButtonGroup pixelSizeFromGroup = new ButtonGroup();
		pixelSizeFromGroup.add(pixelSizeFromConfig);
		pixelSizeFromGroup.add(pixelSizeFromUser);
		class PixelSizeFromListener implements ActionListener
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(pixelSizeFromConfig.isSelected())
				{
					pixelSizeFromConfigPanel.setVisible(true);
					pixelSizeFromUserPanel.setVisible(false);
					if(pixelSizeIDsField.getSelectedItem() != null)
						setPixelSizeConfigID(pixelSizeIDsField.getSelectedItem().toString());
					fireSizeChanged();
				}
				else if(pixelSizeFromUser.isSelected())
				{
					pixelSizeFromConfigPanel.setVisible(false);
					pixelSizeFromUserPanel.setVisible(true);
					fireSizeChanged();
				}
				updateAreaConfig();
			}
		}
		pixelSizeFromConfig.addActionListener(new PixelSizeFromListener());
		pixelSizeFromUser.addActionListener(new PixelSizeFromListener());
		StandardFormats.addGridBagElement(pixelSizeFromConfig, partLayout, newLineConstr, partPanel);
		StandardFormats.addGridBagElement(pixelSizeFromUser, partLayout, newLineConstr, partPanel);
		
		// Pixel Size from config.
		GridBagLayout pixelSizeFromConfigLayout = new GridBagLayout();
		pixelSizeFromConfigPanel = new JPanel(pixelSizeFromConfigLayout);
		StandardFormats.addGridBagElement(new JLabel("Select pixel-size setting:"), pixelSizeFromConfigLayout, newLineConstr, pixelSizeFromConfigPanel);
		pixelSizeIDsField = new JComboBox<String>(getPixelSizeNames());
		pixelSizeIDsField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(pixelSizeIDsField.getSelectedItem() == null)
					return;
				String pixelSizeID = pixelSizeIDsField.getSelectedItem().toString();
				setPixelSizeConfigID(pixelSizeID);
				updateAreaConfig();
			}
		});
		StandardFormats.addGridBagElement(pixelSizeIDsField, pixelSizeFromConfigLayout, newLineConstr, pixelSizeFromConfigPanel);
		StandardFormats.addGridBagElement(pixelSizeFromConfigPanel, partLayout, newLineConstr, partPanel);
		
		// Pixels from user
		GridBagLayout pixelSizeFromUserLayout  = new GridBagLayout();
		pixelSizeFromUserPanel = new JPanel(pixelSizeFromUserLayout);
		StandardFormats.addGridBagElement(new JLabel("Pixel size in um:"), pixelSizeFromUserLayout, newLineConstr, pixelSizeFromUserPanel);
		pixelSizeField.addActionListener(new AreaConfigChangedListener());
		StandardFormats.addGridBagElement(pixelSizeField, pixelSizeFromUserLayout, newLineConstr, pixelSizeFromUserPanel);
		StandardFormats.addGridBagElement(pixelSizeFromUserPanel, partLayout, newLineConstr, partPanel);	

		// Overlap
		StandardFormats.addGridBagElement(new JLabel("Percentage of overlap between the pictures (0.0 - 1.0):"), partLayout, newLineConstr, partPanel);
		overlapField.addActionListener(new AreaConfigChangedListener());
		StandardFormats.addGridBagElement(overlapField, partLayout, newLineConstr, partPanel);

		// Number of images
		StandardFormats.addGridBagElement(new JLabel("Number of images (x- / y-direction):"), partLayout, newLineConstr, partPanel);
		JPanel numImagesPanel = new JPanel(new GridLayout(1, 2));
		nxField.addActionListener(new AreaConfigChangedListener());
		nyField.addActionListener(new AreaConfigChangedListener());
		numImagesPanel.add(nxField);
		numImagesPanel.add(nyField);
		StandardFormats.addGridBagElement(numImagesPanel, partLayout, newLineConstr, partPanel);
		StandardFormats.addGridBagElement(new JPanel(), partLayout, StandardFormats.getBottomContstraint(), partPanel);

		// Image Area information panel
		GridBagLayout informationLayout = new GridBagLayout();
		JPanel informationPanel = new JPanel(informationLayout);
		informationPanel.setBorder(new TitledBorder("Information on Area:"));

		StandardFormats.addGridBagElement(new JLabel("Size of one image in um (width / height):"), informationLayout, newLineConstr, informationPanel);
		JPanel imageWidthPanel = new JPanel(new GridLayout(1, 2));
		pictureHeightField.setEditable(false);
		pictureWidthField.setEditable(false);
		imageWidthPanel.add(pictureWidthField);
		imageWidthPanel.add(pictureHeightField);
		StandardFormats.addGridBagElement(imageWidthPanel, informationLayout, newLineConstr, informationPanel);

		StandardFormats.addGridBagElement(new JLabel("Size of totally imaged area in um (width / height):"), informationLayout, newLineConstr, informationPanel);
		JPanel areaWidthPanel = new JPanel(new GridLayout(1, 2));
		areaWidthField.setEditable(false);
		areaHeightField.setEditable(false);
		areaWidthPanel.add(areaWidthField);
		areaWidthPanel.add(areaHeightField);
		StandardFormats.addGridBagElement(areaWidthPanel, informationLayout, newLineConstr, informationPanel);
		StandardFormats.addGridBagElement(new JPanel(), informationLayout, StandardFormats.getBottomContstraint(), informationPanel);

		setLayout(new GridLayout(1, 2, 3, 3));
		add(partPanel);
		add(informationPanel);
	}

	private void setPixelSizeConfigID(String pixelSizeID)
	{
		try
		{
			double pixelSize = server.getMicroscope().getPixelSizeManager().getPixelSize(pixelSizeID).getPixelSize();
			pixelSizeField.setValue(pixelSize);
		}
		catch(Exception e1)
		{
			client.sendError("Could not get pixel size.", e1);
			return;
		}
	}
	
	private void setCameraPixels(String cameraName)
	{
		Dimension imageSize;
		boolean isSwitched;
		
		try
		{
			CameraDevice cameraDevice = server.getMicroscope().getCameraDevice(cameraName);
			imageSize = cameraDevice.getImageSize();
			isSwitched = cameraDevice.isSwitchXY();
		}
		catch(Exception e1)
		{
			client.sendError("Could not get image size of camera.", e1);
			return;
		}
		if(isSwitched)
		{
			numPixelXField.setValue(imageSize.height);
			numPixelYField.setValue(imageSize.width);
		}
		else
		{
			numPixelXField.setValue(imageSize.width);
			numPixelYField.setValue(imageSize.height);
		}
	}
	private class AreaConfigChangedListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			updateAreaConfig();
		}
	}
	
	private String[] getPixelSizeNames()
	{
		try
		{
			PixelSize[] pixelSizes = server.getMicroscope().getPixelSizeManager().getPixelSizes();
			String[] names = new String[pixelSizes.length];
			for(int i=0; i< pixelSizes.length; i++)
			{
				names[i] = pixelSizes[i].getPixelSizeID();
			}
			
			return names;
		}
		catch(Exception e)
		{
			client.sendError("Could not get IDs of pixel size settings.", e);
			return new String[0];
		}
	}
	
	private String[] getCameraNames()
	{
		String[] cameraNames;
		try
		{
			CameraDevice[] cameraDevices = server.getMicroscope().getCameraDevices();
			cameraNames = new String[cameraDevices.length];
			for(int i=0; i<cameraDevices.length; i++)
			{
				cameraNames[i] = cameraDevices[i].getDeviceID();
			}
		}
		catch(Exception e)
		{
			client.sendError("Could not get names of cameras.", e);
			cameraNames = new String[0];
		}
		
		return cameraNames;
	}

	private void updateAreaConfig()
	{
		double pixelSize = ((Number)pixelSizeField.getValue()).doubleValue();
		int numPixelX = ((Number)numPixelXField.getValue()).intValue();
		int numPixelY = ((Number)numPixelYField.getValue()).intValue();
		int nx = ((Number)nxField.getValue()).intValue();
		int ny = ((Number)nyField.getValue()).intValue();
		double overlap = ((Number)overlapField.getValue()).doubleValue();

		double pictureWidth = pixelSize * numPixelX;
		double pictureHeight = pixelSize * numPixelY;
		double areaWidth = pictureWidth * ((1.0 - overlap) * (nx - 1) + 1);
		double areaHeight = pictureHeight * ((1.0 - overlap) * (ny - 1) + 1);

		pictureWidthField.setValue(pictureWidth);
		pictureHeightField.setValue(pictureHeight);
		areaWidthField.setValue(areaWidth);
		areaHeightField.setValue(areaHeight);
	}

	@Override
	public void loadData(ComposedImagingMeasurementConfiguration configuration)
	{
		overlapField.setValue(configuration.getOverlap());
		nxField.setValue(configuration.getNx());
		nyField.setValue(configuration.getNy());
		pixelSizeField.setValue(configuration.getPixelSize());
		numPixelXField.setValue(configuration.getNumPixels().width);
		numPixelYField.setValue(configuration.getNumPixels().height);
		
		if(configuration.getCameraDevice() != null)
		{
			pixelsFromCamera.doClick();
			cameraNamesField.setSelectedItem(configuration.getCameraDevice());
		}
		else
		{
			pixelsFromUser.doClick();
		}
		
		if(configuration.getPixelSizeID() != null)
		{
			pixelSizeFromConfig.doClick();
			pixelSizeIDsField.setSelectedItem(configuration.getPixelSizeID());
		}
		else
		{
			pixelSizeFromUser.doClick();
		}

		updateAreaConfig();
	}

	@Override
	public boolean saveData(ComposedImagingMeasurementConfiguration configuration)
	{
		// Area settings
		double overlap = ((Number)overlapField.getValue()).doubleValue();
		double pixelSize = ((Number)pixelSizeField.getValue()).doubleValue();
		int numPixelsX = ((Number)numPixelXField.getValue()).intValue();
		int numPixelsY = ((Number)numPixelYField.getValue()).intValue();
		int nx = ((Number)nxField.getValue()).intValue();
		int ny = ((Number)nyField.getValue()).intValue();

		configuration.setOverlap(overlap);
		configuration.setPixelSize(pixelSize);
		configuration.setNumPixels(new Dimension(numPixelsX, numPixelsY));
		configuration.setNx(nx);
		configuration.setNy(ny);
		
		if(pixelSizeFromConfig.isSelected() && pixelSizeIDsField.getSelectedItem() != null)
			configuration.setPixelSizeID(pixelSizeIDsField.getSelectedItem().toString());
		else
			configuration.setPixelSizeID(null);
		
		if(pixelsFromCamera.isSelected() && cameraNamesField.getSelectedItem() != null)
			configuration.setCameraDevice(cameraNamesField.getSelectedItem().toString());
		else
			configuration.setCameraDevice(null);
		
		client.getPropertyProvider().setProperty(PROPERTY_OVERLAP, overlap);
		client.getPropertyProvider().setProperty(PROPERTY_NX, nx);
		client.getPropertyProvider().setProperty(PROPERTY_NY, ny);
		return true;
	}

	@Override
	public void setToDefault(ComposedImagingMeasurementConfiguration configuration)
	{
		// Get last values for parameters where the user expects that they are the same as last time.
		double overlap = client.getPropertyProvider().getProperty(PROPERTY_OVERLAP, configuration.getOverlap());
		int nx = client.getPropertyProvider().getProperty(PROPERTY_NX, configuration.getNx());
		int ny = client.getPropertyProvider().getProperty(PROPERTY_NY, configuration.getNy());
		
		// Get current values for parameters where the user expects that they correspond to the current microscope settings.
		String cameraName = null;
		Dimension numPixels = null;
		double pixelSize = -1;
		String pixelSizeID = null;
		try
		{
			CameraDevice cameraDevice = server.getMicroscope().getCameraDevice();
			if(cameraDevice == null)
			{
				CameraDevice[] cameraDevices = server.getMicroscope().getCameraDevices();
				if(cameraDevices.length > 0)
					cameraDevice = cameraDevices[0];
			}
			if(cameraDevice != null)
			{
				cameraName = cameraDevice.getDeviceID();
				numPixels = cameraDevice.getImageSize();
			}
			else
			{
				numPixels = new Dimension(1344, 1024);
			}
			
			PixelSize[] pixelSizes = server.getMicroscope().getPixelSizeManager().getPixelSizes();
			if(pixelSizes.length > 0)
			{
				pixelSizeID = pixelSizes[0].getPixelSizeID();
				pixelSize = pixelSizes[0].getPixelSize();
			}
			else
			{
				pixelSize = 6.45;
			}
		}
		catch(Exception e)
		{
			client.sendError("Could not get settings of current camera or pixel size.", e);
		}
		
		configuration.setOverlap(overlap);
		configuration.setPixelSizeID(pixelSizeID);
		configuration.setPixelSize(pixelSize);
		configuration.setCameraDevice(cameraName);
		configuration.setNumPixels(numPixels);
		configuration.setNx(nx);
		configuration.setNy(ny);
	}

	@Override
	public String getPageName()
	{
		return "Area Definition";
	}
}
