package org.youscope.common.table;

/**
 * A table entry containing a float.
 * @author Moritz Lang
 *
 */
class TableFloatEntry extends TableEntryAdapter<Float> 
{

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -381739064279371190L;

	public TableFloatEntry(Float value, boolean nullAllowed) throws NullPointerException, TableException 
	{
		super(value, nullAllowed, Float.class);
	}

	@Override
	protected Float cloneValue(Float value) 
	{
		// Floats are immutable
		return value;
	}
	
	@Override
	public String getValueAsString(Float value) 
	{
		return value.toString();
	}

}
