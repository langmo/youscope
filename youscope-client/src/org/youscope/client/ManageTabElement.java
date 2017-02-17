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
package org.youscope.client;

import javax.swing.JPanel;

/**
 * @author Moritz Lang
 *
 */
abstract class ManageTabElement extends JPanel
{
	/**
	 * Serial Version UID
	 */
	private static final long	serialVersionUID	= -2478439915045468521L;
	public abstract void initializeContent();
	public abstract boolean storeContent();
}
