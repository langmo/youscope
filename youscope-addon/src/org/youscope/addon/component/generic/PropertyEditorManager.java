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

import java.lang.reflect.Constructor;
import java.util.HashMap;

import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.configuration.CameraConfiguration;
import org.youscope.common.configuration.ChannelConfiguration;
import org.youscope.common.configuration.Configuration;
import org.youscope.common.configuration.FocusConfiguration;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * Manager to create an editor for a given property (type) of a configuration.
 * @author Moritz Lang
 *
 */
public class PropertyEditorManager 
{
	private final HashMap<Class<?>, Class<? extends PropertyEditor>> editors = new HashMap<Class<?>, Class<? extends PropertyEditor>>(20);
	/**
	 * Constructor.
	 */
	public PropertyEditorManager()
	{
		editors.put(double.class, PropertyDoubleEditor.class);
		editors.put(Double.class, PropertyDoubleEditor.class);
		editors.put(String.class, PropertyStringEditor.class);
		editors.put(int.class, PropertyIntegerEditor.class);
		editors.put(Integer.class, PropertyIntegerEditor.class);
		editors.put(boolean.class, PropertyBooleanEditor.class);
		editors.put(Boolean.class, PropertyBooleanEditor.class);
		editors.put(ChannelConfiguration.class, PropertyChannelEditor.class);
		editors.put(FocusConfiguration.class, PropertyFocusEditor.class);
		editors.put(CameraConfiguration.class, PropertyCameraEditor.class);
		editors.put(Enum.class, PropertyEnumEditor.class);
	}
	/**
	 * Returns an editor for the given property, or throws an exception if no editor is available for the property type.
	 * @param property The property which should be edited.
	 * @param configuration The configuration which should be edited.
	 * @param client 
	 * @param server 
	 * @return An editor for the property.
	 * @throws GenericException
	 */
	public PropertyEditor getEditor(Property property, Configuration configuration, YouScopeClient client, YouScopeServer server) throws GenericException
	{
		Class<?> type = property.getType();
		Class<? extends PropertyEditor> editorClass = editors.get(type);
		if(editorClass == null)
		{
			// test if enumeration type
			if(type.isEnum())
			{
				editorClass = PropertyEnumEditor.class;
			}
			else
				throw new GenericException("No editor available for property " + property.getName() + " having type " + type.getName() + ".");
		}
		
		Constructor<? extends PropertyEditor> constructor;
		try {
			constructor = editorClass.getDeclaredConstructor(Property.class, Configuration.class, YouScopeClient.class, YouScopeServer.class);
		
			constructor.setAccessible(true);
			return constructor.newInstance(property, configuration, client, server);
		} 
		catch (Exception e) 
		{
			throw new GenericException("Error while creating editor for property " + property.getName() + " having type " + type.getName() + ".", e);
		}
	}
}
