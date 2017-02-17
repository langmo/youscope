/*******************************************************************************
 * Copyright (c) 2017 Moritz Lang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Moritz Lang - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package org.youscope.plugin.fluigent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

/**
 * @author Moritz Lang
 *
 */
class TimingProcessor
{
	public static String getProtocol(SyringeTableRow[] timings)
	{
		StringBuilder protocol = new StringBuilder(timings.length * 30);
		for(SyringeTableRow timing : timings)
		{
			protocol.append(timing.time);
			for(double flowRate: timing.flowRates)
			{
				protocol.append(" ");
				protocol.append(flowRate);
			}
			protocol.append("\n");
		}
		return protocol.toString();
	}
	public static SyringeTableRow[] getTimings(String protocol) throws ResourceException
	{
		ArrayList<SyringeTableRow> timings = new ArrayList<SyringeTableRow>();
		BufferedReader reader = new BufferedReader(new StringReader(protocol));
		int numFlowUnits = -1;
		try
		{
			for(String line = reader.readLine(); line != null; line = reader.readLine())
			{
				String[] elements = line.split(" ");
				if(elements.length == 0)
					continue;
				else if(numFlowUnits == -1)
					numFlowUnits = elements.length -1;
				else if(numFlowUnits != elements.length - 1)
					throw new ResourceException("Number of flow units different at different times in the Fluigent protocol.");
				SyringeTableRow timing = new SyringeTableRow(Long.parseLong(elements[0]), numFlowUnits);
				for(int i=0; i<numFlowUnits; i++)
				{
					timing.flowRates[i] = Double.parseDouble(elements[i+1]);
				}
				timings.add(timing);
			}
		}
		catch(IOException e)
		{
			throw new ResourceException("I/O error while parsing the Fluigent protocol.", e);
		}
		catch(NumberFormatException e)
		{
			throw new ResourceException("Could not parse time or flow rate in Fluigent script.", e);
		}
		return timings.toArray(new SyringeTableRow[timings.size()]);
	}
	
}
