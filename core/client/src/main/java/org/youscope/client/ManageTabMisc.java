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
package org.youscope.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;

import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import org.youscope.uielements.DescriptionPanel;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.IntegerTextField;
import org.youscope.uielements.StandardFormats;

/**
 * @author Moritz Lang
 *
 */
class ManageTabMisc extends ManageTabElement
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -6539787664139054703L;
	private JFormattedTextField timeoutField = new JFormattedTextField(StandardFormats.getIntegerFormat());
	private JFormattedTextField pingPeriodField = new JFormattedTextField(StandardFormats.getIntegerFormat());
	private boolean initializing = false;
	private boolean somethingChanged = false;
	private final DynamicPanel imageBufferPanel;
	private final JCheckBox setImageBufferBox = new JCheckBox("Configure image buffer size.");
	private final JLabel imageBufferLabel = new JLabel("Image buffer size in MB:");
	private final IntegerTextField imageBufferField = new IntegerTextField(100);
	private boolean imageBufferSupported = true;
	ManageTabMisc()
	{
		setOpaque(false);
		
		JPanel communicationPanel = new JPanel(new GridLayout(2, 2, 2, 2));
		communicationPanel.setOpaque(false);
		communicationPanel.add(new JLabel("Communication Timeout (ms):"));
		communicationPanel.add(timeoutField);
		communicationPanel.add(new JLabel("Communication Ping Period (ms):"));
		communicationPanel.add(pingPeriodField);
		communicationPanel.setBorder(new TitledBorder("Hardware Communication"));
		
		imageBufferPanel = new DynamicPanel();
		imageBufferPanel.add(setImageBufferBox);
		setImageBufferBox.setOpaque(false);
		setImageBufferBox.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if(initializing)
					return;
				boolean selected = setImageBufferBox.isSelected();
				imageBufferLabel.setVisible(selected);
				imageBufferField.setVisible(selected);
				somethingChanged = true;
			}
		});
		imageBufferPanel.add(imageBufferLabel);
		imageBufferField.setMinimalValue(1);
		imageBufferPanel.add(imageBufferField);
		imageBufferPanel.setBorder(new TitledBorder("Image Buffer"));
		
		ActionListener changeListener = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(initializing)
					return;
				somethingChanged = true;
			}		
		};
		imageBufferField.addActionListener(changeListener);
		
		timeoutField.addActionListener(changeListener);
		
		pingPeriodField.addActionListener(changeListener);
		
		DynamicPanel contentPanel = new DynamicPanel();
		contentPanel.add(communicationPanel);
		contentPanel.add(imageBufferPanel);
		contentPanel.addFillEmpty();
		
		DescriptionPanel descriptionPanel = new DescriptionPanel("Description", "Here you can set several settings influencing how YouScope communicates to the microscope hardware, and how it stores image data in the RAM before saving to the disk.\n"
			+ "The communication timeout specifies how long YouScope will wait for a device to accomplish a task before assuming that a hardware/firmware error occured, i.e. that the specific device will not indicate that a given task was done. Typically, 5s are sufficient. However, when using e.g. a slow stage which needs considerable time to move from one well to another well far away, a higher communication timeout might be appropriate.\n"
			+ "The ping period specifies the time - for devices which do not support handshaking but only polling - between two polls, e.g. when waiting for a device to finish a state change, in which period YouScope asks the device if the state change is finished. A short ping period can reduce unnessecary waiting times, however, produces overhead which might slow down slower computers. A ping period of 10ms is typically appropriate.\n"
			+ "Finally, YouScope stores images taken by cameras in a temporary buffer before saving them to the disk. The normal buffer size (i.e. unchecked check box) is typically sufficient for most applications. However, when imaging fast or even faster than the data can be saved to the disk (depends on hard-disk used for image saving), this buffer can overflow, i.e. some older images might not be saved but discarded. In this case, allocating more space/buffer size can significantly increase the maximal time it can be imaged with high speed before this effect taking place. The buffer size should be smaller than the minimally free RAM space, i.e. the space used by the operating system and all other processes. As a rule of thumb, the free space after setting the buffer size should be around 10 percent of the total space, and at least a few hundret MB (depends on operating system).");
		
		setLayout(new BorderLayout());
		setOpaque(false);
		add(contentPanel, BorderLayout.CENTER);
		JScrollPane scrollPane = new JScrollPane(descriptionPanel);
		scrollPane.setPreferredSize(new Dimension(400, 150));
		add(scrollPane, BorderLayout.NORTH);
	}
	
	@Override
	public void initializeContent()
	{
		initializing = true;
		int timeout;
		int pingPeriod;
		try
		{
			timeout = YouScopeClientImpl.getMicroscope().getMicroscopeConfiguration().getCommunicationTimeout();
			pingPeriod = YouScopeClientImpl.getMicroscope().getMicroscopeConfiguration().getCommunicationPingPeriod();
		}
		catch(Exception e)
		{
			ClientSystem.err.println("Could not get communication timeout/ping period.", e);
			timeout = 5000;
			pingPeriod = 10;
		}
		timeoutField.setValue(timeout);
		pingPeriodField.setValue(pingPeriod);
		
		int imageBufferSize = -1;
		try
		{
			imageBufferSize = YouScopeClientImpl.getMicroscope().getMicroscopeConfiguration().getImageBufferSize();
		}
		catch(@SuppressWarnings("unused") UnsupportedOperationException e)
		{
			imageBufferSupported = false;
		}
		catch(RemoteException e)
		{
			ClientSystem.err.println("Could not get image buffer size.", e);
		}
		if(imageBufferSupported)
		{
			if(imageBufferSize > 0)
			{
				setImageBufferBox.setSelected(true);
				imageBufferField.setValue(imageBufferSize);
			}
			else
			{
				setImageBufferBox.setSelected(false);
				imageBufferField.setVisible(false);
				imageBufferLabel.setVisible(false);
			}
		}
		else
		{
			imageBufferPanel.setVisible(false);
		}
		
		initializing = false;
	}
	@Override
	public boolean storeContent()
	{
		if(somethingChanged)
		{
			int newTimeout = ((Number)timeoutField.getValue()).intValue();
			try
			{
				YouScopeClientImpl.getMicroscope().getMicroscopeConfiguration().setCommunicationTimeout(newTimeout);
			}
			catch(Exception e1)
			{
				ClientSystem.err.println("Could not set communication timeout.", e1);
			}
			
			int pingPeriod = ((Number)pingPeriodField.getValue()).intValue();
			try
			{
				YouScopeClientImpl.getMicroscope().getMicroscopeConfiguration().setCommunicationPingPeriod(pingPeriod);
			}
			catch(Exception e1)
			{
				ClientSystem.err.println("Could not set communication ping period.", e1);
			}
			
			if(imageBufferSupported)
			{
				int imageBufferSize = setImageBufferBox.isSelected() ? imageBufferField.getValue() : -1;
				try
				{
					YouScopeClientImpl.getMicroscope().getMicroscopeConfiguration().setImageBufferSize(imageBufferSize);
				}
				catch(Exception e1)
				{
					ClientSystem.err.println("Could not set image buffer size.", e1);
				}
			}
		}
		
		return somethingChanged;
	}
}
