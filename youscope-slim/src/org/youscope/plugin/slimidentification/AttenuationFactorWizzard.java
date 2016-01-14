package org.youscope.plugin.slimidentification;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.youscope.clientinterfaces.StandardProperty;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.image.ImageEvent;
import org.youscope.common.util.ImageTools;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.CameraField;
import org.youscope.uielements.ChannelField;
import org.youscope.uielements.DoubleTextField;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.ImagePanel;
import org.youscope.uielements.ImagePanel.LineInfo;
import org.youscope.uielements.ImagePanel.LineListener;

class AttenuationFactorWizzard 
{
	private final YouScopeServer server;
	private final YouScopeClient client;
	private final ImagePanel imagePanel;
	private volatile ImageEvent<?> image = null;
	private volatile long[] lineProfile = null;
	private LineProfileComponent lineProfileComponent = new LineProfileComponent();
	class ChannelControl extends DynamicPanel
	{
		/**
		 * Serial Version UID.
		 */
		private static final long serialVersionUID = 6561875723795113098L;
		final CameraField cameraField;
		final ChannelField channelField;
		final DoubleTextField exposureField = new DoubleTextField();
		
		public ChannelControl()
		{
			cameraField = new CameraField(client, server);
			channelField = new ChannelField(client, server);
			
			exposureField.setMinimalValue(0);
			
			// Load settings
			if((boolean) client.getProperties().getProperty(StandardProperty.PROPERTY_STREAM_USE_DEFAULT_SETTINGS))
			{
				exposureField.setValue(client.getProperties().getProperty(StandardProperty.PROPERTY_STREAM_DEFAULT_EXPOSURE));
				cameraField.setCamera((String) client.getProperties().getProperty(StandardProperty.PROPERTY_STREAM_DEFAULT_CAMERA));
				channelField.setChannel((String)client.getProperties().getProperty(StandardProperty.PROPERTY_STREAM_DEFAULT_CHANNEL_GROUP), 
						(String)client.getProperties().getProperty(StandardProperty.PROPERTY_STREAM_DEFAULT_CHANNEL));
			}
			else
			{
				exposureField.setValue(client.getProperties().getProperty(StandardProperty.PROPERTY_STREAM_LAST_EXPOSURE));
				cameraField.setCamera((String) client.getProperties().getProperty(StandardProperty.PROPERTY_STREAM_LAST_CAMERA));
				channelField.setChannel((String)client.getProperties().getProperty(StandardProperty.PROPERTY_STREAM_LAST_CHANNEL_GROUP), 
						(String)client.getProperties().getProperty(StandardProperty.PROPERTY_STREAM_LAST_CHANNEL));
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
					try {
			    		image =server.getMicroscope().getCameraDevice(camera).makeImage(channelGroup, channel, exposure);
					} catch (Exception e) {
						client.sendError("Could not snap image.", e);
						return;
					}
					imagePanel.setImage(image);
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
		public AttenuationControl()
		{
            addFill(lineProfileComponent);
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
			long[] lineProfile = AttenuationFactorWizzard.this.lineProfile;
			if(lineProfile == null || lineProfile.length == 0)
				return;
			long maxValue = 0;
			long minValue = Long.MAX_VALUE;
			for(long pixel : lineProfile)
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
			int[] xpos = new int[lineProfile.length + 2];
			int[] ypos = new int[lineProfile.length + 2];
			for(int i = 0; i < lineProfile.length; i++)
			{
				xpos[i] = i * (width-1) / (lineProfile.length-1);
				ypos[i] = (int) (height - 1 - (height-1) * (lineProfile[i]-minValue) / (maxValue-minValue));
			}
			// Close polygon
			xpos[lineProfile.length] = width-1;
			ypos[lineProfile.length] = height - 1;
			xpos[lineProfile.length + 1] = 0;
			ypos[lineProfile.length + 1] = height - 1;
			g.setColor(new Color(0.2F, 0.2F, 0.6F));

			g.fillPolygon(xpos, ypos, xpos.length);
			g.setColor(Color.BLACK);
			g.drawPolygon(xpos, ypos, xpos.length);
			
			super.paintComponent(g);
		}
	}
	
	AttenuationFactorWizzard(YouScopeClient client, YouScopeServer server)
	{
		this.client = client;
		this.server = server;
		
		imagePanel = new ImagePanel(client);
		imagePanel.insertControl("Imaging", new ChannelControl(), 0);
		imagePanel.addControl("Line Profile", new AttenuationControl());
		
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
		ImageEvent<?> image = this.image;
		if(image == null || line == null)
			return;
		Point start = ImageTools.backTransformCoordinate(image, new Point(line.getX1(), line.getY1()));
		Point end = ImageTools.backTransformCoordinate(image, new Point(line.getX2(), line.getY2()));
		if(start == null || end == null || (start.x == end.x && start.y == end.y))
			return;
		
		int numPixels = Math.max(Math.abs(start.x-end.x), Math.abs(start.y-end.y))+1;
		long[] pixels = new long[numPixels];
		for(int i=0; i<numPixels; i++)
		{
			int x = (int) Math.round(start.x + ((double)i)/(numPixels-1)*(end.x-start.x));
			int y = (int) Math.round(start.y + ((double)i)/(numPixels-1)*(end.y-start.y));
			pixels[i] = ImageTools.getPixelValue(image, x, y);
		}
		lineProfile = pixels;
		lineProfileComponent.repaint();
	}
	
	public YouScopeFrame toFrame()
	{
		return imagePanel.toFrame();
	}
}
