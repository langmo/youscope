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

/**
 * Definition of how a script is rendered. Should be implemented for each script language for which
 * a specific rendering is wanted.
 * 
 * @author langmo
 *
 */
public interface ScriptStyle
{
	/**
	 * Returns true if the given character delimits two words/commands (e.g. ".", ",", ";", ...)
	 * @param character The character for which it should be queried if it is a delimiter.
	 * @return True if character is a delimiter.
	 */
	public boolean isDelimiter(String character);

	/**
	 * Returns true if the given character can be used to delimit a quote (e.g. " or ').
	 * @param character The character for which it should be queried if it is a quote delimiter.
	 * @return True if character is a quote delimiter.
	 */
	public boolean isQuoteDelimiter(String character);

	/**
	 * Returns true if the given token is a language specific keyword (e.g. "global", "function", ...).
	 * @param token
	 * @return true if the token is a keyword.
	 */
	public boolean isKeyword(String token);

	/**
	 * Returns the character sequence used in the language to start a block comment.
	 * @return Character sequence to start a block comment, or null if block comments do not exist in the language.
	 */
	public String getBlockCommentStartDelimiter();

	/**
	 * Returns the character sequence used in the language to end a block comment.
	 * @return Character sequence to end a block comment, or null if block comments do not exist in the language.
	 */
	public String getBlockCommentEndDelimiter();

	/**
	 * Returns the character sequence used in the language to start a single line comment.
	 * @return Character sequence to start a single line comment, or null if single line comments do not exist in the language.
	 */
	public String getSingleLineDelimiter();

	/**
	 * Returns the string used to print the given quote delimiter inside a quote without stopping the quote
	 * (if quote delimiter is e.g. ", the escaped one might be \").
	 * @param quoteDelimiter The escaped version of the quote delimiter.
	 * @return the escaped quote delimiter, or null if not existing.
	 */
	public String getEscapeQuoteDelimiter(String quoteDelimiter);
	
	/**
	 * Returns an array of all file name extensions typically associated with scripts for which this style is made.
	 * @return List of typical file extensions.
	 */
	public String[] getFileNameExtensions();
	
	/**
	 * Returns true if the given file name extension is typically associated with scripts for which this style is made.
	 * @param extension
	 * @return true if this extension is typically for script files of this type.
	 */
	public boolean supportsFileNameExtension(String extension);
	
	/**
	 * Should return a unique ID with which the script style implementation can be identified. Different to the name of the script language, the ID should be different
	 * for every implementation.
	 * @return Unique ID for the script style implementation.
	 */
	public String getScriptStyleID();
	
	/**
	 * Should return a list of the names of all scripting language implemented by the respective implementation (e.g. JavaScript). If two scripting style implementations are styles for the same language,
	 * they should return the same name.
	 * @return Names of scripting languages supported by this style.
	 */
	public String[] getScriptLanguageNames();

}
