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
package org.youscope.plugin.microplate.measurement;


import org.youscope.addon.measurement.MeasurementAddonFactoryAdapter;
/**
 * @author Moritz Lang
 *
 */
public class MicroplateMeasurementAddonFactory extends MeasurementAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public MicroplateMeasurementAddonFactory() 
	{
		super(MicroplateMeasurementAddonUI.class, new MicroplateMeasurementInitializer(), MicroplateMeasurementAddonUI.getMetadata());
	}
}
