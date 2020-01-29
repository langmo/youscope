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
package org.youscope.plugin.measurementviewer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Point2D;
import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultFormatter;

import org.youscope.addon.AddonException;
import org.youscope.addon.tool.ToolAddonUIAdapter;
import org.youscope.addon.tool.ToolMetadata;
import org.youscope.addon.tool.ToolMetadataAdapter;
import org.youscope.clientinterfaces.StandardProperty;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.plugin.measurementviewer.MeasurementView.MeasurementViewListener;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.ImageLoadingTools;
import org.youscope.uielements.ImagePanel;

import org.youscope.uielements.ImagePanel.PixelInfo;
import org.youscope.uielements.ImagePanel.PixelListener;
import org.youscope.uielements.ImagePanel.ZoomAndCenterListener;

/**
 * @author Moritz Lang
 *
 */
class MeasurementViewer extends ToolAddonUIAdapter
{
	private final File imagesTableFileAutoload;
	
	private File baseFolder = null;
	
	
	private final JSlider	imageSelectSlider;
	private final JFormattedTextField	imageSelectField	= new JFormattedTextField(new DefaultFormatter()
	{
		/**
		 * Serial Version UID. 
		 */
		private static final long serialVersionUID = 2384364851898117138L;
		@Override
		public Class<?> getValueClass()
		{
			return ImageNumber.class;
		}
		@Override
		public ImageNumber stringToValue(String string) throws ParseException
		{
			String[] elems = string.split("\\.", -1);
			long[] result = new long[elems.length];
			for(int i=0; i<elems.length; i++)
			{
				if(elems[i].length() == 0)
				{
					result[i] = 0;
					continue;
				}
				try
				{
					result[i] = Long.parseLong(elems[i]);
				}
				catch(@SuppressWarnings("unused") NumberFormatException e)
				{
					int parsePos = 0;
					for(int j=0; j<i; j++)
					{
						parsePos+=elems[j].length()+1;
					}
					throw new ParseException("Invalid image number", parsePos);
				}
			}
			ImageNumber readNumber = new ImageNumber(result);
			int nextImageIndex = Collections.binarySearch(imageNumberList, readNumber);
			if(nextImageIndex < 0)
				nextImageIndex = -nextImageIndex-2;
			if(nextImageIndex >= imageNumberList.size())
				nextImageIndex = imageNumberList.size()-1;
			if(nextImageIndex < 0)
				nextImageIndex = 0;
			return imageNumberList.get(nextImageIndex);
		}
		@Override
		public String valueToString(Object value)
		{
			if(value != null && value instanceof ImageNumber)
			{
				long[] imageNumber = ((ImageNumber)value).toArray();
				StringBuilder builder = new StringBuilder(Long.toString(imageNumber[0]));
				for(int i=1; i<imageNumber.length; i++)
				{
					builder.append(".");
					builder.append(Long.toString(imageNumber[i]));
				}
				return builder.toString();
			}
			return "0";
		}
		@Override
		public boolean getAllowsInvalid()
		{
			return true;
		}
		
		@Override
		public boolean getCommitsOnValidEdit()
		{
			return false;
		}
		@Override
		public boolean getOverwriteMode()
		{
			return false;
		}
	});
	private final JPanel mainPanel = new JPanel(new BorderLayout(0,0));
	private final JPanel contentPanel = new JPanel(new BorderLayout());
	private final DynamicPanel waitPanel = new DynamicPanel();
	
	private volatile boolean imageChanging = false;
	private final static String FRAME_TITLE = "Measurement Viewer";
	private String currentFrameTitle = FRAME_TITLE; 
	private volatile int currentImageIndex = 0;
	
	private ArrayList<MeasurementView> measurementViews = new ArrayList<>(1);
	private JPanel measurementViewsPanel = new JPanel();
	private ImageFolderNode rootNode = null;
	private ArrayList<ImageNumber> imageNumberList = new ArrayList<>();
	
	/**
	 * Constructor.
	 * @param client Interface to the YouScope client.
	 * @param server Interface to the YouScope server.
	 * @param imagesTableLocation Location of the image table file. Set to null to choose manually.
	 * @throws AddonException 
	 */
	public MeasurementViewer(YouScopeClient client, YouScopeServer server, String imagesTableLocation) throws AddonException
	{
		super(getMetadata(), client, server);
		if(imagesTableLocation != null)
		{
			File file = new File(imagesTableLocation);
			if(!file.exists() || !file.isFile())
			{
				imagesTableFileAutoload = null;
			}
			else
				imagesTableFileAutoload = file;
		}
		else
			imagesTableFileAutoload = null;
		
		imageSelectSlider = new JSlider(1, 1, 1);
	}
	
	public final static String TYPE_IDENTIFIER = "YouScope.YouScopeMeasurementViewer";
	
	static ToolMetadata getMetadata()
	{
		return new ToolMetadataAdapter(TYPE_IDENTIFIER, "Measurement Viewer", new String[0], 
				"Displays the images taken in a finished experiment. Provides a user interface to quickly visualize all images taken at a given position and channel, with the option to adjust the contrast.",
				"icons/eye.png");
	}
	
	/**
	 * Convenient constructor. Same as MeasurementViewer(client, server, null).
	 * @param client Interface to the YouScope client.
	 * @param server Interface to the YouScope server.
	 * @throws AddonException 
	 */
	public MeasurementViewer(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		this(client, server, null);
	}
	
	private void updateImageNumbers()
	{
		Runnable runner = new Runnable()
		{
			@Override
			public void run()
			{
				imageChanging = true;
				ImageNumber currentNumber = imageNumberList.isEmpty() ? null : imageNumberList.get(currentImageIndex);
				HashSet<ImageNumber> numberSet = new HashSet<>();
				for(MeasurementView view : measurementViews)
				{
					numberSet.addAll(view.getImageNumbers());
				}
				imageNumberList.clear();
				imageNumberList.addAll(numberSet);
				Collections.sort(imageNumberList);
				int nextImageIndex;
				if(currentNumber == null)
					nextImageIndex = 0;
				else
				{
					nextImageIndex = Collections.binarySearch(imageNumberList, currentNumber);
					if(nextImageIndex < 0)
						nextImageIndex = -nextImageIndex-1;
					if(nextImageIndex >= imageNumberList.size())
						nextImageIndex = imageNumberList.size()-1;
				}
				
				int numImages = imageNumberList.size();
				if(numImages > 0)
					imageSelectSlider.setEnabled(true);
				imageSelectSlider.setMaximum(numImages);
				imageSelectSlider.setEnabled(true);
				
				// Paint approximately 10 major ticks + labels
				int majorDist = (int)Math.round(numImages / 100.0) * 10;
				if(majorDist < 1)
				{
					majorDist = 1;
				}
				imageSelectSlider.setMajorTickSpacing(majorDist);
				int lowerDist = majorDist / 5;
				if(lowerDist <= 0)
					lowerDist = 1;
				if(majorDist == 1 || lowerDist == 1)
					imageSelectSlider.setSnapToTicks(true);
				else
					imageSelectSlider.setSnapToTicks(false);
				imageSelectSlider.setMinorTickSpacing(lowerDist);
				
				Hashtable<Integer, JLabel> majorTicks = new Hashtable<>();
				for(int i=0; i<numImages; i+=majorDist)
				{
					JLabel label = new JLabel(imageNumberList.get(i).toString());
					label.setOpaque(false);
					label.setForeground(imageSelectSlider.getForeground());
					majorTicks.put(i+1, label);
				}
				imageSelectSlider.setLabelTable(majorTicks);
				
				//imageSelectField.setMaximalValue(numImages);
				selectImageIndex(nextImageIndex);
				imageChanging = false;
			}
		};
		if(SwingUtilities.isEventDispatchThread())
			runner.run();
		else
			SwingUtilities.invokeLater(runner);
	}
	
	private void removeMeasurementView(final MeasurementView measurementView)
	{
		if(measurementView == null)
			return;
		Runnable runner = new Runnable()
		{
			@Override
			public void run() 
			{
				if(isSeparateFrame())
				{
					measurementView.removePixelListener(pixelListener);
				}
				measurementView.removeKeyListener(keyListener);
				measurementView.removeZoomAndCenterListener(zoomAndCenterListener);
				measurementView.removeMeasurementViewListener(measurementViewListener);
				measurementViews.remove(measurementView);
							
				// update layout
				reorderTiles();
				updateImageNumbers();
			}
		};
		if(SwingUtilities.isEventDispatchThread())
			runner.run();
		else
			SwingUtilities.invokeLater(runner);
	}
	
	private void addMeasurementView(final MeasurementView measurementView)
	{
		Runnable runner = new Runnable()
		{
			@Override
			public void run() 
			{
				if(isSeparateFrame())
				{
					measurementView.addPixelListener(pixelListener);
				}
				measurementView.addKeyListener(keyListener);
				measurementView.addZoomAndCenterListener(zoomAndCenterListener);
				measurementView.addMeasurementViewListener(measurementViewListener);
				if(!measurementViews.isEmpty())
				{
					double currentZoom = measurementViews.get(0).getZoom();
					Point2D.Double currentCenter = measurementViews.get(0).getCenter();
					measurementView.setZoomAndCenter(currentZoom, currentCenter.getX(), currentCenter.getY());
				}
				measurementViews.add(measurementView);
				
				// update layout
				reorderTiles();
				updateImageNumbers();
			}
	
		};
		if(SwingUtilities.isEventDispatchThread())
			runner.run();
		else
			SwingUtilities.invokeLater(runner);
	}
	private final Icon addTileIcon = ImageLoadingTools.getResourceIcon("icons/application-split.png", "Add Tile");
	private void reorderTiles()
	{
		measurementViewsPanel.removeAll();
		int numX = (int) Math.ceil(Math.sqrt(measurementViews.size()));
		int numY = (int) Math.ceil(((double)measurementViews.size()) / numX);
		measurementViewsPanel.setLayout(new GridLayout(numY, numX));
		for(MeasurementView view : measurementViews)
		{
			view.setIsTileCloseable(measurementViews.size() > 1);
			measurementViewsPanel.add(view);
		}
		for(int i=measurementViews.size(); i<numX*numY; i++)
		{
			DynamicPanel emptyTile = new DynamicPanel();
			emptyTile.setBackground(measurementViews.get(0).getBackground());
			emptyTile.setOpaque(true);
			JButton newTileButton;
			if(addTileIcon == null)
				newTileButton = new JButton("Add Tile");
			else
				newTileButton = new JButton(addTileIcon);
			newTileButton.setToolTipText("Add Tile");
			newTileButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					addMeasurementView(new MeasurementView(getClient(), rootNode, baseFolder, null));
				}
			});
			newTileButton.setOpaque(false);
			emptyTile.addFillEmpty();
			emptyTile.addCenter(newTileButton);
			emptyTile.addFillEmpty();
			measurementViewsPanel.add(emptyTile);
		}
		measurementViewsPanel.revalidate();	
	}
	private final PixelListener pixelListener = new PixelListener()
	{
		@Override
		public void activePixelChanged(PixelInfo pixel) {
			if(pixel == null)
				getContainingFrame().setTitle(currentFrameTitle);
			else
			{
				String text = FRAME_TITLE + " - ";
				text+="X="+Integer.toString(pixel.getX()+1)+" Y="+Integer.toString(pixel.getY()+1)+" I=";
				text+=Long.toString(pixel.getValue())+ " ("+Long.toString(Math.round(pixel.getRelativeValue()*100))+"%)";
				getContainingFrame().setTitle(text);
			}
		}
	};
	private final KeyListener keyListener = new KeyListener() {
		@Override
		public void keyTyped(KeyEvent e) {
			// do nothing.
		}
		
		@Override
		public void keyReleased(KeyEvent e) {
			// do nothing.
		}
		
		@Override
		public void keyPressed(KeyEvent e) {
			int keyCode = e.getKeyCode();
		    switch( keyCode ) { 
		        case KeyEvent.VK_UP:
		        	selectImageIndex(--currentImageIndex);
		            break;
		        case KeyEvent.VK_DOWN:
		        	selectImageIndex(++currentImageIndex);
		            break;
		        case KeyEvent.VK_LEFT:
		        	selectImageIndex(--currentImageIndex);
		            break;
		        case KeyEvent.VK_RIGHT :
		        	selectImageIndex(++currentImageIndex);
		            break;
	            default:
		           // do nothing.
	            	break;
		     }
		}
	};
	
	private final MeasurementViewListener measurementViewListener = new MeasurementViewListener() 
	{
		@Override
		public void imageNumbersChanged() {
			updateImageNumbers();
		}

		@Override
		public void addTiles(ImageFolderNode[] folders) {
			for(ImageFolderNode folder : folders)
			{
				addMeasurementView(new MeasurementView(getClient(), rootNode, baseFolder, folder));
			}
			
		}

		@Override
		public void removeTile(MeasurementView measurementView) {
			removeMeasurementView(measurementView);
			
		}
	};
	
	@Override
	public java.awt.Component createUI()
	{
		setMaximizable(true);
		setResizable(true);
		setTitle(currentFrameTitle);
		setPreferredSize(new Dimension(900, 500));		
	
		imageSelectSlider.setOpaque(false);
		imageSelectSlider.setPaintTicks(true);
		imageSelectSlider.setSnapToTicks(true);
		imageSelectSlider.setEnabled(false);
		imageSelectSlider.setPaintLabels(true);
		
		imageSelectSlider.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent arg0)
			{
				if(imageChanging)
					return;
				int imageNo = imageSelectSlider.getValue()-1;
				selectImageIndex(imageNo);
			}
		});
		imageSelectSlider.setForeground(ImagePanel.DEFAULT_FOREGROUND);
		
		imageSelectField.setOpaque(false);
		//imageSelectField.setMinimalValue(1);
		//imageSelectField.setMaximalValue(1);
		imageSelectField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(imageChanging)
					return;
				ImageNumber readNumber = (ImageNumber) imageSelectField.getValue();
				
				int nextImageIndex = Collections.binarySearch(imageNumberList, readNumber);
				if(nextImageIndex < 0)
					nextImageIndex = -nextImageIndex-2;
				if(nextImageIndex >= imageNumberList.size())
					nextImageIndex = imageNumberList.size()-1;
				if(nextImageIndex < 0)
					nextImageIndex = 0;
				selectImageIndex(nextImageIndex);	
			}
		});
		imageSelectField.setValue(new ImageNumber(0));
		imageSelectField.setForeground(ImagePanel.DEFAULT_FOREGROUND);
		
		JPanel controlsPanel = new JPanel(new BorderLayout());
		controlsPanel.setBackground(ImagePanel.DEFAULT_BACKGROUND);
		JPanel imageIndexPanel = new JPanel(new GridLayout(2, 1, 2, 2));
		imageIndexPanel.setOpaque(false);
		JLabel selectLabel = new JLabel("Evaluation:");
		selectLabel.addKeyListener(keyListener);
		selectLabel.setForeground(ImagePanel.DEFAULT_FOREGROUND);
		imageIndexPanel.add(selectLabel);
		imageIndexPanel.add(imageSelectField);
		controlsPanel.add(imageIndexPanel, BorderLayout.EAST);
		controlsPanel.add(imageSelectSlider, BorderLayout.CENTER);
		controlsPanel.setBorder(new EmptyBorder(3,5,3,5));
		
		
		contentPanel.setOpaque(false);
		contentPanel.add(controlsPanel, BorderLayout.SOUTH);
		contentPanel.add(measurementViewsPanel, BorderLayout.CENTER);
        
		if(isSeparateFrame())
		{
			getContainingFrame().setMargins(0, 0, 0, 0);
		}
		
		waitPanel.addFillEmpty();
		waitPanel.setOpaque(true);
		waitPanel.setBackground(ImagePanel.DEFAULT_BACKGROUND);
		JLabel waitText = new JLabel("<html><h1 style=\"text-align:center\">Loading Measurement</h1><p style=\"text-align:center\">Please wait until measurement images are loaded...</p></html>", SwingConstants.CENTER);
		waitText.setOpaque(false);
		waitText.setForeground(ImagePanel.DEFAULT_FOREGROUND);
		waitPanel.addCenter(waitText);
		waitPanel.addFillEmpty();
		
		if(imagesTableFileAutoload != null)
		{
			loadMeasurement(imagesTableFileAutoload);
		}
		else
		{
			DynamicPanel chooseMeasurementPanel = new DynamicPanel();
			chooseMeasurementPanel.addFillEmpty();
			chooseMeasurementPanel.setOpaque(true);
			chooseMeasurementPanel.setBackground(ImagePanel.DEFAULT_BACKGROUND);
			JLabel text = new JLabel("<html><h1 style=\"text-align:center\">Measurement Viewer</h1><p style=\"text-align:center\">The measurement viewer displays images taken during a measurement.<br />To start, press the button to select the measurement whose images should be display!<br /><br /></p></html>", SwingConstants.CENTER);
			text.setOpaque(false);
			text.setForeground(ImagePanel.DEFAULT_FOREGROUND);
			chooseMeasurementPanel.addCenter(text);
			JButton selectButton = new JButton("Select Measurement");
			selectButton.setOpaque(false);
			selectButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e) {
					loadMeasurement();
					
				}
			});
			chooseMeasurementPanel.addCenter(selectButton);
			chooseMeasurementPanel.addFillEmpty();
			mainPanel.add(chooseMeasurementPanel, BorderLayout.CENTER);
		}
		return mainPanel;
    }
	
	private void loadMeasurement()
	{
		JFileChooser fileChooser = new JFileChooser((String) getClient().getPropertyProvider().getProperty(StandardProperty.PROPERTY_LAST_MEASUREMENT_SAVE_FOLDER));
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Image Table Files (.csv)", "csv"));
		fileChooser.addChoosableFileFilter(fileChooser.getAcceptAllFileFilter());
		int returnVal = fileChooser.showDialog(null, "View Measurement");
		if(returnVal == JFileChooser.APPROVE_OPTION)
		{
			loadMeasurement(fileChooser.getSelectedFile());		
		}
		else
		{
			return;
		}
	}
	
	private void showPanel(final Component panel)
	{
		Runnable runner = new Runnable()
		{
			@Override
			public void run() {
				mainPanel.removeAll();
				mainPanel.add(panel, BorderLayout.CENTER);
				mainPanel.revalidate();
			}
	
		};
		if(SwingUtilities.isEventDispatchThread())
			runner.run();
		else
			SwingUtilities.invokeLater(runner);
	}
	
	private void loadMeasurement(final File imagesTableFile)
	{
		showPanel(waitPanel);
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				// Load image information from images.csv file.
				try
				{
					rootNode = ImagesFileProcessor.processImagesFile(imagesTableFile);
				}
				catch(Exception e)
				{
					sendErrorMessage("Could not load measurement tree.", e);
					return;
				}
				baseFolder = imagesTableFile.getParentFile();
				addMeasurementView(new MeasurementView(getClient(), rootNode, baseFolder, null));
				showPanel(contentPanel);
			}
		}).start();
	}
	
	
	private void selectImageIndex(int index)
	{
		if(index < 0)
			index = 0;
		else if(index >= imageNumberList.size())
			index = imageNumberList.size()-1;
		if(index < 0) // no images available
			return;
		currentImageIndex = index;
		imageChanging = true;
		imageSelectField.setValue(imageNumberList.get(index));
		imageSelectSlider.setValue(index+1);
		imageChanging = false;
		ImageNumber currentNumber = imageNumberList.get(currentImageIndex);
		if(measurementViews.size() == 1)
		{
			String imageName = null;
			try {
				imageName = measurementViews.get(0).selectImage(currentNumber);
			} catch (AddonException e) {
				sendErrorMessage("Could not load image.", e);
			}
			currentFrameTitle = FRAME_TITLE + " - " + (imageName==null ? "unknown" : imageName);
		}
		else
		{
			for(MeasurementView view:measurementViews)
			{
				try {
					view.selectImage(currentNumber);
				} catch (AddonException e) {
					sendErrorMessage("Could not load image.", e);
				}
			}
			currentFrameTitle = FRAME_TITLE + " - Evaluation " + currentNumber.toString();
		}
		
		
		getContainingFrame().setTitle(currentFrameTitle);
	}
	
	private final ZoomAndCenterListener zoomAndCenterListener = new ZoomAndCenterListener() 
	{
		boolean changing = false;
		@Override
		public void zoomOrCenterChanged(double zoom, double centerX, double centerY) {
			if(changing)
				return;
			if(measurementViews.size() <= 1)
				return;
			changing = true;
			for(MeasurementView view : measurementViews)
			{
				view.setZoomAndCenter(zoom, centerX, centerY);
			}
			changing = false;
		}
	};
}
