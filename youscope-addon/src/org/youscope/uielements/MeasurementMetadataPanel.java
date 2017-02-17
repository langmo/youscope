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
package org.youscope.uielements;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.AbstractCellEditor;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.youscope.clientinterfaces.MetadataDefinition;
import org.youscope.clientinterfaces.MetadataDefinitionManager;
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
	private final MetadataDefinitionManager measurementMetadataProvider;
	private final PropertyProvider propertyProvider;
	private final ArrayList<MetadataProperty> properties = new ArrayList<>();
	private static final String PROPERTY_LAST_PREFIX = "YouScope.MeasurementProperties.Last.";
	
	private static final int DELETE_COLUMN_IDX = 0;
	private static final int NAME_COLUMN_IDX = 1;
	private static final int VALUE_COLUMN_IDX = 2;
	/**
	 * Constructor. Adds all default properties to the list. Same as {@code MeasurementMetadataPanel(youscopeClient, null)}.
	 * @param youscopeClient Reference to the YouScope client object.
	 * @throws IllegalArgumentException Thrown if youscopeClient is null.
	 */
	public MeasurementMetadataPanel(YouScopeClient youscopeClient) throws IllegalArgumentException
	{
		this(youscopeClient, null);
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
		
		
        PropertyTableEditor propertyEditor = new PropertyTableEditor();
        table.setDefaultRenderer(String.class, propertyEditor);
        table.setDefaultEditor(String.class, propertyEditor);
        DeleteTableEditor deleteEditor = new DeleteTableEditor();
        table.setDefaultRenderer(Boolean.class, deleteEditor);
        table.setDefaultEditor(Boolean.class, deleteEditor);
        table.setDragEnabled(false);
		table.setShowHorizontalLines(true);
		table.setShowVerticalLines(true);
		table.setShowGrid(false);
		table.setIntercellSpacing(new Dimension(8, 0));
		table.setTableHeader(null);
		table.setAutoCreateColumnsFromModel(true);
		table.setRowSelectionAllowed(false);
		table.setColumnSelectionAllowed(false);
		table.setSurrendersFocusOnKeystroke(true);
		table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		table.setFillsViewportHeight(true);
		tableModel.addTableModelListener(columnAdjuster);
		
        JScrollPane tableScrollPane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tableScrollPane.setPreferredSize(new Dimension(250, 100));
        tableScrollPane.setMinimumSize(new Dimension(100, 75));
        add(tableScrollPane, BorderLayout.CENTER);
        
        // set width of delete column
        TableColumn deleteColumn = table.getColumnModel().getColumn(DELETE_COLUMN_IDX);
        int deleteColumnWidth = deleteEditor.inactiveAllowed.getPreferredSize().width + table.getIntercellSpacing().width;
		deleteColumn.setPreferredWidth(deleteColumnWidth);
		deleteColumn.setMaxWidth(deleteColumnWidth);
		deleteColumn.setMinWidth(deleteColumnWidth);
        
        
        columnAdjuster.tableChanged(new TableModelEvent(tableModel));
	}
	
	private final TableModelListener columnAdjuster = new TableModelListener()
	{

		@Override
		public void tableChanged(final TableModelEvent event) 
		{
			Runnable runner = new Runnable()
			{
				@Override
				public void run() 
				{
					int column = event.getColumn();
					if(column == NAME_COLUMN_IDX || column < 0)
						adjustColumn(NAME_COLUMN_IDX);
				}
			};
			if(SwingUtilities.isEventDispatchThread())
				runner.run();
			else
				SwingUtilities.invokeLater(runner);
		}

		/*
		 *  Adjust the width of the specified column in the table
		 */
		private void adjustColumn(final int column)
		{
			TableColumn tableColumn = table.getColumnModel().getColumn(column);
			if (! tableColumn.getResizable()) 
				return;
			int width = 0;
			for (int row = 0; row < table.getRowCount(); row++)
			{
				TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
				Component c = table.prepareRenderer(cellRenderer, row, column);
				int dataWidth = c.getPreferredSize().width + table.getIntercellSpacing().width;
				width = Math.max(width, dataWidth);
			}

			tableColumn.setPreferredWidth(width);
			tableColumn.setMaxWidth(width);
			tableColumn.setMinWidth(width);
		}	
	};
	
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
		
		tableModel.fireTableDataChanged();
	}
	private boolean deleteProperty(int index)
	{
		if(index < 0 || index >= properties.size())
			return false;
		MetadataDefinition propertyDefinition = measurementMetadataProvider.getMetadataDefinition(properties.get(index).getName());
		if(propertyDefinition != null && propertyDefinition.getType() == MetadataDefinition.Type.MANDATORY)
			return false;
		properties.remove(index);
		return true;
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
	
	private final AbstractTableModel tableModel = new AbstractTableModel() 
    {
		/**
		 * Serial Version UID.
		 */
		private static final long serialVersionUID = 4091073520994701669L;
		@Override
        public String getColumnName(int col)
        {
        	if(col == DELETE_COLUMN_IDX)
        		return "Delete";
        	else if(col == NAME_COLUMN_IDX)
        		return "Property";
        	else if(col == VALUE_COLUMN_IDX)
        		return "Value";
        	return "";
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
            return 3;
        }

        @Override
        public Class<?> getColumnClass(int col)
        {
        	if(col == DELETE_COLUMN_IDX)
        		return Boolean.class;
        	return String.class;
        }

        @Override
        public Object getValueAt(int row, int col)
        {
        	if(row < properties.size())
        	{
        		if(col == DELETE_COLUMN_IDX)
        			return false;
        		else if(col == NAME_COLUMN_IDX)
        			return properties.get(row).getName();
        		else if(col == VALUE_COLUMN_IDX)
        			return properties.get(row).getValue();
        		return "";
        	}
        	// Last row = add properties.
        	if(col == DELETE_COLUMN_IDX)
    			return false;
    		else if(col == NAME_COLUMN_IDX)
    			return "<add new>";
    		else if(col == VALUE_COLUMN_IDX)
    			return "";
    		return "";
        }
        @Override
        public boolean isCellEditable(int row, int col)
        {
        	// Last row = add properties.
        	if(row >= properties.size())
        	{
        		if(col == NAME_COLUMN_IDX)
        			return true;
        		return false;
        	}
        	// Value always editable
        	else if(col == VALUE_COLUMN_IDX)
        		return true;
        	else if(col == NAME_COLUMN_IDX || col == DELETE_COLUMN_IDX)
        	{
	        	MetadataDefinition definition = measurementMetadataProvider.getMetadataDefinition(properties.get(row).getName());
	        	if(definition == null)
	        		return true;
				return definition.getType() != MetadataDefinition.Type.MANDATORY;
        	}
        	else
        		return false;
        }
        @Override
        public void setValueAt(Object rawValue, int row, int col) 
        {
        	if(rawValue == null)
        		return;
        	if(col == DELETE_COLUMN_IDX)
        	{
        		if(!(rawValue instanceof Boolean) || !((Boolean)rawValue).booleanValue())
        			return;
        		if(deleteProperty(row))
        		{
        			fireTableRowsDeleted(row, row);
        		}
        		return;
        	}
        	String value = rawValue.toString();
        	if(row >= properties.size())
        	{
        		// we want to add a value...
        		if(col != NAME_COLUMN_IDX)
        			return;
        		if(addProperty(value, null))
        			fireTableRowsInserted(properties.size(), properties.size());
        		return;
        	}
        	else if(col == VALUE_COLUMN_IDX)
			{
				// we want to change a value...
				addProperty(properties.get(row).getName(), value);
				fireTableCellUpdated(row, col);
				return;
			}
        	else if(col == NAME_COLUMN_IDX)
        	{
				// replace the old property by the new one, if allowed...
				if(setProperty(value, null, row))
				{
					fireTableRowsUpdated(row, row);
				}
				return;
        	}
		}
    };
    private final JTable table = new JTable(tableModel);
	
    
    private class DeleteTableEditor extends AbstractCellEditor implements TableCellEditor, TableCellRenderer
    {
    	/**
		 * Serial Version UID.
		 */
		private static final long serialVersionUID = 5717587328458546821L;
    	private final JLabel inactiveForbidden = new JLabel("");
    	private final JLabel activeForbidden = new JLabel("");
    	private final JButton inactiveAllowed;
    	private final JButton activeAllowed;
    	private boolean lastDecision = false;
    	public DeleteTableEditor() 
    	{
    		Icon deleteIcon = ImageLoadingTools.getResourceIcon("iconsShadowless/cross-script.png", "Delete");
    		if(deleteIcon != null)
    		{
    			inactiveAllowed = new JButton(deleteIcon);
    			activeAllowed = new JButton(deleteIcon);
    		}
    		else
    		{
    			inactiveAllowed = new JButton("X");
    			activeAllowed = new JButton("X");
    		}
    		inactiveAllowed.setBorderPainted(false);
    		activeAllowed.setBorderPainted(false);
    		inactiveAllowed.setBorder(null);
    		activeAllowed.setBorder(null);
    		inactiveAllowed.setOpaque(false);
    		activeAllowed.setOpaque(false);
    		inactiveAllowed.setContentAreaFilled(false);
    		activeAllowed.setContentAreaFilled(false);
    		
    		JTextField colorModel = new JTextField();
    		activeAllowed.setBackground(colorModel.getBackground());
    		inactiveAllowed.setBackground(colorModel.getBackground());
    		activeAllowed.setForeground(colorModel.getForeground());
    		inactiveAllowed.setForeground(colorModel.getForeground());
    		inactiveForbidden.setBackground(colorModel.getBackground());
    		activeForbidden.setBackground(colorModel.getBackground());
    		
    		Dimension dim1 = inactiveForbidden.getPreferredSize();
    		Dimension dim2 = activeAllowed.getPreferredSize();
    		Dimension dim = new Dimension(Math.max(dim1.width,  dim2.width), Math.max(dim1.height,  dim2.height));
    		inactiveForbidden.setPreferredSize(dim);
    		inactiveForbidden.setMaximumSize(dim);
    		inactiveForbidden.setMinimumSize(dim);
    		activeForbidden.setPreferredSize(dim);
    		activeForbidden.setMaximumSize(dim);
    		activeForbidden.setMinimumSize(dim);
    		inactiveAllowed.setPreferredSize(dim);
    		inactiveAllowed.setMaximumSize(dim);
    		inactiveAllowed.setMinimumSize(dim);
    		activeAllowed.setPreferredSize(dim);
    		activeAllowed.setMaximumSize(dim);
    		activeAllowed.setMinimumSize(dim);
    		
    		activeAllowed.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					lastDecision = true;
					fireEditingStopped();
				}
			});
		}
		@Override
		public Object getCellEditorValue() 
		{
			return lastDecision;
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col)
		{
			lastDecision = false;
			activeAllowed.setSelected(false);
			if(tableModel.isCellEditable(row, col))
				return activeAllowed;
			return activeForbidden;
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col)
		{
			if(tableModel.isCellEditable(row, col))
				return inactiveAllowed;
			return inactiveForbidden;
		}
    }
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
		private final Font plainFont;
		private final Font boldFont;
		PropertyTableEditor()
		{
			plainFont = viewLabel.getFont();
			boldFont = plainFont.deriveFont(Font.BOLD);
			viewLabel.setOpaque(true);
			viewLabel.setBackground(editTextField.getBackground());
			viewLabel.setForeground(editTextField.getForeground());
			editComboBox.addActionListener(new ActionListener() 
			{
				@Override
				public void actionPerformed(ActionEvent e) {
					fireEditingStopped();
				}
			});
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
			else if(col == NAME_COLUMN_IDX)
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
			else if(col == VALUE_COLUMN_IDX)
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
			else
			{
				editTextField.setText("");
				lastEditor = editTextField;
				return editTextField;
			}
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col)
		{
			String text = value == null ? "" : value.toString();
			if(text.isEmpty() && col == VALUE_COLUMN_IDX && row < properties.size())
				text = "<enter value>";
			else if(col == NAME_COLUMN_IDX && row < properties.size()) 
				text+=":";
			viewLabel.setText(text);
		    if(row >= properties.size())
		    	viewLabel.setFont(plainFont);
		    else if(col == NAME_COLUMN_IDX) 
		    {
		    	MetadataDefinition definition = measurementMetadataProvider.getMetadataDefinition(properties.get(row).getName());
		    	if(definition == null || definition.getType() != MetadataDefinition.Type.MANDATORY)
		    		viewLabel.setFont(plainFont);
		    	else
		    		viewLabel.setFont(boldFont);
		    }
		    else
		    	viewLabel.setFont(plainFont);
		    return viewLabel;
		}
	}

}
