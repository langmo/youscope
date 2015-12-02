/**
 * 
 */
package ch.ethz.csb.youscope.shared.configuration;

/**
 * Interface each job configuration should implement which contains sub-jobs.
 * By implementing this interface one accepts the contract that each job created by the respective configuration also implements the JobContainer interface.
 * 
 * Implementing this interface allows for configuration validity checks and enables to use several UI elements.
 * @author Moritz Lang
 */
public interface JobContainerConfiguration
{
	/**
	 * Returns the job list which might be edited by the caller.
	 * 
	 * @return Job list.
	 */
	public JobConfiguration[] getJobs();

	/**
	 * Sets the job list to the given list.
	 * 
	 * @param jobs New job list.
	 */
	public void setJobs(JobConfiguration[] jobs);

	/**
	 * Removes the job at the given index.
	 * 
	 * @param index Index of the job to be removed.
	 */
	public void removeJobAt(int index);

	/**
	 * Adds a job to the job list.
	 * 
	 * @param job Job to be added.
	 */
	public void addJob(JobConfiguration job);

	/**
	 * Adds a job to the job list. The job is added such that it has the given index. Jobs having a
	 * higher or equal index before the operation are moved downwards.
	 * 
	 * @param job Job to be added.
	 * @param index Index where the job should be added.
	 */
	public void addJob(JobConfiguration job, int index);

	/**
	 * Removes all jobs from the job list.
	 */
	public void clearJobs();
}
