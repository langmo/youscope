package org.youscope.plugin.custommicroplates;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.UUID;

import org.youscope.addon.microplate.MicroplateResource;
import org.youscope.addon.microplate.RectangularMicroplateLayout;
import org.youscope.common.MeasurementContext;
import org.youscope.common.MessageListener;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.microplate.MicroplateLayout;
import org.youscope.common.resource.ResourceException;

class CustomMicroplateResource extends UnicastRemoteObject implements MicroplateResource
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -7366672098628415823L;
	private CustomMicroplateDefinition microplateDefinition;
	private final PositionInformation positionInformation;
	private final String typeIdentifier;
	private String name = "custom microplate";
	private final UUID uuid = UUID.randomUUID();
	public CustomMicroplateResource(PositionInformation positionInformation, String typeIdentifier)
					throws ConfigurationException, RemoteException {
		this.typeIdentifier = typeIdentifier;
		this.positionInformation= positionInformation;
	}

	@Override
	public MicroplateLayout getMicroplateLayout() throws ResourceException, RemoteException 
	{
		if(microplateDefinition == null)
			throw new ResourceException("Using custom microplate resource without initializing it.");
		
		return new RectangularMicroplateLayout(microplateDefinition.getNumWellsX(), microplateDefinition.getNumWellsY(), microplateDefinition.getWellWidth(), microplateDefinition.getWellHeight());
	}

	@Override
	public String getTypeIdentifier() throws RemoteException {
		return CustomMicroplateManager.getCustomMicroplateTypeIdentifier(microplateDefinition.getCustomMicroplateName());
	}

	@Override
	public void initialize(MeasurementContext measurementContext) throws ResourceException, RemoteException {
		try {
			microplateDefinition = CustomMicroplateManager.getCustomMicroplate(typeIdentifier);
		} catch (CustomMicroplateException e) {
			throw new ResourceException("Could not get information on layout of custom microplate with type identifer " + typeIdentifier+".", e);
		}
	}

	@Override
	public void uninitialize(MeasurementContext measurementContext) throws ResourceException, RemoteException {
		microplateDefinition = null;
	}

	@Override
	public boolean isInitialized() throws RemoteException {
		return microplateDefinition != null;
	}

	@Override
	public void addMessageListener(MessageListener writer) throws RemoteException {
		// no messages
		
	}

	@Override
	public void removeMessageListener(MessageListener writer) throws RemoteException {
		// no messages
	}

	@Override
	public PositionInformation getPositionInformation() throws RemoteException {
		return positionInformation;
	}

	@Override
	public String getName() throws RemoteException {
		return name;
	}

	@Override
	public void setName(String name) throws RemoteException {
		this.name = name;
	}

	@Override
	public UUID getUUID() throws RemoteException {
		return uuid;
	}

}
