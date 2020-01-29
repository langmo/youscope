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
package org.youscope.plugin.measurementviewer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Moritz Lang
 *
 */
class ImageList
{
	private final ArrayList<ImageEntry> images = new ArrayList<ImageEntry>();
	private boolean sorted = false;
	public void add(String imagePath, ImageNumber imageNumber)
	{
		images.add(new ImageEntry(imageNumber, imagePath));
		sorted = false;
	}
	public Collection<? extends ImageNumber> getImageNumbers()
	{
		return images;
	}
	public String get(int index)
	{
		if(!sorted)
		{
			Collections.sort(images);
			sorted = true;
		}
		return images.get(index).path;
	}
	public String getClosest(ImageNumber imageNumber)
	{
		if(images.isEmpty())
			return null;
		if(!sorted)
		{
			Collections.sort(images);
			sorted = true;
		}
		int pos = Collections.binarySearch(images, imageNumber);
		if(pos>= 0)
			return images.get(pos).path;
		// imageNumber is not in the list, and we got the index (encoded) of the first element greater than the imageNumber instead.
		pos = -pos-1;
		// first, check if it's the first element, which means that we only have image numbers greater than the provided one.
		if(pos==0)
			return images.get(0).path;
		// then, if it's the last element, meaning we have to take the last one smaller
		if(pos >= images.size())
			return images.get(images.size()-1).path;
		// now, we have to decide if we take the first greater, or the last smaller than the provided number
		ImageEntry lastSmaller = images.get(pos-1);
		ImageEntry firstGreater = images.get(pos);
		long[] o1 = lastSmaller.toArray();
		long[] o2 = firstGreater.toArray();
		long[] u = imageNumber.toArray();
		
		for(int i=0; i<u.length; i++)
		{
			long d1 = Math.abs((o1.length<i ? o1[i] : -1) - u[i]);
			long d2 = Math.abs((o2.length<i ? o2[i] : -1) - u[i]);
			if(d1 < d2)
				return lastSmaller.path;
			else if(d2>d1)
				return firstGreater.path;
		}
		if(o1.length <= o2.length)
			return lastSmaller.path;
		return firstGreater.path;
	}
	public int size()
	{
		return images.size();
	}
	
	private class ImageEntry extends ImageNumber
	{
		public final String path;
		ImageEntry(ImageNumber imageNumber, String path)
		{
			super(imageNumber.toArray());
			this.path = path;
		}
	}
}
