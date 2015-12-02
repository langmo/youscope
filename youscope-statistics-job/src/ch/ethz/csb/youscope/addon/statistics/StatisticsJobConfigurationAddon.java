/**
 * 
 */
package ch.ethz.csb.youscope.addon.statistics;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import ch.ethz.csb.youscope.shared.measurement.job.basicjobs.StatisticsJob;
import ch.ethz.csb.youscope.client.addon.ConfigurationAddonAdapter;
import ch.ethz.csb.youscope.client.addon.ConfigurationMetadataAdapter;
import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.uielements.DynamicPanel;
import ch.ethz.csb.youscope.client.uielements.JobsDefinitionPanel;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.addon.AddonException;

/**
 * @author Moritz Lang
 */
class StatisticsJobConfigurationAddon extends ConfigurationAddonAdapter<StatisticsJobConfiguration>
{
	private JTextField							fileNameField				= new JTextField();

	private JobsDefinitionPanel jobPanel;
	
	StatisticsJobConfigurationAddon(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(),  client, server);
	}
	static ConfigurationMetadataAdapter<StatisticsJobConfiguration> getMetadata()
	{
		return new ConfigurationMetadataAdapter<StatisticsJobConfiguration>(StatisticsJob.DEFAULT_TYPE_IDENTIFIER, 
				StatisticsJobConfiguration.class, 
				StatisticsJob.class, 
				"Statistics", 
				new String[]{"Containers"});
	}
	
	@Override
	protected Component createUI(StatisticsJobConfiguration configuration) throws AddonException 
	{
		setTitle("Statistics Job");
		setResizable(true);
		setMaximizable(false);
		
		JEditorPane descriptionPane = new JEditorPane();
		descriptionPane.setEditable(false);
		descriptionPane.setContentType("text/html");
		descriptionPane.setText("<html><p style=\"font-size:small;margin-top:0px;\"><b>Description:</b></p>" +
				"<p style=\"font-size:small;margin-top:4px;margin-bottom:0px\">A statistics job gathers information about the timing of its sub-jobs and appends it to the specified file in a CSV format.</p></html>");
		JScrollPane descriptionScrollPane = new JScrollPane(descriptionPane);
		descriptionScrollPane.setPreferredSize(new Dimension(250, 100));
		DynamicPanel contentPane = new DynamicPanel();
		contentPane.add(descriptionScrollPane);
		
		contentPane.add(new JLabel("Statistics file name (without extension):"));
		contentPane.add(fileNameField);

		contentPane.add(new JLabel("Executed and monitored jobs:"));
        jobPanel = new JobsDefinitionPanel(getClient(), getServer(), getContainingFrame());
        jobPanel.setJobs(configuration.getJobs());
        		
		// Load state
		fileNameField.setText(configuration.getFileName());
		contentPane.addFill(jobPanel);
		return contentPane;
	}
	@Override
	protected void commitChanges(StatisticsJobConfiguration configuration){
		configuration.setJobs(jobPanel.getJobs());
		configuration.setFileName(fileNameField.getText());
	}
}
