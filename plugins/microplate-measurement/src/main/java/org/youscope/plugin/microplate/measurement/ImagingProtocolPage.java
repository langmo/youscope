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
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import org.youscope.addon.measurement.MeasurementAddonUIPage;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.job.JobConfiguration;
import org.youscope.common.microplate.WellWithGroup;
import org.youscope.common.microplate.WellWithGroup.WellGroup;
import org.youscope.common.util.ConfigurationTools;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.JobsDefinitionPanel;

/**
 * @author Moritz Lang
 *
 */
class ImagingProtocolPage extends MeasurementAddonUIPage<MicroplateMeasurementConfiguration>
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 356040283004900768L;
	private final YouScopeClient	client;
	private final YouScopeServer			server;
	
	private final HashMap<WellWithGroup.WellGroup, JobsDefinitionPanel> jobPanels = new HashMap<>();
	private final JTabbedPane tabs = new JTabbedPane();
	private final JPanel tabsPanel = new JPanel(new BorderLayout());
	ImagingProtocolPage(YouScopeClient client, YouScopeServer server)
	{
		this.server = server;
		this.client = client;
	}
	
	@Override
	public void loadData(MicroplateMeasurementConfiguration configuration)
	{
		Set<WellWithGroup.WellGroup> groups = jobPanels.keySet();
		for(WellWithGroup.WellGroup group : groups)
		{
			jobPanels.get(group).setJobs(configuration.getJobs(group));
		}
		final Set<WellWithGroup.WellGroup> usedGroups = new HashSet<>();
		for(WellWithGroup well : configuration.getSelectedWells())
		{
			usedGroups.add(well.getGroup());
		}
		if(usedGroups.isEmpty())
			usedGroups.add(WellWithGroup.WellGroup.GROUP0);
		
		
		final WellWithGroup.WellGroup[] usedGroupsArray = usedGroups.toArray(new WellWithGroup.WellGroup[0]);
		Arrays.sort(usedGroupsArray, new Comparator<WellWithGroup.WellGroup>()
		{
			@Override
			public int compare(WellGroup arg0, WellGroup arg1) 
			{
				return arg0.getGroupId() - arg1.getGroupId();
			}	
		});
		
		Runnable runner = new Runnable()
		{
			@Override
			public void run() 
			{
				tabs.removeAll();
				tabsPanel.removeAll();
				if(usedGroupsArray.length == 1)
				{
					tabsPanel.add(jobPanels.get(usedGroupsArray[0]), BorderLayout.CENTER);
				}
				else
				{
					for(WellWithGroup.WellGroup group : usedGroupsArray)
					{
						tabs.addTab(group.getName(), group.getIcon(), jobPanels.get(group));
					}
					tabsPanel.add(tabs, BorderLayout.CENTER);
				}
				fireSizeChanged();
			}
	
		};
		if(SwingUtilities.isEventDispatchThread())
			runner.run();
		else
		{
			try {
				SwingUtilities.invokeAndWait(runner);
			} catch (InvocationTargetException | InterruptedException e) {
				client.sendError("Could not activate job panels for used well groups.", e);
			}
		}
	}

	@Override
	public boolean saveData(MicroplateMeasurementConfiguration configuration)
	{
		final HashSet<WellWithGroup.WellGroup> usedGroups = new HashSet<>();
		for(WellWithGroup well : configuration.getSelectedWells())
		{
			usedGroups.add(well.getGroup());
		}
		HashSet<String> collisions = new HashSet<>();
		for(WellWithGroup.WellGroup group : usedGroups)
		{
			JobConfiguration[] jobs = jobPanels.get(group).getJobs();
			configuration.setJobs(group, jobs);		
			String[] collisionNew = ConfigurationTools.checkImageSaveNameCollision(jobs);
			for(String collision : collisionNew)
				collisions.add(collision);
		}
		if(collisions.size() <= 0)
			return true;
		
		// Ask user if continue
		String message = "One or more imaging job save names is used more than once.\nThis may or may not lead to loss of files, if one job is\noverwriting the image of another.\nThe respective image save names are:\n";
		boolean first = true;
		for(String collision : collisions)
		{
			if(!first)
				message +=", ";
			else
				first = false;
			message += collision;
		}
		message+="\n\nIgnore collision and continue configuration?";
		int decision = JOptionPane.showConfirmDialog(this, message, "Image Save Name Collision", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		return decision == JOptionPane.YES_OPTION;			
	}

	@Override
	public void setToDefault(MicroplateMeasurementConfiguration configuration)
	{
		// Do nothing.
	}

	@Override
	public String getPageName()
	{
		return "Imaging protocol in each well/position";
	}

	@Override
	public void createUI(YouScopeFrame frame)
	{
		setLayout(new BorderLayout());
		for(WellWithGroup.WellGroup group : WellWithGroup.WellGroup.values())
		{
			jobPanels.put(group, new JobsDefinitionPanel(client, server, frame));
		}
		tabs.addTab(WellWithGroup.WellGroup.GROUP0.getName(), WellWithGroup.WellGroup.GROUP0.getIcon(), jobPanels.get(WellWithGroup.WellGroup.GROUP0));
		tabsPanel.add(tabs, BorderLayout.CENTER);
		add(tabsPanel, BorderLayout.CENTER);

		setBorder(new TitledBorder("Imaging protocol in each well/position"));
	}

}
