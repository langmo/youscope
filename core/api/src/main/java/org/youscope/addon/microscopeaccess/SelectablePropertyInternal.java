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
package org.youscope.addon.microscopeaccess;


/**
 * @author Moritz Lang
 *
 */
public interface SelectablePropertyInternal extends PropertyInternal
{
	/**
	 * Returns a list of all allowed property values. If all possible values are allowed, the allowed values are not known, or the allowed values are not discrete, returns null.
	 * @return List of all allowed values or null.
	 */
	public String[] getAllowedPropertyValues();
}
