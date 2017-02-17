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
package org.youscope.addon.celldetection;

import org.youscope.common.table.ColumnDefinition;

/**
 * Set of columns typically returned by cell detection addons.
 * @author Moritz Lang
 *
 */
public class CellDetectionTableColumns
{
	/**
	 * Column containing the identifiers for cells.
	 */
	public final static ColumnDefinition<Integer> TABLE_COLUMN_CELL_ID = ColumnDefinition.createIntegerColumnDefinition("cell.index", "Unique index of the cell in the image, starting at zero.\nIndices must be unique for a given image, i.e. no two different cells in one image may have the same ID. However, they may or may not be unique for a given image series.\nIf more than one row is representing the same cell, the same index should be used.\n ", false);
	
	/**
	 * Column containing the track identifiers of cells.
	 */
	public final static ColumnDefinition<Integer> TABLE_COLUMN_CELL_TRACKING_ID = ColumnDefinition.createIntegerColumnDefinition("track.index", "Unique index of a cell for the complete measurement (not only the current image), starting at zero. Indices must be unique for the whole track, i.e. no two different cells in the whole measurement may have the same ID.\nHowever, one cell which is detected more than once in different images may or may not keep its ID in all or some of the images.\nIf more than one row is representing the same cell, the same index should be used.", false);
	
	/**
	 * Column containing the first previous track identifiers of cells.
	 */
	public final static ColumnDefinition<Integer> TABLE_COLUMN_LAST_CELL_TRACKING_ID1 = ColumnDefinition.createIntegerColumnDefinition("track.last-index", "Indicates that a given cell had a different cell tracking ID in a previous image in the image series."+
	 "Due to cell division and similar, two cells may have the same last-cell-tracking-id (however, they have to have a different tracking-id)."+
	 "Due to mating and similar, one cell may have more than one last-cell-tracking-id. If so, these IDs can be saved in track.last-index and track.last-index-2."+
	 "If only one previous ID is present, it should be saved in track.last-index."+
	 "The last-cell-tracking ID should be the last known ID of the cell in the series. If a cell is changing more than once its ID, the ID should be the most recent one before the current image."+
	 "Note that a cell might be &quote;lost&quote; in one or more of the previous images. This column thus just indicates that a given cell was known the last time by a given ID,"+
	 "but not that the given cell tracking ID was present in the last image."+ 
	 "If a cell did not change its ID, it may or may not list its previous (and current) ID in this column. However, if not listed, the value must be null.", true);
	
	/**
	 * Column containing the second previous track identifiers of cells.
	 */
	public final static ColumnDefinition<Integer> TABLE_COLUMN_LAST_CELL_TRACKING_ID2 = ColumnDefinition.createIntegerColumnDefinition("track.last-index-2", "Indicates that a given cell had a different cell tracking ID in a previous image in the image series."+
			 "Due to cell division and similar, two cells may have the same last-cell-tracking-id (however, they have to have a different tracking-id)."+
			 "Due to mating and similar, one cell may have more than one last-cell-tracking-id. If so, these IDs can be saved in track.last-index and track.last-index-2."+
			 "If only one previous ID is present, it should be saved in track.last-index."+
			 "The last-cell-tracking ID should be the last known ID of the cell in the series. If a cell is changing more than once its ID, the ID should be the most recent one before the current image."+
			 "Note that a cell might be &quote;lost&quote; in one or more of the previous images. This column thus just indicates that a given cell was known the last time by a given ID,"+
			 "but not that the given cell tracking ID was present in the last image."+ 
			 "If a cell did not change its ID, it may or may not list its previous (and current) ID in this column. However, if not listed, the value must be null.", true);
	
	/**
	 * Column containing the x-position of a cell.
	 */
	public final static ColumnDefinition<Double> TABLE_COLUMN_XPOSITION_PX = ColumnDefinition.createDoubleColumnDefinition("cell.center.x", "The x-position is usually the mean or the medium of the x-position of all pixels belonging to a cell.\nThe x-position indicates the number of pixels, counted from the left side of the image, starting at 0.", true);
	
	/**
	 * Column containing the y-position of a cell.
	 */
	public final static ColumnDefinition<Double> TABLE_COLUMN_YPOSITION_PX = ColumnDefinition.createDoubleColumnDefinition("cell.center.y", "The x-position is usually the mean or the medium of the y-position of all pixels belonging to a cell.\nThe y-position indicates the number of pixels, counted from the top of the image, starting at 0.", true);
	
	
	/**
	 * Column containing the quantification image id for which e.g. fluorescence was quantified.
	 */
	public final static ColumnDefinition<Integer> TABLE_COLUMN_QUANTIFICATION_IMAGE_ID = ColumnDefinition.createIntegerColumnDefinition("quantification_image", "Index of the quantification image, starting at zero. The index indicates to which channel e.g. the quantification of the fluorescence belongs.", true);
	
	/**
	 * Column containing the area of cells.
	 */
	public final static ColumnDefinition<Double> TABLE_COLUMN_AREA_PX = ColumnDefinition.createDoubleColumnDefinition("cell.area", "The area (in voxels = pixels^2) the cell is occupying in the image.", true);
	
	/**
	 * Column containing the volume of cells.
	 */
	public final static ColumnDefinition<Double> TABLE_COLUMN_VOLUME_PX = ColumnDefinition.createDoubleColumnDefinition("cell.volume", "The volume (in pixels^3) of a cell.", true);
	
	/**
	 * Column containing cell perimeter.
	 */
	public final static ColumnDefinition<Double> TABLE_COLUMN_PERIMETER_PX = ColumnDefinition.createDoubleColumnDefinition("cell.perimeter", "The perimeter (in pixels) of the cell.", true);
	
	/**
	 * Column containing cells' major axis.
	 */
	public final static ColumnDefinition<Double> TABLE_COLUMN_MAJOR_AXIS_PX = ColumnDefinition.createDoubleColumnDefinition("cell.majoraxis", "The length of the major axis of the cell (in pixels).", true);
	
	/**
	 * Column containing cells' minor axis.
	 */
	public final static ColumnDefinition<Double> TABLE_COLUMN_MINOR_AXIS_PX = ColumnDefinition.createDoubleColumnDefinition("cell.minoraxis", "The length of the minor axis of the cell (in pixels).", true);
	
	/**
	 * Column containing cells' orientations.
	 */
	public final static ColumnDefinition<Double> TABLE_COLUMN_ORIENTATION = ColumnDefinition.createDoubleColumnDefinition("cell.orientation", "Orientation (in degrees) the cell is pointing to (-90° to 90).", true);
	
	/**
	 * Column containing cells' fluorescence.
	 */
	public final static ColumnDefinition<Double> TABLE_COLUMN_FLUORESCENCE = ColumnDefinition.createDoubleColumnDefinition("cell.fluo", "The fluorescence intensity is between zero and one, where one indicates strong fluorescence (white pixels), and zero indicates no fluorescence (black pixels)."+
	 "This value is typically equal to the mean or median intensity of the pixels corresponding to the cell, divided by the maximal pixel intensity (e.g. 2^8-1 for 8bit images).", true);
	
	/**
	 * Column containing cells' excentricity.
	 */
	public final static ColumnDefinition<Double> TABLE_COLUMN_EXCENTRICITY = ColumnDefinition.createDoubleColumnDefinition("cell.excentricity", "Excentricity of the cell.", true);
}
