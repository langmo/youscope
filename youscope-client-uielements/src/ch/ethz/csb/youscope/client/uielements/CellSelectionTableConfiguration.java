/**
 * 
 */
package ch.ethz.csb.youscope.client.uielements;

/**
 * @author langmo
 * 
 */
public interface CellSelectionTableConfiguration
{
	/**
	 * True if the names of the rows should be displayed.
	 * @return Boolean indicating if rows should be labeled.
	 */
	public boolean isRowNamesDisplayed();

	/**
	 * True if the names of the columns should be displayed.
	 * @return Boolean indicating if columns should be labeled.
	 */
	public boolean isColumnNamesDisplayed();

	/**
	 * Returns the name of the row with the given index.
	 * @param index The index of the row.
	 * @return Name of the row.
	 */
	public String getRowName(int index);

	/**
	 * Returns the name of the column with the given index.
	 * @param index The index of the column.
	 * @return Name of the column.
	 */
	public String getColumnName(int index);
}
