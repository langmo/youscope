package org.youscope.plugin.travelingsalesman.blossom;

/**
 * Type of primal update performed by the blossom algorithm.
 * @author Moritz Lang
 *
 */
public enum PrimalUpdateType
{
	/**
	 * Current tree was grown by two free nodes.
	 */
	GROW,
	/**
	 * Two trees were joined to a graph. Subsequently, graph was splitted into pairs of free nodes.
	 */
	AUGMENT,
	/**
	 * A cycle was shrinked into a blossom.
	 */
	SHRINK,
	/**
	 * A blossom was expanded.
	 */
	EXPAND,
	/**
	 * The algorithm has finished.
	 */
	FINISHED,
	/**
	 * No update could be performed.
	 */
	NONE;
	
	@Override
	public String toString()
	{
		switch(this)
		{
		case GROW:
			return "grow";
		case AUGMENT:
			return "augment";
		case SHRINK:
			return "shrink";
		case EXPAND:
			return "expand";
		case FINISHED:
			return "finished";
		case NONE:
			return "none";
		default:
			return "unknown: " + super.toString();
		}
	}
}