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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonUI;
import org.youscope.addon.component.ComponentAddonUIListener;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.JobConfiguration;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * Panel allowing the user to add, edit and delete jobs to a list of jobs.
 * @author Moritz Lang
 */
public class JobsDefinitionPanel extends JPanel
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 3348463708545771385L;

    private JList<String> jobList;

    private ComponentComboBox<JobConfiguration> newJobField = null;
    
    private static final String DEFAULT_IMAGING_JOB_ID_SINGLE = "YouScope.ImagingJob";
    private static final String DEFAULT_IMAGING_JOB_ID_MULTI = "YouScope.ParallelImagingJob";
    
    private final Vector<JobConfiguration> jobs = new Vector<JobConfiguration>();
    /**
     * Constructor.
     * @param client Interface to the client.
     * @param server Interface to the server.
     * @param parentFrame Frame in which this panel is added, or null (if null, non-modal sub-frames are opened).
     */
    public JobsDefinitionPanel(YouScopeClient client, YouScopeServer server, YouScopeFrame parentFrame)
    {
    	this(client, server, parentFrame, null);
    }
    /**
     * Constructor. Only shows jobs which implement a given interface (e.g. ImageProducerConfiguration).
     * @param client Interface to the client.
     * @param server Interface to the server.
     * @param parentFrame Frame in which this panel is added, or null (if null, non-modal sub-frames are opened).
     * @param jobInterface the interface which the jobs should implement.
     */
    public JobsDefinitionPanel(final YouScopeClient client, YouScopeServer server, final YouScopeFrame parentFrame, Class<?> jobInterface)
    {
        super(new BorderLayout());
        setOpaque(false);

        // Load icons
        Icon defaultAddButtonIcon = ImageLoadingTools.getResourceIcon("icons/block--plus.png", "Add Job");
        Icon deleteButtonIcon = ImageLoadingTools.getResourceIcon("icons/block--minus.png", "Delete Job");
        Icon editButtonIcon = ImageLoadingTools.getResourceIcon("icons/block--pencil.png", "Edit Job");
        Icon upButtonIcon = ImageLoadingTools.getResourceIcon("icons/arrow-090.png", "Move upwards");
        Icon downButtonIcon = ImageLoadingTools.getResourceIcon("icons/arrow-270.png", "Move downwards");

        // Add Buttons
        DynamicPanel buttonsPanel = new DynamicPanel();
        // Add extra button for imaging job, since this is the most needed one
        boolean multiCam;
        try
        {
        	multiCam = server.getMicroscope().getCameraDevices().length > 1;
        }
        catch(@SuppressWarnings("unused") RemoteException e)
        {
        	// ignore, not so important...
        	multiCam = false;
        }
        // Check if multi cam job is available.
        if(multiCam)
        {
	        try
	        {
	        	client.getAddonProvider().getComponentMetadata(DEFAULT_IMAGING_JOB_ID_MULTI);
	        }
	        catch(@SuppressWarnings("unused") Exception e)
	        {
	        	multiCam = false;
	        }
        }
        
        final String imagingJobID;
        if(multiCam)
        {
        	imagingJobID = DEFAULT_IMAGING_JOB_ID_MULTI;
        }
        else
        {
        	// either single cam or multi cam imaging not available.
        	imagingJobID = DEFAULT_IMAGING_JOB_ID_SINGLE;
        }
        JButton newImagingJobButton = new JButton("New Imaging Job", defaultAddButtonIcon);
    	newImagingJobButton.setHorizontalAlignment(SwingConstants.LEFT);
    	newImagingJobButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) {
				ComponentAddonUI<? extends JobConfiguration> addon;
				try {
					addon = client.getAddonProvider().createComponentUI(imagingJobID, JobConfiguration.class);
				} catch (AddonException e1) {
					client.sendError("Could not create imaging job configuration UI.", e1);
					return;
				}
        		addon.addUIListener(new ComponentAddonUIListener<JobConfiguration>() 
        		{
					@Override
					public void configurationFinished(JobConfiguration configuration) 
					{
						jobs.addElement(configuration);
				        refreshJobList();
				        jobList.setSelectedIndex(jobs.size()-1);
					}
				});
        		YouScopeFrame frame;
				try {
					frame = addon.toFrame();
				} catch (AddonException e1) {
					client.sendError("Could not create imaging job configuration UI.", e1);
					return;
				}
        		parentFrame.addModalChildFrame(frame);
        		frame.setVisible(true);
			}
		});
    	buttonsPanel.add(newImagingJobButton);
        	
        
        // All other jobs
        newJobField = new ComponentComboBox<JobConfiguration>(client, JobConfiguration.class);
        newJobField.setText("New Job");
        newJobField.setIcon(defaultAddButtonIcon);
        newJobField.setPopupLocation(SwingConstants.RIGHT);
        newJobField.addActionListener(new ActionListener()
		{
        	@Override
			public void actionPerformed(ActionEvent arg0)
            {
        		String typeIdentifier = newJobField.getSelectedTypeIdentifier();
        		if(typeIdentifier == null)
        			return;
        		ComponentAddonUI<? extends JobConfiguration> addon;
				try {
					addon = client.getAddonProvider().createComponentUI(typeIdentifier, JobConfiguration.class);
				} catch (AddonException e) {
					client.sendError("Could not create job configuration UI for configurations with type identifiers "+typeIdentifier+".", e);
					return;
				}
        		addon.addUIListener(new ComponentAddonUIListener<JobConfiguration>() 
        		{
					@Override
					public void configurationFinished(JobConfiguration configuration) 
					{
						jobs.addElement(configuration);
				        refreshJobList();
				        jobList.setSelectedIndex(jobs.size()-1);
					}
				});
        		YouScopeFrame frame;
				try {
					frame = addon.toFrame();
				} catch (AddonException e) {
					client.sendError("Could not create job configuration UI for configurations with type identifiers "+typeIdentifier+".", e);
					return;
				}
        		parentFrame.addModalChildFrame(frame);
        		frame.setVisible(true);
            }
		});
        buttonsPanel.add(newJobField);

        JPanel emptyPanel = new JPanel();
        emptyPanel.setOpaque(false);
        buttonsPanel.add(emptyPanel);

        JButton editJobButton = new JButton("Edit Job", editButtonIcon);
        editJobButton.setHorizontalAlignment(SwingConstants.LEFT);
        editJobButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    final int idx = jobList.getSelectedIndex();
                    if (idx == -1)
                        return;
                    JobConfiguration job = jobs.elementAt(idx);
                    
                    ComponentAddonUI<? extends JobConfiguration> addon;
					try {
						addon = client.getAddonProvider().createComponentUI(job);
					} catch (AddonException e1) {
						client.sendError("Could not create job configuration UI for configurations with type identifiers "+job.getTypeIdentifier()+".", e1);
						return;
					} catch (ConfigurationException e1) {
						client.sendError("Configuration of job with type identifier "+job.getTypeIdentifier()+" invalid.", e1);
						return;
					}
            		addon.addUIListener(new ComponentAddonUIListener<JobConfiguration>() 
            		{
    					@Override
    					public void configurationFinished(JobConfiguration configuration) 
    					{
    						jobs.set(idx, configuration);
    				        refreshJobList();
    				        jobList.setSelectedIndex(idx);
    					}
    				});
            		YouScopeFrame frame;
					try {
						frame = addon.toFrame();
					} catch (AddonException e1) {
						client.sendError("Could not create job configuration UI for configurations with type identifiers "+job.getTypeIdentifier()+".", e1);
						return;
					}
            		parentFrame.addModalChildFrame(frame);
            		frame.setVisible(true);
                }
            });
        buttonsPanel.add(editJobButton);

        JButton deleteJobButton = new JButton("Delete Job", deleteButtonIcon);
        deleteJobButton.setHorizontalAlignment(SwingConstants.LEFT);
        deleteJobButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    int idx = jobList.getSelectedIndex();
                    if (idx == -1)
                        return;
                    jobs.removeElementAt(idx);
                    refreshJobList();
                }
            });
        buttonsPanel.add(deleteJobButton);

        emptyPanel = new JPanel();
        emptyPanel.setOpaque(false);
        buttonsPanel.add(emptyPanel);

        JButton upButton = new JButton("Move Up", upButtonIcon);
        upButton.setHorizontalAlignment(SwingConstants.LEFT);
        upButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent arg0)
                {
                    moveUpDown(true);
                }
            });
        buttonsPanel.add(upButton);

        JButton downButton = new JButton("Move Down", downButtonIcon);
        downButton.setHorizontalAlignment(SwingConstants.LEFT);
        downButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent arg0)
                {
                    moveUpDown(false);
                }
            });
        buttonsPanel.add(downButton);
        buttonsPanel.addFillEmpty();
        add(buttonsPanel, BorderLayout.EAST);

        // Add Job Panel
        jobList = new JList<String>();
        jobList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane jobListPane = new JScrollPane(jobList);
        jobListPane.setPreferredSize(new Dimension(250, 70));
        jobListPane.setMinimumSize(new Dimension(10, 10));
        add(jobListPane, BorderLayout.CENTER);
    }
    
    private void moveUpDown(boolean moveUp)
    {
        int idx = jobList.getSelectedIndex();
        if (idx == -1 || (moveUp && idx == 0)
                || (!moveUp && idx + 1 >= jobs.size()))
            return;
        int newIdx;
        if (moveUp)
            newIdx = idx - 1;
        else
            newIdx = idx + 1;
        JobConfiguration job = jobs.elementAt(idx);
        jobs.removeElementAt(idx);
        jobs.insertElementAt(job, newIdx);
        refreshJobList();
        jobList.setSelectedIndex(newIdx);
    }

    /**
     * Sets the jobs this panel displays.
     * @param jobs Jobs to be displayed.
     */
    public void setJobs(JobConfiguration[] jobs)
    {
    	this.jobs.removeAllElements();
    	for(JobConfiguration job : jobs)
    	{
    		this.jobs.addElement(job);
    	}
    	refreshJobList();
    }
    
    /**
     * Returns a list with all configured jobs.
     * @return List of jobs.
     */
    public JobConfiguration[] getJobs()
    {
    	return jobs.toArray(new JobConfiguration[jobs.size()]);
    }
    
    private void refreshJobList()
    {
        Vector<String> jobDescriptions = new Vector<String>();
        for (JobConfiguration job : jobs)
        {
            jobDescriptions.add("<html>" + job.getDescription() + "</html>");
        }
        jobList.setListData(jobDescriptions);
    }
}
