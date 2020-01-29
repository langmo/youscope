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
package org.youscope.common.configuration;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * Configuration of a camera, identifier by its camera device name.
 * @author Moritz Lang
 *
 */
@XStreamAlias("camera-configuration")
public final class CameraConfiguration  implements Configuration, Comparable<CameraConfiguration>
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 1636100109620400761L;
	@XStreamAlias("camera-device")
	@XStreamAsAttribute
	private String cameraDevice;
	/**
	 * Default Constructor.
	 * Sets camera device to null (i.e. default camera).
	 */
	public CameraConfiguration() 
	{
		this.cameraDevice = null;
	}
	/**
	 * Constructor.
	 * @param cameraDevice The current camera device.
	 */
	public CameraConfiguration(String cameraDevice) 
	{
		this.cameraDevice = cameraDevice;
	}
	
	/**
	 * Copy constructor. If configuration is null, sets camera device to null.
	 * @param configuration The configuration to copy.
	 */
	public CameraConfiguration(CameraConfiguration configuration)
	{
		this.cameraDevice = configuration== null?null : configuration.getCameraDevice();
	}
	/**
	 * Returns the name of the camera device, or null for the default camera.
	 * @return Camera device name.
	 */
	public String getCameraDevice() {
		return cameraDevice;
	}
	
	/**
	 * Copies the content (i.e. the camera device) of the configuration configuration. If configuration is null, sets
	 * camera device to null (i.e. default camera). 
	 * @param configuration The configuration to copy.
	 */
	public void copyConfiguration(CameraConfiguration configuration)
	{
		this.cameraDevice = configuration == null ? null : configuration.getCameraDevice();
	}
	
	/**
	 * Set the camera device name, or null to use the default camera device.
	 * @param cameraDevice Camera device name.
	 */
	public void setCameraDevice(String cameraDevice) {
		this.cameraDevice = cameraDevice;
	}
	@Override
	public int compareTo(CameraConfiguration other) 
	{
		if(cameraDevice == null && other.cameraDevice == null)
			return 0;
		else if(cameraDevice == null && other.cameraDevice != null)
			return 1;
		else if(cameraDevice != null && other.cameraDevice == null)
			return -1;
		else
			return cameraDevice.compareTo(other.cameraDevice);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cameraDevice == null) ? 0 : cameraDevice.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CameraConfiguration other = (CameraConfiguration) obj;
		if (cameraDevice == null) {
			if (other.cameraDevice != null)
				return false;
		} else if (!cameraDevice.equals(other.cameraDevice))
			return false;
		return true;
	}
	@Override
	public String getTypeIdentifier() 
	{
		return "YouScope.Camera";
	}
	@Override
	public void checkConfiguration() throws ConfigurationException {
		// nothing to check.
		
	}
}
