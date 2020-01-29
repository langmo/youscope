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
package org.youscope.plugin.slimjob;

import org.youscope.clientinterfaces.YouScopeClient;

/**
 * Class to conveniently get the value of the properties belonging to SLIM.
 * @author mlang
 *
 */
public class SlimProperties 
{
	private final YouScopeClient client;
	private final static String ATTENUATION_FACTOR_PROPERTY = "YouScope.SLIM.attenuationFactor";
	/**
	 * Constructor.
	 * @param client YouScope client.
	 */
	public SlimProperties(YouScopeClient client)
	{
		this.client = client;
	}
	/**
	 * Returns the last identified attenuation factor, or 1 if yet not identified.
	 * @return Last identified attenuation factor.
	 */
	public double getAttenuationFactor()
	{
		return client.getPropertyProvider().getProperty(ATTENUATION_FACTOR_PROPERTY, 1.0);
	}
	/**
	 * Sets the attenuation factor.
	 * @param attenuationFactor Attenuation factor.
	 */
	public void setAttenuationFactor(double attenuationFactor)
	{
		client.getPropertyProvider().setProperty(ATTENUATION_FACTOR_PROPERTY, attenuationFactor);
	}
}
