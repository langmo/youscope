/*******************************************************************************
 * Copyright (c) 2017 Moritz Lang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Moritz Lang - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package org.youscope.common.image;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Date;

import org.youscope.common.ExecutionInformation;
import org.youscope.common.MeasurementContext;
import org.youscope.common.PositionInformation;

/**
 * Objects of this class contain the pixel data of an image, as well as metadata about it, like where and when an image was made. 
 * @author Moritz Lang
 * @param <T> Specifies the array type of the image. Typically either byte[], short[], or int[].
 * 
 */
public final class ImageEvent<T extends Object> implements Serializable, Cloneable
{

	/**
	 * Serial Version UID.
	 */
	private static final long		serialVersionUID		= 3149338017624936717L;

	private final T					imageData;
	private final int				width;
	private final int				height;
	private final int				bytesPerPixel;
	private final int				bitDepth;
	private ExecutionInformation	executionInformation	= null;
	private PositionInformation		positionInformation		= null;
	private long					creationTime;
	private long 					creationRuntime			= -1;
	private String					camera					= "";
	private String					channelGroup				= "";
	private String					channel					= "";
	private int						bands					= 1;

	

	private boolean					transposeX				= false;
	private boolean					transposeY				= false;
	private boolean					switchXY				= false;

	/**
	 * Private constructor. To create an image, use {@link #createImage(Object, int, int)} or {@link #createImage(Object, int, int, int)}.
	 * @param imageData Object representing the image data.
	 * @param width The width of the image.
	 * @param height The height of the image.
	 * @param bytesPerPixel Number of bytes with which the image is encoded.
	 * @param bitDepth The bit depth of the image.
	 * 
	 */
	private ImageEvent(T imageData, int width, int height, int bytesPerPixel, int bitDepth)
	{
		this.imageData = imageData;
		this.width = width;
		this.height = height;
		this.bytesPerPixel = bytesPerPixel;
		this.bitDepth = bitDepth;
		this.creationTime = System.currentTimeMillis();
	}
	
	/**
	 * Returns the class of the array in which the image data is stored. One of byte[].class, short[].class or int[].class.
	 * @return Class of array of image data.
	 */
	public Class<?> getImageDataArrayType()
	{
		return imageData.getClass();
	}
	
	/**
	 * Returns the class of the primitive type in which one pixel of the image data is stored. One of byte.class, short.class or int.class.
	 * @return Primitive class of image data.
	 */
	public Class<?> getImageDataType()
	{
		return imageData.getClass().getComponentType();
	}
	
	/**
	 * Creates a new image with the given image data and meta information. The image data must be an array of primitive types byte, short or int. This limitation may change in future releases of YouScope.
	 * @param imageData Array of bytes, shorts or ints representing the pixel intensities, either of grayscale images or of several bands (colors) or color images.
	 * @param width The width of the image in pixels.
	 * @param height The height of the image in pixels.
	 * @param bitDepth The bit depth of the image.
	 * @return Newly created image.
	 * @throws NullPointerException Thrown if imageData is null. 
	 * @throws IllegalArgumentException Thrown if type of image data is not supported by YouScope, or if number of pixels in imageData is not in agreement of claimed width and height of the image.
	 */
	public static <T> ImageEvent<T> createImage(T imageData, int width, int height, int bitDepth) throws NullPointerException, IllegalArgumentException
	{
		if(imageData == null)
			throw new NullPointerException("Image data used to create new image is null.");
		else if(!imageData.getClass().isArray())
			throw new IllegalArgumentException("Image data must be an array of primitive types, e.g. byte[], short[], or int[]");
		else if(Array.getLength(imageData) != width * height)
			throw new IllegalArgumentException("Provided image data array has "+Integer.toString(Array.getLength(imageData))+" pixels, while image width "+Integer.toString(width)+" and height "+Integer.toString(height)+" correspond to "+Integer.toString(width*height)+" pixels.");
		
		// get bytes per pixel
		Class<?> primitiveType = imageData.getClass().getComponentType();
		if(!primitiveType.isPrimitive())
			throw new IllegalArgumentException("Image data must be an array of primitive types, whereas an array of non-primitive types is provided.");
		int bytesPerPixel;
		if(primitiveType.equals(int.class))
		{
			bytesPerPixel = 4;
		}
		else if(primitiveType.equals(short.class))
		{
			bytesPerPixel = 2;
		}
		else if(primitiveType.equals(byte.class))
		{
			bytesPerPixel = 1;
		}
		else
			throw new IllegalArgumentException("Currently, only image data represented as arrays of ints, short or bytes is accepted in YouScope. Image data was an array of "+primitiveType.getName()+". This might change in future version of YouScope.");
		if(bitDepth < 0)
			bitDepth = bytesPerPixel*8;
		else if(bitDepth > bytesPerPixel*8)
			throw new IllegalArgumentException("Image pixels are stored with "+Integer.toString(bytesPerPixel)+" bytes per pixel, resulting in a maximal bit depth of "+ (bytesPerPixel*8)+", while a bit depth of "+bitDepth+" was claimed.");
		return new ImageEvent<T>(imageData, width, height, bytesPerPixel, bitDepth);
	}
	
	
	
	/**
	 * Creates a new image with the given image data and meta information. The bit depth is set to the maximal bit depth for the given pixel type, i.e. 8bit for bytes, 16bit for shorts, and 32bit for ints. 
	 * @param imageData Array of bytes, shorts or ints representing the pixel intensities, either of grayscale images or of several bands (colors) or color images.
	 * @param width The width of the image in pixels.
	 * @param height The height of the image in pixels.
	 * @return Newly created image.
	 * @throws NullPointerException Thrown if imageData is null. 
	 * @throws IllegalArgumentException Thrown if type of image data is not supported by YouScope, or if number of pixels in imageData is not in agreement of claimed width and height of the image.
	 */
	public static <T> ImageEvent<T> createImage(T imageData, int width, int height) throws NullPointerException, IllegalArgumentException
	{
		return createImage(imageData, width, height, -1);
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
	 * Returns the pixel data as an array of primitive types (typically byte or short). Note that the pixel values are typically unsigned, whereas
	 * typical java operations on them assume them to be signed. This typically implies that one has to upcast them, e.g. byte to short, or short to int.
	 * @return the imageData
	 */
	public T getImageData()
	{
		return imageData;
	}

	/**
	 * Returns image width in pixels.
	 * @return image width in pixels.
	 */
	public int getWidth()
	{
		return width;
	}

	/**
	 * Returns image height in pixels.
	 * @return image height in pixels
	 */
	public int getHeight()
	{
		return height;
	}

	/**
	 * Returns the number of bytes used to store each pixel. Typically one or two for grayscale images, and more only for colour images.
	 * @return Number of bytes used to store a pixel.
	 */
	public int getBytesPerPixel()
	{
		return bytesPerPixel;
	}

	/**
	 * Returns the bit dept. This has to be smaller or equal to {@link #getBytesPerPixel()}*8. For example, 12bit/pixel images are typically 
	 * stored in 2 bytes/pixel, which would allow for up to 16bit depth. However, 12bit pixels typically only use the lower 12bit of the 16bit available
	 * in two bytes.
	 * @return the bitDepth of the image.
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
	 * Sets the runtime in ms when the image was created. See {@link MeasurementContext#getMeasurementRuntime()}.
	 * @param creationRuntime Runtime in ms, or -1 if not known.
	 */
	public void setCreationRuntime(long creationRuntime)
	{
		this.creationRuntime = creationRuntime;
	}
	
	/**
	 * Returns the runtime in ms when the image was created. See {@link MeasurementContext#getMeasurementRuntime()}.
	 * @return Measurement runtime in ms.
	 */
	public long getCreationRuntime()
	{
		return creationRuntime;
	}

	/**
	 * Set the name of the camera with which the image was taken.
	 * @param camera the name of the camera which took the image.
	 */
	public void setCamera(String camera)
	{
		this.camera = camera;
	}

	/**
	 * Returns the name of the camera which took the image.
	 * @return camera name.
	 */
	public String getCamera()
	{
		return camera;
	}

	/**
	 * Sets the channel group in which the image was made. Together with {@link #setChannel(String)}, this identifies the channel the image was taken in.
	 * @param channelGroup the channel group the image was taken in.
	 */
	public void setChannelGroup(String channelGroup)
	{
		this.channelGroup = channelGroup;
	}

	/**
	 * Returns the channel group in which the image was made. Together with {@link #getChannel()}, this identifies the channel the image was taken in.
	 * @return the channel group the image was taken in.
	 */
	public String getChannelGroup()
	{
		return channelGroup;
	}

	/**
	 * Sets the channel in which the image was made. Together with {@link #setChannelGroup(String)}, this identifies the channel the image was taken in.
	 * @param channel the channel the image was taken in.
	 */
	public void setChannel(String channel)
	{
		this.channel = channel;
	}

	/**
	 * Returns the channel in which the image was made. Together with {@link #getChannelGroup()}, this identifies the channel the image was taken in.
	 * @return the channel the image was taken in.
	 */
	public String getChannel()
	{
		return channel;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ImageEvent<T> clone()
	{
		// We don't clone the image data. Even though it might be manipulated, the convention is that it is not.
		try {
			return (ImageEvent<T>)super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("Clone not supported.", e); // should not happen.
		}
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
		result = prime * result + ((channelGroup == null) ? 0 : channelGroup.hashCode());
		result = prime * result + (int) (creationRuntime ^ (creationRuntime >>> 32));
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
		ImageEvent<?> other = (ImageEvent<?>) obj;
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
		if (channelGroup == null) {
			if (other.channelGroup != null)
				return false;
		} else if (!channelGroup.equals(other.channelGroup))
			return false;
		if (creationRuntime != other.creationRuntime)
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
