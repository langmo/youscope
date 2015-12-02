/**
 * 
 */
package ch.ethz.csb.youscope.client.uielements;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

/**
 * @author langmo
 * 
 */
public class HistogramPlot extends JPanel
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID		= 8148718404278251836L;
	private volatile int[][]				bins					= null;

	private final JPanel		leftComponent;
	private final JPanel		centerComponent;
	private final JPanel		rightComponent;
	private final JSplitPane	rightSplitPane;
	private final JSplitPane	leftSplitPane;

	private volatile boolean				autoAdjusting			= true;

	private volatile double				lowerAdjustmentCuttoff	= 0.03;					// in percent.
	private volatile double				upperAdjustmentCuttoff	= 0.03;					// in percent.

	private int					lastWidth				= -1;

	private volatile double min = 0;
	private volatile double max = 0;
	
	private boolean dividerActualizing = false;
	
	private final ArrayList<ActionListener> actionListeners = new ArrayList<ActionListener>();
	
	/**
	 * Constructor.
	 */
	public HistogramPlot()
	{
		this.setPreferredSize(new Dimension(50, 75));
		this.setMinimumSize(new Dimension(50, 75));
		leftComponent = new JPanel();
		leftComponent.setPreferredSize(new Dimension(0, 0));
		leftComponent.setMinimumSize(new Dimension(0, 0));
		leftComponent.setOpaque(true);
		leftComponent.setBackground(new Color(0.5F, 0.5F, 0.5F, 0.5F));
		centerComponent = new JPanel();
		centerComponent.setMinimumSize(new Dimension(1, 0));
		centerComponent.setOpaque(false);
		rightComponent = new JPanel();
		rightComponent.setPreferredSize(new Dimension(0, 0));
		rightComponent.setMinimumSize(new Dimension(0, 0));
		rightComponent.setOpaque(true);
		rightComponent.setBackground(new Color(0.5F, 0.5F, 0.5F, 0.5F));
		setLayout(new BorderLayout());
		rightSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, centerComponent, rightComponent);
		rightSplitPane.setBorder(null);
		rightSplitPane.setOpaque(false);
		rightSplitPane.setDividerLocation(1.0);
		rightSplitPane.setDividerSize(2);
		rightSplitPane.setResizeWeight(1);
		leftSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftComponent, rightSplitPane);
		leftSplitPane.setBorder(null);
		leftSplitPane.setOpaque(false);
		leftSplitPane.setDividerLocation(0);
		leftSplitPane.setResizeWeight(0);
		leftSplitPane.setDividerSize(2);
		setOpaque(false);
		setBorder(new LineBorder(Color.BLACK, 1));
		add(leftSplitPane, BorderLayout.CENTER);
		this.addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentResized(ComponentEvent e)
			{
				// When resizing, don't change the cuttoffs.
				dividerActualizing = true;
				int thisWidth = HistogramPlot.this.getWidth();
				if(lastWidth > 0)
				{
					double[] minMax = getMinMaxInternal();
					setMinMax(minMax[0] * (thisWidth) / (lastWidth), 1.0 - (1.0 - minMax[1]) * (thisWidth) / (lastWidth));
				}
				lastWidth = thisWidth;
				dividerActualizing = false;
				
			}

		});

		PropertyChangeListener splitPaneListener = new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent changeEvent)
			{
				if(dividerActualizing)
					return;
				String propertyName = changeEvent.getPropertyName();
		        if (propertyName.equals(JSplitPane.DIVIDER_LOCATION_PROPERTY)) 
		        {
					double[] minMax = getMinMaxInternal();
					setMinMaxInternal(minMax[0], minMax[1]);
					fireActionEvent();
		        }
			}
		};
		leftSplitPane.addPropertyChangeListener(splitPaneListener);
		rightSplitPane.addPropertyChangeListener(splitPaneListener);
	}
	
	private void fireActionEvent()
	{
		synchronized (actionListeners) 
		{
			for(ActionListener listener : actionListeners)
			{
				listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_FIRST, "min_max_changed"));
			}
		}
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
	 * Sets the amount of pixels (in percent) which is set to black (lowerAdjustmentCuttoff) and white (upperAdjustmentCuttoff) in the auto-adjustment algorithm. It has to be given that
	 * 0 <= lowerAdjustmentCuttoff < 1
	 * 0 <= upperAdjustmentCuttoff < 1
	 * lowerAdjustmentCuttoff + upperAdjustmentCuttoff < 1
	 * @param lowerAdjustmentCuttoff The percentage of pixels which gets set to be black.
	 * @param upperAdjustmentCuttoff The percentage of pixels which gets set to be white.
	 * 
	 * @throws IllegalArgumentException Thrown if the bounds for the arguments are violated.
	 */
	public void setAutoAdjustmentCuttoffs(double lowerAdjustmentCuttoff, double upperAdjustmentCuttoff)
	{
		if(0 > lowerAdjustmentCuttoff || 0 > upperAdjustmentCuttoff || lowerAdjustmentCuttoff + upperAdjustmentCuttoff >= 1)
			throw new IllegalArgumentException("Both cuttoffs have to be >= 0 and < 1 and their sum has to be < 1.");

		this.lowerAdjustmentCuttoff = lowerAdjustmentCuttoff;
		this.upperAdjustmentCuttoff = upperAdjustmentCuttoff;
	}

	/**
	 * Returns the percentage of pixels (0 <= lowerAdjustmentCuttoff < 1) which is set in the auto-adjustment to be black.
	 * @return Amount of pixels which is set to black in auto-adjustment.
	 */
	public double getLowerAdjustmentCuttoff()
	{
		return lowerAdjustmentCuttoff;
	}

	/**
	 * Returns the percentage of pixels (0 <= upperAdjustmentCuttoff < 1) which is set in the auto-adjustment to be white.
	 * @return Amount of pixels which is set to white in auto-adjustment.
	 */
	public double getUpperAdjustmentCuttoff()
	{
		return upperAdjustmentCuttoff;
	}

	/**
	 * Sets if the lower and upper cuttoffs are adjusting automatically.
	 * @param autoAdjusting
	 */
	public void setAutoAdjusting(boolean autoAdjusting)
	{
		this.autoAdjusting = autoAdjusting;
	}

	/**
	 * Returns an array with two elements, where the first element is the selected minimum and the second the selected maximum.
	 * @return Minimum and maximum.
	 */
	public double[] getMinMax()
	{
		return new double[]{min, max};
	}
	
	private double[] getMinMaxInternal()
	{
		double right = rightSplitPane.getMaximumDividerLocation()- rightSplitPane.getDividerLocation();
		double left = leftSplitPane.getDividerLocation();
		double total = leftSplitPane.getMaximumDividerLocation();
		return new double[]{left/total, 1-right/total};
	}

	/**
	 * Sets the minimum and the maximum of the histogram (in percentage of the total length).
	 * This method is thread save.
	 * @param min Minimum of the histogram cutoff (0-1)
	 * @param max Maximum of the histogram cutoff (0-1)
	 */
	public void setMinMax(double min, double max)
	{
		setMinMaxInternal(min, max);
		
		actualizeDividerLocations();
	}
	
	private void setMinMaxInternal(double min, double max)
	{
		this.min = min;
		this.max = max;
	}
	
	private void actualizeDividerLocations()
	{
		Runnable runner = new Runnable()
		{
			@Override
			public void run()
			{
				dividerActualizing = true;
				leftSplitPane.setDividerLocation(min);
				leftSplitPane.validate();
				rightSplitPane.setDividerLocation((max - min) / (1.0 - min));
				rightSplitPane.validate();
				dividerActualizing = false;
			}
		};
		
		if(SwingUtilities.isEventDispatchThread())
			runner.run();
		else
			SwingUtilities.invokeLater(runner);
	}

	/**
	 * Sets the histogram data.
	 * @param bins The bins of the histogram.
	 */
	public void setBins(int[][] bins)
	{
		synchronized(this)
		{
			this.bins = bins;
		}
		if(autoAdjusting)
		{
			adjust();
		}
		repaint();
	}
	
	/**
	 * Automatically adjusts the minimal and maximal value of the histogram to reasonable values.
	 */
	public void adjust()
	{
		int[][] bins = this.bins;
		if(bins == null)
			return;
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

			int lowerBorder = (int)(pixels * lowerAdjustmentCuttoff);
			int lowerCuttof = 0;
			for(int temp = 0; lowerCuttof < bins[band].length - 1; lowerCuttof++)
			{
				temp += bins[band][lowerCuttof];
				if(temp >= lowerBorder)
					break;
			}
			int upperBorder = (int)(pixels * upperAdjustmentCuttoff);
			int upperCuttof = bins[band].length - 1;
			for(int temp = 0; upperCuttof > lowerCuttof; upperCuttof--)
			{
				temp += bins[band][upperCuttof];
				if(temp >= upperBorder)
					break;
			}
			double relLowerCuttof = ((double)lowerCuttof) / ((double)bins[band].length);
			double relUpperCuttof = ((double)upperCuttof) / ((double)bins[band].length);
			if(relLowerCuttof < absoluteLowerCuttof)
				absoluteLowerCuttof = relLowerCuttof;
			if(relUpperCuttof > absoluteUpperCuttof)
				absoluteUpperCuttof = relUpperCuttof;
		}
		setMinMax(absoluteLowerCuttof, absoluteUpperCuttof);
	}

	@Override
	public synchronized void paintComponent(Graphics grp)
	{
		grp.setColor(Color.WHITE);
		grp.fillRect(0, 0, getWidth(), getHeight());
		Graphics2D g2D = (Graphics2D)grp;
		int width = this.getWidth();
		int height = this.getHeight();
		if(bins == null)
		{
			String text = "No data available.";
			// get metrics from the graphics
			FontMetrics metrics = g2D.getFontMetrics();
			// get the height of a line of text in this font and render context
			int textHeight = metrics.getHeight();
			// get the advance of my text in this font and render context
			int textWidth = metrics.stringWidth(text);
			grp.drawString(text, (width - textWidth) / 2, (height - textHeight) / 2);
			return;
		}
		for(int band = (bins.length==4 ? 1 : 0); band < bins.length; band++)
		{
			int maxValue = 0;
			for(int bin : bins[band])
			{
				if(bin > maxValue)
					maxValue = bin;
			}
			int[] xpos = new int[bins[band].length + 2];
			int[] ypos = new int[bins[band].length + 2];
			for(int i = 0; i < bins[band].length; i++)
			{
				xpos[i] = i * width / bins[band].length;
				ypos[i] = height - 1 - height * bins[band][i] / maxValue;
			}
			// Close polygon
			xpos[bins[band].length] = width - 1;
			ypos[bins[band].length] = height - 1;
			xpos[bins[band].length + 1] = 0;
			ypos[bins[band].length + 1] = height - 1;
			if(bins.length == 1)
			{
				// Grayscale image
				g2D.setColor(new Color(0.2F, 0.2F, 0.6F));
			}
			else
			{
				switch(band)
				{
					case 1:
						// Red
						g2D.setColor(new Color(0.8F, 0.2F, 0.2F));
						break;
					case 2:
						// Green
						g2D.setColor(new Color(0.2F, 0.8F, 0.2F, 0.5F));
						break;
					case 3:
						// Blue
						g2D.setColor(new Color(0.2F, 0.2F, 0.8F, 0.333F));
						break;
				}
			}

			g2D.fillPolygon(xpos, ypos, xpos.length);
			g2D.setColor(Color.BLACK);
			g2D.drawPolygon(xpos, ypos, xpos.length);
		}
		super.paintComponent(g2D);
	}
}
