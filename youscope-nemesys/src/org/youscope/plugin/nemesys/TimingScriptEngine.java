/**
 * 
 */
package org.youscope.plugin.nemesys;

import java.util.Arrays;

/**
 * A simplified "script engine" to allow easy Nemesys timing table processing.
 * @author Moritz Lang
 *
 */
class TimingScriptEngine
{
	private final SyringeTableRow[] timings;
	TimingScriptEngine(String script) throws NemesysException
	{
		timings = TimingProcessor.getTimings(script);
		Arrays.sort(timings);
	}
	SyringeTableRow getActiveSettings(long time)
	{
		SyringeTableRow activeTiming = null;
		for(int i=0; i<timings.length; i++)
		{
			if(timings[i].time <= time)
				activeTiming = timings[i];
			else
				break;
		}
		return activeTiming;
	}
}
