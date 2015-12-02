package ch.ethz.csb.youscope.addon.dropletmicrofluidics.defaultobserver;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.annotations.YSConfigAlias;
import ch.ethz.csb.youscope.shared.configuration.annotations.YSConfigDoubleRange;
import ch.ethz.csb.youscope.shared.resource.dropletmicrofluidics.DropletObserverConfiguration;

/**
 * Configuration for observer based on discrete Fourier transformation.
 * @author Moritz Lang
 *
 */
@YSConfigAlias("Droplet observer")
@XStreamAlias("droplet-default-observer")
public class DefaultObserverConfiguration extends DropletObserverConfiguration
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -2815430198403970866L;
	/**
	 * Type identifier.
	 */
	public static final String TYPE_IDENTIFIER = "CSB::DropletDefaultObserver";
	
	@YSConfigAlias("Individual droplet's height learn speed (0-1)")
	@YSConfigDoubleRange(minValue=0.0,maxValue=1.0)
	@XStreamAlias("observer-individual")
	private double observerIndividual = 0.7;
	
	@YSConfigAlias("Mean droplet's height learn speed (0-inf)")
	@YSConfigDoubleRange(minValue=0.0)
	@XStreamAlias("observer-mean")
	private double observerMean = 1.5;
	
	@YSConfigAlias("Microfluidic chip number")
	@XStreamAlias("microfluidic-chip-id")
	private int microfluidicChipID = 1;
	
	@Override
	public String getTypeIdentifier() 
	{
		return TYPE_IDENTIFIER;
	}

	/**
	 * Returns the ID of the chip for which this observer observes the droplet height.
	 * All observers having the same chip ID have to be called sequentially and equally often, since they considered to observe the height of droplets on the same chip.
	 * Thus, it is possible to observe the droplet heights of several chips by having different IDs.
	 * @return microfluidic chip id.
	 */
	public int getMicrofluidicChipID() {
		return microfluidicChipID;
	}

	/**
	 * Sets the ID of the chip for which this observer observes the droplet height.
	 * All observers having the same chip ID have to be called sequentially and equally often, since they considered to observe the height of droplets on the same chip.
	 * Thus, it is possible to observe the droplet heights of several chips by having different IDs.
	 * @param microfluidicChipID The id of the chip.
	 */
	public void setMicrofluidicChipID(int microfluidicChipID) {
		this.microfluidicChipID = microfluidicChipID;
	}
	
	/**
	 * Returns learn rate constant of individual droplet heights.
	 * @return Individual droplet height learn rate constant (0-1).
	 */
	public double getObserverIndividual() {
		return observerIndividual;
	}

	/**
	 * Sets the learn rate constant of individual droplet heights.
	 * @param observerIndividual Individual droplet height learn rate constant (0-1).
	 */
	public void setObserverIndividual(double observerIndividual) {
		this.observerIndividual = observerIndividual;
	}

	/**
	 * Returns learn rate constant of mean droplet heights.
	 * @return Mean droplet height learn rate constant (0-2).
	 */
	public double getObserverMean() {
		return observerMean;
	}

	/**
	 * Sets the learn rate constant of mean droplet heights.
	 * @param observerMean Mean droplet height learn rate constant (0-2).
	 */
	public void setObserverMean(double observerMean) {
		this.observerMean = observerMean;
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		super.checkConfiguration();
		
		if(observerMean < 0)
			throw new ConfigurationException("Observer mean droplet height learn speed must be bigger or equal to zero.");
		if(observerIndividual < 0 || observerIndividual > 1)
			throw new ConfigurationException("Individual droplet learn speed must be between zero and one.");
	}
	
	
}
