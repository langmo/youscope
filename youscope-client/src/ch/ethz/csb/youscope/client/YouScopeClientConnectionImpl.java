/**
 * 
 */
package ch.ethz.csb.youscope.client;

import javax.script.ScriptEngineFactory;

import ch.ethz.csb.youscope.client.addon.ClientAddonProvider;
import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.YouScopeFrame;
import ch.ethz.csb.youscope.client.addon.YouScopeProperties;
import ch.ethz.csb.youscope.client.addon.job.JobConfigurationAddonFactory;
import ch.ethz.csb.youscope.client.addon.measurement.MeasurementConfigurationAddon;
import ch.ethz.csb.youscope.client.addon.measurement.MeasurementConfigurationAddonFactory;
import ch.ethz.csb.youscope.client.addon.measurement.MeasurementConfigurationAddonListener;
import ch.ethz.csb.youscope.client.addon.microplatetype.MicroplateTypeFactory;
import ch.ethz.csb.youscope.client.addon.postprocessing.MeasurementPostProcessorAddonFactory;
import ch.ethz.csb.youscope.client.addon.tool.ToolAddonFactory;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.MeasurementConfiguration;
import ch.ethz.csb.youscope.shared.measurement.Measurement;

/**
 * @author Moritz Lang
 *
 */
@SuppressWarnings("deprecation")
class YouScopeClientConnectionImpl implements YouScopeClient, YouScopeProperties
{
	
	
	@Override
    public void sendError(String message, Throwable error)
    {
        ClientSystem.err.println(message, error);
    }

    @Override
    public void sendMessage(String message)
    {
        ClientSystem.out.println(message);
    }

    @Override
    public YouScopeFrame createFrame()
    {
        return YouScopeFrameImpl.createTopLevelFrame();
    }

    @Override
    public String getProperty(String name, String defaultValue)
    {
        return ConfigurationSettings.getProperty(name, defaultValue);
    }

    @Override
    public int getProperty(String name, int defaultValue)
    {
        return ConfigurationSettings.getProperty(name, defaultValue);
    }

    @Override
    public double getProperty(String name, double defaultValue)
    {
        return ConfigurationSettings.getProperty(name, defaultValue);
    }

    @Override
    public boolean getProperty(String name, boolean defaultValue)
    {
        return ConfigurationSettings.getProperty(name, defaultValue);
    }

    @Override
    public String[] getProperty(String name, String[] defaultValue)
    {
        return ConfigurationSettings.getProperty(name, defaultValue);
    }
    
    @Override
    public void setProperty(String name, String value)
    {
        ConfigurationSettings.setProperty(name, value);
    }

    @Override
    public void setProperty(String name, int value)
    {
        ConfigurationSettings.setProperty(name, value);
    }

    @Override
    public void setProperty(String name, double value)
    {
        ConfigurationSettings.setProperty(name, value);
    }

    @Override
    public void setProperty(String name, boolean value)
    {
        ConfigurationSettings.setProperty(name, value);
    }

    @Override
    public boolean isLocalServer()
    {
        return ClientSystem.isLocalServer();
    }

    @Override
    @Deprecated
    public Iterable<JobConfigurationAddonFactory> getJobAddons()
    {
        return ClientSystem.getJobAddons();
    }

    @Override
    public Iterable<MeasurementConfigurationAddonFactory> getMeasurementAddons()
    {
        return ClientSystem.getMeasurementAddons();
    }

    @Override
    public Iterable<ToolAddonFactory> getToolAddons()
    {
        return ClientSystem.getToolAddons();
    }

    @Override
    @Deprecated
    public JobConfigurationAddonFactory getJobAddon(String addonID)
    {
        return ClientSystem.getJobAddon(addonID);
    }

    @Override
    public MeasurementConfigurationAddonFactory getMeasurementAddon(String addonID)
    {
        return ClientSystem.getMeasurementAddon(addonID);
    }

    @Override
    public ToolAddonFactory getToolAddon(String addonID)
    {
        return ClientSystem.getToolAddon(addonID);
    }

    @Override
    public Iterable<MicroplateTypeFactory> getMicroplateTypeAddons()
    {
        return ClientSystem.getMicroplateTypeAddons();
    }

    @Override
    public MicroplateTypeFactory getMicroplateTypeAddon(String addonID)
    {
        return ClientSystem.getMicroplateTypeAddon(addonID);
    }

    @Override
    public YouScopeProperties getProperties()
    {
        return this;
    }

    @Override
    public Iterable<ScriptEngineFactory> getScriptEngineFactories()
    {
        return ClientSystem.getScriptEngines();
    }

    @Override
    public ScriptEngineFactory getScriptEngineFactory(String engineName)
    {
        return ClientSystem.getScriptEngine(engineName);
    }

    @Override
    public void sendError(String message)
    {
        ClientSystem.err.println(message);
    }

    @Override
    public Iterable<MeasurementPostProcessorAddonFactory> getMeasurementPostProcessorAddons()
    {
        return ClientSystem.getMeasurementPostProcessorAddons();
    }

    @Override
    public MeasurementPostProcessorAddonFactory getMeasurementPostProcessorAddon(String addonID)
    {
        return ClientSystem.getMeasurementPostProcessorAddon(addonID);
    }

    @Override
    public boolean editMeasurement(MeasurementConfiguration configuration)
    {
        MeasurementConfigurationAddonFactory addonFactory = ClientSystem.getMeasurementAddon(configuration.getTypeIdentifier());
        if (addonFactory == null)
        {
            ClientSystem.err.println("Could not find addon to configure measurement of type " + configuration.getTypeIdentifier() + ".");
            return false;
        }
        MeasurementConfigurationAddon addon =
                addonFactory.createMeasurementConfigurationAddon(configuration.getTypeIdentifier(), new YouScopeClientConnectionImpl(),
                        YouScopeClientImpl.getServer());
        try
        {
            addon.setConfigurationData(configuration);
        } catch (ConfigurationException e)
        {
            ClientSystem.err.println("Could not initialize addon to configure measurement with existing measurement configuration.", e);
            return false;
        }
        YouScopeFrame confFrame = YouScopeFrameImpl.createTopLevelFrame();
        addon.addConfigurationListener(new MeasurementConfigurationAddonListener()
        {

            @Override
            public void measurementConfigurationFinished(MeasurementConfiguration configuration)
            {
                Measurement measurement = YouScopeClientImpl.addMeasurement(configuration);
                if (measurement == null)
                {
                    editMeasurement(configuration);
                    return;
                }
            }
        });
        addon.createUI(confFrame);
        confFrame.setVisible(true);
        return true;
    }

    @Override
    public Measurement initializeMeasurement(MeasurementConfiguration configuration)
    {
        return YouScopeClientImpl.addMeasurement(configuration);
    }

    @Override
    public boolean initializeMeasurement(Measurement measurement)
    {
        return YouScopeClientImpl.addMeasurement(measurement);
    }

	@Override
	public MeasurementConfiguration[] getLastSavedMeasurements() {
	    return LastMeasurementManager.getLastMeasurementManager().getMeasurements();
	}

	@Override
	public ClientAddonProvider getAddonProvider() {
		return ClientAddonProviderImpl.getProvider();
	}

   
}
