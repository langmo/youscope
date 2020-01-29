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

class BDBioscienceMultiwellTC12MicroplateResource extends ResourceAdapter<BDBioscienceMultiwellTC12MicroplateConfiguration> implements MicroplateResource
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -8121231419014104268L;

	public BDBioscienceMultiwellTC12MicroplateResource(PositionInformation positionInformation, BDBioscienceMultiwellTC12MicroplateConfiguration configuration)
					throws ConfigurationException, RemoteException {
		super(positionInformation, configuration, BDBioscienceMultiwellTC12MicroplateConfiguration.TYPE_IDENTIFIER, BDBioscienceMultiwellTC12MicroplateConfiguration.class, "12 well microplate (BD Bioscience™ - Multiwell™ TC Plate)");
	}

	@Override
	public MicroplateLayout getMicroplateLayout() throws ResourceException, RemoteException {
		return new RectangularMicroplateLayout(4, 3, 26000., 26000.);
	}

}
