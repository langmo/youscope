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
package org.youscope.common.saving;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.youscope.common.ExecutionInformation;
import org.youscope.common.PositionInformation;
import org.youscope.common.Well;
import org.youscope.common.image.ImageEvent;
import org.youscope.common.util.TextTools;

/**
 * Helper class to convert file names templates containing macros (e.g. %N for the image name) to real file names.
 * @author Moritz Lang
 */
public class FileNameMacroConverter
{
	/**
	 * Returns all macros which can be used for general file names.
	 * @return General macros.
	 */
	public static ReplacePattern[] getGeneralPathMacros()
	{
		return new ReplacePattern[]{
				PATTERN_MEASUREMENT_NAME,
				PATTERN_MEASUREMENT_DAY,
				PATTERN_MEASUREMENT_MONTH,
				PATTERN_MEASUREMENT_YEAR,
				PATTERN_MEASUREMENT_HOUR12,
				PATTERN_MEASUREMENT_HOUR24,
				PATTERN_MEASUREMENT_MINUTE,
				PATTERN_MEASUREMENT_SECOND,
				PATTERN_MEASUREMENT_MILLISECOND
		};
	}
	
	
	/**
	 * Returns all macros which can be used for file names representing images.
	 * @return Image macros.
	 */
	public static ReplacePattern[] getImagePathMacros()
	{
		return new ReplacePattern[]{
				PATTERN_IMAGE_NAME,
				PATTERN_IMAGE_INDEX,
				PATTERN_IMAGE_NUMBER,
				PATTERN_IMAGE_CHANNEL,
				PATTERN_IMAGE_CAMERA,
				PATTERN_IMAGE_POSITION,
				PATTERN_IMAGE_WELL_NUM,
				PATTERN_IMAGE_WELL,
				PATTERN_IMAGE_DAY,
				PATTERN_IMAGE_MONTH,
				PATTERN_IMAGE_YEAR,
				PATTERN_IMAGE_HOUR12,
				PATTERN_IMAGE_HOUR24,
				PATTERN_IMAGE_MINUTE,
				PATTERN_IMAGE_SECOND,
				PATTERN_IMAGE_MILLISECOND,
				
				PATTERN_MEASUREMENT_NAME,
				PATTERN_MEASUREMENT_DAY,
				PATTERN_MEASUREMENT_MONTH,
				PATTERN_MEASUREMENT_YEAR,
				PATTERN_MEASUREMENT_HOUR12,
				PATTERN_MEASUREMENT_HOUR24,
				PATTERN_MEASUREMENT_MINUTE,
				PATTERN_MEASUREMENT_SECOND,
				PATTERN_MEASUREMENT_MILLISECOND
		};
	}
	/**
	 * Returns all macros which can be used for file names representing tables.
	 * @return Table macros.
	 */
	public static ReplacePattern[] getTablePathMacros()
	{
		return new ReplacePattern[]{
				PATTERN_TABLE_NAME,
				
				PATTERN_MEASUREMENT_NAME,
				PATTERN_MEASUREMENT_DAY,
				PATTERN_MEASUREMENT_MONTH,
				PATTERN_MEASUREMENT_YEAR,
				PATTERN_MEASUREMENT_HOUR12,
				PATTERN_MEASUREMENT_HOUR24,
				PATTERN_MEASUREMENT_MINUTE,
				PATTERN_MEASUREMENT_SECOND,
				PATTERN_MEASUREMENT_MILLISECOND
		};
	}
	
	/**
	 * Definition of a pattern which gets replaced in a file name.
	 * @author Moritz Lang
	 *
	 */
	public static class ReplacePattern implements Serializable
	{
		/**
		 * Serial Version UID.
		 */
		private static final long serialVersionUID = 9169862398039553926L;
		/**
		 * Pattern which gets replaced.
		 */
		public final String pattern;
		/**
		 * Description of pattern.
		 */
		public final String description;
		/**
		 * If replacement string is longer than indicated length, truncate replacement string at the beginning if true, otherwise at the end.
		 */
		public final boolean truncateBeginning;
		/**
		 * If replacement string is shorter than indicated length, pad replacement string at the beginning if true, otherwise at the end.
		 */
		public final boolean padBeginning;
		/**
		 * Character to use when padding replacement string if it is too short.
		 */
		public final char padChar;
		
		/**
		 * Constructor.
		 * @param pattern Pattern which gets replaced.
		 * @param truncateBeginning If replacement string is longer than indicated length, truncate replacement string at the beginning if true, otherwise at the end.
		 * @param padBeginning If replacement string is shorter than indicated length, pad replacement string at the beginning if true, otherwise at the end.
		 * @param padChar Character to use when padding replacement string if it is too short.
		 * @param description Description of pattern.
		 */
		public ReplacePattern(String pattern, boolean truncateBeginning, boolean padBeginning, char padChar, String description)
		{
			this.pattern = pattern;
			this.description = description;
			this.truncateBeginning = truncateBeginning;
			this.padBeginning = padBeginning;
			this.padChar = padChar;
		}
	}
	
	/**
	 * Number of the image, starting at 0.
	 */
	public static final ReplacePattern PATTERN_IMAGE_INDEX = new ReplacePattern("i", true, true, '0', "Number of the image, starting at 0.");
	/**
	 * Number of the image, starting at 1.
	 */
	public static final ReplacePattern PATTERN_IMAGE_NUMBER = new ReplacePattern("n", true, true, '0', "Number of the image, starting at 1.");
	/**
	 * Name of the image.
	 */
	public static final ReplacePattern PATTERN_IMAGE_NAME = new ReplacePattern("N", false, false, '_', "Name of the image.");
	/**
	 * Channel, in which the image was made.
	 */
	public static final ReplacePattern PATTERN_IMAGE_CHANNEL = new ReplacePattern("c", false, false, '_', "Channel, in which the image was made.");
	/**
	 * Camera, with which the image was made.
	 */
	public static final ReplacePattern PATTERN_IMAGE_CAMERA = new ReplacePattern("C", false, false, '_', "Camera, with which the image was made.");
	/**
	 * Position, in which the image was made.
	 */
	public static final ReplacePattern PATTERN_IMAGE_POSITION = new ReplacePattern("p", false, true, '0', "Position, in which the image was made.");
	/**
	 * Well of the image in decimal format, e.g. 0302 (C=\"03\", 2=\"02\") for well C2.
	 */
	public static final ReplacePattern PATTERN_IMAGE_WELL_NUM = new ReplacePattern("w", true, true, '0', "Well of the image in decimal format, e.g. 0302 (C=\"03\", 2=\"02\") for well C2.");
	/**
	 * Well of the image in standard format.
	 */
	public static final ReplacePattern PATTERN_IMAGE_WELL = new ReplacePattern("W", false, true, '_', "Well of the image in standard format.");
	/**
	 * Day (in month) when image was made.
	 */
	public static final ReplacePattern PATTERN_IMAGE_DAY = new ReplacePattern("d", true, true, '0', "Day (in month) when image was made.");
	/**
	 * Month when image was made.
	 */
	public static final ReplacePattern PATTERN_IMAGE_MONTH = new ReplacePattern("m", true, true, '0', "Month when image was made.");
	/**
	 * Year when image was made.
	 */
	public static final ReplacePattern PATTERN_IMAGE_YEAR = new ReplacePattern("y", true, true, '0', "Year when image was made.");
	/**
	 * Hour (0-12) when image was made.
	 */
	public static final ReplacePattern PATTERN_IMAGE_HOUR12 = new ReplacePattern("h", true, true, '0', "Hour (0-12) when image was made.");
	/**
	 * Hour (0-24) when image was made.
	 */
	public static final ReplacePattern PATTERN_IMAGE_HOUR24 = new ReplacePattern("H", true, true, '0', "Hour (0-24) when image was made.");
	/**
	 * Minute (0-59) when image was made.
	 */
	public static final ReplacePattern PATTERN_IMAGE_MINUTE = new ReplacePattern("M", true, true, '0', "Minute (0-59) when image was made.");
	/**
	 * Second (0-59) when image was made.
	 */
	public static final ReplacePattern PATTERN_IMAGE_SECOND = new ReplacePattern("s", true, true, '0', "Second (0-59) when image was made.");
	/**
	 * Millisecond (0-999) when image was made.
	 */
	public static final ReplacePattern PATTERN_IMAGE_MILLISECOND = new ReplacePattern("S", false, false, '0', "Millisecond (0-999) when image was made.");
	
	
	
	/**
	 * Name of the measurement.
	 */
	public static final ReplacePattern PATTERN_MEASUREMENT_NAME = new ReplacePattern("xN", false, false, '_', "Name of the measurement.");
	/**
	 * Day (in month) when measurement was started.
	 */
	public static final ReplacePattern PATTERN_MEASUREMENT_DAY = new ReplacePattern("xd", true, true, '0', "Day (in month) when measurement was started.");
	/**
	 * Month when measurement was started.
	 */
	public static final ReplacePattern PATTERN_MEASUREMENT_MONTH = new ReplacePattern("xm", true, true, '0', "Month when measurement was started.");
	/**
	 * Year when measurement was started.
	 */
	public static final ReplacePattern PATTERN_MEASUREMENT_YEAR = new ReplacePattern("xy", true, true, '0', "Year when measurement was started.");
	/**
	 * Hour (0-12) when measurement was started.
	 */
	public static final ReplacePattern PATTERN_MEASUREMENT_HOUR12 = new ReplacePattern("xh", true, true, '0', "Hour (0-12) when measurement was started.");
	/**
	 * Hour (0-24) when measurement was started.
	 */
	public static final ReplacePattern PATTERN_MEASUREMENT_HOUR24 = new ReplacePattern("xH", true, true, '0', "Hour (0-24) when measurement was started.");
	/**
	 * Minute (0-59) when measurement was started.
	 */
	public static final ReplacePattern PATTERN_MEASUREMENT_MINUTE = new ReplacePattern("xM", true, true, '0', "Minute (0-59) when measurement was started.");
	/**
	 * Second when measurement was started.
	 */
	public static final ReplacePattern PATTERN_MEASUREMENT_SECOND = new ReplacePattern("xs", true, true, '0', "Second when measurement was started.");
	/**
	 * Millisecond when measurement was started.
	 */
	public static final ReplacePattern PATTERN_MEASUREMENT_MILLISECOND = new ReplacePattern("xS", false, false, '0', "Millisecond when measurement was started.");
	
	
	
	/**
	 * Name of the table.
	 */
	public static final ReplacePattern PATTERN_TABLE_NAME = new ReplacePattern("N", false, false, '_', "Name of the table.");
	
	private static class ReplaceInformation
	{
		private final ReplacePattern pattern;
		private final String replaceString;
		private final int minLength;
		public ReplaceInformation(ReplacePattern pattern, String replaceString, int minLength)
		{
			this.pattern = pattern;
			this.replaceString = replaceString;
			this.minLength = minLength;
		}
		public ReplaceInformation(ReplacePattern pattern, String replaceString)
		{
			this(pattern, replaceString, -1);
		}
		private String getReplace(int length)
		{
			if(minLength > 0 && minLength > length)
				length = minLength;
			
			if(length < 0 || replaceString.length() == length)
				return replaceString;
			else if(replaceString.length() > length)
			{
				// Shorten String
				if(pattern.truncateBeginning)
					return replaceString.substring(replaceString.length() - length, replaceString.length());
				return replaceString.substring(0, length);
			}
			else
			{
				String pad = new String(new char[length-replaceString.length()]).replace("\0", ""+pattern.padChar);
				if(pattern.padBeginning)
					return pad+replaceString;
				return replaceString+pad;
			}
		}
		
		public String replace(String string)
		{
			Pattern p = Pattern.compile("%[0-9]?"+pattern.pattern);
			Matcher m = p.matcher(string);
			StringBuffer sb = new StringBuffer();

			while(m.find())
			{
				int length = -1;
				if(m.end()-m.start() > pattern.pattern.length()+1)
					length = Integer.parseInt(string.substring(m.start()+1, m.end()-pattern.pattern.length()));		
				m.appendReplacement(sb, getReplace(length));
			}
			m.appendTail(sb);
			return sb.toString();
		}
	}
	
	private static List<ReplaceInformation> getMeasurementReplaceInformation(SaveInformation saveInformation)
	{
		GregorianCalendar date = new GregorianCalendar();
		date.setTimeInMillis(saveInformation.getMeasurementStartTime());
		
		ArrayList<ReplaceInformation> info = new ArrayList<>();
		info.add(new ReplaceInformation(PATTERN_MEASUREMENT_NAME, TextTools.convertToFileName(saveInformation.getMeasurementName())));
		
		info.add(new ReplaceInformation(PATTERN_MEASUREMENT_DAY, Integer.toString(date.get(Calendar.DAY_OF_MONTH))));
		info.add(new ReplaceInformation(PATTERN_MEASUREMENT_MONTH, Integer.toString(1+date.get(Calendar.MONTH))));
		info.add(new ReplaceInformation(PATTERN_MEASUREMENT_YEAR, Integer.toString(date.get(Calendar.YEAR))));
		info.add(new ReplaceInformation(PATTERN_MEASUREMENT_HOUR12, Integer.toString(date.get(Calendar.HOUR))));
		info.add(new ReplaceInformation(PATTERN_MEASUREMENT_HOUR24, Integer.toString(date.get(Calendar.HOUR_OF_DAY))));
		info.add(new ReplaceInformation(PATTERN_MEASUREMENT_MINUTE, Integer.toString(date.get(Calendar.MINUTE))));
		info.add(new ReplaceInformation(PATTERN_MEASUREMENT_SECOND, Integer.toString(date.get(Calendar.SECOND))));
		info.add(new ReplaceInformation(PATTERN_MEASUREMENT_MILLISECOND, Integer.toString(date.get(Calendar.MILLISECOND))));
	
		return info;

	}
	
	private static List<ReplaceInformation> getTableReplaceInformation(String tableName)
	{
		ArrayList<ReplaceInformation> info = new ArrayList<>(1);
		info.add(new ReplaceInformation(PATTERN_TABLE_NAME, TextTools.convertToFileName(tableName)));
		return info;
	}
	
	private static List<ReplaceInformation> getImageReplaceInformation(ImageEvent<?> event, String imageName)
	{
		String channel = event.getChannel();
		String camera = event.getCamera();
		ExecutionInformation executionInformation = event.getExecutionInformation();
		PositionInformation positionInformation = event.getPositionInformation();
		GregorianCalendar date = new GregorianCalendar();
		date.setTimeInMillis(event.getCreationTime());
		
		ArrayList<ReplaceInformation> info = new ArrayList<>();
		String string;
		String string2;
		
		string = Long.toString(executionInformation.getEvaluationNumber());
		string2 = Long.toString(executionInformation.getEvaluationNumber()+1);
		// add loop numbers
		long[] loopNumbers = executionInformation == null ? new long[0] : executionInformation.getLoopNumbers();
		for(int i = 0; i < loopNumbers.length; i++)
		{
			if(loopNumbers[i] <= 8)
			{
				string += "-0" + Long.toString(loopNumbers[i]);
				string2 += "-0" + Long.toString(loopNumbers[i]+1);
			}
			else if(loopNumbers[i] <= 9)
			{
				string += "-0" + Long.toString(loopNumbers[i]);
				string2 += "-" + Long.toString(loopNumbers[i]+1);
			}
			else
			{
				string += "-" + Long.toString(loopNumbers[i]);
				string2 += "-" + Long.toString(loopNumbers[i]+1);
			}
		}
		info.add(new ReplaceInformation(PATTERN_IMAGE_INDEX, string, string.length()));
		info.add(new ReplaceInformation(PATTERN_IMAGE_NUMBER, string2, string2.length()));
		info.add(new ReplaceInformation(PATTERN_IMAGE_NAME, TextTools.convertToFileName(imageName)));
		info.add(new ReplaceInformation(PATTERN_IMAGE_CHANNEL, channel));
		info.add(new ReplaceInformation(PATTERN_IMAGE_CAMERA, camera));
	
		string = "";
		int[] positions = positionInformation.getPositions();
		for(int i = 0; i < positions.length; i++)
		{
			if(positions[i] < 9)
				string += "0" + Integer.toString(positions[i]+1);
			else
				string += Integer.toString(positions[i]+1);
		}
		info.add(new ReplaceInformation(PATTERN_IMAGE_POSITION, string, string.length()));
		
		Well well = positionInformation == null ? null : positionInformation.getWell();
		if(well==null)
		{
			string = "0000";
		}
		else
		{
			if(well.getWellY() < 0)
				string = "00";
			else if(well.getWellY() < 9)
				string = "0" + Integer.toString(well.getWellY() + 1);
			else
				string = Integer.toString(well.getWellY() + 1);
			
			if(well.getWellX() < 0)
				string += "00";
			else if(well.getWellX() < 9)
				string += "0" + Integer.toString(well.getWellX() + 1);
			else
				string += Integer.toString(well.getWellX() + 1);	
		}
		info.add(new ReplaceInformation(PATTERN_IMAGE_WELL_NUM, string, string.length()));
		
		if(well == null)
			string = "";
		else
			string = well.getWellName();
		info.add(new ReplaceInformation(PATTERN_IMAGE_WELL, string, string.length()));
		
		info.add(new ReplaceInformation(PATTERN_IMAGE_DAY, Integer.toString(date.get(Calendar.DAY_OF_MONTH))));
		info.add(new ReplaceInformation(PATTERN_IMAGE_MONTH, Integer.toString(1+date.get(Calendar.MONTH))));
		info.add(new ReplaceInformation(PATTERN_IMAGE_YEAR, Integer.toString(date.get(Calendar.YEAR))));
		info.add(new ReplaceInformation(PATTERN_IMAGE_HOUR12, Integer.toString(date.get(Calendar.HOUR))));
		info.add(new ReplaceInformation(PATTERN_IMAGE_HOUR24, Integer.toString(date.get(Calendar.HOUR_OF_DAY))));
		info.add(new ReplaceInformation(PATTERN_IMAGE_MINUTE, Integer.toString(date.get(Calendar.MINUTE))));
		info.add(new ReplaceInformation(PATTERN_IMAGE_SECOND, Integer.toString(date.get(Calendar.SECOND))));
		info.add(new ReplaceInformation(PATTERN_IMAGE_MILLISECOND, Integer.toString(date.get(Calendar.MILLISECOND))));
	
		return info;

	}
	
	/**
	 * Converts a file name template representing the file name of the image to the image path.
	 * @param fileNameTemplate the template.
	 * @param imageName name of the image.
	 * @param event the image which should be saved.
	 * @param saveInformation general information on the measurement.
	 * @return the file name the image can be saved to.
	 */
	public static String convertImagePath(String fileNameTemplate, String imageName, ImageEvent<?> event, SaveInformation saveInformation)
	{
		List<ReplaceInformation> replaceInformation = getMeasurementReplaceInformation(saveInformation);
		for(ReplaceInformation replace : replaceInformation)
		{
			fileNameTemplate = replace.replace(fileNameTemplate);
		}
		replaceInformation = getImageReplaceInformation(event, TextTools.convertToFileName(imageName));
		for(ReplaceInformation replace : replaceInformation)
		{
			fileNameTemplate = replace.replace(fileNameTemplate);
		}
		return fileNameTemplate;
	}
	/**
	 * Converts a file name template representing the file name of a table to the file path.
	 * @param fileNameTemplate the template.
	 * @param tableName name of the table.
	 * @param saveInformation general information on the measurement.
	 * @return the file name the table can be saved to.
	 */
	public static String convertTablePath(String fileNameTemplate, String tableName, SaveInformation saveInformation)
	{
		List<ReplaceInformation> replaceInformation = getMeasurementReplaceInformation(saveInformation);
		for(ReplaceInformation replace : replaceInformation)
		{
			fileNameTemplate = replace.replace(fileNameTemplate);
		}
		replaceInformation = getTableReplaceInformation(tableName);
		for(ReplaceInformation replace : replaceInformation)
		{
			fileNameTemplate = replace.replace(fileNameTemplate);
		}
		return fileNameTemplate;
	}
	/**
	 * Converts a file name template representing a general path to the file path.
	 * @param fileNameTemplate the template.
	 * @param saveInformation general information on the measurement.
	 * @return the file name the general file can be saved to.
	 */
	public static String convertGeneralPath(String fileNameTemplate, SaveInformation saveInformation)
	{
		List<ReplaceInformation> replaceInformation = getMeasurementReplaceInformation(saveInformation);
		for(ReplaceInformation replace : replaceInformation)
		{
			fileNameTemplate = replace.replace(fileNameTemplate);
		}
		return fileNameTemplate;
	}
}
