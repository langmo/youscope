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
package org.youscope.uielements;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.microscope.Device;
import org.youscope.common.microscope.DeviceSetting;
import org.youscope.common.microscope.FloatProperty;
import org.youscope.common.microscope.IntegerProperty;
import org.youscope.common.microscope.Property;
import org.youscope.common.microscope.ReadOnlyProperty;
import org.youscope.common.microscope.SelectableProperty;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * A panel where the user can define a set of device settings.
 * @author Moritz Lang
 */
public class DeviceSettingsPanel extends JPanel
{

    /**
     * Serial Version UID.
     */
    private static final long serialVersionUID = 7675638567991178328L;

    private final Vector<ActionListener> actionListeners = new Vector<ActionListener>();
    
    private final JTable table;

    private final DeviceTableModel model;

    private final Vector<DeviceSetting> settings = new Vector<DeviceSetting>();

    private final YouScopeClient client;
    
    private final YouScopeServer server;
    
    private final boolean onlyAbsoluteSettings;
    
    private final JButton deleteSettingButton;
    private final JButton newDeviceSettingButton;
    private final JButton downButton;
    private final JButton upButton;
    
    /**
     * Constructor.
     * No device settings set, yet.
     * @param client Interface to the UI.
     * @param server Interface to the microscope.
     */
    public DeviceSettingsPanel(YouScopeClient client, YouScopeServer server)
    {
        this(client, server, false);
    }
    
    /**
     * Constructor.
     * @param settings Initial device settings.
     * @param client Interface to the UI.
     * @param server Interface to the microscope.
     */
    public DeviceSettingsPanel(DeviceSetting[] settings, YouScopeClient client, YouScopeServer server)
    {
        this(client, server, false);
        setSettings(settings);
    }
    
    /**
     * Constructor.
     * @param client Interface to the UI.
     * @param server Interface to the microscope.
     * @param onlyAbsoluteSettings If true, only absolute (not relative) device settings can be configured.
     */
    public DeviceSettingsPanel(YouScopeClient client, YouScopeServer server, boolean onlyAbsoluteSettings)
    {
        super(new BorderLayout(2, 2));
        this.client = client;
        this.server = server;
        this.onlyAbsoluteSettings = onlyAbsoluteSettings;
        
        // Initialize table
        model = new DeviceTableModel();
        table = new JTable(model);
        
    	setOpaque(false);
    	
        table.setRowSelectionAllowed(true);
        DeviceTableEditor editor = new DeviceTableEditor();
        table.setDefaultRenderer(String.class, editor);
        table.setDefaultEditor(String.class, editor);
        JScrollPane deviceSettingsListPane = new JScrollPane(table);
        deviceSettingsListPane.setPreferredSize(new Dimension(250, 70));
        deviceSettingsListPane.setMinimumSize(new Dimension(10, 10));
        add(deviceSettingsListPane, BorderLayout.CENTER);

        // Up, down, add and remove Buttons
        Icon upButtonIcon = ImageLoadingTools.getResourceIcon("icons/arrow-090.png", "up");
        Icon downButtonIcon = ImageLoadingTools.getResourceIcon("icons/arrow-270.png", "Down");
        Icon addButtonIcon = ImageLoadingTools.getResourceIcon("icons/block--plus.png", "Add Device Setting");
        Icon deleteButtonIcon = ImageLoadingTools.getResourceIcon("icons/block--minus.png", "Delete Device Setting");
        
        if (upButtonIcon == null)
            upButton = new JButton("Up");
        else
            upButton = new JButton(upButtonIcon);
        upButton.setOpaque(false);
        
        if (downButtonIcon == null)
            downButton = new JButton("Down");
        else
            downButton = new JButton(downButtonIcon);
        downButton.setOpaque(false);
        upButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent arg0)
                {
                    moveUpDown(true);
                }
            });
        downButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent arg0)
                {
                    moveUpDown(false);
                }
            });

        if (addButtonIcon == null)
            newDeviceSettingButton = new JButton("New");
        else
            newDeviceSettingButton = new JButton(addButtonIcon);
        newDeviceSettingButton.setOpaque(false);
        newDeviceSettingButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	Device[] devices = loadDevices();
                    if (devices.length < 1)
                    {
                    	DeviceSettingsPanel.this.client.sendError("No devices initialized.");
                    	return;
                    }
                    DeviceSetting deviceSetting = null;
                    for(Device device : devices)
                    {
                    	Property[] properties = loadDeviceProperties(device);
                    	if(properties.length > 0)
                    	{
                    		deviceSetting = new DeviceSetting();
                    		deviceSetting.setAbsoluteValue(true);
                    		try
                    		{
                    			deviceSetting.setDeviceProperty(device.getDeviceID(), properties[0].getPropertyID());
                    			deviceSetting.setValue(properties[0].getValue());
                    		}
                    		catch(Exception e1)
                    		{
                    			DeviceSettingsPanel.this.client.sendError("Could not read device settings.", e1);
                    		}
                    		break;
                    	}
                    }
                    
                    if (deviceSetting == null)
                    {
                    	DeviceSettingsPanel.this.client.sendError("No devices with editable properties initialized.");
                        return;
                    }
                    DeviceSettingsPanel.this.settings.add(deviceSetting);
                    model.fireTableRowsInserted(DeviceSettingsPanel.this.settings.size() - 1,
                            DeviceSettingsPanel.this.settings.size() - 1);
                    notifyListeners();
                }
            });
        
        if (deleteButtonIcon == null)
            deleteSettingButton = new JButton("Delete");
        else
            deleteSettingButton = new JButton(deleteButtonIcon);
        deleteSettingButton.setOpaque(false);
        deleteSettingButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    int minRow = 99999;
                    int maxRow = -1;
                    Vector<DeviceSetting> selectedSettings = new Vector<DeviceSetting>();
                    for (int row : table.getSelectedRows())
                    {
                        selectedSettings.add(DeviceSettingsPanel.this.settings.elementAt(row));
                        if (row < minRow)
                            minRow = row;
                        if (row > maxRow)
                            maxRow = row;
                    }
                    DeviceSettingsPanel.this.settings.removeAll(selectedSettings);
                    if (maxRow >= 0)
                    {
                        model.fireTableRowsDeleted(minRow, maxRow);
                        notifyListeners();
                    }
                }
            });

        JPanel deviceButtonPanel = new JPanel(new GridLayout(1, 5, 2, 2));
        deviceButtonPanel.setOpaque(false);
        deviceButtonPanel.add(upButton);
        deviceButtonPanel.add(downButton);
        JPanel emptyPanel = new JPanel();
        emptyPanel.setOpaque(false);
        deviceButtonPanel.add(emptyPanel);
        deviceButtonPanel.add(newDeviceSettingButton);
        deviceButtonPanel.add(deleteSettingButton);
        add(deviceButtonPanel, BorderLayout.SOUTH);
    }

    /**
     * Adds a listener which gets notified if a device setting changed, was added or deleted.
     * Gets not activated by programmatically changed device settings.
     * @param listener Listener to add.
     */
    public void addActionListener(ActionListener listener)
    {
    	synchronized(actionListeners)
    	{
    		actionListeners.add(listener);
    	}
    }
    
    /**
     * Sets if the UI elements in this panel react on user input.
     * @param editable If true, elements react and allow user input.
     */
    public void setEditable(boolean editable)
    {
    	deleteSettingButton.setEnabled(editable);
        downButton.setEnabled(editable);
        newDeviceSettingButton.setEnabled(editable);
        upButton.setEnabled(editable);
    }
    
    /**
     * Removes a previously added listener.
     * @param listener Listener to remove.
     */
    public void removeActionListener(ActionListener listener)
    {
    	synchronized(actionListeners)
    	{
    		actionListeners.remove(listener);
    	}
    }
    
    private void notifyListeners()
    {
    	synchronized(actionListeners)
    	{
    		for(ActionListener listener : actionListeners)
    		{
    			listener.actionPerformed(new ActionEvent(this, 777, "Device settings changed."));
    		}
    	}
    }
    
    /**
     * Removes all initialized settings.
     */
    public void clear()
    {
        settings.removeAllElements();
        model.fireTableDataChanged();
    }

    /**
     * Returns all initialized settings.
     * @return List of initialized settings.
     */
    public DeviceSetting[] getSettings()
    {
        return settings.toArray(new DeviceSetting[settings.size()]);
    }
    
    /**
     * Sets the settings which are displayed.
     * All previously defined settings are removed.
     * @param newSettings List of settings.
     */
    public void setSettings(DeviceSetting[] newSettings)
    {
    	settings.removeAllElements();
    	if(newSettings != null)
    	{
	    	for(DeviceSetting setting : newSettings)
	    	{
	    		if(onlyAbsoluteSettings && setting.isAbsoluteValue() != true)
	    		{
	    			client.sendError("Trying to add a relative device setting to a table allowing only for aboslute ones. Removing respective element.");
	    			continue;
	    		}
	    		settings.addElement(setting.clone());
	    	}
    	}
    	model.fireTableDataChanged();
    }

    private void moveUpDown(boolean moveUp)
    {
        int idx = table.getSelectedRow();
        if (idx == -1 || (moveUp && idx == 0) || (!moveUp && idx + 1 >= settings.size()))
            return;
        int newIdx;
        if (moveUp)
            newIdx = idx - 1;
        else
            newIdx = idx + 1;
        DeviceSetting deviceSetting = settings.get(idx);
        settings.removeElementAt(idx);
        settings.add(newIdx, deviceSetting);

        model.fireTableDataChanged();
        notifyListeners();
    }

    private class DeviceTableEditor extends AbstractCellEditor implements TableCellEditor,
            TableCellRenderer
    {
        /**
         * Serial Version UID.
         */
        private static final long serialVersionUID = -9118052131595616045L;

        String value;

        @Override
		public Object getCellEditorValue()
        {
            return value;
        }

        // Implement the one method defined by TableCellEditor.
        @Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
        {
            if (column == 0)
            {
            	String[] deviceNames;
            	try
            	{
            		Device[] devices = loadDevices();
            		deviceNames = new String[devices.length];
            		for(int i=0; i<devices.length; i++)
            		{
            			deviceNames[i] = devices[i].getDeviceID();
            		}
            	}
            	catch(Exception e)
            	{
            		client.sendError("Could not get device names.", e);
            		deviceNames = new String[0];
            	}
                JComboBox<String> deviceField = new JComboBox<String>(deviceNames);
                deviceField.addActionListener(new DeviceFieldActionListener(row, deviceField));
                deviceField.addPopupMenuListener(new ComboBoxActivationListener());
                return deviceField;
            } 
            else if (column == 1)
            {
            	String device = settings.elementAt(row).getDevice();
            	Vector<String> propertyNames = new Vector<String>();
            	try
            	{
            		Property[] properties = server.getMicroscope().getDevice(device).getEditableProperties(); 
            		for(int i = 0; i < properties.length; i++)
            		{
            			propertyNames.addElement(properties[i].getPropertyID());
            		}
            	}
            	catch(Exception e)
            	{
            		client.sendError("Could not load properties of device " + device + ".", e);
            		propertyNames.clear();
            	}
                JComboBox<String> propertyField =  new JComboBox<String>(propertyNames);
                propertyField.addActionListener(new PropertyFieldActionListener(row, propertyField));
                propertyField.addPopupMenuListener(new ComboBoxActivationListener());
                return propertyField;
            } 
            else if (!onlyAbsoluteSettings && column == 2)
            {
                settings.elementAt(row)
                        .setAbsoluteValue(!settings.elementAt(row).isAbsoluteValue());
                fireEditingStopped();
                return getTableCellRendererComponent(table, model.getValueAt(row, column), true,
                        true, row, column);
            } 
            else // column == 3
            {
                DeviceSetting setting = settings.elementAt(row);
                try
                {
	                Device device = server.getMicroscope().getDevice(setting.getDevice());
	                Property property = device.getProperty(setting.getProperty());
	                if (property instanceof SelectableProperty)
	                {
	                    JComboBox<String> comboBoxValueField = new JComboBox<String>(((SelectableProperty)property).getAllowedPropertyValues());
	                    comboBoxValueField.setSelectedItem(setting.getStringValue());
	                    comboBoxValueField.addActionListener(new ValueFieldActionListener(row,
	                            comboBoxValueField));
	                    comboBoxValueField.addPopupMenuListener(new ComboBoxActivationListener());
	                    return comboBoxValueField;
	                }
	                else if(property instanceof IntegerProperty)
	                {
	                	JFormattedTextField integerField = new JFormattedTextField(StandardFormats.getIntegerFormat());
	                    integerField.setValue(setting.getIntegerValue());
	                    integerField.addActionListener(new ValueFieldActionListener(row, integerField));
	                    integerField.addFocusListener(new ValueFieldActionListener(row, integerField));
	                    integerField.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
	                    return integerField;
	                }
	                else if(property instanceof FloatProperty)
	                {
	                	JFormattedTextField floatField = new JFormattedTextField(StandardFormats.getDoubleFormat());
	                    floatField.setValue(setting.getFloatValue());
	                    floatField.addActionListener(new ValueFieldActionListener(row, floatField));
	                    floatField.addFocusListener(new ValueFieldActionListener(row, floatField));
	                    floatField.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
	                    return floatField;
	                }
	                else if(property instanceof ReadOnlyProperty)
	                {
	                	JTextField textField = new JTextField(setting.getStringValue());
                        textField.setEditable(false);
                        return textField;
	                }
	                else // StringProperty
	                {
	                	JTextField textField = new JTextField(setting.getStringValue());
                        textField.addActionListener(new ValueFieldActionListener(row, textField));
                        textField.addFocusListener(new ValueFieldActionListener(row, textField));
                        return textField;
	                }
                }
                catch(Exception e)
                {
                	client.sendError("Could not determine type of property " + setting.getDevice() + "." + setting.getProperty() + ".", e);
                	JTextField unknownField = new JTextField(setting.getStringValue());
                	unknownField.setEditable(false);
                    return unknownField;
                }
            }
        }

        class ComboBoxActivationListener implements PopupMenuListener
        {
            @Override
			public void popupMenuCanceled(PopupMenuEvent e)
            {
                fireEditingCanceled();
            }

            @Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
            {
                fireEditingStopped();
            }

            @Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e)
            {
                // Do nothing.
            }
        }

        class DeviceFieldActionListener implements ActionListener
        {
            int row;

            JComboBox<String> deviceField;

            public DeviceFieldActionListener(int row, JComboBox<String> deviceField)
            {
                this.row = row;
                this.deviceField = deviceField;
            }

            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                String deviceName = (String) deviceField.getSelectedItem();
                if (deviceName == null)
                {
                	client.sendError("Device name is null.");
                    return;
                }
                DeviceSetting setting;
                try
                {
	                Device device = server.getMicroscope().getDevice(deviceName);
	                Property[] properties = device.getEditableProperties();
	                if(properties.length < 1)
	                {
	                	client.sendError("Device does not have properties.");
	                    return;
	                }
	                setting = new DeviceSetting();
	                setting.setAbsoluteValue(true);
	                setting.setDeviceProperty(device.getDeviceID(), properties[0].getPropertyID());
	                setting.setValue(properties[0].getValue());
                }
                catch(Exception e)
                {
                	client.sendError("Could not change device setting.", e);
                	return;
                }
                
                settings.setElementAt(setting, row);
                model.fireTableRowsUpdated(row, row);
                notifyListeners();
            }
        }

        class PropertyFieldActionListener implements ActionListener
        {
            int row;

            JComboBox<String> propertyField;

            public PropertyFieldActionListener(int row, JComboBox<String> propertyField)
            {
                this.row = row;
                this.propertyField = propertyField;
            }

            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                String device = settings.elementAt(row).getDevice();
                String property = (String) propertyField.getSelectedItem();
                DeviceSetting setting = new DeviceSetting();
                setting.setAbsoluteValue(true);
                setting.setDeviceProperty(device, property);
                try
				{
					setting.setValue(server.getMicroscope().getDevice(device).getProperty(property).getValue());
				}
				catch(Exception e)
				{
					client.sendError("Could not determine value of property " + device +"." + property + ".", e);
					return;
				}
                settings.setElementAt(setting, row);
                model.fireTableRowsUpdated(row, row);
                notifyListeners();
            }
        }

        class ValueFieldActionListener implements ActionListener, FocusListener
        {
            int row;

            JComponent valuesField;

            public ValueFieldActionListener(int row, JComponent valuesField)
            {
                this.row = row;
                this.valuesField = valuesField;
            }

            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                setValues();
            }

            private void setValues()
            {
                String value;
                if (valuesField instanceof JFormattedTextField)
                    value = ((JFormattedTextField) valuesField).getValue().toString();
                else if (valuesField instanceof JTextField)
                    value = ((JTextField) valuesField).getText();
                else if (valuesField instanceof JComboBox)
                    value = (String) ((JComboBox<?>) valuesField).getSelectedItem();
                else
                    return;
                settings.elementAt(row).setValue(value);
                model.fireTableRowsUpdated(row, row);
                notifyListeners();
            }

            @Override
            public void focusGained(FocusEvent arg0)
            {
                // Do nothing.

            }

            @Override
            public void focusLost(FocusEvent arg0)
            {
                if (valuesField instanceof JTextField)
                    ((JTextField) valuesField).postActionEvent();
            }
        }

        @Override
		public Component getTableCellRendererComponent(JTable table, Object object,
                boolean isSelected, boolean hasFocus, int row, int column)
        {
            DeviceSetting setting = settings.elementAt(row);
            JLabel label = new JLabel(object.toString());
            label.setBorder(new EmptyBorder(2, 2, 2, 2));
            if (column == 2 && !onlyAbsoluteSettings)
            {
            	try
            	{
	            	Device device = server.getMicroscope().getDevice(setting.getDevice());
	            	Property property = device.getProperty(setting.getProperty());
	            	
	            	if(property instanceof IntegerProperty || property instanceof FloatProperty)
	            	{
	            		if (isSelected)
	    	            {
	    	                label.setBackground(table.getSelectionBackground());
	    	                label.setForeground(table.getSelectionForeground());
	    	                label.setOpaque(true);
	    	                return label;
	    	            }
						label.setOpaque(false);
						return label;
	            	}
					label.setBackground(Color.LIGHT_GRAY);
					label.setOpaque(true);
					return label;
            	}
            	catch(Exception e)
            	{
            		client.sendError("Could not detect property type of property " + setting.getDevice()+"."+setting.getProperty(), e);
            		label.setBackground(Color.RED);
	                label.setOpaque(true);
            		label.setText("error");
            		return label;
            	}
            }
			if (isSelected)
			{
			    label.setBackground(table.getSelectionBackground());
			    label.setForeground(table.getSelectionForeground());
			    label.setOpaque(true);
			    return label;
			}
			label.setOpaque(false);
			return label;
        }
    }

    private class DeviceTableModel extends AbstractTableModel
    {
        /**
         * Serial Version UID.
         */
        private static final long serialVersionUID = -4836706465571383139L;

        private String[] columnNames =
            { "Device", "Setting", "Absolute / Relative", "Value" };

        DeviceTableModel()
        {
            // Do nothing.
        }

        @Override
		public String getColumnName(int col)
        {
        	if(col == 0)
        		return columnNames[0];
        	else if(col == 1)
        		return columnNames[1];
        	else if(col == 2 && !onlyAbsoluteSettings)
        		return columnNames[2];
        	else
        		return columnNames[3];
        }

        @Override
		public int getRowCount()
        {
            return settings.size();
        }

        @Override
		public int getColumnCount()
        {
        	if(onlyAbsoluteSettings)
        		return 3;
			return 4;
        }

        @Override
		public Class<?> getColumnClass(int column)
        {
            return String.class;
        }

        @Override
		public Object getValueAt(int row, int col)
        {
            if (col == 0)
                return settings.elementAt(row).getDevice();
            else if (col == 1)
                return settings.elementAt(row).getProperty();
            else if (col == 2 && !onlyAbsoluteSettings)
            {
                if (settings.elementAt(row).isAbsoluteValue())
                    return "Absolute Value";
				return "Relative Value";
            } else
                return settings.elementAt(row).getStringValue();
        }

        @Override
		public boolean isCellEditable(int row, int col)
        {
        	if(onlyAbsoluteSettings || col == 0 || col == 1 || col == 3)
        		return true;
            DeviceSetting setting = settings.elementAt(row);
            try
            {
	            Device device = server.getMicroscope().getDevice(setting.getDevice());
	        	Property property = device.getProperty(setting.getProperty());
	        	if(property instanceof IntegerProperty || property instanceof FloatProperty)
	        		return true;
				return false;
            }
            catch(Exception e)
            {
            	client.sendError("Could not determine if property " + setting.getDevice() + "." + setting.getProperty() + " is editable.",e );
            	return false;
            }
        }
    }

    private Device[] loadDevices()
    {
        try
        {
        	// Only get devices with at least one editable property...
        	Device[] allDevices = server.getMicroscope().getDevices();
        	Vector<Device> devices = new Vector<Device>();
        	for(Device device : allDevices)
        	{
        		if(device.getEditableProperties().length > 0)
        			devices.addElement(device);
        	}
            return devices.toArray(new Device[0]);
        } 
        catch (Exception e)
        {
            client.sendError("Could not load device names.", e);
            return new Device[0];
        }
    }

    private Property[] loadDeviceProperties(Device device)
    {
        try
        {
        	return device.getEditableProperties();
        } 
        catch (Exception e)
        {
            try
			{
				client.sendError("Could not load device properties of device " + device.getDeviceID() + ".", e);
			}
			catch(Exception e1)
			{
				client.sendError("Could not load device properties of unknown device.", e1);
			}
            return new Property[0];
        } 
    }
}
