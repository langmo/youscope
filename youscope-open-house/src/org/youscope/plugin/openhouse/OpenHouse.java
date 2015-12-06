/**
 * 
 */
package org.youscope.plugin.openhouse;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.youscope.addon.tool.ToolAddonUI;
import org.youscope.addon.tool.ToolMetadata;
import org.youscope.addon.tool.ToolMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.microscope.Device;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DescriptionPanel;
import org.youscope.uielements.DynamicPanel;

/**
 * Controller tool for the Nemesys syringe control device.
 * @author Moritz Lang
 *
 */
class OpenHouse implements ToolAddonUI
{
	private final YouScopeServer server;
	private final YouScopeClient client;
	private Device nemesysDevice = null;
	private JSlider	stressSlider = new JSlider(0, 100, 0);
	private JToggleButton stressButton = new JToggleButton("<html><center><font size=\"30\" color=\"#008800\"><b>Zellen<br />am relaxen</b></font></center></html>");
	private final double TOTAL_FLOW = 10.0;
	private final double MAX_STRESS = 0.5;
	private final JLabel stressLabel = new JLabel("Stressintensität: 0%");
	
	public final static String TYPE_IDENTIFIER = "CSB::OpenHouse";
	
	static ToolMetadata getMetadata()
	{
		return new ToolMetadataAdapter(TYPE_IDENTIFIER, "Open House", new String[]{"misc"}, "icons/beaker.png");
	}
	
	private void setFlowRate(int dosingUnit, double flowRate)
	{
		Device nemesysDevice = this.nemesysDevice;
		if(nemesysDevice == null)
		{
			client.sendError("Cannot set flow rate since Nemesys device not set.");
			return;
		}
		try
		{
			nemesysDevice.getProperty("syringe" + Integer.toString(dosingUnit+1) + ".flowRate").setValue(Double.toString(flowRate));
		}
		catch(Exception e)
		{
			client.sendError("Could not set flow rate.", e);
		}
	}
	
	/**
	 * Constructor.
	 * @param client Interface to the YouScope client.
	 * @param server Interface to the YouScope server.
	 */
	public OpenHouse(YouScopeClient client, YouScopeServer server)
	{
		this.server = server;
		this.client = client;
	}
	
	@Override
	public void createUI(final YouScopeFrame frame)
	{
		frame.setClosable(true);
		frame.setMaximizable(false);
		frame.setResizable(true);
		frame.setTitle("Der Zellstressautomat");
		
		try
		{
			Device[] devices = server.getMicroscope().getDevices();
			for(Device device : devices)
			{
				if(device.getDriverID().equals("Nemesys") && device.getLibraryID().equals("Nemesys"))
				{
					nemesysDevice = device;
					break;
				}
			}
		}
		catch(Exception e)
		{
			client.sendError("Could not load Nemesys device IDs.", e);
		}
		
		DescriptionPanel descPanel = new DescriptionPanel("Gestresste Zellen", "Wählen Sie zuerst, wie stark Sie die Zellen stressen wollen, und drücken Sie dann den Knopf, damit die Zellen leiden!\nSeien Sie rücksichtsvoll: Genauso wie Menschen tut Zellen ein bischen Stress für kürzere Zeit auch mal ganz gut, permanenter Stress (länger als 10-15min) führt aber zu Burnouts. Lassen Sie also bitte die armen Geschöpfe immer mal wieder für 10 bis 15 Minuten verschnaufen...");
		
		DynamicPanel mainPanel = new DynamicPanel();
		mainPanel.add(new JLabel("<html><font size=\"14pt\">Stressintensität einstellen:</font></html>"));
		mainPanel.add(stressSlider);
		stressLabel.setHorizontalAlignment(SwingConstants.CENTER);
		mainPanel.add(stressLabel);
		//mainPanel.addFillEmpty();
		
		JPanel contentPane = new JPanel(new BorderLayout(5,5));
		contentPane.add(mainPanel, BorderLayout.CENTER);
		contentPane.add(stressButton, BorderLayout.EAST);
		contentPane.add(descPanel, BorderLayout.NORTH);
		
		stressSlider.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent arg0)
			{
				double relStress = (stressButton.isSelected() ? (stressSlider.getValue()) / 100.0 : 0.0);
				stressLabel.setText("Stressintensität: " + Integer.toString(stressSlider.getValue()) + "%");
				if(relStress > 1.0 || relStress < 0.0)
				{
					client.sendError("Programming Error: cell stress is " + Double.toString(relStress) + " (must be 0<=stress<=1)");
					return;
				}
				
				setFlowRate(0, TOTAL_FLOW * (1-relStress*MAX_STRESS));
				setFlowRate(1, TOTAL_FLOW * (relStress*MAX_STRESS));
			}
		});
		
		stressButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if(stressButton.isSelected())
				{
					stressButton.setText("<html><center><font size=\"30\" color=\"#880000\"><b>Zellen<br />gestresst</b></font></center></html>");
					stressButton.setBackground(new Color(1.0f, 0.3f, 0.3f));
				}
				else
				{
					stressButton.setText("<html><center><font size=\"30\" color=\"#008800\"><b>Zellen<br />am relaxen</b></font></center></html>");
					stressButton.setBackground(new Color(0.3f, 1.0f, 0.3f));
				}
				
				double relStress = (stressButton.isSelected() ? (stressSlider.getValue()) / 100.0 : 0.0);
				
				if(relStress > 1.0 || relStress < 0.0)
				{
					client.sendError("Programming Error: cell stress is " + Double.toString(relStress) + " (must be 0<=stress<=1)");
					return;
				}
				setFlowRate(0, TOTAL_FLOW * (1-relStress*MAX_STRESS));
				setFlowRate(1, TOTAL_FLOW * (relStress*MAX_STRESS));
			}
		});
		
		frame.setContentPane(contentPane);
		frame.pack();
	}
}
