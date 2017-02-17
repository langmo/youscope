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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.youscope.clientinterfaces.StandardProperty;
import org.youscope.clientinterfaces.YouScopeFrame;

/**
 * @author Moritz Lang
 */
class CustomizationFrame
{
    private YouScopeFrame frame;

    private final CustomizationTabAppearance tabAppearance = new CustomizationTabAppearance();
    private final CustomizationTabMeasurement tabMeasurement = new CustomizationTabMeasurement();
    private final CustomizationTabLiveStream tabLiveStream;
    CustomizationFrame(YouScopeFrame frame)
    {
        this.frame = frame;
        frame.setTitle("Customization");
        frame.setResizable(false);
        frame.setClosable(true);
        frame.setMaximizable(false);
        
        tabLiveStream  = new CustomizationTabLiveStream(frame);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    saveSettings();
                }
            });
        JButton saveAndExitButton = new JButton("Save & Exit");
        saveAndExitButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    saveSettings();
                    try
                    {
                        CustomizationFrame.this.frame.setVisible(false);
                    }
                    catch (Exception e1)
                    {
                        ClientSystem.err.println("Could not close frame.", e1);
                    }
                }
            });
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 3, 3));
        buttonPanel.setOpaque(false);
        buttonPanel.add(saveButton);
        buttonPanel.add(saveAndExitButton);

        // Initialize tabs
        tabAppearance.initializeContent();
        tabLiveStream.initializeContent();
        tabMeasurement.initializeContent();
        
        // Create single tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Appearance", tabAppearance);
        tabbedPane.addTab("LiveStream", tabLiveStream);
        tabbedPane.addTab("Measurements", tabMeasurement);

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
        contentPane.add(tabbedPane, BorderLayout.CENTER);
        frame.setContentPane(contentPane);
        frame.pack();
    }

    private void saveSettings()
    {
        tabAppearance.storeContent();
        tabMeasurement.storeContent();
        tabLiveStream.storeContent();
        PropertyProviderImpl.getInstance().setProperty(StandardProperty.PROPERTY_IS_CONFIGURED, true);

    }
    
}
