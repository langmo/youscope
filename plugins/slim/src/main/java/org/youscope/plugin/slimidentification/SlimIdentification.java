/*******************************************************************************
 * Copyright (c) 2017 Moritz Lang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Moritz Lang - initial API and implementation
 ******************************************************************************/
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
import org.youscope.plugin.slimjob.SlimProperties;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DoubleTextField;
import org.youscope.uielements.DynamicPanel;

/**
 * @author Moritz Lang
 */
class SlimIdentification extends ToolAddonUIAdapter
{
	private final DoubleTextField attenuationFactorField = new DoubleTextField(); 
	SlimIdentification(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(), client, server);
	}

	public final static String TYPE_IDENTIFIER = "YouScope.SlimIdentification";
	
	static ToolMetadata getMetadata()
	{
		return new ToolMetadataAdapter(TYPE_IDENTIFIER, "SLIM Identification", new String[]{"misc"}, 
				"Tool for the parametrization of YouScope for SLIM microscopy.",
				"icons/camera-lens.png");
	}

	@Override
	protected Component createUI() throws AddonException {
		setMaximizable(true);
		setResizable(true);
		setTitle("SLIM Identification");
		
		DynamicPanel contentPane = new DynamicPanel();
		contentPane.add(new JLabel("Attenuation Factor"));
		attenuationFactorField.setMinimalValue(0);
		contentPane.add(attenuationFactorField);
		JButton wizzardButton = new JButton("Attenuation Factor Wizard");
		wizzardButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				AttenuationFactorWizard wizard = new AttenuationFactorWizard(getClient(), getServer());
				wizard.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) 
					{
						actualizeValues();
					}
				});
				YouScopeFrame frame = wizard.toFrame();
				getContainingFrame().addModalChildFrame(frame);
				frame.setVisible(true);
			}
		});
		contentPane.add(wizzardButton);
		actualizeValues();
		return contentPane;
	}
	
	private void actualizeValues()
	{
		SlimProperties properties = new SlimProperties(getClient());
		attenuationFactorField.setValue(properties.getAttenuationFactor());
	}
}
