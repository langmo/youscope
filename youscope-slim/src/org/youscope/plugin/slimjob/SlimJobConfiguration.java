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
package org.youscope.plugin.slimjob;

import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.image.ImageProducerConfiguration;
import org.youscope.common.job.JobConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.basic.BooleanConverter;

/**
 * @author Moritz Lang
 *
 */
@XStreamAlias("slim-job")
public class SlimJobConfiguration implements JobConfiguration, ImageProducerConfiguration
{
	/**
	 * Serial version UID.
	 */
	private static final long	serialVersionUID	= 7911732041177941444L;
	
	/**
	 * The identifier for this job type.
	 */
	public static final String	TYPE_IDENTIFIER	= "YouScope.ReflectorJob";
	
	/**
	 * Minimal phase shift value
	 */
	public static final int MIN_PHASE_SHIFT = 255;
	/**
	 * Maximal phase shift value
	 */
	public static final int MAX_PHASE_SHIFT = 255;
	
	/**
	 * Number of different phase shifts of the mask
	 */
	public static final int NUM_PHASE_SHIFT_MASK = 4;

	@XStreamAlias("reflector-device")
	private String				reflectorDevice			= null;
	
	@XStreamAlias("mask-x")
	private int maskX = 1000;
	
	@XStreamAlias("mask-y")
	private int maskY = 500;
	
	@XStreamAlias("inner-radius")
	private int innerRadius = 150;
	
	@XStreamAlias("outer-radius")
	private int outerRadius = 300;
	
	@XStreamAlias("phase-shift-outside")
	private int phaseShiftOutside = 0;
	
	@XStreamAlias("phase-shifts-mask")
	private final int[] phaseShiftsMask = {0,64,128,192};
	
	@XStreamAlias("slim-delay-ms")
	private int slimDelayMS = 60;
	
	@XStreamAlias("mask-file-name")
	private String maskFileName = null;
	
	@XStreamAlias("attenuation-factor")
	private double attenuationFactor = 1;
	
	/**
	 * Returns the attenuation factor with which the SLIM image is calculated.
	 * @return Attenuation factor.
	 */
	public double getAttenuationFactor() {
		return attenuationFactor;
	}

	/**
	 * Sets the attenuation factor with which the SLIM image is calculated.
	 * @param attenuationFactor Attenuation factor.
	 */
	public void setAttenuationFactor(double attenuationFactor) {
		this.attenuationFactor = attenuationFactor;
	}
	/**
	 * The channel where the images should be made. Set to an empty string if
	 * the current channel should be taken.
	 */
	@XStreamAlias("channel")
	private String				channel				= "";

	/**
	 * The channel group where the channel is defined.
	 */
	@XStreamAlias("channel-group")
	private String				channelGroup			= "";
	/**
	 * The exposure time for the imaging.
	 */
	@XStreamAlias("exposure-ms")
	private double				exposure			= 20.0;

	/**
	 * Whether images should be saved to disk or not.
	 */
	@XStreamAlias("save-images")
	@XStreamConverter(value = BooleanConverter.class, booleans = {false}, strings = {"yes", "no"})
	private boolean				saveImages			= true;
	
	@XStreamAlias("camera")
	private String				camera				= null;

	/**
	 * The name under which the image is saved.
	 */
	@XStreamAlias("image-save-name")
	private String				imageSaveName		= "slim";
	
	@Override
	public String getTypeIdentifier()
	{
		return TYPE_IDENTIFIER;
	}
	
	@Override
	public String getDescription()
	{
		
		String description = "<p>for shift=[";
		for(int i=0; i<phaseShiftsMask.length; i++)
		{
			if(i>0)
				description+=",";
			description += Integer.toString(phaseShiftsMask[i]);
		}
		description += "]</p>" +
		"<ul style=\"margin-top:0px;margin-bottom:0px;margin-left:12px;list-style-type:none\">";
		description += "<li>" + reflectorDevice.toString() + ".setShift(shift)</li>";
		description += "<li>" + getImageSaveName() + "=snapImage(channel=\"" + getChannel() + "\", exposure=" + Double.toString(getExposure()) + "ms)</li>";
		description += "</ul><p>end</p>";
		return description;
	}
	
	/**
	 * Sets the channel which should be imaged.
	 * @param channelGroup The group of the channel.
	 * @param channel The channel.
	 */
	public void setChannel(String channelGroup, String channel)
	{
		this.channelGroup = channelGroup;
		this.channel = channel;
	}

	/**
	 * @return the channel in which should be imaged.
	 */
	public String getChannel()
	{
		return channel;
	}

	/**
	 * @return the group of the channel
	 */
	public String getChannelGroup()
	{
		return channelGroup;
	}

	/**
	 * @param exposure
	 *            the exposure to set
	 */
	public void setExposure(double exposure)
	{
		this.exposure = exposure;
	}

	/**
	 * @return the exposure
	 */
	public double getExposure()
	{
		return exposure;
	}

	/**
	 * @param saveImages
	 *            the saveImages to set
	 */
	public void setSaveImages(boolean saveImages)
	{
		this.saveImages = saveImages;
	}

	/**
	 * @return the saveImages
	 */
	public boolean isSaveImages()
	{
		return saveImages;
	}

	/**
	 * Returns the name under which the images should be saved.
	 * @return Name of imaging job.
	 */
	public String getImageSaveName()
	{
		return imageSaveName;
	}
	
	/**
	 * Sets the name under which the images should be saved.
	 * @param name Name of imaging job.
	 */
	public void setImageSaveName(String name)
	{
		this.imageSaveName = name;
	}
	
	@Override
	public String[] getImageSaveNames()
	{
		if(saveImages)
		{
			return new String[]{imageSaveName};
		}
		return null;
	}

	@Override
	public int getNumberOfImages()
	{
		return NUM_PHASE_SHIFT_MASK+1;
	}

	/**
	 * Sets the X position of the center of the inner and outer circle (the "donut").
	 * @param maskX X-position.
	 */
	public void setMaskX(int maskX)
	{
		this.maskX = maskX;
	}

	/**
	 * Returns the X position of the center of the inner and outer circle (the "donut").
	 * @return X-position.
	 */
	public int getMaskX()
	{
		return maskX;
	}

	/**
	 * Sets the Y position of the center of the inner and outer circle (the "donut").
	 * @param maskY Y-position.
	 */
	public void setMaskY(int maskY)
	{
		this.maskY = maskY;
	}

	/**
	 * Returns the Y position of the center of the inner and outer circle (the "donut").
	 * @return Y-position.
	 */
	public int getMaskY()
	{
		return maskY;
	}

	/**
	 * Sets the radius of the inner circle (the hole in the "donut").
	 * @param innerRadius the inner radius. Must be > 0.
	 */
	public void setInnerRadius(int innerRadius)
	{
		this.innerRadius = innerRadius;
	}

	/**
	 * Returns the radius of the inner circle (the hole in the "donut").
	 * @return the inner radius. Must be > 0.
	 */
	public int getInnerRadius()
	{
		return innerRadius;
	}

	/**
	 * Sets the radius of the outer circle (the "donut").
	 * @param outerRadius the outer radius. Must be > innerRadius.
	 */
	public void setOuterRadius(int outerRadius)
	{
		this.outerRadius = outerRadius;
	}

	/**
	 * Returns the radius of the outer circle (the "donut").
	 * @return the outer radius.
	 */
	public int getOuterRadius()
	{
		return outerRadius;
	}

	/**
	 * Sets the phase shift outside of the mask (background of donut).
	 * @param phaseShiftOutside the outer phase shift. Must be >=0 and < 256.
	 */
	public void setPhaseShiftOutside(int phaseShiftOutside)
	{
		this.phaseShiftOutside = phaseShiftOutside;
	}

	/**
	 * Returns the phase shift outside of the mask (background of donut).
	 * @return the outer phase shift.
	 */
	public int getPhaseShiftOutside()
	{
		return phaseShiftOutside;
	}
	
	/**
	 * Returns the phase shift of the mask (the donut) for the maskID mask.
	 * @param maskID the phase shift. Must be >=0 and < 4.
	 * @return phaseShift the phase shift.
	 */
	public int getPhaseShiftMask(int maskID)
	{
		if(maskID < 0 || maskID >= NUM_PHASE_SHIFT_MASK)
			throw new IndexOutOfBoundsException("The pattern ID must be >= 0 and < " + Integer.toString(phaseShiftsMask.length) +".");
		return phaseShiftsMask[maskID];
	}
	
	/**
	 * Sets the phase shift of the mask (the donut) for the maskID mask.
	 * @param phaseShift the phase shift. Must be >=0 and < 256.
	 * @param maskID the phase shift. Must be >=0 and < 4.
	 */
	public void setPhaseShiftMask(int maskID, int phaseShift)
	{
		if(maskID < 0 || maskID >= NUM_PHASE_SHIFT_MASK)
			throw new IndexOutOfBoundsException("The pattern ID must be >= 0 and < " + Integer.toString(phaseShiftsMask.length) +".");
		phaseShiftsMask[maskID] = phaseShift;
	}

	/**
	 * Returns the name of the reflector device which should be used to generate the pattern.
	 * @return Name of the reflector device.
	 */
	public String getReflectorDevice()
	{
		return reflectorDevice;
	}

	/**
	 * Returns the name of the reflector device which should be used to generate the pattern.
	 * @param reflectorDevice Name of the reflector device.
	 */
	public void setReflectorDevice(String reflectorDevice)
	{
		this.reflectorDevice = reflectorDevice;
	}
	
	/**
	 * Sets the camera with which should be imaged. Set to null to use the default camera.
	 * @param camera ID of camera device, or null.
	 */
	public void setCamera(String camera)
	{
		this.camera = camera;
	}

	/**
	 * Returns the ID of the camera with which should be imaged, or null, if imaging with the default camera.
	 * @return ID of camera, or null.
	 */
	public String getCamera()
	{
		return camera;
	}

	/**
	 * Returns the time delay in ms between changing the SLIM reflector settings and taking an image.
	 * @return Delay in ms.
	 */
	public int getSlimDelayMS()
	{
		return slimDelayMS;
	}
	
	/**
	 * Sets the time delay in ms between changing the SLIM reflector settings and taking an image.
	 * @param slimDelayMS delay in ms. Must be >= 0.
	 */
	public void setSlimDelayMS(int slimDelayMS)
	{
		this.slimDelayMS = slimDelayMS;
	}

	/**
	 * Sets the file name of the mask which should be used to define foreground and background. Set to null to use donut mode instead.
	 * @param maskFileName Name of file which defines background and foreground, or null for donut mode.
	 */
	public void setMaskFileName(String maskFileName)
	{
		this.maskFileName = maskFileName;
	}
	/**
	 * Returns the file name of the mask which should be used to define foreground and background. Returns null if donut mode is active.
	 * @return Name of file which defines background and foreground, or null for donut mode.
	 */
	public String getMaskFileName()
	{
		return maskFileName;
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		// do nothing, too complicated.
		
	}
}
