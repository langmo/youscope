package ch.ethz.csb.youscope.addon.autofocus;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import ch.ethz.csb.youscope.addon.simplefocusscores.VarianceFocusScoreConfiguration;
import ch.ethz.csb.youscope.client.addon.ConfigurationAddon;
import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.YouScopeFrame;
import ch.ethz.csb.youscope.client.uielements.ComponentComboBox;
import ch.ethz.csb.youscope.client.uielements.DynamicPanel;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationMetadata;
import ch.ethz.csb.youscope.shared.resource.focusscore.FocusScoreConfiguration;

class FocusScoreAlgorithmPanel extends JPanel
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -7771369551694828424L;
	
	private final ComponentComboBox<FocusScoreConfiguration>	focusScoreAlgorithmField;
	private Component addonConfigurationPanel = null;
	private final YouScopeClient client; 
	private final YouScopeFrame frame;
	private ConfigurationAddon<? extends FocusScoreConfiguration> currentAddon = null;
	private final FocusScoreConfiguration lastConfiguration;
	/**
	 * Constructor.
	 * @param client Interface to the client.
	 * @param server Interface to the server.
	 * @param frame 
	 * @param focusScoreConfiguration 
	 */
	public FocusScoreAlgorithmPanel(YouScopeClient client, YouScopeServer server, YouScopeFrame frame, FocusScoreConfiguration focusScoreConfiguration)
	{
		super(new BorderLayout());
		this.client = client;
		this.frame = frame;
		this.lastConfiguration = 		focusScoreConfiguration;
		
		setOpaque(false);
		String selectedConfigurationID;
		if(focusScoreConfiguration != null)
			selectedConfigurationID = focusScoreConfiguration.getTypeIdentifier();
		else
			selectedConfigurationID = VarianceFocusScoreConfiguration.CONFIGURATION_ID;
		focusScoreAlgorithmField = new ComponentComboBox<FocusScoreConfiguration>(client, FocusScoreConfiguration.class);
		focusScoreAlgorithmField.setSelectedElement(selectedConfigurationID);
		
		DynamicPanel topPanel = new DynamicPanel();
		topPanel.add(new JLabel("Focus Score Algorithm:"));
		topPanel.add(focusScoreAlgorithmField);
		add(topPanel, BorderLayout.NORTH);
		
		displayAddonConfiguration(focusScoreAlgorithmField.getSelectedElement());
		focusScoreAlgorithmField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) {
				displayAddonConfiguration(focusScoreAlgorithmField.getSelectedElement());
			}
		});
		
		
	}
	private Component createErrorUI(String message, Exception exception)
	{
		if(exception != null)
			message += "\n\n"+exception.getMessage();
		JTextArea textArea = new JTextArea(message);
		textArea.setEditable(false);
		JPanel errorPane = new JPanel(new BorderLayout());
		errorPane.add(new JLabel("An error occured:"), BorderLayout.NORTH);
		errorPane.add(new JScrollPane(textArea), BorderLayout.CENTER);
		return errorPane;
	}
	public FocusScoreConfiguration getConfiguration()
	{
		if(currentAddon == null)
			return null;
		return currentAddon.getConfiguration();
	}
	private void displayAddonConfiguration(ConfigurationMetadata<? extends FocusScoreConfiguration> metadata)
	{
		if(currentAddon != null)
		{
			ConfigurationMetadata<?> currentmetadata = currentAddon.getConfigurationMetadata();
			if(currentmetadata!= null && metadata != null && currentmetadata.getTypeIdentifier().equals(metadata.getTypeIdentifier()))
				return;
		}
		if(addonConfigurationPanel != null)
			remove(addonConfigurationPanel);
		if(metadata == null)
		{
			addonConfigurationPanel = createErrorUI("No focus score algorithms available.", null);
			add(addonConfigurationPanel, BorderLayout.CENTER);
			revalidate();
			if(frame.isVisible())
				frame.pack();
			return;
		}
		try 
		{
			currentAddon = client.getAddonProvider().createConfigurationAddon(metadata);
			if(lastConfiguration != null && lastConfiguration.getTypeIdentifier().equals(metadata.getTypeIdentifier()))
			{
				currentAddon.setConfiguration(lastConfiguration);
			}
			addonConfigurationPanel = currentAddon.toPanel(frame);
		} 
		catch (Exception e) 
		{
			addonConfigurationPanel = createErrorUI("Error loading focus score configuration UI.", e);
			add(addonConfigurationPanel, BorderLayout.CENTER);
			client.sendError("Error loading focus score configuration UI.",  e);
			revalidate();
			if(frame.isVisible())
				frame.pack();
			return;
		}
		
		add(addonConfigurationPanel, BorderLayout.CENTER);
		revalidate();
		if(frame.isVisible())
			frame.pack();
	}

}
