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
package org.youscope.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.PositionInformation;
import org.youscope.common.table.ColumnDefinition;
import org.youscope.common.table.TableDefinition;
import org.youscope.common.table.TableProducer;
import org.youscope.common.util.TextTools;
import org.youscope.uielements.StandardFormats;

/**
 * @author Moritz Lang
 *
 */
class TableDataPlotterPanel extends JPanel
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -705056655997121545L;
	private JRadioButton					xTimeField				= new JRadioButton("Time", true);
	private JRadioButton					xEvaluationField				= new JRadioButton("Evaluation", false);
	private JRadioButton					xFromTableField				= new JRadioButton("From Table Column", false);
	private JComboBox<ColumnElement> xFieldCombo;
	private JList<ColumnElement> yFieldList;
	private final JButton displayButton = new JButton("Display Function");
	private final YouScopeClient client;
	private final TableProducer tableDataProducer; 
	
	private JRadioButton					plotTypeScatter				= new JRadioButton("Scatter plot of all values.", false);
	private JRadioButton					plotTypeLineMean				= new JRadioButton("Plot mean of all values.", false);
	private JRadioButton					plotTypeLineMedian				= new JRadioButton("Plot median of all values.", true);
	private JRadioButton					plotTypeLineFirst				= new JRadioButton("Plot only first data.", false);
	private JRadioButton					plotTypeLineIdentity				= new JRadioButton("Use column info to identify curves.", false);
	private JComboBox<ColumnElement> lineIdentityCombo;
	
	private volatile TableDefinition lastTableDefinition = null;
	
	/**
	 * Constructor.
	 * @param positionInformation Position of component in measurement producing tables.
	 * @param tableDataProducer The job which is producing the table data.
	 * @param tableDefinition The definition of the table layout.
	 * @param client Interface to the YouScope client.
	 */
	public TableDataPlotterPanel(final PositionInformation positionInformation, TableProducer tableDataProducer, TableDefinition tableDefinition, YouScopeClient client)
	{
		super(new BorderLayout());
		this.client = client;
		this.tableDataProducer = tableDataProducer;
		
		yFieldList = new JList<ColumnElement>()
		{
			/**
			 * Serial Version UID.
			 */
			private static final long serialVersionUID = -8450462573547019690L;

			@Override
			public String getToolTipText (MouseEvent e)
			{
			      int index = locationToIndex (e.getPoint ());
			      if (index > -1)
			      {
			          ListModel<ColumnElement> listModel = getModel ();
			          ColumnElement element = listModel.getElementAt (index);
			          if(element == null)
			        	  return null;
			          return element.toHTML();
			      }
				return null;
			}
		};
		yFieldList.setToolTipText("");
		
		
		DefaultListCellRenderer cellRenderer = new DefaultListCellRenderer() {
	        /**
			 * Serial Version UID.
			 */
			private static final long serialVersionUID = 7000908855004795312L;

			@Override
			public java.awt.Component getListCellRendererComponent(JList<?> list, 
	                Object value, int index, boolean isSelected, boolean cellHasFocus ){
				javax.swing.JComponent element = (javax.swing.JComponent)super.getListCellRendererComponent( list,
	                    value, index, isSelected, cellHasFocus );
	            if ( value instanceof ColumnElement  && isSelected ) {
	                list.setToolTipText(((ColumnElement)value).toHTML());
	            }
	            else if (isSelected ) {
	                list.setToolTipText("");
	            }
	            return element;
	        }
	    };
	    xFieldCombo = new JComboBox<ColumnElement>();
		xFieldCombo.setRenderer(cellRenderer);

		lineIdentityCombo = new JComboBox<ColumnElement>();
		lineIdentityCombo.setRenderer(cellRenderer);
		 
		JScrollPane yFieldScrollPane = new JScrollPane(yFieldList);
		yFieldScrollPane.setPreferredSize(new Dimension(200, 50));
		 
		setBorder(new TitledBorder("Function Visualization"));
		
		GridBagConstraints newLineConstr = StandardFormats.getNewLineConstraint();
		GridBagConstraints bottomConstr = StandardFormats.getBottomContstraint();
		
		GridBagLayout xLayout = new GridBagLayout();
		JPanel xPanel = new JPanel(xLayout);
		StandardFormats.addGridBagElement(new JLabel("X-Value from:"), xLayout, newLineConstr, xPanel);
		ButtonGroup xValueFromGroup = new ButtonGroup();
		xValueFromGroup.add(xTimeField);
		xValueFromGroup.add(xEvaluationField);
		xValueFromGroup.add(xFromTableField);
		ActionListener isFromFieldListener = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				xFieldCombo.setEnabled(xFromTableField.isSelected());
			}
		};
		xFieldCombo.setEnabled(false);
		xTimeField.addActionListener(isFromFieldListener);
		xEvaluationField.addActionListener(isFromFieldListener);
		xFromTableField.addActionListener(isFromFieldListener);
		StandardFormats.addGridBagElement(xTimeField, xLayout, newLineConstr, xPanel);
		StandardFormats.addGridBagElement(xEvaluationField, xLayout, newLineConstr, xPanel);
		StandardFormats.addGridBagElement(xFromTableField, xLayout, newLineConstr, xPanel);
		StandardFormats.addGridBagElement(new JLabel("X-Column:"), xLayout, newLineConstr, xPanel);
		StandardFormats.addGridBagElement(xFieldCombo, xLayout, newLineConstr, xPanel);
		StandardFormats.addGridBagElement(new JPanel(), xLayout, bottomConstr, xPanel);
		
		GridBagLayout yLayout = new GridBagLayout();
		JPanel yPanel = new JPanel(yLayout);
		StandardFormats.addGridBagElement(new JLabel("Y-Columns:"), yLayout, newLineConstr, yPanel);
		StandardFormats.addGridBagElement(yFieldScrollPane, yLayout, bottomConstr, yPanel);
		
		GridBagLayout typeLayout = new GridBagLayout();
		JPanel typePanel = new JPanel(typeLayout);
		StandardFormats.addGridBagElement(new JLabel("How to deal with various table rows:"), typeLayout, newLineConstr, typePanel);
		ButtonGroup typeGroup = new ButtonGroup();
		typeGroup.add(plotTypeLineMedian);
		typeGroup.add(plotTypeLineMean);
		typeGroup.add(plotTypeScatter);
		typeGroup.add(plotTypeLineFirst);
		typeGroup.add(plotTypeLineIdentity);
		ActionListener plotTypeListener = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				lineIdentityCombo.setEnabled(plotTypeLineIdentity.isSelected());
			}
		};
		lineIdentityCombo.setEnabled(false);
		plotTypeScatter.addActionListener(plotTypeListener);
		plotTypeLineMedian.addActionListener(plotTypeListener);
		plotTypeLineMean.addActionListener(plotTypeListener);
		plotTypeLineFirst.addActionListener(plotTypeListener);
		plotTypeLineIdentity.addActionListener(plotTypeListener);
		StandardFormats.addGridBagElement(plotTypeLineMedian, typeLayout, newLineConstr, typePanel);
		StandardFormats.addGridBagElement(plotTypeLineMean, typeLayout, newLineConstr, typePanel);
		StandardFormats.addGridBagElement(plotTypeScatter, typeLayout, newLineConstr, typePanel);
		StandardFormats.addGridBagElement(plotTypeLineFirst, typeLayout, newLineConstr, typePanel);
		StandardFormats.addGridBagElement(plotTypeLineIdentity, typeLayout, newLineConstr, typePanel);
		StandardFormats.addGridBagElement(new JLabel("Identity-Column:"), typeLayout, newLineConstr, typePanel);
		StandardFormats.addGridBagElement(lineIdentityCombo, typeLayout, newLineConstr, typePanel);
		StandardFormats.addGridBagElement(new JPanel(), typeLayout, bottomConstr, typePanel);
		
		JPanel centralPanel = new JPanel(new GridLayout(1,3,5,5));
		centralPanel.add(xPanel);
		centralPanel.add(yPanel);
		centralPanel.add(typePanel);
		
		displayButton.setEnabled(false);
        displayButton.addActionListener(new ActionListener()
        {
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				String xColumnName = null;
				TableDataVisualizerFrame.XAxisType xAxisType;
				if(xEvaluationField.isSelected())
					xAxisType = TableDataVisualizerFrame.XAxisType.Evaluation;
				else if(xTimeField.isSelected())
					xAxisType = TableDataVisualizerFrame.XAxisType.TimeMin;
				else if(xFromTableField.isSelected())
				{
					xAxisType = TableDataVisualizerFrame.XAxisType.TableColumnValue;
					ColumnElement selectedElement = (ColumnElement) xFieldCombo.getSelectedItem();
					if(selectedElement == null)
						return;
					xColumnName = selectedElement.getName();
				}
				else
					return;
				
				List<ColumnElement> yColumnNamesList = yFieldList.getSelectedValuesList();
				String[] yColumnNames = new String[yColumnNamesList.size()];
				for(int i=0; i<yColumnNames.length; i++)
				{
					yColumnNames[i] = yColumnNamesList.get(i).getName();
				}
				
				TableDataPlotType plotType;
				String identityColumnName = null;
				if(plotTypeLineMedian.isSelected())
					plotType = TableDataPlotType.LineMedian;
				else if(plotTypeLineMean.isSelected())
					plotType = TableDataPlotType.LineMean;
				else if(plotTypeLineFirst.isSelected())
					plotType = TableDataPlotType.LineFirst;
				else if(plotTypeScatter.isSelected())
					plotType = TableDataPlotType.Scatter;
				else 
				{
					plotType = TableDataPlotType.LineIdentity;
					ColumnElement selectedElement = (ColumnElement) lineIdentityCombo.getSelectedItem();
					if(selectedElement == null)
						return;
					identityColumnName = selectedElement.getName();
				}
				
				YouScopeFrame frame = TableDataPlotterPanel.this.client.createFrame();
				TableDataVisualizerFrame visFrame = new TableDataVisualizerFrame(positionInformation, TableDataPlotterPanel.this.tableDataProducer, xAxisType, xColumnName, yColumnNames, plotType, identityColumnName, TableDataPlotterPanel.this.client);
				visFrame.createUI(frame);
				frame.setVisible(true);
			}
        });
		
		add(centralPanel, BorderLayout.CENTER);
		add(displayButton, BorderLayout.SOUTH);
		
		setTableDefinition(tableDefinition);
	}
	
	private static class ColumnElement
	{
		private final String name;
		private final String description;
		public ColumnElement(String name, String description)
		{
			this.name = name;
			this.description = description;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
		public String getName()
		{
			return name;
		}
		public String getDescription()
		{
			return description;
		}
		public String toHTML()
		{
			return "<html>"+TextTools.toHTML(getName(), getDescription())+"</html>";
		}
	}
	/**
	 * Notifies the panel that the available column headers changed. The UI is changed such that the columns can be selected for plotting.
	 * @param tableDefinition The new table information.
	 */
	public void setTableDefinition(final TableDefinition tableDefinition)
	{
		Runnable runner = new Runnable()
		{
			@Override
			public void run() 
			{
				if(lastTableDefinition != null && lastTableDefinition.equals(tableDefinition))
					return;
				lastTableDefinition = tableDefinition;
				xFieldCombo.removeAllItems();
				Vector<ColumnElement> yListElements = new Vector<ColumnElement>();
				for(ColumnDefinition<?> column : tableDefinition)
				{ 
					ColumnElement element = new ColumnElement(column.getColumnName(), column.getColumnDescription());
					if(Number.class.isAssignableFrom(column.getValueType()))
					{
						xFieldCombo.addItem(element);
						yListElements.add(element);
					}
					
					lineIdentityCombo.addItem(element);
					
				}
				
				if(yListElements.size() > 0)
					displayButton.setEnabled(true);
				yFieldList.setListData(yListElements);
			}
		};
		if(SwingUtilities.isEventDispatchThread())
			runner.run();
		else
			SwingUtilities.invokeLater(runner);
	}
}
