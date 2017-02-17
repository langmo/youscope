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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.task.TaskConfiguration;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * Panel which allows the user to add, remove and edit tasks of e.g. an advanced measurement.
 * @author Moritz Lang
 */
public class TasksDefinitionPanel extends JPanel
{

    /**
     * Serial Version UID.
     */
    private static final long serialVersionUID = -5426747514806820936L;

    private JList<String> taskList;

    private final ArrayList<TaskConfiguration> tasks = new ArrayList<TaskConfiguration>();
    
    private final YouScopeFrame frame;
    private YouScopeClient client; 
	private YouScopeServer server;
    /**
     * Constructor.
     * @param client Interface to the client.
     * @param server Interface to the server.
     * @param frame Frame in which this panel is added.
     */
    public TasksDefinitionPanel(YouScopeClient client, YouScopeServer server, YouScopeFrame frame)
    {
        super(new BorderLayout());
        this.frame = frame;
        this.client = client;
		this.server = server;

        // Load icons
        String addButtonFile = "icons/block--plus.png";
        String deleteButtonFile = "icons/block--minus.png";
        String editButtonFile = "icons/block--pencil.png";

        String upButtonFile = "icons/arrow-090.png";
        String downButtonFile = "icons/arrow-270.png";

        ImageIcon addButtonIcon = null;
        ImageIcon deleteButtonIcon = null;
        ImageIcon editButtonIcon = null;
        ImageIcon upButtonIcon = null;
        ImageIcon downButtonIcon = null;
        try
        {
            URL addButtonURL = getClass().getClassLoader().getResource(addButtonFile);
            if (addButtonURL != null)
                addButtonIcon = new ImageIcon(addButtonURL, "Add Job");

            URL deleteButtonURL = getClass().getClassLoader().getResource(deleteButtonFile);
            if (deleteButtonURL != null)
                deleteButtonIcon = new ImageIcon(deleteButtonURL, "Delete Job");

            URL editButtonURL = getClass().getClassLoader().getResource(editButtonFile);
            if (editButtonURL != null)
                editButtonIcon = new ImageIcon(editButtonURL, "Edit Job");

            URL upButtonURL = getClass().getClassLoader().getResource(upButtonFile);
            if (upButtonURL != null)
                upButtonIcon = new ImageIcon(upButtonURL, "Move upwards");

            URL downButtonURL = getClass().getClassLoader().getResource(downButtonFile);
            if (downButtonURL != null)
                downButtonIcon = new ImageIcon(downButtonURL, "Move downwards");

        } catch (@SuppressWarnings("unused") Exception e)
        {
            // Do nothing.
        }

        JButton newTaskButton = new JButton("New Task", addButtonIcon);
        newTaskButton.setHorizontalAlignment(SwingConstants.LEFT);
        newTaskButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	YouScopeFrame newFrame = TasksDefinitionPanel.this.frame.createModalChildFrame();
                	TaskConfigurationPanel configFrame = new TaskConfigurationPanel(TasksDefinitionPanel.this.client, TasksDefinitionPanel.this.server, TasksDefinitionPanel.this.frame);
                	configFrame.showInFrame(newFrame, "Add Task", new TaskConfigurationListenerImpl(-1));
                    newFrame.setVisible(true);
                }
            });

        JButton deleteTaskButton = new JButton("Delete Task", deleteButtonIcon);
        deleteTaskButton.setHorizontalAlignment(SwingConstants.LEFT);
        deleteTaskButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    int idx = taskList.getSelectedIndex();
                    if (idx == -1)
                        return;
                    TasksDefinitionPanel.this.tasks.remove(idx);
                    refreshTaskList();
                }
            });

        JButton editTaskButton = new JButton("Edit Task", editButtonIcon);
        editTaskButton.setHorizontalAlignment(SwingConstants.LEFT);
        editTaskButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    int idx = taskList.getSelectedIndex();
                    if (idx == -1)
                        return;
                    TaskConfiguration task = TasksDefinitionPanel.this.tasks.get(idx);
                    
                    YouScopeFrame newFrame = TasksDefinitionPanel.this.frame.createModalChildFrame();
                	TaskConfigurationPanel configFrame = new TaskConfigurationPanel(TasksDefinitionPanel.this.client, TasksDefinitionPanel.this.server, TasksDefinitionPanel.this.frame);
                	configFrame.setConfigurationData(task);
                	configFrame.showInFrame(newFrame, "Edit Task", new TaskConfigurationListenerImpl(idx));
                    newFrame.setVisible(true);
                }
            });

        add(new JLabel("Tasks:"), BorderLayout.NORTH);

        // add Button Panel
        JPanel centralPanel = new JPanel(new BorderLayout(2, 2));
        GridBagLayout elementsLayout = new GridBagLayout();
        GridBagConstraints newLineConstr = new GridBagConstraints();
        newLineConstr.fill = GridBagConstraints.HORIZONTAL;
        newLineConstr.gridwidth = GridBagConstraints.REMAINDER;
        newLineConstr.anchor = GridBagConstraints.NORTHWEST;
        newLineConstr.gridx = 0;
        newLineConstr.weightx = 1.0;
        newLineConstr.weighty = 0;
        GridBagConstraints bottomConstr = new GridBagConstraints();
        bottomConstr.weighty = 1.0;

        JPanel jobButtonPanel = new JPanel(elementsLayout);
        StandardFormats.addGridBagElement(newTaskButton, elementsLayout, newLineConstr, jobButtonPanel);
        StandardFormats.addGridBagElement(editTaskButton, elementsLayout, newLineConstr, jobButtonPanel);
        StandardFormats.addGridBagElement(deleteTaskButton, elementsLayout, newLineConstr, jobButtonPanel);
        StandardFormats.addGridBagElement(new JPanel(), elementsLayout, bottomConstr, jobButtonPanel);

        add(jobButtonPanel, BorderLayout.EAST);

        taskList = new JList<String>();
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane taskListPane = new JScrollPane(taskList);
        taskListPane.setPreferredSize(new Dimension(250, 70));
        taskListPane.setMinimumSize(new Dimension(10, 10));
        centralPanel.add(taskListPane, BorderLayout.CENTER);

        JButton upButton;
        if (upButtonIcon == null)
            upButton = new JButton("Up");
        else
            upButton = new JButton(upButtonIcon);
        JButton downButton;
        if (downButtonIcon == null)
            downButton = new JButton("Down");
        else
            downButton = new JButton(downButtonIcon);
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
        JPanel upDownPanel = new JPanel(new GridLayout(1, 2, 2, 2));
        upDownPanel.add(upButton);
        upDownPanel.add(downButton);
        centralPanel.add(upDownPanel, BorderLayout.SOUTH);
        add(centralPanel, BorderLayout.CENTER);

        refreshTaskList();
    }

    private void moveUpDown(boolean moveUp)
    {
        int idx = taskList.getSelectedIndex();
        if (idx == -1 || (moveUp && idx == 0)
                || (!moveUp && idx + 1 >= tasks.size()))
            return;
        int newIdx;
        if (moveUp)
            newIdx = idx - 1;
        else
            newIdx = idx + 1;
        TaskConfiguration task = tasks.get(idx);
        tasks.remove(idx);
        tasks.add(newIdx, task);
        refreshTaskList();
    }
    
    /**
     * Sets the tasks this panel displays.
     * @param tasks Tasks to be displayed.
     */
    public void setTasks(TaskConfiguration[] tasks)
    {
    	this.tasks.clear();
    	for(TaskConfiguration task : tasks)
    	{
    		this.tasks.add(task);
    	}
    	refreshTaskList();
    }
    
    /**
     * Returns configured tasks.
     * @return Tasks.
     */
    public TaskConfiguration[] getTasks()
    {
    	return tasks.toArray(new TaskConfiguration[tasks.size()]);
    }

    private class TaskConfigurationListenerImpl implements TaskConfigurationListener
    {
    	private final int idx;
    	TaskConfigurationListenerImpl(int idx)
    	{
    		this.idx = idx;
    	}
	    @Override
	    public void taskConfigurationFinished(TaskConfiguration task)
	    {
	    	if(idx < 0)
	    		tasks.add(task);
	    	else
	    	{
	    		tasks.remove(idx);
	    		tasks.add(idx, task);
	    	}
	    	refreshTaskList();
	    }
    }
	    
    private void refreshTaskList()
    {
        Vector<String> taskDescriptions = new Vector<String>();
        for (int i = 0; i < tasks.size(); i++)
        {
            taskDescriptions.add("<html><body>"
                    + tasks.get(i).getDescription() + "</body></html>");
        }
        taskList.setListData(taskDescriptions);
    }

}
