/**
 * 
 */
package org.youscope.plugin.slimidentification;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;

import org.youscope.addon.AddonException;
import org.youscope.addon.tool.ToolAddonUIAdapter;
import org.youscope.addon.tool.ToolMetadata;
import org.youscope.addon.tool.ToolMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DoubleTextField;
import org.youscope.uielements.DynamicPanel;

/**
 * @author Moritz Lang
 */
class SlimIdentification extends ToolAddonUIAdapter
{
	private final DoubleTextField attenuationFactorField = new DoubleTextField(4); 
	SlimIdentification(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(), client, server);
	}

	public final static String TYPE_IDENTIFIER = "YouScope.SlimIdentification";
	
	static ToolMetadata getMetadata()
	{
		return new ToolMetadataAdapter(TYPE_IDENTIFIER, "SLIM Identification", new String[]{"misc"}, "icons/camera-lens.png");
	}

	@Override
	protected Component createUI() throws AddonException {
		setMaximizable(true);
		setResizable(true);
		setTitle("Attenuation Factor Wizzard");
		
		DynamicPanel contentPane = new DynamicPanel();
		contentPane.add(new JLabel("Attenuation Factor"));
		attenuationFactorField.setMinimalValue(0);
		contentPane.add(attenuationFactorField);
		JButton wizzardButton = new JButton("Attenuation Factor Wizzard");
		wizzardButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				YouScopeFrame frame = new AttenuationFactorWizzard(getClient(), getServer()).toFrame();
				getContainingFrame().addModalChildFrame(frame);
				frame.setVisible(true);
			}
		});
		contentPane.add(wizzardButton);
		return contentPane;
	}
}
