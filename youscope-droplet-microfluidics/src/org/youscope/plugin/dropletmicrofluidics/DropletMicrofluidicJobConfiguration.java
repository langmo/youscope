/**
 * 
 */
package org.youscope.plugin.dropletmicrofluidics;

import org.youscope.addon.dropletmicrofluidics.DropletControllerConfiguration;
import org.youscope.addon.dropletmicrofluidics.DropletObserverConfiguration;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.configuration.JobConfiguration;
import org.youscope.common.configuration.TableProducerConfiguration;
import org.youscope.common.table.TableDefinition;
import org.youscope.plugin.autofocus.AutoFocusJobConfiguration;
import org.youscope.plugin.brentfocussearch.BrentFocusSearchConfiguration;
import org.youscope.plugin.dropletmicrofluidics.defaultobserver.DefaultObserverConfiguration;
import org.youscope.plugin.dropletmicrofluidics.tablecontroller.TableControllerConfiguration;
import org.youscope.plugin.simplefocusscores.AutocorrelationFocusScoreConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Configuration for a job which automatically searches for the focal plane.
 * @author Moritz Lang
 *
 */
@XStreamAlias("droplet-based-microfluidics-job")
public class DropletMicrofluidicJobConfiguration extends JobConfiguration implements TableProducerConfiguration
{
	/**
	 * Serial version UID.
	 */
	private static final long	serialVersionUID	= 7911732041188941111L;
	
	/**
	 * The identifier for this job type.
	 */
	public static final String	TYPE_IDENTIFIER	= "IST::DropletBasedMicrofluidicsJob";

	@XStreamAlias("autofocus-configuration")
	private AutoFocusJobConfiguration autofocusConfiguration = new AutoFocusJobConfiguration();
	
	@XStreamAlias("controller-configuration")
	private DropletControllerConfiguration controllerConfiguration = new TableControllerConfiguration();
	
	@XStreamAlias("observer-configuration")
	private DropletObserverConfiguration observerConfiguration = new DefaultObserverConfiguration();
	
	@XStreamAlias("nemesys-device")
	private String nemesysDevice = null;
	
	/**
	 * Default name for droplet table.
	 */
	public static final String DROPLET_TABLE_DEFAULT_NAME = "droplet";
	/**
	 * The name under which the droplet table should be saved.
	 */
	@XStreamAlias("droplet-table-save-name")
	private String dropletTableSaveName = DROPLET_TABLE_DEFAULT_NAME;
	
	
	/**
	 * Constructor.
	 */
	public DropletMicrofluidicJobConfiguration()
	{
		AutocorrelationFocusScoreConfiguration focusScore = new AutocorrelationFocusScoreConfiguration();	
		autofocusConfiguration.setFocusScoreAlgorithm(focusScore);
		BrentFocusSearchConfiguration focusSearch = new BrentFocusSearchConfiguration();
		focusSearch.setFocusLowerBound(-100);
		focusSearch.setFocusUpperBound(100);
		focusSearch.setMaxSearchSteps(100);
		focusSearch.setTolerance(0.3);
		autofocusConfiguration.setFocusSearchAlgorithm(focusSearch);
		autofocusConfiguration.setRememberFocus(true);
		autofocusConfiguration.setResetFocusAfterSearch(false);
	}
	
	/**
	 * Returns the file name (without extension) under which the droplet table should
	 * be saved. Returns null if droplet table is not saved.
	 * @return Droplet table save name, or null.
	 */
	public String getDropletTableSaveName()
	{
		return dropletTableSaveName;
	}

	/**
	 * Sets the file name (without extension) under which the droplet table should
	 * be saved. Set to null to not save droplet table.
	 * @param dropletTableSaveName Droplet table save name, or null.
	 */
	public void setDropletTableSaveName(String dropletTableSaveName)
	{
		this.dropletTableSaveName = dropletTableSaveName;
	}

	/**
	 * Returns the name of the Nemesys device. Initially null, i.e. unset.
	 * @return nemesys device name.
	 */
	public String getNemesysDevice() {
		return nemesysDevice;
	}

	/**
	 * Sets the name of the Nemesys device.
	 * @param nemesysDevice nemesys device name.
	 */
	public void setNemesysDevice(String nemesysDevice) {
		this.nemesysDevice = nemesysDevice;
	}
	
	@Override
	public String getTypeIdentifier()
	{
		return TYPE_IDENTIFIER;
	}

	/**
	 * Returns the configuration for the autofocus.
	 * @return AUtofocus configuration.
	 */
	public AutoFocusJobConfiguration getAutofocusConfiguration() {
		return autofocusConfiguration;
	}

	/**
	 * Sets the configuration for the autofocus.
	 * @param autofocusConfiguration Autofocus configuration.
	 */
	public void setAutofocusConfiguration(
			AutoFocusJobConfiguration autofocusConfiguration) {
		this.autofocusConfiguration = autofocusConfiguration;
	}

	@Override
	public String getDescription() {
		return "Droplet based microfluidics";
	}

	/**
	 * Returns the configuration of the controll algorithm.
	 * @return Control algorithm configuration.
	 */
	public DropletControllerConfiguration getControllerConfiguration() {
		return controllerConfiguration;
	}

	/**
	 * Sets the configuration of the controll algorithm.
	 * @param controllerConfiguration Control algorithm configuration.
	 */
	public void setControllerConfiguration(DropletControllerConfiguration controllerConfiguration) {
		this.controllerConfiguration = controllerConfiguration;
	}

	/**
	 * Returns the configuration of the observer of the droplet heights.
	 * @return Droplet height observer.
	 */
	public DropletObserverConfiguration getObserverConfiguration() {
		return observerConfiguration;
	}
	/**
	 * Sets the configuration of the observer of the droplet heights.
	 * @param observerConfiguration Droplet height observer configuraion.
	 */
	public void setObserverConfiguration(DropletObserverConfiguration observerConfiguration) {
		this.observerConfiguration = observerConfiguration;
	}

	@Override
	public TableDefinition getProducedTableDefinition() {
		return DropletMicrofluidicTable.getTableDefinition();
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		super.checkConfiguration();
		
		if(autofocusConfiguration == null)
			throw new ConfigurationException("Autofocus not configured.");
		autofocusConfiguration.checkConfiguration();
		if(controllerConfiguration == null)
			throw new ConfigurationException("Controller not configured.");
		controllerConfiguration.checkConfiguration();
		if(nemesysDevice == null || nemesysDevice.length() <=0)
			throw new ConfigurationException("Nemesys device not set.");
		if(observerConfiguration == null)
			throw new ConfigurationException("Observer not configured.");
		observerConfiguration.checkConfiguration();
	}

}
