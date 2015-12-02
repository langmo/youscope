package ch.ethz.csb.youscope.addon.adapters.generic;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.uielements.ChannelField;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.configuration.ChannelConfiguration;
import ch.ethz.csb.youscope.shared.configuration.Configuration;

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
