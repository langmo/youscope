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

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JLabel;

/**
 * A label which looks like a link and fires ActionEvents.
 * @author Moritz Lang
 *
 */
public class LinkLabel extends JLabel
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 7873501357951806589L;
	private String rawText = "";
	private ArrayList<ActionListener> listeners = new ArrayList<ActionListener>(1);
	/**
	 * Constructor.
	 * Initializes an empty link.
	 */
	public LinkLabel()
	{
		addMouseListener(new MouseAdapter() 
			{
				@Override
				public void mouseClicked(MouseEvent e) 
				{
					fireClicked();
				}                 
			}); 
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}
	/**
	 * Constructor.
	 * Initializes a link with the given text.
	 * @param text Text to be displayed for the link.
	 */
	public LinkLabel(String text)
	{
		this();
		setText(text);
	}
	private void fireClicked()
	{
		for(ActionListener listener : listeners)
		{
			listener.actionPerformed(new ActionEvent(this, 1001, "Link clicked"));
		}
	}
	/**
	 * Adds an action listener which gets activated if the link is pressed.
	 * @param listener Listener to add.
	 */
	public void addActionListener(ActionListener listener)
	{
		listeners.add(listener);
	}
	/**
	 * Removes a previously added listener.
	 * @param listener Listener to be removed.
	 */
	public void removeActionListener(ActionListener listener)
	{
		listeners.remove(listener);
	}
	@Override
	public void setText(String text)
	{         
		rawText = text;
		super.setText("<html><p><a href=\"link\">" + rawText +"</a></p></html>");
	} 
	
	/**
	 * Returns the unmodified text.
	 * Use this method instead of getText() to obtain the text content of this label.
	 * @return Unmodified text.
	 */
	public String getRawText()
	{
		return rawText;
	}
}
