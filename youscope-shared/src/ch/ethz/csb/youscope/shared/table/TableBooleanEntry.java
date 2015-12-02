package ch.ethz.csb.youscope.shared.table;

/**
 * A table entry containing a boolean.
 * @author Moritz Lang
 *
 */
class TableBooleanEntry extends TableEntryAdapter<Boolean> 
{

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -381739064279374990L;

	public TableBooleanEntry(Boolean value, boolean nullAllowed) throws NullPointerException, TableException 
	{
		super(value, nullAllowed, Boolean.class);
	}

	@Override
	protected Boolean cloneValue(Boolean value) 
	{
		// Booleans are immutable
		return value;
	}
	
	@Override
	public String getValueAsString(Boolean value) 
	{
		return value.toString();
	}

}
