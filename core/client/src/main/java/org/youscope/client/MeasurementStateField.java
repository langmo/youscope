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
package org.youscope.client;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import javax.swing.JLabel;
import javax.swing.Timer;
import javax.swing.border.LineBorder;

import org.youscope.common.measurement.MeasurementState;

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
		setForeground(Color.BLACK);
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
			case QUEUED:
			case PAUSED:
				return new Color(250, 250, 30);
			case RUNNING:
			case INITIALIZING:
			case INITIALIZED:
				return new Color(120, 250, 30);
			case ERROR:
				return new Color(250, 30, 30);
			case UNINITIALIZED:
				return new Color(120, 250, 120);
			case STOPPING:
			case UNINITIALIZING:
			case STOPPED:
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
			case QUEUED:
			case ERROR:
			case PAUSED:
			case UNINITIALIZED:
			case INITIALIZED:
			case STOPPED:
				return false;
			case RUNNING:
			case STOPPING:
			case INITIALIZING:
			case UNINITIALIZING:
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
				return "Measurement Ready.";
			case QUEUED:
				return "Measurement Queued.";
			case ERROR:
				return "Error in Measurement.";
			case PAUSED:
				return "Measurement Paused.";
			case UNINITIALIZED:
				return "Measurement Finished.";
			case RUNNING:
				return "Measurement Running.";
			case STOPPING:
				return "Measurement Stopping.";
			case INITIALIZING:
				return "Measurement Initializing.";
			case INITIALIZED:
				return "Measurement Initialized.";
			case UNINITIALIZING:
				return "Measurement Uninitializing.";
			case STOPPED:
				return "Measurement Stopped.";
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
