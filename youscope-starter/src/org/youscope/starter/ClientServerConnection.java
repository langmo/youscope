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
package org.youscope.starter;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.Attributes;

/**
 * @author Moritz Lang
 */
abstract class ClientServerConnection
{
    private static final String PLUGINS_LOCATION = "plugins/";
    private static final String LIB_LOCATION = "lib/";

    private String getJarClassPath(URL url) throws IOException
    {
        JarURLConnection uc;
        Attributes attr;
        uc = (JarURLConnection) createJARURL(url).openConnection();
        attr = uc.getMainAttributes();
        return (attr != null ? attr.getValue(Attributes.Name.CLASS_PATH) : null);
    }

    abstract boolean exists();

    static List<URL> getPluginsJars() throws MalformedURLException
    {
    	System.out.println("================================");
        System.out.println("= Searching for plugin jars    =");
        System.out.println("================================");
    	
        File pluginsFolder = new File(PLUGINS_LOCATION); 
        String[] plugins = pluginsFolder.list(new FilenameFilter()
            {
                @Override
                public boolean accept(File dir, String name)
                {
                    if (name.toLowerCase().lastIndexOf(".jar") == name.length() - 4)
                        return true;
					return false;
                }
            });
        if (plugins == null)
        {
        	System.out.println("No plugins found.");
            return new ArrayList<URL>(0);
        }
        ArrayList<URL> urls = new ArrayList<URL>(plugins.length);
        for (String plugin : plugins)
        {
        	File pluginFile = new File(PLUGINS_LOCATION + plugin);
        	System.out.println("Found plugin " + pluginFile.getName() + ".");
            if (!pluginFile.exists())
                continue;
            URL pluginURL = pluginFile.toURI().toURL();
            urls.add(pluginURL);
        }

        return urls;

    }
    
    static List<URL> getLibJars() throws MalformedURLException
    {
    	System.out.println("================================");
        System.out.println("= Searching for library jars    =");
        System.out.println("================================");
    	
        File pluginsFolder = new File(LIB_LOCATION);
        String[] plugins = pluginsFolder.list(new FilenameFilter()
            {
                @Override
                public boolean accept(File dir, String name)
                {
                    if (name.toLowerCase().lastIndexOf(".jar") == name.length() - 4)
                        return true;
					return false;
                }
            });
        if (plugins == null)
        {
        	System.out.println("No libs found.");
            return new ArrayList<URL>(0);
        }
        ArrayList<URL> urls = new ArrayList<URL>(plugins.length);
        for (String plugin : plugins)
        {
        	File pluginFile = new File(PLUGINS_LOCATION + plugin);
        	System.out.println("Found lib " + pluginFile.getName() + ".");
            if (!pluginFile.exists())
                continue;
            URL pluginURL = pluginFile.toURI().toURL();
            urls.add(pluginURL);
        }

        return urls;

    }

    abstract HashSet<URL> getNecessaryJARs() throws MalformedURLException;

    /**
     * A jar file may require other jar files. this method returns a list of them.
     * 
     * @param jarUrl
     */
	protected HashSet<URL> getSubJars(URL jarUrl, HashSet<URL> subJarURLS)
    {
		if(subJarURLS == null)
		{
			System.out.println("================================");
	        System.out.println("= Searching for dependent jars =");
	        System.out.println("================================");
			
			subJarURLS = new HashSet<URL>();
		}
		
		// Add oneself
		if(!subJarURLS.add(jarUrl))
			return subJarURLS;
		
        String jarClassPath;
        File parentFile;
        try
        {
        	parentFile = new File(jarUrl.toURI());
        	System.out.print("Analyzing " + parentFile.getName() + "...");
            jarClassPath = getJarClassPath(jarUrl);
            if(jarClassPath == null)
            {
            	System.out.println(" no dependencies!");
            }
            else
            {
            	System.out.println(" dependent files found:");
            	String[] dependencies = jarClassPath.split(" ");
            	for(String dependency : dependencies)
            	{
            		System.out.println("  - " + dependency);
            	}
            }
            parentFile = parentFile.getParentFile();
        } 
        catch (@SuppressWarnings("unused") IOException e1)
        {
        	System.out.println("Could not get dependent JARS of JAR " + jarUrl.toString() + " (could not load JAR).");
        	
            // Could not find jar file or classpath in manifest -> ignore.
            return subJarURLS;
        }
		catch(@SuppressWarnings("unused") URISyntaxException e)
		{
			System.out.println("Could not get dependent JARS of JAR " + jarUrl.toString() + " (URI syntax wrong).");
			
			return subJarURLS;
		}
        if (jarClassPath == null)
            return subJarURLS;

        // iterate over all elements in the jar file's CLASS_PATH
        // Class-path attribute is composed of space-separated values.
        StringTokenizer tokenizer = new StringTokenizer(jarClassPath);
        while (tokenizer.hasMoreElements())
        {
            String element = tokenizer.nextToken();
            if (element.equals(""))
            {
                continue;
            }
            try
            {
                // Create Jar URL
            	URL url = new File(parentFile, element).getCanonicalFile().toURI().toURL();
            	  
                //URL url = new URL(new URL(file), element);
                //URL subJarURL = createJARURL(url);
                // Add Jar URL and sub-sub-jars.
                getSubJars(url, subJarURLS);
            } 
            catch (@SuppressWarnings("unused") MalformedURLException e)
            {
                System.out.println("JAR file malformed: \"" + parentFile + "\"  -->  \"" + element + "\"");
            }
			catch(@SuppressWarnings("unused") IOException e)
			{
				System.out.println("JAR file could not be converted to canonical form: \"" + parentFile + "\"  -->  \"" + element + "\"");
			}

        }
        return subJarURLS;
    }

    protected URL createJARURL(URL url) throws MalformedURLException
    {
        // URL url = new File(urlString).toURI().toURL();
        return new URL("jar", "", url + "!/");
    }

    public abstract boolean isConnected();
}
