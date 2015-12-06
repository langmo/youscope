package org.youscope.addon.tool;

import javax.swing.Icon;

/**
 * Provides metadata about a given tool.
 * @author Moritz Lang
 *
 */
public interface ToolMetadata 
{
	/**
     * Returns a short human readable name of the tool.
     * @return Human readable name of the tool.
     */
    String getTypeName();
    
    /**
	 * Returns the type identifier of the tool.
	 * 
	 * @return Type identifier of the tool.
	 */
	String getTypeIdentifier();
	
	/**
	 * Returns an icon representative for this tool, or null if no icon is set.
	 * @return Icon representative of this configuration type.
	 */
	Icon getIcon();
	
	/**
	 * Returns an array of strings (possibly of length 0) specifying the classification of the tool.
	 * This classification can e.g. be used to order tools into a certain folder structure.
	 * @return classification of tool.
	 */
	String[] getClassification();
	
	/**
     * Returns the interface of the tool. Typically, this is equal to {@link ToolAddonUI#getClass()}. 
     * Some tools however provide additional interface functions defined in a specific interface, and
     * these interfaces are exposed by this funcion.
     * @return Tool interface class.
     */
    Class<? extends ToolAddonUI> getToolInterface();

}
