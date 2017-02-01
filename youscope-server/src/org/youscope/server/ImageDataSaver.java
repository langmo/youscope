package org.youscope.server;

import java.awt.image.BufferedImage;
import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingDeque;

import javax.imageio.ImageIO;

import org.youscope.common.image.ImageEvent;
import org.youscope.common.image.ImageListener;
import org.youscope.common.resource.ResourceException;
import org.youscope.common.table.ColumnDefinition;
import org.youscope.common.table.Table;
import org.youscope.common.table.TableDefinition;
import org.youscope.common.table.TableException;
import org.youscope.common.table.TableListener;
import org.youscope.common.table.TableProducer;
import org.youscope.common.util.ImageConvertException;
import org.youscope.common.util.ImageTools;

class ImageDataSaver extends UnicastRemoteObject implements ImageListener, TableProducer {

	/**
	 * Serial Version UID.
	 */
	private static final long					serialVersionUID		= 7506663632552331058L;

	private final String						imageSaveName;
	
	private final MeasurementSaverImpl.SaverInformation supervisor;

	private final ArrayList<TableListener> tableDataListeners = new ArrayList<>(1);
	
	private static TableDefinition imageTableDefinition = null;
	
	private final static int MAX_REUSABLE_IMAGES = 10;
	
	private static final LinkedBlockingDeque<BufferedImage>	reusableImages			= new LinkedBlockingDeque<BufferedImage>(MAX_REUSABLE_IMAGES);
	
	/**
	 * Constructor
	 * @throws RemoteException
	 */
	ImageDataSaver(String imageSaveName, MeasurementSaverImpl.SaverInformation supervisor) throws RemoteException
	{
		super();
		this.supervisor = supervisor;
		this.imageSaveName = imageSaveName;
	}

	@Override
	public void imageMade(final ImageEvent<?> event)
	{
		if(!supervisor.isReady())
			return;
		final String relativePath;
		final String filePath;
		final String fileType;
		try {
			relativePath = supervisor.getRelativeImagePath(event, imageSaveName);
			filePath = supervisor.getFullImagePath(event, imageSaveName);
			fileType = supervisor.getImageFileType(event, imageSaveName);
		} catch (ResourceException | RemoteException e1) {
			ServerSystem.err.println("Image of type " + imageSaveName + " cannot be saved since it could not be determined where or how to save the image.", e1);
			return;
		}
		if(filePath == null || fileType == null || relativePath == null)
		{
			ServerSystem.err.println("Image of type " + imageSaveName + " cannot be saved since image file name was not defined.", null);
			return;
		}
		
		ServerSystem.out.println("Queuing image of type " + imageSaveName + " to be saved to " + filePath + ".");
		FileSaverManager.execute(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					saveImage(event, relativePath, filePath, fileType);
				}
				catch(RemoteException e)
				{
					ServerSystem.err.println("Failed saving image.", e);
				}
			}
		});
	}

	private void saveImage(final ImageEvent<?> event, final String relativePath, final String filePath, final String fileType) throws RemoteException
	{
		// Convert image.
		BufferedImage image = reusableImages.pollFirst();
		try
		{
			image = ImageTools.getMicroscopeImage(event, image);
		}
		catch(ImageConvertException e)
		{
			ServerSystem.err.println("Could not process image!", e);
			return;
		}

		// Save image
		File file = new File(filePath);
		File folder = file.getParentFile();
		try
		{
			if(!folder.exists())
				folder.mkdirs();
			if(ImageIO.write(image, fileType, file))
			{
				ServerSystem.out.println("Image of type " + imageSaveName + " saved to " + filePath + ".");
			}
			else
				ServerSystem.err.println("Image of type " + imageSaveName + " cannot be saved to " + filePath + ". Format \"" + fileType + "\" not supported!", null);
		}
		catch(Exception e)
		{
			ServerSystem.err.println("Image of type " + imageSaveName + " cannot be saved to " + filePath + ".", e);
		}
		reusableImages.offer(image);
		// Save image metadata
		try {
			saveImageMetadataInList(event, relativePath, imageSaveName);
		} catch (TableException e) {
			ServerSystem.err.println("Metadata of image of type " + imageSaveName + " saved to " + filePath + " cannot be saved in image table.", e);
		}
	}

	private synchronized void saveImageMetadataInList(final ImageEvent<?> e, String path, String imageName) throws TableException, RemoteException
	{
		Table imageTable = new Table(getProducedTableDefinition(), e.getCreationTime(), e.getPositionInformation(), e.getExecutionInformation());
		imageTable.addRow(path, imageName, e.getCamera(), e.getChannelGroup(), e.getChannel(), new Integer(e.getBitDepth()));
		for(TableListener listener : tableDataListeners)
		{
			listener.newTableProduced(imageTable);
		}
	}

	@Override
	public void removeTableListener(TableListener listener)
	{
		tableDataListeners.remove(listener);
	}

	@Override
	public void addTableListener(TableListener listener)
	{
		tableDataListeners.add(listener);
	}

	@Override
	public TableDefinition getProducedTableDefinition()
	{
		synchronized(ImageDataSaver.class)
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
	}
}