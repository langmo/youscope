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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.task.PeriodConfiguration;
import org.youscope.common.task.RegularPeriodConfiguration;
import org.youscope.common.task.TaskConfiguration;
import org.youscope.common.task.VaryingPeriodConfiguration;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * A window to edit tasks.
 * @author langmo
 */
public class TaskConfigurationPanel extends JPanel
{

	/**
	 * Serial Version UID
	 */
	private static final long				serialVersionUID		= 2022550025726436992L;

	private final JobsDefinitionPanel jobPanel;
	
	private final JLabel periodLabel = new JLabel("Period between successive executions:");
	private final PeriodField			periodField				= new PeriodField();

	private final PeriodField			firstEvaluationField	= new PeriodField();

	private final JRadioButton					stopByUserRadio				= new JRadioButton("When measurement finishes.", false);
	private final JRadioButton					stopByExecutionsRadio		= new JRadioButton("After a given number of executions of the task's jobs.", false);
	private final JLabel							numExecutionsFieldLabel	= new JLabel("Number of Executions:");
	private final JFormattedTextField				numExecutionsField		= new JFormattedTextField(StandardFormats.getIntegerFormat());
	
	private final JRadioButton					burstRadio				= new JRadioButton("As fast as possible / burst.", false);
	private final JRadioButton					dynamicPeriodRadio				= new JRadioButton("Dynamic period (End execution i -> Start execution i+1).", false);
	private final JRadioButton					fixedPeriodRadio				= new JRadioButton("Fixed period (Start execution i -> Start execution i+1).", false);
	private final JRadioButton					varyingPeriodRadio				= new JRadioButton("Varying period.", false);
	private final PeriodVaryingPanel				periodVaryingDataPanel	= new PeriodVaryingPanel();
	
	private YouScopeFrame parentFrame = null;
	
	private final YouScopeClient client;
	
	/**
	 * Constructor.
	 * @param client Interface to the client.
	 * @param server Interface to the server.
	 * @param parentFrame Frame in which this panel is added, or null (if null, non-modal sub-frames are opened).
	 */
	public TaskConfigurationPanel(YouScopeClient client, YouScopeServer server, YouScopeFrame parentFrame)
	{
		this.client = client;
		this.parentFrame = parentFrame;
		
		DynamicPanel leftPanel = new DynamicPanel();
				
		leftPanel.add(new JLabel("Task finishes:"));
		ButtonGroup stopConditionGroup = new ButtonGroup();
		stopConditionGroup.add(stopByUserRadio);
		stopConditionGroup.add(stopByExecutionsRadio);
		class StopTypeChangedListener implements ActionListener
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(stopByUserRadio.isSelected())
				{
					numExecutionsFieldLabel.setVisible(false);
					numExecutionsField.setVisible(false);
				}
				else if(stopByExecutionsRadio.isSelected())
				{
					numExecutionsFieldLabel.setVisible(true);
					numExecutionsField.setVisible(true);
				}
				if(TaskConfigurationPanel.this.parentFrame != null)
					TaskConfigurationPanel.this.parentFrame.pack();
			}
		}
		stopByUserRadio.addActionListener(new StopTypeChangedListener());
		stopByExecutionsRadio.addActionListener(new StopTypeChangedListener());

		leftPanel.add(stopByUserRadio);
		leftPanel.add(stopByExecutionsRadio);
		leftPanel.add(numExecutionsFieldLabel);
		leftPanel.add(numExecutionsField);
		
		leftPanel.add(new JLabel("Period between successive executions of the task's jobs:"));
		leftPanel.add(burstRadio);
		leftPanel.add(fixedPeriodRadio);
		leftPanel.add(dynamicPeriodRadio);
		leftPanel.add(varyingPeriodRadio);
		leftPanel.add(periodLabel);
		leftPanel.add(periodField);
		leftPanel.add(periodVaryingDataPanel);

		ButtonGroup periodGroup = new ButtonGroup();
		periodGroup.add(burstRadio);
		periodGroup.add(fixedPeriodRadio);
		periodGroup.add(dynamicPeriodRadio);
		periodGroup.add(varyingPeriodRadio);
		class PeriodTypeChangedListener implements ActionListener
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(burstRadio.isSelected())
				{
					periodField.setVisible(false);
					periodLabel.setVisible(false);
					periodVaryingDataPanel.setVisible(false);
				}
				else if(fixedPeriodRadio.isSelected() || dynamicPeriodRadio.isSelected())
				{
					periodField.setVisible(true);
					periodLabel.setVisible(true);
					periodVaryingDataPanel.setVisible(false);
				}
				else if(varyingPeriodRadio.isSelected())
				{
					periodField.setVisible(false);
					periodLabel.setVisible(false);
					periodVaryingDataPanel.setVisible(true);
				}
				if(TaskConfigurationPanel.this.parentFrame != null)
					TaskConfigurationPanel.this.parentFrame.pack();
			}
		}
		burstRadio.addActionListener(new PeriodTypeChangedListener());
		fixedPeriodRadio.addActionListener(new PeriodTypeChangedListener());
		dynamicPeriodRadio.addActionListener(new PeriodTypeChangedListener());
		varyingPeriodRadio.addActionListener(new PeriodTypeChangedListener());
		
		leftPanel.add(new JLabel("Delay until first execution of the task's jobs:"));
		leftPanel.add(firstEvaluationField);
		leftPanel.addFillEmpty();
		leftPanel.setBorder(new TitledBorder("Task Execution Settings"));
		
		JPanel rightPanel = new JPanel(new BorderLayout(5,5));
		rightPanel.add(new JLabel("Imaging protocol:"), BorderLayout.NORTH);		
		jobPanel = new JobsDefinitionPanel(client, server, parentFrame);
		rightPanel.add(jobPanel, BorderLayout.CENTER);
		rightPanel.setBorder(new TitledBorder("Definition of the Task's jobs"));
		
		// Initialize look and feel
		setConfigurationData(new TaskConfiguration());

		setLayout(new GridLayout(1,2,5,5));
		add(leftPanel);
		add(rightPanel);
	}
	
	/**
     * Initializes the frame to the configuration data.
     * 
     * @param taskConfiguration The configuration data. 
     */
    public void setConfigurationData(TaskConfiguration taskConfiguration)
    {
    	if(taskConfiguration == null)
    		throw new IllegalArgumentException("Task must not be null.");
    	
    	PeriodConfiguration period = taskConfiguration.getPeriod();
    	if(period == null)
    		period = new RegularPeriodConfiguration();
    	
    	if(period.getNumExecutions() <= 0)
    	{
    		stopByUserRadio.doClick();
    		numExecutionsField.setValue(1);
    	}
    	else
    	{
    		stopByExecutionsRadio.doClick();
    		numExecutionsField.setValue(period.getNumExecutions());
    	}
    	
    	firstEvaluationField.setDuration(period.getStartTime());
    		
    	if(period instanceof RegularPeriodConfiguration)
		{
    		RegularPeriodConfiguration regularPeriod = (RegularPeriodConfiguration)period;
    		if(regularPeriod.isFixedTimes())
    		{
    			fixedPeriodRadio.doClick();
    			periodField.setDuration(regularPeriod.getPeriod());
    		}
    		else
    		{
    			if(regularPeriod.getPeriod() <= 0)
    			{
    				burstRadio.doClick();
    				periodField.setDuration(10 * 1000);
    			}
    			else
    			{
    				dynamicPeriodRadio.doClick();
    				periodField.setDuration(regularPeriod.getPeriod());
    			}
    		}
		}
		else if(period instanceof VaryingPeriodConfiguration)
		{
			VaryingPeriodConfiguration varyingPeriod = (VaryingPeriodConfiguration)period;
			periodVaryingDataPanel.setPeriod(varyingPeriod);
			periodField.setDuration(10 * 1000);
			varyingPeriodRadio.doClick();
		}
		else
		{
			client.sendError("Only regular and varying periods are supported. Period type was " + period.getClass().toString() + ".");
		}
    	
    	jobPanel.setJobs(taskConfiguration.getJobs());
    }
    
    /**
     * Returns the configuration data.
     * 
     * @return Configuration data of this task.
     */
    public TaskConfiguration getConfigurationData()
    {
    	TaskConfiguration taskConfiguration = new TaskConfiguration();
    	taskConfiguration.setJobs(jobPanel.getJobs());
    	
    	PeriodConfiguration period;
    	if(burstRadio.isSelected())
    	{
    		RegularPeriodConfiguration regularPeriod = new RegularPeriodConfiguration();
    		regularPeriod.setFixedTimes(false);
    		regularPeriod.setPeriod(0);
    		
    		period = regularPeriod;
    	}
    	else if(dynamicPeriodRadio.isSelected())
    	{
    		RegularPeriodConfiguration regularPeriod = new RegularPeriodConfiguration();
    		regularPeriod.setFixedTimes(false);
    		regularPeriod.setPeriod(periodField.getDuration());
    		
    		period = regularPeriod;
    	}
    	else if(fixedPeriodRadio.isSelected())
    	{
    		RegularPeriodConfiguration regularPeriod = new RegularPeriodConfiguration();
    		regularPeriod.setFixedTimes(true);
    		regularPeriod.setPeriod(periodField.getDuration());
    		
    		period = regularPeriod;
    	}
    	else
    	{
    		// varying periods
    		period = periodVaryingDataPanel.getPeriod();
    	}
    	
    	if(stopByUserRadio.isSelected())
    	{
    		period.setNumExecutions(-1);
    	}
    	else
    	{
    		period.setNumExecutions(((Number)numExecutionsField.getValue()).intValue());
    	}
    	period.setStartTime(firstEvaluationField.getDuration());
    	
    	taskConfiguration.setPeriod(period);
    	
		return taskConfiguration;
    }
    
    /**
	 * Convenient method to show this panel in a frame with a close button. The frame is not made visible.
	 * @param frame The frame in which the panel should be shown.
	 * @param title The title of the frame.
     * @param configurationListener Listener which gets notified if task should be added.
	 */
    public void showInFrame(YouScopeFrame frame, String title, TaskConfigurationListener configurationListener)
	{
		frame.setTitle(title);
		frame.setResizable(true);
		frame.setClosable(true);
		frame.setMaximizable(false);
		
		JButton addTaskButton = new JButton("Add Task");
		class AddTaskListener implements ActionListener
		{
			private final YouScopeFrame frame;
			private final TaskConfigurationListener configurationListener;
			public AddTaskListener(YouScopeFrame frame, TaskConfigurationListener configurationListener)
			{
				this.frame = frame;
				this.configurationListener = configurationListener;
			}
			@Override
			public void actionPerformed(ActionEvent e)
			{
				configurationListener.taskConfigurationFinished(getConfigurationData());
				
				try
				{
					frame.setVisible(false);
				}
				catch (Exception e1)
				{
					client.sendError("Could not close frame.", e1);
				}
			}
		}
		
		addTaskButton.addActionListener(new AddTaskListener(frame, configurationListener));
		
		JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(this, BorderLayout.CENTER);
        contentPane.add(addTaskButton, BorderLayout.SOUTH);
        frame.setContentPane(contentPane);
        frame.pack();
        parentFrame = frame;
	}

}
