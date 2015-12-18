/**
 * 
 */
package org.youscope.plugin.microplatejob;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.youscope.addon.component.ComponentAddonUIListener;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.job.JobConfiguration;

/**
 * A panel to display and edit several pages of a job configuration. 
 * @author Moritz Lang
 * @param <T> The type of measurement configuration which should be displayed by this panel.
 */
public class JobConfigurationPanel<T extends JobConfiguration> extends JPanel
{

	private Vector<ComponentAddonUIListener<? super T>>	configurationListeners	= new Vector<ComponentAddonUIListener<? super T>>();

	/**
	 * Serial Version UID.
	 */
	private static final long								serialVersionUID		= 2215110680526377705L;

	private int												currentPage				= 0;
	
	private CardLayout										pagesLayout				= new CardLayout(0, 0);

	private JPanel											pagesPanel				= new JPanel(pagesLayout);

	private JButton											previousButton			= new JButton("Previous");

	private JButton											nextButton				= new JButton("Next");
	
	private final ArrayList<ActionListener> sizeChangeListeners = new ArrayList<ActionListener>();

	private T		jobConfiguration;

	private final ArrayList<JobConfigurationPage<T>> pages = new ArrayList<JobConfigurationPage<T>>();
	
	/**
	 * Constructor.
	 * The panel is empty when initialized. Call createUI to create the UI elements.
	 * 
	 * Note: Do not call createUI(..), loadData(..) or saveData(..) on the single pages. 
	 * 
	 * @param pages The pages which should be displayed by this panel. 
	 */
	public JobConfigurationPanel(Collection<? extends JobConfigurationPage<T>> pages)
	{
		super(new BorderLayout());
		this.pages.addAll(pages);
	}

	/**
	 * Creates the UI of this panel. Must be called, otherwise the panel will be empty.
	 * This function can be called in any thread (Changes in the UI are thread save).
	 * @param parentFrame The frame in which this panel is initialized. Needed since child frames might be opened.
	 */
	public void createUI(YouScopeFrame parentFrame)
	{
		if(jobConfiguration == null)
			throw new IllegalStateException("Function setConfigurationData(..) has to be called before createUI(..).");
		// Initialize pages
		for(JobConfigurationPage<T> page : pages)
		{
			page.createUI(parentFrame);
			page.loadData(getConfigurationData());
			page.addSizeChangeListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					fireSizeChanged();
				}
			});
		}
		
		// Next & Last Buttons
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		previousButton.setEnabled(false);
		previousButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if(currentPage < 1)
					return;
				if(!pages.get(currentPage).saveData(getConfigurationData()))
					return;
				
				currentPage--;
				pages.get(currentPage).loadData(getConfigurationData());
				pagesLayout.previous(pagesPanel);
				if(currentPage < 1)
					previousButton.setEnabled(false);
				nextButton.setText("Next");
				fireSizeChanged();
			}
		});

		nextButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if(!pages.get(currentPage).saveData(getConfigurationData()))
					return;
				if(currentPage >= pages.size() - 1)
				{
					createJob();
					return;
				}
				
				currentPage++;
				pages.get(currentPage).loadData(getConfigurationData());
				pagesLayout.next(pagesPanel);
				if(currentPage >= pages.size() - 1)
					nextButton.setText("Finish");
				previousButton.setEnabled(true);
				fireSizeChanged();
			}
		});
		buttonPanel.add(previousButton);
		buttonPanel.add(nextButton);
		
		// Add the pages
		for(JobConfigurationPage<T> page : pages)
		{
			pagesPanel.add(page, page.getPageName());
		}
			
		class LayoutSetter implements Runnable
		{
			private final JComponent buttonPanel;
			private final JComponent pagesPanel;
			LayoutSetter(final JComponent buttonPanel, final JComponent pagesPanel)
			{
				this.buttonPanel = buttonPanel;
				this.pagesPanel = pagesPanel;
			}
			@Override
			public void run()
			{
				add(buttonPanel, BorderLayout.SOUTH);
				add(pagesPanel, BorderLayout.CENTER);
				fireSizeChanged();
			}
			
		}
		
		if(SwingUtilities.isEventDispatchThread())
			new LayoutSetter(buttonPanel, pagesPanel).run();
		else
			SwingUtilities.invokeLater(new LayoutSetter(buttonPanel, pagesPanel));
	}
	
	/**
	 * Adds a listener which gets notified if the size of this page changed.
	 * @param listener Listener to add.
	 */
	public void addSizeChangeListener(ActionListener listener)
	{
		synchronized(sizeChangeListeners)
		{
			sizeChangeListeners.add(listener);
		}
	}
	/**
	 * Removes a previously added listener.
	 * @param listener Listener to be removed.
	 */
	public void removeSizeChangeListener(ActionListener listener)
	{
		synchronized(sizeChangeListeners)
		{
			sizeChangeListeners.remove(listener);
		}
	}
	
	private void createJob()
	{
		// Inform listener that configuration is finished.
		for(ComponentAddonUIListener<? super T> listener : configurationListeners)
		{
			listener.configurationFinished(jobConfiguration);
		}
	}

	/**
	 * Adds a listener which gets notified if the configuration is finished.
	 * @param listener
	 */
	public void addConfigurationListener(ComponentAddonUIListener<? super T> listener)
	{
		configurationListeners.add(listener);
	}

	/**
	 * Removes a previously added listener.
	 * @param listener
	 */
	public void removeConfigurationListener(ComponentAddonUIListener<? super T> listener)
	{
		configurationListeners.remove(listener);
	}

	/**
	 * Returns the configuration data of the job which got configured.
	 * @return Configuration data of the job configured with the help of the pages in this panel.
	 */
	public T getConfigurationData()
	{
		return jobConfiguration;
	}
	
	/**
	 * Sets the configuration of the job which should be configured.
	 * Has to be called before createUI(..).
	 * @param configuration
	 */
	public void setConfigurationData(T configuration)
	{
		this.jobConfiguration = configuration;
	}
	
	/**
	 * Sets all values of the measurement configuration to their default value.
	 * Internally, it only forwards to the setToDefault methods of the pages.
	 * @param configuration
	 */
	public void setToDefault(T configuration)
	{
		for(JobConfigurationPage<T> page : pages)
		{
			page.setToDefault(configuration);
		}
	}
	
	/**
	 * Notifies all listeners that the size of this page changed.
	 */
	protected void fireSizeChanged()
	{
		synchronized(sizeChangeListeners)
		{
			for(ActionListener listener : sizeChangeListeners)
			{
				listener.actionPerformed(new ActionEvent(this, 712, "Size changed."));
			}
		}
	}
}
