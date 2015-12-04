package org.youscope.client;

import java.awt.BorderLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;

import org.youscope.clientinterfaces.YouScopeProperties;
import org.youscope.uielements.DynamicPanel;

class CustomizationTabSpecial extends ManageTabElement
{

    /**
     * Serial Version UID.
     */
    private static final long serialVersionUID = 1706695732109422879L;

    private JCheckBox cameraStartupField = new JCheckBox("Add current camera settings to measurement start setting.", true);
    
    CustomizationTabSpecial()
    {
        setLayout(new BorderLayout());
        setBorder(new TitledBorder("Special"));
        setOpaque(false);
        
        DynamicPanel content = new DynamicPanel();
        content.add(new JLabel(
                                "<html>Activate to add curent camera settings to each measurement start-up setting.<br />" +
                                "Depending on the camera, this feature might lead to errors directly after starting a measurement.</html>"));
        cameraStartupField.setOpaque(false);
        content.add(cameraStartupField);
        content.addFillEmpty();
        add(content, BorderLayout.CENTER);
    }
    
    @Override
    public void initializeContent()
    {
        cameraStartupField.setSelected(ConfigurationSettings.getProperty(
                YouScopeProperties.PROPERTY_PREINITIALIZE_CAMERA_SETTINGS,
                false));
    }

    @Override
    public boolean storeContent()
    {
        ConfigurationSettings.setProperty(YouScopeProperties.PROPERTY_PREINITIALIZE_CAMERA_SETTINGS, cameraStartupField.isSelected());
        return false;
    }

}
