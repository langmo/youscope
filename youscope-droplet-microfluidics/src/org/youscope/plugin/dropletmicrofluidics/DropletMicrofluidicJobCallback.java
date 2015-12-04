/**
 * 
 */
package org.youscope.plugin.dropletmicrofluidics;

import java.rmi.RemoteException;

import org.youscope.common.measurement.ExecutionInformation;
import org.youscope.common.measurement.callback.Callback;
import org.youscope.common.measurement.callback.CallbackException;
import org.youscope.common.table.Table;

/**
 * Callback providing an UI for droplet based microfluidic measurements.
 * 
 * @author Moritz Lang
 */
public interface DropletMicrofluidicJobCallback extends Callback
{
	
	/**
	 * Type identifier for callback.
	 */
	public static final String	TYPE_IDENTIFIER	= "IST::DropletBasedMicrofluidicsJob::Callback";
    /**
     * Sends the table produced by a droplet based microfluidic job (see {@link DropletMicrofluidicTable#getTableDefinition()} to the callback for visualization.
     * @param executionInformation Execution information of droplet based microfluidics job.
     * @param table table containing droplet data.
     * @throws RemoteException
     * @throws CallbackException 
     */
    public void dropletMeasured(ExecutionInformation executionInformation, Table table) throws RemoteException, CallbackException;
}
