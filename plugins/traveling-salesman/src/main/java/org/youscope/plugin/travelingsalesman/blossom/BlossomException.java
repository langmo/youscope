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
package org.youscope.plugin.travelingsalesman.blossom;

/**
 * Exception thrown in blossom algorithm.
 * @author Moritz Lang
 *
 */
public class BlossomException extends Exception {

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -885010991662839919L;
	/**
	 * Constructor.
	 * @param description error description.
	 */
	public BlossomException(String description) {
		super(description);
	}
	/**
	 * Constructor.
	 * @param description error description.
	 * @param cause Cause of the exception.
	 */
	public BlossomException(String description, Throwable cause) {
		super(description, cause);
	}
}
