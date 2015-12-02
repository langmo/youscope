/**
 * 
 */
package ch.ethz.csb.youscope.addon.composedimaging;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JPanel;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.YouScopeFrame;
import ch.ethz.csb.youscope.client.addon.measurement.MeasurementConfigurationAddon;
import ch.ethz.csb.youscope.client.addon.measurement.MeasurementConfigurationAddonListener;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.MeasurementConfiguration;

/**
 * @author Moritz Lang
 */
class ComposedImagingMeasurementConfigurationAddon implements MeasurementConfigurationAddon
{

	private Vector<MeasurementConfigurationAddonListener>	configurationListeners	= new Vector<MeasurementConfigurationAddonListener>();

	private int												currentPage				= 0;
	
	private CardLayout										pagesLayout				= new CardLayout();

	private JPanel											pagesPanel				= new JPanel(pagesLayout);

	private JButton											previousButton			= new JButton("Previous");

	private JButton											nextButton				= new JButton("Next");

	private ComposedImagingMeasurementConfiguration		measurementConfiguration;

	private YouScopeFrame									frame;
	private AbstractConfigurationPage[] pages = null;
	
	/**
	 * Constructor.
	 * @param microscope Interface to the microscope.
	 * @param client Interface to the client.
	 */
	ComposedImagingMeasurementConfigurationAddon(YouScopeClient client, YouScopeServer server)
	{
		// Create single pages.
		pages = new AbstractConfigurationPage[]
		{
				new StartPage(),
				new GeneralSettingsPage(client, server),
				new StartAndEndConfigurationPage(client, server),
				new ImagingConfigurationPage(client, server),
				new AreaConfigurationPage(client, server)				
		};
	}

	@Override
	public void createUI(YouScopeFrame frame)
	{
		this.frame = frame;
		frame.setTitle("Composed Imaging Measurement Configuration");
		frame.setResizable(true);
		frame.setClosable(true);
		frame.setMaximizable(false);

		frame.startInitializing();
		(new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					initilizeFrame();
				}
				catch(RemoteException e)
				{
					ComposedImagingMeasurementConfigurationAddon.this.frame.setToErrorState("Could not initialize frame", e);
				}
				ComposedImagingMeasurementConfigurationAddon.this.frame.endLoading();
			}
		})).start();
	}

	@Override
	public ComposedImagingMeasurementConfiguration getConfigurationData()
	{
		if(measurementConfiguration == null)
		{
			// Set to standard configuration if not set from somebody else
			measurementConfiguration = new ComposedImagingMeasurementConfiguration();
			for(AbstractConfigurationPage page : pages)
			{
				page.setToDefault(measurementConfiguration);
			}
		}
		return measurementConfiguration;
	}

	@Override
	public void setConfigurationData(MeasurementConfiguration measurementConfiguration) throws ConfigurationException
	{
		if(!(measurementConfiguration instanceof ComposedImagingMeasurementConfiguration))
			throw new ConfigurationException("Only composed measurement configurations accepted by this addon.");
		this.measurementConfiguration = (ComposedImagingMeasurementConfiguration)measurementConfiguration;
	}

	private void initilizeFrame() throws RemoteException
	{
		// Initialize pages
		for(AbstractConfigurationPage page : pages)
		{
			page.createUI();
			page.loadData(getConfigurationData());
			page.addSizeChangeListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					frame.pack();
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
				pages[currentPage].saveData(getConfigurationData());
				
				currentPage--;
				pages[currentPage].loadData(getConfigurationData());
				pagesLayout.previous(pagesPanel);
				if(currentPage < 1)
					previousButton.setEnabled(false);
				nextButton.setText("Next");
				frame.pack();
			}
		});

		nextButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				pages[currentPage].saveData(getConfigurationData());
				if(currentPage >= pages.length - 1)
				{
					createMeasurement();
					return;
				}
				
				currentPage++;
				pages[currentPage].loadData(getConfigurationData());
				pagesLayout.next(pagesPanel);
				if(currentPage >= pages.length - 1)
					nextButton.setText("Finish");
				previousButton.setEnabled(true);
				frame.pack();
			}
		});
		buttonPanel.add(previousButton);
		buttonPanel.add(nextButton);
		
		// Add the pages
		for(AbstractConfigurationPage page : pages)
		{
			pagesPanel.add(page, page.getPageName());
		}
		
		pages[currentPage].loadData(getConfigurationData());
		
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(buttonPanel, BorderLayout.SOUTH);
		contentPane.add(pagesPanel, BorderLayout.CENTER);
		frame.setContentPane(contentPane);
		frame.pack();
	}

	private void createMeasurement()
	{
		ComposedImagingMeasurementConfiguration configuration = getConfigurationData();

		// Inform listener that configuration is finished.
		for(MeasurementConfigurationAddonListener listener : configurationListeners)
		{
			listener.measurementConfigurationFinished(configuration);
		}

		frame.setVisible(false);
	}

	@Override
	public void addConfigurationListener(MeasurementConfigurationAddonListener listener)
	{
		configurationListeners.add(listener);
	}

	@Override
	public void removeConfigurationListener(MeasurementConfigurationAddonListener listener)
	{
		configurationListeners.remove(listener);
	}

}
