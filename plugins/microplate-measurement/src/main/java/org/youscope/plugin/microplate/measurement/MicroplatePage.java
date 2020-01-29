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
package org.youscope.plugin.microplate.measurement;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import org.youscope.addon.component.ComponentMetadata;
import org.youscope.addon.measurement.MeasurementAddonUIPage;
import org.youscope.addon.microplate.MicroplateConfiguration;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.plugin.microplate.measurement.MicroplateMeasurementConfiguration;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.SubConfigurationPanel;
/**
 * @author Moritz Lang
 *
 */
class MicroplatePage extends MeasurementAddonUIPage<MicroplateMeasurementConfiguration>
{

	/**
	 * Serial Verision UID.
	 */
	private static final long	serialVersionUID	= -779685914335388563L;
	
	private final YouScopeClient	client;

	private SubConfigurationPanel<MicroplateConfiguration> microplatePanel = null;
	
	private static final String DEFAULT_MICROPLATE_TYPE_IDENTIFIER = "YouScope.microplate.AnsiSBS96Microplate";
	
	private JRadioButton					customPositionsField			= new JRadioButton("Custom Positions.", false);
	private JRadioButton					microplateField			= new JRadioButton("Microplate Positions.", false);
	
	MicroplatePage(YouScopeClient client)
	{
		this.client = client;
	}

	@Override
	public void loadData(MicroplateMeasurementConfiguration configuration)
	{
		if(configuration.getMicroplateConfiguration() != null)
		{
			microplateField.setSelected(true);
			microplatePanel.setConfiguration(configuration.getMicroplateConfiguration());
			microplatePanel.setVisible(true);
		}
		else
		{
			customPositionsField.setSelected(true);				
			microplatePanel.setVisible(false);
		}
	}

	@Override
	public boolean saveData(MicroplateMeasurementConfiguration configuration)
	{
		if(customPositionsField.isSelected())
		{
			configuration.setMicroplateConfiguration(null);
		}
		else
		{
			MicroplateConfiguration microplateConfiguration = microplatePanel.getConfiguration();
			configuration.setMicroplateConfiguration(microplateConfiguration);
		}
		return true;
	}

	@Override
	public void setToDefault(MicroplateMeasurementConfiguration configuration)
	{
		try 
		{
			ComponentMetadata<? extends MicroplateConfiguration> metadata = client.getAddonProvider().getComponentMetadata(DEFAULT_MICROPLATE_TYPE_IDENTIFIER, MicroplateConfiguration.class);
			MicroplateConfiguration microplateConfiguration = metadata.getConfigurationClass().newInstance();
			configuration.setMicroplateConfiguration(microplateConfiguration);
			
		} catch (@SuppressWarnings("unused") Exception e) {
			// do nothing. Default only not set.
		}

	}

	@Override
	public String getPageName()
	{
		return "Imaging Positions";
	}
	
	@Override
	public void createUI(YouScopeFrame frame)
	{
		setLayout(new BorderLayout());
		
		ButtonGroup positionTypeGroup = new ButtonGroup();
		positionTypeGroup.add(customPositionsField);
		positionTypeGroup.add(microplateField);
		DynamicPanel topPanel = new DynamicPanel();
		topPanel.add(customPositionsField);
		topPanel.add(microplateField);
		
		ActionListener positionTypeListener = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				if(customPositionsField.isSelected())				
					microplatePanel.setVisible(false);
				else
					microplatePanel.setVisible(true);
			}
		};
		customPositionsField.addActionListener(positionTypeListener);
		microplateField.addActionListener(positionTypeListener);
		
		microplatePanel = new SubConfigurationPanel<MicroplateConfiguration>("Microplate type:", null, MicroplateConfiguration.class, SubConfigurationPanel.Type.LIST, client, frame);
		add(topPanel, BorderLayout.NORTH);
		add(microplatePanel, BorderLayout.CENTER);

		setBorder(new TitledBorder("Imaging Positions"));
	}
	
	
}
