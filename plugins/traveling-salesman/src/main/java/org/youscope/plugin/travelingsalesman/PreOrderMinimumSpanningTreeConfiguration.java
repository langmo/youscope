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
package org.youscope.plugin.travelingsalesman;

import org.youscope.addon.pathoptimizer.PathOptimizerConfiguration;
import org.youscope.common.configuration.YSConfigAlias;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Pre-order minimum spanning tree configuration
 * @author Moritz Lang
 *
 */
@YSConfigAlias("Pre-order search of minimum spanning tree (2-approximation)")
@XStreamAlias("pre-order-minimum-spanning-tree")
public class PreOrderMinimumSpanningTreeConfiguration extends PathOptimizerConfiguration
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 3032928882205729335L;
	/**
	 * Type identifier.
	 */
	public static final String TYPE_IDENTIFIER = "YouScope.path.PreOrderMinimumSpanningTree";

	@XStreamAlias("metric")
	private MetricType metric = MetricType.MANHATTEN;

	/**
	 * Sets the metric to use to calculate distances.
	 * @param metric metric to use.
	 */
	public void setMetric(MetricType metric) {
		this.metric = metric;
	}

	/**
	 * Returns the metric used to calculate distances.
	 * @return used metric.
	 */
	public MetricType getMetric() {
		return metric;
	}
	
	@Override
	public String getTypeIdentifier() {
		return TYPE_IDENTIFIER;
	}

	

}
