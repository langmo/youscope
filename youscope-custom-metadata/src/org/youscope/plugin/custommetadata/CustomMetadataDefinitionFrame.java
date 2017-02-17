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
package org.youscope.plugin.custommetadata;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.AbstractCellEditor;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.youscope.clientinterfaces.MetadataDefinition;
import org.youscope.clientinterfaces.MetadataDefinition.Type;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeClientException;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.ImageLoadingTools;


/**
 * @author Moritz Lang
 *
 */
class CustomMetadataDefinitionFrame
{
	private final YouScopeFrame									frame;
	
	private final JTextField nameField 									= new JTextField("");
	private final ArrayList<String> values = new ArrayList<>();
	private final ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();
	private final JComboBox<MetadataDefinition.Type> typeField = new JComboBox<>(MetadataDefinition.Type.values());
	private final JCheckBox customValuesField = new JCheckBox("Allow custom values.", true);
	
	CustomMetadataDefinitionFrame(YouScopeClient client, YouScopeFrame frame)
	{
		this(client, frame, null);
	}
	CustomMetadataDefinitionFrame(final YouScopeClient client, YouScopeFrame frame, final MetadataDefinition oldMetadata)
	{
		this.frame = frame;
		
		frame.setTitle("Metadata Definition");
		frame.setResizable(true);
		frame.setClosable(true);
		frame.setMaximizable(false);
		
		ValueTableEditor valueEditor = new ValueTableEditor();
        table.setDefaultRenderer(String.class, valueEditor);
        table.setDefaultEditor(String.class, valueEditor);
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
		
        JScrollPane tableScrollPane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tableScrollPane.setPreferredSize(new Dimension(250, 100));
        tableScrollPane.setMinimumSize(new Dimension(100, 75));
        
        // set width of delete column
        TableColumn deleteColumn = table.getColumnModel().getColumn(0);
        int deleteColumnWidth = deleteEditor.inactiveAllowed.getPreferredSize().width + table.getIntercellSpacing().width;
		deleteColumn.setPreferredWidth(deleteColumnWidth);
		deleteColumn.setMaxWidth(deleteColumnWidth);
		deleteColumn.setMinWidth(deleteColumnWidth);
		
		DynamicPanel elementsPanel = new DynamicPanel();
		elementsPanel.add(new JLabel("Name:"));
		elementsPanel.add(nameField);
		elementsPanel.add(new JLabel("Type:"));
		elementsPanel.add(typeField);
		typeField.setEditable(false);
		elementsPanel.add(new JLabel("Custom values:"));
		elementsPanel.add(customValuesField);
		elementsPanel.add(new JLabel("Predefined values:"));
		elementsPanel.addFill(tableScrollPane);
		
		JButton closeButton = new JButton("Save");
		closeButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	String name = nameField.getText();
                	if(name.length()<1)
                	{
                		JOptionPane.showMessageDialog(null, "Name is empty.", "Invalid setting", JOptionPane.INFORMATION_MESSAGE);
                		return;
                	}
                	boolean customValuesAllowed = customValuesField.isSelected();
                	MetadataDefinition.Type type = (Type) typeField.getSelectedItem();
                	
                	MetadataDefinition newMetadata = new MetadataDefinition(name, type, customValuesAllowed, values.toArray(new String[values.size()]));
                	
                	// we first delete the old definition
                	if(oldMetadata != null)
                	{
                		try {
							client.getMeasurementMetadataProvider().deleteMetadataDefinition(oldMetadata.getName());
						} catch (YouScopeClientException e1) {
							client.sendError("Could not delete old metadata " + oldMetadata.getName()+".", e1);
						}
                	}
                	try 
                	{
						client.getMeasurementMetadataProvider().setMetadataDefinition(newMetadata);
					} 
                	catch (YouScopeClientException e1) {
                		client.sendError("Could not save new metadata " + newMetadata.getName()+".", e1);
                		return;
					}
                	
                    CustomMetadataDefinitionFrame.this.frame.setVisible(false); 
                    for(ActionListener listener : listeners)
                    {
                    	listener.actionPerformed(new ActionEvent(this, 154, "Metadata definition created or edited."));
                    }
                }
            });
		if(oldMetadata != null)
		{
			nameField.setText(oldMetadata.getName());
			typeField.setSelectedItem(oldMetadata.getType());
			customValuesField.setSelected(oldMetadata.isCustomValuesAllowed());
			for(String value : oldMetadata.getKnownValues())
			{
				values.add(value);
			}
		}
        
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(elementsPanel, BorderLayout.CENTER);
        contentPane.add(closeButton, BorderLayout.SOUTH);
        frame.setContentPane(contentPane);
        frame.pack();
	}
	
	public void addActionListener(ActionListener listener)
	{
		listeners.add(listener);
	}
	public void removeActionListener(ActionListener listener)
	{
		listeners.remove(listener);
	}
	private final AbstractTableModel tableModel = new AbstractTableModel()
	{

		/**
		 * Serial Version UID.
		 */
		private static final long serialVersionUID = -2692829731315596399L;

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public int getRowCount() {
			return values.size()+1;
		}

		@Override
        public String getColumnName(int col)
        {
        	if(col == 0)
        		return "Delete";
        	else if(col == 1)
        		return "Value";
        	return "";
        }
		
		@Override
        public Class<?> getColumnClass(int col)
        {
        	if(col == 0)
        		return Boolean.class;
        	return String.class;
        }
		@Override
        public boolean isCellEditable(int row, int col)
        {
			if(row >= values.size() && col == 0)
				return false;
			return true;
        }
		@Override
		public Object getValueAt(int row, int col) 
		{
			if(col == 0)
				return false;
			else if(row < values.size())
				return values.get(row);
			return "<add value>";						
		}
		@Override
		public void setValueAt(Object rawValue, int row, int col)
		{
			if(rawValue == null)
				return;
			else if(col == 0 && row < values.size())
			{
				if(!(rawValue instanceof Boolean)|| !((Boolean)rawValue).booleanValue())
					return;
				values.remove(row);
				fireTableRowsDeleted(row, row);
				return;
			}
			String value = rawValue.toString();
			if(row < values.size())
			{
				values.set(row, value);
				fireTableCellUpdated(row, col);
			}
			else
			{
				values.add(value);
				fireTableRowsInserted(row, row);
			}
		}

	};
	private final JTable table = new JTable(tableModel);
	private class DeleteTableEditor extends AbstractCellEditor implements TableCellEditor, TableCellRenderer
    {
    	/**
		 * Serial Version UID.
		 */
		private static final long serialVersionUID = 5717587328458546822L;
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
	private class ValueTableEditor extends AbstractCellEditor implements TableCellEditor, TableCellRenderer
	{
		/**
		 * Serial Version UID.
		 */
		private static final long serialVersionUID = -7924080179158623862L;
		private final JTextField editTextField = new JTextField();
		private final JLabel viewLabel = new JLabel();
		ValueTableEditor()
		{
			viewLabel.setOpaque(true);
			viewLabel.setBackground(editTextField.getBackground());
			viewLabel.setForeground(editTextField.getForeground());
		}
		@Override
		public Object getCellEditorValue()
		{
			return editTextField.getText();
		}
		
		// @Override
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col)
		{
			// check if new property
			if(row >= values.size())
			{
				editTextField.setText("");
				return editTextField;
			}
			editTextField.setText(values.get(row));
			return editTextField;
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col)
		{
			String text = value == null ? "" : value.toString();
			if(text.isEmpty() && row < values.size())
				text = "<enter value>";
			viewLabel.setText(text);
		    return viewLabel;
		}
	}
}
