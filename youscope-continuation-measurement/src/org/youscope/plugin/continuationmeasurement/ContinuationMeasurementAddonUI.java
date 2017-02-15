/**
 * 
 */
package org.youscope.plugin.continuationmeasurement;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonUI;
import org.youscope.addon.component.ComponentAddonUIAdapter;
import org.youscope.addon.component.ComponentAddonUIListener;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.measurement.MeasurementConfiguration;
import org.youscope.plugin.continuationmeasurement.SelectMeasurementPanel.SelectionListener;
import org.youscope.serverinterfaces.YouScopeServer;


/**
 * @author langmo
 *
 */
class ContinuationMeasurementAddonUI extends ComponentAddonUIAdapter<ContinuationMeasurementConfiguration>
{
	
	/**
	 * Constructor.
	 * @param server YouScope server.
	 * @param YouScope client.
	 * @throws AddonException 
	 */
	ContinuationMeasurementAddonUI(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(), client, server);
		setTitle("Continuation Measurement");
		setShowCloseButton(false);
	}
	static ComponentMetadataAdapter<ContinuationMeasurementConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<ContinuationMeasurementConfiguration>(ContinuationMeasurementConfiguration.TYPE_IDENTIFIER, 
				ContinuationMeasurementConfiguration.class, 
				Measurement.class, "Continuation Measurement", new String[0], 
				"Continues a finished measurement, i.e. appends data to the respective folder.", 
				(String)null);
	}
	@Override
	protected void initializeDefaultConfiguration(ContinuationMeasurementConfiguration configuration)
			throws AddonException {
		configuration.setEncapsulatedConfiguration(null);
	}
	@Override
	protected Component createUI(final ContinuationMeasurementConfiguration configuration) throws AddonException 
	{
		final JPanel mainPanel = new JPanel(new BorderLayout());
		if(configuration.getMeasurementFolder() == null || configuration.getEncapsulatedConfiguration() == null)
		{
			SelectMeasurementPanel selectMeasurementPanel = new SelectMeasurementPanel(getClient(), configuration.getMeasurementFolder(), null);
			selectMeasurementPanel.addSelectionListener(new SelectionListener() {
				
				@Override
				public void selectionMade(MeasurementConfiguration encapsulatedMeasurement, String folder, long deltaEvaluationNumber,
						long previousRuntime) 
				{
					Component encapsulatedComponent;
					try 
					{
						encapsulatedComponent =getEncapsulatedMeasurementUI(configuration, encapsulatedMeasurement); 
						
					} catch (AddonException | ConfigurationException e) {
						getClient().sendError("Could not load user interface of original measurement.", e);
						return;
					}
					
					configuration.setDeltaEvaluationNumber(deltaEvaluationNumber);
					configuration.setEncapsulatedConfiguration(encapsulatedMeasurement);
					configuration.setMeasurementFolder(folder);
					configuration.setPreviousRuntime(previousRuntime);
					mainPanel.removeAll();
					mainPanel.add(encapsulatedComponent, BorderLayout.CENTER);
					mainPanel.revalidate();
					notifyLayoutChanged();
				}
			});
			mainPanel.add(selectMeasurementPanel, BorderLayout.CENTER);
		}
		else
		{
			MeasurementConfiguration encapsulatedMeasurement = configuration.getEncapsulatedConfiguration();
			Component encapsulatedComponent;
			try {
				encapsulatedComponent = getEncapsulatedMeasurementUI(configuration, encapsulatedMeasurement);
			} catch (ConfigurationException e) {
				throw new AddonException("Could not load UI for encapsualted configuration of already defined continuation measurement", e);
			} 
			mainPanel.add(encapsulatedComponent, BorderLayout.CENTER);
		}
		return mainPanel;
	}
	ComponentAddonUI<? extends MeasurementConfiguration> addonUI = null;
	private Component getEncapsulatedMeasurementUI(final ContinuationMeasurementConfiguration configuration, MeasurementConfiguration encapsulatedMeasurement) throws AddonException, ConfigurationException
	{
		addonUI = getClient().getAddonProvider().createComponentUI(encapsulatedMeasurement.getTypeIdentifier(), MeasurementConfiguration.class);
		addonUI.setConfiguration(encapsulatedMeasurement);
		addonUI.addUIListener(new ComponentAddonUIListener<MeasurementConfiguration>() {

			@Override
			public void configurationFinished(MeasurementConfiguration encapsulatedMeasurement) 
			{
				configuration.setEncapsulatedConfiguration(encapsulatedMeasurement);
				closeAddon();
			}
		});
		return addonUI.toPanel(getContainingFrame());
	}
	@Override
	protected void commitChanges(ContinuationMeasurementConfiguration configuration) {
		// do nothing.
	}
}
