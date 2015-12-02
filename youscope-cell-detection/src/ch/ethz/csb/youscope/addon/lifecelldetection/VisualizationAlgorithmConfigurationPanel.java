package ch.ethz.csb.youscope.addon.lifecelldetection;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.YouScopeFrame;
import ch.ethz.csb.youscope.client.uielements.DynamicPanel;
import ch.ethz.csb.youscope.client.uielements.SingleComponentDefinitionPanel;
import ch.ethz.csb.youscope.shared.addon.celldetection.CellVisualizationConfiguration;

class VisualizationAlgorithmConfigurationPanel  extends DynamicPanel {

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 1441062694566624365L;

	private final JCheckBox								createVisualizationImageField			= new JCheckBox("Create Detection Visualization Image", true);
	private final SingleComponentDefinitionPanel<CellVisualizationConfiguration> visualizationConfigurationField;
	public VisualizationAlgorithmConfigurationPanel(CellDetectionJobConfiguration configuration, YouScopeClient client, final YouScopeFrame parentFrame) 
	{
		add(createVisualizationImageField);
		visualizationConfigurationField = new SingleComponentDefinitionPanel<CellVisualizationConfiguration>(CellVisualizationConfiguration.class, configuration.getVisualizationAlgorithmConfiguration(), client, parentFrame);
		visualizationConfigurationField.setLabel("Cell visualization algorithm:");
		createVisualizationImageField.setOpaque(false);
		createVisualizationImageField.setSelected(configuration.getVisualizationAlgorithmConfiguration() != null);
		if(configuration.getVisualizationAlgorithmConfiguration() == null)
			visualizationConfigurationField.setVisible(false);
		addFill(visualizationConfigurationField);
		addFillEmpty();
		createVisualizationImageField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				boolean selected = createVisualizationImageField.isSelected();
				visualizationConfigurationField.setVisible(selected);
				parentFrame.pack();
			}
		});
	}
	public void commitChanges(CellDetectionJobConfiguration configuration)
	{
		if(createVisualizationImageField.isSelected())
			configuration.setVisualizationAlgorithmConfiguration(visualizationConfigurationField.getConfiguration());
	}
}
