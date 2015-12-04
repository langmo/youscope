package org.youscope.plugin.simplemeasurement;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.youscope.addon.measurement.MeasurementAddonUIPage;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.StandardFormats;

class MiscPage extends MeasurementAddonUIPage<SimpleMeasurementConfigurationDTO>
{

	/**
	 * Serial Verision UID.
	 */
	private static final long				serialVersionUID		= 885352612109223078L;

	private JCheckBox 							storeStatisticsField = new JCheckBox("Gather statistics (slower).");
	private JLabel							statisticsFileFieldLabel		= new JLabel("Statistics file name (without extension):");
	private JTextField						statisticsFileField				= new JTextField("statistics");
	
	/**
	 * Constructor
	 * @param client  
	 * @param server 
	 */
	MiscPage(YouScopeClient client, YouScopeServer server)
	{
		// do nothing.
	}

	@Override
	public void loadData(SimpleMeasurementConfigurationDTO configuration)
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
	}

	@Override
	public boolean saveData(SimpleMeasurementConfigurationDTO configuration)
	{
		if(storeStatisticsField.isSelected())
		{
			configuration.setStatisticsFileName(statisticsFileField.getText());
		}
		else
		{
			configuration.setStatisticsFileName(null);
		}
		
		return true;
	}

	@Override
	public void setToDefault(SimpleMeasurementConfigurationDTO configuration)
	{
		// Do nothing.
	}

	@Override
	public String getPageName()
	{
		return "Miscellaneous";
	}

	@Override
	public void createUI(YouScopeFrame frame)
	{
		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);

		GridBagConstraints newLineConstr = StandardFormats.getNewLineConstraint();
		GridBagConstraints bottomConstr = StandardFormats.getBottomContstraint();
		
		// Panel to store statistics
		StandardFormats.addGridBagElement(storeStatisticsField, layout, newLineConstr, this);
		StandardFormats.addGridBagElement(statisticsFileFieldLabel, layout, newLineConstr, this);
		StandardFormats.addGridBagElement(statisticsFileField, layout, newLineConstr, this);
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
		
		StandardFormats.addGridBagElement(new JPanel(), layout, bottomConstr, this);
		setBorder(new TitledBorder("Miscellaneous"));
	}
}
