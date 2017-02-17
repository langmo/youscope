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
package org.youscope.plugin.zslides;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonUIAdapter;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.configuration.FocusConfiguration;
import org.youscope.common.job.basicjobs.SimpleCompositeJob;
import org.youscope.common.microscope.Device;
import org.youscope.common.microscope.DeviceType;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.ImageLoadingTools;
import org.youscope.uielements.JobsDefinitionPanel;
import org.youscope.uielements.StandardFormats;

/**
 * @author Moritz Lang
 */
class ZSlidesJobConfigurationAddon extends ComponentAddonUIAdapter<ZSlidesJobConfiguration>
{
    private JTable zSlicesTable;

    private ZSlicesTableModel zSlicesTableModel;

    private Vector<Double> zSlices = new Vector<Double>();
    
    private JComboBox<String> focusDevicesField = new JComboBox<String>();

    private JobsDefinitionPanel jobPanel;

    /**
	 * Constructor.
	 * @param client Interface to the client.
	 * @param server Interface to the server.
	 * @throws AddonException 
	 */
	public ZSlidesJobConfigurationAddon(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(),  client, server);
	}
    
	static ComponentMetadataAdapter<ZSlidesJobConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<ZSlidesJobConfiguration>(ZSlidesJobConfiguration.TYPE_IDENTIFIER, 
				ZSlidesJobConfiguration.class, 
				SimpleCompositeJob.class, 
				"Z-Stack", 
				new String[]{"Containers"}, 
				"Takes images at different focal positions, realizing a stack of images which might e.g. be used to generate 3D images.",
				"icons/pictures-stack.png");
	}
	@Override
	protected Component createUI(ZSlidesJobConfiguration configuration) throws AddonException
	{
		setTitle("Z-Stack Job");
		setResizable(true);
		setMaximizable(false);
		
		GridBagConstraints newLineConstr = StandardFormats.getNewLineConstraint();
        GridBagConstraints bottomConstr = StandardFormats.getBottomContstraint();
		
        // Top panel
        GridBagLayout topLayout = new GridBagLayout();
        JPanel topPanel = new JPanel(topLayout);
        StandardFormats.addGridBagElement(new JLabel("Focus Device:"), topLayout, newLineConstr, topPanel);
        StandardFormats.addGridBagElement(focusDevicesField, topLayout, newLineConstr, topPanel);
        StandardFormats.addGridBagElement(new JLabel("Focus positions (relative to current focus):"), topLayout, newLineConstr, topPanel);
        
        // Center Panel
        JPanel centralPanel = new JPanel(new BorderLayout(2, 2));
        zSlicesTableModel = new ZSlicesTableModel();
        zSlicesTable = new JTable(zSlicesTableModel);
        zSlicesTable.setRowSelectionAllowed(true);
        zSlicesTable.setColumnSelectionAllowed(false);
        zSlicesTable.setSurrendersFocusOnKeystroke(true);
        zSlicesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumn col = zSlicesTable.getColumnModel().getColumn(0);
        col.setPreferredWidth(40);
        col.setMaxWidth(40);
        col.setMinWidth(40);
        zSlicesTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        ZSlicesTableEditor editor = new ZSlicesTableEditor();
        zSlicesTable.setDefaultRenderer(ZSlicesTableEditor.class, editor);
        zSlicesTable.setDefaultEditor(ZSlicesTableEditor.class, editor);
        JScrollPane zSlicesScrollPane = new JScrollPane(zSlicesTable);
        zSlicesScrollPane.setPreferredSize(new Dimension(250, 70));
        zSlicesScrollPane.setMinimumSize(new Dimension(10, 10));
        centralPanel.add(zSlicesScrollPane, BorderLayout.CENTER);
        // Up, down, add and remove Buttons
        Icon upButtonIcon = ImageLoadingTools.getResourceIcon("icons/arrow-090.png", "Move Upwards");
        Icon downButtonIcon = ImageLoadingTools.getResourceIcon("icons/arrow-270.png", "Move Downwards");
        Icon addButtonIcon = ImageLoadingTools.getResourceIcon("icons/block--plus.png", "Add Z-Position");
        Icon deleteButtonIcon = ImageLoadingTools.getResourceIcon("icons/block--minus.png", "Remove Z-Position");
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
                    addRows(new double[]{0.0});
                }
            });
        JButton addRangeButton;
        if (addButtonIcon == null)
        	addRangeButton = new JButton("Add Range");
        else
        	addRangeButton = new JButton("Add Range", addButtonIcon);
        addRangeButton.setHorizontalAlignment(SwingConstants.LEFT);
        addRangeButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    YouScopeFrame childFrame = getContainingFrame().createModalChildFrame();
                    @SuppressWarnings("unused")
					AddRangeFrame addRangeFrame = new AddRangeFrame(ZSlidesJobConfigurationAddon.this, childFrame);
                    childFrame.setVisible(true);
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
                    int row = zSlicesTable.getSelectedRow();
                    if (row < 0)
                        return;
                    zSlices.removeElementAt(row);
                    zSlicesTableModel.fireTableRowsDeleted(row, row);
                }
            });        
        GridBagLayout buttonLayout = new GridBagLayout();
        JPanel buttonPanel = new JPanel(buttonLayout);
        StandardFormats.addGridBagElement(newPositionButton, buttonLayout, newLineConstr,  buttonPanel);
        StandardFormats.addGridBagElement(addRangeButton, buttonLayout, newLineConstr,  buttonPanel);
        StandardFormats.addGridBagElement(deletePositionButton, buttonLayout, newLineConstr,  buttonPanel);
        StandardFormats.addGridBagElement(new JPanel(), buttonLayout, newLineConstr,  buttonPanel);
        StandardFormats.addGridBagElement(upButton, buttonLayout, newLineConstr,  buttonPanel);
        StandardFormats.addGridBagElement(downButton, buttonLayout, newLineConstr,  buttonPanel);
        StandardFormats.addGridBagElement(new JPanel(), buttonLayout, bottomConstr,  buttonPanel);
        centralPanel.add(buttonPanel, BorderLayout.EAST);
        
        // Bottom panel
        GridBagLayout bottomLayout = new GridBagLayout();
        JPanel bottomPanel = new JPanel(bottomLayout);
        // The jobs in every position
        StandardFormats.addGridBagElement(new JLabel("Jobs executed at each focus position:"), bottomLayout, newLineConstr, bottomPanel);
        jobPanel = new JobsDefinitionPanel(getClient(), getServer(), getContainingFrame());
        jobPanel.setJobs(configuration.getJobs());
        StandardFormats.addGridBagElement(jobPanel, bottomLayout, newLineConstr, bottomPanel);

        // Load state
        for (double z : configuration.getSlideZPositions())
        {
            zSlices.add(z);
        }
        loadFocusDevices();
        if(configuration.getFocusConfiguration()!= null && configuration.getFocusConfiguration().getFocusDevice() != null)
        {
        	String focusDevice = configuration.getFocusConfiguration().getFocusDevice();
        	for (int i = 0; i < focusDevicesField.getItemCount(); i++)
            {
                if (focusDevice.compareTo(focusDevicesField.getItemAt(i).toString()) == 0)
                	focusDevicesField.setSelectedIndex(i);
            }
        }

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(topPanel, BorderLayout.NORTH);
        contentPane.add(centralPanel, BorderLayout.CENTER);
        contentPane.add(bottomPanel, BorderLayout.SOUTH);
        
        return contentPane;
    }

	void addRows(double[] rows)
	{
		for(double row : rows)
		{
			zSlices.add(row);
		}
        zSlicesTableModel.fireTableRowsInserted(zSlices.size() - rows.length,  zSlices.size() - 1);
	}
	
	private void loadFocusDevices()
	{
    	String[] focusDevices;
    	try
		{
    		Device[] devices = getServer().getMicroscope().getDevices(DeviceType.StageDevice);
    		focusDevices = new String[devices.length];
    		for(int i=0; i< devices.length; i++)
    		{
    			focusDevices[i] = devices[i].getDeviceID();
    		}
		}
		catch (Exception e)
		{
			sendErrorMessage("Could not obtain focus device names.", e);
			focusDevices = null;
		}
		
		if (focusDevices == null || focusDevices.length <= 0)
        {
			focusDevices = new String[]{""};
        }
		
		focusDevicesField.removeAllItems();
		for(String focusDevice : focusDevices)
		{
			focusDevicesField.addItem(focusDevice);
		}
	}
	
    private void moveUpDown(boolean moveUp)
    {
        int idx = zSlicesTable.getSelectedRow();
        if (idx == -1 || (moveUp && idx == 0) || (!moveUp && idx + 1 >= zSlices.size()))
            return;
        int newIdx;
        if (moveUp)
            newIdx = idx - 1;
        else
            newIdx = idx + 1;
        double z = zSlices.get(idx);
        zSlices.removeElementAt(idx);
        zSlices.add(newIdx, z);
        zSlicesTableModel.fireTableDataChanged();
        zSlicesTable.setRowSelectionInterval(newIdx, newIdx);
    }

    private class ZSlicesTableEditor extends AbstractCellEditor implements
            TableCellEditor, TableCellRenderer
    {
        /**
         * Serial Version UID.
         */
        private static final long serialVersionUID = -1118052131595616115L;

        private String value;

        @Override
        public Object getCellEditorValue()
        {
            return value;
        }

        // @Override
        @Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
        {
        	JFormattedTextField valuesField = new JFormattedTextField(StandardFormats.getDoubleFormat());
        	
        	valuesField.setValue(zSlices.get(row));
            ValuesFieldActionListener listener = new ValuesFieldActionListener(row, valuesField);
            valuesField.addActionListener(listener);
            valuesField.addFocusListener(listener);
            valuesField.addKeyListener(listener);
            valuesField.setBorder(new EmptyBorder(0, 0, 0, 0));
            valuesField.setMargin(new Insets(0, 0, 0, 0));
            return valuesField;
        }
        
        private class ValuesFieldActionListener implements ActionListener, FocusListener, KeyListener
        {
            private int row;

            private JFormattedTextField valuesField;

            public ValuesFieldActionListener(int row, JFormattedTextField valuesField)
            {
                this.row = row;
                this.valuesField = valuesField;
            }

            @Override
            public void actionPerformed(ActionEvent arg0)
            {
            	commit();
            }

            @Override
            public void focusGained(FocusEvent arg0)
            {
                // Do nothing.

            }

            @Override
            public void focusLost(FocusEvent arg0)
            {
            	commit();
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
				// Do nothing
				
			}
			
			private void commit()
			{
				try
				{
					valuesField.commitEdit();
				}
				catch(@SuppressWarnings("unused") ParseException e)
				{
					valuesField.setBackground(Color.RED);
					zSlicesTableModel.fireTableRowsUpdated(row, row);
					return;
				}
				valuesField.setBackground(Color.WHITE);
				double value = ((Number)valuesField.getValue()).doubleValue();
            	zSlices.setElementAt(value, row);
                zSlicesTableModel.fireTableRowsUpdated(row, row);
			}
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object object,
                boolean isSelected, boolean hasFocus, int row, int column)
        {
        	JLabel label = new JLabel(object.toString());
            label.setOpaque(true);
                        
            if (isSelected)
            {
                label.setBackground(table.getSelectionBackground());
                label.setForeground(table.getSelectionForeground());
            }
            else
            {
            	label.setForeground(Color.BLACK);
            	if(column == 0)
            	{
            		label.setBackground(Color.LIGHT_GRAY);
            	}
            	else
            	{
            		label.setBackground(Color.WHITE);
            	}
            }
                        
            return label;
        }
    }

    private class ZSlicesTableModel extends AbstractTableModel
    {
        /**
         * Serial Version UID.
         */
        private static final long serialVersionUID = -2836711115571383139L;

        private String[] columnNames =
            { "No.", "Relative Focus Position" };

        ZSlicesTableModel()
        {
            // Do nothing.
        }

        @Override
        public String getColumnName(int col)
        {
            return columnNames[col];
        }

        @Override
        public int getRowCount()
        {
            return zSlices.size();
        }

        @Override
        public int getColumnCount()
        {
            return columnNames.length;
        }

        @Override
        public Class<?> getColumnClass(int column)
        {
        	return ZSlicesTableEditor.class;
        }

        @Override
        public Object getValueAt(int row, int col)
        {
        	if(col == 0)
        		return "#" + Integer.toString(row+1);
			return zSlices.elementAt(row);
        }
        @Override
        public boolean isCellEditable(int row, int col)
        {
        	if(col == 0)
        		return false;
			return true;
        }
        @Override
        public void setValueAt(Object value, int row, int col) {
			
        	if(value instanceof Number)
        		zSlices.setElementAt(((Number)value).doubleValue(), row);
		}

    }

	@Override
	protected void commitChanges(ZSlidesJobConfiguration configuration) {
		if (zSlices.size() <= 0)
        {
            JOptionPane.showMessageDialog(null, "At least one z-position has to be set.",
                    "Job Invalid", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double[] zValues = new double[zSlices.size()];
        for(int i=0; i < zValues.length; i++)
        {
        	zValues[i] = zSlices.elementAt(i);
        }
        configuration.setSlideZPositions(zValues);

        String focusDevice = focusDevicesField.getSelectedItem().toString();
        FocusConfiguration focusConfiguration = new FocusConfiguration();
        focusConfiguration.setAdjustmentTime(0);
        focusConfiguration.setFocusDevice(focusDevice);
        configuration.setFocusConfiguration(focusConfiguration);
        configuration.setJobs(jobPanel.getJobs());
	}

	@Override
	protected void initializeDefaultConfiguration(ZSlidesJobConfiguration configuration) throws AddonException {
		// do nothing.
	}
}
