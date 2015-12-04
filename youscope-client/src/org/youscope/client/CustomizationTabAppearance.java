package org.youscope.client;

import java.awt.BorderLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;

import org.youscope.clientinterfaces.YouScopeProperties;
import org.youscope.uielements.DynamicPanel;

class CustomizationTabAppearance extends ManageTabElement
{

    /**
     * Serial Version UID.
     */
    private static final long serialVersionUID = 1706695732109422871L;

    private JComboBox<String> dockingField = new JComboBox<String>(new String[] { "Dock measurement control to main window.",
    "Show measurement control in own window." });
    
    private JComboBox<FramePositionStorage.StorageType> frameField = new JComboBox<FramePositionStorage.StorageType>(FramePositionStorage.StorageType.values());
    CustomizationTabAppearance()
    {
        setLayout(new BorderLayout());
        setBorder(new TitledBorder("Appearance"));
        setOpaque(false);
        
        DynamicPanel content = new DynamicPanel();
        content.add(new JLabel("Location of measurement control:"));
        content.add(dockingField);
        content.add(new JLabel("Remembering of window locations:"));
        content.add(frameField);
        
        content.addFillEmpty();
        add(content, BorderLayout.CENTER);
    }
    @Override
    public void initializeContent()
    {
        dockingField.setSelectedIndex(ConfigurationSettings.getProperty(YouScopeProperties.PROPERTY_DOCK_MEASUREMENT_CONTROL, true) ? 0 : 1);
        frameField.setSelectedItem(FramePositionStorage.StorageType.getType(ConfigurationSettings.getProperty(YouScopeProperties.PROPERTY_POSITION_STORAGE, FramePositionStorage.StorageType.NONE.getIdentifier())));
    }

    @Override
    public boolean storeContent()
    {
        ConfigurationSettings.setProperty(YouScopeProperties.PROPERTY_DOCK_MEASUREMENT_CONTROL, dockingField.getSelectedIndex() == 0);
        FramePositionStorage.StorageType storageType = (FramePositionStorage.StorageType)frameField.getSelectedItem();
        FramePositionStorage.getInstance().setStorageType(storageType);
        ConfigurationSettings.setProperty(YouScopeProperties.PROPERTY_POSITION_STORAGE, storageType.getIdentifier());
        return false;
    }

}
