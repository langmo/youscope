package org.youscope.common.table;

/**
 * A table entry containing a string.
 * @author Moritz Lang
 *
 */
class TableStringEntry extends TableEntryAdapter<String> 
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -6259177334525937339L;

	public TableStringEntry(String value, boolean nullAllowed) throws NullPointerException, TableException 
	{
		super(value, nullAllowed, String.class);
	}

	@Override
	protected String cloneValue(String value) 
	{
		// Strings are immutable.
		return value;
	}

	@Override
	String getValueAsString(String value) 
	{
		return value;
	}
}
