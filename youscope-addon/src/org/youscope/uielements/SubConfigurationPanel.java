package org.youscope.uielements;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.youscope.addon.component.ComponentAddonUI;
import org.youscope.addon.component.ComponentMetadata;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.configuration.Configuration;

/**
 * A configuration panel allowing to choose between all configurations of a given type, and then to configure this configuration.
 * @author Moritz Lang
 *
 * @param <C> Type of configurations which can be chosen.
 */
public class SubConfigurationPanel <C extends Configuration> extends DynamicPanel
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 5007588084125311141L;
	private Component addonConfigurationPanel = null;
	private ComponentAddonUI<? extends C> currentAddon = null;
	private final Class<C> configurationClass;
	private final YouScopeClient client;
	private final YouScopeFrame frame;
	private C lastConfiguration;
	private final ArrayList<ActionListener> actionListeners = new ArrayList<ActionListener>();
	private final ComponentComboBox<C> componentBox;
	/**
	 * Constructor.
	 * @param choiceString If there is one then more choice of configurations the user can select, this string is shown directly above the configuration type chooser. Can be null.
	 * @param lastConfiguration Last configuration which settings should be loaded. Can be null.
	 * @param configurationClass The base class of all configurations which should be choosable.
	 * @param client YouScope client.
	 * @param frame Containing frame.
	 * @param requiredInterface Additional interfaces which are required, e.g. TableProducerConfiguration.class.
	 */
	public SubConfigurationPanel(String choiceString, C lastConfiguration, Class<C> configurationClass, YouScopeClient client, YouScopeFrame frame, Class<?>... requiredInterface)
	{
		this.client = client;
		this.frame = frame;
		this.lastConfiguration = lastConfiguration;
		this.configurationClass = configurationClass;
		componentBox = new ComponentComboBox<C>(client, configurationClass, requiredInterface);
		if(lastConfiguration != null)
			componentBox.setSelectedElement(lastConfiguration.getTypeIdentifier());
		if(componentBox.getNumChoices()>1)
		{
			if(choiceString != null)
				add(new JLabel(choiceString));
			add(componentBox);
		}
		componentBox.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) {
				ComponentMetadata<?> metadata = componentBox.getSelectedElement(); 
				displayAddonConfiguration(metadata==null ? null : metadata.getTypeIdentifier());
			}
		});
		ComponentMetadata<?> metadata = componentBox.getSelectedElement(); 
		addFillEmpty();
		displayAddonConfiguration(metadata==null ? null : metadata.getTypeIdentifier());
	}
	
	private Component createErrorUI(String message, Exception exception)
	{
		if(exception != null)
			message += "\n\n"+exception.getMessage();
		JTextArea textArea = new JTextArea(message);
		textArea.setEditable(false);
		JPanel errorPane = new JPanel(new BorderLayout());
		errorPane.add(new JLabel("An error occured:"), BorderLayout.NORTH);
		errorPane.add(new JScrollPane(textArea), BorderLayout.CENTER);
		return errorPane;
	}
	/**
	 * Returns the current configuration, or null if no configuration option is present.
	 * @return Current configuration.
	 */
	public C getConfiguration()
	{	    	
		return currentAddon == null ? null : currentAddon.getConfiguration();
	}
	
	/**
	 * Sets the current configuration. That means, it tries to change the selected addon UI such that the configuration types match, and
	 * then initializes the addon UI with the given configuration.
	 * @param configuration Configuration to set.
	 */
	public void setConfiguration(C configuration)
	{
		if(configuration == null)
			return;
		lastConfiguration = configuration;
		displayAddonConfiguration(configuration.getTypeIdentifier());
	}
	
	/**
	 * Adds an action listener which gets notified if the chosen configuration type changes.
	 * @param listener Listener to add.
	 */
	public void addActionListener(ActionListener listener)
	{
		actionListeners.add(listener);
	}
	/**
	 * Removes a previously added listener.
	 * @param listener Listener to remove.
	 */
	public void removeActionListener(ActionListener listener)
	{
		actionListeners.remove(listener);
	}
	private void displayAddonConfiguration(String typeIdentifier)
	{
		if(currentAddon != null)
		{
			ComponentMetadata<? extends C> metadata = currentAddon.getAddonMetadata();
			if(metadata!= null && typeIdentifier!= null && metadata.getTypeIdentifier().equals(typeIdentifier))
				return;
			currentAddon = null;
		}
		if(addonConfigurationPanel != null)
			remove(addonConfigurationPanel);
		int pos = getComponentCount()-1;
		if(typeIdentifier == null)
		{
			addonConfigurationPanel = createErrorUI("No choices available.", null);
			addFill(addonConfigurationPanel, pos);
			revalidate();
			if(frame.isVisible())
				frame.pack();
			fireAddonChanged();
			return;
		}
		try 
		{
			currentAddon = client.getAddonProvider().createComponentUI(typeIdentifier, configurationClass);
			
			if(lastConfiguration != null && lastConfiguration.getTypeIdentifier().equals(typeIdentifier))
			{
				currentAddon.setConfiguration(lastConfiguration);
			}
			addonConfigurationPanel = currentAddon.toPanel(frame);
		} 
		catch (Exception e) 
		{
			addonConfigurationPanel = createErrorUI("Error loading addon configuration interface.", e);
			addFill(addonConfigurationPanel, pos);
			client.sendError("Error loading addon configuration interface.",  e);
			revalidate();
			if(frame.isVisible())
				frame.pack();
			fireAddonChanged();
			return;
		}
		
		add(addonConfigurationPanel, pos);
		revalidate();
		if(frame.isVisible())
			frame.pack();
		fireAddonChanged();
	}
	
	private void fireAddonChanged()
	{
		for(ActionListener listener : actionListeners)
		{
			listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_FIRST, "addon changed"));
		}
	}
}
