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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.youscope.addon.AddonException;
import org.youscope.addon.tool.ToolAddonUIAdapter;
import org.youscope.addon.tool.ToolMetadata;
import org.youscope.addon.tool.ToolMetadataAdapter;
import org.youscope.clientinterfaces.StandardProperty;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.ImagePanel;
import org.youscope.uielements.IntegerTextField;
import org.youscope.uielements.ImagePanel.PixelInfo;
import org.youscope.uielements.ImagePanel.PixelListener;

/**
 * @author langmo
 *
 */
class MeasurementViewer extends ToolAddonUIAdapter
{
	private final MeasurementTree measurementTree;
	private File imagesTableFile;
	private final ImagePanel imagePanel;
	private final JSlider	imageSelectSlider;
	private final IntegerTextField	imageSelectField	= new IntegerTextField();
	private final JPanel mainPanel = new JPanel(new BorderLayout(0,0));
	private final JPanel contentPanel = new JPanel(new BorderLayout());
	private final DynamicPanel waitPanel = new DynamicPanel();
	
	private volatile boolean imageChanging = false;
	private ImageFolderNode imageFolder = null;
	private final static String FRAME_TITLE = "Measurement Viewer";
	private String currentFrameTitle = FRAME_TITLE; 
	private volatile int currentImageIndex = 0;
	
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
			imagesTableFile = new File(imagesTableLocation);
			if(!imagesTableFile.exists() || !imagesTableFile.isFile())
			{
				imagesTableFile = null;
			}
		}
		
		measurementTree = new MeasurementTree();
		imagePanel = new ImagePanel(getClient());
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
	
	@Override
	public java.awt.Component createUI()
	{
		setMaximizable(true);
		setResizable(true);
		setTitle(currentFrameTitle);
		setPreferredSize(new Dimension(900, 500));
		
		KeyListener keyListener = new KeyListener() {
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
		
		imagePanel.setUserChoosesAutoAdjustContrast(true);
		imagePanel.setAutoAdjustContrast(true);
		imagePanel.addKeyListener(keyListener);
		
		// Initialize content
		measurementTree.addImageFolderListener(new ImageFolderListener()
		{
			@Override
			public void showFolder(ImageFolderNode imageFolder) {
				showImageSeries(imagesTableFile, imageFolder);
			}
	
		});
		JScrollPane treeScrollPane = new JScrollPane(measurementTree);
		treeScrollPane.getViewport().setOpaque(false);
		treeScrollPane.setOpaque(false);
		treeScrollPane.setBorder(null);
		imagePanel.addControl("Image Series", treeScrollPane, true);
	
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
		imageSelectField.setMinimalValue(1);
		imageSelectField.setMaximalValue(1);
		imageSelectField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(imageChanging)
					return;
				int imageNo = ((Number)imageSelectField.getValue()).intValue() -1;
				selectImageIndex(imageNo);	
			}
		});
		imageSelectField.setValue(1);
		imageSelectField.setMinimalValue(1);
		imageSelectField.setMaximalValue(1);
		imageSelectField.setForeground(ImagePanel.DEFAULT_FOREGROUND);
		
		JPanel controlsPanel = new JPanel(new BorderLayout());
		controlsPanel.setBackground(ImagePanel.DEFAULT_BACKGROUND);
		JPanel imageIndexPanel = new JPanel(new GridLayout(2, 1, 2, 2));
		imageIndexPanel.setOpaque(false);
		JLabel selectLabel = new JLabel("Frame:");
		selectLabel.addKeyListener(keyListener);
		selectLabel.setForeground(ImagePanel.DEFAULT_FOREGROUND);
		imageIndexPanel.add(selectLabel);
		imageIndexPanel.add(imageSelectField);
		controlsPanel.add(imageIndexPanel, BorderLayout.EAST);
		controlsPanel.add(imageSelectSlider, BorderLayout.CENTER);
		controlsPanel.setBorder(new EmptyBorder(3,5,3,5));
		
		
		contentPanel.setOpaque(false);
		contentPanel.add(controlsPanel, BorderLayout.SOUTH);
		contentPanel.add(imagePanel, BorderLayout.CENTER);
        
		if(isSeparateFrame())
		{
			getContainingFrame().setMargins(0, 0, 0, 0);
			imagePanel.addPixelListener(new PixelListener()
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
			});
		}
		
		waitPanel.addFillEmpty();
		waitPanel.setOpaque(true);
		waitPanel.setBackground(ImagePanel.DEFAULT_BACKGROUND);
		JLabel waitText = new JLabel("<html><h1 style=\"text-align:center\">Loading Measurement</h1><p style=\"text-align:center\">Please wait until measurement images are loaded...</p></html>", SwingConstants.CENTER);
		waitText.setOpaque(false);
		waitText.setForeground(ImagePanel.DEFAULT_FOREGROUND);
		waitPanel.addCenter(waitText);
		waitPanel.addFillEmpty();
		
		if(imagesTableFile != null)
		{
			loadMeasurement(imagesTableFile);
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
		JFileChooser fileChooser = new JFileChooser((String) getClient().getProperties().getProperty(StandardProperty.PROPERTY_LAST_MEASUREMENT_SAVE_FOLDER));
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
		this.imagesTableFile = imagesTableFile;
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				// Load image information from images.csv file.
				ImageFolderNode rootNode;
				try
				{
					rootNode = ImagesFileProcessor.processImagesFile(imagesTableFile);
				}
				catch(Exception e)
				{
					sendErrorMessage("Could not load measurement tree.", e);
					return;
				}
				measurementTree.setRootNode(rootNode);
				showPanel(contentPanel);
			}
		}).start();
	}
	
	public void showImageSeries(File imagesTableFile, ImageFolderNode imageFolder)
	{
		this.imageFolder = imageFolder;
		this.imagesTableFile = imagesTableFile;
		int numImages = imageFolder.getImageList().size();
		imageSelectSlider.setMaximum(numImages);
		imageSelectSlider.setValue(1);
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
		imageSelectSlider.setLabelTable(imageSelectSlider.createStandardLabels(majorDist));
		
		imageSelectField.setMaximalValue(numImages);
		imageSelectField.setValue(1);
		
		selectImageIndex(0);
	}
	private void selectImageIndex(int index)
	{
		if(index < 0)
			index = 0;
		else if(index >= imageFolder.getImageList().size())
			index = imageFolder.getImageList().size()-1;
		if(index < 0) // no images available
			return;
		currentImageIndex = index;
		imageChanging = true;
		imageSelectField.setValue(index+1);
		imageSelectSlider.setValue(index+1);
		imageChanging = false;
		
		String imageURL = imageFolder.getImageList().get(index); 
		File imageFile = new File(imagesTableFile.getParent(), imageURL);
		if(!imageFile.exists() || !imageFile.isFile())
		{
			sendErrorMessage("Cannot load image file " + imageFile.getAbsolutePath() + " because it does not exist.", null);
		}
		BufferedImage image;
		try 
		{
		    image = ImageIO.read(imageFile);
		} 
		catch (IOException e) 
		{
			sendErrorMessage("Loading of image file " + imageFile.getAbsolutePath() + " failed because of I/O errors.", e);
			return;
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
			sendErrorMessage("Could not load image file " + imageFile.getAbsolutePath() + " since image type can not be read.\nSupported image types are: " + imageTypesString +".\nTo support more image types, please download an appropriete plugin for YouScope/Java.", null);
			return;
		}
		imagePanel.setImage(image);
		currentFrameTitle = FRAME_TITLE + " - " + imageFile.getName();
		getContainingFrame().setTitle(currentFrameTitle);
	}
}
