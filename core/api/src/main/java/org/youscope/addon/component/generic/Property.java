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
package org.youscope.addon.component.generic;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.youscope.common.configuration.Configuration;
import org.youscope.common.configuration.YSConfigAlias;
import org.youscope.common.configuration.YSConfigConditional;
import org.youscope.common.configuration.YSConfigNotVisible;

/**
 * Class representing a property in the configuration.
 * @author Moritz Lang
 *
 */
public class Property 
{
	private String name;
	private final String propertyID;
	private final Class<? extends Configuration> configurationClass;
	private Class<?> type;
	private Method getter;
	private final Method setter;
	private final Method conditional;
	private boolean visible = true;
	/**
	 * Constructor.
	 * @param configurationClass The class of the configuration of which this is a property.
	 * @param propertyID The ID/name of the property.
	 * @throws GenericException
	 */
	public Property(Class<? extends Configuration> configurationClass, String propertyID) throws GenericException
    {
		this.configurationClass = configurationClass;
		this.propertyID = propertyID;
		name = propertyID;
		Field field;
		try 
		{
			field = configurationClass.getDeclaredField(propertyID);
		} 
		catch (NoSuchFieldException e) 
		{
			throw new GenericException("No property with name " + propertyID + " in class " + configurationClass.getName() + ".", e);
		}
		catch(SecurityException e)
		{
			throw new GenericException("Security exception thrown while analyzing class " + configurationClass.getName() + ".", e);
		}
		
    	name = field.getName();
    	type = field.getType();
    	String getterName = "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
    	String getterBooleanName = "is" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
    	String setterName = "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
    	
		try 
		{
			getter = configurationClass.getMethod(getterName);
		} 
		catch (NoSuchMethodException e) 
		{
			if(type.equals(boolean.class))
			{
				try 
				{
					getter = configurationClass.getMethod(getterBooleanName);
				} 
				catch (NoSuchMethodException e1) {
					throw new GenericException("Class " + configurationClass.getName() + " has property " + name + ", but not method " + getterName + "() or "+getterBooleanName+"().", e1);
				} catch (SecurityException e1) {
					throw new GenericException("Security exception thrown while analyzing class " + configurationClass.getName() + ".", e1);
				}
			}
			else 
				throw new GenericException("Class " + configurationClass.getName() + " has property " + name + ", but not method " + getterName + "().", e);
		}
		catch(SecurityException e)
		{
			throw new GenericException("Security exception thrown while analyzing class " + configurationClass.getName() + ".", e);
		}
		try 
		{
			setter = configurationClass.getMethod(setterName, type);
		} 
		catch (NoSuchMethodException e) 
		{
			throw new GenericException("Class " + configurationClass.getName() + " has property " + name + ", but not method " + setterName + "("+type.getName()+").", e);
		}
		catch(SecurityException e)
		{
			throw new GenericException("Security exception thrown while analyzing class " + configurationClass.getName() + ".", e);
		}
		
    	if(!Modifier.isPublic(getter.getModifiers()))
    		throw new GenericException("Method " + getterName + " is not public.");
    	if(!Modifier.isPublic(setter.getModifiers()))
    		throw new GenericException("Method " + setterName + " is not public.");
    	Class<?> getterReturnType = getter.getReturnType();
    	if(!getterReturnType.equals(type))
    		throw new GenericException("Method " + getterName + " has return type " + getterReturnType.getName() + " while property " + name + " has type " + type.getName() + ".");
    	
    	// get name alias
    	YSConfigAlias alias = field.getAnnotation(YSConfigAlias.class);
    	if(alias != null)
    	{
    		name = alias.value();
    	}
    	if(name.length() > 1)
    		name = name.substring(0, 1).toUpperCase()+name.substring(1);
    	
    	// get conditional function
    	YSConfigConditional conditionalAnnotation = field.getAnnotation(YSConfigConditional.class);
    	if(conditionalAnnotation != null)
    	{
    		String conditionalFunctionName = conditionalAnnotation.value();
    		try {
				conditional = configurationClass.getMethod(conditionalFunctionName);
			} catch (NoSuchMethodException e) {
				throw new GenericException("Class " + configurationClass.getName() + " has no method " + conditionalFunctionName + ", which was declared as a conditional function for property " + name + ".", e);
			} catch (SecurityException e) {
				throw new GenericException("Security exception thrown while analyzing class " + configurationClass.getName() + ".", e);
			}
    		if(!Modifier.isPublic(conditional.getModifiers()))
        		throw new GenericException("Method " + conditionalFunctionName + " is not public.");
    	}
    	else
    	{
    		conditional = null;
    	}
    	
    	YSConfigNotVisible notVisibleAnnotation = field.getAnnotation(YSConfigNotVisible.class);
    	if(notVisibleAnnotation != null)
    	{
    		visible = false;
    	}
    }
	
	/**
	 * Returns true if property should be shown in configuration.
	 * @return True if property should be shown.
	 */
	public boolean isVisible()
	{
		return visible;
	}
	
	/**
	 * Returns the getter method for this property.
	 * @return Getter method
	 */
	public Method getGetter()
	{
		return getter;
	}
	/**
	 * Returns the setter method for this property.
	 * @return setter method.
	 */
	public Method getSetter()
	{
		return setter;
	}
	
	/**
	 * Returns the (human readable) name of the property.
	 * @return name of property.
	 */
	public String getName()
	{
		return name;
	}
	/**
	 * Returns the property type.
	 * @return Property type.
	 */
	public Class<?> getType()
	{
		return type;
	}
	/**
	 * Returns the value of the property. The returned object is of type <code>getType()</code>
	 * @param configuration The configuration for which the value of the property should be returned.
	 * @return Value of property.
	 * @throws GenericException
	 */
	public Object getValue(Configuration configuration) throws GenericException
	{
		try 
		{
			return getter.invoke(configuration);
		} 
		catch (Exception e) 
		{
			throw new GenericException("Could not get value of property " + propertyID + " in class " + configuration.getClass().getName() + ".", e);
		}
	}
	
	/**
	 * Checks if conditional function for property to make sense to add to UI is fulfilled. If no conditional function is defined,
	 * returns null.
	 * @param configuration The configuration for which the value of the property should be returned.
	 * @return True if condition is fulfilled or not defined, false otherwise.
	 * @throws GenericException
	 */
	public boolean isConditionalFulfilled(Configuration configuration) throws GenericException
	{
		if(conditional == null)
			return true;
		Object value;
		try 
		{
			value = conditional.invoke(configuration);
		} 
		catch (Exception e) 
		{
			throw new GenericException("Could not get value of property " + propertyID + " in class " + configuration.getClass().getName() + ".", e);
		}
		if(!(value instanceof Boolean))
			throw new GenericException("Conditional function " + conditional.getName() + " does not return a boolean value, but a value of class " + value.getClass().getName() + ".");
		return (Boolean)value;
	}
	/**
	 * Returns the value of this property, cased to a specific class.
	 * @param configuration The configuration for which the value of the property should be returned.
	 * @param valueType class of property.
	 * @return Value of property.
	 * @throws GenericException
	 */
	public <T> T getValue(Configuration configuration, Class<T> valueType) throws GenericException
	{
		Object rawValue = getValue(configuration);
		if(rawValue == null)
			return null;
		if(!valueType.isAssignableFrom(rawValue.getClass()))
		{
			throw new GenericException("Value of property " + propertyID + " in class " + configurationClass.getName() + " is of type " + rawValue.getClass().getName() + ", and cannot be casted to type " + valueType.getName() + ".");
		}
		try
		{
			return valueType.cast(rawValue);
		}
		catch(ClassCastException e)
		{
			throw new GenericException("Could not cast property " + propertyID + " in class " + configurationClass.getName() + " to type " + valueType.getName() + " (Type of value: " + rawValue.getClass().getName() + ").", e);
		}
	}
	/**
	 * Sets the value of the property.
	 * @param configuration Configuration in which the value should be set.
	 * @param value Value which should be set. Should have the correct type.
	 * @throws GenericException
	 */
	public void setValue(Configuration configuration, Object value) throws GenericException
	{
		try 
		{
			setter.invoke(configuration, value);
		} 
		catch (Exception e) 
		{
			if(!type.isAssignableFrom(value.getClass()))
			{
				throw new GenericException("Value of property " + propertyID + " in class " + configurationClass.getName() + " is of type " + type.getName() + ", and cannot be assigned to a value of type " + value.getClass().getName() + ".", e);
			}
			throw new GenericException("Could not set value of property " + propertyID + " in class " + configurationClass.getName() + " to " + value.toString() + " ( value type " + value.getClass().getName() + ").", e);
		}
	}
}
