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
package org.youscope.plugin.deviceslides;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.ParseException;
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
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonUIAdapter;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.job.basicjobs.SimpleCompositeJob;
import org.youscope.common.microscope.Device;
import org.youscope.common.microscope.DeviceSetting;
import org.youscope.common.microscope.FloatProperty;
import org.youscope.common.microscope.IntegerProperty;
import org.youscope.common.microscope.Property;
import org.youscope.common.microscope.PropertyType;
import org.youscope.common.microscope.SelectableProperty;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.ImageLoadingTools;
import org.youscope.uielements.JobsDefinitionPanel;
import org.youscope.uielements.StandardFormats;

/**
 * @author langmo
 */
class DeviceSlidesJobConfigurationAddon  extends ComponentAddonUIAdapter<DeviceSlidesJobConfiguration>
{
    private JTable multiPosSettingsTable;

    private MultiPosDeviceTableModel multiPosSettingsTableModel;

    private Vector<Vector<Object>> settings = new Vector<Vector<Object>>();
    private Vector<DeviceSetting> headers = new Vector<DeviceSetting>();

    private String[] devices;
	
	private GridBagConstraints newLineConstr = StandardFormats.getNewLineConstraint();
	private GridBagConstraints bottomConstr = StandardFormats.getBottomContstraint();
	private JobsDefinitionPanel jobPanel;

	/**
	 * Constructor.
	 * @param client Interface to the client.
	 * @param server Interface to the server.
	 * @throws AddonException 
	 */
	public DeviceSlidesJobConfigurationAddon(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(),  client, server);
	}
    
	static ComponentMetadataAdapter<DeviceSlidesJobConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<DeviceSlidesJobConfiguration>(DeviceSlidesJobConfiguration.TYPE_IDENTIFIER, 
				DeviceSlidesJobConfiguration.class, 
				SimpleCompositeJob.class, 
				"Device-Slides", 
				new String[]{"Containers"});
	}
	
	@Override
	protected Component createUI(DeviceSlidesJobConfiguration configuration) throws AddonException
	{
		setTitle("Device-Slides Job");
		setResizable(true);
		setMaximizable(false);
		
		// Load state
        DeviceSetting[][] orgSettings = configuration.getMultiPosDeviceSettings();
        if(orgSettings.length == 0)
        {
        	// Do nothing.
        }
        else if(!isValid(orgSettings))
        {
        	sendErrorMessage("Last configuration is invalid. Starting with empty device setting list.", null);
        }
        else
        {
        	for(DeviceSetting setting : orgSettings[0])
        	{
        		headers.add(setting);
        	}
        	for(DeviceSetting[] subSettings : orgSettings)
        	{
        		Vector<Object> values = new Vector<Object>();
        		for(DeviceSetting setting : subSettings)
        		{
        			if(getPropertyType(setting) == PropertyType.PROPERTY_FLOAT)
        				values.add(setting.getFloatValue());
        			else if(getPropertyType(setting) == PropertyType.PROPERTY_INTEGER)
        				values.add(setting.getIntegerValue());
        			else
        				values.add(setting.getStringValue());
        		}
        		settings.add(values);
        	}
        }
        
        // Top Element
        GridBagLayout topLayout = new GridBagLayout();
        JPanel topPanel = new JPanel(topLayout);
        StandardFormats.addGridBagElement(new JLabel("Device Slides:"), topLayout, newLineConstr, topPanel);
        
        // List with settings
        loadDevices();
        JPanel centralPanel = new JPanel(new BorderLayout(2, 2));
        multiPosSettingsTableModel = new MultiPosDeviceTableModel();
        multiPosSettingsTable = new JTable(multiPosSettingsTableModel);
        multiPosSettingsTable.setRowSelectionAllowed(true);
        multiPosSettingsTable.setTableHeader(null);
        multiPosSettingsTable.setSurrendersFocusOnKeystroke(true);
        MultiPosDeviceTableEditor editor = new MultiPosDeviceTableEditor();
        multiPosSettingsTable.setDefaultRenderer(DeviceSetting.class, editor);
        multiPosSettingsTable.setDefaultEditor(DeviceSetting.class, editor);
        TableColumn col = multiPosSettingsTable.getColumnModel().getColumn(0);
        col.setPreferredWidth(40);
        col.setMaxWidth(40);
        col.setMinWidth(40);
        JScrollPane deviceSettingsOnListPane = new JScrollPane(multiPosSettingsTable);
        centralPanel.add(deviceSettingsOnListPane, BorderLayout.CENTER);

        // Up, down, add and remove Buttons
        Icon upButtonIcon = ImageLoadingTools.getResourceIcon("icons/arrow-090.png", "Move Upwards");
        Icon downButtonIcon = ImageLoadingTools.getResourceIcon("icons/arrow-270.png", "Move Downwards");
        Icon addRowButtonIcon = ImageLoadingTools.getResourceIcon("icons/block--plus.png", "Add Row");
        Icon deleteRowButtonIcon = ImageLoadingTools.getResourceIcon("icons/block--minus.png", "Remove Row");
        Icon addColumnButtonIcon = ImageLoadingTools.getResourceIcon("icons/block--plus.png", "Add Column");
        Icon deleteColumnButtonIcon = ImageLoadingTools.getResourceIcon("icons/block--minus.png", "Remove Column");
        
        JButton upButton;
        if (upButtonIcon == null)
            upButton = new JButton("Move Up");
        else
            upButton = new JButton("Move Up", upButtonIcon);
        upButton.setHorizontalAlignment(SwingConstants.LEFT);
        JButton downButton;
        if (downButtonIcon == null)
            downButton = new JButton("Move Down");
        else
            downButton = new JButton("Move Down", downButtonIcon);
        downButton.setHorizontalAlignment(SwingConstants.LEFT);
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

        JButton addRowButton;
        if (addRowButtonIcon == null)
            addRowButton = new JButton("Add Row");
        else
            addRowButton = new JButton("Add Row", addRowButtonIcon);
        addRowButton.setHorizontalAlignment(SwingConstants.LEFT);
        addRowButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	addRow();
                }
            });
        JButton deleteRowButton;
        if (deleteRowButtonIcon == null)
            deleteRowButton = new JButton("Delete Row");
        else
            deleteRowButton = new JButton("Delete Row", deleteRowButtonIcon);
        deleteRowButton.setHorizontalAlignment(SwingConstants.LEFT);
        deleteRowButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    int row = multiPosSettingsTable.getSelectedRow() - 1;
                    if (row < 0)
                        return;
                   
                    settings.removeElementAt(row);
                    multiPosSettingsTableModel.fireTableRowsDeleted(row, row);
                }
            });
        
        JButton addColumnButton;
        if (addColumnButtonIcon == null)
            addColumnButton = new JButton("Add Column");
        else
            addColumnButton = new JButton("Add Column", addColumnButtonIcon);
        addColumnButton.setHorizontalAlignment(SwingConstants.LEFT);
        addColumnButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	addColumn();
                }
            });
        JButton deleteColumnButton;
        if (deleteColumnButtonIcon == null)
            deleteColumnButton = new JButton("Delete Column");
        else
            deleteColumnButton = new JButton("Delete Column", deleteColumnButtonIcon);
        deleteColumnButton.setHorizontalAlignment(SwingConstants.LEFT);
        deleteColumnButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    int column = multiPosSettingsTable.getSelectedColumn() -1;
                    if (column < 0)
                        return;
                    for(Vector<Object> setting : settings)
                    {
                    	setting.removeElementAt(column);
                    }
                    headers.removeElementAt(column);
                    multiPosSettingsTableModel.fireTableStructureChanged();
                    TableColumn col = multiPosSettingsTable.getColumnModel().getColumn(0);
                    col.setPreferredWidth(40);
                    col.setMaxWidth(40);
                    col.setMinWidth(40);
        			return;
                }
            });
        
        GridBagLayout buttonLayout = new GridBagLayout();
        JPanel buttonPanel = new JPanel(buttonLayout);
        StandardFormats.addGridBagElement(addColumnButton, buttonLayout, newLineConstr,  buttonPanel);
        StandardFormats.addGridBagElement(deleteColumnButton, buttonLayout, newLineConstr,  buttonPanel);
        StandardFormats.addGridBagElement(new JPanel(), buttonLayout, newLineConstr,  buttonPanel);
        StandardFormats.addGridBagElement(addRowButton, buttonLayout, newLineConstr,  buttonPanel);
        StandardFormats.addGridBagElement(deleteRowButton, buttonLayout, newLineConstr,  buttonPanel);
        StandardFormats.addGridBagElement(upButton, buttonLayout, newLineConstr,  buttonPanel);
        StandardFormats.addGridBagElement(downButton, buttonLayout, newLineConstr,  buttonPanel);
        StandardFormats.addGridBagElement(new JPanel(), buttonLayout, bottomConstr,  buttonPanel);
        centralPanel.add(buttonPanel, BorderLayout.EAST);

        // bottom panel
        GridBagLayout bottomLayout = new GridBagLayout();
        JPanel bottomPanel = new JPanel(bottomLayout);
        StandardFormats.addGridBagElement(new JLabel("Jobs executed at every setting:"), bottomLayout, newLineConstr, bottomPanel);
        // The jobs in every position
        jobPanel = new JobsDefinitionPanel(getClient(), getServer(), getContainingFrame());
        jobPanel.setJobs(configuration.getJobs());
        StandardFormats.addGridBagElement(jobPanel, bottomLayout, newLineConstr, bottomPanel);

        // Add some rows and columns such that it doesn't look to empty.
        if(headers.size() == 0)
        	addColumn();
        if(settings.size() == 0)
        {
        	addRow();
          	addRow();
        }
        
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(topPanel, BorderLayout.NORTH);
        contentPane.add(centralPanel, BorderLayout.CENTER);
        contentPane.add(bottomPanel, BorderLayout.SOUTH);

        return contentPane;
    }
	
	private void addRow()
	{
		Vector<Object> newSetting = new Vector<Object>();
    	for(DeviceSetting setting : headers)
    	{
    		newSetting.add(getCurrentValue(setting));
    	}
    	settings.add(newSetting);
        multiPosSettingsTableModel.fireTableRowsInserted(settings.size() - 1, settings.size() - 1);
	}
	
	private void addColumn()
	{
		// Find first valid device property
    	for(int i=0; i<devices.length; i++)
    	{
    		try
			{
				for(Property property : getServer().getMicroscope().getDevice(devices[i]).getEditableProperties())
				{
					DeviceSetting newHeader = new DeviceSetting();
					newHeader.setDeviceProperty(devices[i], property.getPropertyID());
					Object value = getCurrentValue(newHeader);
					headers.add(newHeader);
					for(Vector<Object> setting : settings)
					{
						setting.add(value);
					}
					multiPosSettingsTableModel.fireTableStructureChanged();
					TableColumn col = multiPosSettingsTable.getColumnModel().getColumn(0);
			        col.setPreferredWidth(40);
			        col.setMaxWidth(40);
			        col.setMinWidth(40);
					return;
				}
			}
			catch(Exception e1)
			{
				sendErrorMessage("Could not add column.", e1);
			}
    	}
	}
	private static boolean isValid(DeviceSetting[][] settings)
	{
		if(settings.length == 0)
			return true;
		int numSettingsPerPos = settings[0].length;
		for(int i = 1; i < settings.length; i++)
		{
			// Same number of arguments?
			if(settings[i].length != numSettingsPerPos)
			{
				return false;
			}
			// Same arguments?
			for(int j=0; j<numSettingsPerPos; j++)
			{
				if(settings[i][j] == null || settings[0][j] == null
						|| settings[i][j].getDevice().compareTo(settings[0][j].getDevice()) != 0
						|| settings[i][j].getProperty().compareTo(settings[0][j].getProperty()) != 0
						|| settings[i][j].isAbsoluteValue() != settings[0][j].isAbsoluteValue())
				{
					return false;
				}
			}
		}
		return true;
	}
	
	private String[] getPropertyNames(String device)
	{
		try
		{
			Property[] properties = getServer().getMicroscope().getDevice(device).getEditableProperties();
			String[] returnValue = new String[properties.length];
			for(int i=0; i<properties.length; i++)
			{
				returnValue[i] = properties[i].getPropertyID();
			}
			return returnValue;
		}
		catch(Exception e)
		{
			sendErrorMessage("Could not get properties of device " + device + ".", e);
			return new String[0];
		}
	}
	
	private PropertyType getPropertyType(DeviceSetting setting)
	{
		try
		{
			return getServer().getMicroscope().getDevice(setting.getDevice()).getProperty(setting.getProperty()).getType();
		}
		catch(Exception e)
		{
			sendErrorMessage("Could not get type of device property " + setting.getDevice() + "." + setting.getProperty() +". Assuming string. Check configuration!", e);
			return PropertyType.PROPERTY_STRING;
		}
	}
	
	private boolean canPropertyBeRelative(DeviceSetting setting)
	{
		Property property;
		try
		{
			property = getServer().getMicroscope().getDevice(setting.getDevice()).getProperty(setting.getProperty());
		}
		catch(Exception e)
		{
			sendErrorMessage("Cannot detect if property " + setting.getDevice() + "." + setting.getProperty() +" can be relative. Assuming not.", e);
			return false;
		}
		if(property instanceof IntegerProperty || property instanceof FloatProperty)
			return true;
		return false;
	}
	
	private String[] getAllowedPropertyValues(DeviceSetting setting)
	{
		try
		{
			Property property = getServer().getMicroscope().getDevice(setting.getDevice()).getProperty(setting.getProperty());
			if(property instanceof SelectableProperty)
			{
				return ((SelectableProperty)property).getAllowedPropertyValues();
			}
			return null;
		}
		catch(Exception e)
		{
			sendErrorMessage("Could not get allowed property values of device property " + setting.getDevice() + "." + setting.getProperty() +". Check configuration!", e);
			return null;
		}
	}
	
	private Object getCurrentValue(DeviceSetting setting)
	{
		try
		{
			Property property = getServer().getMicroscope().getDevice(setting.getDevice()).getProperty(setting.getProperty());
			if(property instanceof FloatProperty)
			{
				if(setting.isAbsoluteValue())
					return ((FloatProperty)property).getFloatValue();
				return new Float(0.0F);
			}
			else if(property instanceof IntegerProperty)
			{
				if(setting.isAbsoluteValue())
					return ((IntegerProperty)property).getIntegerValue();
				return new Integer(0);
			}
			else
				return property.getValue();
		}
		catch(Exception e)
		{
			sendErrorMessage("Could not get current value of device property " + setting.getDevice() + "." + setting.getProperty() +". Check configuration!", e);
			return null;
		}
	}
    
    private void moveUpDown(boolean moveUp)
    {
        int idx = multiPosSettingsTable.getSelectedRow() - 1;
        if (idx == -1 || (moveUp && idx == 0) || (!moveUp && idx + 1 >= settings.size()))
            return;
        int newIdx;
        if (moveUp)
            newIdx = idx - 1;
        else
            newIdx = idx + 1;
        Vector<Object> values = settings.get(idx);
        settings.removeElementAt(idx);
        settings.add(newIdx, values);
        multiPosSettingsTableModel.fireTableDataChanged();
        multiPosSettingsTable.setRowSelectionInterval(newIdx+1, newIdx+1);
    }
    private void applySetting(DeviceSetting setting, int column)
	{
		if(column < 0 || column >= headers.size())
			return;
		// Check if setting changed.
		DeviceSetting oldSetting = headers.elementAt(column);
		if(setting.getDevice().compareToIgnoreCase(oldSetting.getDevice()) == 0
				&& setting.getProperty().compareToIgnoreCase(oldSetting.getProperty()) == 0
				&& setting.isAbsoluteValue() == oldSetting.isAbsoluteValue())
			return;
		
		// Setting changed.
		headers.setElementAt(setting, column);
		Object defaultValue = getCurrentValue(setting);
		for(Vector<Object> element : settings)
		{
			element.setElementAt(defaultValue, column);
		}
		multiPosSettingsTableModel.fireTableDataChanged();
	}

    private class MultiPosDeviceTableEditor extends AbstractCellEditor implements
            TableCellEditor, TableCellRenderer
    {
        /**
         * Serial Version UID.
         */
        private static final long serialVersionUID = -9118052131595616045L;

        private String value;

        @Override
		public Object getCellEditorValue()
        {
            return value;
        }

        private JButton getHeaderButton(int idx)
        {
        	DeviceSetting setting = headers.get(idx);
        	PropertyType type = getPropertyType(setting);
        	String text = setting.getDevice() + "." + setting.getProperty();
    		if(type == PropertyType.PROPERTY_STRING || type == PropertyType.PROPERTY_FLOAT)
    		{
    			if(setting.isAbsoluteValue())
    				text += " (absolute)";
    			else
    				text += " (relative)";
    		}
    		JButton activateChooserButton = new JButton(text);
    		class ButtonActionListener implements ActionListener
    		{
    			private JComponent parent;
    			private DeviceSetting setting;
    			private int idx;
    			ButtonActionListener(JComponent parent, DeviceSetting setting, int idx)
    			{
    				this.parent = parent;
    				this.setting = setting;
    				this.idx = idx;
    			}
				@Override
				public void actionPerformed(ActionEvent e)
				{
					@SuppressWarnings("unused")
					DevicePropertyChooser devicePropertyChooser = new DevicePropertyChooser(parent, setting, idx);
				}
    			
    		}
    		activateChooserButton.addActionListener(new ButtonActionListener(activateChooserButton, setting, idx));
            return activateChooserButton;
        }
        // Implement the one method defined by TableCellEditor.
        @Override
		public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column)
        {
        	if (row == 0)
            {
                return getHeaderButton(column - 1);
            }
			DeviceSetting setting = headers.get(column - 1);
			PropertyType type = getPropertyType(setting);
			Object valueObject = settings.get(row - 1).get(column - 1);
			
			if (getAllowedPropertyValues(setting) != null)
			{
				JComboBox<String> comboBox = new JComboBox<String>(getAllowedPropertyValues(setting));

				comboBox.setSelectedItem(valueObject.toString());
				comboBox.addActionListener(new ComboBoxValueListener(comboBox, row -1, column - 1));
				return comboBox;
			}
			if(type == PropertyType.PROPERTY_INTEGER && valueObject instanceof Integer)
			{
				JFormattedTextField textField = new JFormattedTextField(StandardFormats.getIntegerFormat());
				textField.setValue(valueObject);
				IntegerValueListener listener = new IntegerValueListener(textField, row -1, column - 1);
				textField.addActionListener(listener);
				textField.addFocusListener(listener);
				textField.addKeyListener(listener);
				textField.setBorder(new EmptyBorder(0, 0, 0, 0));
				textField.setMargin(new Insets(0, 0, 0, 0));
				return textField;
			}
			else if(type == PropertyType.PROPERTY_FLOAT && valueObject instanceof Float)
			{
				JFormattedTextField textField = new JFormattedTextField(StandardFormats.getDoubleFormat());
				textField.setValue(valueObject);
				FloatValueListener listener = new FloatValueListener(textField, row -1, column - 1);
				textField.addActionListener(listener);
				textField.addFocusListener(listener);
				textField.addKeyListener(listener);
				textField.setBorder(new EmptyBorder(0, 0, 0, 0));
				textField.setMargin(new Insets(0, 0, 0, 0));
				return textField;
			}
			else
			{
				JTextField textField = new JTextField(valueObject.toString());
				StringValueListener listener = new StringValueListener(textField, row -1, column - 1);
				textField.addActionListener(listener);
				textField.addFocusListener(listener);
				textField.addKeyListener(listener);
				textField.setBorder(new EmptyBorder(0, 0, 0, 0));
				textField.setMargin(new Insets(0, 0, 0, 0));
				return textField;
			}
        }
        private class ComboBoxValueListener implements ActionListener
        {
        	private JComboBox<String> box;
        	private int row;
        	private int column;
        	ComboBoxValueListener(JComboBox<String> box, int row, int column)
        	{
        		this.box = box;
        		this.row = row;
        		this.column = column;
        	}
			
        	@Override
			public void actionPerformed(ActionEvent e)
			{
				settings.get(row).setElementAt(box.getSelectedItem().toString(), column);
				fireEditingStopped();
			}
        }
        private class FloatValueListener implements ActionListener, FocusListener, KeyListener
        {
        	private JFormattedTextField field;
        	private int row;
        	private int column;
        	FloatValueListener(JFormattedTextField field, int row, int column)
        	{
        		this.field = field;
        		this.row = row;
        		this.column = column;
        	}
			
        	private void commit()
        	{
        		try
				{
        			field.commitEdit();
				}
				catch(@SuppressWarnings("unused") ParseException e)
				{
					field.setBackground(Color.RED);
					return;
				}
				field.setBackground(Color.WHITE);
				float value = ((Number)field.getValue()).floatValue();
				settings.get(row).setElementAt(value, column);
        	}
        	
        	@Override
			public void actionPerformed(ActionEvent e)
			{
        		commit();
        		fireEditingStopped();
			}

			@Override
			public void keyTyped(KeyEvent e)
			{
				commit();
			}

			@Override
			public void keyPressed(KeyEvent e)
			{
				// Do nothing
			}

			@Override
			public void keyReleased(KeyEvent e)
			{
				// Do nothing.
			}

			@Override
			public void focusGained(FocusEvent e)
			{
				// Do nothing.
				
			}

			@Override
			public void focusLost(FocusEvent e)
			{
				commit();
				fireEditingStopped();
			}
        }
        private class IntegerValueListener implements ActionListener, FocusListener, KeyListener
        {
        	private JFormattedTextField field;
        	private int row;
        	private int column;
        	IntegerValueListener(JFormattedTextField field, int row, int column)
        	{
        		this.field = field;
        		this.row = row;
        		this.column = column;
        	}
			
        	private void commit()
        	{
        		try
				{
        			field.commitEdit();
				}
				catch(@SuppressWarnings("unused") ParseException e)
				{
					field.setBackground(Color.RED);
					return;
				}
				field.setBackground(Color.WHITE);
				int value = ((Number)field.getValue()).intValue();
				settings.get(row).setElementAt(value, column);
        	}
        	
        	@Override
			public void actionPerformed(ActionEvent e)
			{
        		commit();
        		fireEditingStopped();
			}

			@Override
			public void keyTyped(KeyEvent e)
			{
				commit();
			}

			@Override
			public void keyPressed(KeyEvent e)
			{
				// Do nothing
			}

			@Override
			public void keyReleased(KeyEvent e)
			{
				// Do nothing.
			}

			@Override
			public void focusGained(FocusEvent e)
			{
				// Do nothing.
				
			}

			@Override
			public void focusLost(FocusEvent e)
			{
				commit();
				fireEditingStopped();
			}
        }
        private class StringValueListener implements ActionListener, FocusListener, KeyListener
        {
        	private JTextField field;
        	private int row;
        	private int column;
        	StringValueListener(JTextField field, int row, int column)
        	{
        		this.field = field;
        		this.row = row;
        		this.column = column;
        	}
			
        	private void commit()
        	{
        		String value = field.getText();
				settings.get(row).setElementAt(value, column);
        	}
        	@Override
			public void actionPerformed(ActionEvent e)
			{
        		commit();
        		fireEditingStopped();
			}

			@Override
			public void keyTyped(KeyEvent e)
			{
				commit();
			}

			@Override
			public void keyPressed(KeyEvent e)
			{
				// Do nothing
			}

			@Override
			public void keyReleased(KeyEvent e)
			{
				// Do nothing.
			}

			@Override
			public void focusGained(FocusEvent e)
			{
				// Do nothing.
				
			}

			@Override
			public void focusLost(FocusEvent e)
			{
				commit();
				fireEditingStopped();
			}
        }
        
        private class DevicePropertyChooser extends JPanel
        {
        	/**
			 * Serializable Version UID.
			 */
			private static final long	serialVersionUID	= -8239627177938786271L;
			private int idx;
        	private JComboBox<String> deviceField;
        	private JComboBox<String> propertyField;
        	private JComboBox<String> absoluteValueField = new JComboBox<String>(new String[]{"Absolute value", "Relative to current value"});
        	private Popup popup;
        	private JButton okButton;
            private JButton cancelButton;
        	DevicePropertyChooser(JComponent parent, DeviceSetting setting, int idx)
        	{
        		GridBagLayout layout = new GridBagLayout();
        		setLayout(layout);
        		this.idx = idx;
        		
        		deviceField = new JComboBox<String>(devices);
                deviceField.setSelectedItem(setting.getDevice());
                deviceField.addActionListener(new ActionListener()
                	{
						@Override
						public void actionPerformed(ActionEvent e)
						{
							propertyField.removeAllItems();
							for(String property : getPropertyNames(deviceField.getSelectedItem().toString()))
							{
								propertyField.addItem(property);
							}
						}
                	});
                StandardFormats.addGridBagElement(new JLabel("Device:"), layout, newLineConstr,  this);
                StandardFormats.addGridBagElement(deviceField, layout, newLineConstr,  this);
                       		
        		// Header row
                propertyField = new JComboBox<String>(getPropertyNames(deviceField.getSelectedItem().toString()));
                propertyField.setSelectedItem(setting.getProperty());
                propertyField.addActionListener(new ActionListener()
                	{
						@Override
						public void actionPerformed(ActionEvent e)
						{
							refreshAbsolute();
						}
                	});
                StandardFormats.addGridBagElement(new JLabel("Property:"), layout, newLineConstr,  this);
                StandardFormats.addGridBagElement(propertyField, layout, newLineConstr,  this);
                
                if(setting.isAbsoluteValue())
                	absoluteValueField.setSelectedIndex(0);
                else
                	absoluteValueField.setSelectedIndex(1);
                StandardFormats.addGridBagElement(new JLabel("Absolute or relative values:"), layout, newLineConstr,  this);
                StandardFormats.addGridBagElement(absoluteValueField, layout, newLineConstr,  this);
                
                
                okButton = new JButton("OK");
                okButton.addActionListener(new ActionListener()
                	{
	                	@Override
	            		public void actionPerformed(ActionEvent e)
	            		{
	                		DeviceSetting setting = new DeviceSetting();
	                		setting.setDeviceProperty(deviceField.getSelectedItem().toString(), propertyField.getSelectedItem().toString());
	                		setting.setAbsoluteValue(absoluteValueField.getSelectedIndex()==0);
	                		
	                		applySetting(setting, DevicePropertyChooser.this.idx);
	                		if(popup != null)
	                		{
	                			popup.hide();
	                			popup = null;
	                			fireEditingStopped();
	                		}
	            		}
                	});
                cancelButton = new JButton("Cancel");
                cancelButton.addActionListener(new ActionListener()
                	{
	                	@Override
	            		public void actionPerformed(ActionEvent e)
	            		{
	                		if(popup != null)
	                		{
	                			popup.hide();
	                			popup = null;
	                			fireEditingCanceled();
	                		}
	            		}
                	});
                JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 2, 2));
                buttonPanel.add(cancelButton);
                buttonPanel.add(okButton);
                buttonPanel.setBorder(new EmptyBorder(8, 0, 0, 0));
                StandardFormats.addGridBagElement(buttonPanel, layout, newLineConstr,  this);
                
                // set layout
                setBorder(new CompoundBorder(new LineBorder(Color.BLACK, 1), new EmptyBorder(5, 5, 5, 5)));
                
                refreshAbsolute();
                
                FocusListener focusListener = new FocusListener()
		            {
                		@Override
						public void focusGained(FocusEvent e)
						{
							// do nothing.
						}

						@Override
						public void focusLost(FocusEvent e)
						{
							Component focusObtainer = e.getOppositeComponent(); 
							if(focusObtainer == DevicePropertyChooser.this || focusObtainer == deviceField 
									|| focusObtainer == propertyField || focusObtainer == absoluteValueField
									|| focusObtainer == okButton || focusObtainer == cancelButton)
								return;
							if(popup != null)
	                		{
	                			popup.hide();
	                			popup = null;
	                			fireEditingCanceled();
	                		}
						}
		            	
		            };
		        addFocusListener(focusListener);
		        deviceField.addFocusListener(focusListener);
		        propertyField.addFocusListener(focusListener);
		        absoluteValueField.addFocusListener(focusListener);
		        okButton.addFocusListener(focusListener);
		        cancelButton.addFocusListener(focusListener);
                
                // Create popup.
                Point location = parent.getLocationOnScreen();
                popup = PopupFactory.getSharedInstance().getPopup(parent, this, location.x, location.y);
        		popup.show();
        		requestFocus();

        	}
        	private void refreshAbsolute()
        	{
        		Object currentDevice = deviceField.getSelectedItem();
        		Object currentProperty = propertyField.getSelectedItem();
        		if(currentProperty == null || currentDevice == null)
        			return;
        		DeviceSetting temp = new DeviceSetting();
        		temp.setDeviceProperty(currentDevice.toString(), currentProperty.toString());
        		if(canPropertyBeRelative(temp))
        		{
        			absoluteValueField.setEnabled(true);
        		}
        		else
        		{
        			absoluteValueField.setSelectedIndex(0);
        			absoluteValueField.setEnabled(false);
        		}
        	}
        	@Override
        	public Dimension getPreferredSize()
        	{
        		Dimension parentSize = super.getPreferredSize();
        		TableColumn col = multiPosSettingsTable.getColumnModel().getColumn(idx + 1);
                int colWidth = col.getWidth();
                if(parentSize.width < colWidth)
                	parentSize.width = colWidth;
        		return parentSize;
        	}
        }
         
        @Override
		public Component getTableCellRendererComponent(JTable table, Object object,
                boolean isSelected, boolean hasFocus, int row, int column)
        {
        	JLabel label;
        	if(row == 0)
        	{
        		if(column == 0)
        		{
        			label = new JLabel("No.");
        			label.setBackground(Color.LIGHT_GRAY);
        			label.setOpaque(true);
        			return label;
        		}
				return getHeaderButton(column - 1);
        	}
			if(column == 0)
			{
				label = new JLabel("#" + Integer.toString(row));
				if (isSelected)
			    {
			        label.setBackground(table.getSelectionBackground());
			        label.setForeground(table.getSelectionForeground());
			    }
				else
				{
					label.setBackground(Color.LIGHT_GRAY);
				}
				label.setOpaque(true);
				return label;
			}
			Object valueObject = settings.get(row - 1).get(column - 1);
			label = new JLabel(valueObject.toString());
			if (isSelected)
			{
			    label.setBackground(table.getSelectionBackground());
			    label.setForeground(table.getSelectionForeground());
			    label.setOpaque(true);
			}
			else
				label.setOpaque(false);
			return label;
        }
    }

    private class MultiPosDeviceTableModel extends AbstractTableModel
    {
        /**
         * Serial Version UID.
         */
        private static final long serialVersionUID = -4836706465571383139L;

        
        MultiPosDeviceTableModel()
        {
            // Do nothing.
        }

        @Override
		public String getColumnName(int col)
        {
            return "Column" + col;
        }

        @Override
		public int getRowCount()
        {
            return settings.size() + 1;
        }

        @Override
		public int getColumnCount()
        {
            return headers.size() + 1;
        }

        @Override
		public Class<?> getColumnClass(int column)
        {
            return DeviceSetting.class;
        }

        @Override
		public Object getValueAt(int row, int column)
        {
        	if(row == 0)
        	{
        		if(column == 0)
        		{
        			return "#";
        		}
				return headers.get(column - 1);
        	}
			if(column == 0)
			{
				return Integer.toString(row);
			}
			return settings.get(row - 1).get(column - 1);
        }

        @Override
		public boolean isCellEditable(int row, int column)
        {
        	if(column == 0)
    		{
        		return false;
    		}
			return true;
        }
    }

    private void loadDevices()
    {
        try
        {
        	Device[] deviceObjects = getServer().getMicroscope().getDevices();
        	devices = new String[deviceObjects.length];
        	for(int i=0; i<deviceObjects.length; i++)
        	{
        		devices[i] = deviceObjects[i].getDeviceID();
        	}
        } 
        catch (Exception e)
        {
        	sendErrorMessage("Could not obtain device names.", e);
            devices = new String[0];
        } 
    }

	@Override
	protected void commitChanges(DeviceSlidesJobConfiguration configuration) {
		DeviceSetting[][] multiSettings = new DeviceSetting[settings.size()][];
        for(int i = 0; i < multiSettings.length; i++)
        {
        	multiSettings[i] = new DeviceSetting[headers.size()];
        	for(int j = 0; j < multiSettings[i].length; j++)
        	{
        		DeviceSetting setting;
				setting = headers.get(j).clone();
        		setting.setValue(settings.get(i).get(j).toString());
        		multiSettings[i][j] = setting;
        	}
        }
        configuration.setMultiPosDeviceSettings(multiSettings);

        configuration.setJobs(jobPanel.getJobs());
	}

	@Override
	protected void initializeDefaultConfiguration(DeviceSlidesJobConfiguration configuration) throws AddonException {
		// do nothing.
	}
}
