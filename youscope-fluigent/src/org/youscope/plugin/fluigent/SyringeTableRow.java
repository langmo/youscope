/**
 * 
 */
package org.youscope.plugin.fluigent;

/**
 * @author Moritz Lang
 *
 */
class SyringeTableRow implements Comparable<SyringeTableRow>
{
	public long time;
	public final double[] flowRates;
	public SyringeTableRow(long time, int numFlowUnits)
	{
		this.time = time;
		this.flowRates = new double[numFlowUnits];
		for(int i=0; i<flowRates.length; i++)
		{
			flowRates[i] = 0;
		}
	}
	@Override
	public int compareTo(SyringeTableRow arg0)
	{
		if(arg0 == null)
			return 1;
		else if(arg0.time > time)
			return -1;
		else if(arg0.time < time)
			return 1;
		else
			return 0;
	}
}
