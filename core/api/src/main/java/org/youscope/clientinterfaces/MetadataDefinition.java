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
package org.youscope.clientinterfaces;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamInclude;

/**
 * An immutable class representing a metadata property, it's known possible values, and properties like if it is mandatory
 * @author Moritz Lang
 *
 */
@XStreamAlias("metadata-definition")
@XStreamInclude(MetadataDefinition.Type.class)
public class MetadataDefinition implements Serializable, Iterable<String>, Comparable<MetadataDefinition>
{

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 4671146611312781626L;

	/**
	 * The type of a property, i.e. if it must be part of all measurement metadata, or if it is optional.
	 * @author mlang
	 *
	 */
	@XStreamAlias("Type")
	public enum Type
	{
		/**
		 * Indicates that this property is mandatory, that is, must be included in all measurements.
		 */
		@XStreamAlias("mandatory")
		MANDATORY,
		/**
		 * Indicates that this property is not mandatory, but should be by default included in all measurements.
		 */
		@XStreamAlias("default")
		DEFAULT,
		/**
		 * Indicates that this property is neither mandatory nor by default included in the measurement.
		 */
		@XStreamAlias("optional")
		OPTIONAL;
		
		@Override
		public String toString()
		{
			return this.name().toLowerCase();
		}
	}
	
	@XStreamImplicit(itemFieldName="value")
	private final String[] knownValues;
	@XStreamAlias("custom-values")
	@XStreamAsAttribute
	private final boolean customValuesAllowed;
	@XStreamAlias("type")
	private final Type type;
	@XStreamAlias("name")
	@XStreamAsAttribute
	private final String name;
	/**
	 * Constructor.
	 * @param name The name of this property.
	 * @param customValuesAllowed true if user can define additional values for this property not in the list of knownValues.
	 * @param type The type of this property, i.e. if it is mandatory to be part of all measurement metadata or not.
	 * @param knownValues Array of known, non-null values the user can choose from, or null if no known values exist. Must be non-empty if customValuesAllowed is false.
	 * @throws IllegalArgumentException Thrown if knownValues contain null values, or if customValuesAllowed is false and no known value is provided.
	 */
	public MetadataDefinition(String name, Type type, boolean customValuesAllowed, String... knownValues) throws IllegalArgumentException
	{
		if(knownValues != null)
		{
			for(String knownValue : knownValues)
			{
				if(knownValue == null)
					throw new IllegalArgumentException("knownValues must not contain null values");
			}
		}
		if(!customValuesAllowed && (knownValues == null || knownValues.length < 1))
			throw new IllegalArgumentException("If property does not allow for user values, at least one possible value to choose from must be provided.");
		if(name == null || name.length() < 1)
			throw new IllegalArgumentException("Name must be non-null and at least one character long.");
		this.knownValues = knownValues == null ? new String[0] : knownValues;
		this.customValuesAllowed = customValuesAllowed;
		this.type = type;
		this.name = name;
		
	}
	
	/**
	 * Convenience constructor to generate a property with an array of known values between minVal and maxVal. The values generated correspond to the values returned by {@link #generateValueRange(int, int, String, String)}.
	 * @param name The name of this property.
	 * @param customValuesAllowed true if user can define additional values for this property not in the list of knownValues.
	 * @param type The type of this property, i.e. if it is mandatory to be part of all measurement metadata or not.
	 * @param minVal minimal value of the array.
	 * @param maxVal maximal value of the array.
	 * @param prefix String to prepend to value. Set to null to not prepend any String.
	 * @param postfix String to append to value. Set to null to not append any String.
	 * @throws IllegalArgumentException Thrown if minVal >= maxVal.
	 */
	public MetadataDefinition(String name, Type type, boolean customValuesAllowed, int minVal, int maxVal, String prefix, String postfix) throws IllegalArgumentException
	{
		
		this(name, type, customValuesAllowed, generateValueRange(minVal, maxVal, prefix, postfix));
	}
	/**
	 * Generates maxVal+minVal+1 Strings, where the first one is {@code prefix+Integer.toString(minVal)+postfix}, the second is {@code prefix+Integer.toString(minVal+1)+postfix}, and so on.
	 * @param minVal minimal value of the array.
	 * @param maxVal maximal value of the array.
	 * @param prefix String to prepend to value. Set to null to not prepend any String.
	 * @param postfix String to append to value. Set to null to not append any String.
	 * @return Array of strings representing the values between minVal and maxVal.
	 * @throws IllegalArgumentException Thrown if minVal >= maxVal.
	 */
	public static String[] generateValueRange(int minVal, int maxVal, String prefix, String postfix) throws IllegalArgumentException
	{
		if(maxVal <= minVal)
			throw new IllegalArgumentException("Minimal value must be smaller than maximal value.");
		if(prefix == null)
			prefix = "";
		if(postfix == null)
			postfix = "";
		String[] values = new String[maxVal-minVal+1];
		for(int i=0; i<values.length; i++)
		{
			values[i] = prefix+Integer.toString(i+minVal)+postfix;
		}
		return values;
	}
	/**
	 * Returns the name of this property.
	 * @return Name of property.
	 */
	public String getName()
	{
		return name;
	}
	
	@Override
	public String toString()
	{
		return name;
	}
	/**
	 * Returns true if user can enter custom values for this property besides choosing one of the known values.
	 * @return If user can enter custom values.
	 */
	public boolean isCustomValuesAllowed()
	{
		return customValuesAllowed;
	}
	/**
	 * Returns the type of this property, i.e. if it is mandatory to include this property in all measurement metadata, or not.
	 * @return Type of this measurement.
	 */
	public Type getType()
	{
		return type;
	}
	/**
	 * Returns an array of all known values for this property the user can choose from.
	 * @return Known values for this property.
	 */
	public String[] getKnownValues()
	{
		if(knownValues == null)
			return new String[0];
		String[] result = new String[knownValues.length];
		System.arraycopy(knownValues, 0, result, 0, knownValues.length);
		return result;
	}
	/**
	 * Iterator over all known values.
	 */
	@Override
	public Iterator<String> iterator() {
		return new Iterator<String>()
		{
			private int currentIndex = 0;
			@Override
			public boolean hasNext() {
				return currentIndex < knownValues.length;
			}

			@Override
			public String next() 
			{
				if(hasNext())
					throw new NoSuchElementException();
				return knownValues[currentIndex++];
			}

			@Override
			public void remove() 
			{
				throw new UnsupportedOperationException();
			}
	
		};
	}
	@Override
	public int compareTo(MetadataDefinition other) {
		if(type == Type.MANDATORY && other.type != Type.MANDATORY)
			return -1;
		else if(type != Type.MANDATORY && other.type == Type.MANDATORY)
			return 1;
		return name.compareTo(other.name);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (customValuesAllowed ? 1231 : 1237);
		result = prime * result + Arrays.hashCode(knownValues);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MetadataDefinition other = (MetadataDefinition) obj;
		if (customValuesAllowed != other.customValuesAllowed)
			return false;
		if (!Arrays.equals(knownValues, other.knownValues))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (type != other.type)
			return false;
		return true;
	}
}
