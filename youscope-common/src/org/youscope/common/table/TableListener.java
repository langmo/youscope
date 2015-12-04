/**
 * 
 */
package org.youscope.common.table;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.EventListener;

import org.youscope.common.measurement.job.Job;

/**
 * Listener which can be registered at a table producing measurement component to get informed when new table data was produced.
 * See {@link Table} for more information about the produced table.
 * @author Moritz Lang
 * 
 */
public interface TableListener extends Remote, EventListener
{
	/**
	 * Gets called when a new table was produced by the table producer. For measurement components (e.g. {@link Job}), this function
	 * should be called for every listener exactly once per evaluation.
	 * This includes calling this function even with a Table containing no rows.
	 * @param table The produced table.
	 * @throws RemoteException
	 */
	public void newTableProduced(Table table) throws RemoteException;
}
