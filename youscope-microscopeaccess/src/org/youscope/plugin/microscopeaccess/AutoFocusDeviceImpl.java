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

import java.util.Formatter;

import org.youscope.addon.microscopeaccess.AutoFocusDeviceInternal;
import org.youscope.common.microscope.DeviceType;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.MicroscopeLockedException;

import mmcorej.CMMCore;

/**
 * @author Moritz Lang
 *
 */
class AutoFocusDeviceImpl extends DeviceImpl implements AutoFocusDeviceInternal
{

	private final static String PROPERTY_OFFSET = "Offset";
	private final static String PROPERTY_LAST_SCORE = "LastScore";
	private final static String PROPERTY_CURRENT_SCORE = "CurrentScore";
	private final static String PROPERTY_ENABLED = "Enabled";
	private final static String PROPERTY_LOCKED = "Locked";
	private final static String PROPERTY_TRIGGER_FULL_FOCUS = "TriggerFullFocus";
	private final static String PROPERTY_TRIGGER_INCREMENTAL_FOCUS = "TriggerIncrementalFocus";
	AutoFocusDeviceImpl(MicroscopeImpl microscope, String deviceID, String libraryID, String driverID)
	{
		super(microscope, deviceID, libraryID, driverID, DeviceType.AutoFocusDevice);
	}
	
	@Override
	protected void initializeDevice(int accessID) throws MicroscopeException
	{
		super.initializeDevice(accessID);
		
		// Add some additional properties...
		properties.put(PROPERTY_OFFSET, new FloatPropertyImpl(microscope, getDeviceID(), PROPERTY_OFFSET, Float.MIN_VALUE, Float.MAX_VALUE, false, this)
		{
			@Override
			public String getValue() throws MicroscopeException, InterruptedException
			{
				return Float.toString((float)getOffset());
			}
			
			@Override
			protected void setStringValue(String value, int accessID) throws MicroscopeException, MicroscopeLockedException, InterruptedException
			{
				try
				{
					setOffset(Double.parseDouble(value), accessID);
				}
				catch(NumberFormatException e)
				{
					throw new MicroscopeException("Provided value for offset \"" + value + "\" is not a float value.", e);
				}
			}
		});
		
		properties.put(PROPERTY_LAST_SCORE, new ReadOnlyPropertyImpl(microscope, getDeviceID(), PROPERTY_LAST_SCORE, false, this)
		{
			@Override
			public String getValue() throws MicroscopeException, InterruptedException
			{
				return Double.toString(getLastScore());
			}
		});
		
		properties.put(PROPERTY_CURRENT_SCORE, new ReadOnlyPropertyImpl(microscope, getDeviceID(), PROPERTY_CURRENT_SCORE, false, this)
		{
			@Override
			public String getValue() throws MicroscopeException, InterruptedException
			{
				return Double.toString(getCurrentScore());
			}
		});
		
		properties.put(PROPERTY_LOCKED, new ReadOnlyPropertyImpl(microscope, getDeviceID(), PROPERTY_LOCKED, false, this)
		{
			@Override
			public String getValue() throws MicroscopeException, InterruptedException
			{
				return isLocked() ? "1" : "0";
			}
		});
		
		properties.put(PROPERTY_ENABLED, new SelectablePropertyImpl(microscope, getDeviceID(), PROPERTY_ENABLED, new String[]{"0", "1"}, false, this)
		{
			@Override
			public String getValue() throws MicroscopeException, InterruptedException
			{
				return isEnabled() ? "1" : "0";
			}
			
			@Override
			protected void setStringValue(String value, int accessID) throws MicroscopeException, MicroscopeLockedException, InterruptedException
			{
			
				if(value.equals("1"))
					setEnabled(true, accessID);
				else if(value.equals("0"))
					setEnabled(false, accessID);
				else
					throw new MicroscopeException(getDeviceID() + "." + PROPERTY_ENABLED + " can only be set to 0 or 1. Provided value: " + value + ".");
			}
		});
		properties.put(PROPERTY_TRIGGER_FULL_FOCUS, new StringPropertyImpl(microscope, getDeviceID(), PROPERTY_TRIGGER_FULL_FOCUS, false, this)
		{
			private String value = "";
			@Override
			public String getValue() throws MicroscopeException, InterruptedException
			{
				return value;
			}
			
			@Override
			protected void setStringValue(String value, int accessID) throws MicroscopeException, MicroscopeLockedException, InterruptedException
			{
				runFullFocus(accessID);
				this.value = value;
			}
		});
		properties.put(PROPERTY_TRIGGER_INCREMENTAL_FOCUS, new StringPropertyImpl(microscope, getDeviceID(), PROPERTY_TRIGGER_INCREMENTAL_FOCUS, false, this)
		{
			private String value = "";
			@Override
			public String getValue() throws MicroscopeException, InterruptedException
			{
				return value;
			}
			
			@Override
			protected void setStringValue(String value, int accessID) throws MicroscopeException, MicroscopeLockedException, InterruptedException
			{
				runIncrementalFocus(accessID);
				this.value = value;
			}
		});
	}
	
	@Override
	public double getLastScore() throws MicroscopeException
	{
		try
		{
			CMMCore core = microscope.startRead();
			core.setAutoFocusDevice(getDeviceID());
			return core.getLastFocusScore();
		}
		catch(Exception e)
		{
			throw new MicroscopeException("Could not get last auto-focus score.", e);
		}
		finally
		{
			microscope.unlockRead();
		}
	}

	@Override
	public double getCurrentScore() throws MicroscopeException
	{
		try
		{
			CMMCore core = microscope.startRead();
			core.setAutoFocusDevice(getDeviceID());
			return core.getCurrentFocusScore();
		}
		catch(Exception e)
		{
			throw new MicroscopeException("Could not get current auto-focus score.", e);
		}
		finally
		{
			microscope.unlockRead();
		}
	}

	@Override
	public void setEnabled(boolean enable, int accessID) throws MicroscopeException, MicroscopeLockedException
	{
		try
		{
			CMMCore core = microscope.startWrite(accessID);
			core.setAutoFocusDevice(getDeviceID());
			core.enableContinuousFocus(enable);
		}
		catch(MicroscopeLockedException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			throw new MicroscopeException("Could not " + (enable ? "enable":"disable") + " auto-focus.", e);
		}
		finally
		{
			deviceStateModified();
			microscope.unlockWrite();
		}
	}

	@Override
	public boolean isEnabled() throws MicroscopeException
	{
		try
		{
			CMMCore core = microscope.startRead();
			core.setAutoFocusDevice(getDeviceID());
			return core.isContinuousFocusEnabled ();
		}
		catch(Exception e)
		{
			throw new MicroscopeException("Could not detect if auto-focus is enabled.", e);
		}
		finally
		{
			microscope.unlockRead();
		}
	}

	@Override
	public boolean isLocked() throws MicroscopeException
	{
		try
		{
			CMMCore core = microscope.startRead();
			core.setAutoFocusDevice(getDeviceID());
			return core.isContinuousFocusLocked ();
		}
		catch(Exception e)
		{
			throw new MicroscopeException("Could not detect if auto-focus is locked.", e);
		}
		finally
		{
			microscope.unlockRead();
		}
	}

	@Override
	public void runFullFocus(int accessID) throws MicroscopeException, MicroscopeLockedException
	{
		try
		{
			CMMCore core = microscope.startWrite(accessID);
			core.setAutoFocusDevice(getDeviceID());
			core.fullFocus();
		}
		catch(MicroscopeLockedException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			throw new MicroscopeException("Could not run full auto-focus search.", e);
		}
		finally
		{
			deviceStateModified();
			microscope.unlockWrite();
		}
		
		microscope.stateChanged("Running full focus for auto-focus \"" + getDeviceID() + "\".");
	}

	@Override
	public void runIncrementalFocus(int accessID) throws MicroscopeException, MicroscopeLockedException
	{
		try
		{
			CMMCore core = microscope.startWrite(accessID);
			core.setAutoFocusDevice(getDeviceID());
			core.incrementalFocus();
		}
		catch(MicroscopeLockedException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			throw new MicroscopeException("Could not run incremental auto-focus search.", e);
		}
		finally
		{
			deviceStateModified();
			microscope.unlockWrite();
		}
		
		microscope.stateChanged("Running incremental focus for auto-focus \"" + getDeviceID() + "\".");
	}

	@Override
	public void setOffset(double offset, int accessID) throws MicroscopeException, MicroscopeLockedException
	{
		try
		{
			CMMCore core = microscope.startWrite(accessID);
			core.setAutoFocusDevice(getDeviceID());
			core.setAutoFocusOffset(offset);
		}
		catch(MicroscopeLockedException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			throw new MicroscopeException("Could not set auto-focus offset.", e);
		}
		finally
		{
			deviceStateModified();
			microscope.unlockWrite();
		}
		Formatter formatter = new Formatter();
		String offsetStr = formatter.format("%2.2f um.", offset).toString();
		formatter.close();
		microscope.stateChanged("Offset of auto-focus \"" + getDeviceID() + "\" set to " + offsetStr);
	}

	@Override
	public double getOffset() throws MicroscopeException
	{
		try
		{
			CMMCore core = microscope.startRead();
			core.setAutoFocusDevice(getDeviceID());
			return core.getAutoFocusOffset();
		}
		catch(Exception e)
		{
			throw new MicroscopeException("Could not get auto-focus offset.", e);
		}
		finally
		{
			microscope.unlockRead();
		}
	}
}
