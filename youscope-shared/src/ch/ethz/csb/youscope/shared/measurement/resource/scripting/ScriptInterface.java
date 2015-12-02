/**
 * 
 */
package ch.ethz.csb.youscope.shared.measurement.resource.scripting;

import java.rmi.Remote;
import java.rmi.RemoteException;

import ch.ethz.csb.youscope.shared.measurement.ImageProducer;
import ch.ethz.csb.youscope.shared.measurement.job.Job;
import ch.ethz.csb.youscope.shared.measurement.job.basicjobs.ScriptingJob;

/**
 * @author langmo
 */
public interface ScriptInterface extends Remote
{
	/**
	 * Starts the measurement job.
	 * 
	 * @param job Job to start.
	 * @throws RemoteException
	 * @throws MicroscopeScriptException
	 */
	public void startJob(Job job) throws RemoteException, MicroscopeScriptException;

	/**
	 * Prints the string in the client console.
	 * 
	 * @param text Text to print.
	 * @throws RemoteException
	 */
	public void println(String text) throws RemoteException;

	/**
	 * Creates a new storage class which stores images from an imaging job such that the script can
	 * get the image data.
	 * 
	 * @param job The image producing job.
	 * @return Image storage class.
	 * @throws RemoteException
	 */
	public ScriptImageStorage createImageStorage(ImageProducer job) throws RemoteException;

	/**
	 * Returns the currently executed scripting job.
	 * 
	 * @return Currently executed scripting job.
	 * @throws RemoteException
	 */
	public ScriptingJob getScriptingJob() throws RemoteException;
}
