/**
 * 
 */
package ch.ethz.csb.youscope.server;

import java.lang.reflect.Constructor;
import java.rmi.RemoteException;
import java.util.ServiceLoader;

import ch.ethz.csb.youscope.server.addon.job.JobConstructionAddonFactory;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;
import ch.ethz.csb.youscope.shared.measurement.job.Job;
import ch.ethz.csb.youscope.shared.measurement.job.JobCreationException;

/**
 * Helper class to find a plug-in which provides a job type of which only the interface is known, and creates an instance of the job.
 * @author Moritz Lang
 * 
 */
@SuppressWarnings("deprecation")
class JobDefinitionProvider
{
	/**
	 * @throws RemoteException
	 */
	protected JobDefinitionProvider() throws RemoteException
	{
		super();
	}

	/**
	 * Creates a job which implements the given interface at the given well and position.
	 * @param <T> Interface of the job.
	 * @param jobInterface Interface of the job type which should be created.
	 * @param positionInformation logical position information of the job.
	 * @return Newly created job, or null.
	 * @throws JobCreationException
	 */
	public static <T extends Job> T createJob(Class<T> jobInterface, PositionInformation positionInformation) throws JobCreationException
	{
		checkJobInterface(jobInterface);

		// get implementing class
		Class<? extends T> factoryClass = getFirstValidImplementation(jobInterface);
		if(factoryClass == null)
			return null;

		// Create object
		// Get constructor
		Constructor<?> constructor = null;
		try
		{
			constructor = factoryClass.getConstructor(new Class<?>[] {PositionInformation.class});
			constructor.setAccessible(true);

		}
		catch(SecurityException e)
		{
			throw new JobCreationException("Security exception thrown while analyzing constructors of job implementation  (" + factoryClass.getName() + ").", e);
		}
		catch(NoSuchMethodException e)
		{
			throw new JobCreationException("Job implementation (" + factoryClass.getName() + ") has no valid constructor.", e);
		}

		Object[] constructorArgs = new Object[] {positionInformation};
		// Call constructor
		Object job;
		try
		{
			job = constructor.newInstance(constructorArgs);
		}
		catch(Exception e)
		{
			throw new JobCreationException("Could not instanciate job implementation  (" + factoryClass.getName() + ").", e);
		}

		try
		{
			return jobInterface.cast(job);
		}
		catch(Exception e)
		{
			throw new JobCreationException("Could not cast created job to the specified interface  (" + factoryClass.getName() + ").", e);
		}
	}

	/**
	 * Returns true if there is at least one implementation for the given job interface.
	 * @param jobInterface job interface to search implementations for.
	 * @return true if at least one implementation exists.
	 * @throws RemoteException
	 * @throws JobCreationException
	 */
	public static boolean hasJobImplementation(Class<? extends Job> jobInterface) throws RemoteException, JobCreationException
	{
		checkJobInterface(jobInterface);

		return getFirstValidImplementation(jobInterface) != null;
	}

	/**
	 * Returns the first implementing of a given job type.
	 * @param <T>
	 * @param callbackInterface
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static <T extends Job> Class<? extends T> getFirstValidImplementation(Class<T> jobInterface)
	{
		for(JobConstructionAddonFactory jobFactory : ServiceLoader.load(JobConstructionAddonFactory.class, JobDefinitionProvider.class.getClassLoader()))
		{
			Iterable<Class<? extends Job>> iterable = jobFactory.getJobImplementations();
			if(iterable == null)
				continue;

			for(Class<? extends Job> factoryClass : iterable)
			{
				if(!isValidJobImplementation(jobFactory, factoryClass))
					continue;
				if(supportsImplementationInterface(factoryClass, jobInterface))
				{
					return (Class<? extends T>)factoryClass;
				}
			}
		}
		return null;
	}

	/**
	 * Returns true if a given implementation supports an interface.
	 * @param factoryClass Implementation
	 * @param jobInterface Interface.
	 * @return
	 */
	private static boolean supportsImplementationInterface(Class<? extends Job> factoryClass, Class<? extends Job> jobInterface)
	{
		return jobInterface.isAssignableFrom(factoryClass);
	}

	/**
	 * Checks the factory class for validity and additionally notifies client if invalid.
	 * @param factory The factory.
	 * @param factoryClass The class to check.
	 * @return True if valid.
	 */
	private static boolean isValidJobImplementation(JobConstructionAddonFactory factory, Class<? extends Job> factoryClass)
	{
		if(factoryClass == null)
		{
			ServerSystem.err.println("Job factory " + factory.getClass().getName() + " returns a null pointer as a job.", null);
			return false;
		}

		if(factoryClass.isInterface())
		{
			ServerSystem.err.println("Job factory " + factory.getClass().getName() + " returns a job (" + factoryClass.getName() + ") which is an interface.", null);
			return false;
		}

		// Check if valid constructor Foo(PositionInformation positionInformation).
		try
		{
			factoryClass.getConstructor(new Class<?>[] {PositionInformation.class});
			return true;
		}
		catch(SecurityException e)
		{
			ServerSystem.err.println("Security exception thrown while analyzing job implementation  (" + factoryClass.getName() + ") of factory " + factory.getClass().getName() + ".", e);
			return false;
		}
		catch(NoSuchMethodException e)
		{
			ServerSystem.err.println("Job factory " + factory.getClass().getName() + " returns a job (" + factoryClass.getName() + ") which does not have an apropriate constructor (Foo(Well well, int[] positionInformation)).", e);
			return false;
		}
	}

	/**
	 * Checks if the given class is a valid job interface. Throws an error if not.
	 * @param jobInterface The interface to check.
	 * @throws JobCreationException
	 */
	private static void checkJobInterface(Class<? extends Job> jobInterface) throws JobCreationException
	{
		if(jobInterface == null)
			throw new JobCreationException("Job interface must not be null.");

		if(!jobInterface.isInterface())
			throw new JobCreationException("Interface definition is not an interface.");

	}
}
