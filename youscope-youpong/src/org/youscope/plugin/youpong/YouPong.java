/**
 * 
 */
package org.youscope.plugin.youpong;

import java.awt.Dimension;

import org.youscope.addon.AddonException;
import org.youscope.addon.tool.ToolAddonUIAdapter;
import org.youscope.addon.tool.ToolMetadata;
import org.youscope.addon.tool.ToolMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrameListener;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * @author langmo
 *
 */
class YouPong extends ToolAddonUIAdapter implements YouScopeFrameListener
{
	private YouPongField field;
	
	public final static String TYPE_IDENTIFIER = "CSB::YouPong";
	
	static ToolMetadata getMetadata()
	{
		return new ToolMetadataAdapter(TYPE_IDENTIFIER, "YouPong", new String[]{"misc"}, "icons/game.png");
	}
	
	/**
	 * Constructor.
	 * @param client Interface to the YouScope client.
	 * @param server Interface to the YouScope server.
	 * @throws AddonException 
	 */
	public YouPong(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(), client, server);
	}
	@Override
	public java.awt.Component createUI()
	{
		setMaximizable(true);
		setResizable(true);
		setTitle("YouPong");
		setPreferredSize(new Dimension(800, 600));
		
		field = new YouPongField(getClient(), getServer());
		field.createUI();
		   
        // End initializing
		getContainingFrame().addFrameListener(YouPong.this);

        if(getContainingFrame().isVisible())
        	field.start();
        return field;
	}
	
	@Override
	public void frameClosed()
	{
		field.stop();
	}

	@Override
	public void frameOpened()
	{
		// do nothing.
	}
}
