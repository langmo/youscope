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
package org.youscope.uielements;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.configuration.CameraConfiguration;
import org.youscope.common.microscope.CameraDevice;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * UI element to select a camera device.
 * @author Moritz Lang
 *
 */
public class CameraField  extends JPanel
{

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -3255470611690866823L;

	private final JComboBox<String>								cameraField		= new JComboBox<String>();

    private final YouScopeClient client;
    private final YouScopeServer server;
    
    private boolean choice = true;
    
    /**
     * Constructor.
     * @param client Interface to the client.
     * @param server Interface to the server.
     */
    public CameraField(YouScopeClient client, YouScopeServer server)
    {
    	this((String)null, client, server);
    }
    
    /**
     * Constructor.
     * @param cameraConfiguration Selected camera device, or null.
     * @param client Interface to the client.
     * @param server Interface to the server.
     */
    public CameraField(CameraConfiguration cameraConfiguration, YouScopeClient client, YouScopeServer server)
    {
    	this((cameraConfiguration==null) ? (String)null : cameraConfiguration.getCameraDevice(), client, server);
    }
    
    /**
     * Constructor.
     * @param camera Selected camera device.
     * @param client Interface to the client.
     * @param server Interface to the server.
     */
    public CameraField(String camera, YouScopeClient client, YouScopeServer server)
    {
    	this.client = client;
    	this.server = server;
    
    	choice = loadCameras() > 1;
    	if(camera == null)
    		camera = getDefaultCameraDevice();
    	if(camera!= null)
    		cameraField.setSelectedItem(camera);
    	setLayout(new BorderLayout());
    	add(cameraField, BorderLayout.CENTER);
    	setOpaque(false);
    	cameraField.setOpaque(false);
    }
    
    /**
     * Adds an action listener which gets notified when the camera selection changed.
     * @param listener Listener to add.
     */
    public void addActionListener(ActionListener listener)
    {
    	cameraField.addActionListener(listener);
    }
    
    /**
     * Removes a previously added Action listener.
     * @param listener Listener to remove.
     */
    public void removeActionListener(ActionListener listener)
    {
    	cameraField.removeActionListener(listener);
    }
    
    /**
     * Returns true if there are at least two cameras, i.e. the user has a real choice which camera to pick.
     * @return True if the user has the choice between at least two cameras.
     */
    public boolean isChoice()
    {
    	return choice;
    }
    
    
    /**
     * Returns the currently selected camera configuration.
     * @return Camera configuration.
     */
    public CameraConfiguration getCameraConfiguration()
    {
    	CameraConfiguration configuration = new CameraConfiguration();
    	Object camera = cameraField.getSelectedItem();
    	configuration.setCameraDevice(camera == null ? null : camera.toString());
    	return configuration;
    }
    
    private String getDefaultCameraDevice()
    {
    	try {
			CameraDevice cameraDevice = server.getMicroscope().getCameraDevice();
			if(cameraDevice == null)
				return null;
			return cameraDevice.getDeviceID();
		} catch (Exception e) {
			client.sendError("Could not get default camera device ID.", e);
			return null;
		}
    }
    
    /**
     * Returns the currently selected camera device, or null if no camera can be selected since there are no camera.
     * @return Camera device name or null.
     */
    public String getCameraDevice()
    {
    	Object camera = cameraField.getSelectedItem();
    	if(camera == null)
    		return null;
    	return camera.toString();
    }
    
    /**
     * Sets the currently chosen camera
     * @param cameraDevice The camera device name.
     */
    public void setCamera(String cameraDevice)
    {
    	if(cameraDevice != null)
    	{
    		cameraField.setSelectedItem(cameraDevice);
    	}
    }
    
    /**
     * Sets the currently selected camera.
     * @param camera Camera configuration.
     */
    public void setCamera(CameraConfiguration camera)
    {
    	if(camera == null || camera.getCameraDevice() == null)
    		return;
    	setCamera(camera.getCameraDevice());
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

}
