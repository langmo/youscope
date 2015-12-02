/**
 * 
 */
package ch.ethz.csb.youscope.client;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import javax.swing.JLabel;
import javax.swing.Timer;
import javax.swing.border.LineBorder;

import ch.ethz.csb.youscope.shared.measurement.MeasurementState;

/**
 * @author langmo
 *
 */
class MeasurementStateField extends JLabel implements ActionListener
{
	/**
	 * Serial Version UID.
	 */
	private static final long		serialVersionUID	= -6135349752663788599L;
	protected MeasurementState		state				= MeasurementState.READY;
	private volatile BufferedImage	image				= null;
	private volatile Timer			timer				= null;
	private static final int		REFRESH_PERIOD		= 300;

	MeasurementStateField()
	{
		setBorder(new LineBorder(Color.BLACK, 1));
		this.setOpaque(false);
	}

	public synchronized void setState(MeasurementState state)
	{
		if(this.state != state)
		{
			boolean wasProcessing = isProcessing();
			this.state = state;
			image = null;
			if(isProcessing() != wasProcessing)
			{
				if(wasProcessing)
				{
					timer.stop();
					timer = null;
				}
				else
				{
					timer = new Timer(REFRESH_PERIOD, this);
					timer.start();
				}
			}
			repaint();
		}
	}

	private Color getColor()
	{
		switch(state)
		{
			case READY:
				return new Color(120, 250, 120);
			case UNQUEUED:
				return new Color(120, 250, 120);
			case QUEUED:
				return new Color(250, 250, 30);
			case RUNNING:
			case INITIALIZING:
			case UNINITIALIZING:
				return new Color(120, 250, 30);
			case ERROR:
				return new Color(250, 30, 30);
			case FINISHED:
				return new Color(120, 250, 120);
			case STOPPING:
				return new Color(250, 250, 30);
			case INTERRUPTING:
				return new Color(250, 250, 30);
			default:
				return new Color(150, 150, 150);
		}
	}

	private boolean isProcessing()
	{
		switch(state)
		{
			case READY:
			case UNQUEUED:
			case ERROR:
			case FINISHED:
				return false;
			case RUNNING:
			case QUEUED:
			case STOPPING:
			case INTERRUPTING:
				return true;
			default:
				return false;
		}
	}

	private synchronized void createImage()
	{
		int width = getWidth();
		int height = getHeight();
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Color color = getColor();
		if(isProcessing())
		{
			double mean = 0.75;
			double amplitude = 0.25;
			int[] colorComponents = new int[] {color.getRed(), color.getGreen(), color.getBlue()};
			WritableRaster raster = image.getRaster();
			for(int band = 0; band < raster.getNumBands() && band < 3; band++)
			{
				for(int i = 0; i < width; i++)
				{
					for(int j = 0; j < height; j++)
					{
						raster.setSample(i, j, band, colorComponents[band] * (mean + amplitude * Math.sin(2 * Math.PI * i / width)));
					}
				}
			}
		}
		else
		{
			Graphics g = image.getGraphics();
			g.setColor(color);
			g.fillRect(0, 0, width, height);
		}
	}

	private String getMessage()
	{
		switch(state)
		{
			case READY:
				return "Measurement Initialized.";
			case UNQUEUED:
				return "Measurement Unqueued.";
			case QUEUED:
				return "Measurement Queued.";
			case RUNNING:
				return "Measurement Running.";
			case ERROR:
				return "Error in Measurement.";
			case FINISHED:
				return "Measurement Finished.";
			case STOPPING:
				return "Measurement Stopping.";
			case INTERRUPTING:
				return "Measurement Interrupting.";
			default:
				return state.toString();
		}
	}

	@Override
	public synchronized void paintComponent(Graphics g)
	{
		if(image == null || image.getWidth() != getWidth() || image.getHeight() != getHeight())
			createImage();
		g.drawImage(image, 0, 0, this);
		setText(" " + getMessage());
		super.paintComponent(g);
	}

	@Override
	public synchronized void actionPerformed(ActionEvent arg0)
	{
		if(image == null)
			return;

		WritableRaster raster = image.getRaster();
		int speed = image.getWidth() / 20;
		int[] empty = null;
		int[] pixelData = raster.getPixels(0, 0, speed, image.getHeight(), empty);
		raster.setRect(-speed, 0, raster);
		raster.setPixels(image.getWidth() - speed, 0, speed, image.getHeight(), pixelData);
		repaint();
	}
}
