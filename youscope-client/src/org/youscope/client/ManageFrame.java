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
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.youscope.clientinterfaces.YouScopeFrame;


/**
 * The central frame to change the configuration of the microscope. Consists of several tabs.
 * @author Moritz Lang
 *
 */
class ManageFrame
{
	
	
	private final YouScopeFrame			frame;
	
	private final JTabbedPane tabbedPane;
	private final JPanel buttonPanel;
	private final ManageTabElement[] tabElements;
	private int lastTabIndex = -1;
	
	private boolean somethingChanged = false;
	
	ManageFrame(YouScopeFrame frame)
	{
		this.frame = frame;
		frame.setTitle("Edit Microscope Configuration");
		frame.setResizable(true);
		frame.setClosable(true);
		frame.setMaximizable(true);

		// Create single tabs
		tabElements = new ManageTabElement[]
		{
			new ManageTabLoadedDevices(frame),
			new ManageTabDelays(),
			new ManageTabStateLabels(),
			new ManageTabStandardRoles(),
			new ManageTabStageCoordinates(),
			new ManageTabCameraCoordinates(),
			new ManageTabImageSynchronization(),
			new ManageTabStartupSettings(),
			new ManageTabShutdownSettings(),
			new ManageTabChannels(frame),
			new ManageTabPixelSize(frame),
			new ManageTabMisc()
		};
		
		String[] tabElementsStrings = new String[]
		{
				"Loaded Devices",
				"Device Delays",
				"State Labels",
				"Standard Roles",
				"Stage Coordinates",
				"Camera Coordinates",
				"Image Synchronization",
				"Startup Settings",
				"Shutdown Settings",
				"Channels",
				"Pixel Size",
				"MISC"
		};
		
		tabbedPane = new JTabbedPane(JTabbedPane.LEFT, JTabbedPane.WRAP_TAB_LAYOUT);
		for(int i=0; i<tabElements.length; i++)
		{
			tabbedPane.addTab(tabElementsStrings[i], tabElements[i]);
		}
		
		// Customize tab visualization, including
		// hack around the wrong text in tab placement bug in Java
		int maxWidth = -1;
		int maxHeight = -1;
		JLabel[] tabLabels = new JLabel[tabbedPane.getTabCount()];
		for(int i=0; i<tabbedPane.getTabCount(); i++)
		{
			tabLabels[i] = new JLabel(Integer.toString(i+1)+". " + tabbedPane.getTitleAt(i), SwingConstants.LEFT);
			tabLabels[i].setFont(tabLabels[i].getFont().deriveFont(Font.BOLD, 12));
			if(tabLabels[i].getPreferredSize().width > maxWidth)
				maxWidth = tabLabels[i].getPreferredSize().width;
			if(tabLabels[i].getPreferredSize().height > maxHeight)
				maxHeight = tabLabels[i].getPreferredSize().height;
		}
		Dimension preferredDim = new Dimension(maxWidth, maxHeight);
		for(int i=0; i<tabbedPane.getTabCount(); i++)
		{
			tabLabels[i].setPreferredSize(preferredDim);
			tabbedPane.setTabComponentAt(i, tabLabels[i]);
		}
		
		
		tabbedPane.addChangeListener(new ChangeListener()
			{
				@Override
				public void stateChanged(ChangeEvent e)
				{
					int idx = tabbedPane.getSelectedIndex();
					if(idx < 0 || idx == lastTabIndex)
						return;
					if(lastTabIndex >= 0)
					{
						somethingChanged = tabElements[lastTabIndex].storeContent() | somethingChanged;
					}
					lastTabIndex = idx;
					tabElements[idx].initializeContent();
				}
			});
		
		// Create main buttons
		JButton saveButton = new JButton("Save Configuration");
		saveButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				// Transmit data from currently opened tab.
				tabElements[tabbedPane.getSelectedIndex()].storeContent();
				// Store config file.
				YouScopeClientImpl.getMainProgram().saveMicroscopeConfiguration();
				somethingChanged = false;
			}
		});
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(somethingChanged)
				{
					int shouldDelete = JOptionPane.showConfirmDialog(null, "You changed the device configuration but did not save the current state.\nAll changes will be lost as soon as the server is restarted.\n\nReally close?", "Unsaved Changes", JOptionPane. YES_NO_OPTION);
					if(shouldDelete != JOptionPane.YES_OPTION)
						return;
				}
				ManageFrame.this.frame.setVisible(false);
			}
		});
		buttonPanel = new JPanel(new GridLayout(1, 2, 3, 3));
		buttonPanel.setOpaque(false);
		buttonPanel.add(saveButton);
		buttonPanel.add(closeButton);
		
		// Initialize content.
		frame.startInitializing();
		new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					tabElements[0].initializeContent();
					// Setup layout
					JPanel contentPane = new JPanel(new BorderLayout());
					contentPane.add(buttonPanel, BorderLayout.SOUTH);
					contentPane.add(tabbedPane, BorderLayout.CENTER);
					ManageFrame.this.frame.setContentPane(contentPane);
					ManageFrame.this.frame.endLoading();
					ManageFrame.this.frame.setSize(new Dimension(800, 600));
				}
			}).start();
	}
}
