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
package org.youscope.plugin.youpong;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.rmi.RemoteException;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.image.ImageEvent;
import org.youscope.common.image.ImageListener;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.measurement.MeasurementException;
import org.youscope.common.microscope.FloatProperty;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.util.ImageConvertException;
import org.youscope.common.util.ImageTools;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * @author langmo
 *
 */
class YouPongField extends JPanel implements Runnable
{
	/**
	 * Serial version UID.
	 */
	private static final long	serialVersionUID	= -6054774251900824502L;
	private volatile boolean shouldRun = false;
	private volatile boolean isConfigured = false;
	
	private final static int REFRESH_PERIOD = 100;
	private final static int REFRESH_PERIODS_TO_TRAVEL_MAX = 40;
	private final static int REFRESH_PERIODS_TO_TRAVEL_MIN = 10;
	private int refreshPeriodsToTravel = REFRESH_PERIODS_TO_TRAVEL_MAX;
	private final static int REFRESH_PADDLE_POSITIONS_PERIOD = 100;
	private volatile double ballX = 0.25;
	private volatile double ballY = 0.25;
	private volatile double ballSpeedX = 1;
	private volatile double ballSpeedY = 0.25;
	
	private static final double PADDLE_DIAMETER = 0.2;
	private static final int PADDLE_WIDTH = 8;
	private static final int BALL_WIDTH = 10;
	private static final double BALL_MAX_SPEED = 0.5;
	
	private final String[] leftPaddleControl = new String[]{"", ""};
	private final String[] rightPaddleControl = new String[]{"", ""};
	
	private volatile double leftPaddleY = 0.5;
	private volatile double rightPaddleY = 0.5;
	
	private volatile double leftPaddleMax = 0;
	private volatile double leftPaddleMin = 0;
	
	private volatile double rightPaddleMax = 0;
	private volatile double rightPaddleMin = 0;
	
	private volatile int scoreLeft = 0;
	private volatile int scoreRight = 0;
	
	private volatile BufferedImage image = null;
	
	private YouScopeServer server;
	private YouPongSounds sounds;
	private YouScopeClient client;
	
	private volatile Measurement measurement = null;
	
	private final YouPongConfiguration configuration;
	YouPongField(YouScopeClient client, YouScopeServer server)
	{
		this.server = server;
		this.client = client;
		sounds = new YouPongSounds(client);
		configuration = new YouPongConfiguration(server, client);
		
		setBorder(new EmptyBorder(30, 30, 30, 30));
		setLayout(new BorderLayout());
		add(configuration);
	}
	
	@Override
	public Dimension getPreferredSize()
	{
		if(image != null)
			return new Dimension(image.getWidth(), image.getHeight());
		return new Dimension(200, 200);
	}
	
	
	
	@Override
	public void run()
	{
		
		// Run YouPong
		while(shouldRun)
		{
			if(isConfigured)
			{
				// Move ball
				ballX += ballSpeedX / refreshPeriodsToTravel;
				ballY += ballSpeedY / refreshPeriodsToTravel;
				
				// Detect if ball did hit the wall. If yes, reflect
				if(ballY < 0)
				{
					ballY = -ballY;
					ballSpeedY *= -1;
					sounds.playHit();
				}
				else if(ballY > 1)
				{
					ballY = 1 - (ballY - 1);
					ballSpeedY *= -1;
					sounds.playHit();
				}
				
				// Detect if somebody scored or hit the ball.
				if(ballX <= 0)
				{
					if(ballY >= leftPaddleY - PADDLE_DIAMETER/2 && ballY <= leftPaddleY + PADDLE_DIAMETER/2)
					{
						ballX = 0;
						ballSpeedX = 1;
						ballSpeedY = 2 * (ballY - leftPaddleY) / PADDLE_DIAMETER * BALL_MAX_SPEED;
						if(refreshPeriodsToTravel > REFRESH_PERIODS_TO_TRAVEL_MIN)
							refreshPeriodsToTravel--;
						sounds.playHit();
					}
					else
					{
						scoreRight++;
						ballX = 0.75;
						ballY = 0.75;
						ballSpeedX = -1;
						ballSpeedY = -0.25;
						refreshPeriodsToTravel = REFRESH_PERIODS_TO_TRAVEL_MAX;
						sounds.playLoose();
					}
				}
				else if(ballX >= 1)
				{
					if(ballY >= rightPaddleY - PADDLE_DIAMETER/2 && ballY <= rightPaddleY + PADDLE_DIAMETER/2)
					{
						ballX = 1;
						ballSpeedX = -1;
						ballSpeedY = 2 * (ballY - rightPaddleY) / PADDLE_DIAMETER * BALL_MAX_SPEED;
						if(refreshPeriodsToTravel > REFRESH_PERIODS_TO_TRAVEL_MIN)
							refreshPeriodsToTravel--;
						sounds.playHit();
					}
					else
					{
						scoreLeft++;
						ballX = 0.25;
						ballY = 0.25;
						ballSpeedX = 1;
						ballSpeedY = 0.25;
						refreshPeriodsToTravel = REFRESH_PERIODS_TO_TRAVEL_MAX;
						sounds.playLoose();
					}
				}
			}
			repaint();
			try
			{
				Thread.sleep(REFRESH_PERIOD);
			}
			catch(InterruptedException e)
			{
				client.sendError("YouPong thread was interrupted. Stopping", e);
				return;
			}
		}	
	}
	
	double[] getPaddlePositions() throws MicroscopeException
	{
		double[] returnVal = new double[2];
		try
		{
			synchronized(leftPaddleControl)
			{
				if(leftPaddleControl[0].length() <= 0 || leftPaddleControl[1].length() <= 0)
				{
					returnVal[0] = 0.5;
				}
				else
				{
					double leftVal = ((FloatProperty)server.getMicroscope().getDevice(leftPaddleControl[0]).getProperty(leftPaddleControl[1])).getFloatValue();
					returnVal[0] = (leftVal - leftPaddleMin) / (leftPaddleMax - leftPaddleMin);
				}
			}
			synchronized(rightPaddleControl)
			{
				if(rightPaddleControl[0].length() <= 0 || rightPaddleControl[1].length() <= 0)
				{
					returnVal[1] = 0.5;
				}
				else
				{
					double rightVal = ((FloatProperty)server.getMicroscope().getDevice(rightPaddleControl[0]).getProperty(rightPaddleControl[1])).getFloatValue();
					returnVal[1] = (rightVal - rightPaddleMin) / (rightPaddleMax - rightPaddleMin);
				}
			}
			return returnVal;
		}
		catch(Exception e)
		{
			throw new MicroscopeException("Could not get paddle positions.", e);
		}
	}
	
	void createUI()
	{
		configuration.createUI();
		configuration.addImagingConfigurationListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				startContinuousImaging();
			}
		});
		configuration.addConfigurationFinishedListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				isConfigured = true;
			}
		});
		
		configuration.addControlConfigurationListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				actualizePaddleControls();
			}
		});
	}
	
	private void actualizePaddleControls()
	{
		synchronized(leftPaddleControl)
		{
			String[] controls = configuration.getPlayerControl(true);
			leftPaddleControl[0] = controls[0];
			leftPaddleControl[1] = controls[1];
			leftPaddleMin = configuration.getPlayerMin(true);
			leftPaddleMax = configuration.getPlayerMax(true);
			if(leftPaddleMin > leftPaddleMax)
			{
				double temp = leftPaddleMin;
				leftPaddleMin = leftPaddleMax;
				leftPaddleMax = temp;
			}
			else if(leftPaddleMax == leftPaddleMin)
			{
				leftPaddleMin--;
				leftPaddleMax++;
			}
		}
		
		synchronized(rightPaddleControl)
		{
			String[] controls = configuration.getPlayerControl(false);
			rightPaddleControl[0] = controls[0];
			rightPaddleControl[1] = controls[1];
			rightPaddleMin = configuration.getPlayerMin(false);
			rightPaddleMax = configuration.getPlayerMax(false);
			if(rightPaddleMin > rightPaddleMax)
			{
				double temp = rightPaddleMin;
				rightPaddleMin = rightPaddleMax;
				rightPaddleMax = temp;
			}
			else if(rightPaddleMax == rightPaddleMin)
			{
				rightPaddleMin--;
				rightPaddleMax++;
			}
		}
	}
	
	private synchronized void stopContinuousImaging()
	{
		if(measurement != null)
		{
			try
			{
				measurement.stopMeasurement(false);
			}
			catch(RemoteException | MeasurementException e)
			{
				client.sendError("Could not stop continuous imaging.", e);
			}
		}
	}
	
	private synchronized void startContinuousImaging()
	{
		stopContinuousImaging();
		String channelGroup = configuration.getChannelGroup();
		String channel = configuration.getChannel();
		int imagingPeriod = configuration.getImagingPeriod();
		double exposure = configuration.getExposure();
		try
		{
			measurement = server.getMeasurementProvider().createContinuousMeasurement(null, channelGroup, channel, imagingPeriod, exposure, new ImageListener()
			{
				@Override
				public void imageMade(ImageEvent<?> e) throws RemoteException
				{
					BufferedImage image;
			        try
					{
						image = ImageTools.getMicroscopeImage(e);
					}
					catch(ImageConvertException e1)
					{
						client.sendError("Image from microscope could not be processed.", e1);
						stop();
						return;
					}
			        setImage(image);
				}
			});
			measurement.startMeasurement();
		}
		catch(Exception e)
		{
			client.sendError("Could not create continuous imaging measurement.", e);
		}
	}
	
	synchronized void start()
	{
		actualizePaddleControls();
		startContinuousImaging();
		shouldRun = true;
		
		// Start paddle position actualization thread
		Thread paddleThread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						while(shouldRun)
						{
					
							// Actualize paddle positions
							double[] currentPositions = getPaddlePositions();
							leftPaddleY = currentPositions[0];
							rightPaddleY = currentPositions[1];
							if(leftPaddleY < PADDLE_DIAMETER/2)
								leftPaddleY = PADDLE_DIAMETER/2;
							else if(leftPaddleY > 1 - PADDLE_DIAMETER/2)
								leftPaddleY = 1 - PADDLE_DIAMETER/2;
							if(rightPaddleY < PADDLE_DIAMETER/2)
								rightPaddleY = PADDLE_DIAMETER/2;
							else if(rightPaddleY > 1 - PADDLE_DIAMETER/2)
								rightPaddleY = 1 - PADDLE_DIAMETER/2;
							
							Thread.sleep(REFRESH_PADDLE_POSITIONS_PERIOD);
						}
					}
					catch(Exception e)
					{
						client.sendError("YouPong could not obtain current paddle positions. Stopping actualizing them.", e);
					}
				}
			});
		paddleThread.start();
		
		new Thread(this).start();
	}
	synchronized void stop()
	{
		shouldRun = false;
		stopContinuousImaging();
	}
	
	private synchronized void setImage(BufferedImage image)
	{
		this.image = image;
	}
	
	@Override
    public synchronized void paintComponent(Graphics grp)
    {
		int width = getWidth();
		int height = getHeight();
		
		// Draw background.
		BufferedImage image = this.image;
        grp.setColor(Color.LIGHT_GRAY);
        grp.fillRect(0, 0, width, height);
        if (image != null)
        {
        	double imageWidth = image.getWidth(this);
            double imageHeight = image.getHeight(this);
            if (getWidth() / imageWidth > getHeight() / imageHeight)
            {
                imageWidth = imageWidth * getHeight() / imageHeight;
                imageHeight = getHeight();
            } 
            else
            {
                imageHeight = imageHeight * getWidth() / imageWidth;
                imageWidth = getWidth();
            }

            // draw the image
            grp.drawImage(image, (int) (getWidth() - imageWidth) / 2, (int) (getHeight() - imageHeight) / 2, (int) imageWidth, (int) imageHeight, this);
        }
        // Draw points
        String pointsString = "";
        if(scoreLeft < 10)
        	pointsString +="0";
        pointsString += Integer.toString(scoreLeft) + " : ";
        if(scoreRight < 10)
        	pointsString +="0";
        pointsString += Integer.toString(scoreRight);
        Font font = new Font(Font.MONOSPACED, Font.BOLD, 60);
        grp.setFont(font);
        int stringWidth = grp.getFontMetrics().stringWidth(pointsString);
        int stringHeight = grp.getFontMetrics().getAscent();
        grp.setColor(new Color(0F, 0F, 0F, 0.2F));
        grp.drawString(pointsString, (width - stringWidth) / 2 , (height + stringHeight)/2);
        
        // Draw walls
        grp.setColor(new Color(0F, 0F, 0F, 0.4F));
        grp.fillRect(0, 0, width, (int)(height * 0.1));
        grp.fillRect(0, (int)(height * 0.9), width, (int)(height * 0.1));
        grp.setColor(Color.BLACK);
        grp.drawRect(-1, -1, width +2, (int)(height * 0.1) + 1);
        grp.drawRect(-1, (int)(height * 0.9), width + 2, (int)(height * 0.1) + 1);
        
        // Draw paddles
        grp.setColor(Color.BLUE);
        grp.fillRoundRect((int)((0.1*width) - PADDLE_WIDTH), (int)((0.1 + 0.8 * (leftPaddleY - PADDLE_DIAMETER /2)) * height), PADDLE_WIDTH, (int)(PADDLE_DIAMETER * 0.8 * height), PADDLE_WIDTH/2, PADDLE_WIDTH/2);
        grp.fillRoundRect((int)((0.9*width)               ), (int)((0.1 + 0.8 * (rightPaddleY - PADDLE_DIAMETER /2)) * height), PADDLE_WIDTH, (int)(PADDLE_DIAMETER * 0.8 * height), PADDLE_WIDTH/2, PADDLE_WIDTH/2);
        grp.setColor(Color.BLACK);
        grp.drawRoundRect((int)((0.1*width) - PADDLE_WIDTH), (int)((0.1 + 0.8 * (leftPaddleY - PADDLE_DIAMETER /2)) * height), PADDLE_WIDTH, (int)(PADDLE_DIAMETER * 0.8 * height), PADDLE_WIDTH/2, PADDLE_WIDTH/2);
        grp.drawRoundRect((int)((0.9*width)               ), (int)((0.1 + 0.8 * (rightPaddleY - PADDLE_DIAMETER /2)) * height), PADDLE_WIDTH, (int)(PADDLE_DIAMETER * 0.8 * height), PADDLE_WIDTH/2, PADDLE_WIDTH/2);
        
        // Draw Ball
        grp.setColor(Color.RED);
        grp.fillOval((int)((0.1 + 0.8 * ballX)*width - BALL_WIDTH / 2), (int)((0.1 + 0.8 * ballY)*height - BALL_WIDTH / 2), BALL_WIDTH, BALL_WIDTH);
        grp.setColor(Color.BLACK);
        grp.drawOval((int)((0.1 + 0.8 * ballX)*width - BALL_WIDTH / 2), (int)((0.1 + 0.8 * ballY)*height - BALL_WIDTH / 2), BALL_WIDTH, BALL_WIDTH);
        
        //super.paintComponent(grp);
    }
}
