/**
 * 
 */
package ch.ethz.csb.youscope.server;

import java.awt.Dimension;
import java.rmi.RemoteException;

import ch.ethz.csb.youscope.server.microscopeaccess.CameraDeviceInternal;
import ch.ethz.csb.youscope.server.microscopeaccess.ChannelInternal;
import ch.ethz.csb.youscope.shared.ImageEvent;
import ch.ethz.csb.youscope.shared.ImageListener;
import ch.ethz.csb.youscope.shared.microscope.CameraDevice;
import ch.ethz.csb.youscope.shared.microscope.DeviceException;
import ch.ethz.csb.youscope.shared.microscope.MicroscopeException;
import ch.ethz.csb.youscope.shared.microscope.MicroscopeLockedException;
import ch.ethz.csb.youscope.shared.microscope.SettingException;

/**
 * RMI interface for camera access.
 * @author Moritz Lang
 */
class CameraDeviceRMI extends DeviceRMI implements CameraDevice
{
	/**
	 * Serial Version UID.
	 */
	private static final long			serialVersionUID	= -6553028247559261711L;

	private final CameraDeviceInternal	camera;
	private final ChannelManagerImpl	channelManager;

	/**
	 * Constructor.
	 * @throws RemoteException
	 */
	protected CameraDeviceRMI(CameraDeviceInternal camera, ChannelManagerImpl channelManager, int accessID) throws RemoteException
	{
		super(camera, accessID);
		this.camera = camera;
		this.channelManager = channelManager;
	}

	@Override
	public ImageEvent makeImage(String channelGroupID, String channelID, double exposure) throws MicroscopeException, MicroscopeLockedException, InterruptedException, SettingException
	{
		ChannelInternal channel = channelManager.getChannel(channelGroupID, channelID);
		return camera.makeImage(channel, exposure, accessID);
	}

	@Override
	public void setExposure(double exposure) throws MicroscopeLockedException, MicroscopeException, InterruptedException
	{
		camera.setExposure(exposure, accessID);

	}

	@Override
	public void stopContinuousSequenceAcquisition() throws MicroscopeException, MicroscopeLockedException, InterruptedException, SettingException, DeviceException
	{
		camera.stopContinuousSequenceAcquisition(accessID);
	}

	@Override
	public void setTransposeX(boolean transpose) throws MicroscopeLockedException
	{
		camera.setTransposeX(transpose, accessID);
	}

	@Override
	public void setTransposeY(boolean transpose) throws MicroscopeLockedException
	{
		camera.setTransposeY(transpose, accessID);
	}

	@Override
	public void setSwitchXY(boolean switchXY) throws MicroscopeLockedException
	{
		camera.setSwitchXY(switchXY, accessID);
	}

	@Override
	public boolean isTransposeX()
	{
		return camera.isTransposeX();
	}

	@Override
	public boolean isTransposeY()
	{
		return camera.isTransposeY();
	}

	@Override
	public boolean isSwitchXY()
	{
		return camera.isSwitchXY();
	}

	@Override
	public Dimension getImageSize() throws MicroscopeLockedException, MicroscopeException, RemoteException
	{
		return camera.getImageSize(accessID);
	}

	@Override
	public void startContinuousSequenceAcquisition(String channelGroupID, String channelID, double exposure, ImageListener listener) throws MicroscopeException, RemoteException, MicroscopeLockedException, InterruptedException, SettingException, DeviceException
	{
		ChannelInternal channel = channelManager.getChannel(channelGroupID, channelID);
		camera.startContinuousSequenceAcquisition(channel, exposure, listener, accessID);
	}

	@Override
	@Deprecated
	public ImageEvent[] makeParallelImages(String channelGroupID, String channelID, String[] cameraIDs, double[] exposures) throws MicroscopeException, MicroscopeLockedException, InterruptedException, SettingException, DeviceException, RemoteException
	{
		ChannelInternal channel = channelManager.getChannel(channelGroupID, channelID);
		return camera.makeParallelImages(channel, cameraIDs, exposures, accessID);
	}
}
