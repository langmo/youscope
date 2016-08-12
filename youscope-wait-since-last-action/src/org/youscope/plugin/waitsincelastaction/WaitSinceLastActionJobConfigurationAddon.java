/**
 * 
 */
package org.youscope.plugin.waitsincelastaction;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonUIAdapter;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.IntegerTextField;
import org.youscope.uielements.PeriodField;

/**
 * @author Moritz Lang
 */
class WaitSinceLastActionJobConfigurationAddon extends ComponentAddonUIAdapter<WaitSinceLastActionJobConfiguration>
{
	private final PeriodField waitTimeField = new PeriodField();
	private final PeriodField initialWaitTimeField = new PeriodField();
	private final IntegerTextField actionIDField = new IntegerTextField();
	private final JCheckBox resetAfterIterationField = new JCheckBox("Reset timer after each iteration."); 
	
	/**
	 * Constructor.
	 * @param client Interface to the client.
	 * @param server Interface to the server.
	 * @throws AddonException 
	 */
	public WaitSinceLastActionJobConfigurationAddon(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(),  client, server);
	}
    
	static ComponentMetadataAdapter<WaitSinceLastActionJobConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<WaitSinceLastActionJobConfiguration>(WaitSinceLastActionJobConfiguration.TYPE_IDENTIFIER, 
				WaitSinceLastActionJobConfiguration.class, 
				WaitSinceLastActionJob.class, 
				"Wait since last action", 
				new String[]{"misc"}, "icons/alarm-clock-select.png");
	}

	@Override
	protected Component createUI(WaitSinceLastActionJobConfiguration configuration) throws AddonException
	{
		setTitle("Wait since last action");
		setResizable(true);
		setMaximizable(false);
		
		DynamicPanel elementsPanel = new DynamicPanel();
		elementsPanel.add(new JLabel("Wait time between successive actions (in ms):"));
		elementsPanel.add(waitTimeField);
		elementsPanel.add(new JLabel("Initial wait time at first actions (in ms):"));
		elementsPanel.add(initialWaitTimeField);
		elementsPanel.add(resetAfterIterationField);
		elementsPanel.add(new JLabel("ID of action (allows for different wait timers):"));
		elementsPanel.add(actionIDField);
		
		waitTimeField.setDuration(configuration.getWaitTime());
		initialWaitTimeField.setDuration(configuration.getInitialWaitTime());
		actionIDField.setMinimalValue(0);
		actionIDField.setValue(configuration.getActionID());
		resetAfterIterationField.setSelected(configuration.isResetAfterIteration());
		return elementsPanel;
	}

	@Override
	protected void commitChanges(WaitSinceLastActionJobConfiguration configuration) {
		configuration.setWaitTime(waitTimeField.getDurationLong());
		configuration.setInitialWaitTime(initialWaitTimeField.getDurationLong());
		configuration.setActionID(actionIDField.getValue());
		configuration.setResetAfterIteration(resetAfterIterationField.isSelected());
		
	}

	@Override
	protected void initializeDefaultConfiguration(WaitSinceLastActionJobConfiguration configuration) throws AddonException {
		// do nothing.
	}
}
