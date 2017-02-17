/*******************************************************************************
 * Copyright (c) 2017 Moritz Lang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Moritz Lang - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package org.youscope.client;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonUI;
import org.youscope.addon.component.ComponentAddonUIListener;
import org.youscope.clientinterfaces.ClientAddonProvider;
import org.youscope.clientinterfaces.MetadataDefinitionManager;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.clientinterfaces.PropertyProvider;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.measurement.MeasurementConfiguration;

/**
 * @author Moritz Lang
 *
 */
class YouScopeClientConnectionImpl implements YouScopeClient
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
    public boolean isLocalServer()
    {
        return ClientSystem.isLocalServer();
    }

    @Override
    public PropertyProvider getPropertyProvider()
    {
        return PropertyProviderImpl.getInstance();
    }

    @Override
    public void sendError(String message)
    {
        ClientSystem.err.println(message);
    }

    @Override
    public boolean editMeasurement(MeasurementConfiguration configuration)
    {
        ComponentAddonUI<? extends MeasurementConfiguration> addon;
		try {
			addon = ClientAddonProviderImpl.getProvider().createComponentUI(configuration);
		} catch (AddonException e1) {
			ClientSystem.err.println("Cannot create measurement configuration UI.", e1);
            return false;
		} catch (ConfigurationException e) {
			ClientSystem.err.println("Configuration of measurement invalid.", e);
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

	@Override
	public MetadataDefinitionManager getMeasurementMetadataProvider() {
		return MetadataManagerImpl.getInstance();
	}

   
}
