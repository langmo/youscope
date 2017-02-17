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
package org.youscope.addon.component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.generic.GenericException;
import org.youscope.addon.component.generic.Property;
import org.youscope.addon.component.generic.PropertyEditor;
import org.youscope.addon.component.generic.PropertyEditorManager;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.Component;
import org.youscope.common.MessageListener;
import org.youscope.common.configuration.Configuration;
import org.youscope.common.configuration.YSConfigAlias;
import org.youscope.common.configuration.YSConfigDescription;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DynamicPanel;

/**
 * User interface to configure a measurement component which is automatically created from the component's configuration class.
 * @author Moritz Lang
 *
 * @param <C>
 */
public class GenericComponentAddonUI<C extends Configuration> extends ComponentAddonUIAdapter<C>
{
	private ArrayList<InternalEditor> propertyEditors = new ArrayList<InternalEditor>();
	/**
	 * Constructor.
	 * @param client Interface to the client.
	 * @param server Interface to the server.
	 * @param configurationClass the class of the configurations which should be configured.
	 * @param typeIdentifier The identifier of the configurations which should be configured. Must be the same as returned by <code>configurationClass.getTypeIdentifier()</code>. 
	 * @param componentInterface Interface of the component. Interface should be public.
	 * @throws AddonException 
	 */
    public GenericComponentAddonUI(String typeIdentifier, Class<C> configurationClass, Class<? extends Component> componentInterface, YouScopeClient client, YouScopeServer server) throws AddonException
    {
    	super(new GenericComponentMetadata<C>(typeIdentifier, configurationClass, componentInterface), client, server); 
    }
    
    private class InternalEditor
    {
    	private final Property property;
    	private final Configuration configuration;
    	private final PropertyEditor editor;
    	private volatile boolean visible = true;
    	InternalEditor(Property property, Configuration configuration, PropertyEditor editor)
    	{
    		this.property = property;
    		this.configuration = configuration;
    		this.editor = editor;
    	}
    	
    	void checkConditional() throws GenericException
    	{
    		boolean fulfilled = property.isConditionalFulfilled(configuration);
    		if(fulfilled == visible)
    			return;
    		visible = fulfilled;
    		Runnable runner = new Runnable()
			{
				@Override
				public void run() 
				{
					editor.getEditor().setVisible(visible);
					getContainingFrame().pack();
				}
			};
			if(SwingUtilities.isEventDispatchThread())
				runner.run();
			else
				SwingUtilities.invokeLater(runner);
    	}
    }
    
    @Override
	protected java.awt.Component createUI(C configuration) throws AddonException 
    {	
    	try
    	{
    		Class<C> configurationClass = getConfigurationClass();
    		Field[] fields = configurationClass.getDeclaredFields();
	    	
    		YSConfigDescription descriptionAnnotation = configurationClass.getAnnotation(YSConfigDescription.class);
    		if(descriptionAnnotation != null)
    			setDescription(descriptionAnnotation.value());
    		YSConfigAlias aliasAnnotation = configurationClass.getAnnotation(YSConfigAlias.class);
    		if(aliasAnnotation != null)
    			setTitle(aliasAnnotation.value());
    		else
    			setTitle("Configuration");
    		
    		ArrayList<Property> properties = new ArrayList<Property>();
	    	for(Field field:fields)
	    	{
	    		if(!checkModifiers(field))
	    			continue;
	    		Property property =new Property(configurationClass, field.getName());
	    		if(!property.isVisible())
	    			continue;
	    		properties.add(property);
	    	}
	    	
	    	DynamicPanel contentPanel = new DynamicPanel();
	    	PropertyEditorManager editorManager = new PropertyEditorManager();
	    	MessageListener messageListener = new MessageListener()
			{
				@Override
				public void sendMessage(String message) 
				{
					GenericComponentAddonUI.this.sendMessage(message);
				}

				@Override
				public void sendErrorMessage(String message, Throwable error) 
				{
					GenericComponentAddonUI.this.sendErrorMessage(message, error);
				}
		
			};
			ActionListener actionListener = new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					try {
						checkConditionals();
					} catch (GenericException e1) {
						getClient().sendError("Error occured while updating visibility of UI elements.", e1);
					}
				}
		
			};
			boolean anyFillSpace = false;
	    	for(Property property : properties)
	    	{	
	    		PropertyEditor editor = editorManager.getEditor(property, configuration, getClient(), getServer());
	    		editor.addMessageListener(messageListener);
	    		editor.addActionListener(actionListener);
	    		propertyEditors.add(new InternalEditor(property, configuration, editor));
	    		boolean fillSpace = editor.isFillSpace();
	    		anyFillSpace = anyFillSpace || fillSpace;
	    		java.awt.Component editorComponent = editor.getEditor();
	    		if(fillSpace)
	    			contentPanel.addFill(editorComponent);
	    		else
	    			contentPanel.add(editorComponent);
	    	}
	    	if(!anyFillSpace)
	    		contentPanel.addFillEmpty();
	    	checkConditionals();
	    	this.setResizable(anyFillSpace);
	    	return contentPanel;
    	}
    	catch(GenericException e)
    	{
    		throw new AddonException("Could not create generic configuration UI.", e);
    	}
	}
        
    private static boolean checkModifiers(Field field)
    {
    	int modifiers = field.getModifiers();
    	if(Modifier.isFinal(modifiers) || Modifier.isStatic(modifiers))
    		return false;
		return true;
    }

    private void checkConditionals() throws GenericException
    {
    	for(InternalEditor propertyEditor : propertyEditors)
		{
    		propertyEditor.checkConditional();
		}
    }
    
	@Override
	protected void commitChanges(C configuration)
	{
		for(InternalEditor propertyEditor : propertyEditors)
		{
			try 
			{
				propertyEditor.editor.commitEdits();
			} 
			catch (@SuppressWarnings("unused") GenericException e) 
			{
				// do nothing.
			}
		}
	}

	@Override
	protected void initializeDefaultConfiguration(C configuration) throws AddonException {
		// do nothing.
		
	}
}
