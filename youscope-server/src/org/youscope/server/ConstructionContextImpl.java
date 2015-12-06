/**
 * 
 */
package org.youscope.server;

import java.rmi.RemoteException;
import java.util.UUID;

import javax.script.ScriptEngineManager;

import org.youscope.addon.component.ComponentProvider;
import org.youscope.addon.component.ConstructionContext;
import org.youscope.common.ImageEvent;
import org.youscope.common.ImageListener;
import org.youscope.common.MessageListener;
import org.youscope.common.configuration.MeasurementConfiguration;
import org.youscope.common.measurement.MeasurementSaveSettings;
import org.youscope.common.measurement.MeasurementSaver;
import org.youscope.common.measurement.callback.Callback;
import org.youscope.common.measurement.callback.CallbackCreationException;
import org.youscope.common.measurement.callback.CallbackProvider;
import org.youscope.common.table.Table;
import org.youscope.common.table.TableListener;

/**
 * @author langmo
 */
class ConstructionContextImpl implements ConstructionContext
{
	private final ComponentProvider 				componentProvider;
	private final MeasurementSaver				measurementSaver;
	private final CallbackProvider	callbackProvider;
	private final UUID measurementUUID;

	public ConstructionContextImpl(MeasurementSaver measurementSaver, CallbackProvider callbackProvider, UUID measurementUUID) throws RemoteException
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
		this.measurementUUID = measurementUUID;
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
					public void imageMade(ImageEvent e) throws RemoteException
					{
						// Do nothing (dummy).
					}
				};
			}

			@Override
			public TableListener getSaveTableDataListener(String tableSaveName) throws RemoteException
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
			public void setSaveSettings(MeasurementSaveSettings saveSettings)
			{
				// Do nothing (dummy).
			}

			@Override
			public MeasurementSaveSettings getSaveSettings() throws RemoteException
			{
				return null;
			}

			@Override
			public String getLastMeasurementFolder() throws RemoteException
			{
				return null;
			}

			@Override
			public MeasurementConfiguration getConfiguration() throws RemoteException
			{
				return null;
			}

			@Override
			public void setConfiguration(MeasurementConfiguration configuration)
			{
				// Do nothing (dummy)
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

	@Override
	public UUID getMeasurementUUID() {
		return measurementUUID;
	}
}
