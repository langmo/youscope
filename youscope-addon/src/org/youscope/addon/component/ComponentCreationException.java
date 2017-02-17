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
package org.youscope.addon.component;

/**
 * Exception thrown if creation of a measurement component failed
 * @author Moritz Lang
 *
 */
public class ComponentCreationException extends Exception {

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 7512230465385081443L;

	/**
	 * Constructor.
	 * @param message The error message.
	 */
	public ComponentCreationException(String message) {
		super(message);
	}

	/**
	 * Constructor.
	 * @param message the error message.
	 * @param cause The more detailed cause of the exception
	 */
	public ComponentCreationException(String message, Throwable cause) {
		super(message, cause);
	}

}
