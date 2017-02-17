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
package org.youscope.plugin.onix;

import java.util.HashSet;

import org.youscope.common.scripting.ScriptStyle;

/**
 * Script style for the Onix (CellAsic) microfluidics protocol programming language.
 * @author Moritz Lang
 *
 */
public class OnixScriptStyle implements ScriptStyle
{
	private HashSet<String> keywords; 

	/**
	 * Constructor.
	 */
	public OnixScriptStyle()
	{
		keywords = new HashSet<String>();
		keywords.add("setflow");
		keywords.add("open");
		keywords.add("close");
		keywords.add("all");
		keywords.add("wait");
		keywords.add("end");
		keywords.add("pwmx");
		keywords.add("pwmy");
		keywords.add("repeat");
		keywords.add("V1");
		keywords.add("V2");
		keywords.add("V3");
		keywords.add("V4");
		keywords.add("V5");
		keywords.add("V6");
		keywords.add("V7");
		keywords.add("V8");
		keywords.add("X");
		keywords.add("Y");
	}
	
	@Override
	public boolean isDelimiter(String character)
	{
		String operands = ";:{}()[]+-/%<=>!&|^~*";

		if (Character.isWhitespace( character.charAt(0) ) ||
			operands.indexOf(character) != -1 )
			return true;
		return false;
	}

	@Override
	public boolean isQuoteDelimiter(String character)
	{
		return false;
	}

	@Override
	public boolean isKeyword(String token)
	{
		return keywords.contains( token );
	}

	@Override
	public String getBlockCommentStartDelimiter()
	{
		return "%{";
	}

	@Override
	public String getBlockCommentEndDelimiter()
	{
		return "%}";
	}

	@Override
	public String getSingleLineDelimiter()
	{
		return "%";
	}

	@Override
	public String getEscapeQuoteDelimiter(String quoteDelimiter)
	{
		return "'" + quoteDelimiter;
	}

	@Override
	public String[] getFileNameExtensions()
	{
		return new String[]{"onix"};
	}

	@Override
	public boolean supportsFileNameExtension(String extension)
	{
		if(extension == null)
			return false;
		if(extension.indexOf('.') == 0)
			extension = extension.substring(1, extension.length());
		for(String supportedExtension : getFileNameExtensions())
		{
			if(supportedExtension.compareToIgnoreCase(extension) == 0)
				return true;
		}
		return false;
	}
	
	@Override
	public String getScriptStyleID()
	{
		return "YouScope.ScriptStyle.Onix";
	}

	@Override
	public String[] getScriptLanguageNames()
	{
		return new String[]{"Onix"};
	}
}
