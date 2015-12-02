/**
 * 
 */
package ch.ethz.csb.youscope.shared.measurement.job.basicjobs;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.shared.measurement.MeasurementRunningException;
import ch.ethz.csb.youscope.shared.measurement.job.Job;

/**
 * A job to change the stage's position.
 * @author Moritz Lang
 */
public interface ChangePositionJob extends Job
{
	/**
	 * The type identifier of the default implementation of this job. 
	 * Basic jobs are considered such essential to YouScope
	 * such that their interfaces are made part of the shared library. However, their implementation are not, and there
	 * might be several addons providing (different) implementations of this job. Most of these implementations, however, are specific
	 * for a given application and not general. The addon exposing this identifier should be general, that is, every other
	 * part of YouScope accessing this job over the default identifier is expecting the job to behave in the general way.
	 * Only one implementation (addon) should expose the default identifier. Typically, this implementation is already part of YouScope,
	 * such that implementing this addon is not necessary. However, there might be cases when the default implementation should be overwritten,
	 * which is why the interface, but not the implementation is part of YouScope's core elements. In this case, the default implementation
	 * already part of YouScope should be removed (i.e. the corresponding default plug-in deleted).
	 * 
	 */
	public static final String	DEFAULT_TYPE_IDENTIFIER	= "CSB::ChangePositionJob";
	/**
	 * Returns the x position information.
	 * 
	 * @return X position information in micro meter.
	 * @throws RemoteException
	 */
	double getX() throws RemoteException;

	/**
	 * Returns the y position information.
	 * 
	 * @return Y position information in micro meter.
	 * @throws RemoteException
	 */
	double getY() throws RemoteException;

	/**
	 * Returns TRUE if position is changed absolutely and false, if relative to current position.
	 * 
	 * @return TRUE if absolute position.
	 * @throws RemoteException
	 */
	boolean isAbsolute() throws RemoteException;

	/**
	 * Sets the position where the microscope should move to.
	 * 
	 * @param x X-position in micro meter.
	 * @param y Y-position in micro meter.
	 * @throws RemoteException
	 * @throws MeasurementRunningException
	 */
	void setPosition(double x, double y) throws RemoteException, MeasurementRunningException;

	/**
	 * Sets the relative distance for which the microscope should move, starting at the current position.
	 * @param dx the distance in the x-direction, in micro meter.
	 * @param dy the distance in the y-direction, in micro meter.
	 * @throws RemoteException
	 * @throws MeasurementRunningException
	 */
	void setRelativePosition(double dx, double dy) throws RemoteException, MeasurementRunningException;

	/**
	 * Returns the ID of the stage device whose position should be changed.
	 * @return ID of the stage device, or null, if default stage device should be used.
	 * @throws RemoteException
	 */
	String getStageDevice() throws RemoteException;

	/**
	 * Sets the ID of the stage device whose position should be changed.
	 * @param deviceID ID of the stage device, or null, if default stage device should be used.
	 * @throws RemoteException
	 * @throws MeasurementRunningException
	 */
	void setStageDevice(String deviceID) throws RemoteException, MeasurementRunningException;
}
