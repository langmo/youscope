package ch.ethz.csb.youscope.client.addon;

import java.awt.Component;

import ch.ethz.csb.youscope.shared.addon.AddonException;
import ch.ethz.csb.youscope.shared.configuration.Configuration;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationMetadata;

/**
 * An addon providing a user interface for the configuration of a component.
 * @author Moritz Lang
 *
 * @param <C>
 */
public interface ConfigurationAddon<C extends Configuration> 
{
	/**
	 * Creates a frame containing the UI elements of this addon. The returned frame should, yet, not be visible (<code>YouScopeFrame.setVisible(true)</code> should be
	 * called by the invoker of this function). A new frame can be created by calling <code>YouScopeClient.createFrame()</code>. The caller can decide to add this frame
	 * as a child or modal child frame to the frame the caller elements are displayed in by calling on its frame <code>addChildFrame()</code> or <code>addModalChildFrame()</code>.
	 * Only one of the functions <code>toFrame()</code> or <code>toPanel()</code> must be called for a given addon. Furthermore, this function must not be called more than once.
	 * To create more than one UI representation of a given addon type, create a second addon using the corresponding factory.
	 * @return The frame containing the UI elements.
	 * @throws AddonException thrown if an error occurs during preparation of the frame.
	 */
	YouScopeFrame toFrame() throws AddonException;
	
	/**
	 * Creates a component (usually a panel) containing the UI elements of this addon. 
	 * The addon should not close the containing frame, nor provide UI elements (e.g. buttons) closing the frame when invoked.
	 * Only one of the functions <code>toFrame()</code> or <code>toPanel()</code> must be called for a given addon. Furthermore, this function must not be called more than once.
	 * To create more than one UI representation of a given addon type, create a second addon using the corresponding factory.
	 * @param containingFrame The frame containing the UI elements. Note that this frame is not necessarily visible when this function is invoked. The reference to the containing frame can e.g. 
	 * be used to register frame listeners to get notified when the containing frame closes.
	 * @return The AWT component containing the UI elements.
	 * @throws AddonException thrown if an error occurs during preparation of the panel.
	 */
	Component toPanel(YouScopeFrame containingFrame) throws AddonException;

    /**
     * Initializes the addon to the configuration data.
     * Must not be called after toXXXFrame() or toPanel() was called.
     * 
     * @param configuration The configuration data.
     * @throws AddonException Thrown if error occurred while processing configuration data.
     * @throws ConfigurationException Thrown if configuration is invalid.
     */
    void setConfiguration(Configuration configuration) throws AddonException, ConfigurationException;

    /**
     * Returns the configuration data. If toXXXFrame() or toPanel() was called already, it is expected that the addon
     * commits all current edits to the configuration. Otherwise, a default configuration should be returned. If possible, the configuration
     * should be at a valid state, which is however not necessary (see {@link Configuration#checkConfiguration()}).
     * 
     * @return Configuration data or NULL, if addon was not yet configured.
     */
    C getConfiguration();
    
    /**
     * Adds a listener to this configuration, which should e.g. be informed if the configuration finished.
     * @param listener The listener to add.
     */
    void addConfigurationListener(ConfigurationAddonListener<? super C> listener);
    
    /**
     * Removes a previously added listener.
     * @param listener The listener to remove.
     */
    void removeConfigurationListener(ConfigurationAddonListener<? super C> listener);
	
	/**
     * Returns the metadata (like human readable name) for the configurations created by this addon.
     * @return Metadata of the configuration
     */
    ConfigurationMetadata<C> getConfigurationMetadata();
}
