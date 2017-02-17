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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.youscope.common.microscope.Device;
import org.youscope.common.microscope.Microscope;
import org.youscope.uielements.DescriptionPanel;
import org.youscope.uielements.DynamicPanel;

/**
 * @author Moritz Lang
 *
 */
class ManageTabStandardRoles extends ManageTabElement
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -5157626502708886101L;
	
	private final JComboBox<String> cameraField = new JComboBox<String>();
	private final JComboBox<String> focusField = new JComboBox<String>();
	private final JComboBox<String> autoFocusField = new JComboBox<String>();
	private final JComboBox<String> shutterField = new JComboBox<String>();
	private final JComboBox<String> xyStageField = new JComboBox<String>();
	
	private boolean initializing = false;
	private boolean somethingChanged = false;
	
	ManageTabStandardRoles()
	{
		setOpaque(false);
		cameraField.setOpaque(false);
		focusField.setOpaque(false);
		shutterField.setOpaque(false);
		xyStageField.setOpaque(false);
		autoFocusField.setOpaque(false);
		
		cameraField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if(initializing)
					return;
				try
				{
					Object camera = cameraField.getSelectedItem();
					if(camera == null)
						return;
					somethingChanged = true;
					YouScopeClientImpl.getMicroscope().setCameraDevice(camera.toString());
				}
				catch(Exception e)
				{
					ClientSystem.err.println("Could not set standard camera device.", e);
				}
			}
		});
		
		focusField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if(initializing)
					return;
				try
				{
					Object focus = focusField.getSelectedItem();
					if(focus == null)
						return;
					somethingChanged = true;
					YouScopeClientImpl.getMicroscope().setFocusDevice(focus.toString());
				}
				catch(Exception e)
				{
					ClientSystem.err.println("Could not set standard focus device.", e);
				}
			}
		});
		
		autoFocusField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if(initializing)
					return;
				try
				{
					Object autoFocus = autoFocusField.getSelectedItem();
					if(autoFocus == null)
						return;
					somethingChanged = true;
					YouScopeClientImpl.getMicroscope().setAutoFocusDevice(autoFocus.toString());
				}
				catch(Exception e)
				{
					ClientSystem.err.println("Could not set standard auto-focus device.", e);
				}
			}
		});
		
		shutterField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if(initializing)
					return;
				try
				{
					Object shutter = shutterField.getSelectedItem();
					if(shutter == null)
						return;
					somethingChanged = true;
					YouScopeClientImpl.getMicroscope().setShutterDevice(shutter.toString());
				}
				catch(Exception e)
				{
					ClientSystem.err.println("Could not set standard shutter device.", e);
				}
			}
		});
		
		xyStageField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if(initializing)
					return;
				try
				{
					Object xyStage = xyStageField.getSelectedItem();
					if(xyStage == null)
						xyStage = "";
					YouScopeClientImpl.getMicroscope().setStageDevice(xyStage.toString());
				}
				catch(Exception e)
				{
					ClientSystem.err.println("Could not set standard stage device.", e);
				}
			}
		});
					
		JPanel elementsPanel = new JPanel(new GridLayout(5, 2, 2, 2));
		elementsPanel.setOpaque(false);
		elementsPanel.add(new JLabel("Standard Camera:"));
		elementsPanel.add(cameraField);
		elementsPanel.add(new JLabel("Standard Focus:"));
		elementsPanel.add(focusField);
		elementsPanel.add(new JLabel("Standard Shutter:"));
		elementsPanel.add(shutterField);
		elementsPanel.add(new JLabel("Standard Stage:"));
		elementsPanel.add(xyStageField);
		elementsPanel.add(new JLabel("Standard Auto-Focus:"));
		elementsPanel.add(autoFocusField);
		
		DescriptionPanel descriptionPanel = new DescriptionPanel("Description", "YouScope supports having more than one device of a specific type, e.g. more than one camera or stage. As a result, one would have to specify each time when performing a standard task (like changing the stage position) which device should perform the task. However, since typically nearly always the same device is used for standard tasks, YouScope conveniently allows to assign a device to be the standard device for a certain task. This device is then automatically used for this task by YouScope, except when explicitly specified differently.");
		JScrollPane scrollPane = new JScrollPane(descriptionPanel);
		scrollPane.setPreferredSize(new Dimension(400, 150));
		
		DynamicPanel mainPanel = new DynamicPanel();
		mainPanel.add(scrollPane);
		mainPanel.add(elementsPanel);
		mainPanel.addFillEmpty();
		
		setOpaque(false);
		setLayout(new BorderLayout());
		add(mainPanel, BorderLayout.CENTER);
	}
	
	@Override
	public boolean storeContent()
	{
		return somethingChanged;
	}
	
	@Override
	public void initializeContent()
	{
		initializing = true;
		cameraField.removeAllItems();
		focusField.removeAllItems();
		shutterField.removeAllItems();
		xyStageField.removeAllItems();
		autoFocusField.removeAllItems();
		
		try
		{
			Microscope microscope = YouScopeClientImpl.getMicroscope();
			// Cameras
			Device[] cameraDevices = microscope.getCameraDevices();
			for(Device cameraDevice : cameraDevices)
			{
				cameraField.addItem(cameraDevice.getDeviceID());
			}
			try
			{
				cameraField.setSelectedItem(microscope.getCameraDevice().getDeviceID());
			}
			catch(@SuppressWarnings("unused") Exception e)
			{
				// No default value is yet set. Ignore.
			}
							
			// Focus
			Device[] focusDevices = microscope.getFocusDevices();
			for(Device focusDevice : focusDevices)
			{
				focusField.addItem(focusDevice.getDeviceID());
			}
			try
			{
				focusField.setSelectedItem(microscope.getFocusDevice().getDeviceID());
			}
			catch(@SuppressWarnings("unused") Exception e)
			{
				// No default value is yet set. Ignore.
			}	
			
			// Shutter
			Device[] shutterDevices = microscope.getShutterDevices();
			for(Device shutterDevice : shutterDevices)
			{
				shutterField.addItem(shutterDevice.getDeviceID());
			}
			try
			{
				shutterField.setSelectedItem(microscope.getShutterDevice().getDeviceID());
			}
			catch(@SuppressWarnings("unused") Exception e)
			{
				// No default value is yet set. Ignore.
			}
			
			// XY-Stage
			Device[] xyStageDevices = microscope.getStageDevices();
			for(Device xyStageDevice : xyStageDevices)
			{
				xyStageField.addItem(xyStageDevice.getDeviceID());
			}
			try
			{
				xyStageField.setSelectedItem(microscope.getStageDevice().getDeviceID());
			}
			catch(@SuppressWarnings("unused") Exception e)
			{
				// No default value is yet set. Ignore.
			}
			
			// Auto-Focus
			Device[] autoFocusDevices = microscope.getAutoFocusDevices();
			for(Device autoFocusDevice : autoFocusDevices)
			{
				autoFocusField.addItem(autoFocusDevice.getDeviceID());
			}
			try
			{
				autoFocusField.setSelectedItem(microscope.getAutoFocusDevice().getDeviceID());
			}
			catch(@SuppressWarnings("unused") Exception e)
			{
				// No default value is yet set. Ignore.
			}
			
		}
		catch(Exception e)
		{
			ClientSystem.err.println("Could not load standard devices for given roles.", e);
		}
		initializing = false;
	}
}
