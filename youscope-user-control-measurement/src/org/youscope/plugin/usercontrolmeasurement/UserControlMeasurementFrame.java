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
package org.youscope.plugin.usercontrolmeasurement;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.image.ImageEvent;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.ImagePanel;
import org.youscope.uielements.LiveStreamPanel;

/**
 * @author Moritz Lang
 *
 */
class UserControlMeasurementFrame extends JPanel
{
	/**
	 * Serial Version UID.
	 */
	private static final long			serialVersionUID		= -1499077134453605763L;

	private final ImagePanel imagePanel;
	private final LiveStreamPanel.ChannelControl channelControl;

	private final UserControlMeasurementCallbackImpl callback; 
	
	private final YouScopeFrame frame;
	
	/**
	 * Constructor.
	 * @param client Interface to the client.
	 * @param server Interface to the server.
	 * @param frame 
	 * @param callback the callback to contol.
	 * @throws RemoteException
	 * @throws InterruptedException
	 * @throws MicroscopeException
	 */
	public UserControlMeasurementFrame(final YouScopeClient client, final YouScopeServer server, final YouScopeFrame frame, final UserControlMeasurementCallbackImpl callback) throws RemoteException, InterruptedException, MicroscopeException
	{
		this.callback = callback;
		this.frame = frame;
		imagePanel = new ImagePanel(client);
		imagePanel.setUserChoosesAutoAdjustContrast(true);
		channelControl = new LiveStreamPanel.ChannelControl(client, server);
		channelControl.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				callback.sendChannelSettingsChanged();
			}
		});
		imagePanel.insertControl("Channel", channelControl, 0);

		
		JButton snapImageButton = new JButton("Snap Image");
		snapImageButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				frame.startLoading();
				UserControlMeasurementFrame.this.callback.sendSnapImage();
			}
		});
		setLayout(new BorderLayout());		
		add(imagePanel, BorderLayout.CENTER);
		add(snapImageButton, BorderLayout.SOUTH);
	}

	public void newImage(ImageEvent<?> event)
	{
		if(imagePanel != null)
			imagePanel.setImage(event);
	}
	
	public void snappedImage()
	{
		frame.endLoading();
	}
	
	public String getCurrentChannel()
	{
		return channelControl.getChannel();
	}

	public String getCurrentChannelGroup() 
	{
		return channelControl.getChannelGroup();
	}

	public double getCurrentExposure() 
	{
		return channelControl.getExposure();
	}
}
