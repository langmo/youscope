/**
 * 
 */
package org.youscope.plugin.youpong;

import java.awt.Dimension;

import org.youscope.addon.tool.ToolAddonUI;
import org.youscope.addon.tool.ToolMetadata;
import org.youscope.addon.tool.ToolMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.clientinterfaces.YouScopeFrameListener;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * @author langmo
 *
 */
class YouPong implements ToolAddonUI, YouScopeFrameListener
{
	private YouScopeServer server;
	private YouScopeClient client;
	private YouScopeFrame						frame;
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
	 */
	public YouPong(YouScopeClient client, YouScopeServer server)
	{
		this.server = server;
		this.client = client;
	}
	@Override
	public void createUI(YouScopeFrame frame)
	{
		this.frame = frame;
		frame.setClosable(true);
		frame.setMaximizable(true);
		frame.setResizable(true);
		frame.setTitle("YouPong");
		
		frame.startInitializing();
		(new Thread(new FrameInitializer())).start();
	}
	private class FrameInitializer implements Runnable
	{
		@Override
		public void run()
		{
			field = new YouPongField(client, server);
			field.createUI();
			   
	        // End initializing
			frame.setContentPane(field);
			frame.setSize(new Dimension(800, 600));
			frame.endLoading();
			
			// Querying of microscope for current position
	        frame.addFrameListener(YouPong.this);

	        if(frame.isVisible())
	        	field.start();
		}
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
