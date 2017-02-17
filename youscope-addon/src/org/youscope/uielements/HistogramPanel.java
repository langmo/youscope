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
package org.youscope.uielements;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.UIManager;

import org.youscope.common.image.ImageEvent;
import org.youscope.common.util.ImageConvertException;
import org.youscope.common.util.ImageTools;
import org.youscope.uielements.plaf.BasicHistogramPanelUI;
import org.youscope.uielements.plaf.HistogramPanelUI;

/**
 * Displays a histogram of an image, and allows to set lower and upper cutoffs.
 * @author Moritz Lang
 *
 */
public class HistogramPanel extends JComponent
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -7547161227019466525L;

	/**
	 * UI Delegate name.
	 */
	public static final String UI_CLASS_ID = "HistogramPanelUI";

	private double				lowerAutoAdjustmentCutoffPercentage	= AUTO_ADJUST_ROBUST;					// in percent.
	private double				upperAutoAdjustmentCutoffPercentage	= AUTO_ADJUST_ROBUST;					// in percent.
	private boolean				autoAdjusting			= false;
	
	private Color channel1Color;
	private Color channel2Color;
	private Color channel3Color;
	private Color channelAdditionalColor;
	
	private boolean overExposed = false;
	
	private boolean notifyIfOverExposed = true;
	
	// number of bins used for histogram.
	/**
	 * Number of bins corresponding to a low resolution.
	 */
	public static final int NUM_BINS_LOW = 25;
	/**
	 * Number of bins corresponding to a medium resolution.
	 */
	public static final int NUM_BINS_MEDIUM = 100;
	/**
	 * Number of bins corresponding to a high resolution.
	 */
	public static final int NUM_BINS_HIGH = 400;
	
	/**
	 * Corresponding to zero percent cutoff, i.e. even a single white or black pixel changes the cutoffs.
	 */
	public static final double AUTO_ADJUST_STRICT = 0;
	/**
	 * Three percent of the brightest and darkest pixels are ignored when calculating the cutoffs. Performs better if camera produces pixel noise.
	 */
	public static final double AUTO_ADJUST_ROBUST = 0.03;
	
	private int numBins = 100;
	
	private int[][] histogram;
	
	private double minIntensity = 0;
	private double maxIntensity = 1;
	
	private boolean logarithmic = false;
	
	private final ArrayList<ActionListener> actionListeners = new ArrayList<ActionListener>();
	
	// Only at most one of the two should be non-zero at all times.
	private ImageEvent<?> lastImageEvent = null;
	private BufferedImage lastBufferedImage = null;
	
	/**
	 * Constuctor.
	 */
	public HistogramPanel() {
		updateUI();		
	}
	
	@Override
	public void updateUI() {
		if (UIManager.get(getUIClassID()) != null) {
			HistogramPanelUI ui = (HistogramPanelUI) UIManager.getUI(this);
            setUI(ui);
        } else {
            setUI(new BasicHistogramPanelUI());
        }

	}

	 @Override
	    public String getUIClassID() {
	        return UI_CLASS_ID;
	    }
	 
	 /**
	  * Sets the color the histogram of a grayscale image is plotted. Same as {@link #setForeground(Color)}.
	  * @param color Color of the histogram.
	  */
	 public void setChannelGrayColor(Color color)
	 {
		 setForeground(color);
	 }
	 /**
	  * Returns the color the histogram of a grayscale image is plotted. Same as {@link #getForeground()}.
	  * @return Color of the histogram.
	  */
	 public Color getChannelGrayColor()
	 {
		 return getForeground();
	 }
	 
	 /**
	  * Sets the color the histogram of the first channel (typically representing red) is plotted for a multi-channel/color image.
	  * Use {@link #setChannelGrayColor(Color)} to define the color of a histogram for grayscale images.
	  * @param color Color in which the first channel of a color image is plotted.
	  */
	 public void setChannel1Color(Color color)
	 {
		 Color oldColor = channel1Color;
		 channel1Color = color;
		 firePropertyChange("channel1Color", oldColor, color);
	 }
	 
	 /**
	  * Set to true to display a message in the histogram if the image is over-exposed, determined if the maximal pixel value is realized in the image.
	  * @param notifyIfOverExposed True to display the message, false otherwise.
	  */
	 public void setNotifyIfOverExposed(boolean notifyIfOverExposed)
	 {
		 if(this.notifyIfOverExposed == notifyIfOverExposed)
			 return;
		 this.notifyIfOverExposed = notifyIfOverExposed;
		 firePropertyChange("notifyIfOverExposed", !notifyIfOverExposed, notifyIfOverExposed);
	 }
	 
	 /**
	  * Returns true if a message is displayed in the histogram if the image is over-exposed, determined if the maximal pixel value is realized in the image.
	  * @return True to display the message, false otherwise.
	  */
	 public boolean isNotifyIfOverExposed()
	 {
		 return notifyIfOverExposed;
	 }
	 
	 /**
	  * Returns true if the last image was over-exposed, determined if the maximal pixel value is realized in the image.. Note that it cannot always be detected if an image was not overexpoesed.
	  * @return true if the last image was over-exposed.
	  */
	 public boolean isOverExposed()
	 {
		 return overExposed;
	 }
	 
	 /**
	  * Sets the number of bins used to calculate the histogram. It is recommended to use one of the properties
	  * {@link #NUM_BINS_LOW}, {@link #NUM_BINS_MEDIUM}, {@link #NUM_BINS_HIGH}, even though other custom values can be used.
	  * @param numBins Number of bins in the histogram
	  */
	 public void setNumBins(int numBins)
	 {
		 if(this.numBins == numBins)
			 return;
		 int oldValue = this.numBins;
		 synchronized(this)
		 {
			 this.numBins = numBins;
			 try
			 {
				 if(lastBufferedImage != null)
					 setImage(lastBufferedImage);
				 else if(lastImageEvent != null)
					 setImage(lastImageEvent);
			 }
			 catch(@SuppressWarnings("unused") ImageConvertException e)
			 {
				 // do nothing, just keep showing the old image.
			 }
		 }
		 firePropertyChange("numBins", oldValue, numBins);
	 }
	 /**
	  * Returns the number of bins used to calculate the histogram. It is recommended to use one of the properties
	  * {@link #NUM_BINS_LOW}, {@link #NUM_BINS_MEDIUM}, {@link #NUM_BINS_HIGH}, even though other custom values can be used.
	  * @return Number of bins in the histogram
	  */
	 public int getNumBins()
	 {
		 return numBins;
	 }
	 
	 /**
	  * Returns the color the histogram of the first channel (typically representing red) is plotted for a multi-channel/color image.
	  * Use {@link #getChannelGrayColor()} to obtain the color of a histogram for grayscale images.
	  * @return Color in which the first channel of a color image is plotted.
	  */
	 public Color getChannel1Color()
	 {
		 return channel1Color;
	 }
	 
	 /**
	  * Sets the color the histogram of the second channel (typically representing green) is plotted for a multi-channel/color image.
	  * Use {@link #setChannelGrayColor(Color)} to define the color of a histogram for grayscale images.
	  * @param color Color in which the second channel of a color image is plotted.
	  */
	 public void setChannel2Color(Color color)
	 {
		 Color oldColor = channel2Color;
		 channel2Color = color;
		 firePropertyChange("channel2Color", oldColor, color);
	 }
	 /**
	  * Returns the color the histogram of the second channel (typically representing green) is plotted for a multi-channel/color image.
	  * Use {@link #getChannelGrayColor()} to obtain the color of a histogram for grayscale images.
	  * @return Color in which the second channel of a color image is plotted.
	  */
	 public Color getChannel2Color()
	 {
		 return channel2Color;
	 }
	 
	 /**
	  * Sets the color the histogram of the third channel (typically representing blue) is plotted for a multi-channel/color image.
	  * Use {@link #setChannelGrayColor(Color)} to define the color of a histogram for grayscale images.
	  * @param color Color in which the third channel of a color image is plotted.
	  */
	 public void setChannel3Color(Color color)
	 {
		 Color oldColor = channel3Color;
		 channel3Color = color;
		 firePropertyChange("channel3Color", oldColor, color);
	 }
	 /**
	  * Returns the color the histogram of the third channel (typically representing blue) is plotted for a multi-channel/color image.
	  * Use {@link #getChannelGrayColor()} to obtain the color of a histogram for grayscale images.
	  * @return Color in which the third channel of a color image is plotted.
	  */
	 public Color getChannel3Color()
	 {
		 return channel3Color;
	 }
	 
	 /**
	  * Sets the color the histogram of any channel greater than the third one is plotted for a multi-channel/color image.
	  * This might happen if different color schemes than RGB are used, or if channels do not directly correspond to colors but, e.g., to multiple cameras.
	  * Use {@link #setChannelGrayColor(Color)} to define the color of a histogram for grayscale images.
	  * @param color Color in which any channel greater than three is plotted.
	  */
	 public void setChannelAdditionalColor(Color color)
	 {
		 Color oldColor = channelAdditionalColor;
		 channelAdditionalColor = color;
		 firePropertyChange("channelAdditionalColor", oldColor, color);
	 }
	 /**
	  * Returns the color the histogram of any channel greater than the third one is plotted for a multi-channel/color image.
	  * This might happen if different color schemes than RGB are used, or if channels do not directly correspond to colors but, e.g., to multiple cameras.
	  * Use {@link #getChannelGrayColor()} to define the color of a histogram for grayscale images.
	  * @return Color in which any channel greater than three is plotted.
	  */
	 public Color getChannelAdditionalColor()
	 {
		 return channelAdditionalColor;
	 }
	 
	/**
	 * Sets the amount of pixels (in percent) which is set to black (lowerCutoff) and white (upperCutoff) in the auto-adjustment algorithm. It has to be given that
	 * 0 <= lowerCutoff < 1,
	 * 0 <= upperCutoff < 1,
	 * lowerCutoff + upperCutoff < 1.
	 * Even though any allowed value can be chosen, it is recommended to set both cutoffs either to {@link #AUTO_ADJUST_ROBUST} or {@link #AUTO_ADJUST_STRICT}.
	 * @param lowerCutoff The percentage of pixels which gets set to be black.
	 * @param upperCutoff The percentage of pixels which gets set to be white.
	 * 
	 * @throws IllegalArgumentException Thrown if the bounds for the arguments are violated.
	 */
	public void setAutoAdjustmentCutoffPercentages(double lowerCutoff, double upperCutoff)
	{
		if(this.lowerAutoAdjustmentCutoffPercentage == lowerCutoff && this.upperAutoAdjustmentCutoffPercentage == upperCutoff)
			return;
		if(0 > lowerCutoff || 0 > upperCutoff || lowerCutoff + upperCutoff >= 1)
			throw new IllegalArgumentException("Both cuttoffs have to be >= 0 and < 1 and their sum has to be < 1.");

		double oldLower = this.lowerAutoAdjustmentCutoffPercentage;
		double oldUpper = this.upperAutoAdjustmentCutoffPercentage;
		this.lowerAutoAdjustmentCutoffPercentage = lowerCutoff;
		this.upperAutoAdjustmentCutoffPercentage = upperCutoff;
		firePropertyChange("lowerAutoAdjustmentCutoffPercentage", oldLower, lowerCutoff);
		firePropertyChange("upperAutoAdjustmentCutoffPercentage", oldUpper, upperCutoff);
	}

	/**
	 * Returns the percentage of pixels (0 <= lowerCutoff < 1) which is set in the auto-adjustment to be black.
	 * @return Amount of pixels which is set to black in auto-adjustment.
	 */
	public double getLowerAutoAdjustmentCutoffPercentage()
	{
		return lowerAutoAdjustmentCutoffPercentage;
	}

	/**
	 * Returns the percentage of pixels (0 <= upperCutoff < 1) which is set in the auto-adjustment to be white.
	 * @return Amount of pixels which is set to white in auto-adjustment.
	 */
	public double getUpperAutoAdjustmentCutoffPercentage()
	{
		return upperAutoAdjustmentCutoffPercentage;
	}

	/**
	 * Sets if the lower and upper cutoffs are adjusting automatically.
	 * @param autoAdjusting True if histogram is auto-adjusting cutoffs.
	 */
	public void setAutoAdjusting(boolean autoAdjusting)
	{
		if(autoAdjusting == this.autoAdjusting)
			return;
		this.autoAdjusting = autoAdjusting;
		firePropertyChange("autoAdjusting", !autoAdjusting, autoAdjusting);
		if(autoAdjusting)
			autoAdjust();
	}
	
	/**
	 * Call this function to automatically adjust the histogram of the current image. However, the histogram of any new image will not be automatically adjusted (see {@link #setAutoAdjusting(boolean)}.
	 * Instead, the last adjustment will be used, except {@link #isAutoAdjusting()} returns true.
	 * Is automatically called for any new image if {@link #isAutoAdjusting()} returns true.
	 */
	public void autoAdjust()
	{
		int[][] bins = this.histogram;
		if(bins == null)
		{
			adjust(0,1);
			return;
		}
		// Always ignore alpha band in auto adjustment.
		double absoluteLowerCuttof = 1.0;
		double absoluteUpperCuttof = 0.0;
		for(int band = 0; band < bins.length && band < 3; band++)
		{
			int pixels = 0;
			for(int bin : bins[band])
			{
				pixels += bin;
			}

			int lowerBorder = (int)(pixels * lowerAutoAdjustmentCutoffPercentage);
			int lowerCuttof = 0;
			for(int temp = 0; lowerCuttof < bins[band].length - 1; lowerCuttof++)
			{
				temp += bins[band][lowerCuttof];
				if(temp > lowerBorder)
				{
					if(lowerCuttof>0)
						lowerCuttof--;
					break;
				}
			}
			int upperBorder = (int)(pixels * upperAutoAdjustmentCutoffPercentage);
			int upperCuttof = bins[band].length - 1;
			for(int temp = 0; upperCuttof > lowerCuttof; upperCuttof--)
			{
				temp += bins[band][upperCuttof];
				if(temp > upperBorder)
				{
					if(upperCuttof+1<bins[band].length)
						upperCuttof++;
					break;
				}
			}
			double relLowerCuttof = (lowerCuttof) / (bins[band].length-1.);
			double relUpperCuttof = (upperCuttof) / (bins[band].length-1.);
			if(relLowerCuttof < absoluteLowerCuttof)
				absoluteLowerCuttof = relLowerCuttof;
			if(relUpperCuttof > absoluteUpperCuttof)
				absoluteUpperCuttof = relUpperCuttof;
		}
		adjust(absoluteLowerCuttof, absoluteUpperCuttof);
	}
	/**
	 * Call this function to manually adjust the histogram of the current image. Subsequent images will use the same adjustment, except {@link #isAutoAdjusting()} returns true.
	 * @param minIntensity Minimal (relative) pixel intensity. Must lie between zero and one.
	 * @param maxIntensity Maximal (relative) pixel intensity. Must lie between zero and one, and be greater than minIntensity.
	 */
	public void adjust(double minIntensity, double maxIntensity)
	{
		double oldMinIntensity = this.minIntensity;
		double oldMaxIntensity = this.maxIntensity;
		this.minIntensity = minIntensity;
		this.maxIntensity = maxIntensity;
		if(minIntensity != oldMinIntensity)
			firePropertyChange("minIntensity", oldMinIntensity, minIntensity);
		if(maxIntensity != oldMaxIntensity)
			firePropertyChange("maxIntensity", oldMaxIntensity, maxIntensity);
	}
	/**
	 * Gets the current lower cutoff / minimum intensity the histogram is adjusted to.
	 * @return current minimal intensity.
	 */
	public double getLowerCutoff()
	{
		return minIntensity;
	}
	
	/**
	 * Gets the current upper cutoff / maximum intensity the histogram is adjusted to.
	 * @return current maximum intensity.
	 */
	public double getUpperCutoff()
	{
		return maxIntensity;
	}
	
	/**
	 * Returns if the lower and upper cutoffs are adjusting automatically.
	 * @return True if histogram is auto-adjusting cutoffs.
	 */
	public boolean isAutoAdjusting()
	{
		return autoAdjusting;
	}
	
	/**
	 * Returns the current (equidistant) bins of the histogram for all channels.
	 * @return Bins of histogram.
	 */
	public int[][] getHistogramBins()
	{
		return histogram;
	}
	
	
	/**
	 * Sets the image for which the histogram should be displayed.
	 * @param bufferedImage Image for which the histogram should be displayed.
	 * @throws ImageConvertException Thrown if histogram could not be calculated from image.
	 */
	public void setImage(BufferedImage bufferedImage) throws ImageConvertException
	{
		int[][] bins = ImageTools.getHistogram(bufferedImage, numBins);
		
		int[][] oldBins;
		synchronized(this)
		{
			oldBins = this.histogram;
			this.lastBufferedImage = bufferedImage;
			this.lastImageEvent = null;
			this.histogram = bins;
		}
		firePropertyChange("histogram", oldBins, bins);
		
		boolean oldOverExposed = this.overExposed;
		long maxExpectedVal = ImageTools.getMaximalExpectedPixelValue(bufferedImage);
		if(maxExpectedVal > 0)
		{
			long maxVal = ImageTools.getMaximalPixelValue(bufferedImage);
			if(maxVal <= 0)
				overExposed = false;
			else if(maxVal >= maxExpectedVal)
				this.overExposed = true;
			else
				this.overExposed = false;
		}
		else
			this.overExposed = false;
		if(oldOverExposed != overExposed)
			firePropertyChange("overExposed", oldOverExposed, overExposed);
		
		
		if(autoAdjusting)
			autoAdjust();
	}
	
	/**
	 * Sets the image for which the histogram should be displayed.
	 * @param imageEvent Image for which the histogram should be displayed.
	 * @throws ImageConvertException Thrown if histogram could not be calculated from image.
	 */
	public synchronized void setImage(ImageEvent<?> imageEvent) throws ImageConvertException
	{
		int[][] bins = ImageTools.getHistogram(imageEvent, numBins);
		
		int[][] oldBins = this.histogram;
		synchronized(this)
		{
			this.lastBufferedImage = null;
			this.lastImageEvent = imageEvent;
			this.histogram = bins;
		}
		firePropertyChange("histogram", oldBins, bins);
		
		boolean oldOverExposed = this.overExposed;
		long maxExpectedVal = ImageTools.getMaximalExpectedPixelValue(imageEvent);
		if(maxExpectedVal > 0)
		{
			long maxVal = ImageTools.getMaximalPixelValue(imageEvent);
			if(maxVal <= 0)
				overExposed = false;
			else if(maxVal >= maxExpectedVal)
				this.overExposed = true;
			else
				this.overExposed = false;
		}
		else
			this.overExposed = false;
		if(oldOverExposed != overExposed)
			firePropertyChange("overExposed", oldOverExposed, overExposed);
		
		if(autoAdjusting)
			autoAdjust();
	}
	
	/**
	 * Adds an action listener which gets notified if the histogram range changed.
	 * @param listener Listener for histogram range.
	 */
	public void addActionListener(ActionListener listener)
	{
		synchronized (actionListeners) 
		{
			actionListeners.add(listener);
		}
	}
	/**
	 * Removes a previously added listener.
	 * @param listener listener to remove.
	 */
	public void removeActionListener(ActionListener listener)
	{
		synchronized (actionListeners) 
		{
			actionListeners.remove(listener);
		}
	}
	/**
	 * Returns all action listeners added to this histogram panel via {@link #addActionListener(ActionListener)}.
	 * @return Action listeners.
	 */
	public ActionListener[] getActionListeners()
	{
		synchronized (actionListeners) 
		{
			return actionListeners.toArray(new ActionListener[actionListeners.size()]);
		}
	}

	/**
	 * Set to true to display the histogram bins on a logarithmic y-axis, false for a linear axis.
	 * @param logarithmic true for logarithmic axis.
	 */
	public void setLogarithmic(boolean logarithmic) 
	{
		if(logarithmic == this.logarithmic)
			return;
		this.logarithmic = logarithmic;
		firePropertyChange("logarithmic", !logarithmic, logarithmic);
	}
	
	/**
	 * Returns true, if the histogram bins are displayed on a logarithmic y-axis, false for a linear axis.
	 * @return true for logarithmic axis.
	 */
	public boolean isLogarithmic() 
	{
		return logarithmic;
	}
}
