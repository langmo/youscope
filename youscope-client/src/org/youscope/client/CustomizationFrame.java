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
    private final CustomizationTabSpecial tabSpecial = new CustomizationTabSpecial();
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
        tabSpecial.initializeContent();
        
        // Create single tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Appearance", tabAppearance);
        tabbedPane.addTab("LiveStream", tabLiveStream);
        tabbedPane.addTab("Special", tabSpecial);

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
        contentPane.add(tabbedPane, BorderLayout.CENTER);
        frame.setContentPane(contentPane);
        frame.pack();
    }

    private void saveSettings()
    {
        tabAppearance.storeContent();
        tabSpecial.storeContent();
        tabLiveStream.storeContent();
        ConfigurationSettings.setProperty(StandardProperty.PROPERTY_IS_CONFIGURED, true);

    }
    
}
