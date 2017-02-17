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
package org.youscope.addon;

/**
 * Exception thrown by any addon to signal an error in the addon.
 * @author Moritz Lang
 *
 */
public class AddonException extends Exception {

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 7512230411385081443L;

	/**
	 * Constructor.
	 * @param message The error message.
	 */
	public AddonException(String message) {
		super(message);
	}

	/**
	 * Constructor.
	 * @param message the error message.
	 * @param cause The more detailed cause of the exception
	 */
	public AddonException(String message, Throwable cause) {
		super(message, cause);
	}

}
