/**
 * 
 */
package org.youscope.plugin.custommicroplates;

import java.io.Serializable;

import org.youscope.common.Microplate;

/**
 * @author langmo
 *
 */
class CustomMicroplateType implements Microplate, Cloneable, Serializable
{
	/**
	 * This class represents a custom microplate type.
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -8181191983847640156L;
	
	private final int numWellsX;
	private final int numWellsY;
	private final double wellWidth;
	private final double wellHeight;
	private final String microplateID;
	private final String microplateName;
	
	/**
	 * Constructor.
	 * @param numWellsX number of wells in x direction.
	 * @param numWellsY number of wells in y direction.
	 * @param wellWidth width of a well in microns.
	 * @param wellHeight height of a well in microns.
	 * @param microplateID unique ID of this microplate type.
	 * @param microplateName Human readable short description of this microplate type.
	 */
	public CustomMicroplateType(int numWellsX, int numWellsY, double wellWidth, double wellHeight, String microplateID, String microplateName)
	{
		this.numWellsX = numWellsX;
		this.numWellsY = numWellsY;
		this.wellWidth = wellWidth;
		this.wellHeight = wellHeight;
		this.microplateID = microplateID;
		this.microplateName = microplateName;
	}
	
	
	@Override
	public int getNumWellsX()
	{
		return numWellsX;
	}

	@Override
	public int getNumWellsY()
	{
		return numWellsY;
	}

	@Override
	public double getWellWidth()
	{
		return wellWidth;
	}

	@Override
	public double getWellHeight()
	{
		return wellHeight;
	}

	@Override
	public String getMicroplateID()
	{
		return microplateID;
	}

	@Override
	public String getMicroplateName()
	{
		return microplateName;
	}


	@Override
	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}

}
