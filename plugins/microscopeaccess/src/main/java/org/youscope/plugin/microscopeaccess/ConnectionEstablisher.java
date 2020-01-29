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
package org.youscope.plugin.microscopeaccess;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

import org.youscope.addon.microscopeaccess.MicroscopeConnectionException;
import org.youscope.addon.microscopeaccess.MicroscopeInternal;

/**
 * @author langmo
 *
 */
class ConnectionEstablisher
{
	private static boolean dllsLoaded = false;
	private final static boolean DEBUG = false;
	
	
	private static URL getThisPackageURL() throws MicroscopeConnectionException
	{
		URL classHomeUrl;
		String thisClassResourceString = ConnectionEstablisher.class.getName().replace('.', '/') + ".class";
		String classHomeString = ConnectionEstablisher.class.getClassLoader().getResource(thisClassResourceString).toString();
		classHomeString = classHomeString.substring(0, classHomeString.lastIndexOf(thisClassResourceString));
		try
		{
			classHomeUrl = new URL(classHomeString);
		}
		catch(MalformedURLException e1)
		{
			throw new MicroscopeConnectionException("URL to microscope access classes is not valid.", e1);
		}
		return classHomeUrl;
	}
	public static MicroscopeInternal createMicroscopeConnection(File driverFolder, File jarFile, boolean loadLibraries, boolean addLibraryFolder, boolean changeSystemLibraryPath) throws MicroscopeConnectionException
	{
		// Add microManager library folder to the system library path. The microManager main JAR file (MMCoreJ.jar) loads
		// the microManager main dll (MMCoreJ_wrap.dll) via JNI. This step ensures, that JNI finds the dll.
		if(changeSystemLibraryPath)
		{
			if(!driverFolder.exists())
				throw new MicroscopeConnectionException("The folder where the device drivers should be (" + driverFolder.toString() + ") does not exist.");
	        System.setProperty("java.library.path", driverFolder.getAbsolutePath() + File.pathSeparator + System.getProperty("java.library.path"));
	        System.out.println("Library path set to " + System.getProperty("java.library.path"));
		}
		
		// Loads all microManager driver DLLs. 
		// This step is necessary, if microManager does not find the respective DLLs on its own, e.g. since the folder containing them is not
		// on the system path.
		if(loadLibraries)
		{
			if(!driverFolder.exists())
				throw new MicroscopeConnectionException("The folder where the device drivers should be (" + driverFolder.toString() + ") does not exist.");
			automaticallyLoadDynamicLibraries(driverFolder);
		}
		
		// Get URL to microManager JAR file.
		if(!jarFile.exists())
		{
			throw new MicroscopeConnectionException("The URL to the MicroManager JAR file could not be found under " + jarFile + ".");
		}
		URL jarURL;
		try
		{
			jarURL = jarFile.toURI().toURL();
		}
		catch(MalformedURLException e1)
		{
			throw new MicroscopeConnectionException("The URL to the MicroMicroManager JAR file (" + jarFile + ") is not valid.", e1);
		}
		
		// Initialize class loader to load the microManager JAR file and connect it to YouScope.
		URL thisPackageUrl = getThisPackageURL();
		URL[] classLoaderURLs = new URL[]{jarURL, thisPackageUrl};
		// TODO: where and when to close classLoader? Are loaded classes invalid after class loader has been closed?
		@SuppressWarnings("resource")
		MicroscopeClassLoader classLoader = new MicroscopeClassLoader(classLoaderURLs, ConnectionEstablisher.class.getClassLoader());

		// Load microManager class and initialize a microManager object.
		Class<?> microManagerClass;
		Object microManager;
		try
		{
			microManagerClass = classLoader.loadClass("mmcorej.CMMCore");
			Constructor<?> microManagerConstructor = microManagerClass.getConstructor();
			microManagerConstructor.setAccessible(true);
			microManager = microManagerConstructor.newInstance();
		}
		catch(ClassNotFoundException e)
		{
			throw new MicroscopeConnectionException("Could not find Micro-Manager main class.\n\n" + getSystemConfiguration(driverFolder, jarFile), e);
		}
		catch(SecurityException e)
		{
			throw new MicroscopeConnectionException("Access to Micro-Manager classes is not allowed due to security settings.", e);
		}
		catch(NoSuchMethodException e)
		{
			throw new MicroscopeConnectionException("The interfaces of Micro-Manager is not compatible to the interface of the selected connection method.", e);
		}
		catch(IllegalArgumentException e)
		{
			throw new MicroscopeConnectionException("The interfaces of Micro-Manager is not compatible to the interface of the selected connection method.", e);
		}
		catch(InstantiationException e)
		{
			throw new MicroscopeConnectionException("The interfaces of Micro-Manager is not compatible to the interface of the selected connection method.", e);
		}
		catch(IllegalAccessException e)
		{
			throw new MicroscopeConnectionException("The interfaces of Micro-Manager is not compatible to the interface of the selected connection method.", e);
		}
		catch(InvocationTargetException e)
		{
			if(e.getCause() instanceof NoClassDefFoundError)
				throw new MicroscopeConnectionException("Could not find definition of a Micro-Manager class.\n\n" + getSystemConfiguration(driverFolder, jarFile), e.getCause());
			else if(e.getCause() instanceof UnsatisfiedLinkError)
				throw new MicroscopeConnectionException("Could not load driver libraries (DLLs in Windows).\n\n" + getSystemConfiguration(driverFolder, jarFile), e.getCause());
			else
				throw new MicroscopeConnectionException("Connection failed due to internal error in driver connection.\n\n" + getSystemConfiguration(driverFolder, jarFile), e.getCause());
		}
		
		// Debug log
		if(DEBUG)
		{
			try
			{
				Method enableDebugLog = microManagerClass.getMethod("enableDebugLog", new Class<?>[] {boolean.class});
				enableDebugLog.setAccessible(true);
				enableDebugLog.invoke(microManager, new Object[] {true});
			}
			catch(Exception e)
			{
				throw new MicroscopeConnectionException("Could not enable MicroManager debug log.", e);
			}
		}
		
		// Tell microManager where its driver libraries are.
		// This is only neccessary if microManager does not find them automatically, which is true either if
		// the driver folder is in the OS's path variable or the drivers reside in a subfolder of the folder where YouScope is.
		if(addLibraryFolder)
		{
			try
			{
				Method addSearchPath = microManagerClass.getMethod("addSearchPath", new Class<?>[] {String.class});
				addSearchPath.setAccessible(true);
				System.out.println("Adding folder " + driverFolder.getAbsolutePath() + " to driver path.");
				addSearchPath.invoke(microManager, new Object[] {driverFolder.getAbsolutePath()});
			}
			catch(Exception e)
			{
				throw new MicroscopeConnectionException("Error in searching for device libraries.", e);
			}
		}

		// Connect YouScope with microManager.
		final String microscopeClass = ConnectionEstablisher.class.getPackage().getName() + ".MicroscopeImpl";
		MicroscopeInternal microscope;
		try
		{
			Class<?> microscopeImplClass = classLoader.loadClass(microscopeClass);
			Constructor<?> microscopeImplConstructor = microscopeImplClass.getConstructor(microManagerClass, String.class);
			microscopeImplConstructor.setAccessible(true);
			microscope = (MicroscopeInternal)microscopeImplConstructor.newInstance(microManager, driverFolder.getAbsolutePath());
			
		}
		catch(Exception e)
		{
			throw new MicroscopeConnectionException("Device interface was loaded, but internal error occured while using its functions.", e);
		}
		
		return microscope;
	}
	private static String getSystemConfiguration(File driverFolder, File jarFile)
	{
		String debugMessage = "Possible Solutions:\n";
		debugMessage += "Check if the installed device drivers (32bit/64bit) correspond to your operating systems(32bit/64bit).\n";
		debugMessage += "Check if the connection method requires the driver folder to be defined in the OS path (e.g. System Properties -> Environmental Variables -> Path).\n";
		debugMessage += "\nSystem information:\n";
		debugMessage += "Expected Driver Folder: " + driverFolder.getAbsolutePath() + "\n";
		debugMessage += "Expected JAR file location: " + jarFile.getAbsolutePath() + "\n";
		debugMessage += "Java library path: " + System.getProperty("java.library.path") + "\n";
		debugMessage += "Path: " + System.getProperty("PATH") + "\n";
		debugMessage += "Operating System: " + System.getProperty("os.name") + "\n";
		debugMessage += "32/64bit: " + System.getProperty("os.arch") + "\n";
		return debugMessage;
	}
	
	private static void automaticallyLoadDynamicLibraries(File driverPath)
	{
		// Java does not like it if one loads a dll twice...
		if(dllsLoaded)
			return;
		dllsLoaded = true;
		
        File[] filesToLoad = driverPath.listFiles(new FilenameFilter()
        	{
				@Override
				public boolean accept(File dir, String name)
				{
					if(!new File(dir, name).isFile())
						return false;
					name = name.toLowerCase();
					if(name.lastIndexOf(".dll") != name.length()-4)
						return false;
					if(name.indexOf("mmgr_") == 0)
						return true;
					if(name.indexOf("mmcorej_wrap") >= 0)
						return false;
					return false;
				}
        	});
        
        for(File libraryFile : filesToLoad)
        {
			try
			{
				System.load(libraryFile.getAbsolutePath());
				//System.out.println("Library "+ libraryFile.toString() + ") successfuly loaded.");
			}
			catch(Throwable e)
			{
				System.out.println("Failed to load library " + libraryFile.getName() + ": " + e.toString() );
			}
        }
	}
	
}
