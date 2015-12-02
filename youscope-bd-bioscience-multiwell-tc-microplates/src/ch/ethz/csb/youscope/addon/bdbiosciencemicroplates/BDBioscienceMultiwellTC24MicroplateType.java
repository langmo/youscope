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
public class BDBioscienceMultiwellTC24MicroplateType implements MicroplateType, Cloneable, Serializable
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 1486785707683005744L;
	/**
	 * The ID of this microplate type.
	 */
	public static final String TYPE_ID = "BD_BIOSCIENCE_MULTIWELL_TC_24";
	
	@Override
	public int getNumWellsX()
	{
		return 6;
	}

	@Override
	public int getNumWellsY()
	{
		return 4;
	}

	@Override
	public double getWellWidth()
	{
		return 19300.0;
	}

	@Override
	public double getWellHeight()
	{
		return 19300.0;
	}

	@Override
	public String getMicroplateID()
	{
		return TYPE_ID;
	}

	@Override
	public String getMicroplateName()
	{
		return "24 well microplate (BD Bioscience™ - Multiwell™ TC Plate).";
	}
	@Override
	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}
}
