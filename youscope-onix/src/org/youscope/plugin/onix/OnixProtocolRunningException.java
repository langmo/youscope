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

/**
 * Thrown by the Onix plugin if the microfluidic device is accessed while a protocol is running, which blocks access to the device.
 * @author Moritz Lang
 *
 */
public class OnixProtocolRunningException extends Exception
{

	/**
	 * Serial version UID.
	 */
	private static final long	serialVersionUID	= 8187133506632404982L;

	/**
	 * Constructor.
	 */
	public OnixProtocolRunningException()
	{
		super("A protocol is currently running on the Onix device. Stop the protocol execution or wait until the protocol is finished before accessing the functionality of the Onix device in any other manner.");
	}
}
