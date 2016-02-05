package org.youscope.plugin.dropletmicrofluidics;

import java.io.Serializable;
import java.rmi.RemoteException;

import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.ExecutionInformation;
import org.youscope.common.callback.CallbackException;
import org.youscope.common.table.Table;
class DropletMicrofluidicJobCallbackImpl implements DropletMicrofluidicJobCallback
{
    private static DropletMicrofluidicJobCallbackUI ui = null;
    private final YouScopeClient client;
    
    DropletMicrofluidicJobCallbackImpl(final YouScopeClient client)
    {
        this.client = client;
    }
    
    @Override
	public void dropletMeasured(ExecutionInformation executionInformation, Table table) throws CallbackException
	{
    	if(ui == null)
    		throw new CallbackException("Callback not initialized");
    	ui.dropletMeasured(executionInformation, table);
	}

	@Override
	public void pingCallback() throws RemoteException {
		// do nothing.
	}

	@Override
	public synchronized void initializeCallback(Serializable... arguments) throws RemoteException, CallbackException 
	{
		if(ui == null || ui.isReceivedData())
		{
			ui = new DropletMicrofluidicJobCallbackUI(client);
		}
		ui.initializeCallback(arguments);
	}

	@Override
	public synchronized void uninitializeCallback() throws RemoteException, CallbackException {
		if(ui != null)
			ui.uninitializeCallback();
	}

	@Override
	public String getTypeIdentifier() throws RemoteException {
		return TYPE_IDENTIFIER;
	}
}
