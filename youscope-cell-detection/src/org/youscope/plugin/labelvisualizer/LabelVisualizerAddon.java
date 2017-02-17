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
package org.youscope.plugin.labelvisualizer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.rmi.RemoteException;

import org.youscope.addon.celldetection.CellDetectionResult;
import org.youscope.addon.celldetection.CellDetectionTableColumns;
import org.youscope.addon.celldetection.CellVisualizationAddon;
import org.youscope.addon.celldetection.CellVisualizationException;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.image.ImageEvent;
import org.youscope.common.resource.ResourceAdapter;
import org.youscope.common.table.ColumnView;
import org.youscope.common.table.TableEntry;
import org.youscope.common.table.TableException;
import org.youscope.common.util.ImageConvertException;
import org.youscope.common.util.ImageTools;

/**
 * Visualizes the detected cells by painting the areas of each cell in a different color. Can also draw information about the cell (area, fluorescence) next to the cell.
 * @author Moritz Lang
 *
 */
class LabelVisualizerAddon extends ResourceAdapter<LabelVisualizerConfiguration> implements CellVisualizationAddon
{
	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = -2086877835027192450L;
	private static final int TEXT_PIXEL_DIST_HORIZONTAL = 12;
	private static final int TEXT_PIXEL_DIST_VERTICAL = 0;
	private static final float FONT_SIZE = 11f;
		
	/**
	 *  Array of color values (RGB) in which the cells get colored.
	 */
	private final int[][] CELL_COLORS = {
			{255, 0, 0},
			{0, 255, 0},
			{0, 0, 255},
			{255, 255, 0},
			{255, 0, 255},
			{0, 255, 255},
			{255, 255, 255}
	};
	LabelVisualizerAddon(PositionInformation positionInformation, LabelVisualizerConfiguration configuration) throws ConfigurationException, RemoteException
	{
		super(positionInformation, configuration, LabelVisualizerConfiguration.CONFIGURATION_ID,LabelVisualizerConfiguration.class, "Label Visualizer");
	}

	@Override
	public ImageEvent<?> visualizeCells(ImageEvent<?> orgImage, CellDetectionResult detectionResult) throws CellVisualizationException, RemoteException
	{
		if(!isInitialized())
			throw new CellVisualizationException("Addon not yet initialized.");
		if(orgImage == null)
			throw new CellVisualizationException("Original microscope image is null.");
		if(detectionResult == null)
			throw new CellVisualizationException("Cell detection result is null.");
		if(detectionResult.getLabelImage() == null)
			throw new CellVisualizationException("Cell detection label image is null.");
		
		ImageEvent<?> labelImage;
		if(getConfiguration().isDrawIntoOrgImage())
		{
			labelImage = drawBorderImage(orgImage, detectionResult.getLabelImage());
		}
		else
		{
			labelImage = convertLabelImage(detectionResult.getLabelImage());
		}
		
		if(getConfiguration().isDrawCellInformation())
		{
			BufferedImage image;
			try
			{
				image = ImageTools.getMicroscopeImage(labelImage);
			}
			catch(ImageConvertException e)
			{
				throw new CellVisualizationException("Could not create image object out of cell label data.", e);
			}
			image = paintInformationIntoImage(image, detectionResult);
			
			
			try
			{
				return ImageTools.toYouScopeImage(image);
			}
			catch(ImageConvertException e)
			{
				throw new CellVisualizationException("Could not convert detection image to YouScope image format.", e);
			}
		}
		return labelImage;
		
	}

	private BufferedImage paintInformationIntoImage(BufferedImage image, CellDetectionResult detectionResult) throws CellVisualizationException
	{
		if(detectionResult.getCellTable().getNumRows() <= 0)
			return image;
		
		ColumnView<Double> xposColumn;
		ColumnView<Double>  yposColumn;
		try
		{
			// find columns of x and y position.
			xposColumn = detectionResult.getCellTable().getColumnView(CellDetectionTableColumns.TABLE_COLUMN_XPOSITION_PX);
			yposColumn = detectionResult.getCellTable().getColumnView(CellDetectionTableColumns.TABLE_COLUMN_YPOSITION_PX);
		}
		catch(@SuppressWarnings("unused") TableException e)
		{
			// if x and y position is not saved, just do not display text at all (wouldn't make sense).
			return image;
		}
		ColumnView<Double>  areaColumn;
		try
		{
			areaColumn = detectionResult.getCellTable().getColumnView(CellDetectionTableColumns.TABLE_COLUMN_AREA_PX);
		}
		catch(@SuppressWarnings("unused") TableException e)
		{
			// not necessarily needed.
			areaColumn = null;
		}
		ColumnView<Integer>  trackColumn;
		try
		{
			trackColumn = detectionResult.getCellTable().getColumnView(CellDetectionTableColumns.TABLE_COLUMN_CELL_TRACKING_ID);
		}
		catch(@SuppressWarnings("unused") TableException e)
		{
			// not necessarily needed.
			trackColumn = null;
		}
		ColumnView<Double>  fluorescenceColumn;
		try
		{
			fluorescenceColumn = detectionResult.getCellTable().getColumnView(CellDetectionTableColumns.TABLE_COLUMN_FLUORESCENCE);
		}
		catch(@SuppressWarnings("unused") TableException e)
		{
			// not necessarily needed.
			fluorescenceColumn = null;
		}
		ColumnView<Integer>  imageIDColumn;
		try
		{
			imageIDColumn = detectionResult.getCellTable().getColumnView(CellDetectionTableColumns.TABLE_COLUMN_QUANTIFICATION_IMAGE_ID);
		}
		catch(@SuppressWarnings("unused") TableException e)
		{
			// not necessarily needed.
			imageIDColumn = null;
		}
				
		// find number of fluorescence channels imaged
		int imageID;
		if(imageIDColumn == null)
		{
			imageID = -1;
		}
		else
		{
			imageID = -1;
			// only use first image ID.
			// TODO: implement others.
			for(TableEntry<? extends Integer> entry : imageIDColumn)
			{
				if(!entry.isNull())
				{
					imageID = entry.getValue().intValue();
					break;
				}
			}
		}
		
		// initialize graphics
		Graphics g = image.getGraphics();
		g.setColor(Color.WHITE);
		g.setFont(g.getFont().deriveFont(FONT_SIZE));
		if(g instanceof Graphics2D)
		{
			((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
			        RenderingHints.VALUE_ANTIALIAS_ON);
		}
		int fontHeight = g.getFontMetrics().getHeight();
		int fontAscent = g.getFontMetrics().getAscent();
		
		// draw cells
		int numLines = 1 + (areaColumn != null ? 1 : 0) + (fluorescenceColumn != null ? 1 : 0);
		for(int row=0; row<xposColumn.getNumRows(); row++)
		{
			if(imageIDColumn != null && imageID >= 0)
			{
				Integer value = imageIDColumn.getValue(row);
				if(value != null && value.intValue() != imageID)
					continue;
			}
			
			double xPosCell = xposColumn.getValue(row);
			double yPosCell = yposColumn.getValue(row);
			double areaCell = areaColumn == null ? -1 : areaColumn.getValue(row);
			int cellID = trackColumn == null ? row+1 : trackColumn.getValue(row)+1;
			double fluorescence = imageID< 0 || fluorescenceColumn == null ? -1 : fluorescenceColumn.getValue(row) == null ? -1 : fluorescenceColumn.getValue(row).doubleValue();
			
			
			int firstLineX = (int)(xPosCell + TEXT_PIXEL_DIST_HORIZONTAL);
			int firstLineY = (int)(yPosCell + TEXT_PIXEL_DIST_VERTICAL - numLines * fontHeight / 2.0 + fontAscent);
			
			// draw line from cell center to text
			g.drawLine((int)xPosCell, (int)yPosCell, firstLineX-2, firstLineY - fontAscent/2);
			
			// draw text
			g.drawString("Cell " + Integer.toString(cellID), firstLineX, firstLineY);
			firstLineY += fontHeight;
			if(areaCell >= 0)
			{
				g.drawString("Area: " + Integer.toString((int)areaCell), firstLineX, firstLineY);
				firstLineY += fontHeight;
			}
			if(fluorescence>=0)
			{
				g.drawString("Fluorescence: " + Integer.toString((int)(fluorescence * 100.0)) + "%", firstLineX, firstLineY);
				firstLineY += fontHeight;
			}
			
		}
	
		return image;
	}
	
	
	private ImageEvent<?> drawBorderImage(ImageEvent<?> orgImage, ImageEvent<?> labelImage) throws CellVisualizationException
	{
		boolean orgImageTurned = orgImage.isSwitchXY() != labelImage.isSwitchXY();
		
		if(orgImageTurned && (orgImage.getWidth() != labelImage.getHeight() || orgImage.getHeight() != labelImage.getWidth())
				|| !orgImageTurned && (orgImage.getWidth() != labelImage.getWidth() || orgImage.getHeight() != labelImage.getHeight()))
			throw new CellVisualizationException("Original image and label image must have the same dimensions. " 
					+ "Original image: " + Integer.toString(orgImage.getWidth()) + " x " + Integer.toString(orgImage.getHeight()) + "; "
					+ "Label image: " + Integer.toString(labelImage.getWidth()) + " x " + Integer.toString(labelImage.getHeight()) + ".");
		
		BufferedImage orgImageBuffered;
		try
		{
			orgImageBuffered = ImageTools.getMicroscopeImage(orgImage);
		}
		catch(ImageConvertException e)
		{
			throw new CellVisualizationException("Could not create image object out of original image data.", e);
		}
		
		BufferedImage image = new BufferedImage(orgImageBuffered.getWidth(), orgImageBuffered.getHeight(), BufferedImage.TYPE_INT_ARGB);
		image.createGraphics().drawImage(orgImageBuffered, 0, 0, null);
		
		Graphics g = image.getGraphics();
		g.setColor(Color.WHITE);
		
		int width = image.getWidth();
		int height = image.getHeight();
		
		int bytesPerPixel = labelImage.getBytesPerPixel();
		
		// Create integer colors.
		int[] processedColors = new int[CELL_COLORS.length];
		for(int i=0; i<processedColors.length; i++)
		{
			processedColors[i] = (255 << 24) | (CELL_COLORS[i][0] <<  16) | (CELL_COLORS[i][1] <<  8) | (CELL_COLORS[i][2]);
		}
		
		if(bytesPerPixel == 1)
		{
			byte[] imageData = (byte[])labelImage.getImageData();
			for(int i = 1; i<width-1; i++)
			{
				for(int j=1; j < height-1; j++)
				{
					int idx = j * width + i;
					if(imageData[idx] == 0)
						continue;
					
					if(imageData[idx-1] == 0 || imageData[idx+1] == 0 || imageData[idx + width] == 0 || imageData[idx - width] == 0)
					{
						image.setRGB(i, j, processedColors[(imageData[idx] >= 0 ? imageData[idx] : -imageData[idx]) % processedColors.length]);
					}
				}
			}
			
		}
		else if(bytesPerPixel == 2)
		{
			short[] imageData = (short[])labelImage.getImageData();
			for(int i = 1; i<width-1; i++)
			{
				for(int j=1; j < height-1; j++)
				{
					int idx = j * width + i;
					if(imageData[idx] == 0)
						continue;
					
					if(imageData[idx-1] == 0 || imageData[idx+1] == 0 || imageData[idx + width] == 0 || imageData[idx - width] == 0)
					{
						image.setRGB(i, j, processedColors[(imageData[idx] >= 0 ? imageData[idx] : -imageData[idx]) % processedColors.length]);
					}
				}
			}
		}
		else
			throw new CellVisualizationException("Detection image can only be created for label images with 1 or 2 bytes/pixel. Current label image has " + Integer.toString(bytesPerPixel) + " bytes/pixel.");
		
		try
		{
			return ImageTools.toYouScopeImage(image);
		}
		catch(ImageConvertException e)
		{
			throw new CellVisualizationException("Could not convert detection image to YouScope image format.", e);
		}
	}
	
	/**
	 * Converts gray level coloring of original image into cells having different RGB colors (better separatable).
	 * @param labelImage
	 * @return
	 * @throws CellVisualizationException 
	 */
	private ImageEvent<int[]> convertLabelImage(ImageEvent<?> labelImage) throws CellVisualizationException
	{
		int[] labelImageData = new int[labelImage.getWidth() * labelImage.getHeight()];
		int bytesPerPixel = labelImage.getBytesPerPixel();
		
		// Create integer colors.
		int[] processedColors = new int[CELL_COLORS.length];
		for(int i=0; i<processedColors.length; i++)
		{
			processedColors[i] = (255 << 24) | (CELL_COLORS[i][0] <<  16) | (CELL_COLORS[i][1] <<  8) | (CELL_COLORS[i][2]);
		}
		
		if(bytesPerPixel == 1)
		{
			byte[] imageData = (byte[])labelImage.getImageData();
			for(int i = 0; i<imageData.length; i++)
			{
				if(imageData[i] == 0)
					labelImageData[i] = 0;
				else
					labelImageData[i] = processedColors[(imageData[i] >= 0 ? imageData[i] : -imageData[i]) % processedColors.length];
			}
			
		}
		else if(bytesPerPixel == 2)
		{
			short[] imageData = (short[])labelImage.getImageData();
			for(int i = 0; i<imageData.length; i++)
			{
				if(imageData[i] == 0)
					labelImageData[i] = 0;
				else
					labelImageData[i] = processedColors[(imageData[i]>0 ? imageData[i] : -imageData[i]) % processedColors.length];
			}
		}
		else
			throw new CellVisualizationException("Detection image can only be created for label images with 1 or 2 bytes/pixel. Current label image has " + Integer.toString(bytesPerPixel) + " bytes/pixel.");
		
		
		
		ImageEvent<int[]> result = ImageEvent.createImage(labelImageData, labelImage.getWidth(), labelImage.getHeight(), 8);
		result.setBands(4);
		result.setCamera("Cell-Detection-Result");
		result.setChannel(labelImage.getChannel());
		result.setChannelGroup(labelImage.getChannelGroup());
		result.setExecutionInformation(labelImage.getExecutionInformation());
		result.setCreationTime(labelImage.getCreationTime());
		result.setCreationRuntime(labelImage.getCreationRuntime());
		result.setPositionInformation(labelImage.getPositionInformation());
		result.setSwitchXY(labelImage.isSwitchXY());
		result.setTransposeX(labelImage.isTransposeX());
		result.setTransposeY(labelImage.isTransposeY());
		result.setPositionInformation(labelImage.getPositionInformation());
		
		return result;
	}
	
}
