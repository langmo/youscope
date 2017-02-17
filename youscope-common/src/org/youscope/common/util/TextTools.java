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
package org.youscope.common.util;

/**
 * Class providing helper functions to handle text like descriptions, e.g. to convert text to html.
 * @author Moritz Lang
 *
 */
public class TextTools {

	private final StringBuffer buffer = new StringBuffer();
	
	/**
	 * Private constructor. Use static methods.
	 */
	private TextTools() 
	{
		// do nothing.
	}

	/**
	 * Returns for a given duration measured in ms a string describing the duration in terms of days, hours, minutes...
	 * @param durationMS Duration in ms for which string should be returned.
	 * @return String describing duration in terms of human readable units.
	 */
	public static String toDurationString(long durationMS)
	{
		if(durationMS == 0)
			return "0 s";
		String[] units = {"d", "h", "m", "s", "ms"};
		long[] values = {
				(durationMS / 1000 / 60/60/24),
				(durationMS / 1000 / 60/60) % 24,
				(durationMS / 1000 / 60) % 60,
				(durationMS / 1000) % 60,
				durationMS % 1000};
		
		int firstNonNull = 0;
		for(int i=0; i<values.length; i++)
		{
			if(values[i] == 0)
				firstNonNull = i+1;
			else
				break;
		}
		
		int lastNonNull = values.length-1;
		for(int i=values.length-1; i>=0; i--)
		{
			if(values[i] == 0)
				lastNonNull = i-1;
			else
				break;
		}
		String returnVal = "";
		for(int i=firstNonNull; i<=lastNonNull; i++)
		{
			if(i != firstNonNull)
				returnVal+=" ";
			returnVal += Long.toString(values[i])+units[i];	
		}
		
		return returnVal;
	}
	
	
	/**
	 * Converts a normal text (e.g. a description) into HTML.
	 * Note: To use in a Swing component, enclose returned String in &lt;html&gt; and &lt;/html&gt; 
	 * @param text Text to convert into HTML.
	 * @return HTML text.
	 */
	public static String toHTML(String text)
	{
		return new TextTools().toHTMLinternal(null, text);
	}
	/**
	 * Converts a normal text (e.g. a description) into HTML. Adds a title on top of the text.
	 * Note: To use in a Swing component, enclose returned String in &lt;html&gt; and &lt;/html&gt; 
	 * @param title The title to add on top of the text.
	 * @param text Text to convert into HTML.
	 * @return HTML text.
	 */
	public static String toHTML(String title, String text)
	{
		return new TextTools().toHTMLinternal(title, text);
	}
	private String toHTMLinternal(String title, String text)
	{
		if(title != null && title.length() > 0)
		{
			buffer.append("<p><b>"+title+"</b></p>");
		}
		String[] lines = text.split("\n");
		for(int lineID = 0; lineID < lines.length; lineID++)
		{
			String line = lines[lineID].trim();
			if(line.isEmpty())
			{
				endParagraph();
				continue;
			}
			newLine();
			beginParagraph();
			addLine(line);
		}
		
		endList();
		endParagraph();
		return buffer.toString();
	}
	
	private boolean paragraph = false;
	private void beginParagraph()
	{
		if(paragraph)
			return;
		buffer.append("<p>");
		paragraph = true;
	}
	private void newLine()
	{
		if(!paragraph)
			return;
		buffer.append("<br />");
	}
	private void endParagraph()
	{
		if(!paragraph)
			return;
		buffer.append("</p>");
		paragraph = false;
	}
	
	private boolean list = false;
	private void beginList()
	{
		if(list)
			return;
		buffer.append("<ul>");
		list = true;
	}
	private void endList()
	{
		if(!list)
			return;
		buffer.append("</ul>");
		list = false;
	}
	private void addString(String string)
	{
		buffer.append(string);
	}
	private void addLine(String line)
	{
		if(line.startsWith("- "))
		{
			beginList();
			buffer.append("<li>");
			addString(line.substring(2));
			buffer.append("</li>");
		}
		else
		{
			endList();
			addString(line);
		}
	}
	
	/**
	 * Capitalizes the first, and each new word in the string.
	 * @param string String to capitalize.
	 * @return Capitalized string.
	 * @throws NullPointerException Thrown if string is null.
	 */
	public static String capitalize(String string) throws NullPointerException
	{
		if(string == null)
			throw new NullPointerException();
		char[] chars = string.toCharArray();
		boolean found = false;
		for (int i = 0; i < chars.length; i++) 
		{
			if (!found && Character.isLetter(chars[i])) 
			{
				chars[i] = Character.toUpperCase(chars[i]);
				found = true;
			} 
			else if(!Character.isAlphabetic(chars[i])) 
			{
				found = false;
			}
		}
		return String.valueOf(chars);
	}
	/**
	 * Capitalizes the first character in the string.
	 * @param string string to modify.
	 * @return input string with first character capitalized.
	 * @throws NullPointerException  Thrown if string is null.
	 */
	public static String capitalizeFirst(String string) throws NullPointerException
	{
		if(string == null)
			throw new NullPointerException();
		else if(string.length() < 1)
			return string;
		return Character.toUpperCase(string.charAt(0))+string.substring(1);
	}
	/**
	 * Takes the string, and converts it to a string which can be used as part of a filename.
	 * The following characters are forbidden, based on windows systems: "*\/:<>?|
	 * These characters are replaced by underscores. Additionally, an underscore is added to the beginning of the file name
	 * if it starts
	 * @param string The string which should be converted.
	 * @return the converted string.
	 */
	public static String convertToFileName(String string)
	{
		string = string.replaceAll("[\"\\*/:<>\\?\\\\\\|]", "_");
		// File names starting with a point not permitted in Windows
		if(string.charAt(0)=='.')
			return "_"+string;
		return string;
	}
}
