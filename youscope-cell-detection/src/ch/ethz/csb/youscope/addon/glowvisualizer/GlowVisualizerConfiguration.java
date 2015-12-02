/**
 * 
 */
package ch.ethz.csb.youscope.addon.glowvisualizer;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import ch.ethz.csb.youscope.shared.addon.celldetection.CellVisualizationConfiguration;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.annotations.YSConfigAlias;
import ch.ethz.csb.youscope.shared.configuration.annotations.YSConfigClassification;
import ch.ethz.csb.youscope.shared.configuration.annotations.YSConfigDescription;

/**
 * @author Moritz Lang
 *
 */
@YSConfigAlias("Glow Visualizer")
@YSConfigClassification("cell visualization")
@YSConfigDescription("This visualizer displays the original image, and lets the detected cells glow.\nThe glow intensity defines how strong the cells glow: as stronger they glow, as better are they visible by eye. However, the background becomes less and less visible.")
@XStreamAlias("glow-visualizer-configuration")
public class GlowVisualizerConfiguration extends CellVisualizationConfiguration
{

	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -1111201676160652226L;
	
	/**
	 * Constructor.
	 */
	public GlowVisualizerConfiguration()
	{
		// do nothing.
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}

	@YSConfigAlias("Glow strength")
	@XStreamAlias("glow-strength")
	private double glowStrength = 0.5;
	
	/**
	 * The identifier for this configuration.
	 */
	public static final String	CONFIGURATION_ID	= "CSB::GlowVisualizer";
	
	@Override
	public String getTypeIdentifier()
	{
		return CONFIGURATION_ID;
	}

	/**
	 * Sets the strength in which the cells should be glowing.
	 * 0 means no glowing at all (returns original image), one means only glowing (original image not visible anymore).
	 * @param glowStrength
	 */
	public void setGlowStrength(double glowStrength)
	{
		if(glowStrength < 0)
			glowStrength = 0;
		if(glowStrength > 1)
			glowStrength = 1;
		this.glowStrength = glowStrength;
	}

	/**
	 * Returns the strength in which the cells should be glowing.
	 * 0 means no glowing at all (returns original image), one means only glowing (original image not visible anymore).
	 * @return the glow strength.
	 */
	public double getGlowStrength()
	{
		return glowStrength;
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		super.checkConfiguration();
		if(glowStrength < 0 || glowStrength > 1)
			throw new ConfigurationException("Glow strength must be between zero and one.");
		
	}

	@Override
	public String[] getImageSaveNames() {
		return new String[0];
	}

	@Override
	public int getNumberOfImages() {
		return 1;
	}
}
