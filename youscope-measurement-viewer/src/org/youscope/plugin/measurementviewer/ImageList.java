/**
 * 
 */
package org.youscope.plugin.measurementviewer;

import java.util.Collections;
import java.util.LinkedList;

/**
 * @author Moritz Lang
 *
 */
class ImageList
{
	private final LinkedList<ImageEntry> images = new LinkedList<ImageEntry>();
	private boolean sorted = false;
	public void add(String imagePath, int[] imageNumber)
	{
		images.add(new ImageEntry(imageNumber, imagePath)); 
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
	public int size()
	{
		return images.size();
	}
	
	class ImageEntry implements Comparable<ImageEntry>
	{
		public final int position[];
		public final String path;
		ImageEntry(int position[], String path)
		{
			this.position = position;
			this.path = path;
		}
		@Override
		public int compareTo(ImageEntry o)
		{
			for(int i=0; i<position.length && i < o.position.length; i++)
			{
				if(position[i]<o.position[i])
					return -1;
				else if(position[i]>o.position[i])
					return 1;
			}
			if(position.length < o.position.length)
				return -1;
			else if(position.length < o.position.length)
				return 1;
			else
				return 0;
		}
	}
}
