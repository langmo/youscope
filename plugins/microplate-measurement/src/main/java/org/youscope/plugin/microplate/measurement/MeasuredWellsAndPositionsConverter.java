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
package org.youscope.plugin.microplate.measurement;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.SingleValueConverter;

/**
 * XML converter for measured well and position array
 * @author Moritz Lang
 *
 */
public class MeasuredWellsAndPositionsConverter implements SingleValueConverter 
{
	@Override
	public String toString(Object source)
    {
    	boolean[][] data = (boolean[][]) source;
    	String marshaledData = "\n";
    	for(int i=0; i<data.length;i++)
    	{
    		if(i!=0)
    			marshaledData += ";\n";
    		for(int j=0; j<data[i].length;j++)
        	{
    			if(j!=0)
    				marshaledData += ", ";
    			if(data[i][j])
    				marshaledData += "1";
    			else
    				marshaledData += "0";
        	}
    	}
    	
    	return marshaledData;
    }

    @Override
	public Object fromString(String marshaledData)
	{
    	String[] rows = marshaledData.split(";");
    	boolean[][] data = new boolean[rows.length][];
    	for(int i=0;i<rows.length;i++)
    	{
    		String[] columns = rows[i].split(",");
    		data[i] = new boolean[columns.length];
    		for(int j=0; j<columns.length; j++)
    		{
    			try
    			{
    				data[i][j] = Integer.parseInt(columns[j].trim()) > 0;
    			}
    			catch(NumberFormatException e)
    			{
    				throw new ConversionException("Element \"" + columns[j].trim() + "\" in row " + Integer.toString(i+1) + ", column " + Integer.toString(j+1) + " is not 1 or 0.", e); 
    			}
    		}
    	}
    	
        return data;
    }

    @SuppressWarnings("rawtypes")
	@Override
    public boolean canConvert(Class type) 
    {
        return type.equals(boolean[][].class);
    }
}
