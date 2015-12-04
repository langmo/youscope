/**
 * 
 */
package org.youscope.server;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.youscope.common.Well;
import org.youscope.common.measurement.ExecutionInformation;

/**
 * @author langmo
 */
class NamingMacros
{

	public static final String[][]	FILE_NAME_MACROS	= { {"i", "Index of image, starting at 0."}, {"n", "Number of the image, starting at 1."}, {"c", "Channel, in which the image was made."}, {"p", "Position, in which the image was made. =0 if no positions were defined."}, {"w", "Well of the image in decimal format, e.g. 0302 for well C2 (C=03, 2=02)."}, {"W", "Well of the image in standard format, e.g. C2."}, {"d", "Day in month."}, {"m", "Month."}, {"y", "Year."}, {"h", "Hour (0-12)."}, {"H", "Hour (0-24)."}, {"M", "Minute."}, {"s", "Second."}, {"S", "Millisecond."}};

	public static final String[][]	FOLDER_NAME_MACROS	= { {"c", "Channel, in which the image was made."}, {"p", "Position, in which the image was made. =0 if no positions were defined."}, {"w", "Well of the image in decimal format, e.g. 0302 for well C2 (C=03, 2=02)."}, {"W", "Well of the image in standard format, e.g. C2."}};

	private static String convertFileRegexp(String exp, String name, String channel, Well well, int[] position, ExecutionInformation executionInformation, GregorianCalendar date, String camera, Object additionalInformation)
	{
		int length = -1;
		if(exp.length() == 3)
			length = Integer.parseInt(exp.substring(1, 2));
		char specifier = exp.charAt(exp.length() - 1);

		String result = "";
		boolean cutStart = true;
		int[] loopNumbers;
		switch(specifier)
		{
			case 'i':
				// Index of the image, starting at 0.
				if(length == -1)
					length = 4;
				result = Integer.toString(executionInformation == null ? 0 : executionInformation.getEvaluationNumber());
				if(length < result.length())
					length = result.length();
				result = "0000000000" + result;

				// add loop numbers
				loopNumbers = executionInformation == null ? new int[0] : executionInformation.getLoopNumbers();
				for(int i = 0; i < loopNumbers.length; i++)
				{
					String addString;
					if(loopNumbers[i] <= 9)
						addString = "-0" + Integer.toString(loopNumbers[i]);
					else
						addString = "-" + Integer.toString(loopNumbers[i]);
					result += addString;
					length += addString.length();
				}

				break;
			case 'n':
				// Number of the image, starting at 1.
				if(length == -1)
					length = 4;
				result = Integer.toString(executionInformation == null ? 1 : executionInformation.getEvaluationNumber() + 1);
				if(length < result.length())
					length = result.length();
				result = "0000000000" + result;

				// add loop numbers
				loopNumbers = executionInformation == null ? new int[0] : executionInformation.getLoopNumbers();
				for(int i = 0; i < loopNumbers.length; i++)
				{
					String addString;
					if(loopNumbers[i] < 9)
						addString = "-0" + Integer.toString(loopNumbers[i] + 1);
					else
						addString = "-" + Integer.toString(loopNumbers[i] + 1);
					result += addString;
					length += addString.length();
				}

				break;
			case 'N':
				// current channel.
				result = name;
				if(result == null || result.length() < 1)
					result = "XX";
				cutStart = false;
				break;
			case 'c':
				// current channel.
				result = channel;
				if(result == null || result.length() < 1)
					result = "XX";
				cutStart = false;
				break;
			case 'C':
				// current channel.
				result = camera;
				if(result == null || result.length() < 1)
					result = "XX";
				cutStart = false;
				break;
			case 'p':
				// Position of the image.
				result = "0000000000";
				for(int i = 0; i < position.length; i++)
				{
					if(position[i] < 10)
						result += "0";
					result += Integer.toString(position[i]);
				}
				if(length == -1)
					length = 2;
				if(length / 2 < position.length)
					length = position.length * 2;
				break;
			case 'w':
				// well of the image in decimal format, e.g. 0302 for well C2 (C=03,
				// 2=02).
				if(well == null)
				{
					result = "00000000000000";
				}
				else
				{
					result = "0000000000";
					if(well.getWellY() < 9)
						result += "0";
					if(well.getWellY() < 0)
						result += "0";
					else
						result += Integer.toString(well.getWellY() + 1);
					if(well.getWellX() < 9)
						result += "0";
					if(well.getWellX() < 0)
						result += "0";
					else
						result += Integer.toString(well.getWellX() + 1);
				}
				if(length == -1)
					length = 4;
				break;
			case 'W':
				// well of the image in standard format, e.g. C2.
				if(well == null)
					result = "";
				else
					result = well.getWellName();
				break;
			case 'd':
				// day in month.
				int day = date.get(Calendar.DAY_OF_MONTH);
				if(day < 10 && length >= 2)
					result += "0";
				result += Integer.toString(day);
				break;
			case 'm':
				// month in year.
				int month = date.get(Calendar.MONTH);
				if(month < 9 && length >= 2)
					result += "0";
				result += Integer.toString(month + 1);
				break;
			case 'y':
				// year.
				int year = date.get(Calendar.YEAR);
				result = Integer.toString(year);
				break;
			case 'h':
				// current hour (0-12)
				int hour = date.get(Calendar.HOUR);
				if(hour < 10 && length >= 2)
					result += "0";
				result += Integer.toString(hour);
				break;
			case 'H':
				// current hour (0-24).
				int hourOfDay = date.get(Calendar.HOUR_OF_DAY);
				if(hourOfDay < 10 && length >= 2)
					result += "0";
				result += Integer.toString(hourOfDay);
				break;
			case 'M':
				// current minute.
				int minute = date.get(Calendar.MINUTE);
				if(minute < 10 && length >= 2)
					result += "0";
				result += Integer.toString(minute);
				break;
			case 's':
				// current minute.
				int second = date.get(Calendar.SECOND);
				if(second < 10 && length >= 2)
					result += "0";
				result += Integer.toString(second);
				break;
			case 'S':
				// current millisecond.
				int millisecond = date.get(Calendar.MILLISECOND);
				if(millisecond < 100 && length >= 3)
					result += "0";
				if(millisecond < 10 && length >= 2)
					result += "0";
				result += Integer.toString(millisecond);
				break;
			case 'A':
				if(additionalInformation == null)
					result = "";
				else if(additionalInformation instanceof int[])
				{
					for(int info : (int[])additionalInformation)
					{
						result += "_" + Integer.toString(info);
					}
				}
				else if(additionalInformation instanceof String[])
				{
					for(String info : (String[])additionalInformation)
					{
						result += "_" + info;
					}
				}
				else
				{
					result = "_" + additionalInformation.toString();
				}
				break;
			default:
				// Since expression is not defined, we just don't replace it.
				return exp;
		}
		if(length == -1 || result.length() <= length)
			return result;

		// Shorten String
		if(cutStart)
			return result.substring(result.length() - length, result.length());
		return result.substring(0, length);

	}

	public static String convertFileName(String fileName, String name, String channel, Well well, int[] position, ExecutionInformation executionInformation, GregorianCalendar date, String camera, Object additionalInformation)
	{
		Pattern p = Pattern.compile("%[0-9]?[a-zA-Z]");
		Matcher m = p.matcher(fileName);

		int lastIndex = 0;
		String result = "";
		while(m.find())
		{
			result += fileName.substring(lastIndex, m.start());
			result += convertFileRegexp(fileName.substring(m.start(), m.end()), name, channel, well, position, executionInformation, date, camera, additionalInformation);
			lastIndex = m.end();
		}
		result += fileName.substring(lastIndex, fileName.length());
		return result;
	}
}
