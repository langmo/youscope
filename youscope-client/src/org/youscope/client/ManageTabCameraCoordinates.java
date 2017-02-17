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
package org.youscope.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import org.youscope.common.microscope.CameraDevice;
import org.youscope.uielements.DescriptionPanel;
import org.youscope.uielements.StandardFormats;

/**
 * @author Moritz Lang
 *
 */
class ManageTabCameraCoordinates extends ManageTabElement
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -2558152206635225814L;

	private final GridBagConstraints	newLineConstr = StandardFormats.getNewLineConstraint();
	private final GridBagConstraints	bottomConstr = StandardFormats.getBottomContstraint();
	
	private final JLabel cameraChooserLabel = new JLabel("Select camera:");
	private final JComboBox<String> cameraChooser = new JComboBox<String>();
	private final JComboBox<String> xIncreasesField = new JComboBox<String>(new String[]{"Not transpose x-coordinate.", "Transpose x-coordinate."});
	private final JComboBox<String> yIncreasesField = new JComboBox<String>(new String[]{"Not transpose y-coordinate.", "Transpose y-coordinate."});
	private final JComboBox<String> xySwitchField = new JComboBox<String>(new String[]{"Not switch x- and y-coordinates.", "Switch x- and y-coordinates."});
	private String lastCamera = null;
	private boolean loadingData = false;
	private boolean somethingChanged = false;
	ManageTabCameraCoordinates()
	{
		setLayout(new BorderLayout(5, 5));
		setOpaque(false);
		
		// Configuration elements
		GridBagLayout elementsLayout = new GridBagLayout();
		JPanel elementsPanel = new JPanel(elementsLayout);
		elementsPanel.setOpaque(false);
		elementsPanel.setBorder(new TitledBorder("Setup of Camera Coordinate System"));
		xIncreasesField.setOpaque(false);
		yIncreasesField.setOpaque(false);
		cameraChooser.setOpaque(false);
		cameraChooser.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent arg0)
			{
				if(arg0.getStateChange() == ItemEvent.DESELECTED)
				{
					// Do nothing.
				}
				else
				{
					selectCamera(arg0.getItem().toString());
				}
			}
		});
		
		xySwitchField.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(!loadingData && lastCamera != null)
				{
					try
					{
						somethingChanged =true;
						CameraDevice cameraDevice = YouScopeClientImpl.getMicroscope().getCameraDevice(lastCamera);
						cameraDevice.setSwitchXY(xySwitchField.getSelectedIndex() == 1);
					}
					catch(Exception e1)
					{
						ClientSystem.err.println("Could not store coordinate system for camera " + lastCamera + ".", e1);
					}
					
				}
			}
		});
		xIncreasesField.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(!loadingData && lastCamera != null)
				{
					try
					{
						somethingChanged =true;
						CameraDevice cameraDevice = YouScopeClientImpl.getMicroscope().getCameraDevice(lastCamera);
						cameraDevice.setTransposeX(xIncreasesField.getSelectedIndex() == 1);
					}
					catch(Exception e1)
					{
						ClientSystem.err.println("Could not store coordinate system for camera " + lastCamera + ".", e1);
					}
					
				}
			}
		});
		yIncreasesField.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(!loadingData && lastCamera != null)
				{
					try
					{
						somethingChanged =true;
						CameraDevice cameraDevice = YouScopeClientImpl.getMicroscope().getCameraDevice(lastCamera);
						cameraDevice.setTransposeY(yIncreasesField.getSelectedIndex() == 1);
					}
					catch(Exception e1)
					{
						ClientSystem.err.println("Could not store coordinate system for camera " + lastCamera + ".", e1);
					}
					
				}
			}
		});

		StandardFormats.addGridBagElement(cameraChooserLabel, elementsLayout, newLineConstr, elementsPanel);
		StandardFormats.addGridBagElement(cameraChooser, elementsLayout, newLineConstr, elementsPanel);
		StandardFormats.addGridBagElement(new JLabel("Switching of coordinates:"), elementsLayout, newLineConstr, elementsPanel);
		StandardFormats.addGridBagElement(xySwitchField, elementsLayout, newLineConstr, elementsPanel);
		StandardFormats.addGridBagElement(new JLabel("Direction of the x-coordinate:"), elementsLayout, newLineConstr, elementsPanel);
		StandardFormats.addGridBagElement(xIncreasesField, elementsLayout, newLineConstr, elementsPanel);
		StandardFormats.addGridBagElement(new JLabel("Direction of the y-coordinate:"), elementsLayout, newLineConstr, elementsPanel);
		StandardFormats.addGridBagElement(yIncreasesField, elementsLayout, newLineConstr, elementsPanel);
		JPanel emptyPanel = new JPanel();
		emptyPanel.setOpaque(false);
		StandardFormats.addGridBagElement(emptyPanel, elementsLayout, bottomConstr, elementsPanel);
		
		DescriptionPanel descriptionPanel = new DescriptionPanel("Description", 
				"Depending on the specific camera and the optics, images taken by the camera might be transposed or rotated. "
				+ "For several taks, like image stitching, it is necessary that the camera coordinates are alligned to the stage.\n"
				+ "To get to know the coordinates used by your camera, start the YouScope LiveStream and manually move the stage. The image shown by the camera should move accordingly, e.g. when slowly moving the stage from well A1 to A2 of a microplate, the image should move to the left, and accordingly to the top when moving from A1 to B1.\n"
				+ "If the images move to the wrong direction, transpose the respective axis. If the image moves vertically when moving the stage horizontally and vice versa, switch the axes.\n"
				+ "Note that this configuration should be done AFTER the coordinate axis of the stage are configured.");
		JScrollPane scrollPane = new JScrollPane(descriptionPanel);
		scrollPane.setPreferredSize(new Dimension(400, 150));
		add(scrollPane, BorderLayout.NORTH);
		add(elementsPanel, BorderLayout.CENTER);
	}

	private void selectCamera(String camera)
	{
		loadingData = true;
		lastCamera = camera;
		if(camera != null)
		{
			xIncreasesField.setEnabled(true);
			yIncreasesField.setEnabled(true);
			xySwitchField.setEnabled(true);
			
			try
			{
				CameraDevice cameraDevice = YouScopeClientImpl.getMicroscope().getCameraDevice(camera);
				xIncreasesField.setSelectedIndex(cameraDevice.isTransposeX() ? 1 : 0);
				yIncreasesField.setSelectedIndex(cameraDevice.isTransposeY() ? 1 : 0);
				xySwitchField.setSelectedIndex(cameraDevice.isSwitchXY() ? 1 : 0);
			}
			catch(Exception e)
			{
				ClientSystem.err.println("Could not load coordinate system for camera " + lastCamera + ".", e);
			}
		}
		else
		{
			xIncreasesField.setEnabled(false);
			yIncreasesField.setEnabled(false);
			xySwitchField.setEnabled(false);
		}
		loadingData = false;
	}
	@Override
	public void initializeContent()
	{
		lastCamera = null;
		try
		{
			CameraDevice[] cameras = YouScopeClientImpl.getMicroscope().getCameraDevices();
			cameraChooser.removeAllItems();
			for(CameraDevice camera : cameras)
				cameraChooser.addItem(camera.getDeviceID());
		}
		catch (Exception e2)
		{
			ClientSystem.err.println("Could not get installed cameras.", e2);
			return;
		}
		
		if(cameraChooser.getItemCount() > 0)
			cameraChooser.setSelectedIndex(0);
	}

	@Override
	public boolean storeContent()
	{
		return somethingChanged;
	}
}
