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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JLabel;

import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.MessageListener;
import org.youscope.common.configuration.Configuration;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DynamicPanel;

/**
 * Adapter class to simplify property editor construction.
 * @author Moritz Lang
 *
 */
public abstract class PropertyEditorAdapter extends DynamicPanel implements PropertyEditor {

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -8067980706767366317L;
	private final Property property;
	private final Configuration configuration;
	private final ArrayList<MessageListener> messageListeners = new ArrayList<MessageListener>();
	private final ArrayList<ActionListener> actionListeners = new ArrayList<ActionListener>();
	private final YouScopeClient client;
	private final YouScopeServer server;
	private final Class<?>[] supportedTypes;
	/**
	 * Constructor.
	 * @param property The property which gets edited by this editor.
	 * @param configuration The configuration where the respective property changes.
	 * @param client Interface to the client.
	 * @param server Interface to the server.
	 * @param supportedType property types supported by this editor.
	 */
	public PropertyEditorAdapter(Property property, Configuration configuration, YouScopeClient client, YouScopeServer server, Class<?>... supportedType) {
		this.property = property;
		this.configuration = configuration;
		this.client = client;
		this.server = server;
		this.supportedTypes = supportedType;
	}
	
	protected void addLabel()
	{
		JLabel label = new JLabel(property.getName() + ":");
		label.setOpaque(false);
		add(label);
	}
	protected Property getProperty() {
		return property;
	}

	@Override
	public Class<?>[] getSupportedTypes()
	{
		return supportedTypes;
	}
	
	protected <T> T getValue(Class<T> valueType) throws GenericException
	{
		return property.getValue(configuration, valueType);
	}
	protected Object getValue() throws GenericException
	{
		return property.getValue(configuration);
	}
	protected void setValue(Object value) throws GenericException
	{
		property.setValue(configuration, value);
	}
	
	protected Configuration getConfiguration() {
		return configuration;
	}

	protected YouScopeClient getClient() {
		return client;
	}

	protected YouScopeServer getServer() {
		return server;
	}

	@Override
	public Component getEditor() 
	{
		return this;
	}

	@Override
	public void addMessageListener(MessageListener listener) {
		synchronized(messageListeners)
		{
			messageListeners.add(listener);
		}
		
	}

	@Override
	public void removeMessageListener(MessageListener listener) {
		synchronized(messageListeners)
		{
			messageListeners.remove(listener);
		}
	}

	@Override
	public void addActionListener(ActionListener listener) {
		synchronized(actionListeners)
		{
			actionListeners.add(listener);
		}
		
	}

	@Override
	public void removeActionListener(ActionListener listener) {
		synchronized(actionListeners)
		{
			actionListeners.remove(listener);
		}
	}

	@Override
	public boolean isFillSpace() 
	{
		return false;
	}

	/**
	 * Call this function to notify all action listeners that the property's value has changed.
	 * Note: update the properties value in the configuration first.
	 */
	protected void notifyPropertyValueChanged()
	{
		synchronized(actionListeners)
		{
			for(ActionListener listener : actionListeners)
			{
				listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Property value changed"));
			}
		}
	}
	
	/**
	 * Sends a message to all message listeners.
	 * @param message The message to send.
	 */
	protected void sendMessage(String message)
	{
		synchronized(messageListeners)
		{	
			for(Iterator<MessageListener> iterator = messageListeners.iterator(); iterator.hasNext();)
			{
				MessageListener listener = iterator.next();
				try {
					listener.sendMessage(message);
				} catch (@SuppressWarnings("unused") RemoteException e) {
					iterator.remove();
				}
			}
		}
	}
	
	/**
	 * Sends an error message to all message listener.
	 * Should only be called when the error is produced autonomously, i.e. not as a direct response to a function call. If a function is called and an
	 * error occurs in the function call, an error should be thrown instead of an error message be send. A typical example when an error message would be send
	 * is when a thread is started as a response to a function call, and an error occurs in the thread while the original function call already succeeded.
	 * Another example are errors occurring as response to the user interacting with UI components.
	 * @param message The message to send.
	 * @param error The error which occurred.
	 */
	protected void sendErrorMessage(String message, Throwable error)
	{
		synchronized(messageListeners)
		{	
			for(Iterator<MessageListener> iterator = messageListeners.iterator(); iterator.hasNext();)
			{
				MessageListener listener = iterator.next();
				try {
					listener.sendErrorMessage(message, error);
				} catch (@SuppressWarnings("unused") RemoteException e) {
					iterator.remove();
				}
			}
		}
	}
}
