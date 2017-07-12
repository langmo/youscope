package org.youscope.plugin.imagesubstraction;

import org.youscope.common.image.ImageEvent;

/**
 * Helper functions to divide two images
 * @author Moritz Lang
 *
 */
class ImageSubstractionTools {

	private ImageSubstractionTools() {
		// all methods static
	}
	public static ImageEvent<?> divideImages(ImageEvent<?> image1, ImageEvent<?> image2) throws Exception 
	{
		// Check if same type
			int bands = image1.getBands();
			int bytesPerPixel = image1.getBytesPerPixel();
			int bitDepth = image1.getBitDepth();
			int height = image1.getHeight();
			int width = image1.getWidth();
			if(bands != image2.getBands())
				throw new Exception("Incompatible bands between images.");
			if(bytesPerPixel != image2.getBytesPerPixel())
				throw new Exception("Incompatible bytes per pixel between images.");
			if(bitDepth != image2.getBitDepth())
				throw new Exception("Incompatible bit depths per pixel between images.");
			if(height != image2.getHeight())
				throw new Exception("Incompatible heights between images.");
			if(width != image2.getWidth())
				throw new Exception("Incompatible widths between images.");
		
			if(bands > 1)
				throw new Exception("Only supporting grayscale images");
			if(bytesPerPixel != 1 && bytesPerPixel != 2)
				throw new Exception("Only supporting grayscale images with 1 or 2 bytes per pixel. Current images have " + Integer.toString(bytesPerPixel) + " bytes per pixel.");
			
			double I1;
			double I2;
			if(bytesPerPixel == 1)
			{
				byte[] imgRaw1 = (byte[]) image1.getImageData();
				byte[] imgRaw2 = (byte[]) image2.getImageData();
				byte[] result= new byte[width*height];
				for(int i=0; i<width*height; i++)
				{
					I1 = 0xff & imgRaw1[i];
					I2 = 0xff & imgRaw2[i];
					result[i] = (byte)((short)((Math.log((I1+1)/(I2+1)) / Math.log(2) + bitDepth)/2/bitDepth * (Math.pow(2, 8)-1)));
				}
				ImageEvent<byte[]> image;
				try
				{
					image = ImageEvent.createImage(result, width, height, 8); 
				}
				catch(Exception e)
				{
					throw new Exception("Error creating YouScope image.", e);
				}
				return image;
			}
			// else bytesPerPixel == 2
			short[] imgRaw1 = (short[]) image1.getImageData();
			short[] imgRaw2 = (short[]) image2.getImageData();
			short[] result= new short[width*height];
			for(int i=0; i<width*height; i++)
			{
				I1 = 0xffff & imgRaw1[i];
				I2 = 0xffff & imgRaw2[i];
				result[i] = (short)((int)((Math.log((I1+1)/(I2+1)) / Math.log(2) + bitDepth)/2/bitDepth * (Math.pow(2, 16)-1)));
			}
			ImageEvent<short[]> image;
			try
			{
				image = ImageEvent.createImage(result, width, height, 16); 
			}
			catch(Exception e)
			{
				throw new Exception("Error creating YouScope image.", e);
			}
			return image;
	}
}
