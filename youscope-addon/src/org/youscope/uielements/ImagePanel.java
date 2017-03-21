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
package org.youscope.uielements;

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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.youscope.clientinterfaces.StandardProperty;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.clientinterfaces.YouScopeFrameListener;
import org.youscope.clientinterfaces.PropertyProvider;
import org.youscope.common.image.ImageEvent;
import org.youscope.common.util.ImageConvertException;
import org.youscope.common.util.ImageTools;

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
	 * The current image--independently if drawn or not-- or null, if no image arrived, yet.
	 */
    private ImageEvent<?> orgImageEvent = null;
    private BufferedImage orgBufferedImage = null;
    
    /**
     * The last drawn image, or null, if no image was drawn, yet.
     */
    private ImageEvent<?> lastDrawnOrgImageEvent = null;
    private BufferedImage lastDrawnOrgBufferedImage = null;
    private BufferedImage lastDrawnRenderedImage = null;
    
    /**
     * Temporary place to save the rendered image. 
     */
    private BufferedImage renderedImage = null;
    
    private final Object imageLock = new Object();
        
    protected final HistogramPanel histogramPanel = new HistogramPanel();
    private final ControlsPanel controlsPanel;
	private final YouScopeClient client;
	private String noImageText = "No image available yet.";
	private final long VISIBILITY_TIMEOUT = 2000; // 2s
	private final HideTimer hideTimer;
	private boolean userChoosesAutoAdjustContrast = false;
	/**
	 * Default background color of the image panel.
	 */
	public final static Color DEFAULT_BACKGROUND = new Color(0.3f, 0.3f, 0.3f);
	/**
	 * Default foreground (text) color of the image panel.
	 */
	public final static Color DEFAULT_FOREGROUND = new Color(1f, 1f, 1f);
	
	/**
	 * Default color of lines in image to measure distances.
	 */
	public final static Color DEFAULT_DRAW_LINE_COLOR = new Color(0.8f, 0f, 0f);
	
	/**
	 * Drawing of lines, e.g. to measure distances
	 */
	private Point startLine = null;
	private Point endLine = null;
	
	private static final double ZOOM_IN_STEP = Math.sqrt(2);
	private static final double ZOOM_OUT_STEP = 1/Math.sqrt(2);
	/**
	 * Maximal zoom level.
	 */
	public static final double ZOOM_MAX = 100;
	private double zoom = 1;
	private double centerX = 0.5;
	private double centerY = 0.5;
	private final UserListener userListener;
	private String title = "Image Viewer";
	private final ArrayList<PixelListener> pixelListeners = new ArrayList<PixelListener>();
	private final ArrayList<LineListener> lineListeners = new ArrayList<LineListener>();
	private final ArrayList<ZoomAndCenterListener> zoomAndCenterListeners = new ArrayList<>();
	private final HistogramControl histogramControl;
	private final ArrayList<Control> controlsList = new ArrayList<Control>(1);
	protected YouScopeFrame frame = null;
	private final LineInfoPopup lineInfoPopup = new LineInfoPopup();
	private class LineInfoPopup extends DynamicPanel 
	{
		/**
		 * Serial Version UID
		 */
		private static final long serialVersionUID = -1259165079716663081L;
		private final JLabel dxLabel = new JLabel("dx=+XXXX px");
		private final JLabel dyLabel = new JLabel("dy=+XXXX px");
		private final JLabel dlLabel = new JLabel("dl=XXXX.XX px");
		LineInfoPopup()
		{
			setOpaque(true);
			setBackground(DEFAULT_BACKGROUND);
			dxLabel.setOpaque(false);
			dyLabel.setOpaque(false);
			dlLabel.setOpaque(false);
			
			dxLabel.setForeground(DEFAULT_FOREGROUND);
			dyLabel.setForeground(DEFAULT_FOREGROUND);
			dlLabel.setForeground(DEFAULT_FOREGROUND);
			
			Font font = new Font("Monospaced", Font.BOLD, 10);
			dxLabel.setFont(font);
			dyLabel.setFont(font);
			dlLabel.setFont(font);
			
			add(dxLabel);
			add(dyLabel);
			add(dlLabel);
			
			setBorder(new CompoundBorder(new LineBorder(DEFAULT_FOREGROUND, 1), new EmptyBorder(2,2,2,2)));
		}
		public void setLine(int x1, int y1, int x2, int y2)
		{
			dxLabel.setText(String.format("\u0394x: %+6dpx", x2-x1));
			dyLabel.setText(String.format("\u0394y: %+6dpx", y2-y1));
			dlLabel.setText(String.format("\u0394l: %6.2fpx", Math.sqrt(Math.pow(x1-x2, 2)+Math.pow(y1-y2, 2))));
		}
	} 
	
	
	
	private class Control
	{
		final String title;
		final Component component;
		boolean fill;
		public Control(String title, Component component, boolean fill)
		{
			this.title = title;
			this.component = component;
			this.fill = fill;
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
	 * Information about a drawn line in the image.
	 * @author Moritz Lang
	 *
	 */
	public static class LineInfo implements Serializable
	{
		/**
		 * Serial Version UID.
		 */
		private static final long serialVersionUID = -1739180587131372289L;
		private final int x1;
		private final int y1;
		private final int x2;
		private final int y2;
		/**
		 * Constructor.
		 * @param x1 x-coordinate of start pixel.
		 * @param y1 y-coordinate of start pixel.
		 * @param x2 x-coordinate of end pixel.
		 * @param y2 y-coordinate of end pixel.
		 */
		public LineInfo(int x1, int y1, int x2, int y2)
		{
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
		}
		/**
		 * Returns (zero based) x-coordinate of the start pixel, counted from the left.
		 * Note: If the original image is an {@link ImageEvent}, the returned coordinate might have to be backtransformed into the original
		 * image coordinate system using e.g. {@link ImageTools#backTransformCoordinate(ImageEvent, Point)}.
		 * @return x-coordinate of start pixel.
		 */
		public int getX1() {
			return x1;
		}
		/**
		 * Returns (zero based) y-coordinate of the start pixel, counted from the top.
		 * Note: If the original image is an {@link ImageEvent}, the returned coordinate might have to be backtransformed into the original
		 * image coordinate system using e.g. {@link ImageTools#backTransformCoordinate(ImageEvent, Point)}.
		 * @return y-coordinate of start pixel.
		 */
		public int getY1() {
			return y1;
		}
		/**
		 * Returns (zero based) x-coordinate of the end pixel, counted from the left.
		 * Note: If the original image is an {@link ImageEvent}, the returned coordinate might have to be backtransformed into the original
		 * image coordinate system using e.g. {@link ImageTools#backTransformCoordinate(ImageEvent, Point)}.
		 * @return x-coordinate of end pixel.
		 */
		public int getX2() {
			return x2;
		}
		/**
		 * Returns (zero based) y-coordinate of the end pixel, counted from the top.
		 * Note: If the original image is an {@link ImageEvent}, the returned coordinate might have to be backtransformed into the original
		 * image coordinate system using e.g. {@link ImageTools#backTransformCoordinate(ImageEvent, Point)}.
		 * @return y-coordinate of end pixel.
		 */
		public int getY2() {
			return y2;
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
	 * Listener which gets informed about a currently drawn line in the image.
	 * @author Moritz Lang
	 *
	 */
	public static interface LineListener extends EventListener
	{
		/**
		 * Notifies a listener that the currently drawn line has changed.
		 * @param line The drawn line, or null if line drawing stopped.
		 */
		public void lineChanged(LineInfo line);
	}
	
	/**
	 * Listener which gets informed if the current zoom or the center of the displayed image changed.
	 * @author Moritz Lang
	 *
	 */
	public static interface ZoomAndCenterListener extends EventListener
	{
		/**
		 * Notifies a listener that the zoom level or the center changed.
		 * @param zoom zoom level. Between 1 and {@link ImagePanel#ZOOM_MAX}.
		 * @param centerX Center of displayed image in x direction. Between 0 and 1.
		 * @param centerY Center of displayed image in y direction. Between 0 and 1.
		 */
		public void zoomOrCenterChanged(double zoom, double centerX, double centerY);
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
	
	/**
	 * Adds a listener which gets informed over lines drawn in the image.
	 * @param listener Line listener to add.
	 */
	public void addLineListener(LineListener listener)
	{
		synchronized (lineListeners) 
		{
			lineListeners.add(listener);
		}
	}
	/**
	 * Removes a previously added line listener.
	 * @param listener Line listener to remove.
	 */
	public void removeLineListener(LineListener listener)
	{
		synchronized(lineListeners) 
		{
			lineListeners.remove(listener);
		}
	}
	
	/**
	 * Adds a listener which gets informed if the current zoom level or the center of the displayed area changed.
	 * @param listener Listener to add.
	 */
	public void addZoomAndCenterListener(ZoomAndCenterListener listener)
	{
		synchronized (zoomAndCenterListeners) 
		{
			zoomAndCenterListeners.add(listener);
		}
	}
	/**
	 * Removes a previously added listener.
	 * @param listener listener to remove.
	 */
	public void removeZoomAndCenterListener(ZoomAndCenterListener listener)
	{
		synchronized(zoomAndCenterListeners) 
		{
			zoomAndCenterListeners.remove(listener);
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
		BufferedImage image = ImagePanel.this.lastDrawnRenderedImage;
		if(image == null)
			return;
		double centerX = this.centerX;
		double centerY = this.centerY;
		double zoom = this.zoom;
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
			centerY = pos.getY() / imageHeight;
			
			zoom*=ZOOM_IN_STEP;
		}
		else
		{
			zoom*=ZOOM_OUT_STEP;
		}
		setZoomAndCenter(zoom, centerX, centerY);
	}
	/**
	 * Sets the current zoom level and the center of the display area.
	 * @param zoom zoom level. Between 1 and {@link #ZOOM_MAX}.
	 * @param centerX x-position of center of displayed area. Between 0 and 1.
	 * @param centerY y-position of center of displayed area. Between 0 and 1.
	 */
	public void setZoomAndCenter(double zoom, double centerX, double centerY)
	{
		if(centerX < 0)
			centerX = 0;
		else if(centerX > 1)
			centerX = 1;
		if(centerY < 0)
			centerY = 0;
		else if(centerY > 1)
			centerY = 1;
		if(zoom > ZOOM_MAX)
			zoom = ZOOM_MAX;
		else if(zoom < 1)
			zoom = 1;
		this.zoom = zoom;
		this.centerX = centerX;
		this.centerY = centerY;
		repaint();
		
		synchronized(zoomAndCenterListeners)
		{
			for(ZoomAndCenterListener listener : zoomAndCenterListeners)
			{
				listener.zoomOrCenterChanged(zoom, centerX, centerY);
			}
		}
	}
	/**
	 * Returns the current zoom level.
	 * @return Current zoom level.
	 */
	public double getZoom()
	{
		return zoom;
	}
	/**
	 * Returns the current center of the display area.
	 * @return center of display area.
	 */
	public Point2D.Double getCenter()
	{
		return new Point2D.Double(centerX, centerY);
	}
	
	/**
	 * Sets if the contrast should be automatically adjusted whenever a new image is set.
	 * @param autoAdjustContrast True if automatic adjustment should be activated.
	 */
	public void setAutoAdjustContrast(boolean autoAdjustContrast)
	{
		histogramPanel.setAutoAdjusting(autoAdjustContrast);
	}
	/**
	 * Returns if the contrast should be automatically adjusted whenever a new image is set.
	 * @return True if automatic adjustment should be activated.
	 */
	public boolean isAutoAdjustContrast()
	{
		return histogramPanel.isAutoAdjusting();
	}
	
	private class HistogramControl  extends DynamicPanel
	{
		/**
		 * Serial Version UID.
		 */
		private static final long serialVersionUID = -4695291153764223394L;
		private final JCheckBox autoAdjustField = new JCheckBox("auto-adjust", histogramPanel.isAutoAdjusting());
		public HistogramControl()
		{
			JPanel contrastButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			contrastButtonsPanel.setOpaque(false);
			Icon adjustIcon = ImageLoadingTools.getResourceIcon("icons/picture-sunset.png", "adjust");
			JButton adjustButton;
			if(adjustIcon != null)
				adjustButton = new JButton(adjustIcon);
			else
				adjustButton = new JButton("adjust");
			adjustButton.setToolTipText("Enhance contrast.");
			adjustButton.setOpaque(false);
			adjustButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e) {
					histogramPanel.autoAdjust();
				}
			});
			contrastButtonsPanel.add(adjustButton);
			
			Icon noAdjustIcon = ImageLoadingTools.getResourceIcon("icons/picture.png", "no adjust");
			JButton noAdjustButton;
			if(noAdjustIcon != null)
				noAdjustButton = new JButton(noAdjustIcon);
			else
				noAdjustButton = new JButton("original");
			noAdjustButton.setToolTipText("Original contrast.");
			noAdjustButton.setOpaque(false);
			noAdjustButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e) {
					histogramPanel.adjust(0, 1);
				}
			});
			contrastButtonsPanel.add(noAdjustButton);
			
			add(histogramPanel);
			add(contrastButtonsPanel);
			
			
			autoAdjustField.setOpaque(false);
			add(autoAdjustField);
			
			histogramPanel.addPropertyChangeListener("autoAdjusting", new PropertyChangeListener() {
				
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					autoAdjustField.setSelected(histogramPanel.isAutoAdjusting());				
				}
			});
			
			autoAdjustField.addActionListener(new ActionListener() 
			{	
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					histogramPanel.setAutoAdjusting(autoAdjustField.isSelected());
				}
			});
			autoAdjustField.setForeground(DEFAULT_FOREGROUND);
			autoAdjustField.setVisible(userChoosesAutoAdjustContrast);
		}
	}
	
	/**
	 * Set to true to display a checkbox with which the user can choose on him/herself if the contrast should be automatically adjusted when a new
	 * image arrives. Default is false. This does only make sense if it can be expected that more than one image can arrive at all.
	 * @param userChoosesAutoAdjustContrast True to display checkbox.
	 */
	public void setUserChoosesAutoAdjustContrast(boolean userChoosesAutoAdjustContrast)
	{
		this.userChoosesAutoAdjustContrast  = userChoosesAutoAdjustContrast;
		histogramControl.autoAdjustField.setVisible(userChoosesAutoAdjustContrast);
		if(userChoosesAutoAdjustContrast)
		{
			setAutoAdjustContrast(histogramControl.autoAdjustField.isSelected());
		}
	}
	
	/**
	 * Adds a component with the given title to the end of the menu containing all controls.
	 * @param title Title of the control.
	 * @param component The control element.
	 */
	public void addControl(String title, Component component)
	{
		addControl(title, component, false);
	}
	
	/**
	 * Adds a component with the given title to the end of the menu containing all controls.
	 * @param title Title of the control.
	 * @param component The control element.
	 * @param resizable True if the control can take up excess vertical space.
	 */
	public void addControl(String title, Component component, boolean resizable)
	{
		synchronized(controlsList)
		{
			controlsList.add(new Control(title, component, resizable));
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
		insertControl(title, component, index, false);
	}
	
	/**
	 * Adds a component with the given title to the menu containing all controls at the given index.
	 * @param title Title of the control.
	 * @param component The control element.
	 * @param index index where to add control. Must be greater or equal to 0, and smaller or equal to {@link #getNumControls()}.
	 * @param resizable True if the control can take up excess vertical space.
	 * @throws IndexOutOfBoundsException Thrown if index is invalid.
	 */
	public void insertControl(String title, Component component, int index, boolean resizable) throws IndexOutOfBoundsException
	{
		synchronized(controlsList)
		{
			controlsList.add(index, new Control(title, component, resizable));
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
		private final Color CONTROLS_BACKGROUND = new Color(DEFAULT_BACKGROUND.getRed(), DEFAULT_BACKGROUND.getGreen(), DEFAULT_BACKGROUND.getBlue(), 200);
		ControlsPanel()
		{
			setOpaque(false);
			setBorder(new EmptyBorder(10,3,10,3));
		}
		@Override
		public Dimension getPreferredSize()
		{
			Dimension superPreferred = super.getPreferredSize();
			superPreferred.width = 150;
			return superPreferred;
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
						boolean anyFill = false;
						for(Control control : controlsList)
						{
							// add panel for border.
							JPanel containingPanel = new JPanel(new BorderLayout());
							containingPanel.setOpaque(false);
							containingPanel.add(control.component);
							TitledBorder border = new TitledBorder(new LineBorder(DEFAULT_FOREGROUND, 1), control.title);
							border.setTitleColor(DEFAULT_FOREGROUND);
							containingPanel.setBorder(border);
							if(control.fill)
							{
								addFill(containingPanel);
								anyFill = true;
							}
							else
								add(containingPanel);
						}
						if(!anyFill)
							addFillEmpty();
						//if(isVisible())
							//revalidate();
					}
				}
			};
			if(SwingUtilities.isEventDispatchThread())
				runner.run();
			else
			{
				SwingUtilities.invokeLater(runner);
			}
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
				long lastAction = this.lastAction;
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
	private void calculateDragedLine(MouseEvent mouseDown, MouseEvent mouseCurrent)
	{
		LineInfo lineInfo = null;
		synchronized(imageLock)
		{
			if(lastDrawnRenderedImage == null || (lastDrawnOrgImageEvent == null && lastDrawnOrgBufferedImage == null))
			{
				mouseDown = null;
				mouseCurrent = null;
			}
			else if(mouseDown != null && mouseCurrent != null)
			{
				// check if valid position
				int imageWidth = lastDrawnRenderedImage.getWidth();
				int imageHeight = lastDrawnRenderedImage.getHeight();
					
				AffineTransform imageTransform = getTransform(imageWidth, imageHeight);
				Point2D pos1;
				Point2D pos2;
				try 
				{
					pos1 = imageTransform.createInverse().transform(new Point(mouseDown.getX(), mouseDown.getY()), null);
					pos2 = imageTransform.createInverse().transform(new Point(mouseCurrent.getX(), mouseCurrent.getY()), null);
				} catch (@SuppressWarnings("unused") NoninvertibleTransformException e1) {
					// will not happen.
					return;
				}
				int x1 = (int) pos1.getX();
				int x2 = (int) pos2.getX();
				int y1 = (int) pos1.getY();
				int y2 = (int) pos2.getY();
				
				if(x1 < 0 || x1 >= imageWidth || x2 < 0 || x2 >= imageWidth || y1 < 0 || y1 >= imageHeight || y2 < 0 || y2 >= imageHeight)
				{
					mouseDown = null;
					mouseCurrent = null;
				}
				else
				{
					lineInfoPopup.setLine(x1,y1,x2,y2);
					lineInfo = new LineInfo(x1, y1, x2, y2);
				}
			}
			
			
			if((mouseDown == null || mouseCurrent == null) && (startLine != null || endLine != null))
			{
				// == previously drawing line, but not anymore
				startLine = null;
				endLine = null;
				lineInfoPopup.setVisible(false);
			}
			else if(mouseDown == null || mouseCurrent == null)
			{
				// == didn't draw line, and don't want to draw line...
				return;
			}
			else
			{
				// == draw line!
				startLine = new Point(mouseDown.getX(), mouseDown.getY());
				endLine = new Point(mouseCurrent.getX(), mouseCurrent.getY());
				
				Dimension d = lineInfoPopup.getPreferredSize();
				if(startLine.x < endLine.x)
					lineInfoPopup.setBounds(endLine.x+10,endLine.y-d.height/2, d.width, d.height);
				else
					lineInfoPopup.setBounds(endLine.x-10-d.width,endLine.y-d.height/2, d.width, d.height);
				lineInfoPopup.setVisible(true);
			}
		}
		repaint();
		synchronized(lineListeners)
		{
			for(LineListener listener : lineListeners)
			{
				listener.lineChanged(lineInfo);
			}
		}
	}
	
	private void calculateHoveredPixel(MouseEvent mouseEvent)
	{
		BufferedImage lastDrawnRenderedImage;
		BufferedImage lastDrawnOrgBufferedImage;
		ImageEvent<?> lastDrawnOrgImageEvent;
		synchronized(imageLock)
		{
			lastDrawnRenderedImage = ImagePanel.this.lastDrawnRenderedImage;
			lastDrawnOrgBufferedImage = ImagePanel.this.lastDrawnOrgBufferedImage;
			lastDrawnOrgImageEvent = ImagePanel.this.lastDrawnOrgImageEvent;
		}
		if(mouseEvent == null || lastDrawnRenderedImage == null || (lastDrawnOrgImageEvent == null && lastDrawnOrgBufferedImage == null))
			return;
		int imageWidth = lastDrawnRenderedImage.getWidth();
		int imageHeight = lastDrawnRenderedImage.getHeight();
			
		AffineTransform imageTransform = getTransform(imageWidth, imageHeight);
		Point2D pos;
		try 
		{
			pos = imageTransform.createInverse().transform(new Point(mouseEvent.getX(), mouseEvent.getY()), null);
		} catch (@SuppressWarnings("unused") NoninvertibleTransformException e1) {
			// will not happen.
			return;
		}
		PixelInfo pixelInfo;
		if(lastDrawnOrgImageEvent != null)
		{
			Point orgCoord = ImageTools.backTransformCoordinate(lastDrawnOrgImageEvent, new Point((int)pos.getX(), (int)pos.getY()));
			
			if(orgCoord != null)
			{
				long pixelValue = ImageTools.getPixelValue(lastDrawnOrgImageEvent, orgCoord.x, orgCoord.y);
				pixelInfo = new PixelInfo((int)pos.getX(), (int)pos.getY(), pixelValue, ((double)pixelValue)/lastDrawnOrgImageEvent.getMaxIntensity());
			}
			else
				pixelInfo = null;
		}
		else
		{
			long pixelValue = ImageTools.getPixelValue(lastDrawnOrgBufferedImage, (int)pos.getX(), (int)pos.getY());
			long maxValue = ImageTools.getMaximalExpectedPixelValue(lastDrawnOrgBufferedImage);
			if(pixelValue >= 0 && maxValue >= 0)
				pixelInfo = new PixelInfo((int)pos.getX(), (int)pos.getY(), pixelValue, ((double)pixelValue)/maxValue);
			else
				pixelInfo = null;
		}
		
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
		private MouseEvent lastDown = null;
		@Override
		public void mouseDragged(MouseEvent mouseEvent) {
			hideTimer.userAction(mouseEvent);
			calculateHoveredPixel(mouseEvent);
			if(isLineMouse(mouseEvent))
				calculateDragedLine(lastDown, mouseEvent);
		}

		private boolean isLineMouse(MouseEvent e)
		{
			return SwingUtilities.isLeftMouseButton(e) && !e.isControlDown();
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
			//hideTimer.userAction(mouseEvent);
		}

		@Override
		public void mouseExited(MouseEvent mouseEvent) 
		{
			if(!(mouseEvent.getX() < getWidth() && mouseEvent.getX() > 0)
					||!(mouseEvent.getY() < getHeight() && mouseEvent.getY() > 0))
            {
				hideTimer.userAction(null);
				calculateDragedLine(null, null);
            }
			else
				hideTimer.userAction(mouseEvent);
		}

		@Override
		public void mousePressed(MouseEvent mouseEvent) {
			hideTimer.userAction(mouseEvent);
			lastClick = mouseEvent;
			if(isLineMouse(mouseEvent))
				lastDown = mouseEvent;
			requestFocusInWindow();
		}

		@Override
		public void mouseReleased(MouseEvent mouseEvent) {
			hideTimer.userAction(mouseEvent);
			
			lastDown = null;
			calculateDragedLine(null, null);
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
		setBackground(DEFAULT_BACKGROUND);
		this.client = client;
		controlsPanel = new ControlsPanel();
		histogramControl = new HistogramControl();
		addControl("Contrast", histogramControl);
		hideTimer = new HideTimer();
		lineInfoPopup.setVisible(false);
		
		add(controlsPanel);
		add(lineInfoPopup);
		
		histogramPanel.setAutoAdjusting(false);
		histogramPanel.addActionListener(new ActionListener()
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
				if(!isVisible())
					return;
				int controlsWidth = (int) controlsPanel.getPreferredSize().getWidth();
				controlsPanel.setBounds(getWidth()-controlsWidth, 0, controlsWidth, getHeight());
			}
			
			@Override
			public void componentShown(ComponentEvent e) 
			{
				if(!isVisible())
					return;
				int controlsWidth = (int) controlsPanel.getPreferredSize().getWidth();
				controlsPanel.setBounds(getWidth()-controlsWidth, 0, controlsWidth, getHeight());
			}
		});
		userListener = new UserListener();
		addMouseListener(userListener);
		addMouseMotionListener(userListener);
		addMouseWheelListener(userListener);
		
		controlsPanel.setVisible(false);
		
		setPreferredSize(new Dimension(800, 600));
		setMinimumSize(new Dimension(200, 200));
		
		setFocusable(true);
    }

	void fireImageChanged()
	{
		synchronized(imageLock)
		{
			renderedImage = null;
		}
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
		grp.setColor(getBackground());
		grp.fillRect(0, 0, getWidth(), getHeight());
		BufferedImage image;
		Point startLine;
		Point endLine;
		synchronized(imageLock)
		{
			ImageEvent<?> imageEvent = this.orgImageEvent;
			BufferedImage bufferedImage = this.orgBufferedImage;
			if(imageEvent == null && bufferedImage == null)
				renderedImage = null;
			else if(renderedImage == null)
	        {
				double min = histogramPanel.getLowerCutoff();
				double max = histogramPanel.getUpperCutoff();
				//double[] minMax = histogram.getMinMax();
				if(imageEvent != null)
				{
		        	
		        	try {
		        		renderedImage = ImageTools.getScaledMicroscopeImage(imageEvent, (float)min, (float)max);
					} catch (ImageConvertException e) {
						client.sendError("Could not generate image.", e);
						noImageText = "Error painting image.";
						renderedImage = null;
					}
				}
				else
				{
					renderedImage = ImageTools.getScaledImage(bufferedImage, (float)min, (float)max, null);
				}
	        }
			image = renderedImage;
			
			startLine = this.startLine;
			endLine = this.endLine;
			
			lastDrawnOrgImageEvent = imageEvent;
		    lastDrawnOrgBufferedImage = bufferedImage;
		    lastDrawnRenderedImage = renderedImage;
		}
        if (image != null)
        {
		    Graphics2D g2D = (Graphics2D) grp;
		    int imageWidth = image.getWidth(this);
		    int imageHeight = image.getHeight(this);
		    
		    g2D.drawImage(image, getTransform(imageWidth, imageHeight), this);
		    
		    if(startLine != null && endLine != null)
		    {
		    	g2D.setColor(DEFAULT_DRAW_LINE_COLOR);
		    	g2D.drawLine(startLine.x, startLine.y, endLine.x, endLine.y);
		    }
		    
        }
        else if(noImageText != null)
        {
        	grp.setColor(DEFAULT_FOREGROUND);
        	grp.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        	int strWidth = grp.getFontMetrics().stringWidth(noImageText);
        	int strHeight = grp.getFontMetrics().getHeight();
        	grp.drawString(noImageText, (getWidth()-strWidth)/2, (getHeight()-strHeight)/2);
        }
    }
	
	/**
	 * Returns a frame listener which can be added to the frame to which this panel is added, taking care of all default behavior.
	 * Listener is automatically added if {@link #toFrame()} is used.
	 * @return Frame listener.
	 */
	public YouScopeFrameListener getFrameListener()
	{
		return new YouScopeFrameListener() {
			
			@Override
			public void frameOpened() {
				hideTimer.userAction(null);
			}
			
			@Override
			public void frameClosed() {
				// do nothing.
			}
		};
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
		BufferedImage lastDrawnOrgBufferedImage;
		ImageEvent<?> lastDrawnOrgImageEvent;
		synchronized(imageLock)
		{
			lastDrawnOrgBufferedImage = this.lastDrawnOrgBufferedImage;
			lastDrawnOrgImageEvent = this.lastDrawnOrgImageEvent;
		}
		
		// get buffered image to save
		BufferedImage saveImage;
		if(lastDrawnOrgImageEvent != null)
		{
			try {
				saveImage = ImageTools.getMicroscopeImage(lastDrawnOrgImageEvent);
			} catch (ImageConvertException e1) {
				client.sendError("Could not parse image.", e1);
				return;
			}
		}
		else if(lastDrawnOrgBufferedImage != null)
		{
			saveImage = lastDrawnOrgBufferedImage;
		}
		else
		{
			return;
		}
		// Let user select file to save to
        String lastFile = "image.tif";
        JFileChooser fileChooser = new JFileChooser(lastFile);
        //Thread.currentThread().setContextClassLoader(ImagePanel.class.getClassLoader());
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
            	JOptionPane.showMessageDialog(null, "YouScope does not have a plug-in installed to support saving images with file type " + fileSuffix + ".", "Image file type not supported", JOptionPane.ERROR_MESSAGE);
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
            imageWriter.write(saveImage);
		}
		catch(IOException e)
		{
			client.sendError("Image " + file.getPath() + " could not be saved.",e);
		}
        finally
        {
        	imageWriter.dispose();
        	if(ios != null)
        	{
            	try
				{
            		ios.flush();
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
        frame.addFrameListener(getFrameListener());
        frame.pack();
		return frame;
	}
	/**
	 * Sets the image which should be displayed.
	 * @param bufferedImage Image which should be displayed.
	 */
	public synchronized void setImage(BufferedImage bufferedImage)
	{
		ImageConvertException exception = null;
		synchronized(imageLock)
		{
			this.orgBufferedImage = bufferedImage;
			this.orgImageEvent = null;
			try
			{
				histogramPanel.setImage(bufferedImage);
			}
			catch(ImageConvertException e)
			{
				exception = e;
			}
			fireImageChanged();
		}
		if(exception != null)
			client.sendError("Could not calculate histogram of image.", exception);
	}
	
	/**
	 * Sets the image which should be displayed.
	 * @param imageEvent Image which should be displayed.
	 */
	public synchronized void setImage(ImageEvent<?> imageEvent)
	{
		ImageConvertException exception = null;
		synchronized(imageLock)
		{
			this.orgBufferedImage = null;
			this.orgImageEvent = imageEvent;
			try
			{
				histogramPanel.setImage(imageEvent);
			}
			catch(ImageConvertException e)
			{
				exception = e;
			}
			fireImageChanged();
		}
		if(exception != null)
			client.sendError("Could not calculate histogram of image.", exception);
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
	/**
	 * Call this function such that the current settings are saved in the YouScope properties, and reloaded the next time started.
	 * @param properties The properties of YouScope to save settings in.
	 */
	public void saveSettings(PropertyProvider properties) {
		boolean autocontrast = isAutoAdjustContrast();
    	properties.setProperty(StandardProperty.PROPERTY_IMAGE_PANEL_LAST_AUTO_CONTRAST, autocontrast);
    	
    	properties.setProperty(StandardProperty.PROPERTY_IMAGE_PANEL_LOWER_AUTO_ADJUSTMENT_CUTOFF_PERCENTAGE, histogramPanel.getLowerAutoAdjustmentCutoffPercentage());
    	properties.setProperty(StandardProperty.PROPERTY_IMAGE_PANEL_UPPER_AUTO_ADJUSTMENT_CUTOFF_PERCENTAGE, histogramPanel.getUpperAutoAdjustmentCutoffPercentage());
    	properties.setProperty(StandardProperty.PROPERTY_IMAGE_PANEL_NUM_BINS, histogramPanel.getNumBins());
    	properties.setProperty(StandardProperty.PROPERTY_IMAGE_PANEL_LOGARITHMIC, histogramPanel.isLogarithmic());
    	properties.setProperty(StandardProperty.PROPERTY_IMAGE_PANEL_NOTIFY_IF_OVEREXPOSED, histogramPanel.isNotifyIfOverExposed());
	}
	/**
	 * Call this function the current settings from the YouScope properties.
	 * @param properties The properties of YouScope the settings are saved in.
	 */
	public void loadSettings(PropertyProvider properties) {
		if((boolean) client.getPropertyProvider().getProperty(StandardProperty.PROPERTY_STREAM_USE_DEFAULT_SETTINGS))
			setAutoAdjustContrast((Boolean) client.getPropertyProvider().getProperty(StandardProperty.PROPERTY_IMAGE_PANEL_DEFAULT_AUTO_CONTRAST));
		else
			setAutoAdjustContrast((Boolean) client.getPropertyProvider().getProperty(StandardProperty.PROPERTY_IMAGE_PANEL_LAST_AUTO_CONTRAST));
		
		histogramPanel.setAutoAdjustmentCutoffPercentages((double)properties.getProperty(StandardProperty.PROPERTY_IMAGE_PANEL_LOWER_AUTO_ADJUSTMENT_CUTOFF_PERCENTAGE),
				(double)properties.getProperty(StandardProperty.PROPERTY_IMAGE_PANEL_UPPER_AUTO_ADJUSTMENT_CUTOFF_PERCENTAGE));
		histogramPanel.setNumBins((int) properties.getProperty(StandardProperty.PROPERTY_IMAGE_PANEL_NUM_BINS));
		histogramPanel.setLogarithmic((boolean) properties.getProperty(StandardProperty.PROPERTY_IMAGE_PANEL_LOGARITHMIC));
		histogramPanel.setNotifyIfOverExposed((boolean) properties.getProperty(StandardProperty.PROPERTY_IMAGE_PANEL_NOTIFY_IF_OVEREXPOSED));
	}
}
