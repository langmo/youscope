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

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.youscope.common.MessageListener;
import org.youscope.uielements.QuickLogger;
import org.youscope.uielements.StandardFormats;

/**
 * Panel to show microscope log.
 * @author Moritz Lang
 *
 */
class LogPanel extends JPanel
{

	/**
	 * SerializableVersion UID.
	 */
	private static final long	serialVersionUID	= 5881608545485744545L;
	
	private final JScrollPane logTextScrollPane;
	private final QuickLogger quickLogger;
	
	public LogPanel()
	{
		super(new BorderLayout());
		
		setOpaque(false);
        GridBagConstraints newLineConstr = StandardFormats.getNewLineConstraint();
		GridBagConstraints bottomConstr = StandardFormats.getBottomContstraint();
		
		// Put the editor pane in a scroll pane.
		quickLogger = new QuickLogger();
        logTextScrollPane = new JScrollPane(quickLogger);
        logTextScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        logTextScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        logTextScrollPane.setPreferredSize(new Dimension(250, 145));
        logTextScrollPane.setMinimumSize(new Dimension(100, 0));

        add(logTextScrollPane, BorderLayout.CENTER);
        
        GridBagLayout buttonsLayout = new GridBagLayout();
        JPanel buttonsPanel = new JPanel(buttonsLayout);
        buttonsPanel.setOpaque(false);
        if(Desktop.isDesktopSupported())
        {   
	        JButton showExternal = new JButton("Show External");
	        showExternal.setOpaque(false);
	        showExternal.addActionListener(new ActionListener()
	        	{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						Desktop desktop = Desktop.getDesktop();
				    	try
						{
							desktop.edit(ClientSystem.getLogFile());
						}
						catch(IOException e1)
						{
							ClientSystem.err.println("Could not open log file.", e1);
						}
					}
	        	});
	        StandardFormats.addGridBagElement(showExternal, buttonsLayout, newLineConstr, buttonsPanel);
	    }
        JButton clearButton = new JButton("Clear");
        clearButton.setOpaque(false);
        clearButton.addActionListener(new ActionListener()
        	{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					quickLogger.clearMessages();
				}
        	});
        StandardFormats.addGridBagElement(clearButton, buttonsLayout, newLineConstr, buttonsPanel);
        JPanel emptyElement = new JPanel();
        emptyElement.setOpaque(false);
        StandardFormats.addGridBagElement(emptyElement, buttonsLayout, bottomConstr, buttonsPanel);
        
        add(buttonsPanel, BorderLayout.EAST);
	}
	
	MessageListener getMessageListener()
    {
        try
		{
			return new LogListener();
		}
		catch(RemoteException e)
		{
			ClientSystem.err.println("Could not create message listener for log panel.", e);
			return null;
		}
    }
	private class LogListener extends UnicastRemoteObject implements MessageListener
	{
		/**
		 * SerializableVersionVersion UID.
		 */
		private static final long serialVersionUID = 2670267144687981493L;
		
		LogListener() throws RemoteException
		{
		    super();
		}
		
		@Override
		public void sendMessage(String message) throws RemoteException
		{
		    quickLogger.addMessage(message);
		}
		
		@Override
		public void sendErrorMessage(String message, Throwable exception) throws RemoteException
		{
			quickLogger.addMessage(		message
		    				+ "\n"
		                    + (exception != null ? exception.getMessage()
		                            : "No further information"));
		}
	}
}
