/**
 * 
 */
package org.youscope.common.table;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;


/**
 * A table data listener which stores the table data it receives, allowing to access it later on.
 * @author Moritz Lang
 * 
 */
public class TableDataAdapter extends UnicastRemoteObject implements TableListener
{
	private Table table = null;

	/**
	 * SerializableVersion UID.
	 */
	private static final long		serialVersionUID	= 5430798742199344228L;

	/**
	 * Constructor.
	 * @throws RemoteException
	 */
	public TableDataAdapter() throws RemoteException
	{
		super();
	}

	/**
	 * Returns the last table the adapter received, or null if yet not received any table or if the adapter was cleared (see {@link #clear()}).
	 * @return last received table, or null
	 */
	public Table getTable()
	{
		return table;
	}

	/**
	 * Clears any currently available table from this adapter. If a table was available, returns the table.
	 * @return last received table, or null.
	 */
	public Table clear()
	{
		Table table = this.table;
		this.table = null;
		return table;
	}

	@Override
	public void newTableProduced(Table table)
	{
		this.table = table;
	}
}
