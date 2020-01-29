package org.youscope.plugin.measurementviewer;

import java.util.Arrays;

class ImageNumber implements Comparable<ImageNumber>
{
	private final long imageNumber[];
	public ImageNumber(long... imageNumber) 
	{
		this.imageNumber = imageNumber;
	}
	long[] toArray()
	{
		return imageNumber;
	}
	
	@Override
	public int compareTo(ImageNumber o)
	{
		for(int i=0; i<imageNumber.length && i < o.imageNumber.length; i++)
		{
			if(imageNumber[i]<o.imageNumber[i])
				return -1;
			else if(imageNumber[i]>o.imageNumber[i])
				return 1;
		}
		if(imageNumber.length < o.imageNumber.length)
			return -1;
		else if(imageNumber.length < o.imageNumber.length)
			return 1;
		else
			return 0;
	}
	
	@Override
	public String toString()
	{
		if(imageNumber.length <= 0)
			return "unknown";
		else if(imageNumber.length == 1)
			return Long.toString(imageNumber[0]);
		StringBuilder builder = new StringBuilder(Long.toString(imageNumber[0]));
		for(int i=1; i<imageNumber.length; i++)
		{
			builder.append(".");
			builder.append(Long.toString(imageNumber[i]));
		}
		return builder.toString();
		
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(imageNumber);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ImageNumber other = (ImageNumber) obj;
		if (!Arrays.equals(imageNumber, other.imageNumber))
			return false;
		return true;
	}

}
