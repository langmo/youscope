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
package org.youscope.plugin.lifecelldetection;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.PeriodField;
import org.youscope.uielements.TimeUnit;

class MiscPanel extends DynamicPanel
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 6928966934371820511L;
	private final JCheckBox								saveSegmentationImagesField			= new JCheckBox("Save segmentation images", true);
	private final JTextField							segmentationImageNameField				= new JTextField();
	private final JLabel								segmentationImageNameLabel				= new JLabel("Segmentation image name:");
    
	private final JCheckBox								saveControlImagesField			= new JCheckBox("Save control images", true);
	private final JTextField							controlImageNameField				= new JTextField();
	private final JLabel								controlImageNameLabel				= new JLabel("Control image name:");
	
	private final JLabel								cellTableLabel				= new JLabel("Cell-table file name (without extension):");
	private final JTextField							cellTableField				= new JTextField();
	private final JCheckBox								saveCellTableField			= new JCheckBox("Save cell-table", true);
	
	private final JCheckBox								waitMinimalExecutionTimeField			= new JCheckBox("Enforce minimal execution time", false);
	private final JLabel								minimalExecutionTimeLabel	= new JLabel("Minimal execution time:");
	private final PeriodField 							minimalExecutionTimeField = new PeriodField(new TimeUnit[]{TimeUnit.MILLI_SECOND, TimeUnit.SECOND, TimeUnit.MINUTE});
		
	public MiscPanel(CellDetectionJobConfiguration configuration, final YouScopeFrame parentFrame) 
	{
		saveCellTableField.setOpaque(false);
		add(saveCellTableField);
		add(cellTableLabel);
		add(cellTableField);
		
		saveSegmentationImagesField.setOpaque(false);
		add(saveSegmentationImagesField);
		add(segmentationImageNameLabel);
		add(segmentationImageNameField);
		
		saveControlImagesField.setOpaque(false);
		add(saveControlImagesField);
		add(controlImageNameLabel);
		add(controlImageNameField);
		
		waitMinimalExecutionTimeField.setOpaque(false);
		add(waitMinimalExecutionTimeField);
		add(minimalExecutionTimeLabel);
		add(minimalExecutionTimeField);
		
		waitMinimalExecutionTimeField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				boolean selected = waitMinimalExecutionTimeField.isSelected();
				minimalExecutionTimeLabel.setVisible(selected);
				minimalExecutionTimeField.setVisible(selected);
				parentFrame.pack();
			}
		});		
		
		saveCellTableField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				boolean selected = saveCellTableField.isSelected();
				
				cellTableLabel.setVisible(selected);
				cellTableField.setVisible(selected);
				parentFrame.pack();
				
			}
		});
		
		saveSegmentationImagesField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				boolean selected = saveSegmentationImagesField.isSelected();
				segmentationImageNameLabel.setVisible(selected);
				segmentationImageNameField.setVisible(selected);
				parentFrame.pack();
			}
		});
		
		saveControlImagesField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				boolean selected = saveControlImagesField.isSelected();
				controlImageNameLabel.setVisible(selected);
				controlImageNameField.setVisible(selected);
				parentFrame.pack();
			}
		});
		
		String segmentationImageName = configuration.getSegmentationImageSaveName();
		if(segmentationImageName == null)
		{
			segmentationImageNameField.setText("segmentation");
			saveSegmentationImagesField.setSelected(false);
			segmentationImageNameField.setVisible(false);
			segmentationImageNameLabel.setVisible(false);
		}
		else
		{
			if (segmentationImageName.length() < 1)
			{
				segmentationImageName = "segmentation";
			}
			segmentationImageNameField.setText(segmentationImageName);
			saveSegmentationImagesField.setSelected(true);
			segmentationImageNameField.setVisible(true);
			segmentationImageNameLabel.setVisible(true);
		}
		
		String controlImageName = configuration.getControlImageSaveName();
		if(controlImageName == null)
		{
			controlImageNameField.setText("control");
			saveControlImagesField.setSelected(false);
		}
		else
		{
			if (controlImageName.length() < 1)
			{
				controlImageName = "control";
			}
			controlImageNameField.setText(controlImageName);
			saveControlImagesField.setSelected(true);
		}
		
		long minExecTime = configuration.getMinimalTimeMS();
		if(minExecTime <= 0)
		{
			minimalExecutionTimeField.setDuration(10000);
			waitMinimalExecutionTimeField.setSelected(false);
			minimalExecutionTimeField.setVisible(false);
			minimalExecutionTimeLabel.setVisible(false);
		}
		else
		{
			minimalExecutionTimeField.setDuration((int)minExecTime);
			waitMinimalExecutionTimeField.setSelected(true);
			minimalExecutionTimeField.setVisible(true);
			minimalExecutionTimeLabel.setVisible(true);
		}
		
		String cellTableName = configuration.getCellTableSaveName();
		if(cellTableName == null)
		{
			cellTableField.setText("cell-table");
			saveCellTableField.setSelected(false);
			cellTableField.setVisible(false);
			cellTableLabel.setVisible(false);
		}
		else
		{
			if (cellTableName.length() < 1)
			{
				cellTableName = "cell-table";
			}
			cellTableField.setText(cellTableName);
			saveCellTableField.setSelected(true);
			cellTableField.setVisible(true);
			cellTableLabel.setVisible(true);
		}
	}
	public void commitChanges(CellDetectionJobConfiguration configuration)
	{
		configuration.setMinimalTimeMS(waitMinimalExecutionTimeField.isSelected() ? minimalExecutionTimeField.getDuration() : -1);
    	configuration.setSegmentationImageSaveName(saveSegmentationImagesField.isSelected() ? segmentationImageNameField.getText() : null);
    	configuration.setControlImageSaveName(saveControlImagesField.isSelected() ? controlImageNameField.getText() : null);
		configuration.setCellTableSaveName(saveCellTableField.isSelected() ? cellTableField.getText() : null);
	}
}
