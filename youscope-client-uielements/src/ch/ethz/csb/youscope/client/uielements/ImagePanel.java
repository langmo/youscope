/**
 * 
 */
package ch.ethz.csb.youscope.client.uielements;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.YouScopeFrame;
import ch.ethz.csb.youscope.shared.ImageEvent;
import ch.ethz.csb.youscope.shared.tools.ImageConvertException;
import ch.ethz.csb.youscope.shared.tools.ImageTools;

/**
 * Easy to use resizeable panel to display an image. Provides a convenient method to display the image in a frame.
 * @author Moritz Lang
 *
 */
public class ImagePanel extends JPanel
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 848031651200033010L;
	/**
	 * The current image, or null, if no image arrived yet.
	 */
    private ImageEvent imageEvent = null;
    private HistogramPlot				histogram				= new HistogramPlot();
    private BufferedImage lastBuffered = null;
    private final ControlsPanel controlsPanel;
	private final YouScopeClient client;
	private String noImageText = "No image available yet.";
	private final long VISIBILITY_TIMEOUT = 2000; // 2s
	private final HideTimer hideTimer;
	private final static Color BACKGROUND = new Color(0.3f, 0.3f, 0.3f);
	private boolean autoAdjustContrast = false;
	private static final double ZOOM_IN_STEP = Math.sqrt(2);
	private static final double ZOOM_OUT_STEP = 1/Math.sqrt(2);
	private static final double ZOOM_MAX = 100;
	private double zoom = 1;
	private double centerX = 0.5;
	private double centerY = 0.5;
	private final UserListener userListener;
	private String title = "Image Viewer";
	private final ArrayList<PixelListener> pixelListeners = new ArrayList<PixelListener>();
	private final HistogramControl histogramControl;
	private final ArrayList<Control> controlsList = new ArrayList<Control>(1);
	private YouScopeFrame frame = null;
	private class Control
	{
		final String title;
		final Component component;
		public Control(String title, Component component)
		{
			this.title = title;
			this.component = component;
		}
	}
	
	/**
	 * Information about a pixel in the image.
	 * @author Moritz Lang
	 *
	 */
	public static class PixelInfo implements Serializable
	{
		/**
		 * Serial Version UID.
		 */
		private static final long serialVersionUID = -2739180587131372289L;
		private final int x;
		private final int y;
		private final long value;
		private final double relativeValue;
		/**
		 * Constructor.
		 * @param x x-coordinate of pixel.
		 * @param y y-coordinate of pixel.
		 * @param value absolute value of pixel.
		 * @param relativeValue relative value of pixel.
		 */
		public PixelInfo(int x, int y, long value, double relativeValue)
		{
			this.x = x;
			this.y = y;
			this.value = value;
			this.relativeValue = relativeValue;
		}
		/**
		 * Returns (zero based) x-coordinate of pixel, counted from the left.
		 * @return x-coordinate of pixel.
		 */
		public int getX() {
			return x;
		}
		/**
		 * Returns (zero based) y-coordinate of pixel, counted from the top.
		 * @return y-coordinate of pixel.
		 */
		public int getY() {
			return y;
		}
		/**
		 * Gets absolute value (intensity) of the pixel.
		 * @return absolute value of pixel.
		 */
		public long getValue() {
			return value;
		}
		/**
		 * Gets relative intensity of the pixel, where 0 is black and 1 is white.
		 * @return relative value of pixel.
		 */
		public double getRelativeValue() {
			return relativeValue;
		}
	}
	
	/**
	 * Listener which gets informed about the currently hovered pixel in the image.
	 * @author Moritz Lang
	 *
	 */
	public static interface PixelListener extends EventListener
	{
		/**
		 * Notifies a listener that the currently active (hovered) pixel changed, or that the mouse exited the image.
		 * @param pixel The newly hovered pixel, or null if no pixel is hovered anymore.
		 */
		public void activePixelChanged(PixelInfo pixel);
	}
	/**
	 * Adds a listener which gets informed over which pixels the user hovers with the mouse.
	 * @param listener Pixel listener to add.
	 */
	public void addPixelListener(PixelListener listener)
	{
		synchronized (pixelListeners) 
		{
			pixelListeners.add(listener);
		}
	}
	/**
	 * Removes a previously added pixel listener.
	 * @param listener Pixel listener to remove.
	 */
	public void removePixelListener(PixelListener listener)
	{
		synchronized (pixelListeners) 
		{
			pixelListeners.remove(listener);
		}
	}
	
	private final JPopupMenu imageOptionsPopupMenu = new JPopupMenu()
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -7544222614639996559L;

		{
			Icon zoomInIcon = ImageLoadingTools.getResourceIcon("icons/magnifier-zoom-in.png", "zoom in");
			JMenuItem zoomInItem = new JMenuItem("Zoom In", zoomInIcon);
			zoomInItem.addActionListener(new ActionListener()
			{

				@Override
				public void actionPerformed(ActionEvent e) {
					zoom(userListener.getLastClick(), true);
				}
				
			});
			add(zoomInItem);
			
			Icon zoomOutIcon = ImageLoadingTools.getResourceIcon("icons/magnifier-zoom-out.png", "zoom in");
			JMenuItem zoomOutItem = new JMenuItem("Zoom Out", zoomOutIcon);
			zoomOutItem.addActionListener(new ActionListener()
			{

				@Override
				public void actionPerformed(ActionEvent e) {
					zoom(userListener.getLastClick(), false);
				}
				
			});
			add(zoomOutItem);
			
			addSeparator();
			
			Icon diskIcon = ImageLoadingTools.getResourceIcon("icons/disk.png", "save image");
			JMenuItem saveMenuItem = new JMenuItem("Save Image", diskIcon);
			saveMenuItem.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					saveImage();
				}
			});
			add(saveMenuItem);
		}
	};
	
	private void zoom(MouseEvent e, boolean zoomIn)
	{
		if(e == null)
			return;
		BufferedImage image = ImagePanel.this.lastBuffered;
		if(image == null)
			return;
		if(zoomIn)
		{
			int imageWidth = image.getWidth();
			int imageHeight = image.getHeight();
			
			AffineTransform imageTransform = getTransform(imageWidth, imageHeight);
			Point2D pos;
			try {
				pos = imageTransform.createInverse().transform(new Point(e.getX(), e.getY()), null);
			} catch (@SuppressWarnings("unused") NoninvertibleTransformException e1) {
				// will not happen.
				return;
			}
			centerX = pos.getX() / imageWidth;
			if(centerX < 0)
				centerX = 0;
			else if(centerX > 1)
				centerX = 1;
			centerY = pos.getY() / imageHeight;
			if(centerY < 0)
				centerY = 0;
			else if(centerY > 1)
				centerY = 1;
			
			zoom*=ZOOM_IN_STEP;
			if(zoom > ZOOM_MAX)
				zoom = ZOOM_MAX;
		}
		else
		{
			zoom*=ZOOM_OUT_STEP;
			if(zoom < 1)
				zoom = 1;
		}
		repaint();
	}
	
	/**
	 * Sets if the contrast should be automatically adjusted whenever a new image is set.
	 * @param autoAdjustContrast True if automatic adjustment should be activated.
	 */
	public void setAutoAdjustContrast(boolean autoAdjustContrast)
	{
		this.autoAdjustContrast = autoAdjustContrast;
		histogramControl.autoAdjustField.setSelected(autoAdjustContrast);
	}
	/**
	 * Returns if the contrast should be automatically adjusted whenever a new image is set.
	 * @return True if automatic adjustment should be activated.
	 */
	public boolean isAutoAdjustContrast()
	{
		return autoAdjustContrast;
	}
	
	private class HistogramControl  extends DynamicPanel
	{
		/**
		 * Serial Version UID.
		 */
		private static final long serialVersionUID = -4695291153764223394L;
		JCheckBox autoAdjustField = new JCheckBox("auto-adjust contrast", autoAdjustContrast);
		public HistogramControl()
		{
			JPanel contrastButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			contrastButtonsPanel.setOpaque(false);
			JButton adjustButton = new JButton("auto");
			adjustButton.setOpaque(false);
			adjustButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e) {
					histogram.adjust();
					fireImageChanged();
				}
			});
			contrastButtonsPanel.add(adjustButton);
			JButton noAdjustButton = new JButton("original");
			noAdjustButton.setOpaque(false);
			noAdjustButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e) {
					histogram.setMinMax(0, 1);
					fireImageChanged();
				}
			});
			contrastButtonsPanel.add(noAdjustButton);
			
			add(histogram);
			add(contrastButtonsPanel);
			autoAdjustField.setOpaque(false);
			add(autoAdjustField);
			autoAdjustField.addActionListener(new ActionListener() 
			{	
				@Override
				public void actionPerformed(ActionEvent e) {
					autoAdjustContrast = autoAdjustField.isSelected();
					if(autoAdjustContrast)
					{
						histogram.adjust();
						fireImageChanged();
					}
				}
			});
			autoAdjustField.setForeground(Color.WHITE);
			autoAdjustField.setVisible(false);
		}
	}
	
	/**
	 * Set to true to display a checkbox with which the user can choose on him/herself if the contrast should be automatically adjusted when a new
	 * image arrives. Default is false. This does only make sense if it can be expected that more than one image can arrive at all.
	 * @param userChooses True to display checkbox.
	 */
	public void setUserChoosesAutoAdjustContrast(boolean userChooses)
	{
		histogramControl.autoAdjustField.setVisible(userChooses);
	}
	
	/**
	 * Adds a component with the given title to the end of the menu containing all controls.
	 * @param title Title of the control.
	 * @param component The control element.
	 */
	public void addControl(String title, Component component)
	{
		synchronized(controlsList)
		{
			controlsList.add(new Control(title, component));
		}
		controlsPanel.revalidateControls();
	}
	
	/**
	 * Adds a component with the given title to the menu containing all controls at the given index.
	 * @param title Title of the control.
	 * @param component The control element.
	 * @param index index where to add control. Must be greater or equal to 0, and smaller or equal to {@link #getNumControls()}.
	 * @throws IndexOutOfBoundsException Thrown if index is invalid.
	 */
	public void insertControl(String title, Component component, int index) throws IndexOutOfBoundsException
	{
		synchronized(controlsList)
		{
			controlsList.add(index, new Control(title, component));
		}
		controlsPanel.revalidateControls();
	}
	
	/**
	 * Returns the number of controls in the menu containing all controls.
	 * @return number of controls
	 */
	public int getNumControls()
	{
		return controlsList.size();
	}
	
	/**
	 * Removes the control at the given index.
	 * @param index index of the control. Must be greater or equal to 0, and smaller than {@link #getNumControls()}.
	 * @throws IndexOutOfBoundsException Thrown if index is invalid.
	 */
	public void removeControl(int index) throws IndexOutOfBoundsException
	{
		synchronized(controlsList)
		{
			controlsList.remove(index);
		}
		controlsPanel.revalidateControls();
	}
	/**
	 * Removes a previously added control. Does nothing if control does not exist.
	 * @param component The control to remove.	 
	 */
	public void removeControl(Component component)
	{
		int index = -1;
		synchronized(controlsList)
		{
			for(int i=0; i<controlsList.size(); i++)
			{
				Control control = controlsList.get(i);
				if(control.component.equals(component))
				{
					index = i;
					break;
				}
			}
		}
		if(index != -1)
			removeControl(index);
	}
	/**
	 * Removes all added controls.
	 */
	public void clearControls()
	{
		synchronized(controlsList)
		{
			if(controlsList.size() <= 0)
				return;
			controlsList.clear();
		}
		controlsPanel.revalidateControls();
	}
	
	private class ControlsPanel extends DynamicPanel
	{
		/**
		 * Serial Version UID.
		 */
		private static final long serialVersionUID = 8492907052808833994L;
		private final Color CONTROLS_BACKGROUND = new Color(BACKGROUND.getRed(), BACKGROUND.getGreen(), BACKGROUND.getBlue(), 200);
		ControlsPanel()
		{
			setOpaque(false);
			setVisible(false);
			setBorder(new EmptyBorder(10,3,10,3));
			revalidateControls();
			setPreferredSize(new Dimension(150, 500));
		}
		void revalidateControls()
		{
			Runnable runner = new Runnable()
			{
				@Override
				public void run() 
				{
					synchronized(controlsList)
					{
						removeAll();
						for(Control control : controlsList)
						{
							// add panel for border.
							JPanel containingPanel = new JPanel(new BorderLayout());
							containingPanel.setOpaque(false);
							containingPanel.add(control.component);
							TitledBorder border = new TitledBorder(new LineBorder(Color.WHITE, 1), control.title);
							border.setTitleColor(Color.WHITE);
							containingPanel.setBorder(border);
							add(containingPanel);
						}
						addFillEmpty();
						revalidate();
					}
				}
			};
			if(SwingUtilities.isEventDispatchThread())
				runner.run();
			else
				SwingUtilities.invokeLater(runner);
			
		}
		@Override
	    public void paintComponent(Graphics g)
	    {
			g.setColor(CONTROLS_BACKGROUND);
			g.fillRect(0, 0, getWidth(), getHeight());
			g.setColor(Color.BLACK);
			g.drawLine(0, 0, 0, getHeight());
			super.paintComponent(g);
	    }
	}
	
	private class HideTimer implements Runnable
	{
		private volatile long lastAction = 0;
		private volatile boolean running = false;
		@Override
		public void run() 
		{
			while(true)
			{
				long lastAction;
				synchronized(this)
				{
					lastAction = this.lastAction;
				}
				long currentTime = System.currentTimeMillis();
				if(lastAction == Long.MAX_VALUE)
				{
					try 
					{
						Thread.sleep(VISIBILITY_TIMEOUT);
					} 
					catch (@SuppressWarnings("unused") InterruptedException e) 
					{
						// Somebody wants us to stop.
						synchronized(this)
						{
							running = false;
							return;
						}
					}
				}
				else if(currentTime - lastAction < VISIBILITY_TIMEOUT)
				{
					try 
					{
						Thread.sleep(VISIBILITY_TIMEOUT-currentTime+lastAction+10);
					} 
					catch (@SuppressWarnings("unused") InterruptedException e) 
					{
						// Somebody wants us to stop.
						synchronized(this)
						{
							running = false;
							return;
						}
					}
				}
				synchronized(this)
				{
					lastAction = this.lastAction;
					if(System.currentTimeMillis()-lastAction >= VISIBILITY_TIMEOUT)
					{
						SwingUtilities.invokeLater(new Runnable()
						{
							@Override
							public void run() 
							{
								controlsPanel.setVisible(false);
							}
						});
						running = false;
						return;
					}
				}
			}
		}
		
		synchronized void userAction(MouseEvent mouseEvent)
		{
			if(mouseEvent != null)
			{
				if(!controlsPanel.isVisible())
				{
					controlsPanel.setVisible(true);
				}
			}
			if(mouseEvent != null && mouseEvent.getX() >= getWidth() - controlsPanel.getPreferredSize().getWidth())
				lastAction = Long.MAX_VALUE;
			else
				lastAction = System.currentTimeMillis();
			if(running)
				return;
			running = true;
			try
			{
				new Thread(this, "Controls hide thread").start();
			}
			catch(@SuppressWarnings("unused") IllegalThreadStateException e)
			{
				running = false;
			}
		}
	}
	private void calculateHoveredPixel(MouseEvent mouseEvent)
	{
		BufferedImage image = ImagePanel.this.lastBuffered;
		ImageEvent imageEvent = ImagePanel.this.imageEvent;
		if(mouseEvent == null || image == null || imageEvent == null)
			return;
		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();
			
		AffineTransform imageTransform = getTransform(imageWidth, imageHeight);
		Point2D pos;
		try 
		{
			pos = imageTransform.createInverse().transform(new Point(mouseEvent.getX(), mouseEvent.getY()), null);
		} catch (@SuppressWarnings("unused") NoninvertibleTransformException e1) {
			// will not happen.
			return;
		}
		
		Point orgCoord = ImageTools.backTransformCoordinate(imageEvent, new Point((int)pos.getX(), (int)pos.getY()));
		PixelInfo pixelInfo;
		if(orgCoord != null)
		{
			long pixelValue = ImageTools.getPixelValue(imageEvent, orgCoord.x, orgCoord.y);
			pixelInfo = new PixelInfo((int)pos.getX(), (int)pos.getY(), pixelValue, ((double)pixelValue)/imageEvent.getMaxIntensity());
		}
		else
			pixelInfo = null;
		
		synchronized (pixelListeners) 
		{
			for(PixelListener listener : pixelListeners)
			{
				listener.activePixelChanged(pixelInfo);
			}
		}
	}
	private class UserListener implements MouseListener, MouseMotionListener, MouseWheelListener
	{
		private MouseEvent lastClick = null;
		@Override
		public void mouseDragged(MouseEvent mouseEvent) {
			hideTimer.userAction(mouseEvent);
			calculateHoveredPixel(mouseEvent);
		}

		@Override
		public void mouseMoved(MouseEvent mouseEvent) {
			hideTimer.userAction(mouseEvent);
			calculateHoveredPixel(mouseEvent);
		}
		
		MouseEvent getLastClick()
		{
			return lastClick;
		}

		@Override
		public void mouseClicked(MouseEvent mouseEvent) {
			hideTimer.userAction(mouseEvent);
		}

		@Override
		public void mouseEntered(MouseEvent mouseEvent) {
			hideTimer.userAction(mouseEvent);
		}

		@Override
		public void mouseExited(MouseEvent mouseEvent) 
		{
			if(!(mouseEvent.getX() < getWidth() && mouseEvent.getX() > 0)
					||!(mouseEvent.getY() < getHeight() && mouseEvent.getY() > 0))
            {
				hideTimer.userAction(null);
            }
			else
				hideTimer.userAction(mouseEvent);
		}

		@Override
		public void mousePressed(MouseEvent mouseEvent) {
			hideTimer.userAction(mouseEvent);
			lastClick = mouseEvent;
		}

		@Override
		public void mouseReleased(MouseEvent mouseEvent) {
			hideTimer.userAction(mouseEvent);
		}
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) 
		{
			int direction = e.getWheelRotation();
			if(direction == 0)
				return;
			boolean zoomIn = direction < 0;
			
			
			zoom(e, zoomIn);
		}
	}
	
	/**
	 * Constructor.
	 * @param client YouScope client.
	 */
	public ImagePanel(YouScopeClient client)
    {
		super(null);
		setOpaque(true);
		this.client = client;
		controlsPanel = new ControlsPanel();
		histogramControl = new HistogramControl();
		addControl("Contrast", histogramControl);
		hideTimer = new HideTimer();
		add(controlsPanel);
		histogram.setAutoAdjusting(false);
		histogram.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				fireImageChanged();
			}
		});
		setComponentPopupMenu(imageOptionsPopupMenu);
		addComponentListener(new ComponentAdapter() 
		{
			@Override
			public void componentResized(ComponentEvent e) 
			{
				int controlsWidth = (int) controlsPanel.getPreferredSize().getWidth();
				controlsPanel.setBounds(getWidth()-controlsWidth, 0, controlsWidth, getHeight());
			}
		});
		userListener = new UserListener();
		addMouseListener(userListener);
		addMouseMotionListener(userListener);
		addMouseWheelListener(userListener);
    }
	
	synchronized void fireImageChanged()
	{
		lastBuffered = null;
		repaint();
	}
	
	double getDefaultScale(int imageOrgWidth, int imageOrgHeight)
	{
		double width = getWidth();
		double height = getHeight();
		if (width / imageOrgWidth > height / imageOrgHeight)
	    {
	        return height / imageOrgHeight;
	    }
		return width / imageOrgWidth;
	}
	
	AffineTransform getTransform(int imageOrgWidth, int imageOrgHeight)
	{
		double defaultScale = getDefaultScale(imageOrgWidth, imageOrgHeight);
		double width = getWidth();
		double height = getHeight();
		double imageWidth = defaultScale*imageOrgWidth;
		double imageHeight = defaultScale*imageOrgHeight;
		double scaledImageWidth = imageWidth * zoom;
		double scaledImageHeight = imageHeight * zoom;
		
		double deltaX;
		if(scaledImageWidth <= width)
			deltaX = (width-scaledImageWidth) / 2;
		else
		{
			deltaX = width/2 - centerX * scaledImageWidth;
			if(deltaX > 0)
				deltaX = 0;
			else if(deltaX < width-scaledImageWidth)
				deltaX = width-scaledImageWidth;
		}
		
		double deltaY;
		if(scaledImageHeight <= height)
			deltaY = (height-scaledImageHeight) / 2;
		else
		{
			deltaY = height/2 - centerY * scaledImageHeight;
			if(deltaY > 0)
				deltaY = 0;
			else if(deltaY < height-scaledImageHeight)
				deltaY = height-scaledImageHeight;
		}
	
		AffineTransform scale = AffineTransform.getScaleInstance(defaultScale*zoom, defaultScale*zoom);
		AffineTransform move = AffineTransform.getTranslateInstance(deltaX, deltaY);
		move.concatenate(scale);
		return move;
	}
	
	@Override
    public void paintComponent(Graphics grp)
    {
		grp.setColor(BACKGROUND);
		grp.fillRect(0, 0, getWidth(), getHeight());
		ImageEvent imageEvent;
		BufferedImage image;
		synchronized(this)
		{
			imageEvent = this.imageEvent;
			if(imageEvent == null)
				lastBuffered = null;
			else if(lastBuffered == null)
	        {
	        	double[] minMax = histogram.getMinMax();
	        	try {
	        		lastBuffered = ImageTools.getScaledMicroscopeImage(imageEvent, (float)minMax[0], (float)minMax[1]);
				} catch (ImageConvertException e) {
					client.sendError("Could not generate image.", e);
					noImageText = "Error painting image.";
					lastBuffered = null;
				}
	        }
			image = lastBuffered;
		}
        if (image != null)
        {
		    Graphics2D g2D = (Graphics2D) grp;
		    int imageWidth = image.getWidth(this);
		    int imageHeight = image.getHeight(this);
		    
		    g2D.drawImage(image, getTransform(imageWidth, imageHeight), this);
		    
        }
        else if(noImageText != null)
        {
        	grp.setColor(Color.WHITE);
        	grp.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        	int strWidth = grp.getFontMetrics().stringWidth(noImageText);
        	int strHeight = grp.getFontMetrics().getHeight();
        	grp.drawString(noImageText, (getWidth()-strWidth)/2, (getHeight()-strHeight)/2);
        }
    }
	
	/**
	 * Sets the text which should be displayed if no image is available, yet. Set to null to not display any text.
	 * @param noImageText Text to display if no images are available.
	 */
	public void setNoImageText(String noImageText)
	{
		this.noImageText = noImageText;
	}
	private void saveImage()
	{
		// make local copy of image.
		BufferedImage image;
		try {
			image = ImageTools.getMicroscopeImage(imageEvent);
		} catch (ImageConvertException e1) {
			client.sendError("Could not parse image.", e1);
			return;
		}
		
		// Let user select file to save to
        String lastFile = "image.tif";
        JFileChooser fileChooser = new JFileChooser(lastFile);
        Thread.currentThread().setContextClassLoader(ImagePanel.class.getClassLoader());
        ImageIO.scanForPlugins();
        String[] imageFormats = ImageIO.getWriterFileSuffixes();
        FileFilter tifFilter = null;
        fileChooser.setAcceptAllFileFilterUsed(false);
        for(int i=0; i<imageFormats.length; i++)
        {
        	if(imageFormats[i] == null || imageFormats[i].length() <= 0)
        		continue;
        	
        	FileNameExtensionFilter filter = new FileNameExtensionFilter(imageFormats[i].toUpperCase() + " Images (."+imageFormats[i].toLowerCase()+")", new String[]{imageFormats[i]});
        	fileChooser.addChoosableFileFilter(filter);
        	if(imageFormats[i].compareToIgnoreCase("tif") == 0)
        		tifFilter = filter;
        }
        if(tifFilter != null)
        {
        	fileChooser.setFileFilter(tifFilter);
        	fileChooser.setSelectedFile(new File("Image.tif"));
        }
                                               
        File file;
        ImageWriter imageWriter;
        while(true)
        {
        	int returnVal = fileChooser.showDialog(null, "Save");
        	if (returnVal != JFileChooser.APPROVE_OPTION)
        	{
        		return;
        	}
        	file = fileChooser.getSelectedFile().getAbsoluteFile();
        	if(file.exists())
        	{
        		returnVal = JOptionPane.showConfirmDialog(null, "Image " + file.toString() + " does already exist.\nOverwrite?", "Image file does already exist", JOptionPane.YES_NO_OPTION);
        		if(returnVal != JOptionPane.YES_OPTION)
        			continue;
        	}
        	
        	FileFilter selectedFilter = fileChooser.getFileFilter();
        	String fileSuffix;
        	if(selectedFilter == null || !(selectedFilter instanceof FileNameExtensionFilter) || ((FileNameExtensionFilter)selectedFilter).getExtensions().length != 1)
        	{
            	String fileName = file.getPath();
                int idx = fileName.lastIndexOf('.');
                if(idx < 0)
                {
                	JOptionPane.showMessageDialog(null, "File " + fileName + " does not have a valid file type ending.", "File has invalid file type", JOptionPane.ERROR_MESSAGE);
            		continue;
                }
                fileSuffix = fileName.substring(idx+1);
        	}
        	else
        	{
        		fileSuffix = ((FileNameExtensionFilter)selectedFilter).getExtensions()[0]; 
        	}
            Iterator<ImageWriter> imageIterator = ImageIO.getImageWritersBySuffix(fileSuffix);
            if(!imageIterator.hasNext())
            {
            	JOptionPane.showMessageDialog(null, "YouScope does not have a plug-in installed to support saving images with file type \"" + fileSuffix + "\".", "Image file type not supported", JOptionPane.ERROR_MESSAGE);
        		continue;
            }
            imageWriter = imageIterator.next();
            break;
        }
        
        ImageOutputStream ios = null;
        try
		{
        	ios = ImageIO.createImageOutputStream(file);
            imageWriter.setOutput(ios);
            imageWriter.write(image);
		}
		catch(IOException e)
		{
			client.sendError("Image " + file.getPath() + " could not be saved.",e);
			return;
		}
        finally
        {
        	if(ios != null)
        	{
            	try
				{
					ios.close();
				}
				catch(@SuppressWarnings("unused") IOException e)
				{
					// do nothing.
				}
        	}
        }
	}
	
	/**
	 * Convenient method to show this panel in a frame. The frame is not made visible and has a default title.
	 * @return The frame containing this panel.
	 */
	public YouScopeFrame toFrame()
	{
		if(frame != null)
			return frame;
		frame = client.createFrame();
		frame.setTitle(title);
		frame.setResizable(true);
		frame.setClosable(true);
		frame.setMaximizable(true);
		frame.setMargins(0,0,0,0);		
        frame.setContentPane(this);
        frame.setSize(new Dimension(800, 600));
        
        addPixelListener(new PixelListener()
		{
			@Override
			public void activePixelChanged(PixelInfo pixel) {
				if(pixel == null)
					frame.setTitle(title);
				else
				{
					String text = title + " - ";
					text+="X="+Integer.toString(pixel.getX()+1)+" Y="+Integer.toString(pixel.getY()+1)+" I=";
					text+=Long.toString(pixel.getValue())+ " ("+Long.toString(Math.round(pixel.getRelativeValue()*100))+"%)";
					frame.setTitle(text);
				}
			}
		});
        
		return frame;
	}
	
	/**
	 * Sets the image which should be displayed.
	 * @param imageEvent Image which should be displayed.
	 */
	public void setImage(ImageEvent imageEvent)
	{
		int[][] bins;
		try
		{
			bins = ImageTools.getHistogram(imageEvent, (int)controlsPanel.getPreferredSize().getWidth());
			histogram.setBins(bins);
			if(autoAdjustContrast)
				histogram.adjust();
		}
		catch(ImageConvertException e)
		{
			client.sendError("Could not calculate histogram of image.", e);
		}
		this.imageEvent = imageEvent;
		fireImageChanged();
	}
	/**
	 * Sets the title of the frame, which is opened by {@link #toFrame()}. Has no effect when using this panel directly.
	 * @param title Title of the frame.
	 */
	public void setTitle(String title)
	{
		this.title = title;
		if(frame != null)
			frame.setTitle(title);
	}
}
