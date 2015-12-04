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
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.youscope.common.YouScopeMessageListener;
import org.youscope.uielements.StandardFormats;

class ConsolePanel extends JPanel
{

    /**
     * @author Moritz Lang
     */
    public enum MessageType
    {
        SERVER_MESSAGE, CLIENT_MESSAGE, SERVER_ERROR, CLIENT_ERROR
    }

    /**
	 * 
	 */
    private static final long serialVersionUID = -5849165088555854369L;

    protected static final int numRingBufferElements = 100;

    protected String[] messages = new String[numRingBufferElements];

    protected MessageType[] messageTypes = new MessageType[numRingBufferElements];

    protected Date[] messageDates = new Date[numRingBufferElements];

    protected int firstMessage = 0;

    protected int lastMessage = 0;

    protected JEditorPane console;

    protected JScrollPane consoleScrollPane;

    protected StringBuffer messageBuffer = new StringBuffer(numRingBufferElements * 20);

    protected MicroscopeListener microscopeListener;

    private volatile boolean shouldActualize = true;

    ConsolePanel()
    {
        super(new BorderLayout());
        setOpaque(false);
        GridBagConstraints newLineConstr = StandardFormats.getNewLineConstraint();
		GridBagConstraints bottomConstr = StandardFormats.getBottomContstraint();
		
        console = new JEditorPane("text/html", "");
        console.setEditable(false);
        // First message
        messages[0] = "YouScope started";
        messageTypes[0] = MessageType.CLIENT_MESSAGE;
        messageDates[0] = new Date();
        // Put the editor pane in a scroll pane.
        consoleScrollPane = new JScrollPane(console);
        consoleScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        consoleScrollPane.setPreferredSize(new Dimension(250, 145));
        consoleScrollPane.setMinimumSize(new Dimension(10, 10));

        add(consoleScrollPane, BorderLayout.CENTER);
        
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
					clearMessages();
				}
        	});
        StandardFormats.addGridBagElement(clearButton, buttonsLayout, newLineConstr, buttonsPanel);
        JPanel emptyElement = new JPanel();
        emptyElement.setOpaque(false);
        StandardFormats.addGridBagElement(emptyElement, buttonsLayout, bottomConstr, buttonsPanel);
        
        add(buttonsPanel, BorderLayout.EAST);
        
        try
        {
            microscopeListener = new MicroscopeListener();
        } 
        catch (RemoteException e)
        {
            // Error will not occur, because class is local...
        	ClientSystem.err.println("Could not create microscope listener.", e);
        }

        Timer timer = new Timer("Console Refresh", true);
        timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    synchronized (ConsolePanel.this)
                    {
                        if (!shouldActualize)
                            return;
                        shouldActualize = false;
                    }
                    messagesChanged();
                }
            }, 1000, 1000);
    }

    synchronized void addMessage(String message, MessageType type, Date time)
    {
    	if(time == null)
    		time = new Date();
        lastMessage++;
        if (lastMessage >= numRingBufferElements)
            lastMessage = 0;
        if (lastMessage == firstMessage)
        {
            firstMessage++;
            if (firstMessage >= numRingBufferElements)
                firstMessage = 0;
        }

        messages[lastMessage] = message;
        messageTypes[lastMessage] = type;
        messageDates[lastMessage] = time;

        shouldActualize = true;
    }
    
    synchronized void clearMessages()
    {
    	firstMessage = 0;
    	lastMessage = 0;
    	
    	messages[0] = "Microscope log cleared.";
        messageTypes[0] = MessageType.CLIENT_MESSAGE;
        messageDates[0] = new Date();
        
        shouldActualize = true;
    }

    protected void messagesChanged()
    {
        messageBuffer.setLength(0);
        synchronized (this)
        {
            if (lastMessage >= firstMessage)
            {
                for (int i = lastMessage; i >= firstMessage; i--)
                {
                    messageBuffer.append(toMessage(messageDates[i], messageTypes[i], messages[i]));
                }
            } else
            {
                for (int i = lastMessage; i >= 0; i--)
                {
                    messageBuffer.append(toMessage(messageDates[i], messageTypes[i], messages[i]));
                }
                for (int i = numRingBufferElements - 1; i >= firstMessage; i--)
                {
                    messageBuffer.append(toMessage(messageDates[i], messageTypes[i], messages[i]));
                }
            }
        }
        console.setText("<html><body><p>" + messageBuffer.toString() + "</p></body></html>");
    }

    protected static String toMessage(Date messageDate, MessageType messageType, String message)
    {
        String output = "<span style=\"color:#666666\">" + messageDate.toString() + ": </span>";
        if (messageType == MessageType.SERVER_ERROR || messageType == MessageType.CLIENT_ERROR)
        {
            output += "<span style=\"color:#AA0000\">";
        } else
        {
            output += "<span style=\"color:#222222\">";
        }
        output += message + "</span><br />";
        return output;
    }

    YouScopeMessageListener getMicroscopeMessageListener()
    {
        return microscopeListener;
    }

    protected class MicroscopeListener extends UnicastRemoteObject implements
            YouScopeMessageListener
    {
        /**
		 * 
		 */
        private static final long serialVersionUID = 2670267144687981490L;

        MicroscopeListener() throws RemoteException
        {
            super();
        }

        @Override
        public void consumeMessage(String message, Date time) throws RemoteException
        {
            addMessage(message, ConsolePanel.MessageType.SERVER_MESSAGE, time);
        }

        @Override
        public void consumeError(String message, Throwable exception, Date time) throws RemoteException
        {
            addMessage(
                    message
                            + " ("
                            + (exception != null ? exception.getMessage()
                                    : "no further information") + ")",
                    ConsolePanel.MessageType.SERVER_ERROR, time);
        }
    }
}
