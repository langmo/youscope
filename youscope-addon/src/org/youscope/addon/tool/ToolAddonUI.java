/**
 * 
 */
package org.youscope.addon.tool;

import org.youscope.addon.AddonException;
import org.youscope.clientinterfaces.YouScopeFrame;

/**
 * Provides user interface of tools.
 * @author Moritz Lang.
 */
public interface ToolAddonUI
{
	/**
	 * Creates a frame containing the UI elements of this tool. The returned frame should, yet, not be visible (<code>YouScopeFrame.setVisible(true)</code> should be
	 * called by the invoker of this function). A new frame can be created by calling <code>YouScopeClient.createFrame()</code>. The caller can decide to add this frame
	 * as a child or modal child frame to the frame the caller elements are displayed in by calling on its frame <code>addChildFrame()</code> or <code>addModalChildFrame()</code>.
	 * Only one of the functions <code>toFrame()</code> or <code>toPanel()</code> must be called for a given tool. Furthermore, this function must not be called more than once.
	 * To create more than one UI representation of a given tool type, create a second tool using the corresponding factory.
	 * @return The frame containing the UI elements of the tool.
	 * @throws AddonException thrown if an error occurs during construction of the UI.
	 */
	YouScopeFrame toFrame() throws AddonException;
	
	/**
	 * Creates a awt component (usually a panel) containing the UI elements of this tool. 
	 * The tool should not close the containing frame, nor provide UI elements (e.g. buttons) closing the frame when invoked.
	 * Only one of the functions <code>toFrame()</code> or <code>toPanel()</code> must be called for a given tool. Furthermore, this function must not be called more than once.
	 * To create more than one UI representation of a given tool type, create a second tool using the corresponding factory.
	 * @param containingFrame The frame containing the UI elements. Note that this frame is not necessarily visible when this function is invoked. The reference to the containing frame can e.g. 
	 * be used to register frame listeners to get notified when the containing frame closes.
	 * @return The AWT component containing the UI elements.
	 * @throws AddonException thrown if an error occurs during creation of the tool UI.
	 */
	java.awt.Component toPanel(YouScopeFrame containingFrame) throws AddonException;

    /**
     * Returns the metadata (like human readable name) for the tool.
     * @return Metadata of the tool.
     */
    ToolMetadata getToolMetadata();
}
