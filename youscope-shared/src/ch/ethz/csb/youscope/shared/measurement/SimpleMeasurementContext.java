package ch.ethz.csb.youscope.shared.measurement;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.UUID;

/**
 * A simple implementation of a measurement context. This implementation can be used to initialize and call measurement components
 * without the need to construct an actual measurement, e.g. during a configuration of a component to show the expected outcome given the current configuration.
 * Note, that YouScope uses a different implementation during the execution of a measurement.
 * @author Moritz Lang
 *
 */
public class SimpleMeasurementContext extends UnicastRemoteObject implements MeasurementContext
{
	private final HashMap<String, Serializable> properties = new HashMap<String, Serializable>();
	
	private final UUID uniqueIdentifier = UUID.randomUUID();
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 8098035348098383719L;
	/**
	 * Constructor.
	 * @throws RemoteException
	 */
	public SimpleMeasurementContext() throws RemoteException
	{
		super();
	}
	
    @Override
    public void setProperty(String identifier, Serializable property)
    {
        properties.put(identifier, property);
    }
    
    @Override
    public String[] getPropertyIdentifiers()
    {
        return properties.keySet().toArray(new String[0]);
    }
    
    @Override
    public Serializable getProperty(String identifier)
    {
        return properties.get(identifier);
    }
    
    @Override
    public <T extends Serializable> T getProperty(String identifier, Class<T> propertyType)
    {
    	Serializable property = getProperty(identifier);
        if (property == null)
            return null;
        if (propertyType.isInstance(property))
            return propertyType.cast(property);
		return null;
    }

    @Override
    public void notifyMeasurementStructureChanged() throws RemoteException
    {
        // do nothing
    }

	@Override
	public UUID getMeasurementUUID() throws RemoteException {
		return uniqueIdentifier;
	}

}
