/**
 * 
 */
package org.youscope.plugin.continuationmeasurement;

import org.youscope.addon.AddonException;
import org.youscope.addon.tool.ToolAddonUIAdapter;
import org.youscope.addon.tool.ToolMetadata;
import org.youscope.addon.tool.ToolMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.measurement.MeasurementConfiguration;
import org.youscope.common.saving.MeasurementFileLocations;
import org.youscope.plugin.continuationmeasurement.SelectMeasurementPanel.SelectionListener;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * @author Moritz Lang
 *
 */
class ContinuationMeasurementPostProcessing extends ToolAddonUIAdapter
{
	/**
	 * The type identifier is a unique ID of your tool. No other tool or other element of YouScope should
	 * have the same type identifier as your tool, thus, change this identifier to something unique. The general notation is
	 * your_identifier DOT tool_identifier, in which your_identifier is e.g. SmithAnton, MyCompanyName, or LonleyProgrammer1972.
	 * tool_identifer is the name of your tool, e.g. FancyFocus, MySpecialDeviceController, or HelloWorldTool.
	 */
	public final static String TYPE_IDENTIFIER = "YouScope.ContinuationMeasurement.PostProcessing";
	private final String lastFolder;
	private final String lastConfigFile;
	/**
	 * Returns the metadata of this tool. The metadata consists of a unique identifier for the tool, a human readable name of the tool, an array of names
	 * of folders under which the tool should be displayed in YouScope (could be empty to display it as a default tool), and similar. To not have to
	 * implement all functions of the interface {@link ToolMetadata} ourselves, we return an instance of the adapter class {@link ToolMetadataAdapter}, which does most of the
	 * boilerplate code for us.
	 * @return Metadata of tool.
	 */
	public static ToolMetadata getMetadata()
	{
		return new ToolMetadataAdapter(TYPE_IDENTIFIER, "Modify and continue", new String[]{"Misc"}, 
				"Allows to modify the measurement, and continue saving the measurement into the same folder.",
				(String) null);
	}
	
	public ContinuationMeasurementPostProcessing(YouScopeClient client, YouScopeServer server, MeasurementFileLocations lastFileLocations) throws AddonException
	{
		super(getMetadata(), client, server); 
		lastFolder = lastFileLocations.getMeasurementBaseFolder();
		lastConfigFile = lastFileLocations.getMeasurementConfigurationFilePath();
	}
	@Override
	public java.awt.Component createUI()
	{
		/**
		 *  This is the place to setup the UI elements of the first window of your tool.
		 */
		// Title of the window, as well as basic configuration.
		setMaximizable(false);
		setResizable(true);
		setTitle("Modify and continue measurement");
		setShowCloseButton(false);
		SelectMeasurementPanel mainPanel = new SelectMeasurementPanel(getClient(), lastFolder, lastConfigFile);
		mainPanel.addSelectionListener(new SelectionListener() {
			
			@Override
			public void selectionMade(MeasurementConfiguration encapsulatedMeasurement, String folder, long deltaEvaluationNumber,
					long previousRuntime) 
			{ 
				ContinuationMeasurementConfiguration configuration = new ContinuationMeasurementConfiguration();
				configuration.setDeltaEvaluationNumber(deltaEvaluationNumber);
				configuration.setEncapsulatedConfiguration(encapsulatedMeasurement);
				configuration.setMeasurementFolder(folder);
				configuration.setPreviousRuntime(previousRuntime);
				getClient().editMeasurement(configuration);
				closeAddon();
			}
		});
        return mainPanel;
	}
}
