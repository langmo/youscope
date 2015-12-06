package org.youscope.addon.component;

import javax.swing.Icon;

import org.youscope.common.configuration.Configuration;
import org.youscope.common.measurement.Component;

/**
 * Provides metadata about a given measurement component.
 * @author Moritz Lang
 * @param <C> The class of configuration of this measurement component.
 *
 */
public interface ComponentMetadata<C extends Configuration> 
{
	/**
     * Returns a short human readable name of the component.
     * @return Human readable name of the component.
     */
    String getTypeName();
    
    /**
     * Returns the class of the configuration of the measurement component.
     * The class should have a public no-argument constructor, such that newInstance() can be called. 
     * @return Configuration class of component.
     */
    Class<C> getConfigurationClass();
    
    /**
	 * Returns the type identifier of the component. This is a unique string
	 * with which a given component type is globally identified.
	 * 
	 * @return Type identifier of the component.
	 */
	String getTypeIdentifier();
	
	/**
	 * Returns an icon representative for this component, or null if no icon is set.
	 * @return Icon representative of this component.
	 */
	Icon getIcon();
	
	/**
	 * Returns an array of strings (possibly of length 0) specifying the classification of the component.
	 * This classification can e.g. be used to order components into a certain folder structure.
	 * @return classification of component.
	 */
	String[] getClassification();
	
	/**
     * Returns the interface of the measurement component created when the configuration is parsed. 
     * This should not be the implementation of the component, but rather
     * a public interface implementing Remote.
     * @return Component interface.
     */
    Class<? extends Component> getComponentInterface();

}
