/**
 * 
 */
package ch.ethz.csb.youscope.addon.microplatemeasurement;

import java.util.Hashtable;
import java.util.Map.Entry;

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
		return Hashtable.class.isAssignableFrom(type);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) 
	{
		Hashtable map = (Hashtable) value;             
		for (Object obj : map.entrySet()) 
		{                 
			Entry entry = (Entry) obj;  
			WellAndTileIdentifier entryKey = (WellAndTileIdentifier)entry.getKey();
			XYAndFocusPositionDTO entryValue = (XYAndFocusPositionDTO)entry.getValue();
			
			writer.startNode("position");
			writer.addAttribute("x", Double.toString(entryValue.getX()));
			writer.addAttribute("y", Double.toString(entryValue.getY()));
			writer.addAttribute("focus", Double.toString(entryValue.getFocus()));
			writer.addAttribute("well-x", Integer.toString(entryKey.getWellX()));
			writer.addAttribute("well-y", Integer.toString(entryKey.getWellY()));
			writer.addAttribute("tile-x", Integer.toString(entryKey.getTileX()));
			writer.addAttribute("tile-y", Integer.toString(entryKey.getTileY()));
			writer.endNode();             
		}         
	}    
	
	@Override
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) 
	{            
		Hashtable<WellAndTileIdentifier, XYAndFocusPositionDTO> map = new Hashtable<WellAndTileIdentifier, XYAndFocusPositionDTO>(500);
		try
		{
			while(reader.hasMoreChildren()) 
			{         
				 reader.moveDown();         
				 double x = Double.parseDouble(reader.getAttribute("x"));
				 double y = Double.parseDouble(reader.getAttribute("y"));
				 double focus = Double.parseDouble(reader.getAttribute("focus"));
				 int wellY = Integer.parseInt(reader.getAttribute("well-y"));
				 int wellX = Integer.parseInt(reader.getAttribute("well-x"));
				 int tileY = Integer.parseInt(reader.getAttribute("tile-y"));
				 int tileX = Integer.parseInt(reader.getAttribute("tile-x"));
				 map.put(new WellAndTileIdentifier(wellY, wellX, tileY, tileX), new XYAndFocusPositionDTO(x, y, focus));         
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


