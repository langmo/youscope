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
package org.youscope.server;

import java.rmi.RemoteException;

import javax.script.ScriptEngineManager;

import org.youscope.common.MessageListener;
import org.youscope.common.callback.Callback;
import org.youscope.common.callback.CallbackCreationException;
import org.youscope.common.callback.CallbackProvider;
import org.youscope.common.image.ImageEvent;
import org.youscope.common.image.ImageListener;
import org.youscope.common.saving.MeasurementFileLocations;
import org.youscope.common.saving.MeasurementSaver;
import org.youscope.common.saving.SaveSettings;
import org.youscope.common.table.Table;
import org.youscope.common.table.TableListener;
import org.youscope.serverinterfaces.ComponentProvider;
import org.youscope.serverinterfaces.ConstructionContext;

/**
 * @author langmo
 */
class ConstructionContextImpl implements ConstructionContext
{
	private final ComponentProvider 				componentProvider;
	private final MeasurementSaver				measurementSaver;
	private final CallbackProvider	callbackProvider;

	public ConstructionContextImpl(MeasurementSaver measurementSaver, CallbackProvider callbackProvider) throws RemoteException
	{
		this.componentProvider = new ComponentProviderImpl(this);
		if(measurementSaver != null)
			this.measurementSaver = measurementSaver;
		else
			this.measurementSaver = getDummyMeasurementSaver();
		if(callbackProvider != null)
			this.callbackProvider = callbackProvider;
		else
			this.callbackProvider = getDummyMeasurementCallbackProvider();
	}

	private static MeasurementSaver getDummyMeasurementSaver()
	{
		return new MeasurementSaver()
		{

			@Override
			public ImageListener getSaveImageListener(String imageSaveName) throws RemoteException
			{
				return new ImageListener()
				{
					@Override
					public void imageMade(ImageEvent<?> e) throws RemoteException
					{
						// Do nothing (dummy).
					}
				};
			}

			@Override
			public TableListener getSaveTableListener(String tableSaveName) throws RemoteException
			{
				return new TableListener()
				{
					@Override
					public void newTableProduced(Table table) throws RemoteException
					{
						// Do nothing (dummy).
					}
				};
			}

			@Override
			public void setSaveSettings(SaveSettings saveSettings)
			{
				// Do nothing (dummy).
			}

			@Override
			public SaveSettings getSaveSettings() throws RemoteException
			{
				return null;
			}

			@Override
			public MeasurementFileLocations getLastMeasurementFileLocations() throws RemoteException {
				return null;
			}
		};
	}

	private static CallbackProvider getDummyMeasurementCallbackProvider()
	{
		return new CallbackProvider()
		{
			@Override
			public Callback createCallback(String typeIdentifier) throws CallbackCreationException {
				throw new CallbackCreationException("Callback with identifier " + typeIdentifier + " not available.");
			}

			@Override
			public <T extends Callback> T createCallback(String typeIdentifier, Class<T> callbackInterface)
					throws CallbackCreationException 
			{
				throw new CallbackCreationException("Callback with identifier " + typeIdentifier + " not available.");
			}
		};
	}

	
	
	@Override
	public MeasurementSaver getMeasurementSaver()
	{
		return measurementSaver;
	}

	@Override
	public CallbackProvider getCallbackProvider()
	{
		return callbackProvider;
	}

	@Override
	public ScriptEngineManager getScriptEngineManager()
	{
		return ServerSystem.getScriptEngineManager();
	}

	@Override
	public MessageListener getLogger()
	{
		return new MessageListener()
				{

					@Override
					public void sendMessage(String message)
					{
						ServerSystem.out.println(message);						
					}

					@Override
					public void sendErrorMessage(String message, Throwable error) 
					{
						ServerSystem.err.println(message, error);
					}
			
				};
	}
	
	@Override
	public ComponentProvider getComponentProvider() {
		return componentProvider;
	}
}
