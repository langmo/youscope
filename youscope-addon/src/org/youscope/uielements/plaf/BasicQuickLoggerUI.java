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
package org.youscope.uielements.plaf;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;

import org.youscope.uielements.QuickLogger;
import org.youscope.uielements.QuickLoggerModel;


/**
 * Default/basic UI delegate for the quick logger.
 * @author Moritz Lang
 *
 */
public class BasicQuickLoggerUI extends QuickLoggerUI 
{
	private QuickLogger quickLogger;
    private ActionListener modelChangeHandler;
    
    private static final int LEFT_BORDER = 5;
    private static final int RIGHT_BORDER = 5;
	private static final int TOP_BORDER = 5;
	private static final int BOTTOM_BORDER = 5;
	private static final int NUM_CHARACTERS = 200;
	
	/**
	 * Name of the UI property defining the background color.
	 */
	public static final String PROPERTY_BACKGROUND = QuickLogger.UI_CLASS_ID+".background";
	/**
	 * Name of the UI property defining the color of messages.
	 */
	public static final String PROPERTY_MESSAGE_FOREGROUND = QuickLogger.UI_CLASS_ID+".messageForeground";
	/**
	 * Name of the UI property defining the color of the date in front of messages.
	 */
	public static final String PROPERTY_DATE_FOREGROUND = QuickLogger.UI_CLASS_ID+".dateForeground";
	
	private final Font dateFont = new Font("Monospaced", Font.BOLD, 14);
	private final Font messageFont = new Font("Dialog", Font.PLAIN, 14);
	
    protected ActionListener getChangeHandler() {
        if (modelChangeHandler == null) 
        {
        	modelChangeHandler = new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0) {
					quickLogger.repaint();
				}
		
			};
        }
        return modelChangeHandler;
    }

    protected void installModelChangeListener() 
    {
        quickLogger.getModel().addActionListener(getChangeHandler());
    }

    @Override
	public Dimension getPreferredSize(JComponent c)
	{
    	QuickLogger quickLogger = (QuickLogger)c;
    	int lineHeight = Math.max(c.getFontMetrics(dateFont).getHeight(), c.getFontMetrics(messageFont).getHeight());
		int maxAdvance = c.getFontMetrics(dateFont).charWidth('8');
		int maxNumLines = quickLogger.getModel().getMaxNumLines();
		return new Dimension(NUM_CHARACTERS * maxAdvance + LEFT_BORDER + RIGHT_BORDER, maxNumLines * lineHeight + TOP_BORDER + BOTTOM_BORDER);
	}
    
    @Override
    public void installUI(JComponent c) 
    {
    	quickLogger = (QuickLogger) c;
    	installDefaults();
        installModelChangeListener();

    }
    
    protected void installDefaults()
    {
    	Color bg = quickLogger.getBackground();
    	if(bg == null || bg instanceof UIResource)
    	{
    		bg = UIManager.getColor(PROPERTY_BACKGROUND);
    		quickLogger.setBackground(bg==null ? new ColorUIResource(Color.WHITE):bg);
        	quickLogger.setOpaque(true);
    	}
    }

    protected void uninstallModelChangeListener() {

    	quickLogger.getModel().removeActionListener(getChangeHandler());

    }

    @Override
    public void uninstallUI(JComponent c) {

        uninstallModelChangeListener();
        quickLogger = null;

    }

    @Override
    public void paint(Graphics g1D, JComponent c) {
        super.paint(g1D, c);

        Graphics2D g = (Graphics2D)g1D;
		Rectangle clip = g.getClipBounds();
		
		// Check if any text is there at all
		QuickLoggerModel model = quickLogger.getModel();
		int numLines = model.getNumLines();
		if(numLines <= 0)
			return;
		
		// Get dimensions of fonts
		int lineHeight = Math.max(c.getFontMetrics(dateFont).getHeight(), c.getFontMetrics(messageFont).getHeight());
		int maxAdvance = c.getFontMetrics(dateFont).charWidth('8');
		int dateLength = 11 * maxAdvance;
		
		// Identify lines to be painted
		int minLine = (clip.y - TOP_BORDER)	/ lineHeight;
		minLine = minLine < 0 ? 0 : (minLine >= numLines ? numLines - 1 : minLine);
		int maxLine = (int)Math.ceil(((double)(clip.y + clip.height - TOP_BORDER)) / lineHeight);
		maxLine = maxLine < 0 ? 0 : (maxLine >= numLines ? numLines - 1 : maxLine);
		
		Color messageColor = UIManager.getColor(PROPERTY_MESSAGE_FOREGROUND);
		if(messageColor == null)
			messageColor = Color.BLACK;
		Color dateColor = UIManager.getColor(PROPERTY_DATE_FOREGROUND);
		if(dateColor == null)
			dateColor = new Color(0.4F, 0.4F, 0.4F);
		// Repaint all respective lines
		for(int i=minLine; i<=maxLine; i++)
		{
			String messageLine = model.getLine(i);
			String dateLine = model.getLineTime(i);
			// Check if message is available
			if(messageLine == null)
				break;
			
			g.setColor(messageColor);
			g.setFont(messageFont);
			g.drawString(messageLine, LEFT_BORDER + dateLength, (i+1) * lineHeight + TOP_BORDER);
			if(dateLine != null)
			{
				g.setFont(dateFont);
				g.setColor(dateColor);
				g.drawString(dateLine, LEFT_BORDER, (i+1) * lineHeight + TOP_BORDER);
			}
		}
    }


    public static ComponentUI createUI(JComponent c) {
    	BasicQuickLoggerUI ui = new BasicQuickLoggerUI();   	
    	return ui;
    }
}
