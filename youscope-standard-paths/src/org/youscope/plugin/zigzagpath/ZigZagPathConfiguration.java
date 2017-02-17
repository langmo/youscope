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
package org.youscope.plugin.zigzagpath;

import org.youscope.addon.pathoptimizer.PathOptimizerConfiguration;
import org.youscope.common.configuration.YSConfigAlias;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Zig-Zag path through microplate
 * @author Moritz Lang
 *
 */
@YSConfigAlias("Zig-Zag Path")
@XStreamAlias("zig-zag-path")
public class ZigZagPathConfiguration extends PathOptimizerConfiguration
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 3051118882205729335L;
	/**
	 * Type identifier.
	 */
	public static final String TYPE_IDENTIFIER = "YouScope.path.ZigZagPath";

	@Override
	public String getTypeIdentifier() {
		return TYPE_IDENTIFIER;
	}

	/**
	 * Direction of the zig-zag path
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
	 * Returns if the zig-zag is done vertically or horizontally.
	 * @return direction of zig-zag.
	 */
	public Direction getDirection() {
		return direction;
	}

	/**
	 * Sets if the zig-zag is done vertically or horizontally.
	 * @param direction direction of zig-zag.
	 */
	public void setDirection(Direction direction) {
		this.direction = direction;
	}


}
