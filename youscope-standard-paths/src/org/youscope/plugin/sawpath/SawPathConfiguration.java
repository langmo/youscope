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
package org.youscope.plugin.sawpath;

import org.youscope.addon.pathoptimizer.PathOptimizerConfiguration;
import org.youscope.common.configuration.YSConfigAlias;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Non-optimized path through microplate
 * @author Moritz Lang
 *
 */
@YSConfigAlias("Saw Path")
@XStreamAlias("saw-path")
public class SawPathConfiguration extends PathOptimizerConfiguration 
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 3041118882205729335L;
	/**
	 * Type identifier.
	 */
	public static final String TYPE_IDENTIFIER = "YouScope.path.SawPath";
	
	/**
	 * Direction of the saw path
	 * @author Moritz Lang
	 *
	 */
	public enum Direction
	{
		/**
		 * Horizontal direction.
		 */
		HORIZONTALLY("Horizontally"),
		/**
		 * Vertical direction.
		 */
		VERTICALLY("Vertically");
		
		private final String description;
		Direction(String description)
		{
			this.description = description;
		}
		@Override
		public String toString()
		{
			return description;
		}
	}
	@XStreamAlias("direction")
	@YSConfigAlias("direction")
	private Direction direction = Direction.HORIZONTALLY;

	/**
	 * Returns if the saw is done vertically or horizontally.
	 * @return direction of saw.
	 */
	public Direction getDirection() {
		return direction;
	}

	/**
	 * Sets if the saw is done vertically or horizontally.
	 * @param direction direction of saw.
	 */
	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	@Override
	public String getTypeIdentifier() {
		return TYPE_IDENTIFIER;
	}

	

}
