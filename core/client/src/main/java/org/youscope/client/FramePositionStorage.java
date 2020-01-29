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
package org.youscope.client;

import java.awt.Dimension;
import java.awt.Point;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

import org.youscope.clientinterfaces.FramePositionStorageType;
import org.youscope.clientinterfaces.StandardProperty;


/**
 * Central handler to store frame position and sizes, if selected by user.
 * @author langmo
 *
 */
class FramePositionStorage
{
    // Position loading and saving
    private final static String FRAME_POSITION_FILE = "configuration/frame_positions.prop";
    
    private final static String FRAME_POSTFIX_SEPARATOR = "__";
    
    public enum FrameProperty
    {
        X("x"),
        Y("y"),
        Z("z"),
        WIDTH("width"),
        HEIGHT("height");
        
        private final String postfix;
        FrameProperty(String postfix)
        {
            this.postfix = postfix;
        }
        public boolean equals(String string)
        {
            return postfix.equals(string);
        }
        public static FrameProperty getProperty(String string)
        {
            for(FrameProperty property : FrameProperty.values())
            {
                if(property.equals(string))
                    return property;
            }
            return null;
        }
    }
    
    private class FramePosition
    {
        public int width = -1;

        public int height=-1;

        public int x=-1;

        public int y=-1;
        
        public int z=-1;
        
        public final String name;

        public FramePosition(final String name)
        {
            this.name = name;
        }
        public void setProperty(FrameProperty property, int value)
        {
            switch(property)
            {
                case X:
                    x = value;
                    break;
                case Y:
                    y = value;
                    break;
                case Z:
                    z = value;
                    break;
                case WIDTH:
                    width = value;
	                break;
				case HEIGHT:
                    height = value;
                    break;
				default:
					// do nothing.
					break;
            }
        }
        public void setProperty(String propertyString, int value)
        {
            FrameProperty property = FrameProperty.getProperty(propertyString);
            if(property != null)
                setProperty(property, value);
        }
    }
    
    private FramePositionStorageType storageType = FramePositionStorageType.NONE;
    private static FramePositionStorage instance = null;
    private final Map<String, FramePosition> frameMap = new HashMap<String, FramePosition>();
    private final Object ioLock = new Object();
    /**
     * Do not construct, but get singleton by function getInstance.
     */
    private FramePositionStorage()
    {
    }
    
    public static FramePositionStorage getInstance()
    {
        if(instance == null)
        {
            instance = new FramePositionStorage();
            instance.initialize();
        }
        return instance;
    }
    
    public void setFramePosition(YouScopeFrameImpl frame)
    {
        if(!storageType.isPositionFrames())
            return;
        
        FramePosition position = frameMap.get(frame.getTitle());
        if(position == null)
            return;
        if(position.width >= 0 && position.height >= 0)
            frame.setSize(new Dimension(position.width, position.height));
        if(position.x >= 0 && position.y >=0)
            frame.setLocation(position.x, position.y);
        if(position.z>=0)
            frame.setLayer(position.z);
    }
    public void storeFramePosition(YouScopeFrameImpl frame)
    {
        if(!storageType.isUpdatePositions())
            return;
        FramePosition position = frameMap.get(frame.getTitle());
        if(position == null)
        {
            if(!storageType.isStoreNewPositions())
                return;
            position = new FramePosition(frame.getTitle());
            frameMap.put(frame.getTitle(), position);
        }
        Dimension frameSize = frame.getSize();
        position.width = frameSize.width;
        position.height = frameSize.height;
        Point location = frame.getLocation();
        position.x = location.x;
        position.y = location.y;
    }
    
    private void loadProperties()
    {
        if(!storageType.isReadDisk())
            return;
        Properties properties = new Properties();
        synchronized (ioLock)
        {
            // Get saved properties
            Reader in;
            try
            {
                in = new InputStreamReader(new FileInputStream(FRAME_POSITION_FILE), "UTF-8");
                properties.load(in);
                in.close();
            } catch (IOException e)
            {
                ClientSystem.err.println("Could not load frame position configuration file "+FRAME_POSITION_FILE+". A new file will be generated.", e);
                saveToDisk();
                return;
            }
        }
            
        for(Map.Entry<Object, Object> property : properties.entrySet())
        {
            String key = property.getKey().toString();
            String valueStr = property.getValue().toString();
            int separatorIdx = key.lastIndexOf(FRAME_POSTFIX_SEPARATOR);
            if(separatorIdx <= 0)
                continue;
            int value;
            try
            {
                value = Integer.parseInt(valueStr);
            }
            catch(@SuppressWarnings("unused") NumberFormatException e)
            {
                continue;
            }
            String frameName = key.substring(0, separatorIdx);
            String frameProperty = key.substring(separatorIdx + FRAME_POSTFIX_SEPARATOR.length());
            
            FramePosition frame = createFramePosition(frameName);
            frame.setProperty(frameProperty, value);
            
        }
    
    }
    
    private FramePosition createFramePosition(String frameName)
    {
        FramePosition position = frameMap.get(frameName);
        if(position != null)
            return position;
        
        position = new FramePosition(frameName);
        frameMap.put(frameName, position);
        return position;
    }
    
    private static String toPropString(String frameName, FrameProperty property)
    {
        return frameName + FRAME_POSTFIX_SEPARATOR + property.postfix;
    }
    
    public void saveToDisk()
    {
        if(!storageType.isWriteDisk())
            return;
        Properties properties = new Properties();
        for(FramePosition position : frameMap.values())
        {
            if(position.x >= 0)
                properties.setProperty(toPropString(position.name,  FrameProperty.X), Integer.toString(position.x));
            if(position.y >= 0)
                properties.setProperty(toPropString(position.name,  FrameProperty.Y), Integer.toString(position.y));
            if(position.z >= 0)
                properties.setProperty(toPropString(position.name,  FrameProperty.Z), Integer.toString(position.z));
            if(position.width >= 0)
                properties.setProperty(toPropString(position.name,  FrameProperty.WIDTH), Integer.toString(position.width));
            if(position.height >= 0)
                properties.setProperty(toPropString(position.name,  FrameProperty.HEIGHT), Integer.toString(position.height));
            
        }
        
        synchronized (ioLock)
        {
            // Save properties
            Writer out;
            try
            {
                out = new OutputStreamWriter(new FileOutputStream(FRAME_POSITION_FILE), "UTF-8");
                
                Properties tmp = new Properties() {
                    /**
                     * Serial Version UID. 
                     */
                    private static final long serialVersionUID = 7109730875996842706L;

                    @Override
                    public synchronized Enumeration<Object> keys() {
                        return Collections.enumeration(new TreeSet<Object>(super.keySet()));
                    }
                };
                tmp.putAll(properties);
                
                tmp.store(out, "Frame Positions");
                out.close();
            } catch (IOException e)
            {
                ClientSystem.err.println("Configuration properties could not be saved: " + e.getMessage());
            }
        }
    }
    
    public void setStorageType(FramePositionStorageType storageType)
    {
        this.storageType = storageType;
    }
    
    private synchronized void initialize()
    {
        setStorageType(FramePositionStorageType.getType((String) PropertyProviderImpl.getInstance().getProperty(StandardProperty.PROPERTY_POSITION_STORAGE)));
        loadProperties();
    }
}
