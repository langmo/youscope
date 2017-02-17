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
package org.youscope.plugin.livemodifiablejob;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.callback.CallbackException;
import org.youscope.serverinterfaces.YouScopeServer;

class LiveModifiableJobCallbackImpl extends UnicastRemoteObject implements LiveModifiableJobCallback
{    
    /**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -8709710643216637921L;
	private final YouScopeClient client;
    private final YouScopeServer server;
    private volatile YouScopeFrame frame = null;
    private final JList<JobHolder> jobHolderList = new JList<JobHolder>();
    private final ArrayList<JobHolder> jobHolders = new ArrayList<JobHolder>();
    private volatile boolean jobsChanged = false;
    private final JPanel centralPanel = new JPanel(new BorderLayout());
    private JobControlPanel currentJobControlPanel = null;
    private int numRegistered = 0;
    
    LiveModifiableJobCallbackImpl(final YouScopeClient client, final YouScopeServer server) throws RemoteException
    {
        this.client = client;
        this.server = server;
    }    
    
    private synchronized void setupUI()
    {
    	if(frame != null)
    	{
    		frame.setVisible(true);
    		return;
    	}
    	
        this.frame = client.createFrame();
        frame.setTitle("Live Job Manipulation");
        frame.setClosable(false);
        
        jobHolderList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jobHolderList.addListSelectionListener(new ListSelectionListener()
            {
                @Override
                public void valueChanged(ListSelectionEvent e)
                {
                    JobHolder jobHolder;
                    synchronized(jobHolders)
                    {
                        jobHolder = jobHolderList.getSelectedValue();
                    }
                    if(jobHolder == null || jobHolder.getJob() == null)
                        return;
                    centralPanel.removeAll();
                    try
                    {
                        currentJobControlPanel = new JobControlPanel(client, server, frame, jobHolder);
                        centralPanel.add(currentJobControlPanel);
                    } catch (Exception e1)
                    {
                        centralPanel.add(new JLabel("<html><center><p style=\"color=#ffffff\">Cannot control job<br />Reason:" + e1.getMessage() + ".</p></center></html>"));
                    }
                    centralPanel.revalidate();
                }
            });
        jobHolderList.setBorder(new EmptyBorder(5, 5, 5, 5));
        JPanel jobHolderListPanel = new JPanel(new BorderLayout());
        jobHolderListPanel.add(new JScrollPane(jobHolderList), BorderLayout.CENTER);
        jobHolderListPanel.setBorder(new TitledBorder("Jobs"));
        jobHolderListPanel.setMinimumSize(new Dimension(150, 100));
        jobHolderListPanel.setPreferredSize(new Dimension(150, 100));
        centralPanel.add(new JLabel("<html><center><p style=\"color=#ffffff\">No Job Selected<br />Select job to modify it.</p></center></html>"), BorderLayout.CENTER);
        final JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(jobHolderListPanel, BorderLayout.WEST);
        contentPane.add(centralPanel, BorderLayout.CENTER);
        frame.setContentPane(contentPane);
        frame.setSize(new Dimension(600, 480));
        frame.setVisible(true);
    }
    
    @Override
	public void registerJob(LiveModifiableJob job)
    {
    	synchronized(jobHolders)
        {
            jobHolders.add(new JobHolder(job));
            jobsChanged = true;
        }
        jobsUpdated();
    }
    private void jobsUpdated()
    {
        Runnable runner = new Runnable()
        {
            @Override
            public void run()
            {
                synchronized(jobHolders)
                {
                    if(jobsChanged)
                    {
                        jobHolderList.setListData(jobHolders.toArray(new JobHolder[jobHolders.size()]));
                        if(currentJobControlPanel!= null && !currentJobControlPanel.isStillValid())
                        {
                            currentJobControlPanel = null;
                            centralPanel.removeAll();
                            centralPanel.add(new JLabel("<html><center><p style=\"color=#ffffff\">Job Uninitialized<br />The selected job is not running anymore, and, thus, cannot be modified.</p></center></html>"));
                            centralPanel.revalidate();
                        }
                        jobsChanged = false;
                    }
                }
            }
        };
        if(SwingUtilities.isEventDispatchThread())
            runner.run();
        else
            SwingUtilities.invokeLater(runner);
    }
    @Override
	public void unregisterJob(LiveModifiableJob job)
    {
        boolean foundJob = false;
        synchronized(jobHolders)
        {
            for(int i=0; i<jobHolders.size(); i++)
            {
                JobHolder jobHolder = jobHolders.get(i);
                if(jobHolder.isJobEqual(job))
                {
                    jobHolder.jobUninitilized();
                    jobHolders.remove(i);
                    foundJob = true;
                    break;
                }
            }
            if(foundJob)
            {
                jobsChanged = true;
                synchronized(LiveModifiableJobCallbackImpl.class)
                {
                    if(jobHolders.size() <= 0)
                    {
                        frame.setVisible(false);
                    }
                }
            }
        }
        if(foundJob)
            jobsUpdated();
    }

	@Override
	public void pingCallback() throws RemoteException {
		// do nothing.
	}

	@Override
	public synchronized void initializeCallback(Serializable... arguments) throws RemoteException, CallbackException {
		numRegistered++;
		setupUI();
	}

	@Override
	public synchronized void uninitializeCallback() throws RemoteException, CallbackException {
		numRegistered--;
		if(numRegistered == 0 && frame != null)
		{
			frame.setVisible(false);
		}
	}

	@Override
	public String getTypeIdentifier() throws RemoteException {
		return TYPE_IDENTIFIER;
	}
}
