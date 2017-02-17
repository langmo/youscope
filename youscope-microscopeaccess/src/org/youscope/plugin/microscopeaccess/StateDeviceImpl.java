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
package org.youscope.plugin.microscopeaccess;


import java.util.Arrays;

import org.youscope.addon.microscopeaccess.StateDeviceInternal;
import org.youscope.common.microscope.DeviceSetting;
import org.youscope.common.microscope.DeviceType;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.MicroscopeLockedException;

import mmcorej.CMMCore;
import mmcorej.StrVector;

/**
 * @author langmo
 *
 */
class StateDeviceImpl extends DeviceImpl implements StateDeviceInternal
{
	private int numStates = -1;
	private String[] stateLabels = null;
	
	private static final String LABEL_PROPERTY_ID = "Label";
	
	private final SelectablePropertyImpl labelProperty;
	
	StateDeviceImpl(MicroscopeImpl microscope, String deviceID, String libraryID, String driverID)
	{
		super(microscope, deviceID, libraryID, driverID, DeviceType.StateDevice);
		
		labelProperty =  new SelectablePropertyImpl(microscope, deviceID, LABEL_PROPERTY_ID, new String[0], false, this)
		{
			@Override
			public String[] getAllowedPropertyValues()
			{
				return stateLabels;
			}
			
			@Override
			protected void setStringValue(String value, int accessID) throws MicroscopeException, MicroscopeLockedException
			{
				setState(value, accessID);
			}
		};
	}

	@Override
	protected void initializeDevice(int accessID) throws MicroscopeException
	{
		// Initialize state labels. This must happen before the initialization of the properties
		// since MicroManager has a bug there: if states are not queried, yet, it doesn't know the allowed values...
		// Number of states
		try
		{
			CMMCore core = microscope.startRead();
			numStates = core.getNumberOfStates(getDeviceID());
			
			//JOptionPane.showMessageDialog(null, "State device " + getDeviceID() + ": " + Integer.toString(numStates));
		}
		catch(Exception e)
		{
			throw new MicroscopeException("Could not get number of states for device " + getDeviceID() + ".", e);
		}
		finally
		{
			microscope.unlockRead();
		}
		
		// State labels
		StrVector labelsOrg;
		try
		{
			CMMCore core = microscope.startRead();
			labelsOrg = core.getStateLabels(getDeviceID());
		}
		catch(Exception e)
		{
			throw new MicroscopeException("Could not get state labels for device " + getDeviceID() + ".", e);
		}
		finally
		{
			microscope.unlockRead();
		}
		stateLabels = new String[(int)labelsOrg.size()];
		for(int i = 0; i < stateLabels.length; i++)
		{
			stateLabels[i] = labelsOrg.get(i);
		}
		if(stateLabels.length != numStates)
			throw new MicroscopeException("Number of states of device " + getDeviceID() + " is " + Integer.toString(numStates) + ", but number of state labels is " + Integer.toString(stateLabels.length) + ".");
		
		
		super.initializeDevice(accessID);
	}
	@Override
	public int getNumStates()
	{
		return numStates;
	}
	
	@Override
	protected PropertyImpl initializeProperty(String propertyID) throws MicroscopeException
	{
		// Property "Label" is not really a property of the device, but something like a shortcut changeable in name...
		if(propertyID.equals(LABEL_PROPERTY_ID))
		{
			return labelProperty;
		}
		return super.initializeProperty(propertyID);
	}

	@Override
	public int getState() throws MicroscopeException
	{
		try
		{
			CMMCore core = microscope.startRead();
			return core.getState(getDeviceID());
		}
		catch(Exception e)
		{
			throw new MicroscopeException("Could not get current states for device " + getDeviceID() + ".", e);
		}
		finally
		{
			microscope.unlockRead();
		}
	}

	@Override
	public String[] getStateLabels()
	{
		return Arrays.copyOf(stateLabels, stateLabels.length);
	}

	@Override
	public String getStateLabel() throws MicroscopeException
	{
		return getStateLabel(getState());
	}

	@Override
	public String getStateLabel(int state) throws ArrayIndexOutOfBoundsException
	{
		if(state < 0 || state >= getNumStates())
			throw new ArrayIndexOutOfBoundsException("State number is invalid (allowed values: 0.."+Integer.toString(getNumStates())+", provided value: "+Integer.toString(state)+").");
		return stateLabels[state];
	}

	@Override
	public void setStateLabel(int state, String label, int accessID) throws ArrayIndexOutOfBoundsException, MicroscopeException, MicroscopeLockedException
	{
		if(state < 0 || state >= getNumStates())
			throw new ArrayIndexOutOfBoundsException("State number is invalid (allowed values: 0.."+Integer.toString(getNumStates())+", provided value: "+Integer.toString(state)+").");
		
		DeviceSetting oldLabelSetting = new DeviceSetting(getDeviceID(), LABEL_PROPERTY_ID, new String(stateLabels[state]));
		DeviceSetting newLabelSetting = new DeviceSetting(getDeviceID(), LABEL_PROPERTY_ID, new String(label));
		try
		{
			microscope.lockWrite(accessID);
			stateLabels[state] = label;
			microscope.labelChanged(oldLabelSetting, newLabelSetting);
		}
		catch(MicroscopeLockedException e)
		{
			throw e;
		}
		finally
		{
			microscope.unlockWrite();
		}
		microscope.stateChanged("Label of state "+Integer.toString(state)+" of device " + getDeviceID() + " set to " + label +".");
	}

	@Override
	public void setStateLabels(String[] labels, int accessID) throws ArrayIndexOutOfBoundsException, MicroscopeException, MicroscopeLockedException
	{
		if(labels == null || labels.length != getNumStates())
			throw new ArrayIndexOutOfBoundsException("Wrong number of states provided (" + Integer.toString(labels.length) + " provided, " + Integer.toString(getNumStates()) + " required.");
		for(int i=0; i<labels.length; i++)
		{
			setStateLabel(i, labels[i], accessID);
		}
	}

	@Override
	public void setState(int state, int accessID) throws MicroscopeException, ArrayIndexOutOfBoundsException, MicroscopeLockedException
	{
		if(state < 0 || state >= getNumStates())
			throw new ArrayIndexOutOfBoundsException("State number is invalid (allowed values: 0.."+Integer.toString(getNumStates())+", provided value: "+Integer.toString(state)+").");
		try
		{
			CMMCore core = microscope.startWrite(accessID);
			core.setState(getDeviceID(), state);
		}
		catch(MicroscopeLockedException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			throw new MicroscopeException("Could not set state to " + Integer.toString(state) + " (" + stateLabels[state] + ") for device " + getDeviceID() + ".", e);
		}
		finally
		{
			deviceStateModified();
			microscope.unlockWrite();
		}
		microscope.stateChanged("State of device " + getDeviceID() + " set to " + Integer.toString(state) + " (" + stateLabels[state] + ").");
	}

	@Override
	public void setState(String label, int accessID) throws MicroscopeException, MicroscopeLockedException
	{
		try
		{
			microscope.startWrite(accessID);
			for(int i=0; i<stateLabels.length; i++)
			{
				if(stateLabels[i].equals(label))
				{
					setState(i, accessID);
					break;
				}
			}
		}
		finally
		{
			microscope.unlockWrite();
		}
	}
	
	
}
