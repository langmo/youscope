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
package org.youscope.template.tool;

import org.youscope.addon.tool.ToolAddonFactory;
import org.youscope.addon.tool.ToolAddonFactoryAdapter;
import org.youscope.addon.tool.ToolMetadata;

/**
 * A tool addon factory represents the interface between YouScope and your tool. It allows YouScope to gather basic information
 * about your tool, like it's name or an icon reprenting the tool, to automatically incorporate the possibility to open
 * your tool into the respective menus of YouScope.
 * Additionally, the factory provides a way to actually create an instance of your tool, which is done when the user request that
 * the tool should be opened by clicking on corresponding menu items in YouScope. Importantly, a factory does not implement any user interface
 * nor accesses the microscope directly. Instead, this is done in the tool created by the factory.
 * 
 * Each tool factory has to implement {@link ToolAddonFactory}, which provides the needed functionality for the interface with YouScope.
 * However, implementing this interface directly is rather complex and tedious. To simplify the creation of tool addon factories, it is
 * therefore more convenient to extend {@link ToolAddonFactoryAdapter} instead of implementing {@link ToolAddonFactory}. The abstract
 * adapter class takes over implementing most of the boilerplate functions for you, such that you can concentrate on developing your tool.
 * Only seldom, your tool is "so special" that extending the adapter is not appropriate since the tool has to be constructed differently
 * to all other tools already available; in these seldom cases, it is still possible to implement {@link ToolAddonFactory} directly.  
 * 
 * Note: if you change the name or the package path of this class, you have to adjust the content of the file 
 * META-INF/services/org.youscope.addon.tool.ToolAddonFactory
 * accordingly for YouScope to be able to find the factory in the plugin jar file.
 * @author Moritz Lang
 *
 */
public class TemplateToolFactory extends ToolAddonFactoryAdapter
{
	/**
	 * Constructor. Calling the superclass constructor {@link ToolAddonFactoryAdapter#ToolAddonFactoryAdapter(Class, org.youscope.addon.tool.ToolMetadata)}
	 * the adapter provides YouScope with the metadata of your tool (like its name), which is an instance of the interface {@link ToolMetadata}.
	 * Furthermore, the superclass constructor specifies the class of which an instance should be
	 * constructed when the user chooses to use your tool, e.g. the class of your tool itself.
	 * 
	 * We also do not have to bother to implement the functions of {@link ToolMetadata} ourselves. Instead, we call the function {@link TemplateTool#getMetadata()}.
	 * 
	 */
	public TemplateToolFactory()
	{
		super(TemplateTool.class, TemplateTool.getMetadata());
	}
}
