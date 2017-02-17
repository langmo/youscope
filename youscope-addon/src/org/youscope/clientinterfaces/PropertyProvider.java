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
package org.youscope.clientinterfaces;

/**
 * Interface through which an addon can save properties as well as access the properties of the YouScope client.
 * 
 * @author Moritz Lang
 */
public interface PropertyProvider 
{
    /**
     * Returns the property with the given name, or default value if the property is not yet set or set invalidly. Properties are saved permanently,
     * such that a property set e.g. in one session can be re-obtained in another.
     * 
     * @param name The name of the property.
     * @param defaultValue Default value to return if property is not set or invalid.
     * @return The value of the property with the given name, or the default value.
     */
    String getProperty(String name, String defaultValue);

    /**
     * Sets the standard property to the given value. A type check is performed to determine if value is of the correct type.
     * If not, it is tried to transform value into the correct type. If this is not possible, nothing is done.
     * value should be of type {@link Double}, {@link Integer}, {@link Boolean}, or {@link String}.
     * @param property The property whose value should be set.
     * @param value The value of the property.
     */
    void setProperty(StandardProperty property, Object value);
    
    /**
     * Returns the current value of the standard property. The return value is of type {@link Double}, {@link Integer}, {@link Boolean}, or {@link String}.
     * @param property Default property whose value should be returned.
     * @return Value of property.
     */
    Object getProperty(StandardProperty property);
    
    /**
     * Returns the property with the given name, or default value if the property is not yet set or set invalidly. Properties are saved permanently,
     * such that a property set e.g. in one session can be re-obtained in another.
     * 
     * @param name The name of the property.
     * @param defaultValue Default value to return if property is not set or invalid.
     * @return The value of the property with the given name, or the default value.
     */
    int getProperty(String name, int defaultValue);

    /**
     * Returns the property with the given name, or default value if the property is not yet set or set invalidly. Properties are saved permanently,
     * such that a property set e.g. in one session can be re-obtained in another.
     * 
     * @param name The name of the property.
     * @param defaultValue Default value to return if property is not set or invalid.
     * @return The value of the property with the given name, or the default value.
     */
    double getProperty(String name, double defaultValue);

    /**
     * Returns the property with the given name, or default value if the property is not yet set or set invalidly. Properties are saved permanently,
     * such that a property set e.g. in one session can be re-obtained in another.
     * 
     * @param name The name of the property.
     * @param defaultValue Default value to return if property is not set or invalid.
     * @return The value of the property with the given name, or the default value.
     */
    boolean getProperty(String name, boolean defaultValue);

    /**
     * Sets the property with the given name to the given value. Properties are saved permanently, such that a property set e.g. in one session can be
     * re-obtained in another.
     * 
     * @param name Name of the property to set.
     * @param value Value to set the property to.
     */
    void setProperty(String name, String value);

    /**
     * Sets the property with the given name to the given value. Properties are saved permanently, such that a property set e.g. in one session can be
     * re-obtained in another.
     * 
     * @param name Name of the property to set.
     * @param value Value to set the property to.
     */
    void setProperty(String name, int value);

    /**
     * Sets the property with the given name to the given value. Properties are saved permanently, such that a property set e.g. in one session can be
     * re-obtained in another.
     * 
     * @param name Name of the property to set.
     * @param value Value to set the property to.
     */
    void setProperty(String name, double value);

    /**
     * Sets the property with the given name to the given value. Properties are saved permanently, such that a property set e.g. in one session can be
     * re-obtained in another.
     * 
     * @param name Name of the property to set.
     * @param value Value to set the property to.
     */
    void setProperty(String name, boolean value);

    /**
     * Returns the property with the given name, or default value if the property is not yet set or set invalidly. Properties are saved permanently,
     * such that a property set e.g. in one session can be re-obtained in another.
     * 
     * @param name The name of the property.
     * @param defaultValue Default value to return if property is not set or invalid.
     * @return The value of the property with the given name, or the default value.
     */
    String[] getProperty(String name, String[] defaultValue);
}
