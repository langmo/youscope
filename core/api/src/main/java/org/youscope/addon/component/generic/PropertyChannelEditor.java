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
import org.youscope.common.configuration.ChannelConfiguration;
import org.youscope.common.configuration.Configuration;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.ChannelField;

/**
 * An editor for Channel properties of a configuration.
 * @author Moritz Lang
 *
 */
public class PropertyChannelEditor extends PropertyEditorAdapter
{

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -1952418075761077283L;
	private final ChannelField field;
	/**
	 * Constructor.
	 * @param property The property which should be edited.
	 * @param configuration The configuration which should be edited.
	 * @param client YouScope client
	 * @param server YouScope server.
	 * @throws GenericException
	 */
	public PropertyChannelEditor(Property property, Configuration configuration, YouScopeClient client, YouScopeServer server) throws GenericException
	{
		super(property, configuration, client, server, ChannelConfiguration.class);
		field = new ChannelField(getValue(ChannelConfiguration.class), client, server);
		addLabel();
		add(field);
	}
	
	@Override
	public void commitEdits() throws GenericException 
	{
		setValue(field.getChannelConfiguration());
	}

}
