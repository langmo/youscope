package org.youscope.server;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.UUID;

import org.youscope.common.measurement.MeasurementContext;

class MeasurementContextImpl implements MeasurementContext
{
    private final HashMap<String, Serializable> storedProperties = new HashMap<String, Serializable>();

    private final MeasurementImpl measurement;

    MeasurementContextImpl(final MeasurementImpl measurement)
    {
        this.measurement = measurement;
    }

    @Override
    public void setProperty(String identifier, Serializable property)
    {
        synchronized (storedProperties)
        {
            storedProperties.put(identifier, property);
        }
    }

    @Override
    public Serializable getProperty(String identifier)
    {
        synchronized (storedProperties)
        {
            return storedProperties.get(identifier);
        }
    }

    @Override
    public String[] getPropertyIdentifiers()
    {
        synchronized (storedProperties)
        {
            return storedProperties.keySet().toArray(new String[0]);
        }
    }

    void clear()
    {
        synchronized (storedProperties)
        {
            storedProperties.clear();
        }
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
        measurement.measurementStructureModified();
    }

	@Override
	public UUID getMeasurementUUID() throws RemoteException {
		return measurement.getUUID();
	}

}
