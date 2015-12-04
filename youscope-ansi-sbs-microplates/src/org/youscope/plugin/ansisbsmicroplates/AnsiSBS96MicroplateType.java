/**
 * 
 */
package org.youscope.plugin.ansisbsmicroplates;

import java.io.Serializable;

import org.youscope.common.MicroplateType;

/**
 * @author langmo
 *
 */
public class AnsiSBS96MicroplateType implements MicroplateType, Cloneable, Serializable
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -8182991983847640156L;
	/**
	 * The ID of this microplate type.
	 */
	public static final String TYPE_ID = "ANSI_SBS_96";
	
	@Override
	public int getNumWellsX()
	{
		return 12;
	}

	@Override
	public int getNumWellsY()
	{
		return 8;
	}

	@Override
	public double getWellWidth()
	{
		return 9000;
	}

	@Override
	public double getWellHeight()
	{
		return 9000;
	}

	@Override
	public String getMicroplateID()
	{
		return TYPE_ID;
	}

	@Override
	public String getMicroplateName()
	{
		return "96 well microplate (ANSI/SBS 1-2004 through ANSI/SBS 4-2004).";
	}

	@Override
	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}
}
