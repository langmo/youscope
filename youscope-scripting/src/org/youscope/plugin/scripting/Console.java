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
package org.youscope.plugin.scripting;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.script.ScriptException;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

/**
 * @author langmo
 *
 */
class Console extends JPanel implements ScriptMessageListener
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -1155646515451975330L;
	
	// UI elements
	private JTextField inputAreaField = new JTextField();
	private JTextArea inputAreaArea = new JTextArea();
	private JButton inputAreaFieldButton = null;
    private JButton inputAreaAreaButton = null;
	private JTextArea outputArea = new JTextArea();
	
	/**
	 * Font used in the console.
	 */
	private static final Font CONSOLE_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);
	
	// History of inputs
	private int currentSurfLocation = -1;
	private Vector<String> evalHistory = new Vector<String>();
	
	/**
	 * Listeners which should be notified when a script should be executed.
	 */
	private Vector<EvaluationListener> evaluationListeners = new Vector<EvaluationListener>();
	Console()
	{		
		// Initialize output area
		outputArea.setEditable(false);
		outputArea.setFont(CONSOLE_FONT);
		
		// Initialize single line input
		inputAreaField.setFont(CONSOLE_FONT);
		
		// Initialize multiple lines input
		inputAreaArea.setFont(CONSOLE_FONT);	
		
		// Single Line Input
        inputAreaField.addActionListener(new SingleInputActionListener());
		inputAreaField.addKeyListener(new PreviousInputListener());
        JPanel inputAreaFieldPanel = new JPanel(new BorderLayout());
        inputAreaFieldButton = new JButton("Send");
        inputAreaFieldButton.addActionListener(new SingleInputActionListener());
        inputAreaFieldPanel.add(inputAreaFieldButton, BorderLayout.EAST);
        inputAreaFieldPanel.add(inputAreaField, BorderLayout.CENTER);
        
        // Multiple Line Input
        JPanel inputAreaAreaPanel = new JPanel(new BorderLayout());
        inputAreaAreaButton = new JButton("Send");
        inputAreaAreaButton.addActionListener(new MultipleInputActionListener());
        inputAreaAreaPanel.add(inputAreaAreaButton, BorderLayout.EAST);
        inputAreaAreaPanel.add(inputAreaArea, BorderLayout.CENTER);
        
        // TabbedPane to select input type
        JTabbedPane inputArea = new JTabbedPane(JTabbedPane.BOTTOM);
        inputArea.addTab("Single Line", inputAreaFieldPanel);
        inputArea.addTab("Multiple Lines", new JScrollPane(inputAreaAreaPanel));
        inputArea.setPreferredSize(new Dimension(200, inputAreaField.getPreferredSize().height));
        
        // Command window
        JScrollPane outputAreaScroll = new JScrollPane(outputArea);
        outputAreaScroll.setPreferredSize(new Dimension(400, 300));
        JSplitPane ioSplitMane = new JSplitPane(JSplitPane.VERTICAL_SPLIT , false, outputAreaScroll, inputArea);
        ioSplitMane.setResizeWeight(1.0);
        setLayout(new BorderLayout());
        add(ioSplitMane, BorderLayout.CENTER);
        setBorder(new TitledBorder("Command Window"));
	}
	
	public void addEvaluationListener(EvaluationListener listener)
	{
		synchronized(evaluationListeners)
		{
			evaluationListeners.add(listener);
		}
	}
	public void removeEvaluationListener(EvaluationListener listener)
	{
		synchronized(evaluationListeners)
		{
			evaluationListeners.remove(listener);
		}
	}
	private void evalString(String script)
	{
		synchronized(evaluationListeners)
		{
			try
			{
				for(EvaluationListener listener : evaluationListeners)
				{
					listener.evalString(script);
				}
			}
			catch(@SuppressWarnings("unused") ScriptException e)
			{
				// Do nothing.
			}
		}
	}
	
	@Override
	public void outputMessage(String message)
	{
		if(message == null || message.length() <= 0)
			return;
		
		outputArea.append(message + "\n");
		scrollDown();
	}

	@Override
	public void inputMessage(String message)
	{
		if(message == null || message.length() <= 0)
			return;
		
		outputArea.append("> " + message + "\n");
		scrollDown();
	}
	
	public void clearConsole()
	{
		outputArea.setText("");
	}
	
	@Override
	public void setEnabled(boolean enabled)
	{
		inputAreaField.setEditable(enabled);
		inputAreaArea.setEditable(enabled);
		inputAreaFieldButton.setEnabled(enabled);
		inputAreaAreaButton.setEnabled(enabled);
	}
	
	private class PreviousInputListener extends KeyAdapter
	{
		@Override
		public void keyPressed(KeyEvent e)
		{
			if(e.getKeyCode() == KeyEvent.VK_DOWN)
			{
				surfHistory(false);
			}
			else if(e.getKeyCode() == KeyEvent.VK_UP)
			{
				surfHistory(true);
			}
		}
	}
	private void surfHistory(boolean goBack)
	{
		if(currentSurfLocation >= evalHistory.size() || currentSurfLocation < -1)
			currentSurfLocation = -1;
		if(currentSurfLocation >= 0)
		{
			// Check if user has changed text. If, than restart at start.
			if(inputAreaField.getText().compareTo(evalHistory.get(currentSurfLocation)) != 0)
				currentSurfLocation = -1;
		}
		
		if(goBack)
			currentSurfLocation++;
		else
			currentSurfLocation--;
		if(currentSurfLocation >= evalHistory.size())
			currentSurfLocation = evalHistory.size() - 1;
		else if(currentSurfLocation < 0)
			inputAreaField.setText("");
		else
			inputAreaField.setText(evalHistory.get(currentSurfLocation));
	}
	private void addToHistory(String string)
	{
		evalHistory.insertElementAt(string, 0);
		currentSurfLocation = -1;
		if(evalHistory.size() > 50)
		{
			evalHistory.removeElementAt(50);
		}
	}
	private class SingleInputActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			String input = inputAreaField.getText();
			if(input.length() <= 0)
				return;
			inputAreaField.setText("");
			evalString(input);
			addToHistory(input);
		}
	}
	private class MultipleInputActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			String input = inputAreaField.getText();
			if(input.length() <= 0)
				return;
			inputAreaField.setText("");
			evalString(input);
		}
	}
	
	private void scrollDown()
	{
		int height = outputArea.getHeight();
		outputArea.scrollRectToVisible(new Rectangle(0, height-11, 10, height-1));
	}
}
