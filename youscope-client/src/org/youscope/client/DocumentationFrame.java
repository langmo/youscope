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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.youscope.clientinterfaces.YouScopeFrame;

/**
 * @author langmo
 * 
 */
class DocumentationFrame
{
	private String	baseUrl			= "";
	private final static String NAVIGATION_PAGE = "navigation.html";

	private String				startPage;

	private JEditorPane			htmlPage			= new JEditorPane();

	private JEditorPane			navigationPage		= new JEditorPane();

	private Vector<URL>			forwardURLs			= new Vector<URL>();
	private Vector<URL>			backwardURLs		= new Vector<URL>();

	private JButton				forwardButton		= null;
	private JButton				backwardButton		= null;

	private YouScopeFrame									frame;
	
	DocumentationFrame(YouScopeFrame frame)
	{
		this(frame, "Documentation.html");
	}

	DocumentationFrame(YouScopeFrame frame, String page)
	{
		this.frame = frame;
		this.startPage = page;
		
		frame.setTitle("Documentation");
		frame.setResizable(true);
		frame.setClosable(true);
		frame.setMaximizable(true);
		
		try
		{
			File file = new File(".").getCanonicalFile();
		
			if(file.getName().compareToIgnoreCase("client") == 0)
				file = file.getParentFile();
			baseUrl = (new File(file, "documentation/")).getCanonicalPath();
		}
		catch(IOException e)
		{
			ClientSystem.err.println("Could not obtain current path.", e);
			baseUrl = "";
		}
		
		frame.startInitializing();
		(new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				initializeFrame();
				DocumentationFrame.this.frame.endLoading();
			}

		})).start();
		frame.setSize(new Dimension(760, 500));
	}

	private void initializeFrame()
	{
		htmlPage.setContentType("text/html");
		htmlPage.setEditable(false);

		navigationPage.setContentType("text/html");
		navigationPage.setEditable(false);

		HyperlinkListener linkListener = new HyperlinkListener()
		{
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e)
			{
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
				{
					forwardURLs.removeAllElements();
					backwardURLs.add(htmlPage.getPage());

					setPage(e.getURL());
					historyUpdated();
				}
			}
		};

		htmlPage.addHyperlinkListener(linkListener);
		navigationPage.addHyperlinkListener(linkListener);

		// Load navigation page
		URL navigationPageURL = null;
		File navigationPageFile = new File(baseUrl + "/" + NAVIGATION_PAGE);
		if(navigationPageFile.exists())
		{
			try
			{
				navigationPageURL = navigationPageFile.toURI().toURL();
			}
			catch(MalformedURLException e1)
			{
				ClientSystem.err.println("Navigation page URL is malformed.", e1);
			}
		}
		if(navigationPageURL != null)
		{
			try
			{
				navigationPage.setPage(navigationPageURL);
			}
			catch(IOException e1)
			{
				ClientSystem.err.println("Navigation page could not be loaded.", e1);
				navigationPageURL = null;
			}
		}
		if(navigationPageURL == null)
		{
			navigationPage.setText("<html><p>Could not load navigation page.<br />Intended location was:<br /><i>" + navigationPageFile.toString() + "</i></p></html>");
		}
		
		
		
		// Load start page
		URL startPageURL = null;
		File startPageFile = new File(baseUrl + "/" + startPage);
		if(startPageFile.exists())
		{
			try
			{
				startPageURL = startPageFile.toURI().toURL();
			}
			catch(MalformedURLException e1)
			{
				ClientSystem.err.println("Start page URL is malformed.", e1);
			}
		}
		setPage(startPageURL);
		
		// Buttons
		String backwardButtonFile = "bonus/icons-24/arrow-180.png";
		String forwardButtonFile = "bonus/icons-24/arrow.png";
		ImageIcon backwardButtonIcon = null;
		ImageIcon forwardButtonIcon = null;
		try
		{
			URL backwardButtonURL = getClass().getClassLoader().getResource(
					backwardButtonFile);
			if (backwardButtonURL != null)
				backwardButtonIcon = new ImageIcon(backwardButtonURL,
						"Backward");
			URL forwardButtonURL = getClass().getClassLoader().getResource(
					forwardButtonFile);
			if (forwardButtonURL != null)
				forwardButtonIcon = new ImageIcon(forwardButtonURL, "Forward");
		}
		catch (@SuppressWarnings("unused") Exception e)
		{
			// Do nothing.
		}
		if (backwardButtonIcon != null)
			backwardButton = new JButton(backwardButtonIcon);
		else
			backwardButton = new JButton("Backward");
		if (forwardButtonIcon != null)
			forwardButton = new JButton(forwardButtonIcon);
		else
			forwardButton = new JButton("Forward");

		backwardButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (backwardURLs.size() <= 0)
					return;
				forwardURLs.addElement(htmlPage.getPage());
				setPage(backwardURLs.remove(backwardURLs.size() - 1));

				historyUpdated();
			}
		});
		forwardButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (forwardURLs.size() <= 0)
					return;
				backwardURLs.addElement(htmlPage.getPage());
				setPage(forwardURLs.remove(forwardURLs.size() - 1));

				historyUpdated();
			}
		});

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
		buttonPanel.add(backwardButton);
		buttonPanel.add(forwardButton);

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(new JScrollPane(htmlPage), BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.NORTH);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				new JScrollPane(navigationPage), mainPanel);
		splitPane.setBorder(null);
		splitPane.setDividerLocation(300);
		
		historyUpdated();
		
		frame.setContentPane(splitPane);
	}

	private void setPage(URL page)
	{
		
		if(page != null)
		{
			String protocol = page.getProtocol();
			if(protocol.compareToIgnoreCase("file")==0)
			{
				String path = page.toString();
				String fileType = path.substring(path.lastIndexOf('.'));
				if (fileType.compareToIgnoreCase(".jpg") == 0
						|| fileType.compareToIgnoreCase(".gif") == 0
						|| fileType.compareToIgnoreCase(".jpeg") == 0
						|| fileType.compareToIgnoreCase(".bmp") == 0
						|| fileType.compareToIgnoreCase(".eps") == 0)
				{
					htmlPage.setText("<html><img src=\"" + path + "\" /></html>");
				}
				else
				{
					try
					{
						htmlPage.setPage(page);
					}
					catch (IOException e1)
					{
						ClientSystem.err.println(
								"Could not navigate to page " + page.toString()
										+ ".", e1);
						page = null;
					}
				}
			}
			else
			{
				// Open external viewer
				if(Desktop.isDesktopSupported())
				{
					Desktop desktop = Desktop.getDesktop();
			    	try
					{
						desktop.browse(page.toURI());
					}
					catch(Exception e)
					{
						ClientSystem.err.println("Could not open external browser.", e);
						page = null;
					}
				}
				else
				{
					page = null;
				}
			}
		}
		if(page == null)
		{
			htmlPage.setText("<html><p>Could not load page.</p></html>");
		}
	}

	private void historyUpdated()
	{
		forwardButton.setEnabled(forwardURLs.size() > 0);
		backwardButton.setEnabled(backwardURLs.size() > 0);
	}
}
