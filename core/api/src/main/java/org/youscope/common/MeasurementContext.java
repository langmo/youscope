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
package org.youscope.common;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;

/**
 * Handed over to measurement components upon initialization, execution and uninitialization. Allows the components to access global measurement properties, transmit data
 * between components.
 * 
 * @author Moritz Lang
 */
public interface MeasurementContext extends Remote
{
	
	/**
	 * Returns the current runtime of the measurement in ms.
	 * @return Current measurement runtime in ms.
	 * @throws RemoteException
	 */
	public long getMeasurementRuntime() throws RemoteException;
	
	
    /**
     * Sets a property, such that it can be loaded by any other job in the measurement. Any property with the same identifier will
     * be replaced.
     * 
     * @param identifier a short identifier for the property.
     * @param property The property which should be saved.
     * @throws RemoteException
     */
    public void setProperty(String identifier, Serializable property) throws RemoteException;

    /**
     * Returns a previously-by this or another measurement component-stored property. 
     * 
     * @param identifier identifier of the property.
     * @return previously stored property, or null if yet not stored.
     * @throws RemoteException
     */
    public Serializable getProperty(String identifier) throws RemoteException;

    /**
     * Returns a previously-by this or another measurement component-stored property. The property is tried to be casted to the provided class or interface. If this fails, null is returned.
     * 
     * @param propertyType type to which the property should be casted.
     * @param identifier identifier of the property.
     * @return previously stored property, or null if yet not stored or cast fails.
     * @throws RemoteException
     */
    <T extends Serializable> T getProperty(String identifier, Class<T> propertyType) throws RemoteException;

    /**
     * Returns the identifiers of all stored properties.
     * 
     * @return identifiers of stored properties.
     * @throws RemoteException
     */
    public String[] getPropertyIdentifiers() throws RemoteException;

    /**
     * Should be called if the structure of the measurement changed during execution, e.g. when child jobs were added or removed or similar.
     * 
     * @throws RemoteException
     */
    public void notifyMeasurementStructureChanged() throws RemoteException;
    
    /**
     * Returns the unique identifier of the measurement containing this component.
     * @return Unique identifier of measurement.
     * @throws RemoteException
     */
    public UUID getMeasurementUUID() throws RemoteException;

}
