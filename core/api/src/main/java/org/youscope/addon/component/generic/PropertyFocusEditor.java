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

import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.configuration.Configuration;
import org.youscope.common.configuration.FocusConfiguration;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.FocusField;

/**
 * An editor for Focus properties of a configuration.
 * @author Moritz Lang
 *
 */
public class PropertyFocusEditor extends PropertyEditorAdapter
{

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -6011301212178813550L;
	private final FocusField field;
	/**
	 * Constructor.
	 * @param property The property which should be edited.
	 * @param configuration The configuration which should be edited.
	 * @param client YouScope client
	 * @param server YouScope server.
	 * @throws GenericException
	 */
	public PropertyFocusEditor(Property property, Configuration configuration, YouScopeClient client, YouScopeServer server) throws GenericException
	{
		super(property, configuration, client, server, FocusConfiguration.class);
		field = new FocusField(getValue(FocusConfiguration.class), client, server);
		addLabel();
		add(field);
	}
	
	@Override
	public void commitEdits() throws GenericException 
	{
		setValue(field.getFocusConfiguration());
	}
}
