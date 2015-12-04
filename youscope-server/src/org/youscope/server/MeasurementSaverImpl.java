/**
 * 
 */
package org.youscope.server;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.locks.ReentrantLock;

import javax.imageio.ImageIO;

import org.youscope.common.ImageEvent;
import org.youscope.common.ImageListener;
import org.youscope.common.Well;
import org.youscope.common.YouScopeMessageListener;
import org.youscope.common.configuration.ImageFolderStructure;
import org.youscope.common.configuration.MeasurementConfiguration;
import org.youscope.common.measurement.MeasurementListener;
import org.youscope.common.measurement.MeasurementRunningException;
import org.youscope.common.measurement.MeasurementSaveSettings;
import org.youscope.common.measurement.MeasurementSaver;
import org.youscope.common.microscope.Microscope;
import org.youscope.common.table.ColumnDefinition;
import org.youscope.common.table.RowView;
import org.youscope.common.table.Table;
import org.youscope.common.table.TableDefinition;
import org.youscope.common.table.TableException;
import org.youscope.common.table.TableListener;
import org.youscope.common.table.TableProducer;
import org.youscope.common.tools.ConfigurationManagement;
import org.youscope.common.tools.ImageConvertException;
import org.youscope.common.tools.ImageTools;
import org.youscope.common.tools.RMIWriter;

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
	private String											measurementSaveFolder	= null;
	private volatile boolean								measurementRunning		= false;
	private MeasurementSaveSettings							saveSettings			= null;
	private final MeasurementImpl							measurement;
	private final Microscope								microscope;

	// private static final int IMAGE_SAVER_THREAD_POOL_SIZE = 10;
	private static final LinkedBlockingDeque<BufferedImage>	reusableImages			= new LinkedBlockingDeque<BufferedImage>(FileSaverManager.SAVER_POOL_SIZE);
	// private static final ExecutorService imageSaverPool = Executors.newFixedThreadPool(IMAGE_SAVER_THREAD_POOL_SIZE);

	private final TableListener							imageListSaver;

	private final Vector<MeasurementTableDataSaver>			tableDataSavers			= new Vector<MeasurementTableDataSaver>();

	private static final String								LOG_FILE_NAME			= "measurement_log.txt";

	private static final String								ERR_FILE_NAME			= "measurement_err.txt";

	private PrintStream										logStream				= null;

	private PrintStream										errStream				= null;

	private final YouScopeMessageListener					logListener				= new YouScopeMessageListener()
																					{

																						@Override
																						public synchronized void consumeMessage(String message, Date time) throws RemoteException
																						{
																							if(logStream == null)
																								return;
																							String NL = System.getProperty("line.separator");
																							logStream.println(time.toString() + ": " + message.replace("\n", NL + "\t"));
																						}

																						@Override
																						public synchronized void consumeError(String message, Throwable exception, Date time) throws RemoteException
																						{
																							if(errStream == null)
																								return;

																							String NL = System.getProperty("line.separator");
																							NL = (NL == null ? "\n" : NL);
																							String errorMessage = "************************************************************************" + NL + "Error occured." + NL + "Time: " + time.toString() + NL + "Message: " + message.replace("\n", NL + "\t\t") + NL;
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
		imageListSaver = getSaveTableDataListener("images");
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
		if(measurementRunning)
			return null;

		MeasurementImageSaver imageSaver = new MeasurementImageSaver(imageSaveName, imageListSaver);
		return imageSaver;
	}

	@Override
	public synchronized TableListener getSaveTableDataListener(String tableSaveName) throws RemoteException
	{
		if(measurementRunning)
			return null;

		// First check if somebody is already writing to the same file...
		for(MeasurementTableDataSaver tableDataSaver : tableDataSavers)
		{
			if(tableDataSaver.getTableSaveName().compareToIgnoreCase(tableSaveName) == 0)
				return tableDataSaver;
		}

		// Construct a new saver
		MeasurementTableDataSaver tableDataSaver = new MeasurementTableDataSaver(tableSaveName);
		tableDataSavers.addElement(tableDataSaver);
		return tableDataSaver;
	}

	private synchronized void startSavingMeasurement()
	{
		if(saveSettings == null)
			return;

		// load image formats
		ImageIO.scanForPlugins();

		// Create output folder
		DateFormat dateToFileName = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss-SSS");
		measurementSaveFolder = saveSettings.getFolder() + File.separator + measurement.getName() + File.separator + dateToFileName.format(new Date());
		File folder = new File(measurementSaveFolder);
		if(!folder.exists())
			folder.mkdirs();

		if(configuration != null)
		{
			// Save measurement configuration as XML file.
			XMLMeasurementDescription.saveDescription(configuration, microscope, measurementSaveFolder + File.separator + "configuration.xml");

			// Save measurement configuration as loadable file.
			saveMeasurement(configuration, measurementSaveFolder + File.separator + "configuration.csb");
		}

		// Save current microscope configuration
		RMIWriter rmiWriter = null;
		try
		{
			rmiWriter = new RMIWriter(new FileWriter(measurementSaveFolder + File.separator + "YSConfig_Microscope.cfg"));
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
		for(MeasurementTableDataSaver tableDataSaver : tableDataSavers)
		{
			tableDataSaver.startWriting();
		}

		// Start log and error streams
		try
		{
			synchronized(logListener)
			{
				logStream = new PrintStream(measurementSaveFolder + File.separator + LOG_FILE_NAME);
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
				errStream = new PrintStream(measurementSaveFolder + File.separator + ERR_FILE_NAME);
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
		for(MeasurementTableDataSaver tableDataSaver : tableDataSavers)
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

	private class MeasurementTableDataSaver extends UnicastRemoteObject implements TableListener, Runnable
	{
		/**
		 * Serial Version UID.
		 */
		private static final long				serialVersionUID	= 7112259208478805045L;
		private volatile Writer					tableWriter			= null;
		private final String					tableSaveName;
		private volatile boolean				dataReceived		= false;
		private final String[]					defaultColumns		= {"Evaluation", "Measurement Time (ms)", "Absolute Time (ms)", "Absolute Time String", "Well", "Position"};

		private volatile LinkedList<String[]>	toBeSavedEntries	= null;

		private final ReentrantLock				writeLock			= new ReentrantLock();

		/**
		 * @throws RemoteException
		 */
		private MeasurementTableDataSaver(String tableSaveName) throws RemoteException
		{
			super();
			this.tableSaveName = tableSaveName;
		}

		@Override
		public synchronized void newTableProduced(Table table) throws RemoteException
		{
			if(saveSettings == null)
				return;

			boolean sendToExecutor;
			synchronized(this)
			{
				sendToExecutor = toBeSavedEntries == null;
				if(sendToExecutor)
					toBeSavedEntries = new LinkedList<String[]>();
				if(!dataReceived)
				{
					// append default information (well, time, etc.)
					String[] columnNames = new String[table.getNumColumns() + defaultColumns.length];
					System.arraycopy(defaultColumns, 0, columnNames, 0, defaultColumns.length);
					System.arraycopy(table.getColumnNames(), 0, columnNames, defaultColumns.length, table.getNumColumns());
					toBeSavedEntries.add(columnNames);
					dataReceived = true;
				}
				for(RowView rowView : table)
				{
					// append default information (well, time, etc.)
					String[] saveEntry = new String[rowView.getNumColumns() + defaultColumns.length];
					saveEntry[0] = table.getExecutionInformation() == null ? "" : table.getExecutionInformation().getEvaluationString();
					saveEntry[1] = (table.getExecutionInformation() == null) ? "" : Long.toString(table.getCreationTime() - table.getExecutionInformation().getMeasurementStartTime());
					saveEntry[2] = Long.toString(table.getCreationTime());
					saveEntry[3] = new Date(table.getCreationTime()).toString();
					saveEntry[4] = (table.getPositionInformation() != null && table.getPositionInformation().getWell() != null) ? table.getPositionInformation().getWell().getWellName() : "";
					saveEntry[5] = table.getPositionInformation() != null ? table.getPositionInformation().getPositionsString() : "";
					
					for(int i=0; i< rowView.getNumColumns(); i++)
					{
						String value =  rowView.get(i).getValueAsString();
						saveEntry[6+i] = value == null ? "" : value;
					}
					toBeSavedEntries.add(saveEntry);
				}
			}
			// only queue this for execution if this is the first row arrived since the last execution.
			// otherwise, it is already queued.
			if(sendToExecutor)
				FileSaverManager.execute(this);
		}

		public String getTableSaveName()
		{
			return tableSaveName;
		}

		@Override
		public void run()
		{
			writeLock.lock();
			try
			{
				// Get entries which should be saved, and reset list.
				LinkedList<String[]> tempList;
				synchronized(this)
				{
					tempList = toBeSavedEntries;
					toBeSavedEntries = null;
				}
				if(tempList == null)
					return;

				// prepare CSV text
				StringBuffer writeData = new StringBuffer(tempList.size() * 50);
				for(String[] data : tempList)
				{
					for(int i = 0; i < data.length; i++)
					{
						if(i > 0)
							writeData.append(";");
						if(data[i] != null)
						{
							writeData.append("\"");
							writeData.append(data[i].replaceAll("\"", "\"\"").replaceAll("\n", " "));
							writeData.append("\"");
						}
					}
					writeData.append("\n");
				}

				boolean reopenedFile = false;
				if(tableWriter == null)
				{
					// This is the case when a measurement already finished, but table data is still incoming.
					// This can happen shortly after a measurement finished, since the table data is saved asynchronously.
					// We just open a new file writer to append the data and close it afterwards again...
					if(!restartWriting())
						return;
					reopenedFile = true;
				}

				// write CSV text
				try
				{
					tableWriter.write(writeData.toString());
					tableWriter.flush();
				}
				catch(IOException e1)
				{
					ServerSystem.err.println("Could not append data to table " + getTableSaveName() + ".", e1);
				}
				if(reopenedFile)
				{
					endWriting();
				}
			}
			finally
			{
				writeLock.unlock();
			}
		}

		private boolean restartWriting()
		{
			writeLock.lock();
			try
			{
				try
				{
					tableWriter = new FileWriter(measurementSaveFolder + File.separator + getTableSaveName() + ".csv", true);
					return true;
				}
				catch(IOException e1)
				{
					tableWriter = null;
					ServerSystem.err.println("Could not create file to save table data \"" + getTableSaveName() + ".", e1);
					return false;
				}
			}
			finally
			{
				writeLock.unlock();
			}

		}

		public void startWriting()
		{
			writeLock.lock();
			try
			{
				dataReceived = false;
				try
				{
					if(tableWriter != null)
						tableWriter.close();
					tableWriter = new FileWriter(measurementSaveFolder + File.separator + getTableSaveName() + ".csv", false);
				}
				catch(IOException e1)
				{
					ServerSystem.err.println("Could not create file to save table data \"" + getTableSaveName() + ".", e1);
				}
			}
			finally
			{
				writeLock.unlock();
			}
		}

		public void endWriting()
		{
			writeLock.lock();
			try
			{
				if(tableWriter == null)
					return;

				try
				{
					tableWriter.close();
				}
				catch(IOException e)
				{
					ServerSystem.err.println("Could not close file of table data \"" + getTableSaveName() + ".", e);
				}
				tableWriter = null;
			}
			finally
			{
				writeLock.unlock();
			}
		}
	}

	private static TableDefinition imageTableDefinition = null;
	
	private static synchronized TableDefinition getImageTableDefinition()
	{
		if(imageTableDefinition != null)
			return imageTableDefinition;
		
			imageTableDefinition = new TableDefinition("Image table", "Table storing information when and where which images were taken.", 
					ColumnDefinition.createStringColumnDefinition("Image File", "Path to the image file.", false), 
					ColumnDefinition.createStringColumnDefinition("Image ID", "Identifier of image.", false), 
					ColumnDefinition.createStringColumnDefinition("Camera", "Name of the camera which took the image", true), 
					ColumnDefinition.createStringColumnDefinition("Channel Group", "Name of the channel group in which image was taken.", true), 
					ColumnDefinition.createStringColumnDefinition("Channel", "Name of the channel in which image was taken.", true), 
					ColumnDefinition.createIntegerColumnDefinition("Original Bit Depth", "Original bit depth of image, before image was rescaled to either 8bit, 16bit or 32bit.", false)
					);
		
		return imageTableDefinition;
	}
	
	private class MeasurementImageSaver extends UnicastRemoteObject implements ImageListener, TableProducer
	{

		/**
		 * Serial Version UID.
		 */
		private static final long					serialVersionUID		= 7506663632552331058L;

		private final String						imageSaveName;

		private final TableListener				tableDataListener;

		private LinkedBlockingQueue<ImageSaveData>	imagesQueue;

		private static final int					queueLimit				= 100;

		
		
		/**
		 * @throws RemoteException
		 */
		private MeasurementImageSaver(String imageSaveName, TableListener tableDataListener) throws RemoteException
		{
			super();
			this.imageSaveName = imageSaveName;
			this.tableDataListener = tableDataListener;
			this.imagesQueue = new LinkedBlockingQueue<ImageSaveData>();
			new Thread()
			{
				@Override
				public void run()
				{
					while(true)
					{
						try
						{
							ImageSaveData imageSaveData = imagesQueue.take();
							saveImage(imageSaveData);
						}
						catch(InterruptedException e)
						{
							ServerSystem.err.println("Interrupted while waiting on image writing thread. No further images written by this thread.", e);
							return;
						} catch (TableException e) {
							ServerSystem.err.println("Error occured while trying to save image. No further images written by this thread.", e);
							return;
						} catch (RemoteException e) {
							ServerSystem.err.println("Remote exception while trying to save image. No further images written by this thread.", e);
							return;
						}
						
					}
				}
			}.start();
		}

		private class ImageSaveData
		{
			public ImageSaveData(BufferedImage image, String imagePath, ImageEvent event)
			{
				this.image = image;
				this.imagePath = imagePath;
				this.event = event;
			}

			BufferedImage	image;
			String			imagePath;
			ImageEvent		event;
		}

		@Override
		public void imageMade(final ImageEvent e)
		{
			if(saveSettings == null)
				return;
			try
			{
				FileSaverManager.execute(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							processImage(e);
						}
						catch(RemoteException e)
						{
							ServerSystem.err.println("Failed saving image.", e);
						}
					}
				});
			}
			catch(RejectedExecutionException e1)
			{
				ServerSystem.out.println("Could not save image since image saver already deinitialized (" + e1.getMessage() + ")");
			}

		}

		private void processImage(ImageEvent event) throws RemoteException
		{
			// Create image.
			BufferedImage image = reusableImages.pollFirst();
			try
			{
				image = ImageTools.getMicroscopeImage(event, image);
			}
			catch(ImageConvertException e)
			{
				ServerSystem.err.println("Could not process and save image!", e);
				return;
			}

			Well well = event.getPositionInformation() == null ? null : event.getPositionInformation().getWell();
			int[] positionInformation = event.getPositionInformation() == null ? new int[0] : event.getPositionInformation().getPositions();

			// Save image
			// Construct file name of image.
			String channel = event.getChannel();
			String camera = event.getCamera();
			GregorianCalendar calendar = new GregorianCalendar();
			calendar.setTime(new Date(event.getCreationTime()));
			String imageFile = NamingMacros.convertFileName(saveSettings.getImageFileName(), imageSaveName, channel, well, positionInformation, event.getExecutionInformation(), calendar, camera, null);

			String imageFolderName = "";
			if(saveSettings.getImageFolderStructure() == ImageFolderStructure.SEPARATE_WELL_AND_CHANNEL || saveSettings.getImageFolderStructure() == ImageFolderStructure.SEPARATE_WELL_AND_POSITION || saveSettings.getImageFolderStructure() == ImageFolderStructure.SEPARATE_WELL_POSITION_AND_CHANNEL)
			{
				if(well != null)
				{
					if(imageFolderName.length() > 0)
						imageFolderName += File.separator;
					imageFolderName += "well " + well.getWellName();
				}
			}
			if(saveSettings.getImageFolderStructure() == ImageFolderStructure.SEPARATE_WELL_AND_POSITION || saveSettings.getImageFolderStructure() == ImageFolderStructure.SEPARATE_WELL_POSITION_AND_CHANNEL)
			{
				// Add position information if necessary (only for
				// multi-position jobs
				for(int i = 0; i < positionInformation.length; i++)
				{
					if(imageFolderName.length() > 0)
						imageFolderName += File.separator;
					imageFolderName += "position " + Integer.toString(positionInformation[i]);
				}
			}
			if(saveSettings.getImageFolderStructure() == ImageFolderStructure.SEPARATE_WELL_AND_CHANNEL || saveSettings.getImageFolderStructure() == ImageFolderStructure.SEPARATE_WELL_POSITION_AND_CHANNEL)
			{
				if(imageFolderName.length() > 0)
					imageFolderName += File.separator;
				imageFolderName += imageSaveName;
			}

			// Combine both to obtain full path
			String imagePath = imageFile + "." + saveSettings.getImageFileType();
			if(imageFolderName.length() > 0)
			{
				imagePath = imageFolderName + File.separator + imagePath;

				// Create folder if yet not existing
				File folder = new File(measurementSaveFolder + File.separator + imageFolderName);
				if(!folder.exists())
					folder.mkdirs();
			}
			
			// Save image to file
			if(imagesQueue.size() > queueLimit)
			{
				ServerSystem.out.println("The image queue is full. YouScope might slow down and some images might get lost. Reduce imaging speed.");
				try {
					saveImage(new ImageSaveData(image, imagePath, event));
				} catch (TableException e) {
					ServerSystem.err.println("Could not save image metadata.", e);
					
				}
			}
			else
			{
				imagesQueue.add(new ImageSaveData(image, imagePath, event));
			}

		}

		private void saveImage(ImageSaveData imageData) throws TableException, RemoteException
		{
			BufferedImage image = imageData.image;
			String imagePath = imageData.imagePath;
			ImageEvent event = imageData.event;
			File file = new File(measurementSaveFolder + File.separator + imagePath);
			try
			{
				if(ImageIO.write(image, saveSettings.getImageFileType(), file))
				{
					ServerSystem.out.println("Image of type " + imageSaveName + " saved to " + imagePath + ".");
				}
				else
					ServerSystem.err.println("Image of type " + imageSaveName + " could not be saved. Format \"" + saveSettings.getImageFileType() + "\" not known!", null);
			}
			catch(IOException e)
			{
				ServerSystem.err.println("New image of type " + imageSaveName + " could not be saved.", e);
			}
			reusableImages.offer(image);
			// Save image metadata
			saveImageMetadataInList(event, imagePath, imageSaveName);
		}

		private synchronized void saveImageMetadataInList(final ImageEvent e, String path, String imageName) throws TableException, RemoteException
		{
			Table imageTable = new Table(getImageTableDefinition(), e.getCreationTime(), e.getPositionInformation(), e.getExecutionInformation());
			imageTable.addRow(path, imageName, e.getCamera(), e.getConfigGroup(), e.getChannel(), new Integer(e.getBitDepth()));
			tableDataListener.newTableProduced(imageTable);
		}

		@Override
		public void removeTableListener(TableListener listener)
		{
			// not relevant, since only used internally
			throw new UnsupportedOperationException();
		}

		@Override
		public void addTableListener(TableListener listener)
		{
			// not relevant, since only used internally
			throw new UnsupportedOperationException();
		}

		@Override
		public TableDefinition getProducedTableDefinition() throws RemoteException {
			return getProducedTableDefinition();
		}
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
	public void setSaveSettings(MeasurementSaveSettings saveSettings) throws MeasurementRunningException
	{
		if(measurementRunning)
			throw new MeasurementRunningException();
		this.saveSettings = saveSettings;
	}

	@Override
	public MeasurementSaveSettings getSaveSettings()
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
		return measurementSaveFolder;
	}
}
