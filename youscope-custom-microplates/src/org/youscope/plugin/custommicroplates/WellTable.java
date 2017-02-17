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
package org.youscope.plugin.custommicroplates;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListDataListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.clientinterfaces.PropertyProvider;
import org.youscope.common.Well;
import org.youscope.common.microplate.WellLayout;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DoubleTextField;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.ImageLoadingTools;

/**
 * Table to show and set the configured well layouts.
 * @author Moritz Lang
 *
 */
class WellTable extends JPanel
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -8901745537053857021L;
	private final JTable wellTable;
	private final WellTableModel wellTableModel;
	private final ArrayList<WellLayout> wellLayouts;
	private final ArrayList<ActionListener> layoutChangedListeners = new ArrayList<>(1);
	private final YouScopeFrame frame;
	private static final String[] COLUMN_NAMES = {"Well", "Left (um)", "Top (um)", "Width (um)", "Height (um)"};
	private final YouScopeServer server;
	private final YouScopeClient client;
	
	private static final String PROPERTY_WELL_X = "YouScope.CustomMicroplate.WellX";
	private static final String PROPERTY_WELL_Y = "YouScope.CustomMicroplate.WellY";
	private static final String PROPERTY_WELL_X_STEP = "YouScope.CustomMicroplate.WellXStep";
	private static final String PROPERTY_WELL_Y_STEP = "YouScope.CustomMicroplate.WellYSetp";
	private static final String PROPERTY_WELL_X_NUM = "YouScope.CustomMicroplate.WellXNum";
	private static final String PROPERTY_WELL_Y_NUM = "YouScope.CustomMicroplate.WellYNum";
	private static final String PROPERTY_LEFT = "YouScope.CustomMicroplate.Left";
	private static final String PROPERTY_TOP = "YouScope.CustomMicroplate.Top";
	private static final String PROPERTY_WIDTH = "YouScope.CustomMicroplate.Wdith";
	private static final String PROPERTY_HEIGHT = "YouScope.CustomMicroplate.Height";
	private static final String PROPERTY_HORIZONTAL_DISTANCE = "YouScope.CustomMicroplate.HorizontalDistance";
	private static final String PROPERTY_VERTICAL_DISTANCE = "YouScope.CustomMicroplate.VerticalDistance";
	
	public WellTable(List<WellLayout> wellLayouts, YouScopeFrame frame, YouScopeClient client, YouScopeServer server)
	{
		super(new BorderLayout(0, 0));
		this.server = server;
		this.client = client;
		this.wellLayouts = new ArrayList<>(wellLayouts);
		Collections.sort(wellLayouts);
		this.frame = frame;
		this.wellTableModel = new WellTableModel();
		this.wellTable = new JTable(wellTableModel);
		wellTable.setAutoCreateColumnsFromModel(true);
		wellTable.setRowSelectionAllowed(true);
		//wellTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        wellTable.setColumnSelectionAllowed(false);
        wellTable.setSurrendersFocusOnKeystroke(true);
        wellTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        wellTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        wellTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        WellTableEditor editor = new WellTableEditor();
        wellTable.setDefaultRenderer(Well.class, editor);
        wellTable.setDefaultRenderer(Double.class, editor);
        wellTable.setDefaultEditor(Double.class, editor);
        JScrollPane pathTableScrollPane = new JScrollPane(wellTable);
        pathTableScrollPane.setPreferredSize(new Dimension(250, 70));
        pathTableScrollPane.setMinimumSize(new Dimension(10, 10));
        add(pathTableScrollPane, BorderLayout.CENTER);
        
        // Up, down, add and remove Buttons
        Icon addButtonIcon = ImageLoadingTools.getResourceIcon("icons/block--plus.png", "Add Well");
        Icon deleteButtonIcon = ImageLoadingTools.getResourceIcon("icons/block--minus.png", "Remove Well");
        JButton addWellButton;
        if (addButtonIcon == null)
            addWellButton = new JButton("Add Well");
        else
            addWellButton = new JButton("Add Well", addButtonIcon);
        addWellButton.setHorizontalAlignment(SwingConstants.LEFT);
        addWellButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	addWell();
                }
            });
        
        JButton addRangeButton;
        if (addButtonIcon == null)
        	addRangeButton = new JButton("Add Multiple Wells");
        else
        	addRangeButton = new JButton("Add Multiple Wells", addButtonIcon);
        addRangeButton.setHorizontalAlignment(SwingConstants.LEFT);
        addRangeButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	addRange();
                }
            });
        
        JButton deleteWellButton;
        if (deleteButtonIcon == null)
            deleteWellButton = new JButton("Delete Well");
        else
            deleteWellButton = new JButton("Delete Well", deleteButtonIcon);
        deleteWellButton.setHorizontalAlignment(SwingConstants.LEFT);
        deleteWellButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	removeWell();
                }
            });        
        DynamicPanel buttonPanel = new DynamicPanel();
        buttonPanel.add(addWellButton);
        buttonPanel.add(addRangeButton);
        buttonPanel.add(deleteWellButton);
        buttonPanel.addFillEmpty();
        buttonPanel.setOpaque(true);
        add(buttonPanel, BorderLayout.EAST);
	}
	public List<WellLayout> getWellLayouts()
	{
		return wellLayouts;
	}
	private void removeWell()
	{
		int[] rows = wellTable.getSelectedRows();
		Arrays.sort(rows);
    	for(int i=rows.length-1; i>=0; i--)
    	{
    		wellLayouts.remove(rows[i]);
    	}
		
    	wellTableModel.fireTableDataChanged();
    	notifyLayoutChanged();
	}
	private void addWell()
	{
		YouScopeFrame childFrame = frame.createModalChildFrame();
		childFrame.setContentPane(new AddWellComponent(childFrame));
		childFrame.pack();
		childFrame.setTitle("Add Well");
		childFrame.setVisible(true);
	}
	private void addRange()
	{
		YouScopeFrame childFrame = frame.createModalChildFrame();
		childFrame.setContentPane(new AddRangeComponent(childFrame));
		childFrame.pack();
		childFrame.setTitle("Add Multiple Wells");
		childFrame.setVisible(true);
	}
	private class AddWellComponent extends DynamicPanel
	{
		/**
		 * Serial Version UID.
		 */
		private static final long serialVersionUID = 7185985159945761181L;
		private JComboBox<String> wellYComboBox = new JComboBox<>(new YComboBoxModel(NUM_POS_OPTIONS));
		private JComboBox<Integer> wellXComboBox = new JComboBox<>(new XComboBoxModel(NUM_POS_OPTIONS));
		private DoubleTextField leftTextField = new DoubleTextField(0);
		private DoubleTextField topTextField = new DoubleTextField(0);
		private DoubleTextField widthTextField = new DoubleTextField(100);
		private DoubleTextField heightTextField = new DoubleTextField(100);
		private static final int NUM_POS_OPTIONS = 999;
		private final YouScopeFrame frame;
		AddWellComponent(YouScopeFrame frame)
		{
			this.frame = frame;
			
			add(new JLabel("Well:"));
			JPanel wellPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			wellPanel.setOpaque(false);
			wellPanel.add(wellYComboBox);
			wellPanel.add(wellXComboBox);
			add(wellPanel);
			
			add(new JLabel("Position left-top (um):"));
			JPanel positionPanel = new JPanel(new GridLayout(1,2));
			positionPanel.setOpaque(false);
			positionPanel.add(leftTextField);
			positionPanel.add(topTextField);
			add(positionPanel);
			
			JButton currentPositionButton = new JButton("Current Position");
			currentPositionButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					Point2D.Double currentPosition;
					try
					{
						currentPosition = server.getMicroscope().getStageDevice().getPosition();
					}
					catch(Exception e)
					{
						client.sendError("Could not obtain current postion of microscope.", e);
						return;
					}
					
					leftTextField.setValue(currentPosition.x);
					topTextField.setValue(currentPosition.y);
				}
			});
			add(currentPositionButton);
			
			add(new JLabel("Width and height (um):"));
			JPanel dimensionPanel = new JPanel(new GridLayout(1,2));
			dimensionPanel.setOpaque(false);
			widthTextField.setMinimalValue(Double.MIN_NORMAL*10);
			dimensionPanel.add(widthTextField);
			heightTextField.setMinimalValue(Double.MIN_NORMAL*10);
			dimensionPanel.add(heightTextField);
			add(dimensionPanel);
			
			JButton currentDimensionButton = new JButton("Current Position - (left,top)");
			currentDimensionButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					Point2D.Double currentPosition;
					try
					{
						currentPosition = server.getMicroscope().getStageDevice().getPosition();
					}
					catch(Exception e)
					{
						client.sendError("Could not obtain current postion of microscope.", e);
						return;
					}
					double width = currentPosition.x - leftTextField.getValue();
					double height = currentPosition.y - topTextField.getValue();
					if(width <= 0 || height <= 0)
					{
						client.sendError("Current position smaller or equal to top-left of well.");
						return;
					}
					widthTextField.setValue(width);
					heightTextField.setValue(height);
				}
			});
			add(currentDimensionButton);
			
			JButton addButton = new JButton("Add Well");
			addButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					confirm();
				}
			});
			add(addButton);
			
			PropertyProvider properties = client.getPropertyProvider();
			wellXComboBox.setSelectedIndex(properties.getProperty(PROPERTY_WELL_X, 0));
			wellYComboBox.setSelectedIndex(properties.getProperty(PROPERTY_WELL_Y, 0));
			leftTextField.setValue(properties.getProperty(PROPERTY_LEFT, 0.0));
			topTextField.setValue(properties.getProperty(PROPERTY_TOP, 0.0));
			widthTextField.setValue(properties.getProperty(PROPERTY_WIDTH, 9000.));
			heightTextField.setValue(properties.getProperty(PROPERTY_HEIGHT, 9000.));
		}
		void confirm()
		{
			int previousPos = -1;
			Well well = new Well(wellYComboBox.getSelectedIndex(), wellXComboBox.getSelectedIndex());
			for(int i=0; i<wellLayouts.size(); i++)
			{
				if(wellLayouts.get(i).getWell().equals(well))
				{
					previousPos = i;
					break;
				}
			}
			if(previousPos >= 0)
			{
				if(JOptionPane.showConfirmDialog(this, "Well " + well.toString()+" already exists. Overwrite?", "Well Already Exists", JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)
					return;
			}
			double x = leftTextField.getValue();
			double y = topTextField.getValue();
			double width = widthTextField.getValue();
			double height = heightTextField.getValue();
			WellLayout wellLayout = new WellLayout(x, y, width, height, well);
			if(previousPos >= 0)
			{
				wellLayouts.set(previousPos, wellLayout);
				wellTableModel.fireTableRowsUpdated(previousPos, previousPos);
			}
			else
			{
				wellLayouts.add(wellLayout);
				Collections.sort(wellLayouts);
				wellTableModel.fireTableDataChanged();
			}
			notifyLayoutChanged();
			frame.setVisible(false);
			
			PropertyProvider properties = client.getPropertyProvider();
			properties.setProperty(PROPERTY_WELL_X, well.getWellX());
			properties.setProperty(PROPERTY_WELL_Y, well.getWellY());
			properties.setProperty(PROPERTY_LEFT, x);
			properties.setProperty(PROPERTY_TOP, y);
			properties.setProperty(PROPERTY_WIDTH, width);
			properties.setProperty(PROPERTY_HEIGHT, height);
		}
	}
	private class AddRangeComponent extends DynamicPanel
	{
		/**
		 * Serial Version UID.
		 */
		private static final long serialVersionUID = 7185985159945761181L;
		private JComboBox<String> wellYComboBox = new JComboBox<>(new YComboBoxModel(NUM_POS_OPTIONS));
		private JComboBox<Integer> wellXComboBox = new JComboBox<>(new XComboBoxModel(NUM_POS_OPTIONS));
		private JComboBox<Integer> wellXStepComboBox = new JComboBox<>(new XComboBoxModel(NUM_POS_OPTIONS));
		private JComboBox<Integer> wellYStepComboBox = new JComboBox<>(new XComboBoxModel(NUM_POS_OPTIONS));
		private JComboBox<Integer> wellXNumComboBox = new JComboBox<>(new XComboBoxModel(NUM_POS_OPTIONS));
		private JComboBox<Integer> wellYNumComboBox = new JComboBox<>(new XComboBoxModel(NUM_POS_OPTIONS));
		private DoubleTextField leftTextField = new DoubleTextField(0);
		private DoubleTextField topTextField = new DoubleTextField(0);
		private DoubleTextField widthTextField = new DoubleTextField(100);
		private DoubleTextField heightTextField = new DoubleTextField(100);
		private DoubleTextField horizontalDistanceTextField = new DoubleTextField(100);
		private DoubleTextField verticalDistanceTextField = new DoubleTextField(100);
		
		private static final int NUM_POS_OPTIONS = 999;
		private final YouScopeFrame frame;
		AddRangeComponent(YouScopeFrame frame)
		{
			this.frame = frame;
			
			add(new JLabel("Top-left Well:"));
			JPanel wellPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			wellPanel.setOpaque(false);
			wellPanel.add(wellYComboBox);
			wellPanel.add(wellXComboBox);
			add(wellPanel);
			
			add(new JLabel("Position top-left of top-left well (um):"));
			JPanel positionPanel = new JPanel(new GridLayout(1,2));
			positionPanel.setOpaque(false);
			positionPanel.add(leftTextField);
			positionPanel.add(topTextField);
			add(positionPanel);
			
			JButton currentPositionButton = new JButton("Current Position");
			currentPositionButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					Point2D.Double currentPosition;
					try
					{
						currentPosition = server.getMicroscope().getStageDevice().getPosition();
					}
					catch(Exception e)
					{
						client.sendError("Could not obtain current postion of microscope.", e);
						return;
					}
					
					leftTextField.setValue(currentPosition.x);
					topTextField.setValue(currentPosition.y);
				}
			});
			add(currentPositionButton);
			
			add(new JLabel("Width and height of wells (um):"));
			JPanel dimensionPanel = new JPanel(new GridLayout(1,2));
			dimensionPanel.setOpaque(false);
			widthTextField.setMinimalValue(Double.MIN_NORMAL*10);
			dimensionPanel.add(widthTextField);
			heightTextField.setMinimalValue(Double.MIN_NORMAL*10);
			dimensionPanel.add(heightTextField);
			add(dimensionPanel);
			
			JButton currentDimensionButton = new JButton("Current Position - (left,top)");
			currentDimensionButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					Point2D.Double currentPosition;
					try
					{
						currentPosition = server.getMicroscope().getStageDevice().getPosition();
					}
					catch(Exception e)
					{
						client.sendError("Could not obtain current postion of microscope.", e);
						return;
					}
					double width = Math.abs(currentPosition.x - leftTextField.getValue());
					double height = Math.abs(currentPosition.y - topTextField.getValue());
					if(width < Double.MIN_NORMAL*10)
						width = Double.MIN_NORMAL*10;
					if(height < Double.MIN_NORMAL*10)
						height = Double.MIN_NORMAL*10;
					widthTextField.setValue(width);
					heightTextField.setValue(height);
				}
			});
			add(currentDimensionButton);
			
			add(new JLabel("Number of wells horizontally/vertically:"));
			JPanel numPanel = new JPanel(new GridLayout(1,2));
			numPanel.setOpaque(false);
			numPanel.add(wellXNumComboBox);
			numPanel.add(wellYNumComboBox);
			add(numPanel);
			
			add(new JLabel("Next well identifier steps horizontally/vertically:"));
			JPanel stepPanel = new JPanel(new GridLayout(1,2));
			stepPanel.setOpaque(false);
			stepPanel.add(wellXStepComboBox);
			stepPanel.add(wellYStepComboBox);
			add(stepPanel);
			
			add(new JLabel("Distance of wells horizontally/vertically:"));
			JPanel distancePanel = new JPanel(new GridLayout(1,2));
			distancePanel.setOpaque(false);
			horizontalDistanceTextField.setMinimalValue(Double.MIN_NORMAL*10);
			distancePanel.add(horizontalDistanceTextField);
			verticalDistanceTextField.setMinimalValue(Double.MIN_NORMAL*10);
			distancePanel.add(verticalDistanceTextField);
			add(distancePanel);
			JButton currentDistanceButton = new JButton("Current Position - (left,top)");
			currentDistanceButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					Point2D.Double currentPosition;
					try
					{
						currentPosition = server.getMicroscope().getStageDevice().getPosition();
					}
					catch(Exception e)
					{
						client.sendError("Could not obtain current postion of microscope.", e);
						return;
					}
					double width = Math.abs(currentPosition.x - leftTextField.getValue());
					double height = Math.abs(currentPosition.y - topTextField.getValue());
					if(width < Double.MIN_NORMAL*10)
						width = Double.MIN_NORMAL*10;
					if(height < Double.MIN_NORMAL*10)
						height = Double.MIN_NORMAL*10;
					horizontalDistanceTextField.setValue(width);
					verticalDistanceTextField.setValue(height);
				}
			});
			add(currentDistanceButton);
			
			JButton addButton = new JButton("Add Wells");
			addButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					confirm();
				}
			});
			add(addButton);
			
			PropertyProvider properties = client.getPropertyProvider();
			wellXComboBox.setSelectedIndex(properties.getProperty(PROPERTY_WELL_X, 0));
			wellYComboBox.setSelectedIndex(properties.getProperty(PROPERTY_WELL_Y, 0));
			wellXStepComboBox.setSelectedIndex(properties.getProperty(PROPERTY_WELL_X_STEP, 1)-1);
			wellYStepComboBox.setSelectedIndex(properties.getProperty(PROPERTY_WELL_Y_STEP, 1)-1);
			wellXNumComboBox.setSelectedIndex(properties.getProperty(PROPERTY_WELL_X_NUM, 2)-1);
			wellYNumComboBox.setSelectedIndex(properties.getProperty(PROPERTY_WELL_Y_NUM, 2)-1);
			leftTextField.setValue(properties.getProperty(PROPERTY_LEFT, 0.0));
			topTextField.setValue(properties.getProperty(PROPERTY_TOP, 0.0));
			widthTextField.setValue(properties.getProperty(PROPERTY_WIDTH, 9000.));
			heightTextField.setValue(properties.getProperty(PROPERTY_HEIGHT, 9000.));
			horizontalDistanceTextField.setValue(properties.getProperty(PROPERTY_HORIZONTAL_DISTANCE, 9000.));
			verticalDistanceTextField.setValue(properties.getProperty(PROPERTY_VERTICAL_DISTANCE, 9000.));
			
			
		}
		void confirm()
		{
			int wellY0 = wellYComboBox.getSelectedIndex();
			int wellX0 = wellXComboBox.getSelectedIndex();
			int wellYStep = wellYStepComboBox.getSelectedIndex()+1;
			int wellXStep = wellXStepComboBox.getSelectedIndex()+1;
			int wellNumX = wellXNumComboBox.getSelectedIndex()+1;
			int wellNumY = wellYNumComboBox.getSelectedIndex()+1;
			double x0 = leftTextField.getValue();
			double y0 = topTextField.getValue();
			double width = widthTextField.getValue();
			double height = heightTextField.getValue();
			double distanceX = horizontalDistanceTextField.getValue();
			double distanceY = verticalDistanceTextField.getValue();
			
			boolean confirmOverwrite = false;
			for(int yID = wellY0; yID < wellY0 + wellNumY*wellYStep; yID+=wellYStep)
			{
				for(int xID = wellX0; xID < wellX0 + wellNumX*wellXStep; xID+=wellXStep)
				{
					Well well = new Well(yID, xID);
					for(int i=0; i<wellLayouts.size(); i++)
					{
						if(wellLayouts.get(i).getWell().equals(well))
						{
							if(confirmOverwrite || JOptionPane.showConfirmDialog(this, "Some wells with given identifiers already exist. Overwrite?", "Wells Already Exists", JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)
								return;
							confirmOverwrite = true;
							wellLayouts.remove(i);
						}
					}
				}
			}
			
			for(int yID = wellY0; yID < wellY0 + wellNumY*wellYStep; yID+=wellYStep)
			{
				for(int xID = wellX0; xID < wellX0 + wellNumX*wellXStep; xID+=wellXStep)
				{
					Well well = new Well(yID, xID);
					WellLayout wellLayout = new WellLayout(x0 + (xID-wellX0)/wellXStep * distanceX, y0 + (yID-wellY0)/wellYStep * distanceY, width, height, well);
					wellLayouts.add(wellLayout);
				}
			}
			
			Collections.sort(wellLayouts);
			wellTableModel.fireTableDataChanged();
			notifyLayoutChanged();
			frame.setVisible(false);
			
			PropertyProvider properties = client.getPropertyProvider();
			properties.setProperty(PROPERTY_WELL_X, wellX0);
			properties.setProperty(PROPERTY_WELL_Y, wellY0);
			properties.setProperty(PROPERTY_WELL_X_STEP, wellXStep);
			properties.setProperty(PROPERTY_WELL_Y_STEP, wellYStep);
			properties.setProperty(PROPERTY_WELL_X_NUM, wellNumX);
			properties.setProperty(PROPERTY_WELL_Y_NUM, wellNumY);
			properties.setProperty(PROPERTY_LEFT, x0);
			properties.setProperty(PROPERTY_TOP, y0);
			properties.setProperty(PROPERTY_WIDTH, width);
			properties.setProperty(PROPERTY_HEIGHT, height);
			properties.setProperty(PROPERTY_HORIZONTAL_DISTANCE, distanceX);
			properties.setProperty(PROPERTY_VERTICAL_DISTANCE, distanceY);
		}
	}
	private static class YComboBoxModel implements ComboBoxModel<String>
	{
		private Object selectedItem = Well.getYWellName(0);
		private final int numPos;
		YComboBoxModel(int numPos)
		{
			this.numPos = numPos;
		}
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
			return numPos;
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
	private static class XComboBoxModel implements ComboBoxModel<Integer>
	{
		private Object selectedItem = 1;
		private final int numPos;
		XComboBoxModel(int numPos)
		{
			this.numPos = numPos;
		}
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
			return numPos;
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
	public void addLayoutChangedListener(ActionListener listener)
	{
		layoutChangedListeners.add(listener);
	}
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
	private class WellTableModel extends AbstractTableModel
    {
        /**
		 * Serial Version UID.
		 */
		private static final long serialVersionUID = -3595541853840543126L;
		WellTableModel()
        {
           // do nothing.
        }

        @Override
        public String getColumnName(int column)
        {
        	if(column < 0 || column >= COLUMN_NAMES.length)
        		return "Invalid Column";
        	return COLUMN_NAMES[column];
        }

        @Override
        public int getRowCount()
        {
            return wellLayouts.size();
        }

        @Override
        public int getColumnCount()
        {
            return COLUMN_NAMES.length;
        }

        @Override
        public Class<?> getColumnClass(int column)
        {
        	if(column == 0)
        		return Well.class;
			return Double.class;
        }

        @Override
        public Object getValueAt(int row, int column)
        {
        	if(row < 0 || row >= wellLayouts.size() || column < 0 || column >= COLUMN_NAMES.length)
        		return "Invalid Column";
        	WellLayout wellLayout = wellLayouts.get(row);
        	switch(column)
        	{
        	case 0:
        		return wellLayout.getWell();
        	case 1:
        		return wellLayout.getX();
        	case 2:
        		return wellLayout.getY();
        	case 3:
        		return wellLayout.getWidth();
        	case 4:
        		return wellLayout.getHeight();
        	default:
        		return "Invalid Column";
        	}
        }
        @Override
        public boolean isCellEditable(int row, int column)
        {
        	if(column <= 0 || column >= COLUMN_NAMES.length)
        		return false;
        	return true;
        }
        @Override
        public void setValueAt(Object objectValue, int row, int column) 
        {
        	if(row < 0 || row >= wellLayouts.size() || column <= 0 || column >= COLUMN_NAMES.length)
        		return;
        	if(!(objectValue instanceof Double))
        		return;
        	double value = (Double)objectValue;
        	
        	WellLayout oldLayout = wellLayouts.get(row);
        	WellLayout newLayout;
        	switch(column)
        	{
        	case 1:
        		newLayout = new WellLayout(value, oldLayout.getY(), oldLayout.getWidth(), oldLayout.getHeight(), oldLayout.getWell());
        		break;
        	case 2:
        		newLayout = new WellLayout(oldLayout.getX(), value, oldLayout.getWidth(), oldLayout.getHeight(), oldLayout.getWell());
        		break;
        	case 3:
        		newLayout = new WellLayout(oldLayout.getX(), oldLayout.getY(), value, oldLayout.getHeight(), oldLayout.getWell());
        		break;
        	case 4:
        		newLayout = new WellLayout(oldLayout.getX(), oldLayout.getY(), oldLayout.getWidth(), value, oldLayout.getWell());
        		break;
        	default:
        		return;
        	}
        	wellLayouts.set(row, newLayout);
        	fireTableCellUpdated(row, column);
        	notifyLayoutChanged();
		}
    }
	
	private class WellTableEditor extends AbstractCellEditor implements
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
		WellTableEditor()
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
			if(column < 0 || column >= COLUMN_NAMES.length)
			{
				label.setText("Invalid Column");
				label.setBackground(Color.RED);
				label.setForeground(Color.BLACK);
				label.setHorizontalAlignment(SwingConstants.LEFT);
				return label;
			}
			label.setText(object.toString());
			if(column>0)
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
