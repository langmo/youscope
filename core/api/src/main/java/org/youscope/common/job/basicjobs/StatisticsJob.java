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
/**
 * 
 */
package org.youscope.common.job.basicjobs;

import org.youscope.common.job.CompositeJob;
import org.youscope.common.job.Job;
import org.youscope.common.table.TableProducer;

/**
 * Interface of a measurement job which collects statistical data about the evaluation and appends it to a file.
 * @author Moritz Lang
 * 
 */
public interface StatisticsJob extends Job, CompositeJob, TableProducer
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
	public static final String	DEFAULT_TYPE_IDENTIFIER	= "YouScope.StatisticsJob";
}
