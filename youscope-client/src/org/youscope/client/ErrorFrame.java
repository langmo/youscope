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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.MessageListener;

import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * Window to show error messages to the user.
 * @author Moritz Lang
 */
class ErrorFrame
{
	private final LinkedList<ErrorContent>	content				= new LinkedList<ErrorContent>();

	private int currentErrorID = -1;
	
	private final JEditorPane					contentArea			= new JEditorPane("text/html", "");

	private final YouScopeFrame					frame;

	private MicroscopeListener			microscopeListener;

	private final JButton nextButton = new JButton("Next");
	private final JButton previousButton = new JButton("Previous");
	
	private final QRImageField qrImageField = new QRImageField(null);
	
	ErrorFrame(YouScopeFrame frame)
	{
		this.frame = frame;
		frame.setTitle("Error Summary");
		frame.setResizable(true);
		frame.setClosable(true);
		frame.setMaximizable(true);
		frame.setMargins(0, 0, 0, 0);
		try
		{
			microscopeListener = new MicroscopeListener();
		}
		catch(Exception e)
		{
			microscopeListener = null;
			frame.setToErrorState("Could not add error listener to client.", e);
			return;
		}

		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				ErrorFrame.this.frame.setVisible(false);
				clearErrors();
				updateButtons();
			}
		});

		previousButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				currentErrorID = currentErrorID > 0 ? currentErrorID - 1 : 0;
				loadError();
				updateButtons();
			}
		});
		previousButton.setEnabled(false);
		
		nextButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				currentErrorID = currentErrorID >= content.size() ? content.size()-1 : currentErrorID + 1;
				loadError();
				updateButtons();
			}
		});
		nextButton.setEnabled(false);

		JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonsPanel.add(previousButton);
		buttonsPanel.add(nextButton);
		buttonsPanel.add(closeButton);
		
		contentArea.setEditable(false);
		contentArea.setPreferredSize(new Dimension(400, 300));

		JPanel errorField = new JPanel(new BorderLayout());
		errorField.setOpaque(false);
		errorField.add(qrImageField, BorderLayout.CENTER);
		errorField.add(new JLabel("<html><p>Scan to sent error report!</p>", SwingConstants.CENTER), BorderLayout.SOUTH);
		
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(new JLabel("<html><h1>Error occured</h1>", SwingConstants.CENTER), BorderLayout.NORTH);
		contentPane.add(buttonsPanel, BorderLayout.SOUTH);
		contentPane.add(errorField, BorderLayout.WEST);
		contentPane.add(new JScrollPane(contentArea), BorderLayout.CENTER);
		frame.setContentPane(contentPane);
		frame.setSize(new Dimension(500, 400));
	}
	
	private class QRImageField extends JComponent
    {
        /**
         * Serial Version UID.
         */
        private static final long serialVersionUID = 3857578873912009511L;

        private BufferedImage image = null;
        
        private static final int WIDTH = 300;
        private static final int HEIGHT = 300;
        
        QRImageField(BufferedImage image)
        {
        	this.image = image;
        }
        private void setError(ErrorContent content)
    	{
    		if(content == null)
    		{
    			this.image = null;
    			repaint();
    			return;
    		}
    		
    		String NL = "\n";
    		String errorMessage = "MATMSG:TO:info@youscope.org;SUB:Error Report;BODY:" + NL
	        	+ "Time> " + content.date.toString() +NL
	        	+"Message> " + content.message+NL;
	        if (content.exception != null)
	        {
	        	errorMessage += "Cause> ";
	        	for (Throwable throwable = content.exception; throwable != null; throwable = throwable.getCause())
	            {
	                if (throwable.getMessage() != null)
	                	errorMessage +=
	                            throwable.getClass().getName() +": " + throwable.getMessage().replace("\n", NL) + NL;
	            }
	        	/*errorMessage+="Stack: " +NL;
	        	for (Throwable throwable = content.exception; throwable != null; throwable = throwable.getCause())
	            {
	        		errorMessage+= throwable.getClass().getName()+": " +NL;
	        		StackTraceElement[] stacks = throwable.getStackTrace();
	                for(StackTraceElement stack : stacks)
	                {
	                	errorMessage += stack.toString() + NL;
	                }
	            }*/
	        }
	        errorMessage +=";;";
    		
    		
    		BitMatrix matrix;
    		try
    		{
    			matrix = new QRCodeWriter().encode(errorMessage, com.google.zxing.BarcodeFormat.QR_CODE, WIDTH, HEIGHT);
    		}
    		catch(@SuppressWarnings("unused") WriterException e)
    		{
    			this.image = null;
    			repaint();
    			return;
    		}
    		
    		this.image = MatrixToImageWriter.toBufferedImage(matrix);
    		repaint();
    		return;
    	}
        
        @Override
        public Dimension getPreferredSize()
        {
        	return new Dimension(WIDTH, HEIGHT);
        }
        
        @Override
        public synchronized void paintComponent(Graphics grp)
        {
            Graphics2D g2D = (Graphics2D) grp;

            if (image == null)
            {
                return;
            }

            double imageWidth = image.getWidth(this);
            double imageHeight = image.getHeight(this);
            if (WIDTH / imageWidth > HEIGHT / imageHeight)
            {
                imageWidth = imageWidth * HEIGHT / imageHeight;
                imageHeight = HEIGHT;
            } 
            else
            {
                imageHeight = imageHeight * WIDTH / imageWidth;
                imageWidth = WIDTH;
            }

            // draw the image
            g2D.drawImage(image, (int) (getWidth() - imageWidth) / 2,
                    (int) (getHeight() - imageHeight) / 2, (int) imageWidth, (int) imageHeight,
                    this);
        }
    }

	void addError(String message, Throwable e, Date time)
	{
		if(time == null)
			time = new Date();
		synchronized(content)
		{
			content.add(new ErrorContent(message, e, time));
			if(content.size() == 1)
			{
				currentErrorID = 0;
				loadError();
				updateButtons();
			}
			else if(currentErrorID >= content.size() - 2)
				updateButtons();
		}
		// Show frame if frame is hidden.
		if(!frame.isVisible())
		{
			frame.pack();
			frame.setVisible(true);
		}
	}

	void clearErrors()
	{
		synchronized(content)
		{
			content.clear();
			currentErrorID = -1;
			loadError();
		}
	}

	MessageListener getMicroscopeMessageListener()
	{
		return microscopeListener;
	}

	private class MicroscopeListener extends UnicastRemoteObject implements MessageListener
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= 2670267144687981490L;

		MicroscopeListener() throws RemoteException
		{
			super();
		}

		@Override
		public void sendMessage(String message) throws RemoteException
		{
			addError(message, null, new Date());
		}

		@Override
		public void sendErrorMessage(String message, Throwable exception) throws RemoteException
		{
			addError(message, exception, new Date());
		}
	}
	
	private void updateButtons()
	{
		final boolean isFirst = currentErrorID <= 0;
		final boolean isLast = currentErrorID >= content.size() - 1;
		Runnable runnable = new Runnable()
		{
			@Override
			public void run()
			{
				if(nextButton.isEnabled() == isLast)
					nextButton.setEnabled(!isLast);
				if(previousButton.isEnabled() == isFirst)
					previousButton.setEnabled(!isFirst);
			}
		};
		
		if(SwingUtilities.isEventDispatchThread())
			runnable.run();
		else
			SwingUtilities.invokeLater(runnable);
	}

	private void loadError()
	{
		ErrorContent error;
		synchronized(content)
		{
			if(currentErrorID >= content.size())
			{
				currentErrorID = content.size()-1;
			}
			if(currentErrorID < 0)
				error = null;
			else
				error = content.get(currentErrorID);
		}
		
		String text = "<html>";
		if(error == null)
		{
			text += "<p>No more errors.</p>";
		}
		else
		{
			text += "<p style=\"margin-bottom:0pt;margin-top:0pt;color:#666666;font-family:monospace\">" + error.date.toString() + ": </p>";
			if(error.message != null && error.message.length() > 0)
			{
				text += "<p style=\"margin-bottom:0pt;margin-top:0pt;font-family:monospace\">" + error.message.replace("\n", "<br />") + "</p>";
			}
			if(error.exception != null)
			{
				text += "<p style=\"margin-top:0pt;color:#666666;font-family:monospace\">Details:</p>";
				Throwable throwable = error.exception;
				for(; throwable != null; throwable = throwable.getCause())
				{
					if(throwable.getMessage() != null)
					{
						text += "<p style=\"margin-left:15pt;margin-top:0pt;color:#666666;font-family:monospace\">" + throwable.getClass().getSimpleName() + ": " + throwable.getMessage().replace("\n", "<br />") + "</p>";
					}
					else
					{
						text += "<p style=\"margin-left:15pt;margin-top:0pt;color:#666666;font-family:monospace\">" + throwable.getClass().getSimpleName() + ": No error descirption.</p>";
					}
				}
			}
		}
		text += "</html>";
		setErrorText(text);
		qrImageField.setError(error);
	}
	private void setErrorText(final String text)
	{
		Runnable runnable = new Runnable()
		{
			@Override
			public void run()
			{
				contentArea.setText(text);
			}
		};
		if(SwingUtilities.isEventDispatchThread())
			runnable.run();
		else
			SwingUtilities.invokeLater(runnable);
	}

	private class ErrorContent
	{
		String		message		= null;

		Throwable	exception	= null;

		Date		date		= null;

		public ErrorContent(String message, Throwable exception, Date date)
		{
			this.message = message;
			this.exception = exception;
			this.date = date;
		}
	}
}
