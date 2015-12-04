/**
 * 
 */
package org.youscope.plugin.measurementviewer;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.uielements.StandardFormats;

/**
 * @author Moritz Lang
 *
 */
class ImageSeriesPanel extends JPanel
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -7457257407427813213L;
	private final ImageFolderNode imageFolder;
	private final ImageField currentImageField = new ImageField(null);
	private final JSlider	imageSelectSlider;
	private final JFormattedTextField	imageSelectField	= new JFormattedTextField(StandardFormats.getIntegerFormat());
	private final YouScopeClient client;
	private final File measurementFolder;
	private final int numImages;
	private boolean imageChanging = false;
	ImageSeriesPanel(YouScopeClient client, File measurementFolder, ImageFolderNode imageFolder)
	{
		this.imageFolder = imageFolder;
		this.client = client;
		this.measurementFolder = measurementFolder;
		this.numImages = imageFolder.getImageList().size();
	
		setOpaque(false);
		setLayout(new BorderLayout());
		imageSelectSlider = new JSlider(1, numImages, 1);
		imageSelectSlider.setOpaque(false);
		imageSelectSlider.setPaintTicks(true);
		imageSelectSlider.setSnapToTicks(true);
		// Paint approximately 10 major ticks + labels
		int majorDist = (int)Math.round(numImages / 100.0) * 10;
		if(majorDist <= 1)
		{
			// Less then 15 elements, paint them all as major
			imageSelectSlider.setMajorTickSpacing(1);
		}
		else
		{
			imageSelectSlider.setMajorTickSpacing(majorDist);
			int lowerDist = majorDist / 5;
			if(lowerDist <= 0)
				lowerDist = 1;
			imageSelectSlider.setMinorTickSpacing(lowerDist);
		}
		imageSelectSlider.setPaintLabels(true);
		
		imageSelectSlider.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent arg0)
			{
				
				if(imageChanging)
					return;
				imageChanging = true;
				int imageNo = imageSelectSlider.getValue();
				imageSelectField.setValue(imageNo);
				selectImageIndex(imageNo - 1);
				imageChanging = false;
			}
		});
		
		imageSelectField.setOpaque(false);
		imageSelectField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(imageChanging)
					return;
				imageChanging = true;
				int imageNo = ((Number)imageSelectField.getValue()).intValue();
				if(imageNo < 1)
					imageNo = 1;
				if(imageNo > numImages)
					imageNo = numImages;
				imageSelectField.setValue(imageNo);
				imageSelectSlider.setValue(imageNo);
				selectImageIndex(imageNo - 1);	
				imageChanging = false;
			}
		});
		imageSelectField.setValue(1);
		selectImageIndex(0);
		
		JPanel controlsPanel = new JPanel(new BorderLayout());
		controlsPanel.setBorder(new TitledBorder("Image Number Selection"));
		controlsPanel.setOpaque(false);
		JPanel imageIndexPanel = new JPanel(new GridLayout(1, 2, 2, 2));
		imageIndexPanel.setOpaque(false);
		imageIndexPanel.add(new JLabel("Select image index:"));
		imageIndexPanel.add(imageSelectField);
		controlsPanel.add(imageIndexPanel, BorderLayout.NORTH);
		controlsPanel.add(imageSelectSlider, BorderLayout.CENTER);
		
		JPanel imagePanel = new JPanel(new BorderLayout());
		imagePanel.setOpaque(false);
		imagePanel.add(currentImageField);
		imagePanel.setBorder(new TitledBorder("Selected Image"));
		
		add(controlsPanel, BorderLayout.SOUTH);
		add(imagePanel, BorderLayout.CENTER);
	}
	
	private void selectImageIndex(int index)
	{
		String imageURL = imageFolder.getImageList().get(index);
		File imageFile = new File(measurementFolder, imageURL);
		if(!imageFile.exists() || !imageFile.isFile())
		{
			client.sendError("Cannot load image file " + imageFile.getAbsolutePath() + " because it does not exist.");
		}
		BufferedImage image;
		try 
		{
		    image = ImageIO.read(imageFile);
		} 
		catch (IOException e) 
		{
			client.sendError("Loading of image file " + imageFile.getAbsolutePath() + " failed because of I/O errors.", e);
			return;
		}
		if(image == null)
		{
			String[] supportedImageTypes = ImageIO.getWriterFormatNames();
			String imageTypesString = "";
			for(String supportedImageType : supportedImageTypes)
			{
				if(imageTypesString.length() > 0)
					imageTypesString += ", ";
				imageTypesString += supportedImageType;
			}
			client.sendError("Could not load image file " + imageFile.getAbsolutePath() + " since image type can not be read.\nSupported image types are: " + imageTypesString +".\nTo support more image types, please download an appropriete plugin for YouScope/Java.");
			return;
		}
		currentImageField.setImage(image);
	}
	
	private class ImageField extends JComponent
    {
        /**
         * Serial Version UID.
         */
        private static final long serialVersionUID = 3857511173912009525L;

        private BufferedImage image = null;
        
        ImageField(BufferedImage image)
        {
        	this.image = image;
        }
        public synchronized void setImage(BufferedImage image)
        {
        	this.image = image;
        	repaint();
        }
        @Override
		public synchronized void paintComponent(Graphics grp)
        {
            Graphics2D g2D = (Graphics2D) grp;

            if (image == null)
            {
                return;
            }

            double imageWidth = image.getWidth(this);
            double imageHeight = image.getHeight(this);
            if (getWidth() / imageWidth > getHeight() / imageHeight)
            {
                imageWidth = imageWidth * getHeight() / imageHeight;
                imageHeight = getHeight();
            } else
            {
                imageHeight = imageHeight * getWidth() / imageWidth;
                imageWidth = getWidth();
            }

            // draw the image
            g2D.drawImage(image, (int) (getWidth() - imageWidth) / 2,
                    (int) (getHeight() - imageHeight) / 2, (int) imageWidth, (int) imageHeight,
                    this);
        }
    }
}
