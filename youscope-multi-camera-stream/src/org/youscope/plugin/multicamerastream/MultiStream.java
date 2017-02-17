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
package org.youscope.plugin.multicamerastream;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.youscope.addon.AddonException;
import org.youscope.addon.tool.ToolAddonUIAdapter;
import org.youscope.addon.tool.ToolMetadata;
import org.youscope.addon.tool.ToolMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.microscope.Device;
import org.youscope.common.microscope.DeviceType;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.StandardFormats;

/**
 * @author langmo
 *
 */
class MultiStream extends ToolAddonUIAdapter
{
	private String[] cameraDevices = new String[0];
	private JCheckBox[] cameraCheckBoxes;
	
	/**
	 * Constructor.
	 * @param client Interface to the YouScope client.
	 * @param server Interface to the YouScope server.
	 * @throws AddonException 
	 */
	public MultiStream(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(), client, server);
	}
	
	public final static String TYPE_IDENTIFIER = "YouScope.YouScopeMultiStream";
	
	static ToolMetadata getMetadata()
	{
		return new ToolMetadataAdapter(TYPE_IDENTIFIER, "Multi-Camera Stream", new String[]{"multi-cam"}, 
				"Displays the current images of multiple cameras next to one another.",
				"icons/films.png");
	}
	
	@Override
	public java.awt.Component createUI() throws AddonException
	{
		setMaximizable(false);
		setResizable(false);
		setTitle("Choose Cameras");
	
		// Get cameras
		try
		{
			Device[] devices = getMicroscope().getDevices(DeviceType.CameraDevice);
			cameraDevices = new String[devices.length];
			for(int i=0; i<cameraDevices.length; i++)
			{
				cameraDevices[i] = devices[i].getDeviceID();
			}
		}
		catch (Exception e2)
		{
			throw new AddonException("Could not detect installed cameras.", e2);
		}		
		
		GridBagLayout elementsLayout = new GridBagLayout();
		JPanel elementsPanel = new JPanel(elementsLayout);
        GridBagConstraints newLineConstr = StandardFormats.getNewLineConstraint();
        addConfElement(new JLabel("Select cameras you want to image with:"), elementsLayout, newLineConstr, elementsPanel);
        
        GridBagLayout camerasLayout = new GridBagLayout();
		JPanel camerasPanel = new JPanel(camerasLayout);
		cameraCheckBoxes = new JCheckBox[cameraDevices.length];
		for(int i = 0; i < cameraDevices.length; i++)
		{
			cameraCheckBoxes[i] = new JCheckBox(cameraDevices[i], true);
			addConfElement(cameraCheckBoxes[i], camerasLayout, newLineConstr, camerasPanel);
		}
		addConfElement(new JPanel(), camerasLayout, StandardFormats.getBottomContstraint(), camerasPanel);

		JButton startImagingButton = new JButton("start imaging");
		startImagingButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					startButtonPressed();
				}
			});
		
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(startImagingButton, BorderLayout.SOUTH);
		contentPane.add(elementsPanel, BorderLayout.NORTH);
		contentPane.add(new JScrollPane(camerasPanel), BorderLayout.CENTER);
		return contentPane;
	}
	private void startButtonPressed()
	{
		Vector<String> cameras = new Vector<String>();
		for(int i = 0; i < cameraDevices.length; i++)
		{
			if(cameraCheckBoxes[i].isSelected())
				cameras.addElement(cameraDevices[i]);
		}
		closeAddon();
				
		// Start main window.
		@SuppressWarnings("unused")
		MultiStreamFrame multiStreamFrame = new MultiStreamFrame(getContainingFrame().createFrame(), getServer(), getClient(), cameras.toArray(new String[cameras.size()]));
	}
	private static void addConfElement(Component component, GridBagLayout layout,
            GridBagConstraints constr, JPanel panel)
    {
        layout.setConstraints(component, constr);
        panel.add(component);
    }
}
