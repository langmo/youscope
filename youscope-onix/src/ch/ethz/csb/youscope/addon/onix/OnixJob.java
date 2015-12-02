/**
 * 
 */
package ch.ethz.csb.youscope.addon.onix;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.shared.measurement.MeasurementRunningException;
import ch.ethz.csb.youscope.shared.measurement.job.Job;
import ch.ethz.csb.youscope.shared.table.TableConsumer;

/**
 * A job to control the CellAsic Onix microfluidic system.
 * @author Moritz Lang
 */
public interface OnixJob extends Job, TableConsumer
{
	/**
	 * Sets the protocol which gets evaluated on the onix device every time the job gets evaluated.
	 * @param onixProtocol The onix protocol.
	 * @throws RemoteException 
	 * @throws MeasurementRunningException 
	 */
	public void setOnixProtocol(String onixProtocol) throws RemoteException, MeasurementRunningException;

	/**
	 * Returns the onix protocol.
	 * @return Onix protocol.
	 * @throws RemoteException 
	 */
	public String getOnixProtocol() throws RemoteException;

	/**
	 * If true, the job waits when evaluating the onix protocol until the protocol is finished. If false, the onix protocol gets evaluated in parallel.
	 * @param waitUntilFinished True if job should wait until end of onix protocol evaluation.
	 * @throws RemoteException 
	 * @throws MeasurementRunningException 
	 */
	public void setWaitUntilFinished(boolean waitUntilFinished) throws RemoteException, MeasurementRunningException;

	/**
	 * If true, the job waits when evaluating the onix protocol until the protocol is finished. If false, the onix protocol gets evaluated in parallel.
	 * @return True if job should wait until end of onix protocol evaluation.
	 * @throws RemoteException 
	 */
	public boolean isWaitUntilFinished() throws RemoteException;
}
