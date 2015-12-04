package org.youscope.addon.component;

import javax.swing.ImageIcon;

import org.youscope.common.configuration.Configuration;
import org.youscope.common.measurement.Component;

/**
 * Provides metadata about a given configuration type.
 * @author Moritz Lang
 * @param <C> The specific class of configurations for which this is metadata.
 *
 */
public interface ComponentMetadata<C extends Configuration> 
{
	/**
     * Returns a short human readable name of the configuration type.
     * @return Human readable name of the configuration type.
     */
    String getTypeName();
    
    /**
     * Returns the class of the configuration.
     * The class should have a public no-argument constructor, such that newInstance() can be called. 
     * @return Configuration class.
     */
    Class<C> getConfigurationClass();
    
    /**
	 * Returns the type identifier of the configuration type.
	 * 
	 * @return Type identifier of the configuration.
	 */
	String getTypeIdentifier();
	
	/**
	 * Returns an icon representative for this configuration type, or null if no icon is set.
	 * @return Icon representative of this configuration type.
	 */
	ImageIcon getIcon();
	
	/**
	 * Returns an array of strings (possibly of length 0) specifying the classification of the configuration.
	 * This classification can e.g. be used to order configurations into a certain folder structure.
	 * @return classification of configuration.
	 */
	String[] getConfigurationClassification();
	
	/**
     * Returns the interface of the measurement component created when the configuration is parsed. 
     * This should not be the implementation of the component, but rather
     * a public interface implementing Remote.
     * @return Component interface class.
     */
    Class<? extends Component> getComponentInterface();

}
