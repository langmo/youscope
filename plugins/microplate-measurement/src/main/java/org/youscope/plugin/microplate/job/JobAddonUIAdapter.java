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
package org.youscope.plugin.microplate.job;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonUIAdapter;
import org.youscope.addon.component.ComponentMetadata;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.job.JobConfiguration;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * A panel to display and edit several pages of a measurement configuration. 
 * @author Moritz Lang
 * @param <T> The type of measurement configuration which should be displayed by this panel.
 */
class JobAddonUIAdapter<T extends JobConfiguration> extends ComponentAddonUIAdapter<T>
{
	private int												currentPage				= 0;
	
	private CardLayout										pagesLayout				= new CardLayout(0, 0);

	private JPanel											pagesPanel				= new JPanel(pagesLayout);

	private JButton											previousButton			= new JButton("Previous");

	private JButton											nextButton				= new JButton("Next");

	private final ArrayList<JobAddonUIPage<? super T>> pages = new ArrayList<JobAddonUIPage<? super T>>();
	
	/**
	 * Constructor.
	 * @param metadata The metadata of the addon.
	 * @param client The YouScope client.
	 * @param server The YouScope server.
	 * @throws AddonException
	 */
	public JobAddonUIAdapter(final ComponentMetadata<T> metadata,  final YouScopeClient client, final YouScopeServer server) throws AddonException 
	{
		super(metadata, client, server);
		setShowCloseButton(false);
	}
	
	/**
	 * Adds a page to the layout. Must be called before toFrame() or toPanel(org.youscope.clientinterfaces.YouScopeFrame) is called.
	 * @param page Page to be added.
	 */
	public void addPage(JobAddonUIPage<? super T> page)
	{
		pages.add(page);
	}
	
	/**
	 * Inserts a page into the layout at the given index. Must be called before toFrame() or {toPanel(org.youscope.clientinterfaces.YouScopeFrame) is called.
	 * @param page Page to be added.
	 * @param index Index where to add page. Must be bigger or equal to 0, and smaller or equal to getNumPages().
	 * @throws IndexOutOfBoundsException thrown if index is invalid. 
	 */
	public void insertPage(JobAddonUIPage<? super T> page, int index) throws IndexOutOfBoundsException
	{
		pages.add(index, page);
	}
	
	/**
	 * Removes all pages from the current layout.
	 */
	public void clearPages()
	{
		pages.clear();
	}
	
	/**
	 * Returns the number of pages.
	 * @return Number of pages.
	 */
	public int getNumPages()
	{
		return pages.size();
	}

	@Override
	protected Component createUI(final T configuration) throws AddonException
	{
		// Initialize pages
		for(JobAddonUIPage<? super T> page : pages)
		{
			page.createUI(getContainingFrame());
			page.loadData(configuration);
			page.addSizeChangeListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					getContainingFrame().pack();
				}
			});
		}
		
		// Next & Last Buttons
		final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		previousButton.setEnabled(false);
		previousButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if(!pages.get(currentPage).saveData(configuration))
					return;
				if(currentPage <= 0)
					return;
				currentPage--;
				while(currentPage >= 0 && !pages.get(currentPage).isAppear(configuration))
				{
					currentPage--;
				}
				if(currentPage < 0)
				{
					for(currentPage = 0; currentPage < pages.size(); currentPage++)
					{
						if(pages.get(currentPage).isAppear(configuration))
							break;
					}
				}
				JobAddonUIPage<? super T> page = pages.get(currentPage);
				page.loadData(configuration);
				pagesLayout.show(pagesPanel, page.getPageName());
				boolean firstPage = true;
				for(int pageID = 0; pageID<currentPage; pageID++)
				{
					if(pages.get(pageID).isAppear(configuration))
					{
						firstPage = false;
						break;
					}
				}
				if(firstPage)
					previousButton.setEnabled(false);
				else
					previousButton.setEnabled(true);
				
				boolean lastPage = true;
				for(int pageID = pages.size()-1; pageID>currentPage; pageID--)
				{
					if(pages.get(pageID).isAppear(configuration))
					{
						lastPage = false;
						break;
					}
				}
				if(lastPage)
					nextButton.setText("Finish");
				else
					nextButton.setText("Next");
				getContainingFrame().pack();
			}
		});

		nextButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if(!pages.get(currentPage).saveData(configuration))
					return;
				currentPage++;
				while(currentPage < pages.size() && !pages.get(currentPage).isAppear(configuration))
				{
					currentPage++;
				}
				if(currentPage >= pages.size())
				{
					closeAddon();
					return;
				}
				JobAddonUIPage<? super T> page = pages.get(currentPage);
				page.loadData(configuration);
				pagesLayout.show(pagesPanel, page.getPageName());
				boolean firstPage = true;
				for(int pageID = 0; pageID<currentPage; pageID++)
				{
					if(pages.get(pageID).isAppear(configuration))
					{
						firstPage = false;
						break;
					}
				}
				if(firstPage)
					previousButton.setEnabled(false);
				else
					previousButton.setEnabled(true);
				nextButton.setText("Next");
				
				boolean lastPage = true;
				for(int pageID = pages.size()-1; pageID>currentPage; pageID--)
				{
					if(pages.get(pageID).isAppear(configuration))
					{
						lastPage = false;
						break;
					}
				}
				if(lastPage)
					nextButton.setText("Finish");
				else
					nextButton.setText("Next");
				getContainingFrame().pack();
			}
		});
		buttonPanel.add(previousButton);
		buttonPanel.add(nextButton);
		
		// Add the pages
		for(JobAddonUIPage<? super T> page : pages)
		{
			pagesPanel.add(page, page.getPageName());
		}
		
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(buttonPanel, BorderLayout.SOUTH);
		contentPane.add(pagesPanel, BorderLayout.CENTER);
		return contentPane;
	}

	@Override
	protected void commitChanges(T configuration) 
	{
		for(JobAddonUIPage<? super T> page : pages)
		{
			page.saveData(configuration);
		}
	}

	@Override
	protected void initializeDefaultConfiguration(T configuration) throws AddonException {
		for(JobAddonUIPage<? super T> page : pages)
		{
			page.setToDefault(configuration);
		}
	}
}
