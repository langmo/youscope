/**
 * 
 */
package ch.ethz.csb.youscope.server.addon.job;

import ch.ethz.csb.youscope.server.addon.ConstructionAddonFactory;
import ch.ethz.csb.youscope.shared.measurement.job.Job;

/**
 * @author Moritz Lang
 * @deprecated Use {@link ConstructionAddonFactory} instead.
 */
@Deprecated
public interface JobConstructionAddonFactory
{
	/**
	 * Returns a new job construction addon for the given ID, or null if addon does not support the construction of jobs with the given ID..
	 * @param ID The ID for which a job should be created.
	 * 
	 * @return New addon for the construction of jobs being compatible with this addon.
	 */
	JobConstructionAddon createJobConstructionAddon(String ID);

	/**
	 * Returns a list of all job configuration types supported by this addon.
	 * 
	 * @return List of supported configurations.
	 */
	String[] getSupportedConfigurationIDs();

	/**
	 * Returns true if this addon supports job configurations with the given ID, false otherwise.
	 * @param ID The ID of the job configuration for which it should be queried if this addon supports its construction.
	 * @return True if this addon supports job configurations with the given ID, false otherwise.
	 */
	boolean supportsConfigurationID(String ID);
	
	/**
     * Returns an iterator of all job implementations available in this addon factory. Each returned class must have a two argument constructor, with the first argument being of type Well
     * and the second an array of integers, indicating the position (e.g. tile) in the well (e.g. Foo(Well well, int[] positionInformation)).
     * Furthermore. it must be remote transferable (e.g. extends UnicastRemoteObject).
     * The such exposed classes are made available for direct construction (i.e. without a JobConfigurationDTO) and subsequent "manual" configuration.
     * Thus, all exposed implementations should be possible to configure by appropriate getters and setters. 
     * YouScope automatically detects all job interfaces implemented by the returned classes, and may or may not decide to create an instance of the class
     * if asked for a specific job type.
     * 
     * Note: not the interface of the job, but its implementation should be returned. Wrongly defined implementation might generate a warning in YouScope's log.
     * 
     * @return Iterator over classes implementing job interfaces.
     */
    public Iterable<Class<? extends Job>> getJobImplementations();
}
