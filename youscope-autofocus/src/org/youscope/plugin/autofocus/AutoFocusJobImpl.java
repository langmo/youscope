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
package org.youscope.plugin.autofocus;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.youscope.addon.focusscore.FocusScoreResource;
import org.youscope.addon.focussearch.FocusSearchOracle;
import org.youscope.addon.focussearch.FocusSearchResource;
import org.youscope.common.ComponentRunningException;
import org.youscope.common.ExecutionInformation;
import org.youscope.common.MeasurementContext;
import org.youscope.common.PositionInformation;
import org.youscope.common.image.ImageEvent;
import org.youscope.common.image.ImageListener;
import org.youscope.common.job.Job;
import org.youscope.common.job.JobAdapter;
import org.youscope.common.job.JobException;
import org.youscope.common.microscope.Channel;
import org.youscope.common.microscope.DeviceException;
import org.youscope.common.microscope.Microscope;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.MicroscopeLockedException;
import org.youscope.common.microscope.SettingException;
import org.youscope.common.resource.ResourceException;
import org.youscope.common.table.Table;
import org.youscope.common.table.TableDefinition;
import org.youscope.common.table.TableException;
import org.youscope.common.table.TableListener;

/**
 * @author Moritz Lang
 */
class AutoFocusJobImpl extends JobAdapter implements AutoFocusJob
{
	/**
	 * Serial Version UID.
	 */
	private static final long				serialVersionUID	= -4448352144003480453L;
	private volatile String					channel				= null;
	private volatile String					configGroup			= null;
	private volatile double					exposure			= 10;
	private final ArrayList<ImageListener>	imageListeners		= new ArrayList<ImageListener>();
	private final ArrayList<TableListener>	tableDataListeners	= new ArrayList<TableListener>();
	private volatile String imageDescription = null;
	private final ArrayList<Job>	jobs				= new ArrayList<Job>();
	
	private volatile int				adjustmentTime		= 0;

	private String			focusDevice			= null;
	
	private volatile boolean resetFocusAfterSearch = true;
	private boolean rememberFocus = false;
	
	private FocusScoreResource focusScoreAlgorithm = null;
	private FocusSearchResource focusSearchAlgorithm = null;
	
	double lastOptimalRelativeFocus = 0;
	
	public AutoFocusJobImpl(PositionInformation positionInformation) throws RemoteException
	{
		super(positionInformation);
	}

	@Override
	public String getImageDescription() 
	{
		if(imageDescription == null)
			return getDefaultImageDescription();
		return imageDescription;
	}
	private String getDefaultImageDescription()
	{
		String retVal = "";
		if(channel != null && channel.length() > 0)
			retVal += "channel = " + channel;
		if(retVal.length() > 0)
			retVal += ", ";
		retVal += "exposure = " + Double.toString(exposure) + "ms";
		return retVal;
	}

	@Override
	public void setImageDescription(String description) throws RemoteException, ComponentRunningException
	{
		assertRunning();
		imageDescription = description;
	}
	
	@Override
	public synchronized void setExposure(double exposure) throws ComponentRunningException
	{
		assertRunning();
		this.exposure = exposure;
	}
	
	

	@Override
	public double getExposure()
	{
		return exposure;
	}

	@Override
	public String getChannelGroup() throws RemoteException
	{
		return configGroup;
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

	@Override
	public void initializeJob(Microscope microscope, MeasurementContext measurementContext) throws JobException, RemoteException, InterruptedException
	{
		super.initializeJob(microscope, measurementContext);
	
		lastOptimalRelativeFocus = 0;
	
		// Initialize Focus Score algorithm
		if(focusScoreAlgorithm == null)
			throw new JobException("No focus score algorithm set.");
		try
		{
			focusScoreAlgorithm.initialize(measurementContext);
		}
		catch(ResourceException e)
		{
			throw new JobException("Could not initialize focus score algorithm.", e);
		}
		
		// Initialize Focus Search algorithm
		if(focusSearchAlgorithm == null)
			throw new JobException("No focus search algorithm set.");
		
		try
		{
			focusSearchAlgorithm.initialize(measurementContext);
		}
		catch(ResourceException e)
		{
			throw new JobException("Could not initialize focus search algorithm.", e);
		}
		
		// Initialize child jobs
		synchronized(jobs)
		{
			for(Job job : jobs)
			{
				job.initializeJob(microscope, measurementContext);
			}
		}
	}

	private class FocusInfo
	{
		public final double relFocusPosition;
		public final double focusScore;
		FocusInfo(double relFocusPosition, double focusScore)
		{
			this.relFocusPosition = relFocusPosition;
			this.focusScore = focusScore;
		}
	}
	
	@Override
	public void uninitializeJob(Microscope microscope, MeasurementContext measurementContext) throws JobException, RemoteException, InterruptedException
	{
		// Uninitialize child jobs
		synchronized(jobs)
		{
			for(Job job : jobs)
			{
				job.uninitializeJob(microscope, measurementContext);
			}
		}
		
		// uninitialize focus score
		if(focusScoreAlgorithm != null)
		{
			try
			{
				focusScoreAlgorithm.uninitialize(measurementContext);
			}
			catch(@SuppressWarnings("unused") ResourceException e)
			{
				// do nothing.
			}
		}
		
		// uninitialize focus search
		if(focusSearchAlgorithm != null)
		{
			try
			{
				focusSearchAlgorithm.uninitialize(measurementContext);
			}
			catch(@SuppressWarnings("unused") ResourceException e)
			{
				// do nothing.
			}
		}
		
		super.uninitializeJob(microscope, measurementContext);
	}
	
	@Override
	public void runJob(final ExecutionInformation executionInformation, final Microscope microscope, final MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		// wait a short time. Some focus devices do not adjust their current position immediately.
		Thread.sleep(100);
		
		// get current absolute focus
		final double zeroPosition;
		try
		{
			if(focusDevice == null)
			{
				zeroPosition = microscope.getFocusDevice().getFocusPosition();
			}
			else
			{
				zeroPosition = microscope.getFocusDevice(focusDevice).getFocusPosition();
			}
		}
		catch(DeviceException e)
		{
			throw new JobException("Could not obtain current focus position.", e);
		}
		catch(MicroscopeException e)
		{
			throw new JobException("Could not obtain current focus position.", e);
		}
		
		// Activate channel.
		final Channel myChannel;
		try
		{
			myChannel = microscope.getChannelManager().getChannel(configGroup, AutoFocusJobImpl.this.channel);
		}
		catch(SettingException e)
		{
			throw new JobException("Could not find channel "+configGroup+"."+channel, e);
		}
		if(channel != null)
		{
			try
			{
				microscope.applyDeviceSettings(myChannel.getChannelOnSettings());
				if(myChannel.getShutter() != null)
					microscope.getShutterDevice(myChannel.getShutter()).setOpen(true);
			}
			catch(Exception e)
			{
				throw new JobException("Could not activate channel "+configGroup+"."+channel, e);
			}
		}
		
		// Run focus search
		class Oracle implements FocusSearchOracle
		{
			private static final int EXPECTED_NUM_ITERATIONS = 10;
			public ArrayList<FocusInfo> focusInfos = new ArrayList<FocusInfo>(EXPECTED_NUM_ITERATIONS);
			public int maxFocusIdx = -1; 
			public double maxFocusScore = 0;
			@Override
			public double getFocusScore(double relativeFocusPosition) throws ResourceException 
			{
				int step = focusInfos.size();
				if(rememberFocus)
					relativeFocusPosition += lastOptimalRelativeFocus;
				double focusScore;
				try
				{
					setFocus(microscope, zeroPosition + relativeFocusPosition);
					if(adjustmentTime>0)
						Thread.sleep(adjustmentTime);
					ImageEvent<?> image = takeImage(microscope, measurementContext, executionInformation, new PositionInformation(getPositionInformation(), PositionInformation.POSITION_TYPE_ZSTACK, step));			
					focusScore = getScore(image);
				}
				catch(RemoteException e)
				{
					throw new ResourceException("Could not determine focus score at relative focus position " + zeroPosition + relativeFocusPosition + ".", e);
				}
				catch(InterruptedException e)
				{
					throw new ResourceException("Could not determine focus score at relative focus position " + zeroPosition + relativeFocusPosition + ".", e);
				}
				catch(JobException e)
				{
					throw new ResourceException("Could not determine focus score at relative focus position " + zeroPosition + relativeFocusPosition + ".", e);
				}
				focusInfos.add(new FocusInfo(relativeFocusPosition, focusScore));
				if(focusScore > maxFocusScore)
				{
					maxFocusScore = focusScore;
					maxFocusIdx = step;
				}
				
				sendMessage("Focus score " + Integer.toString(step+1) + " (relative focus " + Double.toString(relativeFocusPosition) + " ) is " + Double.toString(focusScore) + ".");
				
				return focusScore;
			}
			
		}
		Oracle oracle = new Oracle();
		try 
		{
			lastOptimalRelativeFocus += focusSearchAlgorithm.runAutofocus(oracle);
		} 
		catch (ResourceException e1) 
		{
			throw new JobException("Focus optimization failed.", e1);
		}
		
		// Deactivate channel
		if(channel != null)
		{
			try
			{
				microscope.applyDeviceSettings(myChannel.getChannelOffSettings());
				if(myChannel.getShutter() != null)
					microscope.getShutterDevice(myChannel.getShutter()).setOpen(false);
			}
			catch(Exception e)
			{
				throw new JobException("Could not activate channel "+configGroup+"."+channel, e);
			}
		}
		
		if(oracle.maxFocusIdx < 0)
			throw new JobException("Could not find focus positon with positive score.");
		
		// set to best focus
		setFocus(microscope, zeroPosition + lastOptimalRelativeFocus);
	
		
		// now evaluate child jobs
		synchronized(jobs)
		{
			for(int i = 0; i < jobs.size(); i++)
			{
				jobs.get(i).executeJob(executionInformation, microscope, measurementContext);
				if(Thread.interrupted())
					throw new InterruptedException();
			}
		}
		
		// set focus back.
		if(resetFocusAfterSearch)
		{
			setFocus(microscope, zeroPosition);
		}
		
		// send focus information to listeners
		Table table;
		try
		{
			table = new Table(AutoFocusTable.getTableDefinition(), new Date().getTime(), getPositionInformation(), executionInformation);
			for(int i=0; i<oracle.focusInfos.size(); i++)
			{
				FocusInfo info = oracle.focusInfos.get(i);
				if(i==0)
				{
					table.addRow(new Integer(i+1),
							new Double(info.relFocusPosition),
							new Double(zeroPosition + info.relFocusPosition),
							new Double(info.focusScore),
							
							new Integer(oracle.maxFocusIdx+1),
							new Double(lastOptimalRelativeFocus),
							new Double(zeroPosition + lastOptimalRelativeFocus),
							new Double(oracle.maxFocusScore)
						);
				}
				else
				{
					table.addRow(new Integer(i+1),
							new Double(info.relFocusPosition),
							new Double(zeroPosition + info.relFocusPosition),
							new Double(info.focusScore),
							
							null,
							null,
							null,
							null
						);
				}
			}
		}
		catch(TableException e)
		{
			throw new JobException("Could not produce table data for listeners.", e);
		}
		
		synchronized(tableDataListeners)
		{
			for(Iterator<TableListener> iterator = tableDataListeners.iterator(); iterator.hasNext(); )
			{
				try
				{
					iterator.next().newTableProduced(table.clone());
				}
				catch(@SuppressWarnings("unused") RemoteException e)
				{
					iterator.remove();
				}
			}
		}
	}
	
	private void setFocus(Microscope microscope, double focusPosition) throws RemoteException, InterruptedException, JobException
	{
		if(Thread.interrupted())
			throw new InterruptedException();
		try
		{
			if(focusDevice == null)
			{
				microscope.getFocusDevice().setFocusPosition(focusPosition);
			}
			else
			{
				microscope.getFocusDevice(focusDevice).setFocusPosition(focusPosition);
			}
		}
		catch(DeviceException e)
		{
			throw new JobException("Could not find focus device during auto focus.", e);
		}
		catch(MicroscopeLockedException e)
		{
			throw new JobException("Microscope locked by other thread during auto focus.", e);
		}
		catch(MicroscopeException e)
		{
			throw new JobException("Focus adjustment during auto focus failed.", e);
		}
		if(Thread.interrupted())
			throw new InterruptedException();
		if(adjustmentTime > 0)
			Thread.sleep(adjustmentTime);
	}
	
	private double getScore(ImageEvent<?> image) throws RemoteException, JobException, InterruptedException
	{
		if(Thread.interrupted())
			throw new InterruptedException();
		try
		{
			return focusScoreAlgorithm.calculateScore(image);
		}
		catch(ResourceException e1)
		{
			throw new JobException("Error occured while calculating focus score for autofocus.", e1);
		}
	}
	
	private ImageEvent<?> takeImage(Microscope microscope, MeasurementContext measurementContext, ExecutionInformation executionInformation, PositionInformation positionInformation) throws RemoteException, JobException, InterruptedException
	{
		if(Thread.interrupted())
			throw new InterruptedException();
		ImageEvent<?> image;
		try
		{
			//image = microscope.getCameraDevice().makeImage(configGroup, channel, exposure);
			image = microscope.getCameraDevice().makeImage(null, null, exposure);
		}
		catch(DeviceException e)
		{
			throw new JobException("Could not find camera during auto focus.", e);
		}
		catch(MicroscopeException e)
		{
			throw new JobException("Taking image failed during auto focus.", e);
		}
		catch(MicroscopeLockedException e)
		{
			throw new JobException("Microscope locked by other thread during auto focus.", e);
		}
		catch(SettingException e)
		{
			throw new JobException("Image settings invalid during auto focus.", e);
		}
		image.setPositionInformation(positionInformation);
		image.setExecutionInformation(executionInformation);
		image.setCreationRuntime(measurementContext.getMeasurementRuntime());
		
		sendImageToListeners(image);
		return image;
	}

	@Override
	protected String getDefaultName()
	{
		String text = "Auto-Focus search in channel " + configGroup + "." + channel;
		text += ", exposure " + Double.toString(exposure) + "ms";
	
		return text;
	}

	private void sendImageToListeners(final ImageEvent<?> e)
	{
		synchronized(imageListeners)
		{
			for(int i = 0; i < imageListeners.size(); i++)
			{
				ImageListener listener = imageListeners.get(i);
				try
				{
					listener.imageMade(e);
				}
				catch(@SuppressWarnings("unused") RemoteException e1)
				{
					// Connection probably broken down...
					imageListeners.remove(i);
					i--;
				}
			}
		}	
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
	public String getFocusDevice()
	{
		return focusDevice;
	}

	@Override
	public void setFocusDevice(String focusDevice) throws ComponentRunningException
	{
		assertRunning();
		this.focusDevice = focusDevice;
	}

	@Override
	public int getFocusAdjustmentTime()
	{
		return adjustmentTime;
	}

	@Override
	public void setFocusAdjustmentTime(int adjustmentTime) throws ComponentRunningException
	{
		assertRunning();
		this.adjustmentTime = adjustmentTime;
	}

	@Override
	public void setFocusScoreAlgorithm(FocusScoreResource focusScoreAlgorithm) throws RemoteException, ComponentRunningException
	{
		assertRunning();
		this.focusScoreAlgorithm = focusScoreAlgorithm;
	}

	@Override
	public FocusScoreResource getFocusScoreAlgorithm() throws RemoteException
	{
		return focusScoreAlgorithm;
	}

	@Override
	public void setResetFocusAfterSearch(boolean resetFocusAfterSearch) throws RemoteException, ComponentRunningException
	{
		assertRunning();
		this.resetFocusAfterSearch = resetFocusAfterSearch;
	}

	@Override
	public boolean isResetFocusAfterSearch() throws RemoteException
	{
		return resetFocusAfterSearch;
	}

	@Override
	public void addTableListener(TableListener listener)
	{
		if(listener == null)
			return;
		synchronized(tableDataListeners)
		{
			tableDataListeners.add(listener);
		}
	}

	@Override
	public void removeTableListener(TableListener listener)
	{
		if(listener == null)
			return;
		synchronized(tableDataListeners)
		{
			tableDataListeners.remove(listener);
		}
	}

	@Override
	public TableDefinition getProducedTableDefinition()
	{
		return AutoFocusTable.getTableDefinition();
	}


	@Override
	public void setRememberFocus(boolean rememberFocus) throws ComponentRunningException
	{
		assertRunning();
		this.rememberFocus = rememberFocus;
	}

	@Override
	public boolean isRememberFocus()
	{
		return rememberFocus;
	}

	@Override
	public int getNumberOfImages()
	{
		return -1;
	}

	@Override
	public void setFocusSearchAlgorithm(FocusSearchResource focusSearchAlgorithm)
			throws ComponentRunningException
	{
		assertRunning();
		this.focusSearchAlgorithm = focusSearchAlgorithm;
	}

	@Override
	public FocusSearchResource getFocusSearchAlgorithm()
	{
		return focusSearchAlgorithm;
	}

	@Override
	public void insertJob(Job job, int jobIndex)
			throws RemoteException, ComponentRunningException, IndexOutOfBoundsException {
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
