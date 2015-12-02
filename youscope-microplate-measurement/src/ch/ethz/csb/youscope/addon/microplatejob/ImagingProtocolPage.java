/**
 * 
 */
package ch.ethz.csb.youscope.addon.microplatejob;

import java.awt.BorderLayout;

import javax.swing.JOptionPane;
import javax.swing.border.TitledBorder;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.YouScopeFrame;
import ch.ethz.csb.youscope.client.uielements.JobConfigurationPage;
import ch.ethz.csb.youscope.client.uielements.JobsDefinitionPanel;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.tools.ConfigurationValidation;

/**
 * @author Moritz Lang
 *
 */
class ImagingProtocolPage extends JobConfigurationPage<MicroplateJobConfigurationDTO>
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 356040281004900768L;
	private final YouScopeClient	client;
	private final YouScopeServer			server;
	
	private JobsDefinitionPanel jobPanel;
	
	ImagingProtocolPage(YouScopeClient client, YouScopeServer server)
	{
		this.server = server;
		this.client = client;
	}
	
	@Override
	public void loadData(MicroplateJobConfigurationDTO configuration)
	{
		jobPanel.setJobs(configuration.getJobs());
	}

	@Override
	public boolean saveData(MicroplateJobConfigurationDTO configuration)
	{
		configuration.setJobs(jobPanel.getJobs());		
		String[] collisions = ConfigurationValidation.checkImageSaveNameCollision(configuration.getJobs());
		if(collisions.length <= 0)
			return true;
		
		// Ask user if continue
		String message = "One or more imaging job save names is used more than once.\nThis may or may not lead to loss of files, if one job is\noverwriting the image of another.\nThe respective image save names are:\n";
		for(int i=0; i<collisions.length; i++)
		{
			if(i > 0)
				message +=", ";
			message += collisions[i];
		}
		message+="\n\nIgnore collision and continue configuration?";
		int decision = JOptionPane.showConfirmDialog(this, message, "Image Save Name Collision", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		return decision == JOptionPane.YES_OPTION;			
	}

	@Override
	public void setToDefault(MicroplateJobConfigurationDTO configuration)
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
		jobPanel = new JobsDefinitionPanel(client, server, frame); 
		add(jobPanel, BorderLayout.CENTER);

		setBorder(new TitledBorder("Imaging protocol in each well/position"));
	}

}
