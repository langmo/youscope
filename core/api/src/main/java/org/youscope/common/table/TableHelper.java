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
package org.youscope.common.table;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

/**
 * Static class providing some useful helper functions to create, consume or fill tables.
 * @author Moritz Lang
 *
 */
public class TableHelper 
{
	private class SupportedValueType<T extends Serializable>
	{
		final Class<T> valueType;
		final Class<? extends TableEntryAdapter<T>> entryType;
		SupportedValueType(Class<T> valueType, Class<? extends TableEntryAdapter<T>> entryType)
		{
			this.valueType = valueType;
			this.entryType = entryType;
		}
		
		public TableEntryAdapter<T> createEntry(Serializable value, boolean nullAllowed) throws TableException
		{
			if(value != null && !valueType.isInstance(value))
				throw new TableException("Value is of type " + value.getClass().getName()+", expecting class " +valueType.getName()+".");
			@SuppressWarnings("unchecked")
			T valueCasted = (T)value;
			try 
			{
				Constructor<? extends TableEntryAdapter<T>> constructor = entryType.getDeclaredConstructor(valueType, boolean.class);
				constructor.setAccessible(true);
				return constructor.newInstance(valueCasted, nullAllowed);
			} 
			catch (InvocationTargetException e) 
			{
				if(e.getCause() instanceof TableException)
					throw (TableException)e.getCause();
				throw new TableException("Could not create table entry for value of type " + valueType.getName()+".", e);
			}
			catch (Exception e) 
			{
				throw new TableException("Could not create table entry for value of type " + valueType.getName()+".", e);
			} 
		}
	}
	
	private final ArrayList<SupportedValueType<? extends Serializable>> supportedValueTypes = new ArrayList<SupportedValueType<? extends Serializable>>(3);
	
	private static TableHelper singleton = null;
	
	/**
	 * Private constructor. Use static methods instead.
	 */
	private TableHelper()
	{
		supportedValueTypes.add(new SupportedValueType<Integer>(Integer.class, TableIntegerEntry.class));
		supportedValueTypes.add(new SupportedValueType<Double>(Double.class, TableDoubleEntry.class));
		supportedValueTypes.add(new SupportedValueType<String>(String.class, TableStringEntry.class));
		supportedValueTypes.add(new SupportedValueType<Boolean>(Boolean.class, TableBooleanEntry.class));
		supportedValueTypes.add(new SupportedValueType<Long>(Long.class, TableLongEntry.class));
		supportedValueTypes.add(new SupportedValueType<Float>(Float.class, TableFloatEntry.class));
	}
	
	/**
	 * Returns the singleton instance of this class.
	 * @return Singleton instance.
	 */
	private synchronized static TableHelper getInstance()
	{
		if(singleton != null)
			return singleton;
		singleton = new TableHelper();
		return singleton;
	}
	
	/**
	 * Creates a table entry containing the given value.
	 * @param value The value for which an entry containing the value should be constructed.
	 * @param valueType The precise value type of the entry, that is, one of the value types returned by {@link #getSupportedValueTypes()}.
	 * @param nullAllowed indicating if assigning null to the returned table entry is allowed or not.
	 * @return The constructed entry.
	 * @throws TableException Thrown if entry could not be constructed, e.g. if value type was invalid.
	 * @throws NullPointerException Thrown if valueType is null.
	 */
	public static <T extends Serializable> TableEntryAdapter<T> createEntry(Class<T> valueType, T value, boolean nullAllowed) throws TableException, NullPointerException
	{
		if(valueType == null)
			throw new NullPointerException();
		assertSupported(valueType);
		for(SupportedValueType<?> supported : getInstance().supportedValueTypes)
		{
			if(supported.valueType.equals(valueType))
			{
				@SuppressWarnings("unchecked")
				TableEntryAdapter<T> returnVal = (TableEntryAdapter<T>) supported.createEntry(value, nullAllowed);
				return returnVal;
			}
		}
		// Should not happen, since we checked before if we support that type.
		throw new TableException("Value of type " + valueType.getName() + " currently not supported by YouScope.");
	}	
	
	/**
	 * Returns all currently supported value types of YouScope.
	 * Currently (2015-11-20), YouScope only supports a limited set of value types for tables. 
	 * These are String, Integer, Double, Float, Boolean and Long. Note that {@link Integer#getClass()} is different from <code>int.class()</code>,
	 * and that the former is supported only.
	 * @return Currently supported value types of YouScope.
	 */
	public static Iterable<Class<? extends Serializable>> getSupportedValueTypes()
	{
		ArrayList<Class<? extends Serializable>> result = new ArrayList<Class<? extends Serializable>>();
		for(SupportedValueType<?> supported : getInstance().supportedValueTypes)
		{
			result.add(supported.valueType);
		}
		return result;
	}
	
	/**
	 * Checks if the given value type is supported by YouScope.
	 * Currently, YouScope only supports a limited set of value types for tables.
	 * @param valueType The value type to check for support.
	 * @return True if supported, false otherwise.
	 */
	public static boolean isSupportedValueType(Class<? extends Serializable> valueType)
	{
		for(SupportedValueType<?> supported : getInstance().supportedValueTypes)
		{
			if(supported.valueType.isAssignableFrom(valueType))
				return true;
		}
		return false;
	}
	
	/**
	 * Helper function throwing a {@link TableException} if given value type is not supported by YouScope, and doing nothing otherwise.
	 * See {@link #isSupportedValueType(Class)}.
	 * Currently, YouScope only supports a limited set of value types for tables. See {@link #getSupportedValueTypes()} for the list of supported value types.
	 * @param valueType Value type to check.
	 * @throws TableException Thrown if value type is not supported.
	 */
	public static void assertSupported(Class<? extends Serializable> valueType) throws TableException
	{
		if(!isSupportedValueType(valueType))
		{
			String message = "Currently, YouScope only supports a limited set of value types for tables. The provided value type is " + valueType.getName() + ", which is not supported. Supported value types are: ";
			boolean first = true;
			for(Class<? extends Serializable> supported : getSupportedValueTypes())
			{
				if(first)
					first = false;
				else
					message+=", ";
				message+=supported.getName();
			}
			message+=".";
			throw new TableException(message);
		}
	}
}
