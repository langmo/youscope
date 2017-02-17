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
package org.youscope.plugin.openbis;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.Vector;


/**
 * @author Moritz Lang
 *
 */
public class OpenBISAddonImpl extends UnicastRemoteObject implements OpenBISAddon, Runnable
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 2803347419574073428L;
	private final Vector<OpenBISListener> transferListeners = new Vector<OpenBISListener>();
	private volatile boolean running = false;
	private Process transferProcess = null;
	private ProcessBuilder touchProcessBuilder = null;
	
	/**
	 * Constructor
	 * @throws RemoteException
	 */
	public OpenBISAddonImpl() throws RemoteException
	{
		super();
	}

	@Override
	public String getAddonName() throws RemoteException
	{
		return "OpenBIS Uploader";
	}

	@Override
	public float getAddonVersion() throws RemoteException
	{
		return 1.0F;
	}

	@Override
	public String getAddonDescription() throws RemoteException
	{
		return "Addon to transfer measurement data (e.g. images and meta-data) to OpenBIS.\n"
			+ "OpenBIS is a database for microscopy data, exspecially for screening data.\n"
			+ "Visit http://www.cisd.ethz.ch/software/openBIS for more information.";
	}

	@Override
	public String getAddonID() throws RemoteException
	{
		return "YouScope.OpenBISUploader";
	}

	@Override
	public synchronized void transferMeasurement(String sshUser, String sshServer, String sshDirectory, String openBISUser, String projectIdentifier, String measurementIdentifier, String measurementFolder, boolean overwrite) throws RemoteException, OpenBISException
	{
		if(running)
			throw new OpenBISException("Another transfer is already running. Please wait until the transfer is finished or create a different object to run transfers in parallel.");
		
		// Check if all necessary files exist
		File openBISDirectory = new File("openbis");
		if(!openBISDirectory.exists() || !openBISDirectory.isDirectory())
			throw new OpenBISException("Directory " + openBISDirectory.getAbsolutePath() + " does not exist. Check the OpenBIS addon installation for consistency.");
		// TODO: Currently only Windows.
		File rsyncFile = new File(openBISDirectory, "rsync.exe");
		if(!rsyncFile.exists() || !rsyncFile.isFile())
			throw new OpenBISException("RSync executable " + rsyncFile.getAbsolutePath() + " does not exist. Check the OpenBIS addon installation for consistency.");
		File sshFile = new File(openBISDirectory, "ssh.exe");
		if(!sshFile.exists() || !sshFile.isFile())
			throw new OpenBISException("SSH executable " + sshFile.getAbsolutePath() + " does not exist. Check the OpenBIS addon installation for consistency.");
		
		running = true;
		
		// Claim to just send a good old dos command
		String rsyncCommand = "rsync -rltdv --chmod u+rwx " + toUnixPath(measurementFolder) + " " + sshUser + "@" + sshServer + ":" + sshDirectory + "/" + openBISUser+"_" + projectIdentifier + "_" + measurementIdentifier;
		notifyTransferProgress(0, "> " + rsyncCommand +"\n");
		
		// Create a process builder for rsync
		ProcessBuilder rsyncProcessBuilder = new ProcessBuilder(new String[]
		{		
				rsyncFile.getAbsolutePath(),
				"-rltdv", // recursive, preserve symlinks, preserve times, copy directories without recursion
				"--chmod", 	// Together with the following line:
				"u+rwx", 	// Give the OpenBIS server the right to read and write to its own files (otherwise, it can happen that rsync creates a folder at the server side without giving itself the permission to access it.
				toUnixPath(measurementFolder),       
				sshUser + "@" + sshServer + ":" + sshDirectory + "/" + openBISUser+"_" + projectIdentifier + "_" + measurementIdentifier     
		});                 
		Map<String, String> rsyncEnvironment = rsyncProcessBuilder.environment();                 
		rsyncEnvironment.put("Path", openBISDirectory.getAbsolutePath());//+";" + env.get("PATH"));                 
		rsyncEnvironment.put("HOME", openBISDirectory.getAbsolutePath());
		rsyncEnvironment.put("RSYNC_RSH", "ssh.exe");
		rsyncProcessBuilder.directory(openBISDirectory.getAbsoluteFile());                 
		
		// Create a process builder for ssh
		touchProcessBuilder = new ProcessBuilder(new String[]
   		{		
				sshFile.getAbsolutePath(),
   				sshUser + "@" + sshServer,
   				"touch "  + sshDirectory + "/.MARKER_is_finished_" + openBISUser+"_" + projectIdentifier + "_" + measurementIdentifier, 
   		});                 
   		Map<String, String> touchEnvironment = touchProcessBuilder.environment();                 
   		touchEnvironment.put("Path", openBISDirectory.getAbsolutePath());//+";" + env.get("PATH"));                 
   		touchEnvironment.put("HOME", openBISDirectory.getAbsolutePath());
   		touchProcessBuilder.directory(openBISDirectory.getAbsoluteFile());                
		
   		// Start transfer of files
		try
		{
			transferProcess = rsyncProcessBuilder.start();        
		}
		catch(IOException e)
		{
			running = false;
			throw new OpenBISException("Error occured while starting rsync.", e);
		}
		
		// Start listening to the output in another thread
		(new Thread(this)).start();
	}
	
	private static String toUnixPath(String windowsPath)
	{
		String path = windowsPath.replace('\\', '/');
		//path = path.replaceAll(" ", "\\ ");
		path = "/cygdrive/" + path.charAt(0) + path.substring(2);
		return "\""+path+"/\"";

	}
	
	@Override
	public void addTransferListener(OpenBISListener listener) throws RemoteException
	{
		synchronized(transferListeners)
		{
			transferListeners.addElement(listener);
		}
	}

	@Override
	public void removeTransferListener(OpenBISListener listener) throws RemoteException
	{
		synchronized(transferListeners)
		{
			transferListeners.removeElement(listener);
		}
	}

	private void notifyTransferStarted()
	{
		synchronized(transferListeners)
		{
			for(int i = 0; i < transferListeners.size(); i++)
			{
				try
				{
					transferListeners.elementAt(i).transferStarted();
				}
				catch(@SuppressWarnings("unused") RemoteException e)
				{
					// Remove listener
					transferListeners.removeElementAt(i);
					i--;
				}
			}
		}
	}
	
	private void notifyTransferFinished()
	{
		synchronized(transferListeners)
		{
			for(int i = 0; i < transferListeners.size(); i++)
			{
				try
				{
					transferListeners.elementAt(i).transferFinished();
				}
				catch(@SuppressWarnings("unused") RemoteException e)
				{
					// Remove listener
					transferListeners.removeElementAt(i);
					i--;
				}
			}
		}
	}
	
	private void notifyTransferFailed()
	{
		synchronized(transferListeners)
		{
			for(int i = 0; i < transferListeners.size(); i++)
			{
				try
				{
					transferListeners.elementAt(i).transferFailed(new Exception("Check messages for more detail."));
				}
				catch(@SuppressWarnings("unused") RemoteException e)
				{
					// Remove listener
					transferListeners.removeElementAt(i);
					i--;
				}
			}
		}
	}
	
	private void notifyTransferProgress(float progress, String message)
	{
		synchronized(transferListeners)
		{
			for(int i = 0; i < transferListeners.size(); i++)
			{
				try
				{
					transferListeners.elementAt(i).transferProgress(progress, message);
				}
				catch(@SuppressWarnings("unused") RemoteException e)
				{
					// Remove listener
					transferListeners.removeElementAt(i);
					i--;
				}
			}
		}
	}
	
	/**
	 * Does updata the state of this addon based on the progress of rsync.
	 */
	@Override
	public synchronized void run()
	{
		// Notify listeners that transfer started.
		notifyTransferStarted();
		final BufferedReader errorReader = new BufferedReader(new InputStreamReader(transferProcess.getErrorStream()));
		final BufferedReader outputReader = new BufferedReader(new InputStreamReader(transferProcess.getInputStream()));
		int exitCode = 0;
		boolean shouldContinueListening = true;
		while(shouldContinueListening)
		{
			// Check if process is finished.
			// We cannot check this directly. Therefore, we ask for the process's exit code. If the process did not exit yet, an
			// exception is thrown.
			try
			{
				// If this works, we are finished.
				exitCode = transferProcess.exitValue();
				shouldContinueListening = false;
			}
			catch(@SuppressWarnings("unused") IllegalThreadStateException e)
			{
				// Exception means that we did not finish yet.
			}
			
			// Sleep for 100ms before continuing looping.
			try
			{
				Thread.sleep(100);
			}
			catch(@SuppressWarnings("unused") InterruptedException e)
			{
				// Somebody wants us to stop. Better do so.
				notifyTransferProgress(-1, "Thread observing transfer process got interrupted. Process may continue to run in the background.\n");
				notifyTransferFailed();
				running = false;
				return;
			}
			
			// First, readout new console output.
			String stateMessage = "";
			try
			{
				while (errorReader.ready())
				{
				    final String line = errorReader.readLine();
				    if(line == null)
				    {
				        break;
				    }
					stateMessage += line + "\n";
				}
			}
			catch(@SuppressWarnings("unused") IOException e1)
			{
				// Do nothing, its just output for information purposes.
			}
			try
			{
				while (outputReader.ready())
				{
				    final String line = outputReader.readLine();
				    if(line == null)
				    {
				        break;
				    }
					stateMessage += line + "\n";
				}
			}
			catch(@SuppressWarnings("unused") IOException e1)
			{
				// Do nothing, its just output for information purposes.
			}
			if(stateMessage.length() > 0)
			{
				notifyTransferProgress(-1, stateMessage);
			}
		}
		if(exitCode != 0)
		{
			notifyTransferProgress(1, "Data transferred failed with error code " + Integer.toString(exitCode) + " (see rsync manual for more information).\n");
			notifyTransferFailed();
			running = false;
			return;
		}
		
		// Create marker file
		notifyTransferProgress(0.95F, "Data transferred successfully.\nSending marker file to invoke processing.\n");
		try
		{
			transferProcess = touchProcessBuilder.start();        
		}
		catch(@SuppressWarnings("unused") IOException e)
		{
			notifyTransferProgress(1, "Could not start SSH call to touch the marker file.\n");
			notifyTransferFailed();
			running = false;
			return;
		}
		final BufferedReader touchErrorReader = new BufferedReader(new InputStreamReader(transferProcess.getErrorStream()));
		final BufferedReader touchOutputReader = new BufferedReader(new InputStreamReader(transferProcess.getInputStream()));
		
		try
		{
			transferProcess.waitFor();
		}
		catch(@SuppressWarnings("unused") InterruptedException e)
		{
			notifyTransferProgress(1, "Thread observing touch process got interrupted. Process may continue to run in the background.\n");
			notifyTransferFailed();
			running = false;
			return;
		}
		String stateMessage = "";
		try
		{
			while (touchErrorReader.ready())
			{
			    final String line = touchErrorReader.readLine();
			    if(line == null)
			    {
			        break;
			    }
				stateMessage += line + "\n";
			}
		}
		catch(@SuppressWarnings("unused") IOException e1)
		{
			// Do nothing, its just output for information purposes.
		}
		try
		{
			while (touchOutputReader.ready())
			{
			    final String line = touchOutputReader.readLine();
			    if(line == null)
			    {
			        break;
			    }
				stateMessage += line + "\n";
			}
		}
		catch(@SuppressWarnings("unused") IOException e1)
		{
			// Do nothing, its just output for information purposes.
		}
		if(stateMessage.length() > 0)
		{
			notifyTransferProgress(-1, stateMessage);
		}
		if(transferProcess.exitValue() == 0)
		{
			notifyTransferProgress(1, "Marker file sucessfully created.\n");
			notifyTransferFinished();
		}
		else
		{
			notifyTransferProgress(1, "Touch call to create marker file failed with error code " + Integer.toString(transferProcess.exitValue()) + " (see ssh manual for more information).\n");
			notifyTransferFailed();
		}
		transferProcess = null;
		running = false;
	}

	@Override
	public void cancelTransfer() throws RemoteException
	{
		if(!isTransferring())
			return;
		Process process = transferProcess;
		if(process == null)
			return;
		class TransferStopper implements Runnable
		{
			private final Process process;
			TransferStopper(Process process)
			{
				this.process = process;
			}
			@Override
			public void run()
			{
				process.destroy();
			}
		}
		(new Thread(new TransferStopper(process))).start();
	}

	@Override
	public boolean isTransferring() throws RemoteException
	{
		return running;
	}

}
