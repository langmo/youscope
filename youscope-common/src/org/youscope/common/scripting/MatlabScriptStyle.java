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
package org.youscope.common.scripting;

import java.util.HashSet;

/**
 * Script style for Matlab scripts.
 * @author Moritz Lang
 *
 */
public class MatlabScriptStyle implements ScriptStyle
{
	private HashSet<String> keywords;

	/**
	 * Constructor.
	 */
	public MatlabScriptStyle()
	{
		keywords = new HashSet<String>();
		keywords.add( "break" );
		keywords.add( "case" );
		keywords.add( "catch" );
		keywords.add( "classdef" );
		keywords.add( "continue" );
		keywords.add( "else" );
		keywords.add( "elseif" );
		keywords.add( "end" );
		keywords.add( "for" );
		keywords.add( "function" );
		keywords.add( "global" );
		keywords.add( "if" );
		keywords.add( "otherwise" );
		keywords.add( "parfor" );
		keywords.add( "persistent" );
		keywords.add( "return" );
		keywords.add( "spmd" );
		keywords.add( "switch" );
		keywords.add( "try" );
		keywords.add( "while" );
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
		String quoteDelimiters = "'";

		if (quoteDelimiters.indexOf(character) < 0)
			return false;
		return true;
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
		return new String[]{"m"};
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
		return "YouScope.ScriptStyle.Matlab";
	}

	@Override
	public String[] getScriptLanguageNames()
	{
		return new String[]{"Matlab"};
	}
}
