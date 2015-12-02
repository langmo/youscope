/**
 * 
 */
package ch.ethz.csb.youscope.client.addon.tool;

import javax.swing.ImageIcon;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.shared.YouScopeServer;

/**
 * @author langmo
 */
public interface ToolAddonFactory
{

    /**
     * Returns a new ToolAddon for the given ID, or null if addon does not support tools with the given ID.
     * 
     * Remark: tool IDs are not case dependent.
     * 
     * @param toolID The ID for which a tool should be created.
     * @param client Interface to allow the addon to communicate with the client.
     * @param server Interface to the server.
     * 
     * @return New Addon.
     */
    ToolAddon createToolAddon(String toolID, YouScopeClient client, YouScopeServer server);    
    
    /**
	 * Returns a list of all tool types supported by this addon
	 * 
	 * @return List of supported tool types.
	 */
	String[] getSupportedToolIDs();

	/**
	 * Returns true if this addon supports tools with the given ID, false otherwise.
	 * 
	 * Remark: tool IDs are not case dependent.
	 * 
	 * @param toolID The ID of the tool for which it should be queried if this addon supports it.
	 * @return True if this addon supports tools with the given ID, false otherwise.
	 */
	boolean supportsToolID(String toolID);

    /**
     * Should return a short human readable name of the tool which corresponds to the given ID. If the addon does
     * not support tools with the given ID, null should be returned.
     * The name may or may not consist of sub-strings separated by slashes (e.g. "Misc/Foo"). The last string corresponds to the base name.
     * In this case, a client may or may not display some kind of folder structure to navigate to a given addon.
     * @param toolID The ID of the tool for which the human readable name should be returned.
     * 
     * @return Human readable name of the tool.
     */
    String getToolName(String toolID);
    
    /**
     * Returns an icon for this tool, or null, if this tool does not have an own icon.
     * @param toolID The ID of the tool for which the icon should be returned.
     * @return Icon for the tool, or null.
     */
    ImageIcon getToolIcon(String toolID);
}
