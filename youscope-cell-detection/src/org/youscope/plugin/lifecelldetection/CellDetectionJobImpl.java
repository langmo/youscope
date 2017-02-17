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
package org.youscope.plugin.lifecelldetection;

import java.rmi.RemoteException;
import java.util.ArrayList;

import org.youscope.addon.celldetection.CellDetectionAddon;
import org.youscope.addon.celldetection.CellDetectionException;
import org.youscope.addon.celldetection.CellDetectionResult;
import org.youscope.addon.celldetection.CellVisualizationAddon;
import org.youscope.addon.celldetection.CellVisualizationException;
import org.youscope.common.ComponentRunningException;
import org.youscope.common.ExecutionInformation;
import org.youscope.common.MeasurementContext;
import org.youscope.common.PositionInformation;
import org.youscope.common.image.ImageAdapter;
import org.youscope.common.image.ImageEvent;
import org.youscope.common.image.ImageListener;
import org.youscope.common.image.ImageProducer;
import org.youscope.common.job.Job;
import org.youscope.common.job.JobAdapter;
import org.youscope.common.job.JobException;
import org.youscope.common.microscope.Microscope;
import org.youscope.common.resource.ResourceException;
import org.youscope.common.table.Table;
import org.youscope.common.table.TableDefinition;
import org.youscope.common.table.TableListener;

/**
 * This job detects cells in the image passed to it. It passes information about the detected cells to its table listeners. If configured respectively,
 * it also produces an image in which the detected cells are highlighted to its image listeners.
 * @author Moritz Lang
 */
class CellDetectionJobImpl extends JobAdapter implements CellDetectionJob
{
	/**
	 * Serial Version UID.
	 */
	private static final long				serialVersionUID	= -3768352144003480453L;
	private final ArrayList<ImageListener>	imageListeners		= new ArrayList<ImageListener>();
	private final ArrayList<ImageListener>	segmentationImageListeners		= new ArrayList<ImageListener>();
	private final ArrayList<ImageListener>	controlImageListeners		= new ArrayList<ImageListener>();
	
	private final ArrayList<TableListener>	tableDataListeners		= new ArrayList<TableListener>();
	
	private CellDetectionAddon detectionAlgorithm = null;
	
	private CellVisualizationAddon visualizationAlgorithm = null;
	
	private final ArrayList<Job>	jobs				= new ArrayList<Job>();
	private ImageAdapter[] imageAdapters = null;
	
	private volatile String imageDescription = null;
	
	private volatile long minimalTimeMS = -1;
	
	public CellDetectionJobImpl(PositionInformation positionInformation) throws RemoteException
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
		String retVal = "Cell-Detection-Image";
		return retVal;
	}

	@Override
	public void initializeJob(Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		super.initializeJob(microscope, measurementContext);
		
		// Initialize Detection algorithm
		if(detectionAlgorithm == null)
		{
			throw new JobException("No cell detection algorithm set.");
		}
		
		try
		{
			detectionAlgorithm.initialize(measurementContext);
		}
		catch(ResourceException e)
		{
			throw new JobException("Could not initialize cell detection algorithm.", e);
		}
		
		if(visualizationAlgorithm != null)
		{
			try
			{
				visualizationAlgorithm.initialize(measurementContext);
			}
			catch(ResourceException e)
			{
				throw new JobException("Could not initialize cell visualization algorithm.", e);
			}
		}
		
		// Initialize child jobs
		synchronized(jobs)
		{
			imageAdapters = new ImageAdapter[jobs.size()];
			for(int i=0; i<jobs.size(); i++)
			{
				imageAdapters[i] = new ImageAdapter();
				((ImageProducer)jobs.get(i)).addImageListener(imageAdapters[i]);
				jobs.get(i).initializeJob(microscope, measurementContext);
			}
		}
	}

	@Override
	public void uninitializeJob(Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		// Uninitialize child jobs
		synchronized(jobs)
		{
			for(int i=0; i< jobs.size(); i++)
			{
				jobs.get(i).uninitializeJob(microscope, measurementContext);
				if(imageAdapters != null && i < imageAdapters.length && imageAdapters[i] != null)
				{
					((ImageProducer)jobs.get(i)).removeImageListener(imageAdapters[i]);
				}
			}
		}
		imageAdapters = null;
		
		// uninitialize detection algorithm.
		if(detectionAlgorithm != null)
		{
			try
			{
				detectionAlgorithm.uninitialize(measurementContext);
			}
			catch(ResourceException e)
			{
				this.sendErrorMessage("Could not uninitialize detection algorithm.", e);
			}
		}
		
		// uninitialize detection algorithm.
		if(visualizationAlgorithm != null)
		{
			try
			{
				visualizationAlgorithm.uninitialize(measurementContext);
			}
			catch(ResourceException e)
			{
				this.sendErrorMessage("Could not uninitialize visualization algorithm.", e);
			}
		}
		
		super.uninitializeJob(microscope, measurementContext);
	}

	@Override
	public void runJob(ExecutionInformation executionInformation, Microscope microscope, MeasurementContext measurementContext) throws JobException, RemoteException, InterruptedException
	{
		long startJobTime = System.currentTimeMillis();
		// run child jobs (image producers)
		synchronized(jobs)
		{
			for(int i = 0; i < jobs.size(); i++)
			{
				jobs.get(i).executeJob(executionInformation, microscope, measurementContext);
				if(Thread.interrupted())
					throw new InterruptedException();
			}
		}
		
		if(imageAdapters == null || imageAdapters.length <= 0)
			return;
		ImageEvent<?> detectionImage = imageAdapters[0].clearImage();
		
		if(detectionImage == null)
			return;
		ImageEvent<?>[] quantificationImages = new ImageEvent<?>[imageAdapters.length-1];
		for(int i=0; i<imageAdapters.length-1; i++)
		{
			quantificationImages[i] = imageAdapters[i+1].clearImage();
		}
		
		CellDetectionResult result;
		try
		{
			result = detectionAlgorithm.detectCells(detectionImage, quantificationImages);
		}
		catch(CellDetectionException e)
		{
			throw new JobException("Error occured while detecting cells.", e);
		}
		
		if(result != null && result.getLabelImage() != null)
		{
			ImageEvent<?> image = result.getLabelImage();
			image.setPositionInformation(getPositionInformation());
			image.setExecutionInformation(executionInformation);
			image.setCreationRuntime(measurementContext.getMeasurementRuntime());
			sendSegmentationImageToListeners(image);
			if(visualizationAlgorithm == null)
			{
				ImageEvent<?> e = result.getLabelImage();
				e.setPositionInformation(getPositionInformation());
				e.setExecutionInformation(executionInformation);
				e.setCreationRuntime(measurementContext.getMeasurementRuntime());
				sendImageToListeners(e);
			}
		}
		
		
		if(result!= null && result.getLabelImage() != null && visualizationAlgorithm != null)
		{
			ImageEvent<?> visImage;
			try
			{
				visImage = visualizationAlgorithm.visualizeCells(detectionImage, result);
			}
			catch(CellVisualizationException e)
			{
				throw new JobException("Error occured while visualizing cells.", e);
			}
			if(visImage != null)
			{
				visImage.setPositionInformation(getPositionInformation());
				visImage.setExecutionInformation(executionInformation);
				visImage.setCreationRuntime(measurementContext.getMeasurementRuntime());
				sendControlImageToListeners(visImage);
				sendImageToListeners(visImage);
			}
		}
		
		if(result != null)
		{
			// get data
			Table table = result.getCellTable();	
			synchronized(tableDataListeners)
			{
				for(TableListener listener : tableDataListeners)
				{
					listener.newTableProduced(table);
				}
			}
		}
		
		long waitTime = minimalTimeMS - (System.currentTimeMillis() - startJobTime);
		if(waitTime > 0)
		{
			Thread.sleep(waitTime);
		}
	}
	
	
	@Override
	protected String getDefaultName()
	{
		String retVal = "Cell Detection Job";
		return retVal;
	}
	
	private void sendImageToListeners(ImageEvent<?> e)
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
	
	private void sendSegmentationImageToListeners(ImageEvent<?> e)
	{
		synchronized(segmentationImageListeners)
		{
			for(int i = 0; i < segmentationImageListeners.size(); i++)
			{
				ImageListener listener = segmentationImageListeners.get(i);
				try
				{
					listener.imageMade(e);
				}
				catch(@SuppressWarnings("unused") RemoteException e1)
				{
					// Connection probably broken down...
					segmentationImageListeners.remove(i);
					i--;
				}
			}
		}
	}
	
	private void sendControlImageToListeners(ImageEvent<?> e)
	{
		synchronized(controlImageListeners)
		{
			for(int i = 0; i < controlImageListeners.size(); i++)
			{
				ImageListener listener = controlImageListeners.get(i);
				try
				{
					listener.imageMade(e);
				}
				catch(@SuppressWarnings("unused") RemoteException e1)
				{
					// Connection probably broken down...
					controlImageListeners.remove(i);
					i--;
				}
			}
		}
	}
	
	@Override
	public synchronized void addJob(Job job) throws ComponentRunningException
	{
		if(!(job instanceof ImageProducer))
			throw new IllegalArgumentException("All child jobs of a cell detection job must be image producers!");
		
		assertRunning();
		synchronized(jobs)
		{
			jobs.add(job);
		}
	}

	@Override
	public synchronized void removeJob(int jobIndex) throws ComponentRunningException, IndexOutOfBoundsException
	{
		assertRunning();
		synchronized(jobs)
		{
			jobs.remove(jobIndex);
		}
	}

	@Override
	public synchronized void clearJobs() throws ComponentRunningException
	{
		assertRunning();
		synchronized(jobs)
		{
			jobs.clear();
		}
	}

	@Override
	public Job[] getJobs()
	{
		synchronized(jobs)
		{
			return jobs.toArray(new Job[jobs.size()]);
		}
	}

	@Override
	public synchronized void setDetectionAlgorithm(CellDetectionAddon detectionAlgorithm) throws ComponentRunningException
	{
		assertRunning();
		this.detectionAlgorithm = detectionAlgorithm;
	}

	@Override
	public CellDetectionAddon getDetectionAlgorithm()
	{
		return detectionAlgorithm;
	}
	
	@Override
	public void setVisualizationAlgorithm(CellVisualizationAddon visualizationAlgorithm) throws ComponentRunningException
	{
		assertRunning();
		this.visualizationAlgorithm = visualizationAlgorithm;
	}

	@Override
	public CellVisualizationAddon getVisualizationAlgorithm()
	{
		return visualizationAlgorithm;
	}

	@Override
	public int getNumberOfImages()
	{
		return 1;
	}
	
	@Override
	public void setImageDescription(String description) throws ComponentRunningException
	{
		assertRunning();
		imageDescription = description;
	}

	@Override
	public void setMinimalTimeMS(long minimalTimeMS) throws ComponentRunningException
	{
		assertRunning();
		this.minimalTimeMS = minimalTimeMS;
	}

	@Override
	public long getMinimalTimeMS()
	{
		return minimalTimeMS;
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
	public void addSegmentationImageListener(ImageListener listener) 
	{
		if(listener == null)
			return;
		synchronized(segmentationImageListeners)
		{
			segmentationImageListeners.add(listener);
		}
		
	}

	@Override
	public void removeSegmentationImageListener(ImageListener listener) 
	{
		if(listener == null)
			return;
		synchronized(segmentationImageListeners)
		{
			segmentationImageListeners.remove(listener);
		}
	}

	@Override
	public void addControlImageListener(ImageListener listener) 
	{
		if(listener == null)
			return;
		synchronized(controlImageListeners)
		{
			controlImageListeners.add(listener);
		}
		
	}

	@Override
	public void removeControlImageListener(ImageListener listener) 
	{
		if(listener == null)
			return;
		synchronized(controlImageListeners)
		{
			controlImageListeners.remove(listener);
		}
	}

	@Override
	public void removeTableListener(TableListener listener){
		if(listener == null)
			return;
		synchronized(tableDataListeners)
		{
			tableDataListeners.remove(listener);
		}
		
	}

	@Override
	public void addTableListener(TableListener listener) {
		if(listener == null)
			return;
		synchronized(tableDataListeners)
		{
			tableDataListeners.add(listener);
		}
		
	}

	@Override
	public TableDefinition getProducedTableDefinition() throws RemoteException
	{
		if(detectionAlgorithm != null)
			return detectionAlgorithm.getProducedTableDefinition();
		return null;
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
