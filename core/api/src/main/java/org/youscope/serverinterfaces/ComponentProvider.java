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
package org.youscope.serverinterfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.youscope.addon.component.ComponentCreationException;
import org.youscope.common.Component;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.Configuration;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.Job;
import org.youscope.common.job.JobConfiguration;

/**
 * Interface to create measurement components like jobs or resources.
 * @author Moritz Lang
 *
 */
public interface ComponentProvider extends Remote 
{
	/**
	 * Creates a measurement component for the given configuration.
	 * @param configuration The configuration of the component.
	 * @param positionInformation The abstract position of the newly created component in the measurement.
	 * @return A newly created component for the given configuration.
	 * @throws ComponentCreationException 
	 * @throws ConfigurationException 
	 * @throws RemoteException 
	 * @thrown ComponentCreationException
	 */
	public Component createComponent(PositionInformation positionInformation, Configuration configuration) throws ComponentCreationException, ConfigurationException, RemoteException;
	
	/**
	 * Creates a component with the given type identifier and its default configuration, which additionally has a given interface.
	 * Note that not all components support default construction. In this case, a ComponentCreationException is thrown. Furthermore, note that the state of the component
	 * might be invalid, and that the component should be further configured using the component's interface methods.
	 * @param positionInformation The abstract position of the newly created component in the measurement.
	 * @param typeIdentifier The type identifier of the component.
	 * @param componentInterface The interface of the component which should be created.
	 * @return A newly created component with the given type identifier and interface.
	 * @throws ComponentCreationException
	 * @throws RemoteException
	 */
	public <T extends Component> T createComponent(PositionInformation positionInformation, String typeIdentifier, Class<T> componentInterface) throws ComponentCreationException, RemoteException;
	
	/**
	 * Creates a component with the given type identifier and its default configuration
	 * Note that not all components support default construction. In this case, a ComponentCreationException is thrown. Furthermore, note that the state of the component
	 * might be invalid, and that the component should be further configured using the component's interface methods.
	 * @param positionInformation The abstract position of the newly created component in the measurement.
	 * @param typeIdentifier The type identifier of the component.
	 * @return A newly created component with the given type identifier and interface.
	 * @throws ComponentCreationException
	 * @throws RemoteException
	 */
	public Component createComponent(PositionInformation positionInformation, String typeIdentifier) throws ComponentCreationException, RemoteException;
	
	/**
	 * Creates a measurement component of a given type for the given configuration. If the component corresponding to the configuration is not of the specified type,
	 * a ComponentCreationException is thrown.
	 * @param componentKind The super-class/kind of the component.
	 * @param configuration The configuration of the component.
	 * @param positionInformation The abstract position of the newly created component in the measurement.
	 * @return A newly created component for the given configuration.
	 * @throws ComponentCreationException 
	 * @throws ConfigurationException 
	 * @throws RemoteException 
	 * @thrown ComponentCreationException
	 */
	public <T extends Component> T createComponent(PositionInformation positionInformation, Configuration configuration, Class<T> componentKind) throws ComponentCreationException, ConfigurationException, RemoteException;
	
	/**
	 * Creates a job for the given configuration. Shorthand for <code>createComponent(positionInformation, jobConfiguration, Job.class</code>. If the component corresponding to the configuration is not a job,
	 * a ComponentCreationException is thrown.
	 * @param jobConfiguration The configuration of the job.
	 * @param positionInformation The abstract position of the newly created job in the measurement.
	 * @return A newly created job for the given job configuration.
	 * @throws ComponentCreationException
	 * @throws ConfigurationException
	 * @throws RemoteException
	 */
	public Job createJob(PositionInformation positionInformation, JobConfiguration jobConfiguration) throws ComponentCreationException, ConfigurationException, RemoteException;
	
	/**
	 * Creates a job which has a given type identifier and interface with its default configuration. 
	 * Note that not all jobs support default construction. In this case, a ComponentCreationException is thrown. Furthermore, note that the state of the job
	 * might be invalid, and that the job should be further configured using the job interface methods.
	 * @param positionInformation The abstract position of the newly created job in the measurement.
	 * @param typeIdentifier Type identifier of job.
	 * @param jobInterface The interface of the job which should be created.
	 * @return A newly created job with the given interface.
	 * @throws ComponentCreationException
	 * @throws RemoteException
	 */
	public <T extends Job> T createJob(PositionInformation positionInformation, String typeIdentifier, Class<T> jobInterface) throws ComponentCreationException, RemoteException;
	
	/**
	 * Creates a job which has a given type identifier with its default configuration. 
	 * Note that not all jobs support default construction. In this case, a ComponentCreationException is thrown. Furthermore, note that the state of the job
	 * might be invalid, and that the job should be further configured using the job interface methods.
	 * @param positionInformation The abstract position of the newly created job in the measurement.
	 * @param typeIdentifier Type identifier of job.
	 * @return A newly created job with the given interface.
	 * @throws ComponentCreationException
	 * @throws RemoteException
	 */
	public Job createJob(PositionInformation positionInformation, String typeIdentifier) throws ComponentCreationException, RemoteException;
	
	/**
	 * Creates a job of a given class for the given configuration. Shorthand for <code>createComponent(positionInformation, jobConfiguration, jobInterface)</code>. If the component corresponding to the configuration does not implement the given job interface,
	 * a ComponentCreationException is thrown.
	 * @param jobConfiguration The configuration of the job.
	 * @param positionInformation The abstract position of the newly created job in the measurement.
	 * @param jobInterface The interface of the job which should be created.
	 * @return A newly created job with the given interface for the given job configuration.
	 * @throws ComponentCreationException
	 * @throws ConfigurationException
	 * @throws RemoteException
	 */
	public <T extends Job> T  createJob(PositionInformation positionInformation, JobConfiguration jobConfiguration, Class<T> jobInterface) throws ComponentCreationException, ConfigurationException, RemoteException;
}
