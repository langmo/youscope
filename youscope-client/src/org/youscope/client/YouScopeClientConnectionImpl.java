/**
 * 
 */
package org.youscope.client;

import javax.script.ScriptEngineFactory;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonUI;
import org.youscope.addon.component.ComponentAddonUIListener;
import org.youscope.addon.measurement.MeasurementAddonFactory;
import org.youscope.addon.microplate.MicroplateAddonFactory;
import org.youscope.addon.postprocessing.PostProcessorAddonFactory;
import org.youscope.addon.tool.ToolAddonFactory;
import org.youscope.clientinterfaces.ClientAddonProvider;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.clientinterfaces.YouScopeProperties;
import org.youscope.common.configuration.MeasurementConfiguration;
import org.youscope.common.measurement.Measurement;

/**
 * @author Moritz Lang
 *
 */
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
    public Iterable<MeasurementAddonFactory> getMeasurementAddons()
    {
        return ClientSystem.getMeasurementAddons();
    }

    @Override
    public Iterable<ToolAddonFactory> getToolAddons()
    {
        return ClientSystem.getToolAddons();
    }

    @Override
    public MeasurementAddonFactory getMeasurementAddon(String addonID)
    {
        return ClientSystem.getMeasurementAddon(addonID);
    }

    @Override
    public ToolAddonFactory getToolAddon(String addonID)
    {
        return ClientSystem.getToolAddon(addonID);
    }

    @Override
    public Iterable<MicroplateAddonFactory> getMicroplateTypeAddons()
    {
        return ClientSystem.getMicroplateTypeAddons();
    }

    @Override
    public MicroplateAddonFactory getMicroplateTypeAddon(String addonID)
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
    public Iterable<PostProcessorAddonFactory> getMeasurementPostProcessorAddons()
    {
        return ClientSystem.getMeasurementPostProcessorAddons();
    }

    @Override
    public PostProcessorAddonFactory getMeasurementPostProcessorAddon(String addonID)
    {
        return ClientSystem.getMeasurementPostProcessorAddon(addonID);
    }

    @Override
    public boolean editMeasurement(MeasurementConfiguration configuration)
    {
        MeasurementAddonFactory addonFactory = ClientSystem.getMeasurementAddon(configuration.getTypeIdentifier());
        if (addonFactory == null)
        {
            ClientSystem.err.println("Could not find addon to configure measurement of type " + configuration.getTypeIdentifier() + ".");
            return false;
        }
        ComponentAddonUI<? extends MeasurementConfiguration> addon;
		try {
			addon = addonFactory.createMeasurementUI(configuration.getTypeIdentifier(), new YouScopeClientConnectionImpl(),
			        YouScopeClientImpl.getServer());
		} catch (AddonException e1) {
			ClientSystem.err.println("Cannot create measurement configuration UI.", e1);
            return false;
		}
        try
        {
            addon.setConfiguration(configuration);
        } catch (Exception e)
        {
            ClientSystem.err.println("Cannot load measurement configuration.", e);
            return false;
        }
        addon.addUIListener(new ComponentAddonUIListener<MeasurementConfiguration>()
        {

        	@Override
			public void configurationFinished(MeasurementConfiguration configuration)
            {
                Measurement measurement = YouScopeClientImpl.addMeasurement(configuration);
                if (measurement == null)
                {
                    editMeasurement(configuration);
                    return;
                }
            }
        });
        YouScopeFrame confFrame;
		try {
			confFrame = addon.toFrame();
		} catch (AddonException e) {
			ClientSystem.err.println("Cannot create measurement configuration UI.", e);
            return false;
		}
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
