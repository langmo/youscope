package org.youscope.plugin.bdbiosciencemicroplates;

import java.rmi.RemoteException;

import org.youscope.addon.microplate.MicroplateResource;
import org.youscope.addon.microplate.RectangularMicroplateLayout;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.microplate.MicroplateLayout;
import org.youscope.common.resource.ResourceAdapter;
import org.youscope.common.resource.ResourceException;

class BDBioscienceMultiwellTC24MicroplateResource extends ResourceAdapter<BDBioscienceMultiwellTC24MicroplateConfiguration> implements MicroplateResource
{

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 8952798724774398062L;
	
	public BDBioscienceMultiwellTC24MicroplateResource(PositionInformation positionInformation, BDBioscienceMultiwellTC24MicroplateConfiguration configuration)
					throws ConfigurationException, RemoteException {
		super(positionInformation, configuration, BDBioscienceMultiwellTC24MicroplateConfiguration.TYPE_IDENTIFIER, BDBioscienceMultiwellTC24MicroplateConfiguration.class, "24 well microplate (BD Bioscience™ - Multiwell™ TC Plate)");
	}

	@Override
	public MicroplateLayout getMicroplateLayout() throws ResourceException, RemoteException {
		return new RectangularMicroplateLayout(6, 4, 19300., 19300.);
	}

}
