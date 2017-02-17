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
package org.youscope.common;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Every job has a certain logical position where it gets evaluated, e.g. in a certain well, at a certain tile, in a certain focus position, being part of a focus stack.
 * This class provides information about this position.
 * The class is immutable.
 * @author Moritz Lang
 * 
 */
public final class PositionInformation implements Serializable, Comparable<PositionInformation>
{
	/**
	 * Serial version UID.
	 */
	private static final long	serialVersionUID			= 7182040397567776618L;
	private final Well			well;
	private final int[]			positions;
	private final String[]		positionTypes;

	/**
	 * Position type representing the index of a tile in the y-direction
	 */
	public static final String	POSITION_TYPE_YTILE			= "y-tile";

	/**
	 * Position type for non-microplate measurements, which nevertheless have a main counter of the positions, which might be however arbitrary.
	 */
	public static final String	POSITION_TYPE_MAIN_POSITION	= "position";

	/**
	 * Position type representing the index of a tile in the x-direction
	 */
	public static final String	POSITION_TYPE_XTILE			= "x-tile";

	/**
	 * Position type representing the index of a job in a focus/z- stack.
	 */
	public static final String	POSITION_TYPE_ZSTACK		= "z-stack";

	/**
	 * Position type for multi-camera imaging. If set, the position corresponds to the index of the camera.
	 */
	public static final String	POSITION_TYPE_CAMERA		= "camera";

	/**
	 * Creates a new position information, representing a given position in a well. No other position information is added.
	 * Should only be created by a measurement construction addon, since the measurement is the root of the measurement hierarchy. 
	 * Jobs construction addons and similar should always use <code>PositionInformation(PositionInformation parentInformation, String positionType, int position)</code> 
	 * to add additional position information to sub-jobs, but keeping the information upper the hierarchy.
	 * @param well The well where the job gets evaluated.
	 */
	public PositionInformation(Well well)
	{
		this.well = well;
		this.positions = new int[0];
		this.positionTypes = new String[0];
	}
	
	/**
	 * Creates a new position information, representing the root of a position hierarchy. No position information is added.
	 * Should only be created by a measurement construction addon, since the measurement is the root of the measurement hierarchy. 
	 * Jobs construction addons and similar should always use <code>PositionInformation(PositionInformation parentInformation, String positionType, int position)</code> 
	 * to add additional position information to sub-jobs, but keeping the information upper the hierarchy. 
	 */
	public PositionInformation()
	{
		this.well = null;
		this.positions = new int[0];
		this.positionTypes = new String[0];
	}

	/**
	 * Creates a new position information out of a parent position information. All information from the parent is copied, and a
	 * sub-position information is added at the tail. This constructor is typically used by a job creation addon which creates
	 * a set of jobs several times, e.g. always the same jobs, but at different focus positions (z-stack), or always the same jobs at
	 * different stage positions (tiles).
	 * @param parentInformation The position information which is copied by this information. Must not be null.
	 * @param positionType The type of the position. Use constants defined in this class when possible, and otherwise strings with a length of at least 3 characters.
	 * @param position The position in the position type, starting at 0.
	 */
	public PositionInformation(PositionInformation parentInformation, String positionType, int position)
	{
		if(parentInformation == null)
			throw new IllegalArgumentException("Parent position information must not be null.");
		if(positionType == null || positionType.length() < 3)
			throw new IllegalArgumentException("Position type must be not null and have a length of at least 3 characters.");
		well = parentInformation.well;

		positions = new int[parentInformation.getNumPositions() + 1];
		System.arraycopy(parentInformation.getPositions(), 0, positions, 0, parentInformation.getNumPositions());
		positions[positions.length - 1] = position;

		positionTypes = new String[parentInformation.getNumPositions() + 1];
		System.arraycopy(parentInformation.getPositionTypes(), 0, positionTypes, 0, parentInformation.getNumPositions());
		positionTypes[positions.length - 1] = positionType;
	}
	
	/**
	 * Creates a new position information with well set to null and the given position as only configured position.
	 * @param positionType The type of the position. Use constants defined in this class when possible, and otherwise strings with a length of at least 3 characters.
	 * @param position The position in the position type, starting at 0.
	 */
	public PositionInformation(String positionType, int position)
	{
		if(positionType == null || positionType.length() < 3)
			throw new IllegalArgumentException("Position type must be not null and have a length of at least 3 characters.");
		well = null;

		positions = new int[]{position};
		positionTypes = new String[]{positionType};
	}

	/**
	 * Returns all logical positions the job is in as an array, where the first element represents the outermost and the last the innermost position.
	 * @return Position array. Might be an empty array.
	 */
	public int[] getPositions()
	{
		int[] returnVal = new int[positions.length];
		System.arraycopy(positions, 0, returnVal, 0, positions.length);
		return returnVal;
	}

	/**
	 * Returns a string with the position informations, separated by dashes.
	 * Note that in the return string the numbering of positions starts at one, different to the
	 * internal variables which start at zero. If the position is empty, returns an empty string.
	 * @return String indicating the positions, separated by dashes, counting from one upwards.
	 */
	public String getPositionsString()
	{
		String returnVal = "";
		for(int j = 0; j < positions.length; j++)
		{
			if(j > 0)
				returnVal += ", ";
			returnVal += getPositionString(j);
		}
		return returnVal;
	}

	/**
	 * Returns the types of positioning the logical positions of the job describe as an array, where the first element represents the outermost and the last the innermost position.
	 * @return Position type array. Might be an empty array.
	 */
	public String[] getPositionTypes()
	{
		String[] returnVal = new String[positionTypes.length];
		System.arraycopy(positionTypes, 0, returnVal, 0, positionTypes.length);
		return returnVal;
	}
	
	@Override
	public String toString()
	{
		String returnVal = "";
		if(well!= null)
		{
			returnVal+="Well "+well.toString();
			if(getNumPositions()>0)
				returnVal+=", ";
		}
		returnVal+=getPositionsString();
		return returnVal;
	}

	/**
	 * Returns the well the job is executed in. Might be null.
	 * @return well of job.
	 */
	public Well getWell()
	{
		return well;
	}

	/**
	 * Returns the number of logical position types the job is executed in.
	 * @return Number of positions.
	 */
	public int getNumPositions()
	{
		return positions.length;
	}

	/**
	 * Returns the n-th logical position the job is executed in.
	 * @param idx index of the position.
	 * @return n-th position. Each position index is starting at 0.
	 */
	public int getPosition(int idx)
	{
		return positions[idx];
	}

	/**
	 * Returns the n-th logical position type the job is executed in.
	 * @param idx index of the position type.
	 * @return n-th position type.
	 */
	public String getPositionType(int idx)
	{
		return positionTypes[idx];
	}

	/**
	 * Returns a string representation composed of the position type and the position.
	 * Note that since this function is meant to generate output to the user, the position index is here starting at 1 (i.e. the index in the string is
	 * one higher than in getPosition(idx).
	 * @param idx index of the position for which a string representation should be extracted.
	 * @return String representation of position type and position.
	 */
	public String getPositionString(int idx)
	{
		return getPositionType(idx) + ": " + Integer.toString(positions[idx] + 1);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(positionTypes);
		result = prime * result + Arrays.hashCode(positions);
		result = prime * result + ((well == null) ? 0 : well.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PositionInformation other = (PositionInformation) obj;
		if (!Arrays.equals(positionTypes, other.positionTypes))
			return false;
		if (!Arrays.equals(positions, other.positions))
			return false;
		if (well == null) {
			if (other.well != null)
				return false;
		} else if (!well.equals(other.well))
			return false;
		return true;
	}
	
	@Override
	public int compareTo(PositionInformation o) {
		if(o==null)
			return -1;
		if(well == null && o.well != null)
			return -1;
		else if (well != null && o.well == null)
			return 1;
		else if(well != null && o.well != null)
		{
			int compare = well.compareTo(o.well);
			if(compare != 0)
				return compare;
		}
		for(int i=0; i<positions.length && i < o.positions.length; i++)
		{
			if(positions[i]!=o.positions[i])
				return positions[i] < o.positions[i] ? -1 : 1;
		}
		if(positions.length < o.positions.length)
			return -1;
		else if(positions.length > o.positions.length)
			return 1;
		return 0;
	}
}
