/**
 * 
 */
package ch.ethz.csb.youscope.shared.measurement.job.basicjobs;

import ch.ethz.csb.youscope.shared.measurement.job.EditableJobContainer;
import ch.ethz.csb.youscope.shared.measurement.job.Job;


/**
 * A job which consists of other jobs. The single jobs will be run after each other in the order
 * they are created. It is guaranteed that in between two jobs no other tasks are executed by the
 * microscope.
 * 
 * @author Moritz Lang
 */
public interface CompositeJob extends Job, EditableJobContainer
{
	/**
	 * The type identifier of the default implementation of this job. 
	 * Basic jobs are considered such essential to YouScope
	 * such that their interfaces are made part of the shared library. However, their implementation are not, and there
	 * might be several addons providing (different) implementations of this job. Most of these implementations, however, are specific
	 * for a given application and not general. The addon exposing this identifier should be general, that is, every other
	 * part of YouScope accessing this job over the default identifier is expecting the job to behave in the general way.
	 * Only one implementation (addon) should expose the default identifier. Typically, this implementation is already part of YouScope,
	 * such that implementing this addon is not necessary. However, there might be cases when the default implementation should be overwritten,
	 * which is why the interface, but not the implementation is part of YouScope's core elements. In this case, the default implementation
	 * already part of YouScope should be removed (i.e. the corresponding default plug-in deleted).
	 * 
	 */
	public static final String	DEFAULT_TYPE_IDENTIFIER	= "CSB::CompositeJob";
}
