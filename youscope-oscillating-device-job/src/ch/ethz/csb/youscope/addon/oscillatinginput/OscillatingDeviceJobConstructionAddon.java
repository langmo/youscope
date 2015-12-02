/**
 * 
 */
package ch.ethz.csb.youscope.addon.oscillatinginput;

import java.net.URL;
import java.rmi.RemoteException;

import ch.ethz.csb.youscope.server.addon.ConstructionContext;
import ch.ethz.csb.youscope.server.addon.job.JobConstructionAddon;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;
import ch.ethz.csb.youscope.shared.measurement.ComponentCreationException;
import ch.ethz.csb.youscope.shared.measurement.MeasurementRunningException;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;
import ch.ethz.csb.youscope.shared.measurement.job.Job;
import ch.ethz.csb.youscope.shared.measurement.job.JobCreationException;
import ch.ethz.csb.youscope.shared.measurement.job.basicjobs.DeviceSettingJob;
import ch.ethz.csb.youscope.shared.measurement.job.basicjobs.ScriptingJob;

/**
 * @author langmo
 * 
 */
class OscillatingDeviceJobConstructionAddon implements JobConstructionAddon
{

	@Override
	public Job createJob(JobConfiguration generalJobConfiguration, ConstructionContext initializer, PositionInformation positionInformation) throws RemoteException, ConfigurationException, JobCreationException
	{
		if(!(generalJobConfiguration instanceof OscillatingDeviceJobConfigurationDTO))
		{
			throw new ConfigurationException("Configuration is not supported by this addon.");
		}
		OscillatingDeviceJobConfigurationDTO jobConfiguration = (OscillatingDeviceJobConfigurationDTO)generalJobConfiguration;
		
		// Create job.
		ScriptingJob job;
		try {
			job = initializer.getComponentProvider().createJob(positionInformation, ScriptingJob.DEFAULT_TYPE_IDENTIFIER, ScriptingJob.class);
		} catch (ComponentCreationException e1) {
			throw new JobCreationException("Oscillating device jobs need the scripting job plugin.", e1);
		}
		
		// Set script file
		URL scriptURL = getClass().getClassLoader().getResource("ch/ethz/csb/youscope/addon/oscillatinginput/oscillating_device.js");
		if(scriptURL == null)
			throw new ConfigurationException("Could not load script file as resource. Check consistency of JAR file.");
		try
		{
			job.setScriptFile(scriptURL);
		}
		catch(Exception e)
		{
			throw new ConfigurationException("Could not load script file as resource. Syntax of script file "+ scriptURL.toString() + " invalid.", e);
		}
		
		// Initialize parameters of script file
		try
		{
			job.putVariable("device", jobConfiguration.getDevice());
			job.putVariable("property", jobConfiguration.getProperty());
			job.putVariable("minValue", jobConfiguration.getMinValue());
			job.putVariable("maxValue", jobConfiguration.getMaxValue());
			job.putVariable("periodLength", jobConfiguration.getPeriodLength());
			job.putVariable("initialPhase", jobConfiguration.getInitialPhase());
	
			// Set engine to default JavaScript engine.
			job.setScriptEngine("Mozilla Rhino");
			
			// Add sub job to change position.
			DeviceSettingJob devJob;
			try {
				devJob = initializer.getComponentProvider().createJob(positionInformation, DeviceSettingJob.DEFAULT_TYPE_IDENTIFIER, DeviceSettingJob.class);
			} catch (ComponentCreationException e) {
				throw new JobCreationException("Oscillating device jobs need the device setting job plugin.", e);
			}
			job.addJob(devJob);
		}
		catch(MeasurementRunningException e)
		{
			throw new ConfigurationException("Newly created job already running.", e);
		}
		return job;
	}

	
}
