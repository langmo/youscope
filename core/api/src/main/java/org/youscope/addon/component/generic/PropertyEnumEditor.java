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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.configuration.Configuration;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * An editor for enum properties of a configuration.
 * @author Moritz Lang
 *
 */
public class PropertyEnumEditor extends PropertyEditorAdapter
{

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -8734624486559686392L;

	private final JComboBox<Object> field = new JComboBox<Object>();
	/**
	 * Constructor.
	 * @param property The property which should be edited.
	 * @param configuration The configuration which should be edited.
	 * @param client YouScope client
	 * @param server YouScope server.
	 * @throws GenericException
	 */
	public PropertyEnumEditor(Property property, Configuration configuration, YouScopeClient client, YouScopeServer server) throws GenericException
	{
		super(property, configuration, client, server, Enum.class);
		for(Object constant : property.getType().getEnumConstants())
		{
			field.addItem(constant);
		}
		field.setSelectedItem(getValue());
		addLabel();
		add(field);
		field.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				try 
				{
					commitEdits();
					notifyPropertyValueChanged();
				} 
				catch (GenericException e1) 
				{
					sendErrorMessage("Could not set value of property " + getProperty().getName() + ".", e1);
				}
			}
		});
	}
	
	@Override
	public void commitEdits() throws GenericException 
	{
		setValue(field.getSelectedItem());
	}

}
