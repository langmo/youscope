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
package org.youscope.plugin.slimjob;

import java.rmi.RemoteException;

import org.youscope.common.ComponentRunningException;
import org.youscope.common.image.ImageProducer;
import org.youscope.common.job.CompositeJob;

/**
 * Job to take SLIM images with four different phase shift patterns.
 * 
 * @author Moritz Lang
 */
public interface SlimJob extends CompositeJob, ImageProducer
{		
	/**
	 * Sets the X position of the center of the inner and outer circle (the "donut").
	 * @param maskX X-position.
	 * @throws RemoteException 
	 * @throws ComponentRunningException 
	 */
	public void setMaskX(int maskX) throws RemoteException, ComponentRunningException;

	/**
	 * Returns the X position of the center of the inner and outer circle (the "donut").
	 * @return X-position.
	 * @throws RemoteException 
	 */
	public int getMaskX() throws RemoteException;

	/**
	 * Sets the Y position of the center of the inner and outer circle (the "donut").
	 * @param maskY Y-position.
	 * @throws RemoteException 
	 * @throws ComponentRunningException 
	 */
	public void setMaskY(int maskY) throws RemoteException, ComponentRunningException;

	/**
	 * Returns the attenuation factor with which the SLIM image is calculated.
	 * @return Attenuation factor.
	 * @throws RemoteException 
	 */
	public double getAttenuationFactor()  throws RemoteException;

	/**
	 * Sets the attenuation factor with which the SLIM image is calculated.
	 * @param attenuationFactor Attenuation factor.
	 * @throws RemoteException 
	 * @throws ComponentRunningException 
	 */
	public void setAttenuationFactor(double attenuationFactor) throws RemoteException, ComponentRunningException;
	
	/**
	 * Returns the Y position of the center of the inner and outer circle (the "donut").
	 * @return Y-position.
	 * @throws RemoteException 
	 */
	public int getMaskY() throws RemoteException;

	/**
	 * Sets the radius of the inner circle (the hole in the "donut").
	 * @param innerRadius the inner radius. Must be > 0.
	 * @throws RemoteException 
	 * @throws ComponentRunningException 
	 */
	public void setInnerRadius(int innerRadius) throws RemoteException, ComponentRunningException;

	/**
	 * Returns the radius of the inner circle (the hole in the "donut").
	 * @return the inner radius. Must be > 0.
	 * @throws RemoteException 
	 */
	public int getInnerRadius() throws RemoteException;

	/**
	 * Sets the radius of the outer circle (the "donut").
	 * @param outerRadius the outer radius. Must be > innerRadius.
	 * @throws RemoteException 
	 * @throws ComponentRunningException 
	 */
	public void setOuterRadius(int outerRadius) throws RemoteException, ComponentRunningException;

	/**
	 * Returns the radius of the outer circle (the "donut").
	 * @return the outer radius.
	 * @throws RemoteException 
	 */
	public int getOuterRadius() throws RemoteException;

	/**
	 * Sets the phase shift outside of the mask (background of donut).
	 * @param phaseShiftOutside the outer phase shift. Must be >=0 and < 256.
	 * @throws RemoteException 
	 * @throws ComponentRunningException 
	 */
	public void setPhaseShiftOutside(int phaseShiftOutside) throws RemoteException, ComponentRunningException;

	/**
	 * Returns the phase shift outside of the mask (background of donut).
	 * @return the outer phase shift.
	 * @throws RemoteException 
	 */
	public int getPhaseShiftOutside() throws RemoteException;
	
	/**
	 * Returns the phase shift of the mask (the donut) for the maskID mask.
	 * @param maskID the phase shift. Must be >=0 and < 4.
	 * @return phaseShift the phase shift.
	 * @throws RemoteException 
	 */
	public int getPhaseShiftMask(int maskID) throws RemoteException;
	
	/**
	 * Sets the phase shift of the mask (the donut) for the maskID mask.
	 * @param phaseShift the phase shift. Must be >=0 and < 256.
	 * @param maskID the phase shift. Must be >=0 and < 4.
	 * @throws RemoteException 
	 * @throws ComponentRunningException 
	 */
	public void setPhaseShiftMask(int maskID, int phaseShift) throws RemoteException, ComponentRunningException;
	/**
	 * Sets a short string describing the images which are made by this job.
	 * @param description The description which should be returned for the images produced by this job, or null, to switch to the default description.
	 * @throws RemoteException
	 * @throws ComponentRunningException 
	 */
	void setImageDescription(String description) throws RemoteException, ComponentRunningException;
	
	/**
	 * Returns the name of the reflector device which should be used to generate the pattern.
	 * @return Name of the reflector device.
	 * @throws RemoteException 
	 */
	public String getReflectorDevice() throws RemoteException;

	/**
	 * Returns the name of the reflector device which should be used to generate the pattern.
	 * @param reflectorDevice Name of the reflector device.
	 * @throws RemoteException 
	 * @throws ComponentRunningException 
	 */
	public void setReflectorDevice(String reflectorDevice) throws RemoteException, ComponentRunningException;
		
	/**
	 * Returns the time delay in ms between changing the SLIM reflector settings and taking an image.
	 * @return Delay in ms.
	 * @throws RemoteException 
	 */
	public int getSlimDelayMs() throws RemoteException;
	
	/**
	 * Sets the time delay in ms between changing the SLIM reflector settings and taking an image.
	 * @param delayMs delay in ms. Must be >= 0.
	 * @throws RemoteException 
	 * @throws ComponentRunningException 
	 */
	public void setSlimDelayMs(int delayMs) throws RemoteException, ComponentRunningException;
	
	/**
	 * Sets the file name of the mask which should be used to define foreground and background. Set to null to use donut mode instead.
	 * @param maskFileName Name of file which defines background and foreground, or null for donut mode.
	 * @throws RemoteException 
	 * @throws ComponentRunningException 
	 */
	public void setMaskFileName(String maskFileName) throws RemoteException, ComponentRunningException;
	
	/**
	 * Returns the file name of the mask which should be used to define foreground and background. Returns null if donut mode is active.
	 * @return Name of file which defines background and foreground, or null for donut mode.
	 * @throws RemoteException 
	 */
	public String getMaskFileName() throws RemoteException;
	
}
