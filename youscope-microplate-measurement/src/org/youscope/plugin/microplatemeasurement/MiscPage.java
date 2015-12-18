package org.youscope.plugin.microplatemeasurement;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.ServiceLoader;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.youscope.addon.measurement.MeasurementAddonUIPage;
import org.youscope.addon.pathoptimizer.PathOptimizer;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DynamicPanel;

class MiscPage extends MeasurementAddonUIPage<MicroplateMeasurementConfiguration>
{

	/**
	 * Serial Verision UID.
	 */
	private static final long				serialVersionUID		= 885352612109223078L;

	private final YouScopeClient	client;

	private final JCheckBox 							allowEditsField = new JCheckBox("Allow measurement to be edited while running (experimental).");
	private final JCheckBox 							storeStatisticsField = new JCheckBox("Gather statistics about job runtimes.");
	private final JLabel							statisticsFileFieldLabel		= new JLabel("Statistics file name (without extension):");
	private final JTextField						statisticsFileField				= new JTextField("statistics");
	
	private final JComboBox<ComparableOptimizer>						pathOptimizerField	= new JComboBox<ComparableOptimizer>();

	/**
	 * Constructor.
	 * @param client
	 * @param server
	 */
	MiscPage(YouScopeClient client, YouScopeServer server)
	{
		this.client = client;
	}

	@Override
	public void loadData(MicroplateMeasurementConfiguration configuration)
	{
		String statisticsFileName = configuration.getStatisticsFileName();
		if(statisticsFileName == null)
		{
			storeStatisticsField.setSelected(false);
			statisticsFileFieldLabel.setVisible(false);
			statisticsFileField.setVisible(false);
		}
		else
		{
			storeStatisticsField.setSelected(true);
			statisticsFileFieldLabel.setVisible(true);
			statisticsFileField.setVisible(true);
			statisticsFileField.setText(statisticsFileName);			
		}
		allowEditsField.setSelected(configuration.isAllowEditsWhileRunning());
		
		loadPathOptimizers(configuration);
	}
	
	private class ComparableOptimizer implements Comparable<ComparableOptimizer>
	{
		private final PathOptimizer optimizer;
		private final MicroplatePositionConfiguration positionConfiguration;
		ComparableOptimizer(PathOptimizer optimizer, MicroplatePositionConfiguration positionConfiguration)
		{
			this.optimizer = optimizer;
			this.positionConfiguration = positionConfiguration;
		}
		
		@Override
		public int compareTo(ComparableOptimizer arg0)
		{
			if(arg0 == null)
				return -1;
			return arg0.optimizer.getSpecificity(positionConfiguration) - optimizer.getSpecificity(positionConfiguration) > 0 ? 1 : -1; 
		}
		
		@Override
		public String toString()
		{
			return optimizer.getName();
		}
		
		public String getOptimizerID()
		{
			return optimizer.getOptimizerID();
		}
	}
	
	private void loadPathOptimizers(MicroplateMeasurementConfiguration configuration)
	{
		
		
		MicroplatePositionConfiguration positionConfiguration = configuration.getMicroplatePositions();
		pathOptimizerField.removeAllItems();
		Vector<ComparableOptimizer> optimizers = new Vector<ComparableOptimizer>();
		for(PathOptimizer optimizer : getPathOptimizers())
		{
			if(!optimizer.isApplicable(positionConfiguration))
				continue;
			
			optimizers.addElement(new ComparableOptimizer(optimizer, positionConfiguration));
		}
		Collections.sort(optimizers);
		
		for(ComparableOptimizer optimizer : optimizers)
		{
			pathOptimizerField.addItem(optimizer);
		}
		
		if(configuration.getPathOptimizerID() != null)
		{
			int i=0;
			for(ComparableOptimizer optimizer : optimizers)
			{
				if(optimizer.getOptimizerID().equals(configuration.getPathOptimizerID()))
				{
					pathOptimizerField.setSelectedIndex(i);
					break;
				}
				i++;
			}
		}
		
		if(pathOptimizerField.getItemCount() == 0)
			client.sendError("No path optimizer found. Check installation.");
	}

	@Override
	public boolean saveData(MicroplateMeasurementConfiguration configuration)
	{
		if(storeStatisticsField.isSelected())
		{
			configuration.setStatisticsFileName(statisticsFileField.getText());
		}
		else
		{
			configuration.setStatisticsFileName(null);
		}
		configuration.setAllowEditsWhileRunning(allowEditsField.isSelected());
		configuration.setPathOptimizerID(((ComparableOptimizer)pathOptimizerField.getSelectedItem()).getOptimizerID());
		return true;
	}

	@Override
	public void setToDefault(MicroplateMeasurementConfiguration configuration)
	{
		// Do nothing.
	}

	@Override
	public String getPageName()
	{
		return "Misc";
	}

	@Override
	public void createUI(YouScopeFrame frame)
	{
		setLayout(new BorderLayout());
		
		DynamicPanel mainPanel = new DynamicPanel();
		mainPanel.add(allowEditsField);
		mainPanel.add(storeStatisticsField);
		mainPanel.add(statisticsFileFieldLabel);
		mainPanel.add(statisticsFileField);
		storeStatisticsField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				statisticsFileFieldLabel.setVisible(storeStatisticsField.isSelected());
				statisticsFileField.setVisible(storeStatisticsField.isSelected());
				fireSizeChanged();
			}
		});
		
		// Path optimizer
		mainPanel.add(new JLabel("Path:"));
		mainPanel.add(pathOptimizerField);
		
		mainPanel.addFillEmpty();
		add(mainPanel, BorderLayout.CENTER);
		setBorder(new TitledBorder("Miscellaneous"));
	}
	
	private static Iterable<PathOptimizer> getPathOptimizers()
    {
        ServiceLoader<PathOptimizer> pathOptimizers =
                ServiceLoader.load(PathOptimizer.class,
                		MicroplateMeasurementInitializer.class.getClassLoader());
        return pathOptimizers;
    }
}
