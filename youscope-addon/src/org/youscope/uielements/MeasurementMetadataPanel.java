package org.youscope.uielements;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.youscope.clientinterfaces.MetadataDefinition;
import org.youscope.clientinterfaces.MetadataDefinitionProvider;
import org.youscope.clientinterfaces.PropertyProvider;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.MetadataProperty;
import org.youscope.common.measurement.MeasurementConfiguration;

/**
 * A panel to define and edit the metadata of a {@link MeasurementConfiguration}.
 * @author Moritz Lang
 *
 */
public class MeasurementMetadataPanel extends JPanel 
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 1608172021539503499L;
	private final MetadataDefinitionProvider measurementMetadataProvider;
	private final PropertyProvider propertyProvider;
	private final ArrayList<MetadataProperty> properties = new ArrayList<>();
	private static final String PROPERTY_LAST_PREFIX = "YouScope.MeasurementProperties.Last.";
	/**
	 * Constructor. Adds all default properties to the list. Same as {@code MeasurementMetadataPanel(youscopeClient, null)}.
	 * @param youscopeClient Reference to the YouScope client object.
	 * @throws IllegalArgumentException Thrown if youscopeClient is null.
	 */
	public MeasurementMetadataPanel(YouScopeClient youscopeClient) throws IllegalArgumentException
	{
		super(new BorderLayout());
		if(youscopeClient == null)
			throw new IllegalArgumentException();
		measurementMetadataProvider = youscopeClient.getMeasurementMetadataProvider();
		propertyProvider = youscopeClient.getPropertyProvider();
		
		// Initialize with default properties
		setMetadataProperties(null);
		
		JTable table = new JTable(propertyTableModel);
		table.setAutoCreateColumnsFromModel(true);
		table.setRowSelectionAllowed(true);
		table.setColumnSelectionAllowed(false);
		table.setSurrendersFocusOnKeystroke(true);
		table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        PropertyTableEditor editor = new PropertyTableEditor();
        table.setDefaultRenderer(String.class, editor);
        table.setDefaultEditor(String.class, editor);
        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.setPreferredSize(new Dimension(250, 150));
        tableScrollPane.setMinimumSize(new Dimension(10, 10));
        add(tableScrollPane, BorderLayout.CENTER);
	}
	
	/**
	 * Constructor. Adds all provided properties to the list of defined properties, besides the mandatory properties (see {@link #setMetadataProperties(Collection)}).
	 * @param youscopeClient Reference to the YouScope client object.
	 * @param properties Properties which should be added, or null to add all default properties.
	 * @throws IllegalArgumentException Thrown if youscopeClient is null.
	 */
	public MeasurementMetadataPanel(YouScopeClient youscopeClient, Collection<MetadataProperty> properties) throws IllegalArgumentException
	{
		super(new BorderLayout());
		if(youscopeClient == null)
			throw new IllegalArgumentException();
		measurementMetadataProvider = youscopeClient.getMeasurementMetadataProvider();
		propertyProvider = youscopeClient.getPropertyProvider();
		
		// Initialize with default properties
		setMetadataProperties(properties);
		
		JTable table = new JTable(propertyTableModel);
		table.setAutoCreateColumnsFromModel(true);
		table.setRowSelectionAllowed(true);
		table.setColumnSelectionAllowed(false);
		table.setSurrendersFocusOnKeystroke(true);
		table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        PropertyTableEditor editor = new PropertyTableEditor();
        table.setDefaultRenderer(String.class, editor);
        table.setDefaultEditor(String.class, editor);
        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.setPreferredSize(new Dimension(250, 150));
        tableScrollPane.setMinimumSize(new Dimension(10, 10));
        add(tableScrollPane, BorderLayout.CENTER);
	}
	
	/**
	 * Returns all defined metadata properties.
	 * @return Defined properties.
	 */
	public Collection<MetadataProperty> getMetadataProperties()
	{
		return new ArrayList<MetadataProperty>(properties);
	}
	/**
	 * Sets the displayed and editable properties. All previous properties are deleted.
	 * Mandatory properties (i.e. properties for which {@link MetadataDefinition#getType()} returns {@link org.youscope.clientinterfaces.MetadataDefinition.Type#MANDATORY})
	 * are always added. If properties are null, also default properties (i.e. properties for which {@link MetadataDefinition#getType()} returns {@link org.youscope.clientinterfaces.MetadataDefinition.Type#DEFAULT}) are added.
	 * @param properties Properties which should be displayed, or null to display default properties.
	 */
	public void setMetadataProperties(Collection<MetadataProperty> properties)
	{
		this.properties.clear();
		// Add default or mandatory properties.
		Collection<MetadataDefinition> defaultProperties;
		if(properties == null)
			defaultProperties = measurementMetadataProvider.getDefaultMetadataDefinitions();
		else
			defaultProperties = measurementMetadataProvider.getMandatoryMetadataDefinitions();
		for(MetadataDefinition defaultProperty : defaultProperties)
		{
			addProperty(defaultProperty.getName(), null);
		}
		
		// Add provided properties
		if(properties != null)
		{
			for(MetadataProperty property : properties)
			{
				addProperty(property.getName(), property.getValue());
			}
		}
		
		propertyTableModel.fireTableDataChanged();
	}
	private boolean addProperty(String propertyName, String propertyValue)
	{
		return setProperty(propertyName, propertyValue, -1);
	}
	private boolean setProperty(String propertyName, String propertyValue, int index)
	{
		if(propertyName == null || propertyName.isEmpty())
			return false;
		
		// find out if property with same name already exists...
		int lastIndex = -1;
		for(int i=0; i<properties.size(); i++)
		{
			if(properties.get(i).getName().equals(propertyName))
			{
				lastIndex = i;
				break;
			}
		}
		if(index >= 0 && lastIndex >= 0)
		{
			// we want to replace a given element with an element which does already exist. Skip!
			return false;
		}
		else if(index >= 0)
		{
			// we want to replace a property. Check if we are actually allowed to do so.
			MetadataDefinition oldPropertyDefinition = measurementMetadataProvider.getMetadataDefinition(properties.get(index).getName());
			if(oldPropertyDefinition != null && oldPropertyDefinition.getType() == MetadataDefinition.Type.MANDATORY)
				return false;
		}
		// if the value is null and we already have that property in the list, just keep the old one.
		if(propertyValue == null && lastIndex>=0)
			return false;
		// if the value is null but the property is not yet in the list, try to get the last used value in any measurement.
		if(propertyValue == null)
			propertyValue = getLastPropertyValue(propertyName);
		
		// get the definition for this property		
		MetadataDefinition propertyDefinition = measurementMetadataProvider.getMetadataDefinition(propertyName);
		if(propertyDefinition == null)
		{
			// no definition means custom property. Check if allowed...
			if(!measurementMetadataProvider.isAllowCustomMetadata())
				return false;
		}
		else 
		{
			// when we are still null, take first value
			if(propertyValue == null)
			{
				String[] allowedValues = propertyDefinition.getKnownValues();
				if(allowedValues.length > 0)
					propertyValue = allowedValues[0];
			}
			
			if(!propertyDefinition.isCustomValuesAllowed())
			{
				// OK, we must check if value is in agreement with allowed values.
				boolean allowed = false;
				if(propertyValue != null)
				{
					for(String allowedValue : propertyDefinition.getKnownValues())
					{
						if(propertyValue.equals(allowedValue))
						{
							allowed = true;
							break;
						}
					}
				}
				if(!allowed)
				{
					// if we have an old allowed value already in the list, use the old one...
					if(lastIndex >= 0)
						return false;
					// Since no custom values are allowed, we can be sure that at least one value is known...
					propertyValue = propertyDefinition.getKnownValues()[0];
				}
			}
		}
		if(index >= 0)
			properties.set(index, new MetadataProperty(propertyName, propertyValue == null ? "" : propertyValue));
		else if(lastIndex >= 0)
			properties.set(lastIndex, new MetadataProperty(propertyName, propertyValue == null ? "" : propertyValue));
		else
			properties.add(new MetadataProperty(propertyName, propertyValue == null ? "" : propertyValue));
		if(propertyValue != null && !propertyValue.isEmpty())
			setLastPropertyValue(propertyName, propertyValue);	
		return true;
	}
	
	private String getLastPropertyValue(String propertyName)
	{
		return propertyProvider.getProperty(PROPERTY_LAST_PREFIX+propertyName, (String)null);
	}
	private void setLastPropertyValue(String propertyName, String propertyValue)
	{
		propertyProvider.setProperty(PROPERTY_LAST_PREFIX+propertyName, propertyValue);
	}
	
	private final AbstractTableModel propertyTableModel = new AbstractTableModel() 
    {
        /**
		 * Serial Version UID.
		 */
		private static final long serialVersionUID = 4091073520994701669L;
		@Override
        public String getColumnName(int col)
        {
        	if(col == 0)
        		return "Property";
			return "Value";
        	
        }

        @Override
        public int getRowCount()
        {
        	if(measurementMetadataProvider.isAllowCustomMetadata() || measurementMetadataProvider.getNumMetadataDefinitions()>properties.size())
        	{
        		// last row is add property row.
        		return properties.size()+1;
        	}
			return properties.size();
        }

        @Override
        public int getColumnCount()
        {
            return 2;
        }

        @Override
        public Class<?> getColumnClass(int column)
        {
        	return String.class;
        }

        @Override
        public Object getValueAt(int row, int col)
        {
        	if(row < properties.size())
        	{
        		if(col == 0)
        			return properties.get(row).getName();
				return properties.get(row).getValue();
        	}
        	// Last row = add properties.
			if(col == 0)
				return "(+) Add Metadata";
			return "";
        }
        @Override
        public boolean isCellEditable(int row, int col)
        {
        	// Last row = add properties.
        	if(row >= properties.size())
        	{
        		if(col == 0)
        			return true;
        		return false;
        	}
        	// Value always editable
        	if(col == 1)
        		return true;
        	MetadataDefinition definition = measurementMetadataProvider.getMetadataDefinition(properties.get(row).getName());
        	if(definition == null)
        		return true;
			return definition.getType() != MetadataDefinition.Type.MANDATORY;
        }
        @Override
        public void setValueAt(Object rawValue, int row, int col) 
        {
        	if(rawValue == null)
        		return;
        	String value = rawValue.toString();
        	if(row >= properties.size())
        	{
        		// we want to add a value...
        		if(col != 0)
        			return;
        		if(addProperty(value, null))
        			fireTableRowsInserted(properties.size(), properties.size());
        		return;
        	}
			if(col == 1)
			{
				// we want to change a value...
				addProperty(properties.get(row).getName(), value);
				fireTableCellUpdated(row, col);
				return;
			}
			// replace the old property by the new one, if allowed...
			if(setProperty(value, null, row))
			{
				fireTableRowsUpdated(row, row);
			}
		}
    };
	
	private class PropertyTableEditor extends AbstractCellEditor implements TableCellEditor, TableCellRenderer
	{
		
		/**
		 * Serial Version UID.
		 */
		private static final long serialVersionUID = -7924080179258623862L;
		private final JComboBox<String> editComboBox = new JComboBox<String>();
		private final JTextField editTextField = new JTextField();
		private final JLabel viewLabel = new JLabel();
		private Object lastEditor = null;
		PropertyTableEditor()
		{
			viewLabel.setOpaque(true);
			editComboBox.addActionListener(new ActionListener() 
			{
				@Override
				public void actionPerformed(ActionEvent e) {
					fireEditingStopped();
				}
			});
			editTextField.setBorder(new EmptyBorder(0, 0, 0, 0));
		}
		@Override
		public Object getCellEditorValue()
		{
			if(lastEditor == null)
				return null;
			else if(lastEditor == editTextField)
			{
				return editTextField.getText();
			}
			else if(lastEditor == editComboBox)
			{
				return editComboBox.getSelectedItem().toString();
			}
			else
				return null;
		}
		
		// @Override
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col)
		{
			// check if new property
			if(row >= properties.size())
			{
				// get all defined properties not yet set
				HashSet<String> availableProperties = new HashSet<>();
				for(MetadataDefinition property : measurementMetadataProvider.getMetadataDefinitions())
				{
					availableProperties.add(property.getName());
				}
				for(MetadataProperty property : properties)
				{
					availableProperties.remove(property.getName());
				}
				if(availableProperties.size() > 0)
				{
					editComboBox.removeAllItems();
					for(String propertyName : availableProperties)
					{
						editComboBox.addItem(propertyName);
					}
					if(measurementMetadataProvider.isAllowCustomMetadata())
					{
						editComboBox.setEditable(true);
						editComboBox.setSelectedItem("");
					}
					else
					{
						editComboBox.setEditable(false);
					}
					lastEditor = editComboBox;
					return editComboBox;
				}
				editTextField.setText("");
				lastEditor = editTextField;
				return editTextField;
			}
			else if(col == 0)
			{
				// we want to change a property to another...
				// get all defined properties not yet set
				HashSet<String> availableProperties = new HashSet<>();
				for(MetadataDefinition property : measurementMetadataProvider.getMetadataDefinitions())
				{
					availableProperties.add(property.getName());
				}
				for(MetadataProperty property : properties)
				{
					availableProperties.remove(property.getName());
				}
				editComboBox.removeAllItems();
				editComboBox.addItem(properties.get(row).getName());
				for(String propertyName : availableProperties)
				{
					editComboBox.addItem(propertyName);
				}
				editComboBox.setSelectedItem(properties.get(row).getName());
				editComboBox.setEditable(measurementMetadataProvider.isAllowCustomMetadata());
				lastEditor = editComboBox;
				return editComboBox;
			}
			else
			{
				// we want to change a value
				MetadataDefinition definition = measurementMetadataProvider.getMetadataDefinition(properties.get(row).getName());
				if(definition == null)
				{
					editTextField.setText(properties.get(row).getValue());
					lastEditor = editTextField;
					return editTextField;
				}
				String[] knownValues = definition.getKnownValues();
				if(knownValues.length > 0)
				{
					editComboBox.removeAllItems();
					for(String propertyValue : knownValues)
					{
						editComboBox.addItem(propertyValue);
					}
					editComboBox.setEditable(definition.isCustomValuesAllowed());
					editComboBox.setSelectedItem(properties.get(row).getValue());
					lastEditor = editComboBox;
					return editComboBox;
				}
				editTextField.setText(properties.get(row).getValue());
				lastEditor = editTextField;
				return editTextField;
			}
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value,
		        boolean isSelected, boolean hasFocus, int row, int col)
		{
			String text = value == null ? "" : value.toString();
			viewLabel.setText(text);
		    if(row >= properties.size() || col>0)
		    	viewLabel.setEnabled(true);
		    else 
		    {
		    	MetadataDefinition definition = measurementMetadataProvider.getMetadataDefinition(properties.get(row).getName());
		    	if(definition == null)
		    	{
		    		viewLabel.setEnabled(true);
		    	}
		    	else
		    		viewLabel.setEnabled(definition.getType() != MetadataDefinition.Type.MANDATORY);
		    }
		    return viewLabel;
		}
	}

}
