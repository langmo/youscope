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
package org.youscope.plugin.slimidentification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class PeakFinder 
{
	public static class Peak implements Comparable<Peak>
	{
		public final int idx;
		public final long val;
		public final int leftIdx;
		public final long leftVal;
		public final int rightIdx;
		public final long rightVal;
		Peak(int idx, long val, int leftIdx, long leftVal, int rightIdx, long rightVal)
		{
			this.idx = idx;
			this.val = val;
			this.leftIdx = leftIdx;
			this.leftVal = leftVal;
			this.rightIdx = rightIdx;
			this.rightVal = rightVal;
		}
		public long getProminence()
		{
			return val - Math.max(leftVal, rightVal);
		}
		@Override
		public int compareTo(Peak o) {
			return idx-o.idx;
		}
	}
	
	public static List<Peak> findPeaks(long[] lineProfile, int maxNumPeaks)
	{
		List<Peak> peaks = findPeaks(lineProfile);
		if(peaks.size() <= maxNumPeaks)
			return peaks;
		Collections.sort(peaks, new Comparator<Peak>() 
		{
			@Override
			public int compare(Peak o1, Peak o2) 
			{
				return (int) (o2.getProminence()-o1.getProminence());
			}
		});
		ArrayList<Peak> result = new ArrayList<Peak>(maxNumPeaks);
		for(int i=0; i<maxNumPeaks; i++)
		{
			result.add(peaks.get(i));
		}
		Collections.sort(result);
		return result;
	}
	
	public static List<Peak> findPeaks(long[] lineProfile)
	{
		ArrayList<Peak> peaks = new ArrayList<>();
		
		for(int idx = 1; idx < lineProfile.length-1; idx++)
		{
			if(lineProfile[idx-1]>=lineProfile[idx] || lineProfile[idx+1]>lineProfile[idx])
				continue;
			long val = lineProfile[idx];
			
			// left prevalence
			int left =-1;
			long leftVal = Long.MAX_VALUE;
			for(int i=idx-1; i>=0; i--)
			{
				if(lineProfile[i]>lineProfile[idx])
					break;
				if(lineProfile[i]<leftVal)
				{
					left = i;
					leftVal = lineProfile[i];
				}
			}
			if(left<0 || leftVal >= val)
				continue;
			
			// right prevalence
			int right =-1;
			long rightVal = Long.MAX_VALUE;
			for(int i=idx+1; i<lineProfile.length; i++)
			{
				if(lineProfile[i]>lineProfile[idx])
					break;
				if(lineProfile[i]<rightVal)
				{
					right = i;
					rightVal = lineProfile[i];
				}
			}
			if(rightVal >= val)
				continue;
			
			peaks.add(new Peak(idx,val,left,leftVal,right,rightVal));
		}
		
		
		
		return peaks;
	}
}
