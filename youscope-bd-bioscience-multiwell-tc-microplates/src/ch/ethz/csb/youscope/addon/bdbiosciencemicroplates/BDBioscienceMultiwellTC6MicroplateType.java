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
public class BDBioscienceMultiwellTC6MicroplateType implements MicroplateType, Cloneable, Serializable
{
	/**
	 * Seria Version UID.
	 */
	private static final long	serialVersionUID	= -848867083797637643L;
	/**
	 * The ID of this microplate type.
	 */
	public static final String TYPE_ID = "BD_BIOSCIENCE_MULTIWELL_TC_6";
	
	@Override
	public int getNumWellsX()
	{
		return 3;
	}

	@Override
	public int getNumWellsY()
	{
		return 2;
	}

	@Override
	public double getWellWidth()
	{
		return 39240;
	}

	@Override
	public double getWellHeight()
	{
		return 39240;
	}

	@Override
	public String getMicroplateID()
	{
		return TYPE_ID;
	}

	@Override
	public String getMicroplateName()
	{
		return "6 well microplate (BD Bioscience™ - Multiwell™ TC Plate).";
	}
	@Override
	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}
}
