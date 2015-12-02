package ch.ethz.csb.youscope.addon.adapters.generic;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.uielements.CameraField;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.configuration.CameraConfiguration;
import ch.ethz.csb.youscope.shared.configuration.Configuration;

/**
 * An editor for Camera properties of a configuration.
 * @author Moritz Lang
 *
 */
public class PropertyCameraEditor extends PropertyEditorAdapter
{

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -6025301212178813550L;
	private final CameraField field;
	/**
	 * Constructor.
	 * @param property The property which should be edited.
	 * @param configuration The configuration which should be edited.
	 * @param client YouScope client
	 * @param server YouScope server.
	 * @throws GenericException
	 */
	public PropertyCameraEditor(Property property, Configuration configuration, YouScopeClient client, YouScopeServer server) throws GenericException
	{
		super(property, configuration, client, server, CameraConfiguration.class);
		field = new CameraField(getValue(CameraConfiguration.class), client, server);
		addLabel();
		add(field);
	}
	
	@Override
	public void commitEdits() throws GenericException 
	{
		setValue(field.getCameraConfiguration());
	}
}
