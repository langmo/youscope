package org.youscope.plugin.microplatemeasurement;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.youscope.addon.measurement.MeasurementAddonUIPage;
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
	
	private final JCheckBox 							allowEditsField = new JCheckBox("Allow measurement to be edited while running (experimental).");
	private final JCheckBox 							storeStatisticsField = new JCheckBox("Gather statistics about job runtimes.");
	private final JLabel							statisticsFileFieldLabel		= new JLabel("Statistics file name (without extension):");
	private final JTextField						statisticsFileField				= new JTextField("statistics");

	/**
	 * Constructor.
	 * @param client
	 * @param server
	 */
	MiscPage(YouScopeClient client, YouScopeServer server)
	{
		// do nothing.
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
	public void createUI(final YouScopeFrame frame)
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
		
		mainPanel.addFillEmpty();
		add(mainPanel, BorderLayout.CENTER);
		setBorder(new TitledBorder("Miscellaneous"));
	}
}
