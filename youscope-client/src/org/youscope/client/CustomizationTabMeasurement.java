package org.youscope.client;

import java.awt.BorderLayout;
import java.rmi.RemoteException;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;

import org.youscope.clientinterfaces.StandardProperty;
import org.youscope.common.saving.SaveSettingsConfiguration;
import org.youscope.uielements.ComponentComboBox;
import org.youscope.uielements.DynamicPanel;

class CustomizationTabMeasurement extends ManageTabElement
{

    /**
     * Serial Version UID.
     */
    private static final long serialVersionUID = 1706695732109422879L;

    private JCheckBox cameraStartupField = new JCheckBox("Add current camera settings to measurement start setting.", true);
    private JComboBox<String> imageTypeField;
    private final ComponentComboBox<SaveSettingsConfiguration> saveSettingsBox;
    CustomizationTabMeasurement()
    {
        setLayout(new BorderLayout());
        setBorder(new TitledBorder("Measurement"));
        setOpaque(false);
        
        // Get save settings types
        saveSettingsBox = new ComponentComboBox<SaveSettingsConfiguration>(new YouScopeClientConnectionImpl(), SaveSettingsConfiguration.class);
        
        // Get supported image types
 		String[] imageTypes;
 		try
 		{
 			imageTypes = YouScopeClientImpl.getServer().getProperties().getSupportedImageFormats();
 		}
 		catch(RemoteException e1)
 		{
 			ClientSystem.err.println("Could not obtain supported image file types from server.", e1);
 			imageTypes = new String[0];
 		}
 		imageTypeField = new JComboBox<String>(imageTypes);
        
        DynamicPanel content = new DynamicPanel();
        
        content.add(new JLabel("Standard Save Settings"));
        content.add(saveSettingsBox);
        
        content.add(new JLabel("Standard Image File Type"));
        content.add(imageTypeField);
        
        content.add(new JLabel(
                                "<html>Possibility to automatically add camera settings to measurement start-up settings.<br />" +
                                "Depending on the camera, this feature might lead to errors directly after starting a measurement.</html>"));
        cameraStartupField.setOpaque(false);
        content.add(cameraStartupField);
        content.addFillEmpty();
        add(content, BorderLayout.CENTER);
    }
    
    @Override
    public void initializeContent()
    {
        cameraStartupField.setSelected((Boolean) ConfigurationSettings.getProperty(
        		StandardProperty.PROPERTY_PREINITIALIZE_CAMERA_SETTINGS));
        imageTypeField.setSelectedItem(ConfigurationSettings.getProperty(
        		StandardProperty.PROPERTY_MEASUREMENT_STANDARD_IMAGE_FILE_TYPE));
        saveSettingsBox.setSelectedElement(ConfigurationSettings.getProperty(StandardProperty.PROPERTY_MEASUREMENT_STANDARD_SAVE_SETTINGS_TYPE).toString());
        
    }

    @Override
    public boolean storeContent()
    {
        ConfigurationSettings.setProperty(StandardProperty.PROPERTY_PREINITIALIZE_CAMERA_SETTINGS, cameraStartupField.isSelected());
        ConfigurationSettings.setProperty(StandardProperty.PROPERTY_MEASUREMENT_STANDARD_SAVE_SETTINGS_TYPE, saveSettingsBox.getSelectedTypeIdentifier());
        Object selectedItem = imageTypeField.getSelectedItem();
        if(selectedItem != null)
        	ConfigurationSettings.setProperty(StandardProperty.PROPERTY_MEASUREMENT_STANDARD_IMAGE_FILE_TYPE, selectedItem.toString());
        return false;
    }

}
