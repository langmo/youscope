/**
 * 
 */
package ch.ethz.csb.youscope.client.uielements;

import java.awt.Dimension;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

/**
 * A panel showing the descirption of something, e.g. a job or a measurement, in YouScope's standard format, including a scroll bar.
 * @author Moritz Lang
 *
 */
public class DescriptionPanel extends JScrollPane
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 2123920497408701552L;

	private final JEditorPane descriptionPane;
	
	private String text = "";
	
	private String header = null;
	
	private boolean isHTML = false;
	
	/**
	 * Same as <code>DescriptionPanel("Description", text)</code>
	 * @param text The text of the description.
	 */
	public DescriptionPanel(String text)
	{
		this("Description", text);
	}
	
	/**
	 * Creates a description panel with the given header and text. The text may contain line breaks (\n).
	 * 
	 * @param header The header of the description.
	 * @param text The text of the description.
	 */
	public DescriptionPanel(String header, String text)
	{
		descriptionPane = new JEditorPane();
		descriptionPane.setEditable(false);
		descriptionPane.setContentType("text/html");
		
		this.text = text;
		this.header = header;
		refreshContent();
				
		setViewportView(descriptionPane);
		setPreferredSize(new Dimension(250, 100));
	}
	
	/**
	 * Sets the text which should be displayed in the description pane. The text may contain line breaks (\n).
	 * @param text Text to be displayed.
	 */
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
	
	/**
	 * Sets if the text (not the header) is HTML (true) or plain text (false). Default is false.
	 * Remark: do not add &lt;html&gt; add the beginning of HTML text.
	 * @param isHTML True if text is HTML formatted.
	 */
	public void setHTML(boolean isHTML)
	{
		this.isHTML = isHTML;
	}
	
	/**
	 * Returns if the text (not the header) is HTML (true) or plain text (false). Default is false.
	 * Remark: do not add &lt;html&gt; add the beginning of HTML text.
	 * @return True if text is HTML formatted.
	 */
	public boolean isHTML()
	{
		return isHTML;
	}
	
	private void refreshContent()
	{
		String content = "<html>";
		if(header!= null)
			content+="<p style=\"font-size:small;margin-top:0px;\"><b>" + header + "</b></p>";
		if(text != null)
		{
			if(isHTML)
			{
				content += text;
			}
			else
			{
				String[] textLines = text.split("\n");
				for(String textLine : textLines)
				{
					content += "<p style=\"font-size:small;margin-top:4px;margin-bottom:0px\">"+textLine+"</p>";
				}
			}
		}
		content += "</html>";
		
		descriptionPane.setText(content);
	}
}
