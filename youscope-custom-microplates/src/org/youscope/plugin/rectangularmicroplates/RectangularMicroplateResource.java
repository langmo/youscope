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
