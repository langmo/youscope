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
package org.youscope.plugin.slimidentification;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.youscope.clientinterfaces.StandardProperty;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.image.ImageEvent;
import org.youscope.common.util.ImageTools;
import org.youscope.plugin.slimidentification.PeakFinder.Peak;
import org.youscope.plugin.slimjob.SlimProperties;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.CameraField;
import org.youscope.uielements.ChannelField;
import org.youscope.uielements.DoubleTextField;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.ImagePanel;
import org.youscope.uielements.ImagePanel.LineInfo;
import org.youscope.uielements.ImagePanel.LineListener;

class AttenuationFactorWizard 
{
	private final YouScopeServer server;
	private final YouScopeClient client;
	private final ImagePanel imagePanel;
	private Object imageLock = new Object();
	private volatile ImageEvent<?> imageEvent = null;
	private volatile BufferedImage bufferedImage = null;
	private volatile Profile profile = null;
	private final AttenuationControl attenuationControl= new AttenuationControl(); 
	
	private final ArrayList<ActionListener> actionListeners = new ArrayList<ActionListener>(1);
	
	private void notifyListeners()
	{
		synchronized (actionListeners) 
		{
			for(ActionListener listener: actionListeners)
			{
				listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_FIRST, "attenuation factor changed"));
			}
		}
	}
	public void addActionListener(ActionListener listener)
	{
		synchronized(actionListeners)
		{
			actionListeners.add(listener);
		}
	}
	public void removeActionListener(ActionListener listener)
	{
		synchronized(actionListeners)
		{
			actionListeners.remove(listener);
		}
	}
	private class Profile
	{
		final long[] lineProfile;
		final Peak[] maxima;
		final Peak[] minima;
		Profile(long[] lineProfile, Peak[] maxima)
		{
			this.lineProfile = lineProfile;
			this.maxima = maxima;
			ArrayList<Peak> minima = new ArrayList<Peak>(2);
			for(int left = 0; left<maxima.length-1; left++)
			{
				int right = left+1;
				if(maxima[left].rightIdx < maxima[right].idx)
				{
					minima.add(new Peak(maxima[left].rightIdx, maxima[left].rightVal,maxima[left].idx, maxima[left].val, maxima[right].idx, maxima[right].val));
				}
				else if(maxima[right].leftIdx > maxima[left].idx)
				{
					minima.add(new Peak(maxima[right].leftIdx, maxima[right].leftVal,maxima[left].idx, maxima[left].val, maxima[right].idx, maxima[right].val));
				}
			}
			this.minima = minima.toArray(new Peak[minima.size()]);
		}
		double getAttenuationFactor()
		{
			if(maxima.length != 3 || minima.length != 2)
				return -1;
			
			double c = (minima[0].val+minima[1].val)/2.0;
			double a = (maxima[0].val+maxima[2].val)/2.0;
			double b = maxima[1].val;
			return Math.sqrt(c/(a+b)*2.0);
			
		}
	}
	class ChannelControl extends DynamicPanel
	{
		/**
		 * Serial Version UID.
		 */
		private static final long serialVersionUID = 6561875723795113098L;
		final CameraField cameraField;
		final ChannelField channelField;
		final DoubleTextField exposureField = new DoubleTextField();

		private final static String LAST_SLIM_IMAGE_PROPERTY = "YouScope.SLIM.lastImage";
		public ChannelControl()
		{
			cameraField = new CameraField(client, server);
			channelField = new ChannelField(client, server);
			
			exposureField.setMinimalValue(0);
			
			// Load settings
			if((boolean) client.getPropertyProvider().getProperty(StandardProperty.PROPERTY_STREAM_USE_DEFAULT_SETTINGS))
			{
				exposureField.setValue(client.getPropertyProvider().getProperty(StandardProperty.PROPERTY_STREAM_DEFAULT_EXPOSURE));
				cameraField.setCamera((String) client.getPropertyProvider().getProperty(StandardProperty.PROPERTY_STREAM_DEFAULT_CAMERA));
				channelField.setChannel((String)client.getPropertyProvider().getProperty(StandardProperty.PROPERTY_STREAM_DEFAULT_CHANNEL_GROUP), 
						(String)client.getPropertyProvider().getProperty(StandardProperty.PROPERTY_STREAM_DEFAULT_CHANNEL));
			}
			else
			{
				exposureField.setValue(client.getPropertyProvider().getProperty(StandardProperty.PROPERTY_STREAM_LAST_EXPOSURE));
				cameraField.setCamera((String) client.getPropertyProvider().getProperty(StandardProperty.PROPERTY_STREAM_LAST_CAMERA));
				channelField.setChannel((String)client.getPropertyProvider().getProperty(StandardProperty.PROPERTY_STREAM_LAST_CHANNEL_GROUP), 
						(String)client.getPropertyProvider().getProperty(StandardProperty.PROPERTY_STREAM_LAST_CHANNEL));
			}
			
			JLabel label;
			if(cameraField.isChoice())
			{
				label = new JLabel("Camera:");
				label.setForeground(Color.WHITE);
				add(label);
				add(cameraField);
			}
			
			label = new JLabel("Channel:");
			label.setForeground(Color.WHITE);
			add(label);
			add(channelField);
			
			label = new JLabel("Exposure (ms):");
			label.setForeground(Color.WHITE);
			add(label);
			add(exposureField);
			
			JButton takeImageButton = new JButton("Snap image");
			takeImageButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					takeImage();
				}
			});
			takeImageButton.setOpaque(false);
			add(takeImageButton);
			
			JButton loadImageButton = new JButton("Load Image");
			loadImageButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					loadImage();
				}
			});
			loadImageButton.setOpaque(false);
			add(loadImageButton);
		}
		private void loadImage()
		{
			JFileChooser fileChooser =
                    new JFileChooser(client.getPropertyProvider().getProperty(LAST_SLIM_IMAGE_PROPERTY, "/"));
            BufferedImage image;
            while(true)
            {
            	int returnVal = fileChooser.showDialog(null, "Load SLIM Attenuation Image");
            	if (returnVal != JFileChooser.APPROVE_OPTION)
            	{
            		return;
            	}
            	File file = fileChooser.getSelectedFile().getAbsoluteFile();
            	if(!file.exists())
            	{
            		JOptionPane.showMessageDialog(null, "File " + file.toString() + " does not exist.", "File does not exist", JOptionPane. INFORMATION_MESSAGE);
            		continue;
            	}
            	
            	
        		try 
        		{
        		    image = ImageIO.read(file);
        		} 
        		catch (IOException e) 
        		{
        			client.sendError("Loading of image file " + file.getAbsolutePath() + " failed because of I/O errors.", e);
        			JOptionPane.showMessageDialog(null, "Loading of image file " + file.getAbsolutePath() + " failed because of I/O errors.", "File could not be loaded", JOptionPane. INFORMATION_MESSAGE);
            		continue;
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
        			client.sendError("Could not load image file " + file.getAbsolutePath() + " since image type can not be read.\nSupported image types are: " + imageTypesString +".\nTo support more image types, please download an appropriete plugin for YouScope/Java.", null);
        			JOptionPane.showMessageDialog(null, "Could not load image file " + file.getAbsolutePath() + " since image type can not be read.", "Image Type Not Supported", JOptionPane. INFORMATION_MESSAGE);
            		continue;
        		}
            	
				break;
            }
            
            client.getPropertyProvider().setProperty(LAST_SLIM_IMAGE_PROPERTY, fileChooser
                    .getCurrentDirectory().getAbsolutePath());
            synchronized(imageLock)
            {
            	bufferedImage = image;
            	imageEvent = null;
            	imagePanel.setImage(image);
            }
		}
		private void takeImage()
		{
			final String channel = channelField.getChannel();
			final String channelGroup = channelField.getChannelGroup();
			final double exposure = exposureField.getValue();
			final String camera = cameraField.getCameraDevice();
			new Thread(new Runnable()
			{
				@Override
				public void run() 
				{
					try 
					{
						synchronized(imageLock)
						{
							imageEvent =server.getMicroscope().getCameraDevice(camera).makeImage(channelGroup, channel, exposure);
							bufferedImage = null;
							imagePanel.setImage(imageEvent);
						}
					} 
					catch (Exception e) {
						client.sendError("Could not snap image.", e);
						return;
					}
				}
			}).start();
		}
	}
	
	class AttenuationControl extends DynamicPanel
	{
		/**
		 * Serial Version UID.
		 */
		private static final long serialVersionUID = 6561875723795113098L;
		private JTextField max1Field = new JTextField();
		private JTextField max2Field = new JTextField();
		private JTextField max3Field = new JTextField();
		private JTextField min1Field = new JTextField();
		private JTextField min2Field = new JTextField();
		private JTextField attenuationFactorField = new JTextField();
		private LineProfileComponent lineProfileComponent = new LineProfileComponent();
		private JButton acceptButton = new JButton("Save Factor");
		public AttenuationControl()
		{
            addFill(lineProfileComponent);
            
            JPanel gridPanel = new JPanel(new GridLayout(5,2));
            gridPanel.setOpaque(false);
            
            JLabel max1Label = new JLabel("Max 1:");
            max1Label.setForeground(Color.GREEN);
            gridPanel.add(max1Label);
            max1Field.setEditable(false);
            gridPanel.add(max1Field);
            
            JLabel min1Label = new JLabel("Min 1:");
            min1Label.setForeground(Color.RED);
            gridPanel.add(min1Label);
            min1Field.setEditable(false);
            gridPanel.add(min1Field);
            
            JLabel max2Label = new JLabel("Max 2:");
            max2Label.setForeground(Color.GREEN);
            gridPanel.add(max2Label);
            max2Field.setEditable(false);
            gridPanel.add(max2Field);
            
            JLabel min2Label = new JLabel("Min 2:");
            min2Label.setForeground(Color.RED);
            gridPanel.add(min2Label);
            min2Field.setEditable(false);
            gridPanel.add(min2Field);
            
            JLabel max3Label = new JLabel("Max 3:");
            max3Label.setForeground(Color.GREEN);
            gridPanel.add(max3Label);
            max3Field.setEditable(false);
            gridPanel.add(max3Field);
            
            add(gridPanel);
            
            JLabel attenuationLabel = new JLabel("Attenuation Factor:");
            attenuationLabel.setForeground(Color.WHITE);
            add(attenuationLabel);
            attenuationFactorField.setEditable(false);
            add(attenuationFactorField);
            
            acceptButton.setOpaque(false);
            acceptButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					Profile profile = AttenuationFactorWizard.this.profile;
					double attenuationFactor = profile == null ? -1 : profile.getAttenuationFactor();
					if(attenuationFactor < 0)
						return;
					new SlimProperties(client).setAttenuationFactor(attenuationFactor);
					notifyListeners();
				}
			});
            add(acceptButton);
            actualize();
		}
		void actualize()
		{
			lineProfileComponent.repaint();
			Profile profile = AttenuationFactorWizard.this.profile;
			if(profile == null || profile.maxima.length < 1)
			{
				max1Field.setText("N.A.");
			}
			else
			{
				max1Field.setText(Long.toString(profile.maxima[0].val));
			}	
			
			if(profile == null || profile.maxima.length < 2)
			{
				max2Field.setText("N.A.");
			}
			else
			{
				max2Field.setText(Long.toString(profile.maxima[1].val));
			}	
			
			if(profile == null || profile.maxima.length < 3)
			{
				max3Field.setText("N.A.");
			}
			else
			{
				max3Field.setText(Long.toString(profile.maxima[2].val));
			}	
			
			if(profile == null || profile.minima.length < 1)
			{
				min1Field.setText("N.A.");
			}
			else
			{
				min1Field.setText(Long.toString(profile.minima[0].val));
			}	
			
			if(profile == null || profile.minima.length < 2)
			{
				min2Field.setText("N.A.");
			}
			else
			{
				min2Field.setText(Long.toString(profile.minima[1].val));
			}	
			
			if(profile == null)
			{
				attenuationFactorField.setText("N.A.");
				acceptButton.setEnabled(false);
			}
			else
			{
				double attenuationFactor = profile.getAttenuationFactor();
				if(attenuationFactor<0)
				{
					attenuationFactorField.setText("invalid profile");
					acceptButton.setEnabled(false);
				}
				else
				{
					attenuationFactorField.setText(String.format("%6.4f", attenuationFactor));
					acceptButton.setEnabled(true);
				}
			}
		}
		
	}
	
	class LineProfileComponent extends JComponent
	{
		/**
		 * Serial Version UID.
		 */
		private static final long serialVersionUID = 5253303147703913035L;

		LineProfileComponent()
		{
			setPreferredSize(new Dimension(50, 75));
            setMinimumSize(new Dimension(50, 75));
            setBackground(Color.WHITE);
            setForeground(Color.BLACK);
            setOpaque(true);
		}

		@Override
		protected void paintComponent(Graphics g) {
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
			g.setColor(getForeground());
			Profile profile = AttenuationFactorWizard.this.profile;
			if(profile == null || profile.lineProfile.length == 0)
				return;
			long maxValue = 0;
			long minValue = Long.MAX_VALUE;
			for(long pixel : profile.lineProfile)
			{
				if(maxValue < pixel)
					maxValue = pixel;
				if(minValue > pixel)
					minValue = pixel;
			}
			if(minValue >= maxValue)
				maxValue = minValue+1;
			int width = getWidth();
			int height = getHeight();
			int[] xpos = new int[profile.lineProfile.length + 2];
			int[] ypos = new int[profile.lineProfile.length + 2];
			for(int i = 0; i < profile.lineProfile.length; i++)
			{
				xpos[i] = i * (width-1) / (profile.lineProfile.length-1);
				ypos[i] = (int) (height - 1 - (height-1) * (profile.lineProfile[i]-minValue) / (maxValue-minValue));
			}
			// Close polygon
			xpos[profile.lineProfile.length] = width-1;
			ypos[profile.lineProfile.length] = height - 1;
			xpos[profile.lineProfile.length + 1] = 0;
			ypos[profile.lineProfile.length + 1] = height - 1;
			g.setColor(new Color(0.2F, 0.2F, 0.6F));

			g.fillPolygon(xpos, ypos, xpos.length);
			g.setColor(Color.BLACK);
			g.drawPolygon(xpos, ypos, xpos.length);
			
			g.setColor(Color.GREEN);
			for(Peak peak : profile.maxima)
			{
				g.drawLine(xpos[peak.idx], ypos[peak.idx], xpos[peak.idx], height - 1);
			}
			g.setColor(Color.RED);
			for(Peak peak : profile.minima)
			{
				g.drawLine(xpos[peak.idx], ypos[peak.idx], xpos[peak.idx], height - 1);
			}
			super.paintComponent(g);
		}
	}
	
	AttenuationFactorWizard(YouScopeClient client, YouScopeServer server)
	{
		this.client = client;
		this.server = server;
		
		imagePanel = new ImagePanel(client);
		imagePanel.setTitle("Attenuation Factor Wizard");
		imagePanel.insertControl("Imaging", new ChannelControl(), 0);
		imagePanel.addControl("Attenuation Factor", attenuationControl);
		
		imagePanel.addLineListener(new LineListener()
		{
			@Override
			public void lineChanged(LineInfo line) {
				calculateLine(line);
			}
		});
	}
	
	private void calculateLine(LineInfo line)
	{
		ImageEvent<?> imageEvent;
		BufferedImage bufferedImage;
		synchronized(imageLock)
		{
			imageEvent = this.imageEvent;
			bufferedImage = this.bufferedImage;
		}
		if((imageEvent == null && bufferedImage == null) || line == null)
			return;
		long[] pixels;
		if(imageEvent != null)
		{
			Point start = ImageTools.backTransformCoordinate(imageEvent, new Point(line.getX1(), line.getY1()));
			Point end = ImageTools.backTransformCoordinate(imageEvent, new Point(line.getX2(), line.getY2()));
			if(start == null || end == null || (start.x == end.x && start.y == end.y))
				return;
			
			int numPixels = Math.max(Math.abs(start.x-end.x), Math.abs(start.y-end.y))+1;
			pixels = new long[numPixels];
			for(int i=0; i<numPixels; i++)
			{
				int x = (int) Math.round(start.x + ((double)i)/(numPixels-1)*(end.x-start.x));
				int y = (int) Math.round(start.y + ((double)i)/(numPixels-1)*(end.y-start.y));
				pixels[i] = ImageTools.getPixelValue(imageEvent, x, y);
			}
		}
		else
		{
			Point start = new Point(line.getX1(), line.getY1());
			Point end = new Point(line.getX2(), line.getY2());
			if(start == null || end == null || (start.x == end.x && start.y == end.y))
				return;
			
			int numPixels = Math.max(Math.abs(start.x-end.x), Math.abs(start.y-end.y))+1;
			pixels = new long[numPixels];
			for(int i=0; i<numPixels; i++)
			{
				int x = (int) Math.round(start.x + ((double)i)/(numPixels-1)*(end.x-start.x));
				int y = (int) Math.round(start.y + ((double)i)/(numPixels-1)*(end.y-start.y));
				pixels[i] = ImageTools.getPixelValue(bufferedImage, x, y);
			}
		}
		
		Peak[] peaks = PeakFinder.findPeaks(pixels, 3).toArray(new Peak[0]);
		profile = new Profile(pixels, peaks);
		
		attenuationControl.actualize();
	}
	
	public YouScopeFrame toFrame()
	{
		return imagePanel.toFrame();
	}
}
