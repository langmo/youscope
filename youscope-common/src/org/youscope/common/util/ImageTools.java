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
package org.youscope.common.util;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import org.youscope.common.image.ImageEvent;

/**
 * Helper functions to transform YouScope images.
 * @author Moritz Lang
 */
public class ImageTools
{

	/**
	 * Converts a BufferedImage to YouScope's internal raw + meta data format.
	 * Note that only a limited set of meta data (width, height, bytes per pixel, bit depth, and number of bands) is set.
	 * @param image The image which should be converted.
	 * @return The resulting image.
	 * @throws ImageConvertException
	 */
	public static ImageEvent<?> toYouScopeImage(BufferedImage image) throws ImageConvertException
	{
		Raster ras = image.getData();
		int bands = ras.getNumBands();
		DataBuffer data = ras.getDataBuffer();
		int bytesPerPixel;
		Object rawData;
		if(data instanceof DataBufferInt)
		{
			bytesPerPixel = 4; // its a set of ints
			rawData = ((DataBufferInt)data).getData();
		}
		else if(data instanceof DataBufferShort)
		{
			bytesPerPixel = 2; // bytes
			rawData = ((DataBufferShort)data).getData();
		}
		else if(data instanceof DataBufferUShort)
		{
			bytesPerPixel = 2; // bytes
			rawData = ((DataBufferUShort)data).getData();
		}
		else if(data instanceof DataBufferByte)
		{
			bytesPerPixel = 1; // bytes
			rawData = ((DataBufferByte)data).getData();
		}
		else
			throw new ImageConvertException("Data is stored in unknown buffer type (" + data.getClass().getName() + ")");

		int bitDepth;
		// TODO: Support for 32bit grayscale images?
		if(bands > 1 || bytesPerPixel == 4)
		{
			bitDepth = 8;
		}
		else
		{
			bitDepth = 8 * bytesPerPixel;
		}
		ImageEvent<?> result;
		try
		{
			result = ImageEvent.createImage(rawData, image.getWidth(), image.getHeight(), bitDepth);
		}
		catch(Exception e)
		{
			throw new ImageConvertException("Error creating YouScope image from buffered image.", e);
		}
		result.setBands(bands);

		return result;
	}

	/**
	 * Creates an image from microscope supplied data, and saves it in the provided image.
	 * Note that a new image is created if the provided image has a different size or type than the image event.
	 * @param imageEvent The raw image data and metadata supplied by the microscope.
	 * @param bufferedImage The image in which the data should be saved in, or null to create a new image.
	 * @return created Image.
	 * @throws ImageConvertException
	 */
	public static BufferedImage getMicroscopeImage(ImageEvent<?> imageEvent, BufferedImage bufferedImage) throws ImageConvertException
	{
		return getScaledMicroscopeImage(imageEvent, 0.0F, 1.0F, bufferedImage);
	}

	/**
	 * Creates an image from microscope supplied data.
	 * @param imageEvent The raw image data and metadata supplied by the microscope.
	 * @return created Image.
	 * @throws ImageConvertException
	 */
	public static BufferedImage getMicroscopeImage(ImageEvent<?> imageEvent) throws ImageConvertException
	{
		return getScaledMicroscopeImage(imageEvent, 0.0F, 1.0F, null);
	}

	/**
	 * Creates an image from microscope supplied data. Furthermore the image is scaled such that all
	 * lowerCutoff * 100% of the colors become black and the upperCutoff * 100% of the colors become white.
	 * @param imageEvent The raw image data and metadata supplied by the microscope.
	 * @param lowerCutoff The lower cutoff for pixel grayness.
	 * @param upperCutoff The upper cutoff for pixel grayness.
	 * @return created Image.
	 * @throws ImageConvertException
	 */
	public static BufferedImage getScaledMicroscopeImage(ImageEvent<?> imageEvent, float lowerCutoff, float upperCutoff) throws ImageConvertException
	{
		return getScaledMicroscopeImage(imageEvent, lowerCutoff, upperCutoff, null);
	}
	
	/**
	 * Clones a buffered image. The two images are independent, i.e. changes in one do not affect the other.
	 * @param bufferedImage The image to clone.
	 * @return the cloned image.
	 */
	public static BufferedImage cloneImage(BufferedImage bufferedImage) 
	{
		 ColorModel cm = bufferedImage.getColorModel();
		 boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		 WritableRaster raster = bufferedImage.copyData(null);
		 return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}

	/**
	 * Returns a scaled version of the image. Only supports 8 and 16bit grayscale images. For all other images, returns the original image.
	 * @param bufferedImage The buffered image to transform.
	 * @param lowerCutoff Lower cutoff (0<=lowerCutoff<upperCutoff<=1).
	 * @param upperCutoff Upper cutoff (0<=lowerCutoff<upperCutoff<=1)
	 * @param returnVal Image to save the result in, or null to create new image. Note: the function might save the resulting image in this variable, or might not, depending on if returnVal is suited.
	 * @return The scaled image, or the original image if image could not be scaled.
	 */
	public static BufferedImage getScaledImage(BufferedImage bufferedImage, float lowerCutoff, float upperCutoff, BufferedImage returnVal)
	{
		if(lowerCutoff > upperCutoff)
		{
			float temp = lowerCutoff;
			lowerCutoff = upperCutoff;
			upperCutoff = temp;
		}
		if(lowerCutoff < 0)
			lowerCutoff = 0;
		if(upperCutoff > 1)
			upperCutoff = 1;
		if(lowerCutoff == 0.0 && upperCutoff == 1.0)
			return bufferedImage;
		long maxValue = getMaximalExpectedPixelValue(bufferedImage);
		if(maxValue <=0)
			return bufferedImage;
		
		if(returnVal == null || returnVal.getWidth() != bufferedImage.getWidth() || returnVal.getHeight() != bufferedImage.getHeight() || returnVal.getType() != bufferedImage.getType() || returnVal.getRaster().getTransferType() != bufferedImage.getRaster().getTransferType() || returnVal.getRaster().getNumDataElements() != bufferedImage.getRaster().getNumDataElements())
		{
			returnVal = cloneImage(bufferedImage);
		}
		
		double scaleFactor = 1.0 / (upperCutoff - lowerCutoff);
		int offset = (int)(-lowerCutoff * maxValue);
		int highestValue = (int)(maxValue / scaleFactor - offset);
		int lowestValue = -offset;

		int width = bufferedImage.getWidth();
		int height = bufferedImage.getHeight();
		
		WritableRaster raster = returnVal.getRaster();
		WritableRaster orgRaster = bufferedImage.getRaster();
		int imageType = returnVal.getType();
		int dataType = bufferedImage.getRaster().getTransferType();
		int numElementsPerPixel = bufferedImage.getRaster().getNumDataElements();
		if(dataType==DataBuffer.TYPE_BYTE && imageType == BufferedImage.TYPE_BYTE_GRAY && numElementsPerPixel==1)
		{
			byte[] imageData = new byte[1];
			int value;
			for(int x = 0; x < width; x++)
			{
				for(int y = 0; y < height; y++)
				{
					orgRaster.getDataElements(x, y, imageData);
					value = imageData[0] & 0xff;
					if(value <= lowestValue)
						imageData[0] = 0;
					else if(value >= highestValue)
						imageData[0] = (byte) maxValue;
					else
						imageData[0] = (byte) ((value + offset) * scaleFactor);
					
					raster.setDataElements(x, y, imageData);
				}
			}
		}
		else if((dataType==DataBuffer.TYPE_SHORT || dataType==DataBuffer.TYPE_USHORT)&&imageType == BufferedImage.TYPE_USHORT_GRAY && numElementsPerPixel==1)
		{
			short[] imageData = new short[1];
			int value;
			for(int x = 0; x < width; x++)
			{
				for(int y = 0; y < height; y++)
				{
					orgRaster.getDataElements(x, y, imageData);
					value = imageData[0] & 0xffff;
					if(value <= lowestValue)
						imageData[0] = 0;
					else if(value >= highestValue)
						imageData[0] = (short) maxValue;
					else
						imageData[0] = (short) ((value + offset) * scaleFactor);
					
					raster.setDataElements(x, y, imageData);
				}
			}
		}
		else if(imageType == BufferedImage.TYPE_CUSTOM && dataType==DataBuffer.TYPE_BYTE && numElementsPerPixel >= 3)
		{
			byte[] imageData = new byte[numElementsPerPixel];
			int value;
			for(int x = 0; x < width; x++)
			{
				for(int y = 0; y < height; y++)
				{
					orgRaster.getDataElements(x, y, imageData);
					for(int i=0; i<3; i++)
					{
						value = imageData[i] & 0xff;
						if(value <= lowestValue)
							imageData[i] = 0;
						else if(value >= highestValue)
							imageData[i] = (byte) maxValue;
						else
							imageData[i] = (byte) ((value + offset) * scaleFactor);
					}
					raster.setDataElements(x, y, imageData);
				}
			}
		}
		else
			returnVal = bufferedImage;
		return returnVal;
	}
	
	/**
	 * Returns the intensity of the pixel at the (zero based) x and y position in original coordinates.
	 * Use backTransformCoordinate() if only transformed coordinates are available.
	 * Method does only work if image is a 1 or 2 byte grayscale image.
	 * @param image The image from which the pixel intensity should be extracted.
	 * @param x The x position of the pixel (0<=x<image.getWidth()).
	 * @param y The y position of the pixel (0<=y<image.getHeight()).
	 * @return The pixel intensity at the specific position, or -1 if position is invalid or image is not a 1 or 2 byte grayscale image.
	 */
	public static long getPixelValue(ImageEvent<?> image, int x, int y)
	{
		if(image == null || x < 0 || x >= image.getWidth() || y < 0 || y >= image.getHeight())
			return -1;
		int bytesPerPixel = image.getBytesPerPixel();
		int arrayPos = x + y * image.getWidth();
		if(bytesPerPixel == 1)// && bands == 1)
		{
			byte pixelValue = ((byte[])image.getImageData())[arrayPos];
			return (pixelValue & 0xff);
		}
		else if(bytesPerPixel == 2)// && bands == 1)
		{
			short pixelValue = ((short[])image.getImageData())[arrayPos];
			return (pixelValue & 0xffff);
		}
		else
			return -1;
	}

	/**
	 * Translates the (zero based) coordinates in the not transposed and not switched coordinate system (as the image is obtained from the
	 * camera) to the new coordinate system, in which the image axes are eventually transposed and/or switched.
	 * @param image The image in which the coordinates are given.
	 * @param orgCoords The original coordinates.
	 * @return the switched coordinates, or null if the original coordinates were invalid.
	 */
	public static Point transformCoordinate(ImageEvent<?> image, Point orgCoords)
	{
		if(image == null || orgCoords == null || orgCoords.x < 0 || orgCoords.x >= image.getWidth() || orgCoords.y < 0 || orgCoords.y > image.getHeight())
		{
			return null;
		}

		Point transCoords = new Point(orgCoords);
		if(image.isTransposeX())
			transCoords.x = (image.getWidth() - orgCoords.x - 1);
		if(image.isTransposeY())
			transCoords.y = (image.getHeight() - orgCoords.y - 1);
		if(image.isSwitchXY())
		{
			int oldX = transCoords.x;
			int oldY = transCoords.y;
			transCoords.x = oldY;
			transCoords.y = oldX;
		}
		return transCoords;
	}

	/**
	 * Inverse operation to transformCoordinate().
	 * Takes a point corresponding to a pixel position in the transformed image, and returns the original coordinates.
	 * @param image The image in which the coordinates are given.
	 * @param transCoords The transformed coordinates
	 * @return the original coordinates, or null if the transformed coordinates were invalid.
	 */
	public static Point backTransformCoordinate(ImageEvent<?> image, Point transCoords)
	{
		if(image == null || transCoords == null)
			return null;

		Point orgCoords = new Point(transCoords);
		if(image.isSwitchXY())
		{
			orgCoords.x = transCoords.y;
			orgCoords.y = transCoords.x;
		}
		if(image.isTransposeX())
			orgCoords.x = (image.getWidth() - orgCoords.x - 1);
		if(image.isTransposeY())
			orgCoords.y = (image.getHeight() - orgCoords.y - 1);
		if(orgCoords.x < 0 || orgCoords.x >= image.getWidth() || orgCoords.y < 0 || orgCoords.y > image.getHeight())
		{
			return null;
		}
		return orgCoords;
	}

	/**
	 * Creates an image from microscope supplied data, and saves it in the provided image.
	 * Note that a new image is created if the provided image has a different size or type than the image event.
	 * Furthermore the image is scaled such that all
	 * lowerCutoff * 100% of the colors become black and the upperCutoff * 100% of the colors become white.
	 * @param imageEvent The raw image data and metadata supplied by the microscope.
	 * @param lowerCutoff The lower cutoff for pixel grayness.
	 * @param upperCutoff The upper cutoff for pixel grayness.
	 * @param bufferedImage The image in which the data should be saved in, or null to create a new image.
	 * @return created Image.
	 * @throws ImageConvertException
	 */
	public static BufferedImage getScaledMicroscopeImage(ImageEvent<?> imageEvent, float lowerCutoff, float upperCutoff, BufferedImage bufferedImage) throws ImageConvertException
	{
		int bands = imageEvent.getBands();
		int bytesPerPixel = imageEvent.getBytesPerPixel();
		int width = imageEvent.getWidth();
		int height = imageEvent.getHeight();
		int bitDepth = imageEvent.getBitDepth();
		if(bands == 3)
		{
			// We ignore the alpha channel anyway, thus only makes live easier.
			bands = 4;
		}
		if(lowerCutoff > upperCutoff)
		{
			// lower cuttof must be lower than higher one.
			float temp = lowerCutoff;
			lowerCutoff = upperCutoff;
			upperCutoff = temp;
		}
		else if(lowerCutoff == upperCutoff)
		{
			// prevent dividing by zero
			if(lowerCutoff > 0.01)
				lowerCutoff -= 0.01;
			else
				upperCutoff += 0.01;
		}

		int pixelWith;
		if(bytesPerPixel == 1)// && bands == 1)
		{
			byte[] imageData = (byte[])imageEvent.getImageData();
			if(bufferedImage == null || bufferedImage.getWidth() != width || bufferedImage.getHeight() != height || bufferedImage.getType() != BufferedImage.TYPE_BYTE_GRAY)
				bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
			WritableRaster wr = bufferedImage.getRaster();
			wr.setDataElements(0, 0, width, height, imageData);
			pixelWith = Byte.MAX_VALUE - Byte.MIN_VALUE + 1;
			// Some cameras have the error to claim to be 1 or 2 byte/pixel RGBA images.
			// If there is however only one or two bytes, we can safely assume that it is a grayscale image.
			bands = 1;

		}
		else if(bytesPerPixel == 2)// && bands == 1)
		{
			short[] imageData = (short[])imageEvent.getImageData();
			if(bufferedImage == null || bufferedImage.getWidth() != width || bufferedImage.getHeight() != height || bufferedImage.getType() != BufferedImage.TYPE_USHORT_GRAY)
				bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_USHORT_GRAY);
			WritableRaster wr = bufferedImage.getRaster();
			wr.setDataElements(0, 0, width, height, imageData);
			pixelWith = Short.MAX_VALUE - Short.MIN_VALUE + 1;

			// Some cameras have the error to claim to be 1 or 2 byte/pixel RGBA images.
			// If there is however only one or two bytes, we can safely assume that it is a grayscale image.
			bands = 1;
		}
		else if(bytesPerPixel == 4)// && (bands == 4 || bands == 3))
		{
			int[] imageData = (int[])imageEvent.getImageData();
			if(bufferedImage == null || bufferedImage.getWidth() != width || bufferedImage.getHeight() != height || bufferedImage.getType() != BufferedImage.TYPE_INT_RGB)
				bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			WritableRaster wr = bufferedImage.getRaster();
			wr.setDataElements(0, 0, width, height, imageData);
			// Total image has 32bit and 4 bands, every band thus has 8 bit.
			pixelWith = Byte.MAX_VALUE - Byte.MIN_VALUE + 1;
		}
		else
		{
			throw new ImageConvertException("Can only interpret grayscale images with 1 or 2 byte per pixel or color (RGBA) images with 4 bytes. This image has " + Integer.toString(bytesPerPixel) + " bytes per pixel and " + Integer.toString(bands) + " bands!");
		}

		// Adjust gray levels.
		if(bitDepth != bytesPerPixel * 8 / bands || lowerCutoff != 0.0 || upperCutoff != 1.0)
		{
			double scaleFactor = Math.pow(2, bytesPerPixel * 8 / bands - bitDepth) * 1 / (upperCutoff - lowerCutoff);
			int offset = (int)(-1 / scaleFactor * lowerCutoff / (upperCutoff - lowerCutoff) * pixelWith);
			int highestValueEnd = (int)(Math.pow(2, bytesPerPixel * 8 / bands) - 1);
			int highestValue = (int)(highestValueEnd / scaleFactor - offset);
			int lowestValue = -offset;

			WritableRaster raster = bufferedImage.getRaster();
			for(int band = 0; band < raster.getNumBands() && band < 3; band++)
			{
				for(int i = 0; i < width; i++)
				{
					for(int j = 0; j < height; j++)
					{
						int value = raster.getSample(i, j, band);
						if(value <= lowestValue)
							raster.setSample(i, j, band, 0);
						else if(value >= highestValue)
							raster.setSample(i, j, band, highestValueEnd);
						else
							raster.setSample(i, j, band, (value + offset) * scaleFactor);
					}
				}
			}
		}
		// Switch coordinate systems
		if(imageEvent.isSwitchXY() || imageEvent.isTransposeX() || imageEvent.isTransposeY())
		{
			AffineTransform transform = new AffineTransform();
			if(imageEvent.isSwitchXY())
				transform.concatenate(new AffineTransform(0, 1, 1, 0, 0, 0));
			if(imageEvent.isTransposeX())
				transform.concatenate(new AffineTransform(-1, 0, 0, 1, 0, 0));
			if(imageEvent.isTransposeY())
				transform.concatenate(new AffineTransform(1, 0, 0, -1, 0, 0));

			AffineTransformOp transformOp = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			Rectangle2D transformRect = transformOp.getBounds2D(bufferedImage);

			AffineTransform imageTransform = new AffineTransform();
			imageTransform.translate(-transformRect.getX(), -transformRect.getY());
			imageTransform.concatenate(transform);

			transformOp = new AffineTransformOp(imageTransform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			bufferedImage = transformOp.filter(bufferedImage, null);
		}
		return bufferedImage;
	}

	/**
	 * Returns the intensity of the pixel at the (zero based) x and y position in original coordinates.
	 * Method does only work if image is a 1 or 2 byte grayscale image.
	 * @param bufferedImage The image from which the pixel intensity should be extracted.
	 * @param x The x position of the pixel (0<=x<image.getWidth()).
	 * @param y The y position of the pixel (0<=y<image.getHeight()).
	 * @return The pixel intensity at the specific position, or -1 if position is invalid or image is not a 1 or 2 byte grayscale image.
	 */
	public static long getPixelValue(BufferedImage bufferedImage, int x, int y)
	{
		if(x<0 || x>=bufferedImage.getWidth() || y < 0 || y>= bufferedImage.getHeight())
			return -1;
		int imageType = bufferedImage.getType();
		if(imageType == BufferedImage.TYPE_BYTE_GRAY)
		{
			byte[] imageData = new byte[1];
			bufferedImage.getRaster().getDataElements(x, y, imageData);
			return imageData[0] & 0xff;
		}
		else if(imageType == BufferedImage.TYPE_USHORT_GRAY)
		{
			short[] imageData = new short[1];
			bufferedImage.getRaster().getDataElements(x, y, imageData);
			return imageData[0] & 0xffff;
		}
		return -1;
	}
	
	/**
	 * Returns the maximal intensity a pixel actually has in the image.
	 * Method does only work if image is a 1 or 2 byte grayscale image.
	 * @param imageEvent The image from which the maximal pixel intensity should be returned.
	 * @return The maximal pixel intensity , or -1 if image is not a 1 or 2 byte grayscale image.
	 */
	public static long getMaximalPixelValue(ImageEvent<?> imageEvent)
	{
		int bytesPerPixel = imageEvent.getBytesPerPixel();
		if(bytesPerPixel == 1)// && bands == 1)
		{
			byte[] imageData = (byte[])imageEvent.getImageData();
			short maxValue = 0;
			for(byte pixel : imageData)
			{
				short value = (short) (pixel & 0xff);
				if(value > maxValue)
					maxValue = value;
			}
			return maxValue;
		}
		else if(bytesPerPixel == 2)
		{
			short[] imageData = (short[])imageEvent.getImageData();
			int maxValue = 0;
			for(short pixel : imageData)
			{
				int value = (short) (pixel & 0xffff);
				if(value > maxValue)
					maxValue = value;
			}
			return maxValue;
		}
		else
			return -1;
	}
	
	/**
	 * Returns the maximal intensity a pixel actually has in the image.
	 * Method does only work if image is a 1 or 2 byte grayscale image.
	 * @param bufferedImage The image from which the maximal pixel intensity should be returned.
	 * @return The maximal pixel intensity , or -1 if image is not a 1 or 2 byte grayscale image.
	 */
	public static long getMaximalPixelValue(BufferedImage bufferedImage)
	{
		Raster ras = bufferedImage.getData();
		DataBuffer data = ras.getDataBuffer();
		if(data instanceof DataBufferInt)
		{
			long maxValue = 0;
			for(int valueRaw : ((DataBufferInt)data).getData())
			{
				long value = valueRaw & 0xffffffff;
				if(value > maxValue)
					maxValue = value;
			}
			return maxValue;
		}
		else if(data instanceof DataBufferShort)
		{
			int maxValue = 0;
			for(short valueRaw : ((DataBufferShort)data).getData())
			{
				int value = valueRaw & 0xffff;
				if(value > maxValue)
					maxValue = value;
			}
			return maxValue;
		}
		else if(data instanceof DataBufferUShort)
		{
			int maxValue = 0;
			for(short valueRaw : ((DataBufferUShort)data).getData())
			{
				int value = valueRaw & 0xffff;
				if(value > maxValue)
					maxValue = value;
			}
			return maxValue;
		}
		else if(data instanceof DataBufferByte)
		{
			short maxValue = 0;
			for(byte valueRaw : ((DataBufferByte)data).getData())
			{
				short value = (short) (valueRaw & 0xff);
				if(value > maxValue)
					maxValue = value;
			}
			return maxValue;
		}
		else
			return -1;
	}
	/**
	 * Returns the maximal intensity a pixel can theoretically have in the image.
	 * Method does only work if image is a 1 or 2 byte grayscale image.
	 * @param bufferedImage The image from which the maximal pixel intensity should be returned.
	 * @return The maximal pixel intensity , or -1 if image is not a 1 or 2 byte grayscale image.
	 */
	public static long getMaximalExpectedPixelValue(BufferedImage bufferedImage)
	{
		int imageType = bufferedImage.getType();
		int dataType = bufferedImage.getRaster().getTransferType();
		int numElementsPerPixel = bufferedImage.getRaster().getNumDataElements();
		if(dataType==DataBuffer.TYPE_BYTE && imageType == BufferedImage.TYPE_BYTE_GRAY && numElementsPerPixel==1)
		{
			return (long) (Math.pow(2, 8)-1);
		}
		else if((dataType==DataBuffer.TYPE_SHORT || dataType==DataBuffer.TYPE_USHORT)&&imageType == BufferedImage.TYPE_USHORT_GRAY && numElementsPerPixel==1)
		{
			return(long) (Math.pow(2, 16)-1);
		}
		else if(imageType == BufferedImage.TYPE_CUSTOM && dataType==DataBuffer.TYPE_BYTE && numElementsPerPixel >= 3)
		{
			return (long) (Math.pow(2, 8)-1);
		}
		else
			return -1;
	}
	/**
	 * Returns the maximal intensity a pixel can theoretically have in the image.
	 * @param imageEvent The image from which the maximal pixel intensity should be returned.
	 * @return The maximal pixel intensity.
	 */
	public static long getMaximalExpectedPixelValue(ImageEvent<?> imageEvent)
	{
		return(long) (Math.pow(2, imageEvent.getBitDepth())-1);
		}
	
	/**
	 * Generates a histogram for the given image.
	 * @param bufferedImage The buffered image, either stored as byte or short gray image, or int RGB/BGR image.
	 * @param numBins The number of bins the histogram should have.
	 * @return histogram.
	 * @throws ImageConvertException
	 */
	public static int[][] getHistogram(BufferedImage bufferedImage, int numBins) throws ImageConvertException
	{
		final int width = bufferedImage.getWidth();
		final int height = bufferedImage.getHeight();
		int imageType = bufferedImage.getType();
		int dataType = bufferedImage.getRaster().getTransferType();
		int numElementsPerPixel = bufferedImage.getRaster().getNumDataElements();
		if(dataType==DataBuffer.TYPE_BYTE && (imageType == BufferedImage.TYPE_BYTE_GRAY || imageType == BufferedImage.TYPE_BYTE_INDEXED) && numElementsPerPixel==1)
		{
			int bands = 1;
			int[][] bins = new int[bands][numBins];
			int maxExpectValue = (int) (Math.pow(2, 8)-1);
			
			byte[] imageData = new byte[width * height];
			bufferedImage.getRaster().getDataElements(0, 0, width, height, imageData);
			
			for(byte pixel : imageData)
			{
				int binID = (pixel & 0xff) * numBins / maxExpectValue;
				if(binID < numBins)
					bins[0][binID]++;
				else
					bins[0][numBins - 1]++;
			}
			return bins;
			
		}
		else if((dataType==DataBuffer.TYPE_SHORT || dataType==DataBuffer.TYPE_USHORT)&&imageType == BufferedImage.TYPE_USHORT_GRAY && numElementsPerPixel==1)
		{
			int bands = 1;
			int[][] bins = new int[bands][numBins];
			int maxExpectValue = (int) (Math.pow(2, 16)-1);
			short[] imageData = new short[width * height];
			bufferedImage.getRaster().getDataElements(0, 0, width, height, imageData);
			for(short pixel : imageData)
			{
				int binID = (pixel & 0xffff) * numBins / maxExpectValue;
				if(binID < numBins)
					bins[0][binID]++;
				else
					bins[0][numBins - 1]++;
			}
			return bins;
		}
		else if(dataType==DataBuffer.TYPE_INT&&(imageType == BufferedImage.TYPE_INT_BGR
				||imageType == BufferedImage.TYPE_INT_RGB
				|| imageType == BufferedImage.TYPE_INT_ARGB) && numElementsPerPixel==1)
		{
			int bands = 3;
			int[][] bins = new int[bands][numBins];
			int maxExpectValue = (int) (Math.pow(2, 8)-1);
			
			int[] imageData = new int[width * height];
			bufferedImage.getRaster().getDataElements(0, 0, width, height, imageData);
			
			for(int pixel : imageData)
			{
				// Always cal
				for(int band = 0; band < bands; band++)
				{
					int binID;
					switch(band)
					{
						case 0:
							// Red
							binID = ((pixel & 0xFF000000) >>> 24) * numBins / maxExpectValue;
							break;
						case 1:
							// Green
							binID = ((pixel & 0x00FF0000) >>> 16) * numBins / maxExpectValue;
							break;
						case 2: 
							// Blue
							binID = ((pixel & 0x0000FF00) >>> 8) * numBins / maxExpectValue;
							break;
						case 3:
							// Alpha
							binID = ((pixel & 0x000000FF) >>> 0) * numBins / maxExpectValue;
							break;
						default:
							// This case should not happen...
							continue;
					}

					if(binID >= 0 && binID < numBins)
						bins[band][binID]++;
					else if(binID >= numBins)
						bins[band][numBins - 1]++;
					else
						bins[band][0]++;
				}
			}
			return bins;
		}
		else if((imageType == BufferedImage.TYPE_CUSTOM )&& dataType==DataBuffer.TYPE_BYTE && numElementsPerPixel >= 3)
		{
			
			int bands = 3;
			int[][] bins = new int[bands][numBins];
			int maxExpectValue = (int) (Math.pow(2, 8)-1);
			
			byte[] imageData = new byte[width * height * numElementsPerPixel];
			bufferedImage.getRaster().getDataElements(0, 0, width, height, imageData);
			
			for(int i=0; i<imageData.length; i+=numElementsPerPixel)
			{
				// Always cal
				for(int band = 0; band < bands; band++)
				{
					int binID = (imageData[i+band] & 0xFF) * numBins / maxExpectValue;

					if(binID >= 0 && binID < numBins)
						bins[band][binID]++;
					else if(binID >= numBins)
						bins[band][numBins - 1]++;
					else
						bins[band][0]++;
				}
			}
			// assume BGR instead of RGB. YouScope assumes RGB
			int[] temp = bins[0];
			bins[0] = bins[2];
			bins[2] = temp;
			return bins;
		}
		else 
		{
			throw new ImageConvertException("Can only interpret grayscale images with 1 or 2 byte per pixel and RGBA images with 4 bytes. This image type id was "+ Integer.toString(imageType)+", data buffer transfer type "+Integer.toString(dataType)+", and elements per pixel "+Integer.toString(numElementsPerPixel)+".");
		}
	}
	
	/**
	 * Generates a histogram for the given image.
	 * @param imageEvent The raw image data and metadata supplied by the microscope.
	 * @param numBins The number of bins the histogram should have.
	 * @return histogram.
	 * @throws ImageConvertException
	 */
	public static int[][] getHistogram(ImageEvent<?> imageEvent, int numBins) throws ImageConvertException
	{
		int bands = imageEvent.getBands();
		int bytesPerPixel = imageEvent.getBytesPerPixel();
		int bitDepth = imageEvent.getBitDepth();
		int maxExpectValue = (int)Math.pow(2, bitDepth);
		if(bytesPerPixel == 1)// && bands == 1)
		{
			int[][] bins = new int[1][numBins];
			byte[] imageData = (byte[])imageEvent.getImageData();
			for(byte pixel : imageData)
			{
				int binID = (pixel & 0xff) * numBins / maxExpectValue;
				if(binID < numBins)
					bins[0][binID]++;
				else
					bins[0][numBins - 1]++;
			}
			return bins;
		}
		else if(bytesPerPixel == 2)
		{
			int[][] bins = new int[1][numBins];
			short[] imageData = (short[])imageEvent.getImageData();
			for(short pixel : imageData)
			{
				int binID = (pixel & 0xffff) * numBins / maxExpectValue;
				if(binID < numBins)
					bins[0][binID]++;
				else
					bins[0][numBins - 1]++;
			}
			return bins;
		}
		else if(bytesPerPixel == 4 && (bands == 4 || bands == 3))
		{
			int[][] bins = new int[bands][numBins];
			int[] imageData = (int[])imageEvent.getImageData();
			for(int pixel : imageData)
			{
				// Always cal
				for(int band = 0; band < bands; band++)
				{
					int binID;
					switch(band)
					{
						case 0:
							// Red
							binID = ((pixel & 0xFF000000) >>> 24) * numBins / maxExpectValue;
							break;
						case 1:
							// Green
							binID = ((pixel & 0x00FF0000) >>> 16) * numBins / maxExpectValue;
							break;
						case 2:
							// Blue
							binID = ((pixel & 0x0000FF00) >>> 8) * numBins / maxExpectValue;
							break;
						case 3:
							// Alpha
							binID = ((pixel & 0x000000FF) >>> 0) * numBins / maxExpectValue;
							break;
						default:
							// This case should not happen...
							continue;
					}

					if(binID >= 0 && binID < numBins)
						bins[band][binID]++;
					else if(binID >= numBins)
						bins[band][numBins - 1]++;
					else
						bins[band][0]++;
				}
			}
			return bins;
		}
		else
		{
			throw new ImageConvertException("Can only interpret grayscale images with 1 or 2 byte per pixel and RGBA images with 4 bytes. This image has " + Integer.toString(bytesPerPixel) + " bytes per pixel and " + Integer.toString(bands) + " bands.");
		}
	}

	/**
	 * Creates an image from microscope supplied data. The grayscale values of each pixel is scaled such that the lightest pixels are white and the darkest black.
	 * @param imageEvent The raw image data and metadata supplied by the microscope.
	 * @return Created image.
	 * @throws ImageConvertException
	 */
	public static BufferedImage getScaledMicroscopeImage(ImageEvent<?> imageEvent) throws ImageConvertException
	{
		int bytesPerPixel = imageEvent.getBytesPerPixel();
		int bitDepth = imageEvent.getBitDepth();
		if(bytesPerPixel == 1)
		{
			byte[] imageData = (byte[])imageEvent.getImageData();
			byte minVal = Byte.MAX_VALUE;
			byte maxVal = 0;
			for(byte val : imageData)
			{
				if(val < minVal)
					minVal = val;
				if(val > maxVal)
					maxVal = val;
			}
			float upperBound = maxVal / Byte.MAX_VALUE * bytesPerPixel * 8 / bitDepth;
			float lowerBound = minVal / Byte.MAX_VALUE * bytesPerPixel * 8 / bitDepth;
			return getScaledMicroscopeImage(imageEvent, lowerBound, upperBound);
		}
		else if(bytesPerPixel == 2)
		{
			short[] imageData = (short[])imageEvent.getImageData();
			short minVal = Byte.MAX_VALUE;
			short maxVal = 0;
			for(short val : imageData)
			{
				if(val < minVal)
					minVal = val;
				if(val > maxVal)
					maxVal = val;
			}
			float upperBound = maxVal / Short.MAX_VALUE * bytesPerPixel * 8 / bitDepth;
			float lowerBound = minVal / Short.MAX_VALUE * bytesPerPixel * 8 / bitDepth;
			return getScaledMicroscopeImage(imageEvent, lowerBound, upperBound);
		}
		else
		{
			throw new ImageConvertException("Can only interpret images with 1 or 2 byte per pixel. This image has " + Integer.toString(bytesPerPixel) + " bytes per pixel!");
		}
	}
}
