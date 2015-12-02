/**
 * 
 */
package ch.ethz.csb.youscope.addon.youpong;

import java.awt.Dimension;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.YouScopeFrame;
import ch.ethz.csb.youscope.client.addon.YouScopeFrameListener;
import ch.ethz.csb.youscope.client.addon.tool.ToolAddon;
import ch.ethz.csb.youscope.shared.YouScopeServer;

/**
 * @author langmo
 *
 */
class YouPong implements ToolAddon, YouScopeFrameListener
{
	private YouScopeServer server;
	private YouScopeClient client;
	private YouScopeFrame						frame;
	private YouPongField field;
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
