/**
 * 
 */
package org.youscope.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Date;

import org.youscope.addon.ConfigurationManagement;
import org.youscope.common.MessageListener;
import org.youscope.common.image.ImageEvent;
import org.youscope.common.image.ImageListener;
import org.youscope.common.measurement.MeasurementConfiguration;
import org.youscope.common.measurement.MeasurementListener;
import org.youscope.common.measurement.MeasurementRunningException;
import org.youscope.common.microscope.Microscope;
import org.youscope.common.saving.MeasurementSaver;
import org.youscope.common.saving.SaveSettings;
import org.youscope.common.table.TableListener;
import org.youscope.common.util.RMIWriter;

/**
 * Class which saves information of a measurement together with the images made to the file system.
 * @author langmo
 */
class MeasurementSaverImpl extends UnicastRemoteObject implements MeasurementSaver
{
	/**
	 * Serial Version UID.
	 */
	private static final long								serialVersionUID		= 7096169973018880421L;
	private MeasurementConfiguration						configuration			= null;
	private volatile boolean								measurementRunning		= false;
	private SaveSettings							saveSettings			= null;
	private final MeasurementImpl							measurement;
	private final Microscope								microscope;

	private final TableDataSaver							imageListSaver;

	private final ArrayList<TableDataSaver>			tableDataSavers			= new ArrayList<TableDataSaver>();

	private PrintStream										logStream				= null;

	private PrintStream										errStream				= null;

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
		public void initialize(SaveSettings saveSettings, String measurementName)
		{
			this.saveSettings = saveSettings;
			baseFolder = saveSettings.getMeasurementBasePath(measurementName, System.currentTimeMillis());
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
			return saveSettings != null && baseFolder != null;
		}

		public String getFullImagePath(ImageEvent<?> event, String imageName) 
		{
			if(!isReady())
				return null;
			return baseFolder + File.separator + saveSettings.getImageFilePath(event, imageName);
		}
		
		public String getImagePath(ImageEvent<?> event, String imageName) 
		{
			if(!isReady())
				return null;
			return saveSettings.getImageFilePath(event, imageName);
		}

		public String getImageFileType(ImageEvent<?> event, String imageName) {
			if(!isReady())
				return null;
			return saveSettings.getImageExtension(event, imageName);
		}
		public String getFullTablePath(String tableName)
		{
			if(!isReady())
				return null;
			return baseFolder + File.separator + saveSettings.getTableFilePath(tableName);
		}
		public String getFullImageTablePath()
		{
			if(!isReady())
				return null;
			return baseFolder + File.separator + saveSettings.getImageMetadataTableFilePath();
		}
		
		public String getFullMeasurementConfigurationFilePath()
		{
			if(!isReady())
				return null;
			return baseFolder + File.separator + saveSettings.getMeasurementConfigurationFilePath();
		}
		
		public String getFullLogErrFilePath()
		{
			if(!isReady())
				return null;
			return baseFolder + File.separator + saveSettings.getLogErrFilePath();
		}
		
		public String getFullLogOutFilePath()
		{
			if(!isReady())
				return null;
			return baseFolder + File.separator + saveSettings.getLogOutFilePath();
		}
		public String getFullMicroscopeConfigurationFilePath()
		{
			if(!isReady())
				return null;
			return baseFolder + File.separator + saveSettings.getMicroscopeConfigurationFilePath();
		}
		public String getFullScopeSettingsFilePath()
		{
			if(!isReady())
				return null;
			return baseFolder + File.separator + saveSettings.getScopeSettingsFilePath();
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
			public void measurementStarted() throws RemoteException
			{
				// Do nothing.
			}

			@Override
			public void measurementFinished() throws RemoteException
			{
				setMeasurementRunning(false);
			}

			@Override
			public void measurementQueued() throws RemoteException
			{
				// Do nothing.
			}

			@Override
			public void errorOccured(Exception e) throws RemoteException
			{
				// Do nothing.
			}

			@Override
			public void measurementUnqueued() throws RemoteException
			{
				// Do nothing.
			}

			@Override
			public void measurementInitializing() throws RemoteException
			{
				setMeasurementRunning(true);
			}

			@Override
			public void measurementUninitializing() throws RemoteException
			{
				// do nothing.
			}

			@Override
			public void measurementStructureModified() throws RemoteException {
				// do nothing.
				
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

		saverInformation.initialize(saveSettings,  measurement.getName());
		if(!saverInformation.isReady())
		{
			ServerSystem.err.println("Specified save settings for measurement "+measurement.getName()+" are invalid or incomplete. Data will not be saved.", null);
			return;
		}

		if(configuration != null)
		{
			// Save measurement configuration as XML file.
			XMLMeasurementDescription.saveDescription(configuration, microscope, saverInformation.getFullScopeSettingsFilePath());

			// Save measurement configuration as loadable file.
			saveMeasurement(configuration, saverInformation.getFullMeasurementConfigurationFilePath());
		}

		// Save current microscope configuration
		RMIWriter rmiWriter = null;
		try
		{
			rmiWriter = new RMIWriter(new FileWriter(saverInformation.getFullMicroscopeConfigurationFilePath()));
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

		// Start all table data savers
		for(TableDataSaver tableDataSaver : tableDataSavers)
		{
			tableDataSaver.startWriting();
		}

		// Start log and error streams
		try
		{
			synchronized(logListener)
			{
				logStream = new PrintStream(saverInformation.getFullLogOutFilePath());
			}
		}
		catch(FileNotFoundException e)
		{
			ServerSystem.err.println("Could not establish measurement log stream. No log data will be collected!", e);
		}
		try
		{
			synchronized(logListener)
			{
				errStream = new PrintStream(saverInformation.getFullLogErrFilePath());
			}
		}
		catch(FileNotFoundException e)
		{
			ServerSystem.err.println("Could not establish measurement error stream. No error data will be collected!", e);
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
	public void setSaveSettings(SaveSettings saveSettings) throws MeasurementRunningException
	{
		if(measurementRunning)
			throw new MeasurementRunningException();
		this.saveSettings = saveSettings;
	}

	@Override
	public SaveSettings getSaveSettings()
	{
		return saveSettings;
	}

	@Override
	public MeasurementConfiguration getConfiguration()
	{
		return configuration;
	}

	@Override
	public void setConfiguration(MeasurementConfiguration configuration) throws MeasurementRunningException
	{
		if(measurementRunning)
			throw new MeasurementRunningException();
		this.configuration = configuration;
	}

	@Override
	public String getLastMeasurementFolder() throws RemoteException
	{
		return saverInformation.getMeasurementFolder();
	}
}
