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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonUIAdapter;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.microscope.CameraDevice;
import org.youscope.common.microscope.PixelSize;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.JobsDefinitionPanel;
import org.youscope.uielements.StandardFormats;

/**
 * @author langmo
 */
class StaggeringJobConfigurationAddon  extends ComponentAddonUIAdapter<StaggeringJobConfiguration>
{

	private JFormattedTextField						nxField						= new JFormattedTextField(StandardFormats.getIntegerFormat());

	private JFormattedTextField						nyField						= new JFormattedTextField(StandardFormats.getIntegerFormat());
	
	private JFormattedTextField						nTilesPerIterField						= new JFormattedTextField(StandardFormats.getIntegerFormat());
	private JFormattedTextField						nIterBreakField						= new JFormattedTextField(StandardFormats.getIntegerFormat());
	
	private JFormattedTextField						deltaXField						= new JFormattedTextField(StandardFormats.getDoubleFormat());

	private JFormattedTextField						deltaYField						= new JFormattedTextField(StandardFormats.getDoubleFormat());

	private JobsDefinitionPanel jobPanel;
	
	private final GridBagConstraints newLineConstr = StandardFormats.getNewLineConstraint();
	
	StaggeringJobConfigurationAddon(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(),  client, server);
	}
    
    static ComponentMetadataAdapter<StaggeringJobConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<StaggeringJobConfiguration>(StaggeringJobConfiguration.TYPE_IDENTIFIER, 
				StaggeringJobConfiguration.class, 
				StaggeringJob.class, 
				"Staggering", 
				new String[]{"Containers"},
				"Performs one or more jobs at a rectangular x/y-grid. Thereby, the jobs of only every second/third/... grid element can be configured to be executed each evaluation, and also a certain number of evaluations can be skipped between the execution of the jobs of any grid element.",
				"icons/layers-group.png");
	}

    @Override
	protected Component createUI(StaggeringJobConfiguration configuration) throws AddonException
	{
		setTitle("Staggering Job");
		setResizable(true);
		setMaximizable(false);

		// Area panel
		GridBagLayout partLayout = new GridBagLayout();
		JPanel partPanel = new JPanel(partLayout);
		partPanel.setBorder(new TitledBorder("Stage Movement"));

		// Number of images
		StandardFormats.addGridBagElement(new JLabel("Number of tiles (x- / y-direction):"), partLayout, newLineConstr, partPanel);
		JPanel numImagesPanel = new JPanel(new GridLayout(1, 2));
		numImagesPanel.add(nxField);
		numImagesPanel.add(nyField);
		StandardFormats.addGridBagElement(numImagesPanel, partLayout, newLineConstr, partPanel);
		
		StandardFormats.addGridBagElement(new JLabel("Distance tiles (x- / y-direction):"), partLayout, newLineConstr, partPanel);
		JPanel distancePanel = new JPanel(new GridLayout(1, 2));
		distancePanel.add(deltaXField);
		distancePanel.add(deltaYField);
		StandardFormats.addGridBagElement(distancePanel, partLayout, newLineConstr, partPanel);
		
		JButton fromCameraButton = new JButton("Derive Distance from Pixel Size");
		fromCameraButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				YouScopeFrame childFrame = getContainingFrame().createModalChildFrame();
				@SuppressWarnings("unused")
				FromImageSize fromImageSize = new FromImageSize(childFrame);
				childFrame.setVisible(true);
			}
		});
		StandardFormats.addGridBagElement(fromCameraButton, partLayout, newLineConstr, partPanel);
		
		StandardFormats.addGridBagElement(new JLabel("Number of tiles per iteration:"), partLayout, newLineConstr, partPanel);
		StandardFormats.addGridBagElement(nTilesPerIterField, partLayout, newLineConstr, partPanel);
		
		StandardFormats.addGridBagElement(new JLabel("Number of iterations without actions:"), partLayout, newLineConstr, partPanel);
		StandardFormats.addGridBagElement(nIterBreakField, partLayout, newLineConstr, partPanel);


		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.add(new JLabel("Jobs executed at every tile:"), BorderLayout.NORTH);
        jobPanel = new JobsDefinitionPanel(getClient(), getServer(), getContainingFrame());
        centerPanel.add(jobPanel, BorderLayout.CENTER);
        centerPanel.setBorder(new TitledBorder("Imaging Protocol"));

		// Load state
		nxField.setValue(configuration.getNumTilesX());
		nyField.setValue(configuration.getNumTilesY());
		
		deltaXField.setValue(configuration.getDeltaX());
		deltaYField.setValue(configuration.getDeltaY());
		
		nIterBreakField.setValue(configuration.getNumIterationsBreak());
		nTilesPerIterField.setValue(configuration.getNumTilesPerIteration());
		
		jobPanel.setJobs(configuration.getJobs());
		
		// initialize layout
		JPanel contentPane = new JPanel(new BorderLayout());
		
		contentPane.add(partPanel, BorderLayout.NORTH);
		contentPane.add(centerPanel, BorderLayout.CENTER);
		return contentPane;
	}
	
	private class FromImageSize extends JPanel
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -975640805603628594L;

		private final YouScopeFrame frame;
		
		private final JFormattedTextField						overlapField				= new JFormattedTextField(StandardFormats.getDoubleFormat());
		private final JFormattedTextField						pixelSizeField				= new JFormattedTextField(StandardFormats.getDoubleFormat());

		private final JFormattedTextField						numPixelXField				= new JFormattedTextField(StandardFormats.getIntegerFormat());

		private final JFormattedTextField						numPixelYField				= new JFormattedTextField(StandardFormats.getIntegerFormat());

		private JRadioButton							pixelsFromCamera			= new JRadioButton("From camera configuration.", false);

		private JRadioButton							pixelsFromUser				= new JRadioButton("Enter manually.", false);

		private JComboBox<String>								cameraNamesField;

		private JComboBox<String>								pixelSizeIDsField;

		private JPanel									pixelsFromCameraPanel		= null;
		private JPanel									pixelsFromUserPanel			= null;

		private JRadioButton							pixelSizeFromConfig			= new JRadioButton("From pixel-size configuration.", false);

		private JRadioButton							pixelSizeFromUser			= new JRadioButton("Enter manually.", false);

		private JPanel									pixelSizeFromConfigPanel	= null;
		private JPanel									pixelSizeFromUserPanel		= null;

		FromImageSize(YouScopeFrame frame)
		{
			super(new BorderLayout());
			this.frame = frame;
			
			frame.setTitle("Stage Move Distance");
			frame.setResizable(false);
			frame.setClosable(true);
			frame.setMaximizable(false);
			
			GridBagLayout partLayout = new GridBagLayout();
			JPanel partPanel = new JPanel(partLayout);
			
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
						FromImageSize.this.frame.pack();
					}
					else if(pixelsFromUser.isSelected())
					{
						pixelsFromCameraPanel.setVisible(false);
						pixelsFromUserPanel.setVisible(true);
						FromImageSize.this.frame.pack();
					}
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
				}
			});
			StandardFormats.addGridBagElement(cameraNamesField, pixelsFromCameraLayout, newLineConstr, pixelsFromCameraPanel);
			StandardFormats.addGridBagElement(pixelsFromCameraPanel, partLayout, newLineConstr, partPanel);

			// Pixels from user
			GridBagLayout pixelsFromUserLayout = new GridBagLayout();
			pixelsFromUserPanel = new JPanel(pixelsFromUserLayout);
			StandardFormats.addGridBagElement(new JLabel("Number of pixels in an image (width / height):"), pixelsFromUserLayout, newLineConstr, pixelsFromUserPanel);
			JPanel numPixelPanel = new JPanel(new GridLayout(1, 2));
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
						FromImageSize.this.frame.pack();
					}
					else if(pixelSizeFromUser.isSelected())
					{
						pixelSizeFromConfigPanel.setVisible(false);
						pixelSizeFromUserPanel.setVisible(true);
						FromImageSize.this.frame.pack();
					}
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
				}
			});
			StandardFormats.addGridBagElement(pixelSizeIDsField, pixelSizeFromConfigLayout, newLineConstr, pixelSizeFromConfigPanel);
			StandardFormats.addGridBagElement(pixelSizeFromConfigPanel, partLayout, newLineConstr, partPanel);

			// Pixels from user
			GridBagLayout pixelSizeFromUserLayout = new GridBagLayout();
			pixelSizeFromUserPanel = new JPanel(pixelSizeFromUserLayout);
			StandardFormats.addGridBagElement(new JLabel("Pixel size in um:"), pixelSizeFromUserLayout, newLineConstr, pixelSizeFromUserPanel);
			StandardFormats.addGridBagElement(pixelSizeField, pixelSizeFromUserLayout, newLineConstr, pixelSizeFromUserPanel);
			StandardFormats.addGridBagElement(pixelSizeFromUserPanel, partLayout, newLineConstr, partPanel);

			// Overlap
			StandardFormats.addGridBagElement(new JLabel("Percentage of overlap between the pictures (0.0 - 1.0):"), partLayout, newLineConstr, partPanel);
			StandardFormats.addGridBagElement(overlapField, partLayout, newLineConstr, partPanel);
			
			
			// Add job button
			JButton addJobButton = new JButton("Accept");
			addJobButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					double overlap = ((Number)overlapField.getValue()).doubleValue();
					double pixelSize = ((Number)pixelSizeField.getValue()).doubleValue();
					int numPixelsX = ((Number)numPixelXField.getValue()).intValue();
					int numPixelsY = ((Number)numPixelYField.getValue()).intValue();
					
					deltaXField.setValue(pixelSize * numPixelsX * (1-overlap));
					deltaYField.setValue(pixelSize * numPixelsY * (1-overlap));
					
					try
					{
						FromImageSize.this.frame.setVisible(false);
					}
					catch(Exception e1)
					{
						sendErrorMessage("Could not close window.", e1);
					}
				}
			});
			
			
			
			// Initialize state
			overlapField.setValue(0.1);
			pixelsFromCamera.doClick();
			pixelSizeFromConfig.doClick();
			
			add(partPanel, BorderLayout.CENTER);
			add(addJobButton, BorderLayout.SOUTH);
			frame.setContentPane(this);
			frame.pack();

		}
		
		private void setPixelSizeConfigID(String pixelSizeID)
		{
			try
			{
				double pixelSize = getServer().getMicroscope().getPixelSizeManager().getPixelSize(pixelSizeID).getPixelSize();
				pixelSizeField.setValue(pixelSize);
			}
			catch(Exception e1)
			{
				sendErrorMessage("Could not get pixel size.", e1);
				return;
			}
		}
		
		private void setCameraPixels(String cameraName)
		{
			Dimension imageSize;
			boolean isSwitched;
			
			try
			{
				CameraDevice cameraDevice = getServer().getMicroscope().getCameraDevice(cameraName);
				imageSize = cameraDevice.getImageSize();
				isSwitched = cameraDevice.isSwitchXY();
			}
			catch(Exception e1)
			{
				sendErrorMessage("Could not get image size of camera.", e1);
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
		private String[] getPixelSizeNames()
		{
			try
			{
				PixelSize[] pixelSizes = getServer().getMicroscope().getPixelSizeManager().getPixelSizes();
				String[] names = new String[pixelSizes.length];
				for(int i=0; i< pixelSizes.length; i++)
				{
					names[i] = pixelSizes[i].getPixelSizeID();
				}
				
				return names;
			}
			catch(Exception e)
			{
				sendErrorMessage("Could not get IDs of pixel size settings.", e);
				return new String[0];
			}
		}
		
		private String[] getCameraNames()
		{
			String[] cameraNames;
			try
			{
				CameraDevice[] cameraDevices = getServer().getMicroscope().getCameraDevices();
				cameraNames = new String[cameraDevices.length];
				for(int i=0; i<cameraDevices.length; i++)
				{
					cameraNames[i] = cameraDevices[i].getDeviceID();
				}
			}
			catch(Exception e)
			{
				sendErrorMessage("Could not get names of cameras.", e);
				cameraNames = new String[0];
			}
			
			return cameraNames;
		}
	}

	@Override
	protected void commitChanges(StaggeringJobConfiguration configuration) {
		int nx = ((Number)nxField.getValue()).intValue();
		int ny = ((Number)nyField.getValue()).intValue();
		
		int nBreak = ((Number)nIterBreakField.getValue()).intValue();
		int nTilesIter = ((Number)nTilesPerIterField.getValue()).intValue();
		
		double deltaX = ((Number)deltaXField.getValue()).doubleValue();
		double deltaY = ((Number)deltaYField.getValue()).doubleValue();

		configuration.setNumTilesX(nx);
		configuration.setNumTilesY(ny);
		configuration.setDeltaX(deltaX);
		configuration.setDeltaY(deltaY);
		configuration.setNumIterationsBreak(nBreak);
		configuration.setNumTilesPerIteration(nTilesIter);
		configuration.setJobs(jobPanel.getJobs());		
	}

	@Override
	protected void initializeDefaultConfiguration(StaggeringJobConfiguration configuration) throws AddonException {
		// do nothing.
	}
}
