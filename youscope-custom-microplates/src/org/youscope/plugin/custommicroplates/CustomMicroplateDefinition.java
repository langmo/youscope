package org.youscope.plugin.custommicroplates;

class CustomMicroplateDefinition 
{
	private int numWellsX = 12;
	private int numWellsY = 8;
	private double wellWidth = 9000.;
	private double wellHeight = 9000.;
	private String customMicroplateName = "unnamed";
	public int getNumWellsX()
	{
		return numWellsX;
	}
	
	public void setNumWellsX(int numWellsX)
	{
		this.numWellsX = numWellsX;
	}

	public int getNumWellsY()
	{
		return numWellsY;
	}
	
	public void setNumWellsY(int numWellsY)
	{
		this.numWellsY = numWellsY;
	}

	public double getWellWidth()
	{
		return wellWidth;
	}
	
	public void setWellWidth(double wellWidth)
	{
		this.wellWidth =  wellWidth;
	}

	public double getWellHeight()
	{
		return wellHeight;
	}
	
	public void setWellHeight(double wellHeight)
	{
		this.wellHeight =  wellHeight;
	}

	public void setCustomMicroplateName(String customMicroplateName)
	{
		this.customMicroplateName = customMicroplateName;
	}

	public String getCustomMicroplateName()
	{
		return customMicroplateName;
	}

}
