/**
 * 
 */
package org.youscope.plugin.nemesys;

import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.configuration.JobConfiguration;
import org.youscope.common.configuration.TableConsumerConfiguration;
import org.youscope.common.configuration.TableProducerConfiguration;
import org.youscope.common.table.TableDefinition;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This job takes controls the syringes of a Nemesys device.
 * 
 * @author Moritz Lang
 */
@XStreamAlias("nemesys-job")
public class NemesysJobConfiguration extends JobConfiguration implements TableConsumerConfiguration, TableProducerConfiguration
{

	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -8465192595299726111L;

	/**
	 * The identifier for this measurement type.
	 */
	public static final String	TYPE_IDENTIFIER		= "CSB::NemesysJob";
	
	/**
	 * Script engine name representing that the script to control the syringes is a simple time-table, for which an own interpreter is written.
	 */
	public static final String SCRIPT_ENGINE_TIMETABLE = "Time Table";
	
	@XStreamAlias("script")
	private String script = "";
	
	@XStreamAlias("script-engine")
	private String scriptEngine = SCRIPT_ENGINE_TIMETABLE;
	

	@XStreamAlias("nemesys-device")
	private String nemesysDevice = null;
	
	@XStreamAlias("table-save-name")
	private String tableSaveName = "nemesys-state";
	
	@Override 
	public String getTypeIdentifier()
	{
		return TYPE_IDENTIFIER;
	}
	
	@Override
	public String getDescription()
	{
		return "<p>Nemesys syringe control</p>";
	}

	/**
	 * Sets the script which controls the syringes, i.e. how the flow rates vary over time.
	 * @param script The script to set.
	 */
	public void setScript(String script)
	{
		if(script == null)
			this.script = "";
		else
			this.script = script;
	}

	/**
	 * Returns the script which controls the syringes, i.e. how the flow rates vary over time.
	 * @return Nemesys syringe control script.
	 */
	public String getScript()
	{
		return script;
	}

	/**
	 * Sets the name of the script engine with which the script should be executed.
	 * @param scriptEngine Name of the script engine.
	 */
	public void setScriptEngine(String scriptEngine)
	{
		if(scriptEngine == null)
			this.scriptEngine = "Mozilla Rhino";
		else
			this.scriptEngine = scriptEngine;
	}

	/**
	 * Returns the name of the script engine with which the script should be executed.
	 * @return Name of the script engine.
	 */
	public String getScriptEngine()
	{
		return scriptEngine;
	}

	/**
	 * Sets the ID of the Nemesys device which should be controlled. An error will be thrown later in the job initialization
	 * if the device does not exist or is not a Nemesys device.
	 * @param nemesysDevice The ID of the nemesys device.
	 */
	public void setNemesysDevice(String nemesysDevice)
	{
		this.nemesysDevice = nemesysDevice;
	}

	/**
	 * Returns the ID of the Nemesys device which should be controlled, or null if yet not set.
	 * @return The ID of the nemesys device, or null.
	 */
	public String getNemesysDevice()
	{
		return nemesysDevice;
	}

	@Override
	public TableDefinition getConsumedTableDefinition()
	{
		return NemesysControlTable.getTableDefinition();
	}

	@Override
	public TableDefinition getProducedTableDefinition()
	{
		return NemesysStateTable.getTableDefinition();
	}

	/**
	 * Sets the name (without extension) of the file to which the flow rate and  volume information of the syringe pumps should be stored during evaluation. 
	 * Set to null to not store anything to a file.
	 * @param tableSaveName Name of the file (without extension) where the Nemesys state should be saved, or null.
	 */
	public void setTableSaveName(String tableSaveName)
	{
		this.tableSaveName = tableSaveName;
	}

	/**
	 * Returns the name (without extension) of the file to which the flow rate and  volume information of the syringe pumps should be stored during evaluation. 
	 * Returns null if not storing anything to a file.
	 * @return Name of the file (without extension) where the Nemesys state should be saved, or null.
	 */
	public String getTableSaveName()
	{
		return tableSaveName;
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		super.checkConfiguration();
		if(nemesysDevice == null)
			throw new ConfigurationException("No Nemesys device selected.");
	}
}
