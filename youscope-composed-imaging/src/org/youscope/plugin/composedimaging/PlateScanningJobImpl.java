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
import java.util.ArrayList;
import java.util.Vector;

import org.youscope.common.ComponentRunningException;
import org.youscope.common.ExecutionInformation;
import org.youscope.common.MeasurementContext;
import org.youscope.common.PositionInformation;
import org.youscope.common.job.Job;
import org.youscope.common.job.JobAdapter;
import org.youscope.common.job.JobException;
import org.youscope.common.microscope.Microscope;

/**
 * Job that scans a microplate at previous defined positions. Executes one job of its job list at every of the defined positions
 * @author langmo 
 */
class PlateScanningJobImpl  extends JobAdapter implements PlateScanningJob
{
	/**
	 * SerializableVersion UID.
	 */
	private static final long	serialVersionUID	= 1128149758338178084L;

	private double				dx = 0;

	private double				dy = 0;

	private int					nx = 0;

	private int					ny = 0;
	
	private final ArrayList<Job>	jobs				= new ArrayList<Job>();

	public PlateScanningJobImpl(PositionInformation positionInformation) throws RemoteException
	{
		super(positionInformation);
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
		PlateScanningJobImpl.this.dx = deltaX;
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
		PlateScanningJobImpl.this.dy = deltaY;
	}

	@Override
	public Dimension getNumTiles()
	{
		return new Dimension(nx, ny);
	}

	@Override
	public void setNumTiles(Dimension imageNumbers) throws ComponentRunningException
	{
		assertRunning();
		PlateScanningJobImpl.this.nx = imageNumbers.width;
		PlateScanningJobImpl.this.ny = imageNumbers.height;
	}

	protected class ScanningPosition
	{
		private Point2D.Double	xyPosition;

		private Point			rowColumnPosition;

		ScanningPosition(double xpos, double ypos, int column, int row)
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

	private ScanningPosition[] calculatePositions(double x0, double y0, double dx, double dy, int nx, int ny)
	{
		Vector<ScanningPosition> positions = new Vector<ScanningPosition>();
		
		for(int j = 0; j < ny; j++)
		{
			for(int i = 0; i < nx; i++)
			{
				ScanningPosition position = new ScanningPosition(x0 + i * dx, y0 + j * dy, i, j);
				positions.add(position);
			}
		}

		return positions.toArray(new ScanningPosition[positions.size()]);
	}

	@Override
	public void initializeJob(Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		if(jobs.size() != nx * ny)
			throw new JobException("Number of jobs (currently " + Integer.toString(jobs.size()) + ") must be equal to number of positions (currently " + Integer.toString(nx * ny) + ").");
		
		super.initializeJob(microscope, measurementContext);

		synchronized(jobs)
		{
			for(Job job : jobs)
			{
				job.initializeJob(microscope, measurementContext);
			}
		}
	}

	@Override
	public void uninitializeJob(Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		super.uninitializeJob(microscope, measurementContext);

		synchronized(jobs)
		{
			for(Job job : jobs)
			{
				job.uninitializeJob(microscope, measurementContext);
			}
		}
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
		catch(Exception e)
		{
			throw new JobException("Could not obtain initial stage position for scanning.", e);
		}
		// Calculate positions where images are made.
		ScanningPosition[] positions = calculatePositions(zeroPosition.x, zeroPosition.y, dx, dy, nx, ny);

		if(Thread.interrupted())
			throw new InterruptedException();
		
		// Iterate over all positions
		for(int pos = 0; pos < positions.length; pos++)
		{
			if(Thread.interrupted())
				throw new InterruptedException();

			// Set position
			try
			{
				microscope.getStageDevice().setPosition(positions[pos].getXYPosition().x, positions[pos].getXYPosition().y);
			}
			catch(Exception e)
			{
				throw new JobException("Could not move stage to next scanning position.", e);
			}

			if(Thread.interrupted())
				throw new InterruptedException();
			
			// Execute respective jobs
			jobs.get(pos).executeJob(executionInformation, microscope, measurementContext);
		}
		try
		{
			microscope.getStageDevice().setPosition(zeroPosition.x, zeroPosition.y);
		}
		catch(Exception e)
		{
			throw new JobException("Could not move stage back to initial position after scanning.", e);
		}
	}

	@Override
	protected String getDefaultName()
	{
		return "Plate Scanning";
	}

	@Override
	public synchronized void addJob(Job job) throws RemoteException, ComponentRunningException
	{
		assertRunning();
		synchronized(jobs)
		{
			jobs.add(job);
		}
	}

	@Override
	public synchronized void removeJob(int jobIndex) throws RemoteException, ComponentRunningException, IndexOutOfBoundsException
	{
		assertRunning();
		synchronized(jobs)
		{
			jobs.remove(jobIndex);
		}
	}

	@Override
	public synchronized void clearJobs() throws RemoteException, ComponentRunningException
	{
		assertRunning();
		synchronized(jobs)
		{
			jobs.clear();
		}
	}

	@Override
	public Job[] getJobs() throws RemoteException
	{
		synchronized(jobs)
		{
			return jobs.toArray(new Job[jobs.size()]);
		}
	}

	@Override
	public void insertJob(Job job, int jobIndex)
			throws RemoteException, ComponentRunningException, IndexOutOfBoundsException {
		assertRunning();
		jobs.add(jobIndex, job);
	}

	@Override
	public int getNumJobs() throws RemoteException {
		return jobs.size();
	}

	@Override
	public Job getJob(int jobIndex) throws RemoteException, IndexOutOfBoundsException {
		return jobs.get(jobIndex);
	}
}
