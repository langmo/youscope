/**
 * 
 */
package org.youscope.plugin.ansisbsmicroplates;

import java.io.Serializable;

import org.youscope.common.MicroplateType;

/**
 * @author Moritz Lang
 *
 */
public class AnsiSBS1536MicroplateType implements MicroplateType, Cloneable, Serializable
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -5859563749287866121L;
	/**
	 * The ID of this microplate type.
	 */
	public static final String TYPE_ID = "ANSI_SBS_1536";
	
	/**
	 * Constructor
	 */
	public AnsiSBS1536MicroplateType()
	{
		// do nothing.
	}
	
	@Override
	public int getNumWellsX()
	{
		return 48;
	}

	@Override
	public int getNumWellsY()
	{
		return 32;
	}

	@Override
	public double getWellWidth()
	{
		return 2250.0;
	}

	@Override
	public double getWellHeight()
	{
		return 2250.0;
	}

	@Override
	public String getMicroplateID()
	{
		return TYPE_ID;
	}

	@Override
	public String getMicroplateName()
	{
		return "1536 well microplate (ANSI/SBS 1-2004 through ANSI/SBS 4-2004).";
	}

	@Override
	public AnsiSBS1536MicroplateType clone()
	{
		try {
			return (AnsiSBS1536MicroplateType) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("Clone not supported.", e); // will not happen.
		}
	}
}
