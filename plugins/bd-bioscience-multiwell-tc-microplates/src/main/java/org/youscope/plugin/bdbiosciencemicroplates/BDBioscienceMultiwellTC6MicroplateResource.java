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
package org.youscope.plugin.bdbiosciencemicroplates;

import java.rmi.RemoteException;

import org.youscope.addon.microplate.MicroplateResource;
import org.youscope.addon.microplate.RectangularMicroplateLayout;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.microplate.MicroplateLayout;
import org.youscope.common.resource.ResourceAdapter;
import org.youscope.common.resource.ResourceException;

class BDBioscienceMultiwellTC6MicroplateResource extends ResourceAdapter<BDBioscienceMultiwellTC6MicroplateConfiguration> implements MicroplateResource
{

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -380977306932867385L;

	public BDBioscienceMultiwellTC6MicroplateResource(PositionInformation positionInformation, BDBioscienceMultiwellTC6MicroplateConfiguration configuration)
					throws ConfigurationException, RemoteException {
		super(positionInformation, configuration, BDBioscienceMultiwellTC6MicroplateConfiguration.TYPE_IDENTIFIER, BDBioscienceMultiwellTC6MicroplateConfiguration.class, "6 well microplate (BD Bioscience™ - Multiwell™ TC Plate)");
	}

	@Override
	public MicroplateLayout getMicroplateLayout() throws ResourceException, RemoteException {
		return new RectangularMicroplateLayout(3, 2, 39240., 39240.);
	}

}
