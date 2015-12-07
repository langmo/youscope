package org.youscope.addon.tool;

import org.youscope.addon.AddonException;
import org.youscope.addon.AddonUIAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * An adapter class to simplify tool development.
 * @author Moritz Lang
 *
 */
public abstract class ToolAddonUIAdapter extends AddonUIAdapter<ToolMetadata> implements ToolAddonUI 
{
	/**
	 * Constructor.
	 * @param metadata The metadata of the tool.
	 * @param client The YouScope client.
	 * @param server The YouScope server.
	 * @throws AddonException
	 */
	public ToolAddonUIAdapter(final ToolMetadata metadata,  final YouScopeClient client, final YouScopeServer server) throws AddonException 
	{
		super(metadata, client, server);
		setCloseButtonLabel("Close");
		setShowCloseButton(false);
	}
}
