package org.youscope.common.table;

/**
 * A table entry containing a double.
 * @author Moritz Lang
 *
 */
class TableDoubleEntry extends TableEntryAdapter<Double>  
{

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -5118391925520387931L;
	
	public TableDoubleEntry(Double value, boolean nullAllowed) throws NullPointerException, TableException 
	{
		super(value, nullAllowed, Double.class);
	}

	@Override
	String getValueAsString(Double value) 
	{
		return value.toString();
	}

	@Override
	protected Double cloneValue(Double value) 
	{
		// Double's are immutable
		return value;
	}
	
}
