/**
 * 
 */
package org.youscope.common.job;

import org.youscope.common.configuration.Configuration;

/**
 * The interface all job configurations have to implement.
 * 
 * @author Moritz Lang
 */
public interface JobConfiguration extends Configuration
{
	/**
	 * Returns a short description of this job.
	 * 
	 * @return Short description of the job.
	 */
	public String getDescription();
}
