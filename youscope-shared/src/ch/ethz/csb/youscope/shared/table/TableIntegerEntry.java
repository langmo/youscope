package ch.ethz.csb.youscope.shared.table;

/**
 * A table entry containing an integer.
 * @author Moritz Lang
 *
 */
class TableIntegerEntry extends TableEntryAdapter<Integer> 
{

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -5198391925520387931L;

	public TableIntegerEntry(Integer value, boolean nullAllowed) throws NullPointerException, TableException 
	{
		super(value, nullAllowed, Integer.class);
	}

	@Override
	protected Integer cloneValue(Integer value) 
	{
		// Integer's are immutable
		return value;
	}
	
	@Override
	public String getValueAsString(Integer value) 
	{
		return value.toString();
	}
	
}
