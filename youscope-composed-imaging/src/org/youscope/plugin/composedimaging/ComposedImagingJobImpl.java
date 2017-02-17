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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.rmi.RemoteException;
import java.util.Vector;

import org.youscope.common.ComponentRunningException;
import org.youscope.common.ExecutionInformation;
import org.youscope.common.MeasurementContext;
import org.youscope.common.PositionInformation;
import org.youscope.common.image.ImageEvent;
import org.youscope.common.image.ImageListener;
import org.youscope.common.job.JobAdapter;
import org.youscope.common.job.JobException;
import org.youscope.common.microscope.Microscope;

/**
 * @author langmo 
 */
class ComposedImagingJobImpl  extends JobAdapter implements ComposedImagingJob
{
	/**
	 * SerializableVersion UID.
	 */
	private static final long	serialVersionUID	= 8128149758338178084L;

	private String							channel				= null;
	private String							configGroup			= null;
	private double[]							exposures			= new double[] {10};
	private String[]							cameras				= new String[] {null};						

	private Vector<ImageListener>	imageListeners		= new Vector<ImageListener>();
	
	private double				dx = 0;

	private double				dy = 0;

	private int					nx = 1;

	private int					ny = 1;

	public ComposedImagingJobImpl(PositionInformation positionInformation) throws RemoteException
	{
		super(positionInformation);
	}

	@Override
	public String getImageDescription()
	{
		String description = "Composed " + Integer.toString(nx) + "x" + Integer.toString(ny) + " image";
		if(channel != null && channel.length() > 0)
			description += ", channel " + channel;
		if(exposures[0] > 0)
			description += ", exposure = " + Double.toString(exposures[0]) + "ms";
		return description;
	}

	@Override
	public double getDeltaX()
	{
		return dx;
	}

	@Override
	public void setDeltaX(double deltaX) throws ComponentRunningException
	{
		assertRunning();
		ComposedImagingJobImpl.this.dx = deltaX;
	}

	@Override
	public double getDeltaY()
	{
		return ny;
	}

	@Override
	public void setDeltaY(double deltaY) throws ComponentRunningException
	{
		assertRunning();
		ComposedImagingJobImpl.this.dy = deltaY;
	}

	@Override
	public Dimension getSubImageNumber()
	{
		return new Dimension(nx, ny);
	}

	@Override
	public void setSubImageNumber(Dimension imageNumbers) throws ComponentRunningException
	{
		assertRunning();
		ComposedImagingJobImpl.this.nx = imageNumbers.width;
		ComposedImagingJobImpl.this.ny = imageNumbers.height;
	}

	private class ComposedImagePosition
	{
		private Point2D.Double	xyPosition;

		private Point			rowColumnPosition;

		ComposedImagePosition(double xpos, double ypos, int column, int row)
		{
			xyPosition = new Point2D.Double(xpos, ypos);
			rowColumnPosition = new Point(column, row);
		}

		public Point2D.Double getXYPosition()
		{
			return xyPosition;
		}

		public Point getRowColumnPosition()
		{
			return rowColumnPosition;
		}
	}

	private ComposedImagePosition[] calculatePositions(double x0, double y0, double dx, double dy, int nx, int ny)
	{
		Vector<ComposedImagePosition> positions = new Vector<ComposedImagePosition>();
		for(int i = 0; i < nx; i++)
		{
			for(int j = 0; j < ny; j++)
			{
				ComposedImagePosition position = new ComposedImagePosition(x0 + i * dx, y0 + j * dy, i, j);
				positions.add(position);
			}
		}

		return positions.toArray(new ComposedImagePosition[positions.size()]);
	}

	@Override
	public void runJob(ExecutionInformation executionInformation, Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		// Get current position
		// Added wait for one second since the current microscope position
		// is updated with a
		// certain delay.
		// TODO: Check if it is possible to discard the wait.
		Thread.sleep(1000);
		Point2D.Double zeroPosition;
		try
		{
			zeroPosition = microscope.getStageDevice().getPosition();
		}
		catch(Exception e1)
		{
			throw new JobException("Could not obtain stage position where composed imaging starts.", e1);
		}

		// Calculate positions where images are made.
		ComposedImagePosition[] positions = calculatePositions(zeroPosition.x, zeroPosition.y, dx, dy, nx, ny);

		if(Thread.interrupted())
			throw new InterruptedException();
		
		// Iterate over all positions
		for(ComposedImagePosition position : positions)
		{
			if(Thread.interrupted())
				throw new InterruptedException();

			// Set position
			try
			{
				microscope.getStageDevice().setPosition(position.getXYPosition().x, position.getXYPosition().y);
			}
			catch(Exception e1)
			{
				throw new JobException("Could not set stage position to next sub-image position.", e1);
			}

			if(Thread.interrupted())
				throw new InterruptedException();

			ImageEvent<?> e;
			try
			{
				e = microscope.getCameraDevice().makeImage(configGroup, channel, exposures[0]);
			}
			catch(Exception e1)
			{
				throw new JobException("Could not take sub-image for composed image..", e1);
			}
			e.setChannel(channel);
			e.setChannelGroup(configGroup);
			
			// Create position information for element.
			PositionInformation temp = new PositionInformation(getPositionInformation(), PositionInformation.POSITION_TYPE_YTILE, position.getRowColumnPosition().y);
			PositionInformation positionInformation = new PositionInformation(temp, PositionInformation.POSITION_TYPE_XTILE, position.getRowColumnPosition().x);
			
			e.setPositionInformation(positionInformation);
			e.setExecutionInformation(executionInformation);
			e.setCreationRuntime(measurementContext.getMeasurementRuntime());
			
			sendImageToListeners(e);
		}
		try
		{
			microscope.getStageDevice().setPosition(zeroPosition.x, zeroPosition.y);
		}
		catch(Exception e1)
		{
			throw new JobException("Could not reset stage position after taking sub-images for composed image.", e1);
		}
	}

	@Override
	protected String getDefaultName()
	{
		return "Composed Imaging";
	}

	@Override
	public void setExposure(double exposure) throws ComponentRunningException
	{
		exposures = new double[cameras.length];
		for(int i = 0; i < exposures.length; i++)
		{
			exposures[i] = exposure;
		}
		setExposures(exposures);
	}

	@Override
	public double getExposure()
	{
		return getExposures()[0];
	}

	@Override
	public String getChannelGroup() throws RemoteException
	{
		return configGroup;
	}

	@Override
	public String[] getCameras() throws RemoteException
	{
		return cameras;
	}

	@Override
	public synchronized void setChannel(String deviceGroup, String channel) throws ComponentRunningException
	{
		assertRunning();
		this.configGroup = deviceGroup;
		this.channel = channel;
	}

	@Override
	public String getChannel()
	{
		return channel;
	}

	@Override
	public synchronized void setCameras(String[] cameras) throws ComponentRunningException
	{
		assertRunning();

		if(cameras == null)
			cameras = new String[] {null};

		this.cameras = cameras;

		// Adjust length of exposures array
		if(exposures.length > this.cameras.length)
		{
			double[] newExposures = new double[cameras.length];
			System.arraycopy(exposures, 0, newExposures, 0, newExposures.length);
			exposures = newExposures;
		}
		else if(exposures.length < this.cameras.length)
		{
			double[] newExposures = new double[cameras.length];
			System.arraycopy(exposures, 0, newExposures, 0, exposures.length);
			for(int i = exposures.length; i < newExposures.length; i++)
			{
				// Fill with "do not set".
				newExposures[i] = -1.0;
			}
			exposures = newExposures;
		}
	}

	@Override
	public synchronized void setExposures(double[] exposures) throws ComponentRunningException
	{
		assertRunning();
		this.exposures = exposures;
	}

	@Override
	public double[] getExposures()
	{
		return exposures;
	}

	@Override
	public void addImageListener(ImageListener listener)
	{
		if(listener == null)
			return;
		synchronized(imageListeners)
		{
			imageListeners.add(listener);
		}
	}

	@Override
	public void removeImageListener(ImageListener listener)
	{
		if(listener == null)
			return;
		synchronized(imageListeners)
		{
			imageListeners.remove(listener);
		}
	}
	
	protected void sendImageToListeners(ImageEvent<?> e)
	{
		synchronized(imageListeners)
		{
			for(int i = 0; i < imageListeners.size(); i++)
			{
				ImageListener listener = imageListeners.elementAt(i);
				try
				{
					listener.imageMade(e);
				}
				catch(@SuppressWarnings("unused") RemoteException e1)
				{
					// Connection probably broken down...
					imageListeners.removeElementAt(i);
					i--;
				}
			}
		}
	}

	@Override
	public int getNumberOfImages() throws RemoteException
	{
		return nx * ny;
	}
}
