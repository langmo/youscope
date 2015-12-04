package org.youscope.plugin.quickdetect;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface for Matlab.
 * @author Moritz Lang
 *
 */
public interface TableSink extends Remote 
{
	/**
	 * @param cellID
	 * @param quantID
	 * @param xpos
	 * @param ypos
	 * @param area
	 * @param fluorescence
	 * @throws RemoteException
	 * @throws Exception 
	 */
	public void addRow(Integer cellID, Integer quantID, Double xpos, Double ypos, Double area, Double fluorescence) throws RemoteException, Exception;
}
