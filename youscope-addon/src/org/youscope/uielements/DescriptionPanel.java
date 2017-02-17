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


import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;

/**
 * A panel showing the descirption of something, e.g. a job or a measurement, in YouScope's standard format, including a scroll bar.
 * @author Moritz Lang
 *
 */
public class DescriptionPanel extends JEditorPane
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 2123920497408701552L;

	private String text;
	
	private String header;
	
	/**
	 * Same as <code>DescriptionPanel(null, null)</code>
	 */
	public DescriptionPanel()
	{
		this(null, null);
	}
	
	/**
	 * Same as <code>DescriptionPanel(null, text)</code>
	 * @param text The text of the description.
	 */
	public DescriptionPanel(String text)
	{
		this(null, text);
	}
	
	/**
	 * Creates a description panel with the given header and text. The text may contain line breaks (\n).
	 * 
	 * @param header The header of the description.
	 * @param text The text of the description.
	 */
	public DescriptionPanel(String header, String text)
	{
		setEditable(false);
		setContentType("text/html");
		
		this.text = text;
		this.header = header;
		refreshContent();
				
	}
	
	/**
	 * Sets the text which should be displayed in the description pane. The text may contain line breaks (\n), but should not contain any (html or similar) formatting.
	 * @param text Text to be displayed.
	 */
	@Override
	public void setText(String text)
	{
		this.text = text;
		refreshContent();
	}
	
	
	/**
	 * Sets the header for this description. Set to null to not display a header.
	 * @param header Header to be displayed.
	 */
	public void setHeader(String header)
	{
		this.header = header;
		refreshContent();
	}
	
	
	private void refreshContent()
	{
		Runnable runner = new Runnable()
		{

			@Override
			public void run() {
				String content = "<html>";
				if(header!= null)
					content+="<h1>" + header + "</h1>";
				if(text != null)
				{
					String[] textLines = text.split("\n");
					for(String textLine : textLines)
					{
						content += "<p>"+textLine+"</p>";
					}
				}
				content += "</html>";
				
				DescriptionPanel.super.setText(content);
				setCaretPosition(0); 
			}
	
		};
		if(SwingUtilities.isEventDispatchThread())
			runner.run();
		else
			SwingUtilities.invokeLater(runner);
	}
}
