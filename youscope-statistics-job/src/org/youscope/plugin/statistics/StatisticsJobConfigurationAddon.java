/**
 * 
 */
package org.youscope.plugin.statistics;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonUIAdapter;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.measurement.job.basicjobs.StatisticsJob;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.JobsDefinitionPanel;

/**
 * @author Moritz Lang
 */
class StatisticsJobConfigurationAddon extends ComponentAddonUIAdapter<StatisticsJobConfiguration>
{
	private JTextField							fileNameField				= new JTextField();

	private JobsDefinitionPanel jobPanel;
	
	StatisticsJobConfigurationAddon(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(),  client, server);
	}
	static ComponentMetadataAdapter<StatisticsJobConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<StatisticsJobConfiguration>(StatisticsJob.DEFAULT_TYPE_IDENTIFIER, 
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
	@Override
	protected void initializeDefaultConfiguration(StatisticsJobConfiguration configuration) throws AddonException {
		// do nothing.
	}
}
