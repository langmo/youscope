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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.youscope.addon.ConfigurationManagement;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.measurement.MeasurementConfiguration;
import org.youscope.common.util.ConfigurationTools;

class LastMeasurementManager
{
    private static final int NUM_LAST_MEASUREMENTS_STORED = 5; 
    private static final String CONFIG_FILE_NAME_FORMAT = "lastMeasurement_%1$tF_%1$tH-%1$tM-%1$tS.csb";
    private static LastMeasurementManager singleton = null;
    private final ArrayList<MeasurementConfiguration> lastMeasurements = new ArrayList<MeasurementConfiguration>();
    private final String LAST_MEASUREMENTS_PATH = "configuration" + File.separator + "last_measurements";
    private LastMeasurementManager()
    {
        // singleton.
    }
    public synchronized static LastMeasurementManager getLastMeasurementManager()
    {
        if(singleton == null)
        {
            singleton = new LastMeasurementManager();
            singleton.loadLastMeasurements();
        }
        return singleton;
    }
    private void loadLastMeasurements()
    {
        File folder = new File(LAST_MEASUREMENTS_PATH);
        if(!folder.exists() || !folder.isDirectory())
            return;
        File[] lastConfigs = folder.listFiles(new FilenameFilter()
            {
                @Override
                public boolean accept(File dir, String name)
                {
                    return name.endsWith(".csb");
                }
            });
        Arrays.sort(lastConfigs);
        for(File lastConfig : lastConfigs)
        {
            MeasurementConfiguration configuration;
            try
            {
                configuration = (MeasurementConfiguration) ConfigurationManagement.loadConfiguration(lastConfig.getAbsolutePath());
            } catch (@SuppressWarnings("unused") Throwable e)
            {
                continue;
            }
            lastMeasurements.add(configuration);
        }
    }
    public void addMeasurement(MeasurementConfiguration configuration)
    {
    	try 
    	{
    		// Make local copy.
    		configuration = ConfigurationTools.deepCopy(configuration, MeasurementConfiguration.class);
		} 
    	catch (ConfigurationException e) 
    	{
			ClientSystem.err.println("Could not add measurement configuration to last measurements folder.", e);
			return;
		}
        synchronized(lastMeasurements)
        {
            lastMeasurements.add(configuration);
        }
        String filePath = LAST_MEASUREMENTS_PATH + File.separator + String.format(CONFIG_FILE_NAME_FORMAT, new Date());
        File file = new File(filePath);
        if(!file.exists())
            trimSavedMeasurementsTo(NUM_LAST_MEASUREMENTS_STORED - 1);
        File folder = new File(LAST_MEASUREMENTS_PATH);
        if(!folder.exists())
        {
            if(!folder.mkdirs())
            {
                ClientSystem.err.println("Could not create last measurements folder.");
                return;
            }
        }
        try
        {
            ConfigurationManagement.saveConfiguration(file.getAbsolutePath(), configuration);
        } catch (IOException e)
        {
            ClientSystem.err.println("Could not add measurement configuration to last measurements folder.", e);
        }
        
    }
    
    public MeasurementConfiguration[] getMeasurements()
    {
        synchronized(lastMeasurements)
        {
            MeasurementConfiguration[] returnVal = new MeasurementConfiguration[lastMeasurements.size()];
            for(int i=0; i<lastMeasurements.size(); i++)
            {
                try
                {
                    returnVal[i] = ConfigurationTools.deepCopy(lastMeasurements.get(i), MeasurementConfiguration.class);
                } catch (@SuppressWarnings("unused") ConfigurationException e)
                {
                    returnVal[i] = lastMeasurements.get(i);
                }
            }
            return returnVal;
        }
    }
    
    private synchronized void trimSavedMeasurementsTo(int trimTo)
    {
        File folder = new File(LAST_MEASUREMENTS_PATH);
        if(!folder.exists() || !folder.isDirectory())
            return;
        File[] lastConfigs = folder.listFiles(new FilenameFilter()
            {
                @Override
                public boolean accept(File dir, String name)
                {
                    return name.endsWith(".csb");
                }
            });
        if(lastConfigs.length <= trimTo)
            return;
        // throw away oldest configs. These are identified by ordering of their names, since we name them by config time.
        String[] lastConfigNames = new String[lastConfigs.length];
        for(int i=0;i<lastConfigs.length; i++)
        {
            lastConfigNames[i] = lastConfigs[i].getName();
        }
        Arrays.sort(lastConfigNames);
        for(int i=lastConfigNames.length - trimTo - 1; i>=0; i--)
        {
            File file = new File(LAST_MEASUREMENTS_PATH + File.separator + lastConfigNames[i]);
            try
            {
                if(!file.exists() || !file.isFile())
                    throw new Exception("Could not locate file.");
                if(!file.delete())
                    throw new Exception("Could not delete file.");
            }
            catch(Exception e)
            {
                ClientSystem.err.println("Could not delete last measurement file ("+file.getAbsolutePath()+").", e);
            }
        }
    }
}
