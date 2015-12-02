package ch.ethz.csb.youscope.addon.adapters.generic;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.configuration.Configuration;

/**
 * An editor for Boolean properties of a configuration.
 * @author Moritz Lang
 *
 */
public class PropertyBooleanEditor extends PropertyEditorAdapter
{ 
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 2174121041495259420L;
	private final JCheckBox field;
	/**
	 * Constructor.
	 * @param property The property which should be edited.
	 * @param configuration The configuration which should be edited.
	 * @param client YouScope client
	 * @param server YouScope server.
	 * @throws GenericException
	 */
	public PropertyBooleanEditor(Property property, Configuration configuration, YouScopeClient client, YouScopeServer server) throws GenericException
	{
		super(property, configuration, client, server, boolean.class, Boolean.class);
		field = new JCheckBox(property.getName(), getValue(Boolean.class));
		field.setOpaque(false);
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
		add(field);
	}

	@Override
	public void commitEdits() throws GenericException 
	{
		setValue(field.isSelected());
	}
}
