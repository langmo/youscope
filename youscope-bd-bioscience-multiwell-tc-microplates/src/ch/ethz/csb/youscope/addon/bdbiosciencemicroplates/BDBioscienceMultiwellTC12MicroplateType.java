/**
 * 
 */
package ch.ethz.csb.youscope.addon.bdbiosciencemicroplates;

import java.io.Serializable;

import ch.ethz.csb.youscope.shared.MicroplateType;

/**
 * @author langmo
 *
 */
public class BDBioscienceMultiwellTC12MicroplateType implements MicroplateType, Cloneable, Serializable
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 6473798009242211940L;
	/**
	 * The ID of this microplate type.
	 */
	public static final String TYPE_ID = "BD_BIOSCIENCE_MULTIWELL_TC_12";
	
	@Override
	public int getNumWellsX()
	{
		return 4;
	}

	@Override
	public int getNumWellsY()
	{
		return 3;
	}

	@Override
	public double getWellWidth()
	{
		return 26000;
	}

	@Override
	public double getWellHeight()
	{
		return 26000;
	}

	@Override
	public String getMicroplateID()
	{
		return TYPE_ID;
	}

	@Override
	public String getMicroplateName()
	{
		return "12 well microplate (BD Bioscience™ - Multiwell™ TC Plate).";
	}
	@Override
	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}
}
