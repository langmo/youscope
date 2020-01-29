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
package org.youscope.plugin.rectangularmicroplates;

import java.rmi.RemoteException;

import org.youscope.addon.microplate.MicroplateResource;
import org.youscope.addon.microplate.RectangularMicroplateLayout;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.microplate.MicroplateLayout;
import org.youscope.common.resource.ResourceAdapter;
import org.youscope.common.resource.ResourceException;

class RectangularMicroplateResource extends ResourceAdapter<RectangularMicroplateConfiguration> implements MicroplateResource
{
	/**
	 * Serial Version UID. 
	 */
	private static final long serialVersionUID = -6734244891085687375L;

	public RectangularMicroplateResource(PositionInformation positionInformation, RectangularMicroplateConfiguration configuration)
					throws ConfigurationException, RemoteException {
		super(positionInformation, configuration, RectangularMicroplateConfiguration.TYPE_IDENTIFIER, RectangularMicroplateConfiguration.class, "Rectangular Microplate");
	}

	@Override
	public MicroplateLayout getMicroplateLayout() throws ResourceException, RemoteException 
	{
		RectangularMicroplateConfiguration configuration = getConfiguration();
		return new RectangularMicroplateLayout(configuration.getNumWellsX(), configuration.getNumWellsY(), configuration.getWellWidth(), configuration.getWellHeight());
	}

}
