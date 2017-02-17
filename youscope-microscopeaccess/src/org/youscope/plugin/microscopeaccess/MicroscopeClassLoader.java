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

import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author langmo
 *
 */
class MicroscopeClassLoader extends URLClassLoader
{
	MicroscopeClassLoader(URL[] urls, ClassLoader parent)
	{
		super(urls, parent);
	}

	/**
	 * This implementation prevents the parent class loaders to load any implementation
	 * class. Implementation classes are identified by the name: They have an "Impl" in
	 * it.
	 */
	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
	{
		//if((name.indexOf("Impl") <= 0 || name.lastIndexOf("Impl") != name.length() - 4) && name.compareToIgnoreCase("mmcorej.CMMCore") != 0)
		if(name.indexOf(MicroscopeClassLoader.class.getPackage().getName()) != 0 && name.indexOf("mmcorej") != 0)
			return super.loadClass(name, resolve);
		// First, check if the class has already been loaded
		Class<?> c = findLoadedClass(name);
		if(c == null)
		{
			c = findClass(name);
		}
		if(resolve)
		{
			resolveClass(c);
		}
		return c;
	}
}
