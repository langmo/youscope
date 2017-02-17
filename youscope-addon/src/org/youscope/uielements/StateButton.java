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
package org.youscope.uielements;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JComponent;

/**
 * A component consisting of a label at the left and a little square at the right, being either red (deactivated) or green (activated).
 * @author Moritz Lang
 *
 */
public class StateButton extends JComponent
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 683590117757351737L;
	private String text;
	private boolean active;
	
	private static final Font FONT = new Font("Dialog", Font.PLAIN, 10);
	private static final Color ACTIVE_COLOR = new Color(0f, 0.8f, 0f);
	private static final Color PASSIVE_COLOR = new Color(0.8f, 0f, 0f);
	
	private final int BOX_SIZE = 12;
	
	/**
	 * Constructor. Same as StateButton(text, false).
	 * @param text The text of the state button.
	 */
	public StateButton(String text)
	{
		this(text, false);
		this.setMinimumSize(new Dimension(100, 20));
	}
	
	/**
	 * Constructor.
	 * @param text The text of the state button.
	 * @param active True if the state button should be active at startup.
	 */
	public StateButton(String text, boolean active)
	{
		this.text = text;
		this.active = active;
		this.setPreferredSize(new Dimension(150, 20));
		this.setOpaque(false);
	}

	/**
	 * Changes the text of the state button.
	 * @param text The new text.
	 */
	public void setText(String text)
	{
		this.text = text;
		repaint();
	}

	/**
	 * Returns the current text of the button.
	 * @return Current text of button.
	 */
	public String getText()
	{
		return text;
	}

	/**
	 * Sets if the button is active.
	 * @param active True if active.
	 */
	public void setActive(boolean active)
	{
		if(active == this.active)
			return;
		this.active = active;
		repaint();
	}

	/**
	 * Returns if the button is active.
	 * @return True if active.
	 */
	public boolean isActive()
	{
		return active;
	}
	
	@Override
	public void paint(Graphics g)
	{
		int height = getHeight();
		
		g.setColor(Color.BLACK);
		g.setFont(FONT);
		g.drawString(text, 4 + BOX_SIZE, 10 + (height - 10) / 2);
		if(active)
			g.setColor(ACTIVE_COLOR);
		else
			g.setColor(PASSIVE_COLOR);
		
		g.fillRect(2, (height - BOX_SIZE)/2, BOX_SIZE, BOX_SIZE);
		g.setColor(Color.BLACK);
		g.drawRect(2, (height - BOX_SIZE)/2, BOX_SIZE, BOX_SIZE);
	}
}
