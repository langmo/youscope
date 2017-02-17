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

import org.youscope.addon.microscopeaccess.StageDeviceInternal;
import org.youscope.common.microscope.DeviceType;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.MicroscopeLockedException;

import mmcorej.CMMCore;

/**
 * @author langmo
 * 
 */
class StageDeviceImpl extends DeviceImpl implements StageDeviceInternal
{
	private double unitMagnifier = 1.0;
	private boolean transposeX = false;
	private boolean transposeY = false;
	
	private final static String PROPERTY_TRANSPOSE_X = "TransposeMirrorX";
	private final static String PROPERTY_TRANSPOSE_Y = "TransposeMirrorY";
	
	private final static String PROPERTY_POSITION_X = "PositionX";
	private final static String PROPERTY_POSITION_Y = "PositionY";
	
	StageDeviceImpl(MicroscopeImpl microscope, String deviceName, String libraryID, String driverID)
	{
		super(microscope, deviceName, libraryID, driverID, DeviceType.XYStageDevice, new String[]{PROPERTY_TRANSPOSE_X, PROPERTY_TRANSPOSE_Y, PROPERTY_POSITION_X, PROPERTY_POSITION_Y});
	}

	@Override
	protected void initializeDevice(int accessID) throws MicroscopeException
	{
		super.initializeDevice(accessID);
		
		// Add some additional properties...
		properties.put(PROPERTY_POSITION_X, new FloatPropertyImpl(microscope, getDeviceID(), PROPERTY_POSITION_X, Float.MIN_VALUE, Float.MAX_VALUE, false, this)
		{
			@Override
			public String getValue() throws MicroscopeException, InterruptedException
			{
				return Double.toString(getPosition().x);
			}
			
			@Override
			protected void setStringValue(String value, int accessID) throws MicroscopeException, MicroscopeLockedException, InterruptedException
			{
				try
				{
					setPosition(Double.parseDouble(value), getPosition().y, accessID);
				}
				catch(NumberFormatException e)
				{
					throw new MicroscopeException("Value for stage x-position \"" + value + "\" is not a float value.", e);
				}
			}
		});
		
		properties.put(PROPERTY_POSITION_Y, new FloatPropertyImpl(microscope, getDeviceID(), PROPERTY_POSITION_Y, Float.MIN_VALUE, Float.MAX_VALUE, false, this)
		{
			@Override
			public String getValue() throws MicroscopeException, InterruptedException
			{
				return Double.toString(getPosition().y);
			}
			
			@Override
			protected void setStringValue(String value, int accessID) throws MicroscopeException, MicroscopeLockedException, InterruptedException
			{
				try
				{
					setPosition(getPosition().x, Double.parseDouble(value), accessID);
				}
				catch(NumberFormatException e)
				{
					throw new MicroscopeException("Value for stage y-position \"" + value + "\" is not a float value.", e);
				}
			}
		});
	}
	
	@Override
	public java.awt.geom.Point2D.Double getPosition() throws MicroscopeException, InterruptedException
	{
		if(Thread.interrupted())
			throw new InterruptedException();
		double[] x = new double[1];
		double[] y = new double[1];

		try
		{
			CMMCore core = microscope.startRead();
			core.waitForDevice(getDeviceID());
			core.getXYPosition(getDeviceID(), x, y);
		}
		catch(Exception e)
		{
			throw new MicroscopeException("Could not get current position of stage " + getDeviceID() + ".", e);
		}
		finally
		{
			microscope.unlockRead();
		}

		// Transform position from microscope units to program units
		if(isTransposeX())
			x[0] *= -1;
		if(isTransposeY())
			y[0] *= -1;
		double magnifier = getUnitMagnifier();
		if(magnifier != 1.0)
		{
			x[0] *= magnifier;
			y[0] *= magnifier;
		}

		return new java.awt.geom.Point2D.Double(x[0], y[0]);
	}

	@Override
	public void setPosition(double x, double y, int accessID) throws MicroscopeLockedException, MicroscopeException, InterruptedException
	{
		setPosition(x, y, true, accessID);
	}

	@Override
	public void setRelativePosition(double dx, double dy, int accessID) throws MicroscopeLockedException, MicroscopeException, InterruptedException
	{
		setPosition(dx, dy, false, accessID);
	}

	private void setPosition(double x, double y, boolean absolute, int accessID) throws MicroscopeLockedException, MicroscopeException, InterruptedException
	{
		if(Thread.interrupted())
			throw new InterruptedException();
		try
		{
			// Store original values.
			double xOrg = x;
			double yOrg = y;
			// Lock microscope
			CMMCore core = microscope.startWrite(accessID);

			// Transform position to microscope units
			if(isTransposeX())
				x *= -1;
			if(isTransposeY())
				y *= -1;
			double magnifier = getUnitMagnifier();
			if(magnifier != 1.0)
			{
				x /= magnifier;
				y /= magnifier;
			}

			// Calculate absolute from relative position if necessary.
			if(!absolute)
			{
				double[] lastX = new double[1];
				double[] lastY = new double[1];

				try
				{
					core.getXYPosition(getDeviceID(), lastX, lastY);
				}
				catch(Exception e)
				{
					throw new MicroscopeException("Cannot get last stage position.", e);
				}
				x += lastX[0];
				y += lastY[0];
			}
			if(Thread.interrupted())
				throw new InterruptedException();
			try
			{
				core.setXYPosition(getDeviceID(), x, y);
				deviceStateModified();
				// Sleep for 50 ms. Some stages have problems with multi-threading, i.e.
				// they indicate that they are ready even a little bit after they got a new command.
				// Anyway, there is probably no stage which is faster for reasonable distances than 50ms, so this
				// call will probably never harm...
				Thread.sleep(50);
				waitForDevice();
			}
			catch(InterruptedException e)
			{
				throw e;
			}
			catch(Exception e)
			{
				throw new MicroscopeException("Cannot set stage position to " + Double.toString(xOrg) + "/" + Double.toString(yOrg) + ".", e);
			}
			Formatter formatter = new Formatter();
			if(absolute)
				microscope.stateChanged("Position of stage \"" + getDeviceID() + "\" set to " + formatter.format("x = %2.2f um / y = %2.2f um.", xOrg, yOrg));
			else
				microscope.stateChanged("Position of stage \"" + getDeviceID() + "\" changed for " + formatter.format("dx = %2.2f um / dy = %2.2f um.", xOrg, yOrg));
			formatter.close();
		}
		finally
		{
			microscope.unlockWrite();
		}
	}

	@Override
	public void setTransposeX(boolean transpose, int accessID) throws MicroscopeLockedException
	{
		try
		{
			microscope.lockWrite(accessID);
			transposeX = transpose;
		}
		finally
		{
			microscope.unlockWrite();
		}
		microscope.stateChanged("Set x-direction of stage " + getDeviceID() + " to " + (transpose? "transposed." : "not transposed."));
	}

	@Override
	public void setTransposeY(boolean transpose, int accessID) throws MicroscopeLockedException
	{
		try
		{
			microscope.lockWrite(accessID);
			transposeY = transpose;
		}
		finally
		{
			microscope.unlockWrite();
		}
		microscope.stateChanged("Set y-direction of stage " + getDeviceID() + " to " + (transpose? "transposed." : "not transposed."));
	}

	@Override
	public boolean isTransposeX()
	{
		return transposeX;
	}

	@Override
	public boolean isTransposeY()
	{
		return transposeY;
	}

	@Override
	public double getUnitMagnifier()
	{
		return unitMagnifier;
	}

	@Override
	public void setUnitMagnifier(double unitMagnifier, int accessID) throws MicroscopeLockedException
	{
		try
		{
			microscope.lockWrite(accessID);
			this.unitMagnifier = unitMagnifier;
		}
		finally
		{
			microscope.unlockWrite();
		}
		microscope.stateChanged("Set unit magnifier of stage " + getDeviceID() + " to " + Double.toString(unitMagnifier) + " (microns = "+Double.toString(unitMagnifier)+" * units).");
	}

}
