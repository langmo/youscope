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
package org.youscope.uielements.plaf;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;

import org.youscope.uielements.HistogramPanel;

/**
 * Default/basic UI delegate for the {@link HistogramPanel}.
 * @author Moritz Lang
 *
 */
public class BasicHistogramPanelUI extends HistogramPanelUI
{
	private HistogramPanel histogramPanel;
	private final double ZOOM_IN_STEP = Math.sqrt(2);
	private final double ZOOM_OUT_STEP = 1/Math.sqrt(2);
	private final double ZOOM_MAX = 100;
	private double zoomX = 1;
	private double centerX = 0.5;
	
	private final JRadioButtonMenuItem linScaleField = new JRadioButtonMenuItem("Linear scale");
	private final JRadioButtonMenuItem logScaleField = new JRadioButtonMenuItem("Logarithmic scale");
	
	private final JRadioButtonMenuItem robustAdjustmentField = new JRadioButtonMenuItem("Robust auto-adjust");
	private final JRadioButtonMenuItem strictAdjustmentField = new JRadioButtonMenuItem("Strict auto-adjust");
	
	private final JRadioButtonMenuItem lowResolutionField = new JRadioButtonMenuItem("Low resolution");
	private final JRadioButtonMenuItem mediumResolutionField = new JRadioButtonMenuItem("Medium resolution");
	private final JRadioButtonMenuItem highResolutionField = new JRadioButtonMenuItem("High resolution");
	
	private final JCheckBoxMenuItem notifyIfOverExposedField = new JCheckBoxMenuItem("Notify if overexposed");
	
	private double draggedCutoffX = 1;
	private int currentlyDragging = -1; // -1=none, 0=lower cutoff, 1 = upper cutoff 
	
	private Color cutoffLineColor;
	private Color cutoffFillColor;
	
	private Color overexposedMessageColor;
	/**
	 * Name of the UI property defining the background color.
	 */
	public static final String PROPERTY_BACKGROUND = HistogramPanel.UI_CLASS_ID+".background";
	/**
	 * Name of the UI property defining the color of the histogram of grayscale images.
	 */
	public static final String PROPERTY_CHANNEL_GRAY_COLOR = HistogramPanel.UI_CLASS_ID+".channelGrayColor";
	
	/**
	 * Name of the UI property defining the color of the histogram of the first channel of color images.
	 */
	public static final String PROPERTY_CHANNEL1_COLOR = HistogramPanel.UI_CLASS_ID+".channel1Color";
	
	/**
	 * Name of the UI property defining the color of the histogram of the second channel of color images.
	 */
	public static final String PROPERTY_CHANNEL2_COLOR = HistogramPanel.UI_CLASS_ID+".channel2Color";
	
	/**
	 * Name of the UI property defining the color of the histogram of the third channel of color images.
	 */
	public static final String PROPERTY_CHANNEL3_COLOR = HistogramPanel.UI_CLASS_ID+".channel3Color";
	
	/**
	 * Name of the UI property defining the color of the histogram of any additional channel of color images.
	 */
	public static final String PROPERTY_CHANNEL_ADDITIONAL_COLOR = HistogramPanel.UI_CLASS_ID+".channelAdditionalColor";
	
	/**
	 * Name of the UI property defining the color of the cutoff lines.
	 */
	public static final String PROPERTY_CUTOFF_LINE_COLOR = HistogramPanel.UI_CLASS_ID+".cutoffLineColor";
	
	/**
	 * Name of the UI property defining the color of the cutoff fills.
	 */
	public static final String PROPERTY_CUTOFF_FILL_COLOR = HistogramPanel.UI_CLASS_ID+".cutoffFillColor";
	/**
	 * Name of the UI property defining the color of the overexposed warning message.
	 */
	public static final String PROPERTY_OVEREXPOSED_MESSAGE_COLOR = HistogramPanel.UI_CLASS_ID+".overexposedMessageColor";
	
	private static final int CUTOFF_LINE_PERCEIVED_WIDTH = 2; // pixels
	
	private final PropertyChangeListener changeListener = new PropertyChangeListener() {
		
		@Override
		public void propertyChange(PropertyChangeEvent evt) 
		{
			String property = evt.getPropertyName();
			if(property == null)
				return;
			else if(property.equals("logarithmic"))
			{
				updateScaleFields();
				histogramPanel.repaint();
			}
			else if(property.equals("numBins"))
				updateResolutionFields();
			else if(property.equals("lowerAutoAdjustmentCutoffPercentage") || property.equals("upperAutoAdjustmentCutoffPercentage"))
				updateAdjustmentFields();
			else if(property.equals("notifyIfOverExposed"))
			{
				updateOverexposedFiled();
				histogramPanel.repaint();
			}
			else
				histogramPanel.repaint();
		}
	};
	private final OptionsPopupMenu optionsPopupMenu = new OptionsPopupMenu();
	private class OptionsPopupMenu extends JPopupMenu
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -7541222614639996559L;

		{
			ButtonGroup scaleGroup = new ButtonGroup();
			scaleGroup.add(linScaleField);
			scaleGroup.add(logScaleField);
			add(linScaleField);
			add(logScaleField);
			ActionListener scaleListener = new ActionListener() 
			{
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					histogramPanel.setLogarithmic(logScaleField.isSelected());
				}
			};
			linScaleField.addActionListener(scaleListener);
			logScaleField.addActionListener(scaleListener);
			
			addSeparator();
			
			ButtonGroup resolutionGroup = new ButtonGroup();
			resolutionGroup.add(lowResolutionField);
			resolutionGroup.add(mediumResolutionField);
			resolutionGroup.add(highResolutionField);
			add(lowResolutionField);
			add(mediumResolutionField);
			add(highResolutionField);
			ActionListener resolutionListener = new ActionListener() 
			{
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					if(lowResolutionField.isSelected())
						histogramPanel.setNumBins(HistogramPanel.NUM_BINS_LOW);
					else if(mediumResolutionField.isSelected())
						histogramPanel.setNumBins(HistogramPanel.NUM_BINS_MEDIUM);
					else if(highResolutionField.isSelected())
					histogramPanel.setNumBins(HistogramPanel.NUM_BINS_HIGH);
				}
			};
			lowResolutionField.addActionListener(resolutionListener);
			mediumResolutionField.addActionListener(resolutionListener);
			highResolutionField.addActionListener(resolutionListener);
			
			addSeparator();
			
			ButtonGroup adjustmentGroup = new ButtonGroup();
			adjustmentGroup.add(robustAdjustmentField);
			adjustmentGroup.add(strictAdjustmentField);
			add(robustAdjustmentField);
			add(strictAdjustmentField);
			ActionListener adjustmentListener = new ActionListener() 
			{
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					if(robustAdjustmentField.isSelected())
						histogramPanel.setAutoAdjustmentCutoffPercentages(HistogramPanel.AUTO_ADJUST_ROBUST, HistogramPanel.AUTO_ADJUST_ROBUST);
					else
						histogramPanel.setAutoAdjustmentCutoffPercentages(HistogramPanel.AUTO_ADJUST_STRICT, HistogramPanel.AUTO_ADJUST_STRICT);
				}
			};
			robustAdjustmentField.addActionListener(adjustmentListener);
			strictAdjustmentField.addActionListener(adjustmentListener);
			
			addSeparator();
			
			add(notifyIfOverExposedField);
			notifyIfOverExposedField.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					histogramPanel.setNotifyIfOverExposed(notifyIfOverExposedField.isSelected());
				}
			});
		}
	}
	private class UserListener implements MouseListener, MouseMotionListener, MouseWheelListener
	{
		private void dragCutoff(MouseEvent mouseEvent)
		{
			draggedCutoffX = backTransformX(mouseEvent.getX(), getTransform());
			if(draggedCutoffX < 0)
				draggedCutoffX = 0;
			else if(draggedCutoffX > 1)
				draggedCutoffX = 1;
			if(currentlyDragging == 0 && draggedCutoffX > histogramPanel.getUpperCutoff())
				draggedCutoffX = histogramPanel.getUpperCutoff();
			else if(currentlyDragging == 1 && draggedCutoffX < histogramPanel.getLowerCutoff())
				draggedCutoffX = histogramPanel.getLowerCutoff();
			if(currentlyDragging >= 0)
				histogramPanel.repaint();
		}
		@Override
		public void mouseDragged(MouseEvent mouseEvent) 
		{
			dragCutoff(mouseEvent);
		}

		private boolean isLeftMouse(MouseEvent e)
		{
			return SwingUtilities.isLeftMouseButton(e) && !e.isControlDown();
		}
		
		@Override
		public void mouseMoved(MouseEvent mouseEvent) 
		{
			updateCursor(mouseEvent);
		}
		private void updateCursor(MouseEvent mouseEvent)
		{
			AffineTransform transform = getTransform();
			int lowerCutoffX = transformX(histogramPanel.getLowerCutoff(), transform);
			int upperCutoffX = transformX(histogramPanel.getUpperCutoff(), transform);
			if(Math.abs(mouseEvent.getX()-lowerCutoffX) < CUTOFF_LINE_PERCEIVED_WIDTH|| Math.abs(mouseEvent.getX()-upperCutoffX) < CUTOFF_LINE_PERCEIVED_WIDTH)
				histogramPanel.setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
			else
				histogramPanel.setCursor(null);
		}

		@Override
		public void mouseClicked(MouseEvent mouseEvent) 
		{
			// do nothing.
		}

		@Override
		public void mouseEntered(MouseEvent mouseEvent) 
		{
			// do nothing.
		}

		@Override
		public void mouseExited(MouseEvent mouseEvent) 
		{
			// do nothing.
		}
				
		@Override
		public void mousePressed(MouseEvent mouseEvent) 
		{
			if(!isLeftMouse(mouseEvent))
				return;
			AffineTransform transform = getTransform();
			int lowerCutoffX = transformX(histogramPanel.getLowerCutoff(), transform);
			int upperCutoffX = transformX(histogramPanel.getUpperCutoff(), transform);
			if(Math.abs(mouseEvent.getX()-lowerCutoffX) < CUTOFF_LINE_PERCEIVED_WIDTH && Math.abs(mouseEvent.getX()-lowerCutoffX) < Math.abs(mouseEvent.getX()-upperCutoffX))
			{
				currentlyDragging = 0;
				dragCutoff(mouseEvent);
			}
			else if( Math.abs(mouseEvent.getX()-upperCutoffX) < CUTOFF_LINE_PERCEIVED_WIDTH)
			{
				currentlyDragging = 1;
				dragCutoff(mouseEvent);
			}
		}

		@Override
		public void mouseReleased(MouseEvent mouseEvent) 
		{
			if(currentlyDragging < 0)
				return;
			histogramPanel.setAutoAdjusting(false);
			if(currentlyDragging == 0)
				histogramPanel.adjust(draggedCutoffX, histogramPanel.getUpperCutoff());
			else if(currentlyDragging == 1)
				histogramPanel.adjust(histogramPanel.getLowerCutoff(), draggedCutoffX);
			fireActionEvent();
			currentlyDragging = -1;
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
	private final UserListener userListener = new UserListener();
	
	private void fireActionEvent()
	{
		for(ActionListener listener : histogramPanel.getActionListeners())
		{
			listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_FIRST, "cutoffs_changed"));
		}
	}
	
	private void zoom(MouseEvent e, boolean zoomIn)
	{
		if(e == null)
			return;
		if(zoomIn)
		{
			AffineTransform imageTransform = getTransform();
			Point2D pos;
			try {
				pos = imageTransform.createInverse().transform(new Point(e.getX(), e.getY()), null);
			} catch (@SuppressWarnings("unused") NoninvertibleTransformException e1) {
				// will not happen.
				return;
			}
			centerX = pos.getX();
			if(centerX < 0)
				centerX = 0;
			else if(centerX > 1)
				centerX = 1;
			zoomX*=ZOOM_IN_STEP;
			if(zoomX > ZOOM_MAX)
				zoomX = ZOOM_MAX;
		}
		else
		{
			zoomX*=ZOOM_OUT_STEP;
			if(zoomX < 1)
				zoomX = 1;
		}
		histogramPanel.repaint();
	}
	
	protected void installListeners() 
    {
        histogramPanel.addPropertyChangeListener(changeListener);
        
        histogramPanel.addMouseListener(userListener);
        histogramPanel.addMouseMotionListener(userListener);
        histogramPanel.addMouseWheelListener(userListener);
    }

    @Override
	public Dimension getPreferredSize(JComponent c)
	{
    	return new Dimension(50, 75);
	}
    @Override
	public Dimension getMinimumSize(JComponent c)
    {
    	return new Dimension(50, 75);
    }
    
    @Override
    public void installUI(JComponent c) 
    {
    	histogramPanel = (HistogramPanel) c;
    	installDefaults();
    	installPopupMenu();
        installListeners();
        updateScaleFields();
        updateResolutionFields();
        updateAdjustmentFields();
        updateOverexposedFiled();

    }
    protected void installPopupMenu()
    {
    	JPopupMenu oldPopup = histogramPanel.getComponentPopupMenu();
    	if((oldPopup == null || oldPopup instanceof OptionsPopupMenu) && oldPopup != optionsPopupMenu)
    		histogramPanel.setComponentPopupMenu(optionsPopupMenu);
    }
    protected void uninstallPopupMenu()
    {
    	JPopupMenu oldPopup = histogramPanel.getComponentPopupMenu();
    	if(oldPopup == optionsPopupMenu)
    		histogramPanel.setComponentPopupMenu(null);
    }
    protected void updateScaleFields()
    {
    	if(histogramPanel.isLogarithmic())
    		logScaleField.setSelected(true);
    	else
    		linScaleField.setSelected(true);
    }
    protected void updateAdjustmentFields()
    {
    	double lowerCutoff = histogramPanel.getLowerAutoAdjustmentCutoffPercentage();
    	double upperCutoff = histogramPanel.getUpperAutoAdjustmentCutoffPercentage();
    	if(lowerCutoff != upperCutoff || (lowerCutoff != HistogramPanel.AUTO_ADJUST_ROBUST && lowerCutoff != HistogramPanel.AUTO_ADJUST_STRICT))
    	{
    		robustAdjustmentField.setSelected(false);
    		strictAdjustmentField.setSelected(false);
    	}
    	else if(lowerCutoff == HistogramPanel.AUTO_ADJUST_ROBUST)
    		robustAdjustmentField.setSelected(true);
    	else
    		strictAdjustmentField.setSelected(true);
    }
    protected void updateOverexposedFiled()
    {
    	notifyIfOverExposedField.setSelected(histogramPanel.isNotifyIfOverExposed());
    }
    protected void updateResolutionFields()
    {
    	int numBins = histogramPanel.getNumBins();
    	if(numBins == HistogramPanel.NUM_BINS_LOW)
    		lowResolutionField.setSelected(true);
    	else if(numBins == HistogramPanel.NUM_BINS_MEDIUM)
    		mediumResolutionField.setSelected(true);
    	else if(numBins == HistogramPanel.NUM_BINS_HIGH)
    		highResolutionField.setSelected(true);
    	else
    	{
    		lowResolutionField.setSelected(false);
    		mediumResolutionField.setSelected(false);
    		highResolutionField.setSelected(false);
    	}
    }
    protected void installDefaults()
    {
    	Color color = histogramPanel.getBackground();
    	if(color == null || color instanceof UIResource)
    	{
    		color = UIManager.getColor(PROPERTY_BACKGROUND);
    		histogramPanel.setBackground(color==null ? new ColorUIResource(Color.WHITE):color);
    		histogramPanel.setOpaque(true);
    	}
    	
    	color = histogramPanel.getChannelGrayColor();
    	if(color == null || color instanceof UIResource)
    	{
    		color = UIManager.getColor(PROPERTY_CHANNEL_GRAY_COLOR);
    		histogramPanel.setChannelGrayColor(color==null ? new ColorUIResource(new Color(0.2F, 0.2F, 0.6F)):color);
    	}
    	
    	color = histogramPanel.getChannel1Color();
    	if(color == null || color instanceof UIResource)
    	{
    		color = UIManager.getColor(PROPERTY_CHANNEL1_COLOR);
    		histogramPanel.setChannel1Color(color==null ? new ColorUIResource(new Color(0.8F, 0.2F, 0.2F)):color);
    	}
    	
    	color = histogramPanel.getChannel2Color();
    	if(color == null || color instanceof UIResource)
    	{
    		color = UIManager.getColor(PROPERTY_CHANNEL2_COLOR);
    		histogramPanel.setChannel2Color(color==null ? new ColorUIResource(new Color(0.2F, 0.8F, 0.2F, 0.5F)):color);
    	}
    	
    	color = histogramPanel.getChannel3Color();
    	if(color == null || color instanceof UIResource)
    	{
    		color = UIManager.getColor(PROPERTY_CHANNEL3_COLOR);
    		histogramPanel.setChannel3Color(color==null ? new ColorUIResource(new Color(0.2F, 0.2F, 0.8F, 0.333F)):color);
    	}
    	
    	color = histogramPanel.getChannelAdditionalColor();
    	if(color == null || color instanceof UIResource)
    	{
    		color = UIManager.getColor(PROPERTY_CHANNEL_ADDITIONAL_COLOR);
    		histogramPanel.setChannelAdditionalColor(color==null ? new ColorUIResource(new Color(0.5F, 0.5F, 0.5F, 0.333F)):color);
    	}
    	
    	cutoffFillColor = UIManager.getColor(PROPERTY_CUTOFF_FILL_COLOR);
    	if(cutoffFillColor == null)
    		cutoffFillColor = new Color(0.5F, 0.5F, 0.5F, 0.5F);
    	cutoffLineColor = UIManager.getColor(PROPERTY_CUTOFF_LINE_COLOR);
    	if(cutoffLineColor == null)
    		cutoffLineColor = Color.BLACK;
    	overexposedMessageColor = UIManager.getColor(PROPERTY_OVEREXPOSED_MESSAGE_COLOR);
    	if(overexposedMessageColor == null)
    		overexposedMessageColor = Color.RED;
    	
    		
    }

    protected void uninstallListeners() {

    	histogramPanel.removePropertyChangeListener(changeListener);

    	histogramPanel.removeMouseListener(userListener);
        histogramPanel.removeMouseMotionListener(userListener);
        histogramPanel.removeMouseWheelListener(userListener);
    }

    @Override
    public void uninstallUI(JComponent c) {
    	uninstallPopupMenu();
    	uninstallListeners();
        histogramPanel = null;
    }
    
    AffineTransform getTransform()
	{
    	// -1, because we want to paint in [0,1], not in [0,1).
		double width = histogramPanel.getWidth()-1;
		double height = histogramPanel.getHeight()-1;
		// total width of the histogram, after scaling, in original coordinates, even if larger than painted area.
		double scaledHistogramWidth = width*zoomX;
		
		AffineTransform transform;
		double deltaX;
		if(zoomX == 1)
		{
			deltaX = 0;
		}
		else
		{
			deltaX = 1/2 - centerX;
			if(deltaX > 0)
				deltaX = 0;
			//else if(deltaX < 1-zoomX)
			//	deltaX = 1-zoomX;
			
			deltaX = width/2 - centerX * scaledHistogramWidth;
			if(deltaX > 0)
				deltaX = 0;
			else if(deltaX < width-scaledHistogramWidth)
				deltaX = width-scaledHistogramWidth;
		}
		transform = AffineTransform.getTranslateInstance(deltaX, 0);
		transform.concatenate(AffineTransform.getScaleInstance(width*zoomX, height));
		return transform;
	}

    @Override
    public void paint(Graphics g1D, JComponent c) {
        super.paint(g1D, c);
        Graphics2D g2D = (Graphics2D)g1D;
        
        int[][] bins = histogramPanel.getHistogramBins();
        int panelWidth = histogramPanel.getWidth();
		int panelHeight = histogramPanel.getHeight();
		boolean logarithmic = histogramPanel.isLogarithmic();
		boolean overexposed = histogramPanel.isNotifyIfOverExposed() && histogramPanel.isOverExposed();
		
        g1D.setColor(histogramPanel.getBackground());
        g1D.fillRect(0, 0, panelWidth, panelHeight);
		
        if(bins == null)
		{
			String text = "No data available.";
			// get metrics from the graphics
			FontMetrics metrics = g1D.getFontMetrics();
			// get the height of a line of text in this font and render context
			int textHeight = metrics.getHeight();
			// get the advance of my text in this font and render context
			int textWidth = metrics.stringWidth(text);
			g1D.setColor(histogramPanel.getForeground());
			g1D.drawString(text, (panelWidth - textWidth) / 2, (panelHeight + textHeight) / 2);
			return;
		}
        
        
        // We paint the shape in a coordinate system with x and y values both going from zero to one, and transform the shape to the real coordingate system afterwards.
		AffineTransform transform = getTransform();
		
		// paint channels. If there are four channels, the first is probably the transparency channel...
		for(int band = (bins.length==4 ? 1 : 0); band < bins.length; band++)
		{
			if(bins[band] == null || bins[band].length < 2)
				continue;
			double maxValue = 0;
			for(int bin : bins[band])
			{
				if(bin > maxValue)
					maxValue = bin;
			}
			
			Path2D path = new Path2D.Double();
			if(logarithmic)
			{
				path.moveTo(0, 1-Math.log(1+bins[band][0] / Math.log(1+maxValue)));
				for(int i = 0; i < bins[band].length; i++)
				{
					path.lineTo(i / (bins[band].length-1.0), 1-Math.log(1+bins[band][i]) / Math.log(1+maxValue));
				}
				path.lineTo(1,1);
				path.lineTo(0,  1);
				path.closePath();
			}
			else
			{
				path.moveTo(0, 1-bins[band][0] / maxValue);
				for(int i = 0; i < bins[band].length; i++)
				{
					path.lineTo(i / (bins[band].length-1.0), 1-bins[band][i] / maxValue);
				}
				path.lineTo(1,1);
				path.lineTo(0,  1);
				path.closePath();
			}
			Shape histogramShape = transform.createTransformedShape(path);
			
			Color channelColor;
			if(bins.length == 1)
			{
				// Grayscale image
				channelColor = histogramPanel.getChannelGrayColor();
			}
			else
			{
				switch(band)
				{
					case 1:
						// Red
						channelColor = histogramPanel.getChannel1Color();
						break;
					case 2:
						// Green
						channelColor = histogramPanel.getChannel2Color();
						break;
					case 3:
						// Blue
						channelColor = histogramPanel.getChannel3Color();
						break;
					default:
						// Should not happen that more than three bands. Anyway, gets its own color.
						channelColor = histogramPanel.getChannelAdditionalColor();
						break;
				}
			}
			
			g2D.setColor(channelColor);
			g2D.fill(histogramShape);
			g2D.setColor(channelColor.darker());
			g2D.draw(histogramShape);
		}
		
		// draw over-exposed message
		if(overexposed)
		{
			String text = "Overexposed";
			// get metrics from the graphics
			FontMetrics metrics = g1D.getFontMetrics();
			// get the height of a line of text in this font and render context
			int textHeight = metrics.getHeight();
			// get the advance of my text in this font and render context
			int textWidth = metrics.stringWidth(text);
			g1D.setColor(overexposedMessageColor);
			g1D.drawString(text, (panelWidth - textWidth) / 2, (panelHeight + textHeight) / 2);
		}
		
		// draw cutoffs
		int lowerCutoffX;
		if(currentlyDragging == 0)
			lowerCutoffX = transformX(draggedCutoffX, transform);
		else
			lowerCutoffX = transformX(histogramPanel.getLowerCutoff(), transform);
		int upperCutoffX;
		if(currentlyDragging == 1)
			upperCutoffX = transformX(draggedCutoffX, transform);
		else
			upperCutoffX= transformX(histogramPanel.getUpperCutoff(), transform);
		g2D.setColor(cutoffFillColor);
		if(lowerCutoffX>0)
			g2D.fillRect(0, 0, lowerCutoffX, panelHeight-1);
		if(panelWidth-upperCutoffX > 0)
			g2D.fillRect(upperCutoffX, 0, panelWidth-upperCutoffX, panelHeight-1);
		g2D.setColor(cutoffLineColor);
		g2D.drawLine(lowerCutoffX, 0, lowerCutoffX, panelHeight-1);
		g2D.drawLine(upperCutoffX, 0, upperCutoffX, panelHeight-1);
    }
    
    private int transformX(double x, AffineTransform transform)
    {
    	return (int)Math.round(transform.transform(new Point2D.Double(x,  0), null).getX());
    }
    
    private double backTransformX(int x, AffineTransform transform)
    {
    	try {
			return transform.createInverse().transform(new Point2D.Double(x,  0), null).getX();
		} catch (NoninvertibleTransformException e) {
			// will not happen, since we prevent the transformation to be singular.
			throw new RuntimeException("Inversion of x position failed.", e);
		}
    }


    public static ComponentUI createUI(JComponent c) {
    	BasicHistogramPanelUI ui = new BasicHistogramPanelUI();   	
    	return ui;
    }

}
