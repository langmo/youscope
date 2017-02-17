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
package org.youscope.plugin.microplate.measurement;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.swing.AbstractCellEditor;
import javax.swing.ComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListDataListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.PositionInformation;
import org.youscope.common.Well;
import org.youscope.uielements.DoubleTextField;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.ImageLoadingTools;

/**
 * Table to show and set configured positions.
 * @author Moritz Lang
 *
 */
public class PathTable extends JPanel
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 6131526938094793770L;
	private final JTable pathTable;
	private final PathTableModel pathTableModel;
	private final ArrayList<PositionInformation> positionInformations;
	private final HashMap<PositionInformation, XYAndFocusPosition> configuredPositions;
	private final ArrayList<ActionListener> layoutChangedListeners = new ArrayList<>(1);
	private final YouScopeFrame frame;
	private final Column[] columns;
	/**
	 * Columns in the path table.
	 * @author Moritz Lang
	 *
	 */
	public enum Column
	{
		/**
		 * Position of a custom positions.
		 */
		MAIN_POSITION("Position", String.class, false),
		/**
		 * Well
		 */
		WELL("Well", String.class, false),
		/**
		 * Tile
		 */
		TILE("Tile", String.class, false),
		/**
		 * X-position in um
		 */
		X("X-Position (um)", Double.class, true),
		/**
		 * y-position in um.
		 */
		Y("Y-Position (um)", Double.class, true),
		/**
		 * focus in um.
		 */
		FOCUS("Focus (um)", Double.class, true);
		private final String name;
		private final Class<?> type;
		private final boolean editable;
		Column(String name, Class<?> type, boolean editable)
		{
			this.name = name;
			this.type = type;
			this.editable = editable;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
		/**
		 * Returns true if the column can be edited.
		 * @return true if editable
		 */
		public boolean isEdiable()
		{
			return editable;
		}
		/**
		 * Returns the class of content stored in this column.
		 * @return content type.
		 */
		public Class<?> getType()
		{
			return type;
		}
	}
	/**
	 * Constructor.
	 * @param configuredPositions Hash map of already configured positions. Can be empty.
	 * @param frame The containing frame.
	 * @param columns The columns which should show up in the table.
	 */
	public PathTable(HashMap<PositionInformation, XYAndFocusPosition> configuredPositions, YouScopeFrame frame, Column... columns)
	{
		super(new BorderLayout(0, 0));
		this.columns = columns;
		this.frame = frame;
		this.configuredPositions = configuredPositions;
		positionInformations = new ArrayList<>(configuredPositions.keySet());
		Collections.sort(positionInformations);
		this.pathTableModel = new PathTableModel();
		this.pathTable = new JTable(pathTableModel);
		pathTable.setAutoCreateColumnsFromModel(true);
		pathTable.setRowSelectionAllowed(true);
		pathTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pathTable.setColumnSelectionAllowed(false);
        pathTable.setSurrendersFocusOnKeystroke(true);
        pathTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        pathTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        pathTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        PathTableEditor editor = new PathTableEditor();
        pathTable.setDefaultRenderer(String.class, editor);
        pathTable.setDefaultRenderer(Double.class, editor);
        pathTable.setDefaultEditor(Double.class, editor);
        JScrollPane pathTableScrollPane = new JScrollPane(pathTable);
        pathTableScrollPane.setPreferredSize(new Dimension(250, 70));
        pathTableScrollPane.setMinimumSize(new Dimension(10, 10));
        add(pathTableScrollPane, BorderLayout.CENTER);
        
        // Up, down, add and remove Buttons
        Icon addButtonIcon = ImageLoadingTools.getResourceIcon("icons/block--plus.png", "Add Position");
        Icon deleteButtonIcon = ImageLoadingTools.getResourceIcon("icons/block--minus.png", "Remove Position");
        JButton newPositionButton;
        if (addButtonIcon == null)
            newPositionButton = new JButton("Add Position");
        else
            newPositionButton = new JButton("Add Position", addButtonIcon);
        newPositionButton.setHorizontalAlignment(SwingConstants.LEFT);
        newPositionButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	addPosition();
                }
            });
        JButton deletePositionButton;
        if (deleteButtonIcon == null)
            deletePositionButton = new JButton("Delete Position");
        else
            deletePositionButton = new JButton("Delete Position", deleteButtonIcon);
        deletePositionButton.setHorizontalAlignment(SwingConstants.LEFT);
        deletePositionButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	deletePosition();
                }
            });        
        DynamicPanel buttonPanel = new DynamicPanel();
        buttonPanel.add(newPositionButton);
        buttonPanel.add(deletePositionButton);
        buttonPanel.addFillEmpty();
        buttonPanel.setOpaque(true);
        add(buttonPanel, BorderLayout.EAST);
	}
	private class AddPositionComponent extends DynamicPanel
	{
		/**
		 * Serial Version UID.
		 */
		private static final long serialVersionUID = -6550278275596583426L;
		private JComboBox<Integer> mainPositionComboBox = new JComboBox<>(new XComboBoxModel());
		private JComboBox<String> wellYComboBox = new JComboBox<>(new YComboBoxModel());
		private JComboBox<Integer> wellXComboBox = new JComboBox<>(new XComboBoxModel());
		private JComboBox<String> tileYComboBox = new JComboBox<>(new YComboBoxModel());
		private JComboBox<Integer> tileXComboBox = new JComboBox<>(new XComboBoxModel());
		private DoubleTextField xPosTextField = new DoubleTextField();
		private DoubleTextField yPosTextField = new DoubleTextField();
		private DoubleTextField focusTextField = new DoubleTextField();
		private static final int NUM_POS_OPTIONS = 999;
		private final YouScopeFrame frame;
		AddPositionComponent(YouScopeFrame frame)
		{
			this.frame = frame;
			for(Column column : columns)
			{
				add(new JLabel(column.toString()+":"));
				switch(column)
				{
				case MAIN_POSITION:
					add(mainPositionComboBox);
					break;
				case WELL:
					JPanel wellPanel = new JPanel(new FlowLayout());
					wellPanel.setOpaque(false);
					wellPanel.add(wellYComboBox);
					wellPanel.add(wellXComboBox);
					add(wellPanel);
					break;
				case TILE:
					JPanel tilePanel = new JPanel(new FlowLayout());
					tilePanel.setOpaque(false);
					tilePanel.add(tileYComboBox);
					tilePanel.add(tileXComboBox);
					add(tilePanel);
					break;
				case X:
					add(xPosTextField);
					break;
				case Y:
					add(yPosTextField);
					break;
				case FOCUS:
					add(focusTextField);
					break;
				default:
					break;	
				}
			}
			JButton addButton = new JButton("Add Position");
			addButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					addPosition();
				}
			});
			add(addButton);
		}
		void addPosition()
		{
			PositionInformation positionInformation = getPositionInformation();
			boolean alreadyExists = false;
			if(positionInformations.contains(positionInformation))
			{
				if(JOptionPane.showConfirmDialog(this, "Position " + positionInformation.toString()+" already exists. Overwrite?", "Position Already Exists", JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)
					return;
				alreadyExists = true;
			}
			double x = xPosTextField.getValue();
			double y = yPosTextField.getValue();
			double focus = Double.NaN;
			for(Column column : columns)
			{
				if(column == Column.FOCUS)
				{
					focus = focusTextField.getValue();
					break;
				}
			}
			configuredPositions.put(positionInformation, new XYAndFocusPosition(x, y, focus));
			if(!alreadyExists)
			{
				positionInformations.add(positionInformation);
				Collections.sort(positionInformations);
			}
			
			int row = Collections.binarySearch(positionInformations, positionInformation);
			if(row>= 0)
			{
				pathTableModel.fireTableRowsInserted(row, row);
				pathTable.setRowSelectionInterval(row, row);
				pathTable.scrollRectToVisible(new Rectangle(pathTable.getCellRect(row, 0, true)));
			}
			notifyLayoutChanged();
			
			frame.setVisible(false);
		}
		PositionInformation getPositionInformation()
		{
			for(Column column : columns)
			{
				if(column == Column.MAIN_POSITION)
				{
					return new PositionInformation(PositionInformation.POSITION_TYPE_MAIN_POSITION, mainPositionComboBox.getSelectedIndex());
				}
			}
			PositionInformation positionInformation = new PositionInformation(new Well(wellYComboBox.getSelectedIndex(), wellXComboBox.getSelectedIndex()));
			for(Column column : columns)
			{
				if(column == Column.TILE)
				{
					positionInformation = new PositionInformation(positionInformation, PositionInformation.POSITION_TYPE_YTILE, tileYComboBox.getSelectedIndex());
					positionInformation = new PositionInformation(positionInformation, PositionInformation.POSITION_TYPE_XTILE, tileXComboBox.getSelectedIndex());
					break;
				}
			}
			return positionInformation;
		}
		private class YComboBoxModel implements ComboBoxModel<String>
		{
			private Object selectedItem = Well.getYWellName(0);
			@Override
			public void addListDataListener(ListDataListener arg0) {
				// not needed, no changes possible.
			}

			@Override
			public String getElementAt(int arg0) {
				return Well.getYWellName(arg0);
			}

			@Override
			public int getSize() {
				return NUM_POS_OPTIONS;
			}

			@Override
			public void removeListDataListener(ListDataListener arg0) {
				// not needed, no changes possible.
			}

			@Override
			public Object getSelectedItem() {
				return selectedItem;
			}

			@Override
			public void setSelectedItem(Object anItem) {
				selectedItem = anItem;				
			}
		}
		private class XComboBoxModel implements ComboBoxModel<Integer>
		{
			private Object selectedItem = 1;
			@Override
			public void addListDataListener(ListDataListener arg0) {
				// not needed, no changes possible.
			}

			@Override
			public Integer getElementAt(int arg0) {
				return arg0+1;
			}

			@Override
			public int getSize() {
				return NUM_POS_OPTIONS;
			}

			@Override
			public void removeListDataListener(ListDataListener arg0) {
				// not needed, no changes possible.
			}

			@Override
			public Object getSelectedItem() {
				return selectedItem;
			}

			@Override
			public void setSelectedItem(Object anItem) {
				selectedItem = anItem;				
			}
			
		}
	}
	private void addPosition()
	{
		YouScopeFrame childFrame = frame.createModalChildFrame();
		childFrame.setTitle("Add Position");
		childFrame.setResizable(false);
		childFrame.setContentPane(new AddPositionComponent(childFrame));
		childFrame.pack();
		childFrame.setVisible(true);
	}
	private void deletePosition()
	{
		int row = pathTable.getSelectedRow();
		if(row < 0 || row >= positionInformations.size())
			return;
		PositionInformation positionInformation = positionInformations.remove(row);
		configuredPositions.remove(positionInformation);
		pathTableModel.fireTableRowsDeleted(row, row);
		notifyLayoutChanged();
	}
	/**
	 * Adds a listener which gets notified if wells/tiles were added or deleted. Does not get notified if
	 * positions of wells/tiles changed.
	 * @param listener Listener to add.
	 */
	public void addLayoutChangedListener(ActionListener listener)
	{
		layoutChangedListeners.add(listener);
	}
	/**
	 * Removes a previously added listener.
	 * @param listener listener to remove.
	 */
	public void removeLayoutChangedListener(ActionListener listener)
	{
		layoutChangedListeners.remove(listener);
	}
	private void notifyLayoutChanged()
	{
		for(ActionListener listener : layoutChangedListeners)
		{
			listener.actionPerformed(new ActionEvent(this, 1234, "Layout Changed"));
		}
	}
	private class PathTableModel extends AbstractTableModel
    {
        /**
		 * Serial Version UID.
		 */
		private static final long serialVersionUID = -3595541853840543126L;
		PathTableModel()
        {
           // do nothing.
        }

        @Override
        public String getColumnName(int column)
        {
        	if(column < 0 || column >= columns.length)
        		return "Invalid Column";
        	return columns[column].toString();
        }

        @Override
        public int getRowCount()
        {
            return positionInformations.size();
        }

        @Override
        public int getColumnCount()
        {
            return columns.length;
        }

        @Override
        public Class<?> getColumnClass(int column)
        {
        	if(column < 0 || column >= columns.length)
        		return String.class;
			return columns[column].type;
        }

        @Override
        public Object getValueAt(int row, int column)
        {
        	if(row < 0 || row >= positionInformations.size() || column < 0 || column >= columns.length)
        		return "Invalid Column";
        	PositionInformation positionInformation = positionInformations.get(row);
        	if(columns[column] == Column.MAIN_POSITION)
        		return positionInformation.getNumPositions() == 1 && positionInformation.getPositionType(0) == PositionInformation.POSITION_TYPE_MAIN_POSITION ? Integer.toString(positionInformation.getPosition(0)+1) : "---";
        	else if(columns[column] == Column.WELL)
        		return positionInformation.getWell() != null ? positionInformation.getWell().toString() : "---";
        	else if(columns[column] == Column.TILE)
        		return positionInformation.getNumPositions() == 2 ? Well.getWellName(positionInformation.getPosition(0), positionInformation.getPosition(1)) : "---";
    		else 
    		{
    			XYAndFocusPosition position = configuredPositions.get(positionInformation);
    			if(columns[column] == Column.X)
	        		return position.getX();
	    		else if(columns[column] == Column.Y)
	        		return position.getY();
	    		else if(columns[column] == Column.FOCUS)
	        		return position.getFocus();
	        	return "Invalid Column";
    		}
        }
        @Override
        public boolean isCellEditable(int row, int column)
        {
        	if(column < 0 || column >= columns.length)
        		return false;
        	return columns[column].isEdiable();
        }
        @Override
        public void setValueAt(Object objectValue, int row, int column) 
        {
        	if(row < 0 || row >= positionInformations.size() || column < 0 || column >= columns.length)
        		return;
        	if(!(objectValue instanceof Double))
        		return;
        	double value = (Double)objectValue;
        	PositionInformation positionInformation = positionInformations.get(row);
        	XYAndFocusPosition oldPosition = configuredPositions.get(positionInformation);
        	XYAndFocusPosition newPosition;
        	if(columns[column] == Column.X)
        		newPosition = new XYAndFocusPosition(value, oldPosition.getY(), oldPosition.getFocus());
        	else if(columns[column] == Column.Y)
        		newPosition = new XYAndFocusPosition(oldPosition.getX(), value, oldPosition.getFocus());
        	else if(columns[column] == Column.FOCUS)
        		newPosition = new XYAndFocusPosition(oldPosition.getX(), oldPosition.getY(), value);
        	else
        		return;
        	configuredPositions.put(positionInformation, newPosition);
        	fireTableCellUpdated(row, column);
		}
    }
	
	private class PathTableEditor extends AbstractCellEditor implements
	    TableCellEditor, TableCellRenderer
	{
		/**
		 * Serial Version UID.
		 */
		private static final long serialVersionUID = -5745423014387039692L;
		private DoubleTextField valueField = new DoubleTextField();
		private final JLabel label = new JLabel("");
		private Color activeBackground;
		private Color activeForeground;
		private Color passiveBackground;
		private Color passiveForeground;
		
		private Color activeSelectedBackground;
		private Color activeSelectedForeground;
		private Color passiveSelectedBackground;
		private Color passiveSelectedForeground;
		PathTableEditor()
		{
			label.setOpaque(true);
			label.setBorder(new EmptyBorder(0,3,0,3));
			valueField.addActionListener(new ActionListener() 
			{
				@Override
				public void actionPerformed(ActionEvent e) {
					fireEditingStopped();
				}
			});
			valueField.setBorder(new EmptyBorder(0,3,0,3));
			
			activeBackground = UIManager.getColor ("Table.background");
			if(activeBackground == null)
				activeBackground = getBackground();
			activeForeground = UIManager.getColor ("Table.foreground");
			if(activeForeground == null)
				activeForeground = getForeground();
			passiveBackground = activeBackground.darker();
			passiveForeground = activeForeground.darker();
			
			activeSelectedBackground = activeForeground;
			activeSelectedForeground = activeBackground;
			passiveSelectedBackground = activeSelectedBackground;
			passiveSelectedForeground = activeSelectedForeground.brighter();
			
			valueField.setBackground(activeSelectedBackground);
			valueField.setForeground(activeSelectedForeground);
			valueField.setCaretColor(activeSelectedForeground);
			
		}
		@Override
		public Object getCellEditorValue()
		{
			return valueField.getValue();
		}
		
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
		{
			if(value instanceof Double)
			{
				valueField.setValue(value);
			}
			else
			{
				valueField.setValue(Double.NaN);
			}
			return valueField;
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object object,
		        boolean isSelected, boolean hasFocus, int row, int column)
		{
			if(column < 0 || column >= columns.length)
			{
				label.setText("Invalid Column");
				label.setBackground(Color.RED);
				label.setForeground(Color.BLACK);
				label.setHorizontalAlignment(SwingConstants.LEFT);
				return label;
			}
			label.setText(object.toString());
			if(columns[column].isEdiable())
			{
				label.setForeground(isSelected ? activeSelectedForeground : activeForeground);
				label.setBackground(isSelected ? activeSelectedBackground : activeBackground);
				label.setHorizontalAlignment(SwingConstants.LEFT);
			}
			else
			{
				label.setForeground(isSelected ? passiveSelectedForeground : passiveForeground);
				label.setBackground(isSelected ? passiveSelectedBackground : passiveBackground);
				label.setHorizontalAlignment(SwingConstants.RIGHT);
			}
		    return label;
		}
	}
}
