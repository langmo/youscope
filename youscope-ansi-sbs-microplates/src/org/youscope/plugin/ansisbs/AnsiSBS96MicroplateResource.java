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
package org.youscope.plugin.ansisbs;

import java.rmi.RemoteException;

import org.youscope.addon.microplate.MicroplateResource;
import org.youscope.addon.microplate.RectangularMicroplateLayout;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.microplate.MicroplateLayout;
import org.youscope.common.resource.ResourceAdapter;
import org.youscope.common.resource.ResourceException;

class AnsiSBS96MicroplateResource extends ResourceAdapter<AnsiSBS96MicroplateConfiguration> implements MicroplateResource
{

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 7111640645919510650L;

	public AnsiSBS96MicroplateResource(PositionInformation positionInformation, AnsiSBS96MicroplateConfiguration configuration)
					throws ConfigurationException, RemoteException {
		super(positionInformation, configuration, AnsiSBS96MicroplateConfiguration.TYPE_IDENTIFIER, AnsiSBS96MicroplateConfiguration.class, "96 well microplate (ANSI/SBS 1-2004 through ANSI/SBS 4-2004)");
	}

	@Override
	public MicroplateLayout getMicroplateLayout() throws ResourceException, RemoteException {
		return new RectangularMicroplateLayout(12, 8, 9000., 9000.);
	}

}
