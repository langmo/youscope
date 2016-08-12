package org.youscope.plugin.ansisbs;

import java.rmi.RemoteException;

import org.youscope.addon.microplate.MicroplateResource;
import org.youscope.addon.microplate.RectangularMicroplateLayout;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.microplate.MicroplateLayout;
import org.youscope.common.resource.ResourceAdapter;
import org.youscope.common.resource.ResourceException;

class AnsiSBS1536MicroplateResource extends ResourceAdapter<AnsiSBS1536MicroplateConfiguration> implements MicroplateResource
{

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 7113640645919510650L;

	public AnsiSBS1536MicroplateResource(PositionInformation positionInformation, AnsiSBS1536MicroplateConfiguration configuration)
					throws ConfigurationException, RemoteException {
		super(positionInformation, configuration, AnsiSBS1536MicroplateConfiguration.TYPE_IDENTIFIER, AnsiSBS1536MicroplateConfiguration.class, "1536 well microplate (ANSI/SBS 1-2004 through ANSI/SBS 4-2004)");
	}

	@Override
	public MicroplateLayout getMicroplateLayout() throws ResourceException, RemoteException {
		return new RectangularMicroplateLayout(48, 32, 2250., 2250.);
	}

}
