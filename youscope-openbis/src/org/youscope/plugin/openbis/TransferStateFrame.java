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
package org.youscope.plugin.openbis;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.StandardFormats;

/**
 * @author Moritz Lang
 *
 */
class TransferStateFrame implements Runnable
{
	private final JProgressBar waitBar = new JProgressBar(0, 100);
	private final TransferSettings settings;
	private final YouScopeServer server;
	private final YouScopeClient client;
	private final Timer messageUpdater = new Timer(100, new MessageUpdater());
	private final static GridBagConstraints newLineCnstr = StandardFormats.getNewLineConstraint();
	
	private final JTextArea messageArea = new JTextArea();
	
	private final JButton stopButton = new JButton("Interrupt Transfer");
		
	private final YouScopeFrame frame;
	
	private volatile String newMessages = "";
	
	private int repeatMessageQuerying = -1;
	
	private OpenBISAddon addon = null;
	
	public TransferStateFrame(YouScopeFrame frame, YouScopeServer server, YouScopeClient client, TransferSettings settings)
	{
		this.settings = settings;
		this.server = server;
		this.client = client;
		this.frame = frame;
		
		// Set frame properties
		frame.setTitle("0% - Uploading " + settings.userID + "/" + settings.projectID + "/" + settings.measurementID);
		frame.setResizable(true);
		frame.setClosable(false);
		frame.setMaximizable(false);
		
		final GridBagLayout elementsLayout = new GridBagLayout();
		JPanel elementsPanel = new JPanel(elementsLayout);
		JEditorPane descriptionPane = new JEditorPane();
		descriptionPane.setEditable(false);
		descriptionPane.setContentType("text/html");
		descriptionPane.setText("<html><p style=\"font-size:small;margin-top:0px;\"><b>Uploading measurement to OpenBIS.</b></p>" +
				"<p style=\"font-size:small;margin-top:4px;margin-bottom:0px\">The transfer might take, depending on the size of the data, several minutes.</p>" +
				"<p style=\"font-size:small;margin-top:4px;margin-bottom:0px\">Uploading from: "+settings.measurementFolder+"</p>" +
				"<p style=\"font-size:small;margin-top:4px;margin-bottom:0px\">Uploading to: "+settings.userID + "/" + settings.projectID + "/" + settings.measurementID+"</p>" +
				"</html>");
		JScrollPane descriptionScrollPane = new JScrollPane(descriptionPane);
		StandardFormats.addGridBagElement(descriptionScrollPane, elementsLayout, newLineCnstr, elementsPanel);
		
		StandardFormats.addGridBagElement(new JLabel("Progress:"), elementsLayout, newLineCnstr, elementsPanel);
		waitBar.setValue(0);
		waitBar.setString("Initializing");
		waitBar.setStringPainted(true);
		StandardFormats.addGridBagElement(waitBar, elementsLayout, newLineCnstr, elementsPanel);
		StandardFormats.addGridBagElement(new JLabel("Messages:"), elementsLayout, newLineCnstr, elementsPanel);
		messageArea.setEditable(false);
		messageArea.setBackground(Color.WHITE);
		
		stopButton.addActionListener(new ActionListener()
		 {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(repeatMessageQuerying == 0)
					TransferStateFrame.this.frame.setVisible(false);
				else
				{
					if(addon == null)
						return;
					try
					{
						addon.cancelTransfer();
					}
					catch(RemoteException e1)
					{
						TransferStateFrame.this.client.sendError("Could not interrupt OpenBIS data transfer.", e1);
					}
				}
			}
		 });
		
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(elementsPanel, BorderLayout.NORTH);
		JScrollPane messageAreaScroller = new JScrollPane(messageArea);
		messageAreaScroller.setPreferredSize(new Dimension(200, 200));
		contentPane.add(messageAreaScroller, BorderLayout.CENTER);
		contentPane.add(stopButton, BorderLayout.SOUTH);
		frame.setContentPane(contentPane);
		frame.pack();
		
		(new Thread(this)).start();
	}
	
	private class TransferListener extends UnicastRemoteObject implements OpenBISListener
	{

		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= 7599045370082950312L;

		/**
		 * Constructor.
		 * @throws RemoteException
		 */
		protected TransferListener() throws RemoteException
		{
			super();
		}

		@Override
		public void transferStarted() throws RemoteException
		{
			updataProgress("Connection to OpenBIS database established. Starting data transfer...", 0.1F);
		}

		@Override
		public void transferFinished() throws RemoteException
		{
			updataProgress("Transfer of measurement data to database finished successfully.", 1F);
			frame.setClosable(true);
			// Query 1 more second for new messages
			repeatMessageQuerying = 10;
		}

		@Override
		public void transferFailed(Exception e) throws RemoteException
		{
			updataProgress("Error occured during transfer of measurement data to OpenBIS.", 1.0F);
			frame.setClosable(true);
			// Query 1 more second for new messages
			repeatMessageQuerying = 10;
		}

		@Override
		public void transferProgress(float progress, String message) throws RemoteException
		{
			updataProgress("Transferring data to OpenBIS...", progress >= 0 ? progress * 0.9F + 0.1F : -1);
			if(message != null)
			{
				synchronized(TransferStateFrame.this)
				{
					newMessages += message;
				}
			}
		}
		
	}
	
	private void updataProgress(String description, float progress)
	{
		class Runner implements Runnable
		{
			private final String description;
			private final float progress;
			Runner(String description, float progress)
			{
				this.description = description;
				this.progress = progress;
			}
			@Override
			public void run()
			{
				if(progress >= 0)
				{
					if(waitBar.isIndeterminate())
						waitBar.setIndeterminate(false);
					waitBar.setValue((int)(100*progress));
					
					frame.setTitle(Integer.toString((int)(progress * 100)) + "% - Uploading " + settings.userID + "/" + settings.projectID + "/" + settings.measurementID);
				}
				else
				{
					if(!waitBar.isIndeterminate())
						waitBar.setIndeterminate(true);
					
					frame.setTitle("Uploading " + settings.userID + "/" + settings.projectID + "/" + settings.measurementID);
				}
				waitBar.setString(description);
			}
		}
		if(SwingUtilities.isEventDispatchThread())
			(new Runner(description, progress)).run();
		else
			SwingUtilities.invokeLater(new Runner(description, progress));
	}
	@Override
	public void run()
	{
		// Create a new OpenBIS addon for the transfer.
		try
		{
			addon = server.getProperties().getServerAddon(OpenBISAddon.class);
			if(addon == null)
			{
				throw new Exception("YouScope server does not know the OpenBIS addon. Check if the OpenBIS addon is installed on both, the YouScope client and the YouScope server computer.");
			}
			addon.addTransferListener(new TransferListener());
		}
		catch(Exception e)
		{
			client.sendError("Could not get OpenBIS addon from YouScope server.", e);
			updataProgress("Could not get OpenBIS addon from YouScope server, see error log.", 1.0F);
			frame.setClosable(true);
			return;
		}
		
		updataProgress("OpenBIS addon loaded. Initializing connection to OpenBIS database.", 0.01F);
		try
		{
			addon.transferMeasurement(settings.sshUser, settings.sshServer, settings.sshDirectory, settings.userID, settings.projectID, settings.measurementID, settings.measurementFolder, false);
		}
		catch(Exception e)
		{
			client.sendError("Could not initialize transfer of measurement to OpenBIS.", e);
			updataProgress("Could not initialize transfer of measurement to OpenBIS, see error log.", 1.0F);
			frame.setClosable(true);
			return;
		}
		messageUpdater.start();
	}
	
	private class MessageUpdater implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			if(repeatMessageQuerying > 0)
				repeatMessageQuerying--;
			else if(repeatMessageQuerying == 0)
			{
				messageUpdater.stop();
				stopButton.setText("Close");
			}
			
			String tempMessage;
			synchronized(TransferStateFrame.this)
			{
				tempMessage = newMessages;
				newMessages = "";
			}
			if(tempMessage.length() <= 0)
				return;
			messageArea.append(tempMessage);
		}
	}
}
