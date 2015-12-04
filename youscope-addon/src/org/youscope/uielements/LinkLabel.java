/**
 * 
 */
package org.youscope.uielements;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.JLabel;

/**
 * A label which looks like a link and fires ActionEvents.
 * @author langmo
 *
 */
public class LinkLabel extends JLabel
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 7873501357951806589L;
	private String rawText = "";
	private boolean hoover = false;
	private Vector<ActionListener> listeners = new Vector<ActionListener>();
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
				@Override
				public void mouseEntered(MouseEvent e) 
				{                     
					setHoover(true);                 
				}                 
				@Override
				public void mouseExited(MouseEvent e) 
				{                     
					setHoover(false);                 
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
	private void setHoover(boolean hoover)
	{
		this.hoover = hoover;
		setText(getRawText());
	}
	@Override
	public void setText(String text)
	{         
		rawText = text;
		if(hoover)
		{
			super.setText("<html><span style=\"color: #000099;\">" + getRawText() +"</span></html>");
		}
		else
		{
			super.setText("<html><span style=\"color: #000099;\"><u>" + getRawText() +"</u></span></html>");
		}
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
