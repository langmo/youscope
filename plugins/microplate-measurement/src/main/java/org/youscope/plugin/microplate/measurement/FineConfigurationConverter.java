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

import java.util.HashMap;
import java.util.Map.Entry;

import org.youscope.common.PositionInformation;
import org.youscope.common.Well;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Class to convert the well/position fine configuration into XML.
 * @author Moritz Lang
 *
 */
public class FineConfigurationConverter implements Converter 
{
	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class type) 
	{
		return HashMap.class.isAssignableFrom(type);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) 
	{
		HashMap map = (HashMap) value;             
		for (Object obj : map.entrySet()) 
		{                 
			Entry entry = (Entry) obj;  
			PositionInformation entryKey = (PositionInformation)entry.getKey();
			Well well = entryKey.getWell();
			XYAndFocusPosition entryValue = (XYAndFocusPosition)entry.getValue();
			
			writer.startNode("position");
			writer.addAttribute("x", Double.toString(entryValue.getX()));
			writer.addAttribute("y", Double.toString(entryValue.getY()));
			if(!Double.isNaN(entryValue.getFocus()))
				writer.addAttribute("focus", Double.toString(entryValue.getFocus()));
			if(well != null)
			{
				writer.addAttribute("well-x", Integer.toString(well.getWellX()));
				writer.addAttribute("well-y", Integer.toString(well.getWellY()));
			}
			if(entryKey.getNumPositions() == 2)
			{
				writer.addAttribute("tile-x", Integer.toString(entryKey.getPosition(1)));
				writer.addAttribute("tile-y", Integer.toString(entryKey.getPosition(0)));
			}
			writer.endNode();             
		}         
	}    
	
	@Override
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) 
	{            
		HashMap<PositionInformation, XYAndFocusPosition> map = new HashMap<PositionInformation, XYAndFocusPosition>(200);
		try
		{
			while(reader.hasMoreChildren()) 
			{         
				 reader.moveDown();         
				 double x = Double.parseDouble(reader.getAttribute("x"));
				 double y = Double.parseDouble(reader.getAttribute("y"));
				 String focusString = reader.getAttribute("focus");
				 double focus;
				 if(focusString == null)
					 focus = Double.NaN;
				 else
					 focus = Double.parseDouble(focusString);
				 String wellXString = reader.getAttribute("well-x");
				 String wellYString = reader.getAttribute("well-y");
				 Well well;
				 if(wellXString != null && wellYString != null)
					 well = new Well(Integer.parseInt(wellYString), Integer.parseInt(wellXString));
				 else
					 well = null;
				 PositionInformation posInfo = new PositionInformation(well);
				 String tileXString = reader.getAttribute("tile-x");
				 String tileYString = reader.getAttribute("tile-y");
				 if(tileXString != null && tileYString != null)
				 {
					 posInfo = new PositionInformation(posInfo, PositionInformation.POSITION_TYPE_YTILE, Integer.parseInt(tileYString));
					 posInfo = new PositionInformation(posInfo, PositionInformation.POSITION_TYPE_XTILE, Integer.parseInt(tileXString));
				 }
				 map.put(posInfo, new XYAndFocusPosition(x, y, focus));         
				 reader.moveUp();     
			}
		}
		catch(Exception e)
		{
			throw new ConversionException("Could not parse fine-configuration of measurement.", e);
		}
		return map; 
	}
}


