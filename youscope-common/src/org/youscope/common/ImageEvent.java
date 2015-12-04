/**
 * 
 */
package org.youscope.common;

import java.io.Serializable;
import java.util.Date;

import org.youscope.common.measurement.ExecutionInformation;
import org.youscope.common.measurement.PositionInformation;

/**
 * @author langmo
 * 
 */
public class ImageEvent implements Serializable, Cloneable
{

	/**
	 * Serial Version UID.
	 */
	private static final long		serialVersionUID		= 3149338017624936717L;

	private final Object			imageData;
	private final int				width;
	private final int				height;
	private final int				bytesPerPixel;
	private final int				bitDepth;
	private ExecutionInformation	executionInformation	= null;
	private PositionInformation		positionInformation		= null;
	private long					creationTime;
	private String					camera					= "";
	private String					configGroup				= "";
	private String					channel					= "";
	private int						bands					= 1;

	

	private boolean					transposeX				= false;
	private boolean					transposeY				= false;
	private boolean					switchXY				= false;

	/**
	 * Constructor.
	 * Sets bit depth to maximal depth (i.e. bytesPerPixel * 8).
	 * @param imageData Object representing the image data.
	 * @param width The width of the image.
	 * @param height The height of the image.
	 * @param bytesPerPixel Number of bytes with which the image is encoded.
	 * 
	 */
	public ImageEvent(Object imageData, int width, int height, int bytesPerPixel)
	{
		this.imageData = imageData;
		this.width = width;
		this.height = height;
		this.bytesPerPixel = bytesPerPixel;
		this.bitDepth = bytesPerPixel * 8;
		this.creationTime = new Date().getTime();
	}

	/**
	 * Constructor.
	 * @param imageData Object representing the image data.
	 * @param width The width of the image.
	 * @param height The height of the image.
	 * @param bytesPerPixel Number of bytes with which the image is encoded.
	 * @param bitDepth The bit depth of the image.
	 * 
	 */
	public ImageEvent(Object imageData, int width, int height, int bytesPerPixel, int bitDepth)
	{
		this.imageData = imageData;
		this.width = width;
		this.height = height;
		this.bytesPerPixel = bytesPerPixel;
		this.bitDepth = bitDepth;
		this.creationTime = new Date().getTime();
	}

	/**
	 * Sets if the x-direction of images made by this camera should be transposed.
	 * @param transpose True, if the x-direction should be transposed.
	 */
	public void setTransposeX(boolean transpose)
	{
		transposeX = transpose;
	}

	/**
	 * Sets if the y-direction of images made by this camera should be transposed.
	 * @param transpose True, if the y-direction should be transposed
	 */
	public void setTransposeY(boolean transpose)
	{
		transposeY = transpose;
	}

	/**
	 * Sets if the x and the y direction should be switched for images made by this camera.
	 * @param switchXY True, if the x and y direction should be switched.
	 */
	public void setSwitchXY(boolean switchXY)
	{
		this.switchXY = switchXY;
	}

	/**
	 * Returns if the x-direction of images made by this camera should be transposed.
	 * @return True, if the x-direction is transposed.
	 */
	public boolean isTransposeX()
	{
		return transposeX;
	}

	/**
	 * Returns if the if the y-direction of images made by this camera should be transposed.
	 * @return True, if the y-direction is transposed.
	 */
	public boolean isTransposeY()
	{
		return transposeY;
	}

	/**
	 * Returns if the x and the y direction should be switched for images made by this camera.
	 * @return True, if the x and y direction should be switched.
	 */
	public boolean isSwitchXY()
	{
		return switchXY;
	}

	/**
	 * Returns the number of bands (colors in image).
	 * @return Number of bands.
	 */
	public int getBands()
	{
		return bands;
	}

	/**
	 * Sets the number of bands (colors in image).
	 * @param bands Number of bands.
	 */
	public void setBands(int bands)
	{
		this.bands = bands;
	}

	/**
	 * @return the imageData
	 */
	public Object getImageData()
	{
		return imageData;
	}

	/**
	 * @return the width
	 */
	public int getWidth()
	{
		return width;
	}

	/**
	 * @return the height
	 */
	public int getHeight()
	{
		return height;
	}

	/**
	 * @return the bytesPerPixel
	 */
	public int getBytesPerPixel()
	{
		return bytesPerPixel;
	}

	/**
	 * @return the bitDepth
	 */
	public int getBitDepth()
	{
		return bitDepth;
	}
	
	/**
	 * Returns the maximal intensity a pixel can have. This intensity is equal to 2^({@link #getBitDepth()})-1.
	 * Note that {@link #getImageData()} returns unsigned values.
	 * @return Maximal pixel intensity.
	 */
	public long getMaxIntensity()
	{
		return Math.round(Math.pow(2, getBitDepth())-1);
	}

	/**
	 * Sets the information of how often the image producing job was already executed when producing the image.
	 * @param executionInformation Information about producing job's evaluation times, or null.
	 */
	public void setExecutionInformation(ExecutionInformation executionInformation)
	{
		this.executionInformation = executionInformation;
	}

	/**
	 * Returns the information of how often the image producing job was already executed when producing the image.
	 * @return executionInformation Information about producing job's evaluation times, or null.
	 */
	public ExecutionInformation getExecutionInformation()
	{
		return executionInformation;
	}

	/**
	 * Sets the information about the logical position this image was made in.
	 * @param positionInformation Logical description of position, or null.
	 */
	public void setPositionInformation(PositionInformation positionInformation)
	{
		this.positionInformation = positionInformation;
	}

	/**
	 * Returns the information about the logical position this image was made in.
	 * @return Logical description of position, or null.
	 */
	public PositionInformation getPositionInformation()
	{
		return positionInformation;
	}

	/**
	 * @deprecated Use {@link #setCreationTime(long)} instead.
	 * @param imageCreationTime the imageCreationTime to set
	 */
	@Deprecated
	public void setImageCreationTime(Date imageCreationTime)
	{
		if(imageCreationTime == null)
			throw new NullPointerException();
		this.creationTime = imageCreationTime.getTime();
	}
	
	/**
	 * Sets the time milliseconds since January 1, 1970, 00:00:00 GMT when the image was created (see {@link Date#getTime()}).
	 * @param creationTime The time when image was created.
	 */
	public void setCreationTime(long creationTime)
	{
		this.creationTime = creationTime;
	}
	
	/**
	 * Returns the time milliseconds since January 1, 1970, 00:00:00 GMT when the image was created (see {@link Date#Date(long)}).
	 * @return The time when image was created.
	 */
	public long getCreationTime()
	{
		return creationTime;
	}

	/**
	 * @deprecated Use {@link #getCreationTime()} instead.
	 * @return the imageCreationTime
	 */
	@Deprecated
	public Date getImageCreationTime()
	{
		return new Date(creationTime);
	}

	/**
	 * @param camera the camera to set
	 */
	public void setCamera(String camera)
	{
		this.camera = camera;
	}

	/**
	 * @return the camera
	 */
	public String getCamera()
	{
		return camera;
	}

	/**
	 * @param configGroup the configGroup to set
	 */
	public void setConfigGroup(String configGroup)
	{
		this.configGroup = configGroup;
	}

	/**
	 * @return the configGroup
	 */
	public String getConfigGroup()
	{
		return configGroup;
	}

	/**
	 * @param channel the channel to set
	 */
	public void setChannel(String channel)
	{
		this.channel = channel;
	}

	/**
	 * @return the channel
	 */
	public String getChannel()
	{
		return channel;
	}

	

	@Override
	public ImageEvent clone()
	{
		// image data needs not to be cloned since it is final
		ImageEvent clone;
		try {
			clone = (ImageEvent)super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("Clone not supported.", e); // should not happen.
		}
		return clone;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + bands;
		result = prime * result + bitDepth;
		result = prime * result + bytesPerPixel;
		result = prime * result + ((camera == null) ? 0 : camera.hashCode());
		result = prime * result + ((channel == null) ? 0 : channel.hashCode());
		result = prime * result + ((configGroup == null) ? 0 : configGroup.hashCode());
		result = prime * result + (int) (creationTime ^ (creationTime >>> 32));
		result = prime * result + ((executionInformation == null) ? 0 : executionInformation.hashCode());
		result = prime * result + height;
		result = prime * result + ((imageData == null) ? 0 : imageData.hashCode());
		result = prime * result + ((positionInformation == null) ? 0 : positionInformation.hashCode());
		result = prime * result + (switchXY ? 1231 : 1237);
		result = prime * result + (transposeX ? 1231 : 1237);
		result = prime * result + (transposeY ? 1231 : 1237);
		result = prime * result + width;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ImageEvent other = (ImageEvent) obj;
		if (bands != other.bands)
			return false;
		if (bitDepth != other.bitDepth)
			return false;
		if (bytesPerPixel != other.bytesPerPixel)
			return false;
		if (camera == null) {
			if (other.camera != null)
				return false;
		} else if (!camera.equals(other.camera))
			return false;
		if (channel == null) {
			if (other.channel != null)
				return false;
		} else if (!channel.equals(other.channel))
			return false;
		if (configGroup == null) {
			if (other.configGroup != null)
				return false;
		} else if (!configGroup.equals(other.configGroup))
			return false;
		if (creationTime != other.creationTime)
			return false;
		if (executionInformation == null) {
			if (other.executionInformation != null)
				return false;
		} else if (!executionInformation.equals(other.executionInformation))
			return false;
		if (height != other.height)
			return false;
		if (imageData == null) {
			if (other.imageData != null)
				return false;
		} else if (!imageData.equals(other.imageData))
			return false;
		if (positionInformation == null) {
			if (other.positionInformation != null)
				return false;
		} else if (!positionInformation.equals(other.positionInformation))
			return false;
		if (switchXY != other.switchXY)
			return false;
		if (transposeX != other.transposeX)
			return false;
		if (transposeY != other.transposeY)
			return false;
		if (width != other.width)
			return false;
		return true;
	}

}
