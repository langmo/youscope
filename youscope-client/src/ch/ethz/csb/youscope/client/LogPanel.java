/**
 * 
 */
package ch.ethz.csb.youscope.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import ch.ethz.csb.youscope.client.uielements.StandardFormats;
import ch.ethz.csb.youscope.shared.YouScopeMessageListener;

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
	
	private static final int NUM_LINES = 100;
	private static final int NUM_CHARACTERS = 200;
	
	private final String[] dateLines = new String[NUM_LINES];
	private final String[] messageLines = new String[NUM_LINES];
	
	private final JScrollPane logTextScrollPane;
	private final LogTextComponent logTextComponent;
	
	private volatile boolean shouldActualize = false;
	
	private volatile int firstLine = 0;
	private volatile int lastLine = -1;
	private volatile boolean fullLines = false;
	
	private final Calendar calendar = GregorianCalendar.getInstance();
	
	private final static int CONSOLE_REFRESH_PERIOD = 500; 
	
	public LogPanel()
	{
		super(new BorderLayout());
		
		setOpaque(false);
        GridBagConstraints newLineConstr = StandardFormats.getNewLineConstraint();
		GridBagConstraints bottomConstr = StandardFormats.getBottomContstraint();
		
		// Put the editor pane in a scroll pane.
		logTextComponent = new LogTextComponent();
        logTextScrollPane = new JScrollPane(logTextComponent);
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
					clearMessages();
				}
        	});
        StandardFormats.addGridBagElement(clearButton, buttonsLayout, newLineConstr, buttonsPanel);
        JPanel emptyElement = new JPanel();
        emptyElement.setOpaque(false);
        StandardFormats.addGridBagElement(emptyElement, buttonsLayout, bottomConstr, buttonsPanel);
        
        add(buttonsPanel, BorderLayout.EAST);
		
		
		Timer timer = new Timer("Console Refresh", true);
        timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    synchronized (LogPanel.this)
                    {
                        if (!shouldActualize)
                            return;
                        shouldActualize = false;
                    }
                    logTextScrollPane.getViewport().repaint();
                }
            }, 1000, CONSOLE_REFRESH_PERIOD);
	}
	private class LogTextComponent extends JComponent
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -7005553395944159115L;
		private static final int LEFT_BORDER = 5;
		private static final int TOP_BORDER = 5;
		private static final int BOTTOM_BORDER = 5;
		private static final int RIGHT_BORDER = 5;
		private final int lineHeight;
		private int maxAdvance;
		private final int dateLength;
		
		private final Color dateColor = new Color(0.4F, 0.4F, 0.4F);
		private final Color messageColor = Color.BLACK;
		private final Color backgroundColor = Color.WHITE;
		private final Font dateFont = new Font("Monospaced", Font.BOLD, 14);
		private final Font messageFont = new Font("Dialog", Font.PLAIN, 14);
		LogTextComponent()
		{
			setOpaque(true);
						
			// Get dimensions of fonts
			lineHeight = Math.max(getFontMetrics(dateFont).getHeight(), getFontMetrics(messageFont).getHeight());
			//maxAdvance = getFontMetrics(dateFont).getMaxAdvance();
			//maxAdvance = maxAdvance <= 0 ? getFontMetrics(dateFont).charWidth('W'):maxAdvance;
			maxAdvance = getFontMetrics(dateFont).charWidth('8');
			dateLength = 11 * maxAdvance;
		}
		
		@Override
		protected void paintComponent(Graphics g1D)
		{
			Graphics2D g = (Graphics2D)g1D;
			Rectangle clip = g.getClipBounds();
			
			// Fill area
			g.setColor(backgroundColor);
			g.fillRect(clip.x, clip.y, clip.width, clip.height);
			
			// Check if any text is there at all
			if(lastLine == -1)
				return;
			
			// Identify lines to be painted
			int minLine = (clip.y - TOP_BORDER)	/ lineHeight;
			minLine = minLine < 0 ? 0 : (minLine >= NUM_LINES ? NUM_LINES - 1 : minLine);
			int maxLine = (int)Math.ceil(((double)(clip.y + clip.height - TOP_BORDER)) / lineHeight);
			maxLine = maxLine < 0 ? 0 : (maxLine >= NUM_LINES ? NUM_LINES - 1 : maxLine);
			
			synchronized(LogPanel.this)
			{
				
				
				// Repaint all respective lines
				for(int i=minLine; i<=maxLine; i++)
				{
					int idx = (NUM_LINES + lastLine - i) % NUM_LINES;
					// Check if message is available
					if(messageLines[idx] == null || (!fullLines && idx > lastLine))
						break;
					
					g.setColor(messageColor);
					g.setFont(messageFont);
					g.drawString(messageLines[idx], LEFT_BORDER + dateLength, (i+1) * lineHeight + TOP_BORDER);
					if(dateLines[idx] != null)
					{
						g.setFont(dateFont);
						g.setColor(dateColor);
						g.drawString(dateLines[idx], LEFT_BORDER, (i+1) * lineHeight + TOP_BORDER);
					}
				}
			}
		}
	
		@Override
		public Dimension getPreferredSize()
		{
			return new Dimension(NUM_CHARACTERS * maxAdvance + LEFT_BORDER + RIGHT_BORDER, NUM_LINES * lineHeight + TOP_BORDER + BOTTOM_BORDER);
		}
	}
	
	YouScopeMessageListener getMessageListener()
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
	private class LogListener extends UnicastRemoteObject implements YouScopeMessageListener
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
		public void consumeMessage(String message, Date time) throws RemoteException
		{
		    addMessage(message, time);
		}
		
		@Override
		public void consumeError(String message, Throwable exception, Date time) throws RemoteException
		{
		    addMessage(		message
		    				+ "\n"
		                    + (exception != null ? exception.getMessage()
		                            : "No further information"), time);
		}
	}
	
	synchronized void clearMessages()
    {
    	firstLine = 0;
    	lastLine = -1;
    	fullLines = false;
    	
    	shouldActualize = true;
    }
	
	void addMessage(String message, Date time)
    {
    	if(time == null)
    		time = new Date();
    	
    	// Get time
    	int hour;
    	int minute;
    	int second;
    	synchronized(calendar)
    	{
	    	calendar.setTime(time);
	    	hour = calendar.get(Calendar.HOUR_OF_DAY);
	    	minute = calendar.get(Calendar.MINUTE);
	    	second = calendar.get(Calendar.SECOND);
    	}
    	    	
    	// prepare strings
    	String[] lines = message.split("\n");
    	String date = (hour<10 ? "0" : "") + Integer.toString(hour)+":"
    		+ (minute<10 ? "0" : "") + Integer.toString(minute)+":"
    		+ (second<10 ? "0" : "") + Integer.toString(second) +" >";
    	
    	// Update elements
    	synchronized(this)
    	{
    		for(int i=lines.length-1; i>=0; i--)
    		{
    			// Update line index in buffer
    			if(lastLine < 0)
    				lastLine++;
    			else
    			{
    				lastLine++;
    				if(lastLine >= NUM_LINES)
    				{
    					lastLine = 0;
    					fullLines = true;
    				}
    				if(lastLine == firstLine)
    				{
    					firstLine++;
    					if(firstLine >= NUM_LINES)
    						firstLine = 0;
    				}
    			}
    			
    			messageLines[lastLine] = lines[i];
    			if(i==0)
    				dateLines[lastLine] = date;
    			else
    				dateLines[lastLine] = null;
    		}
    	}

        shouldActualize = true;
    }
}
