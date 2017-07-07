package org.youscope.plugin.cellx;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.youscope.addon.celldetection.CellDetectionException;

/**
 * Interface for helper class with which CellX saves cell detection result in Matlab.
 * @author Moritz Lang
 *
 */
public interface CellXSink extends Remote 
{
	/**
	 * Adds a cell to the cell detection table.
	 * @param headers Header of the columns
	 * @param data data of the columns. Must be one row/cell only.
	 * @throws RemoteException
	 * @throws CellDetectionException
	 */
	void addCell(String[] headers, double[] data) throws RemoteException, CellDetectionException;

}
