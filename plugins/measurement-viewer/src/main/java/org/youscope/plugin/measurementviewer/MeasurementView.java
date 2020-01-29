package org.youscope.plugin.measurementviewer;

import java.awt.BorderLayout;
import java.awt.event.KeyListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.youscope.addon.AddonException;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.plugin.measurementviewer.MeasurementTilingControl.TilingListener;
import org.youscope.uielements.ImagePanel;
import org.youscope.uielements.ImagePanel.PixelListener;
import org.youscope.uielements.ImagePanel.ZoomAndCenterListener;

class MeasurementView extends JPanel 
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -6361712668659321234L;
	private final MeasurementTree measurementTree;
	private final MeasurementTilingControl tilingControl;
	private final ImagePanel imagePanel;
	private ImageFolderNode imageFolder = null;
	private final File baseFolder;
	private final ArrayList<MeasurementViewListener> measurementViewListeners = new ArrayList<MeasurementViewListener>(1);
	public MeasurementView(YouScopeClient client, ImageFolderNode rootNode, File baseFolder, ImageFolderNode selectedNode) 
	{
		super(new BorderLayout());
		this.baseFolder = baseFolder;
		measurementTree = new MeasurementTree();
		// Initialize content
		measurementTree.addImageFolderListener(new ImageFolderListener()
		{
			@Override
			public void showFolder(ImageFolderNode imageFolder) {
				showImageSeries(imageFolder);
			}

			@Override
			public void addFolders(ImageFolderNode[] folders) {
				for(MeasurementViewListener listener : measurementViewListeners)
				{
					listener.addTiles(folders);
				}
				
			}
	
		});
		measurementTree.setRootNode(rootNode, selectedNode);
		
		tilingControl = new MeasurementTilingControl();
		tilingControl.addTilingListener(new TilingListener() {
			
			@Override
			public void removeTiling() {
				ArrayList<MeasurementViewListener> listCopy = new ArrayList<>(measurementViewListeners);
				for(MeasurementViewListener listener : listCopy)
				{
					listener.removeTile(MeasurementView.this);
				}
			}
			
			@Override
			public void addTiling() {
				ArrayList<MeasurementViewListener> listCopy = new ArrayList<>(measurementViewListeners);
				for(MeasurementViewListener listener : listCopy)
				{
					listener.addTiles(new ImageFolderNode[]{imageFolder});
				}
			}
		});
		
		imagePanel = new ImagePanel(client);
		imagePanel.setUserChoosesAutoAdjustContrast(true);
		imagePanel.setAutoAdjustContrast(true);
		setBackground(imagePanel.getBackground());
		
		JScrollPane treeScrollPane = new JScrollPane(measurementTree);
		treeScrollPane.getViewport().setOpaque(false);
		treeScrollPane.setOpaque(false);
		treeScrollPane.setBorder(null);
		imagePanel.addControl("Image Series", treeScrollPane, true);
		
		imagePanel.addControl("Tiles", tilingControl);
		
		add(imagePanel, BorderLayout.CENTER);
	}
	public void setIsTileCloseable(final boolean closeable)
	{
		tilingControl.setIsTileCloseable(closeable);
	}
	/**
	 * Returns the current zoom level.
	 * @return Current zoom level.
	 */
	public double getZoom()
	{
		return imagePanel.getZoom();
	}
	/**
	 * Returns the current center of the display area.
	 * @return center of display area.
	 */
	public Point2D.Double getCenter()
	{
		return imagePanel.getCenter();
	}
	
	static interface MeasurementViewListener
	{
		void imageNumbersChanged();
		void addTiles(ImageFolderNode[] folders);
		void removeTile(MeasurementView measurementView);
	}
	
	public Collection<? extends ImageNumber> getImageNumbers()
	{
		if(imageFolder == null)
			return new ArrayList<ImageNumber>(0);
		return imageFolder.getImageList().getImageNumbers();
	}
	
	public void addPixelListener(PixelListener listener)
	{
		imagePanel.addPixelListener(listener);
	}
	public void removePixelListener(PixelListener listener)
	{
		imagePanel.removePixelListener(listener);
	}
	public void addZoomAndCenterListener(ZoomAndCenterListener listener)
	{
		imagePanel.addZoomAndCenterListener(listener);
	}
	public void removeZoomAndCenterListener(ZoomAndCenterListener listener)
	{
		imagePanel.removeZoomAndCenterListener(listener);
	}
	public void setZoomAndCenter(double zoom, double centerX, double centerY)
	{
		imagePanel.setZoomAndCenter(zoom, centerX, centerY);
	}
	public void addMeasurementViewListener(MeasurementViewListener listener)
	{
		measurementViewListeners.add(listener);
	}
	public void removeMeasurementViewListener(MeasurementViewListener listener)
	{
		measurementViewListeners.remove(listener);
	}
	
	@Override
	public synchronized void addKeyListener(KeyListener listener)
	{
		super.addKeyListener(listener);
		imagePanel.addKeyListener(listener);
	}
	@Override
	public synchronized void removeKeyListener(KeyListener listener)
	{
		super.removeKeyListener(listener);
		imagePanel.removeKeyListener(listener);
	}
	
	private void showImageSeries(ImageFolderNode imageFolder)
	{
		this.imageFolder = imageFolder;
		for(MeasurementViewListener listener : measurementViewListeners)
		{
			listener.imageNumbersChanged();
		}
	}
	String selectImage(ImageNumber imageNumber) throws AddonException
	{
		String imageURL = imageFolder.getImageList().getClosest(imageNumber); 
		File imageFile = new File(baseFolder, imageURL);
		if(!imageFile.exists() || !imageFile.isFile())
		{
			throw new AddonException("Cannot load image file " + imageFile.getAbsolutePath() + " because it does not exist.", null);
		}
		BufferedImage image;
		try 
		{
		    image = ImageIO.read(imageFile);
		} 
		catch (IOException e) 
		{
			throw new AddonException("Loading of image file " + imageFile.getAbsolutePath() + " failed because of I/O errors.", e);
		}
		if(image == null)
		{
			String[] supportedImageTypes = ImageIO.getReaderFileSuffixes();
			String imageTypesString = "";
			for(String supportedImageType : supportedImageTypes)
			{
				if(supportedImageType == null || supportedImageType.length() < 1)
					continue;
				if(imageTypesString.length() > 0)
					imageTypesString += ", ";
				imageTypesString += supportedImageType;
			}
			throw new AddonException("Could not load image file " + imageFile.getAbsolutePath() + " since image type can not be read.\nSupported image types are: " + imageTypesString +".\nTo support more image types, please download an appropriete plugin for YouScope/Java.", null);
		}
		imagePanel.setImage(image);
		return imageFile.getName();
	}
}
