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
package org.youscope.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;
import org.youscope.addon.ConfigurationManagement;
import org.youscope.common.ComponentRunningException;
import org.youscope.common.MeasurementContext;
import org.youscope.common.MessageListener;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.image.ImageEvent;
import org.youscope.common.image.ImageListener;
import org.youscope.common.measurement.MeasurementConfiguration;
import org.youscope.common.measurement.MeasurementListener;
import org.youscope.common.measurement.MeasurementState;
import org.youscope.common.microscope.Microscope;
import org.youscope.common.resource.ResourceException;
import org.youscope.common.saving.MeasurementFileLocations;
import org.youscope.common.saving.MeasurementSaver;
import org.youscope.common.saving.SaveInformation;
import org.youscope.common.saving.SaveSettings;
import org.youscope.common.table.TableListener;
import org.youscope.common.util.RMIWriter;

/**
 * Class which saves information of a measurement together with the images made to the file system.
 * @author Moritz Lang
 */
class MeasurementSaverImpl extends UnicastRemoteObject implements MeasurementSaver
{
	/**
	 * Serial Version UID.
	 */
	private static final long								serialVersionUID		= 7096169973018880421L;
	private volatile boolean								measurementRunning		= false;
	private SaveSettings							saveSettings			= null;
	private final MeasurementImpl							measurement;
	private final Microscope								microscope;

	private final TableDataSaver							imageListSaver;

	private final ArrayList<TableDataSaver>			tableDataSavers			= new ArrayList<TableDataSaver>();

	private PrintStream										logStream				= null;

	private PrintStream										errStream				= null;
	
	private volatile MeasurementFileLocations lastMeasurementInformation = null;
	private volatile InformationSaver informationSaver = null;

	private final MessageListener					logListener				= new MessageListener()
	{

		@Override
		public synchronized void sendMessage(
				String message)
						throws RemoteException {
			if(logStream == null)
				return;
			String NL = System.getProperty("line.separator");
			logStream.println(new Date().toString() + ": " + message.replace("\n", NL + "\t"));
		}

		@Override
		public synchronized void sendErrorMessage(
				String message,
				Throwable exception)
						throws RemoteException {
			sendMessage("Error occured: "+message+"\nFor more information, see error log.");
			if(errStream == null)
				return;
			
			String NL = System.getProperty("line.separator");
			NL = (NL == null ? "\n" : NL);
			String errorMessage = "************************************************************************" + NL + "Error occured." + NL + "Time: " + new Date().toString() + NL + "Message: " + message.replace("\n", NL + "\t\t") + NL;
			if(exception != null)
			{
				errorMessage += "Cause: ";
				for(Throwable throwable = exception; throwable != null; throwable = throwable.getCause())
				{
					if(throwable.getMessage() != null)
						errorMessage += "\t" + throwable.getClass().getName() + ": " + throwable.getMessage().replace("\n", NL + "\t\t") + NL;
				}
				errorMessage += "Stack: " + NL;
				for(Throwable throwable = exception; throwable != null; throwable = throwable.getCause())
				{
					errorMessage += "\t" + throwable.getClass().getName() + ": " + NL;
					StackTraceElement[] stacks = throwable.getStackTrace();
					for(StackTraceElement stack : stacks)
					{
						errorMessage += "\t\t" + stack.toString() + NL;
					}
				}
			}
			errorMessage += NL;

			// Print message in file
			errStream.println(errorMessage);
		}
	};

	public static class SaverInformation
	{
		private volatile SaveSettings saveSettings = null;
		private volatile String baseFolder = null;
		private volatile SaveInformation saveInformation = null; 
		public void initialize(SaveSettings saveSettings, String measurementName, MeasurementContext measurementContext) throws ResourceException, RemoteException
		{
			this.saveSettings = saveSettings;
			//TODO: pass real measurement start time, not current time. Pass pause duration.
			this.saveInformation = new SaveInformation(measurementName, System.currentTimeMillis());
			if(saveSettings.isInitialized())
				saveSettings.uninitialize(measurementContext);
			saveSettings.initialize(measurementContext);
			baseFolder = saveSettings.getMeasurementBasePath(saveInformation);
			File folder = new File(baseFolder);
			if(!folder.exists())
				folder.mkdirs();
			
		}
		
		String getMeasurementFolder()
		{
			return baseFolder;
		}
		
		boolean isReady()
		{
			return saveSettings != null && baseFolder != null && saveInformation != null;
		}

		public String getFullImagePath(ImageEvent<?> event, String imageName) throws ResourceException, RemoteException 
		{
			if(!isReady())
				return null;
			return baseFolder + File.separator + saveSettings.getImageFilePath(saveInformation, event, imageName);
		}
		
		public String getRelativeImagePath(ImageEvent<?> event, String imageName) throws ResourceException, RemoteException 
		{
			Path pathAbsolute = Paths.get(getFullImagePath(event, imageName));
	        Path pathBase = Paths.get(getFullImageTablePath()).getParent();
	        if(pathBase == null)
	        	return pathAbsolute.toString();
	        return pathBase.relativize(pathAbsolute).toString();

		}

		public String getImageFileType(ImageEvent<?> event, String imageName) throws ResourceException, RemoteException {
			if(!isReady())
				return null;
			return saveSettings.getImageExtension(saveInformation, event, imageName);
		}
		public String getFullTablePath(String tableName) throws ResourceException, RemoteException
		{
			if(!isReady())
				return null;
			return baseFolder + File.separator + saveSettings.getTableFilePath(saveInformation, tableName);
		}
		public String getFullImageTablePath() throws ResourceException, RemoteException
		{
			if(!isReady())
				return null;
			return baseFolder + File.separator + saveSettings.getImageMetadataTableFilePath(saveInformation);
		}
		
		public String getFullMeasurementConfigurationFilePath() throws ResourceException, RemoteException
		{
			if(!isReady())
				return null;
			return baseFolder + File.separator + saveSettings.getMeasurementConfigurationFilePath(saveInformation);
		}
		
		public String getFullLogErrFilePath() throws ResourceException, RemoteException
		{
			if(!isReady())
				return null;
			return baseFolder + File.separator + saveSettings.getLogErrFilePath(saveInformation);
		}
		
		public String getFullLogOutFilePath() throws ResourceException, RemoteException
		{
			if(!isReady())
				return null;
			return baseFolder + File.separator + saveSettings.getLogOutFilePath(saveInformation);
		}
		public String getFullMicroscopeConfigurationFilePath() throws ResourceException, RemoteException
		{
			if(!isReady())
				return null;
			return baseFolder + File.separator + saveSettings.getMicroscopeConfigurationFilePath(saveInformation);
		}
		public String getFullXMLInformationFilePath() throws ResourceException, RemoteException
		{
			if(!isReady())
				return null;
			return baseFolder + File.separator + saveSettings.getXMLInformationFilePath(saveInformation);
		}
		public String getFullHTMLInformationFilePath() throws ResourceException, RemoteException
		{
			if(!isReady())
				return null;
			return baseFolder + File.separator + saveSettings.getHTMLInformationFilePath(saveInformation);
		}
	}
	private final SaverInformation saverInformation;
																					
	/**
	 * Constructor.
	 * @param measurement Measurement which should be saved to the file system.
	 * @throws RemoteException
	 */
	MeasurementSaverImpl(MeasurementImpl measurement) throws RemoteException
	{
		this.measurement = measurement;
		this.microscope = YouScopeServerImpl.getMainProgram().getMicroscope();
		measurement.addMeasurementListener(getMeasurementListener());
		saverInformation = new SaverInformation();
		imageListSaver = new TableDataSaver("images", saverInformation, true);
		tableDataSavers.add(imageListSaver);
	}

	/**
	 * Returns the listener which gets added to the observed measurement for the saver to know when
	 * it starts and stops.
	 * @return Listener to be added to the observed measurement.
	 * @throws RemoteException
	 */
	private MeasurementListener getMeasurementListener() throws RemoteException
	{
		class MeasurementSaveListener extends UnicastRemoteObject implements MeasurementListener
		{
			/**
			 * Serial Version UID.
			 */
			private static final long	serialVersionUID	= 6658831597399735839L;

			MeasurementSaveListener() throws RemoteException
			{
				super();
			}

			@Override
			public void measurementStructureModified() throws RemoteException {
				// do nothing.
				
			}

			@Override
			public void measurementStateChanged(MeasurementState oldState, MeasurementState newState)
					throws RemoteException 
			{
				if(newState == MeasurementState.ERROR
						|| newState == MeasurementState.READY
						|| newState == MeasurementState.UNINITIALIZED)
					setMeasurementRunning(false);
				else
					setMeasurementRunning(true);
				
			}

			@Override
			public void measurementError(Exception e) throws RemoteException {
				// do nothing, notification will be sent again by state change to MeasurementState.ERROR.
				
			}
		}
		return new MeasurementSaveListener();
	}

	@Override
	public synchronized ImageListener getSaveImageListener(String imageSaveName) throws RemoteException
	{
		ImageDataSaver imageSaver = new ImageDataSaver(imageSaveName, saverInformation);
		imageSaver.addTableListener(imageListSaver);
		return imageSaver;
	}

	@Override
	public synchronized TableListener getSaveTableListener(String tableSaveName) throws RemoteException
	{
		// First check if somebody is already writing to the same file...
		for(TableDataSaver tableDataSaver : tableDataSavers)
		{
			if(tableDataSaver.getTableSaveName().compareToIgnoreCase(tableSaveName) == 0)
				return tableDataSaver;
		}

		// Construct a new saver
		TableDataSaver tableDataSaver = new TableDataSaver(tableSaveName, saverInformation);
		if(measurementRunning)
			tableDataSaver.startWriting();
		tableDataSavers.add(tableDataSaver);
		return tableDataSaver;
	}

	private synchronized void startSavingMeasurement()
	{
		if(saveSettings == null)
		{
			ServerSystem.out.println("No save settings defined for measurement "+measurement.getName()+". Data will not be saved.");
			return;
		}

		MeasurementContext context = measurement.getMeasurementContext();
		try {
			saverInformation.initialize(saveSettings,  measurement.getName(), context);
		} catch (Exception e1) {
			ServerSystem.err.println("Could not initialize save settings. This might lead to follow up errors.", e1);
		} 
		if(!saverInformation.isReady())
		{
			ServerSystem.err.println("Specified save settings for measurement "+measurement.getName()+" are invalid or incomplete. Data will not be saved.", null);
			return;
		}
		
		String measurementBaseFolder = null;
		String xmlInformationFilePath = null;
		String htmlInformationFilePath = null;
		String measurementConfigurationFilePath = null;
		String microscopeConfigurationFilePath = null;
		String logOutFilePath = null;
		String logErrFilePath = null;
		String imageTablePath = null;
		
		try {
			measurementBaseFolder = saverInformation.getMeasurementFolder();
		}
		catch (Exception e1) {
			ServerSystem.err.println("Could not get information where the measurement base folder is.", e1);
		} 
		try {
			imageTablePath = saverInformation.getFullImageTablePath();
		}
		catch (Exception e1) {
			ServerSystem.err.println("Could not get information where the image table should be stored.", e1);
		}
		try {
			xmlInformationFilePath = saverInformation.getFullXMLInformationFilePath();
		} catch (Exception e1) {
			ServerSystem.err.println("Could not get information where to store measurement metadata, channels, and initial scope settings as XML. Not storing information.", e1);
		}
		try {
			htmlInformationFilePath = saverInformation.getFullHTMLInformationFilePath();
		} catch (Exception e1) {
			ServerSystem.err.println("Could not get information where to store measurement metadata, channels, and initial scope settings as HTML. Not storing information.", e1);
		}
		try {
			 measurementConfigurationFilePath = saverInformation.getFullMeasurementConfigurationFilePath();
		} catch (Exception e1) {
			ServerSystem.err.println("Could not get information where to store measurement configuration. Not storing measurement configuration.", e1);
		} 
	
		try {
			microscopeConfigurationFilePath = saverInformation.getFullMicroscopeConfigurationFilePath();
		} catch (Exception e1) {
			ServerSystem.err.println("Could not get information where to store microscope configuration. Not storing microscope configuration.", e1);
		} 
		try {
			logOutFilePath = saverInformation.getFullLogOutFilePath();
		} catch (Exception e1) {
			ServerSystem.err.println("Could not get information where to store output log. Not storing output log.", e1);
		} 
		try {
			logErrFilePath = saverInformation.getFullLogErrFilePath();
		} catch (Exception e1) {
			ServerSystem.err.println("Could not get information where to store error log. Not storing error log.", e1);
		} 
		
		lastMeasurementInformation = new MeasurementFileLocations(measurementBaseFolder, imageTablePath, measurementConfigurationFilePath, logErrFilePath, logOutFilePath, microscopeConfigurationFilePath, xmlInformationFilePath, htmlInformationFilePath);
		
		// Save measurement configuration as XML file.
		if(xmlInformationFilePath != null)
		{
			informationSaver = new InformationSaver(xmlInformationFilePath, htmlInformationFilePath, measurement, microscope, saverInformation);
			try {
				informationSaver.saveXMLInformation();
			} catch (IOException e) {
				ServerSystem.err.println("Could not save information about measurement to "+xmlInformationFilePath+".", e);
				informationSaver = null;
			}
			
			try {
				informationSaver.saveHTMLInformation();
			} catch (IOException | SAXException | TransformerException | ParserConfigurationException e) {
				ServerSystem.err.println("Could not save information about measurement to "+htmlInformationFilePath+".", e);
				informationSaver = null;
			}
		
		}

		// Save measurement configuration as loadable file.
		if(measurementConfigurationFilePath != null)
		{
			MeasurementConfiguration configuration;
			try {
				configuration = measurement.getMetadata().getConfiguration();
			} catch (ConfigurationException e) {
				configuration = null;
				ServerSystem.err.println("Cannot get measurement configuration to save it to "+measurementConfigurationFilePath+".", e);
			}
			if(configuration != null)
				saveMeasurement(configuration, measurementConfigurationFilePath);
		}

		// Save current microscope configuration
		if(microscopeConfigurationFilePath != null)
		{
			RMIWriter rmiWriter = null;
			try
			{
				File folder = new File(microscopeConfigurationFilePath).getParentFile();
				if(!folder.exists())
					folder.mkdirs();
				rmiWriter = new RMIWriter(new FileWriter(microscopeConfigurationFilePath));
				microscope.saveConfiguration(rmiWriter);
			}
			catch(Exception e)
			{
				ServerSystem.err.println("Could not save current microscope configuration to measurement folder. Skipping and continuing.", e);
			}
			finally
			{
				if(rmiWriter!= null)
				{
					try {
						rmiWriter.close();
					} catch (IOException e) {
						ServerSystem.err.println("Could not close RMI writer.", e);
					}
				}
			}
		}

		// Start all table data savers
		for(TableDataSaver tableDataSaver : tableDataSavers)
		{
			tableDataSaver.startWriting();
		}

		// Start log and error streams
		if(logOutFilePath != null)
		{
			try
			{
				File folder = new File(logOutFilePath).getParentFile();
				if(!folder.exists())
					folder.mkdirs();
				synchronized(logListener)
				{
					logStream = new PrintStream(new FileOutputStream(logOutFilePath, true), true);
				}
			}
			catch(FileNotFoundException e)
			{
				ServerSystem.err.println("Could not establish measurement log stream. No log data will be collected!", e);
			} catch (Exception e) {
				ServerSystem.err.println("Could not determine where to store measurement log. No log data will be collected!", e);
			}
		}
		if(logErrFilePath != null)
		{
			try
			{
				String filePath = saverInformation.getFullLogErrFilePath();
				File folder = new File(filePath).getParentFile();
				if(!folder.exists())
					folder.mkdirs();
				synchronized(logListener)
				{
					errStream = new PrintStream(filePath);
				}
			}
			catch(FileNotFoundException e)
			{
				ServerSystem.err.println("Could not establish measurement error stream. No error data will be collected!", e);
			}
			catch (Exception e) {
				ServerSystem.err.println("Could not determine where to store measurement error log. No log data will be collected!", e);
			}
		}

		// register log and error listeners
		ServerSystem.addMessageOutListener(logListener);
		ServerSystem.addMessageErrListener(logListener);

	}

	private synchronized void stopSavingMeasurement()
	{
		if(saveSettings == null)
			return;

		// stop saving of new images
		ServerSystem.out.println("Waiting for all images of measurement to be saved...");
		try
		{
			FileSaverManager.waitForExecutions();
			ServerSystem.out.println("All images saved.");
		}
		catch(InterruptedException e)
		{
			ServerSystem.err.println("Waiting for image saving interrupted. Continuing uninitialization of measurement.", e);
		}

		// Stop all table data savers
		for(TableDataSaver tableDataSaver : tableDataSavers)
		{
			tableDataSaver.endWriting();
		}

		// Stop log and error streams
		synchronized(logListener)
		{
			if(logStream != null)
			{
				logStream.close();
				logStream = null;
			}
			if(errStream != null)
			{
				errStream.close();
				errStream = null;
			}
		}
		// unregister log and error listeners
		ServerSystem.removeMessageOutListener(logListener);
		ServerSystem.removeMessageErrListener(logListener);
		
		// Update information about measurement.
		if(informationSaver != null)
		{
			try {
				informationSaver.saveXMLInformation();
			} catch (IOException e) {
				ServerSystem.err.println("Could not update information about measurement to XML.", e);
				informationSaver = null;
			}
			try {
				informationSaver.saveHTMLInformation();
			} catch (IOException | SAXException | TransformerException | ParserConfigurationException e) {
				ServerSystem.err.println("Could not update information about measurement to HTML.", e);
				informationSaver = null;
			}
		
		
			informationSaver = null;
		}

		// cleanup ram.
		System.gc();
	}
	
	private static void saveMeasurement(MeasurementConfiguration measurement, String fileName)
	{
		try
		{
			ConfigurationManagement.saveConfiguration(fileName, measurement);
		}
		catch(FileNotFoundException e)
		{
			ServerSystem.err.println("Could not save microscope configuration file.", e);
			return;
		}
		catch(IOException e)
		{
			ServerSystem.err.println("Could not save microscope configuration file.", e);
			return;
		}
	}

	private synchronized void setMeasurementRunning(boolean measurementRunning)
	{
		if(this.measurementRunning == measurementRunning)
			return;
		this.measurementRunning = measurementRunning;
		if(measurementRunning)
		{
			startSavingMeasurement();
		}
		else
		{
			stopSavingMeasurement();
		}
	}

	@Override
	public void setSaveSettings(SaveSettings saveSettings) throws ComponentRunningException
	{
		if(measurementRunning)
			throw new ComponentRunningException();
		this.saveSettings = saveSettings;
	}

	@Override
	public SaveSettings getSaveSettings()
	{
		return saveSettings;
	}

	@Override
	public MeasurementFileLocations getLastMeasurementFileLocations() throws RemoteException {
		return lastMeasurementInformation;
	}
}
