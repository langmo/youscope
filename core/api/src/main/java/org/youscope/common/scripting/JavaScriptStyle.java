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
 * Script style for JavaScript scripts.
 * @author langmo
 *
 */
public class JavaScriptStyle implements ScriptStyle
{
	private HashSet<String> keywords;

	/**
	 * Constructor.
	 */
	public JavaScriptStyle()
	{
		keywords = new HashSet<String>();
		keywords.add( "abstract" );
		keywords.add( "boolean" );
		keywords.add( "break" );
		keywords.add( "byte" );
		keywords.add( "case" );
		keywords.add( "catch" );
		keywords.add( "char" );
		keywords.add( "class" );
		keywords.add( "const" );
		keywords.add( "continue" );
		keywords.add( "default" );
		keywords.add( "delete" );
		keywords.add( "do" );
		keywords.add( "double" );
		keywords.add( "else" );
		keywords.add( "export" );
		keywords.add( "extends" );
		keywords.add( "false" );
		keywords.add( "final" );
		keywords.add( "finally" );
		keywords.add( "float" );
		keywords.add( "for" );
		keywords.add( "function" );
		keywords.add( "goto" );
		keywords.add( "if" );
		keywords.add( "implements" );
		keywords.add( "in" );
		keywords.add( "instanceof" );
		keywords.add( "int" );
		keywords.add( "long" );
		keywords.add( "native" );
		keywords.add( "new" );
		keywords.add( "null" );
		keywords.add( "package" );
		keywords.add( "private" );
		keywords.add( "protected" );
		keywords.add( "public" );
		keywords.add( "return" );
		keywords.add( "short" );
		keywords.add( "static" );
		keywords.add( "super" );
		keywords.add( "switch" );
		keywords.add( "synchronized" );
		keywords.add( "this" );
		keywords.add( "throw" );
		keywords.add( "throws" );
		keywords.add( "transient" );
		keywords.add( "true" );
		keywords.add( "try" );
		keywords.add( "typeof" );
		keywords.add( "undefined" );
		keywords.add( "var" );
		keywords.add( "void" );
		keywords.add( "while" );
		keywords.add( "with" );
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
		String quoteDelimiters = "\"'";

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
		return "/*";
	}

	@Override
	public String getBlockCommentEndDelimiter()
	{
		return "*/";
	}

	@Override
	public String getSingleLineDelimiter()
	{
		return "//";
	}

	@Override
	public String getEscapeQuoteDelimiter(String quoteDelimiter)
	{
		return "\\" + quoteDelimiter;
	}

	@Override
	public String[] getFileNameExtensions()
	{
		return new String[]{"js"};
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
		return "YouScope.ScriptStyle.JavaScript";
	}

	@Override
	public String[] getScriptLanguageNames()
	{
		return new String[]{"JavaScript"};
	}
}
