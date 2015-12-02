/**
 * 
 */
package ch.ethz.csb.youscope.client;

import java.awt.Dimension;

import ch.ethz.csb.youscope.client.addon.YouScopeFrame;
import ch.ethz.csb.youscope.client.addon.YouScopeFrameListener;
import ch.ethz.csb.youscope.client.uielements.ContinousMeasurementAndControlsPanel;

/**
 * @author Moritz Lang
 */
class ContinousMeasurementFrame implements YouScopeFrameListener
{
	private ContinousMeasurementAndControlsPanel	mainPanel;

	ContinousMeasurementFrame(YouScopeFrame frame)
	{
		frame.setTitle("YouScope LiveStream");
		frame.setResizable(true);
		frame.setClosable(true);
		frame.setMaximizable(true);
		
		try
		{
			mainPanel = new ContinousMeasurementAndControlsPanel(new YouScopeClientConnectionImpl(), YouScopeClientImpl.getServer());
		}
		catch(Exception e)
		{
			mainPanel = null;
			frame.setToErrorState("Could not establish continuous measurement.", e);
			return;
		}
		
		frame.setContentPane(mainPanel);
		frame.addFrameListener(this);
		frame.setSize(new Dimension(800, 600));
		frame.setMaximum(true);
	}

	@Override
	public void frameClosed()
	{
		mainPanel.stopMeasurement();
	}

	@Override
	public void frameOpened()
	{
		mainPanel.startMeasurement();
	}
}
