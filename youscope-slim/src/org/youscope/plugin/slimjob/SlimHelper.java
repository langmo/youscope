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
package org.youscope.plugin.slimjob;

import java.util.Arrays;

import org.youscope.common.image.ImageEvent;

class SlimHelper 
{
	private SlimHelper()
	{
		// static functions only
	}
	public static ImageEvent<short[]> calculateSlimImage(ImageEvent<?>[] images, double attenuationFactor) throws Exception
	{
		if(images.length!=4)
			throw new Exception("Expected 4 SLIM images, found " + Integer.toString(images.length));
		// Check if same type
		int bands = images[0].getBands();
		int bytesPerPixel = images[0].getBytesPerPixel();
		int height = images[0].getHeight();
		int width = images[0].getWidth();
		for(int i=1; i<images.length; i++)
		{
			if(bands != images[i].getBands())
				throw new Exception("Incompatible bands between images.");
			if(bytesPerPixel != images[i].getBytesPerPixel())
				throw new Exception("Incompatible bytes per pixel between images.");
			if(height != images[i].getHeight())
				throw new Exception("Incompatible heights between images.");
			if(width != images[i].getWidth())
				throw new Exception("Incompatible widths between images.");
		}
		if(bands > 1)
			throw new Exception("Only supporting grayscale images");
		if(bytesPerPixel != 1 && bytesPerPixel != 2)
			throw new Exception("Only supporting grayscale images with 1 or 2 bytes per pixel. Current images have " + Integer.toString(bytesPerPixel) + " bytes per pixel.");
		Object[] imgRaw = {images[0].getImageData(), images[1].getImageData(), images[2].getImageData(), images[3].getImageData()};
		
		// Get all values of 4U_0^2
		double[] TU0s = new double[width*height]; // = 4U_0^2
		for(int i=0; i<width*height; i++)
		{
			double I0;
			double I1;
			double I2;
			double I3;
			if(bytesPerPixel == 1)
			{
				I0 = 0xff & ((byte[])imgRaw[0])[i];
				I1 = 0xff & ((byte[])imgRaw[1])[i];
				I2 = 0xff & ((byte[])imgRaw[2])[i];
				I3 = 0xff & ((byte[])imgRaw[3])[i];
			}
			else // bytesPerPixel == 2
			{
				I0 = 0xffff & ((short[])imgRaw[0])[i];
				I1 = 0xffff & ((short[])imgRaw[1])[i];
				I2 = 0xffff & ((short[])imgRaw[2])[i];
				I3 = 0xffff & ((short[])imgRaw[3])[i];
			}
			// Calculate the factor beta 
			double S = Math.pow(I0,2)+Math.pow(I1,2)+Math.pow(I2,2)+Math.pow(I3,2);
			double D = Math.pow(Math.pow(I0, 2)-Math.pow(I2, 2), 2) + Math.pow(Math.pow(I3,2)-Math.pow(I1,2),2);
			TU0s[i] = S/2*(1+Math.sqrt(Math.max(0, 1-4*D/Math.pow(S, 2)))); 
		}
		// Take the median of all values of 4U_0^2
		Arrays.sort(TU0s);
		double TU0;
		if (TU0s.length % 2 == 0)
			TU0 = (TU0s[TU0s.length/2] + TU0s[TU0s.length/2 - 1])/2;
		else
			TU0 = TU0s[TU0s.length/2];
		
		TU0 *= attenuationFactor; // Multiply TU_0 with either manual or directly derived attenuation factor
		
		short[] result= new short[width*height];
		
		// calculate phi
		for(int i=0; i<width*height; i++)
		{
			double I0;
			double I1;
			double I2;
			double I3;
			if(bytesPerPixel == 1)
			{
				I0 = 0xff & ((byte[])imgRaw[0])[i];
				I1 = 0xff & ((byte[])imgRaw[1])[i];
				I2 = 0xff & ((byte[])imgRaw[2])[i];
				I3 = 0xff & ((byte[])imgRaw[3])[i];
			}
			else // bytesPerPixel == 2
			{
				I0 = 0xffff & ((short[])imgRaw[0])[i];
				I1 = 0xffff & ((short[])imgRaw[1])[i];
				I2 = 0xffff & ((short[])imgRaw[2])[i];
				I3 = 0xffff & ((short[])imgRaw[3])[i];
			}
			double phi = Math.atan2(Math.pow(I3,2)-Math.pow(I1,2), TU0+(Math.pow(I0,2)-Math.pow(I2,2)));
			
			result[i]=(short)((int)Math.round((phi+Math.PI)/(2*Math.PI)*(Math.pow(2, 16)-1)));
		}
		ImageEvent<short[]> image;
		try
		{
			image = ImageEvent.createImage(result, width, height, 16); 
		}
		catch(Exception e)
		{
			throw new Exception("Error creating YouScope image from SLIM raw image data.", e);
		}
		return image;
	}
}
