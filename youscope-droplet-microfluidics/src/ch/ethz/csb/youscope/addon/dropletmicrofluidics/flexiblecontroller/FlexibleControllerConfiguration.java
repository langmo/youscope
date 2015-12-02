package ch.ethz.csb.youscope.addon.dropletmicrofluidics.flexiblecontroller;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.annotations.YSConfigAlias;
import ch.ethz.csb.youscope.shared.configuration.annotations.YSConfigDoubleRange;
import ch.ethz.csb.youscope.shared.resource.dropletmicrofluidics.DropletControllerConfiguration;

/**
 * Configuration for the droplet based microfluidic measurements based on a syringe table.
 * @author Moritz Lang
 *
 */
@YSConfigAlias("Table controller")
@XStreamAlias("droplet-table-controller")
public class FlexibleControllerConfiguration extends DropletControllerConfiguration
{
 
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -7518731968362660759L;
	
	@YSConfigAlias("Time constant of controller's proportional part (ms)")
	@YSConfigDoubleRange(minValue=0.0)
	@XStreamAlias("time-constant-p-min")
	private long timeConstantProportional = 15*60*1000;
	
	@YSConfigAlias("Time constant of controller's integral part (ms)")
	@YSConfigDoubleRange(minValue=0.0)
	@XStreamAlias("time-constant-integral")
	private long timeConstantIntegral = 10*60*60*1000;
	
	@XStreamAlias("syringe-table")
	private FlexibleSyringeTableRow[] syringeTableRows = null;
	
	@XStreamAlias("ratio-height-to-volume-um-ul")
	private double ratioHeightToVolume = 8.5;

	/**
	 * Returns the (estimated) ratio between droplet height in um and the droplet volume in ul.
	 * @return ratio height to volume.
	 */
	public double getRatioHeightToVolume() {
		return ratioHeightToVolume;
	}

	/**
	 * Sets the (estimated) ratio between droplet height in um and the droplet volume in ul.
	 * @param ratioHeightToVolume ratio height to volume.
	 */
	public void setRatioHeightToVolume(double ratioHeightToVolume) {
		this.ratioHeightToVolume = ratioHeightToVolume;
	}

	/**
	 * The identifier for this configuration.
	 */
	public static final String	TYPE_IDENTIFIER	= "CSB::DropletMicrofluidics::FlexibleController";
	
	@Override
	public String getTypeIdentifier()
	{
		return TYPE_IDENTIFIER;
	}

	/**
	 * Returns the time constant (in ms) of the proportional part of the controller.
	 * @return time constant of proportional part.
	 */
	public long getTimeConstantProportional() {
		return timeConstantProportional;
	}

	/**
	 * Sets the time constant (in ms) of the proportional part of the controller.
	 * @param timeConstantProportional time constant of proportional part.
	 */
	public void setTimeConstantProportional(long timeConstantProportional) {
		this.timeConstantProportional = timeConstantProportional;
	}

	/**
	 * Returns the time constant (in ms) of the integral part of the controller.
	 * @return time constant of integral part.
	 */
	public long getTimeConstantIntegral() {
		return timeConstantIntegral;
	}

	/**
	 * Sets the time constant (in ms) of the integral part of the controller.
	 * 
	 * @param timeConstantIntegral time constant of integral part.
	 */
	public void setTimeConstantIntegral(long timeConstantIntegral) {
		this.timeConstantIntegral = timeConstantIntegral;
	}

	/**
	 * Returns a table defining when which syringe should act as an outflow or inflow syringe.
	 * @return syringe table.
	 */
	public FlexibleSyringeTableRow[] getSyringeTableRows() {
		return syringeTableRows;
	}

	/**
	 * Sets a table defining when which syringe should act as an outflow or inflow syringe.
	 * @param syringeTableRows syringe table.
	 */
	public void setSyringeTableRows(FlexibleSyringeTableRow[] syringeTableRows) {
		this.syringeTableRows = syringeTableRows;
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		super.checkConfiguration();
		
		if(syringeTableRows == null || syringeTableRows.length == 0)
    		throw new ConfigurationException("Add at least one row to the syringe table.");
    	if(syringeTableRows[0].getNumSyringes() <=0)
    		throw new ConfigurationException("No flow units available.");
    	for(int i = 0; i < syringeTableRows.length; i++)
    	{
    		SyringeControlState[] states = syringeTableRows[i].getSyringeControlStates();
    		double[] flows = syringeTableRows[i].getTargetFlowRates();
    		double deltaFlow = syringeTableRows[i].getMaxDeltaFlowRate();
    		if(deltaFlow < 0)
    			throw new ConfigurationException("Maximal delta flow rate must be zero or positive.");
    		boolean atLeastOne = false;
    		for(int j=0; j<states.length; j++)
    		{
    			double flow = flows[j];
    			SyringeControlState state = states[j];
    			if(flow > 0 && state == SyringeControlState.NEGATIVE)
    				throw new ConfigurationException("Target flow of Flow Unit " +Integer.toString(j+1)+" in row " + Integer.toString(i+1) + " is positive, while keep negative is selected.");
    			else if(flow < 0 && state == SyringeControlState.POSITIVE)
    				throw new ConfigurationException("Target flow of Flow Unit " +Integer.toString(j+1)+" in row " + Integer.toString(i+1) + " is negative, while keep positive is selected.");
    			atLeastOne = atLeastOne || !(state == SyringeControlState.FIXED);
    		}
    		if(!atLeastOne && deltaFlow != 0)
    			throw new ConfigurationException("No syringe in row " + Integer.toString(i+1) + " allowed to vary flow rate, while non-zero maximal delta flow rate is selected.\nEither set maximal delta flow rate to zero, or allow at least the flow of one syringe to vary.");
    	}
	}


}
