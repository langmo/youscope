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
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

import org.youscope.addon.microscopeaccess.MicroscopeInternal;
import org.youscope.common.microscope.Channel;
import org.youscope.common.microscope.ChannelManager;
import org.youscope.common.microscope.DeviceSetting;
import org.youscope.common.microscope.MicroscopeConfigurationListener;
import org.youscope.common.microscope.MicroscopeLockedException;
import org.youscope.common.microscope.SettingException;

/**
 * The channel manager keeps track of the channels in which the microscope can image in.
 * This is an internal class. Use getRMIInterface() to get the respective class for external usage.
 * @author Moritz Lang
 * 
 */
class ChannelManagerImpl implements MicroscopeConfigurationListener
{
	private MicroscopeInternal		microscope;
	private ArrayList<ChannelImpl>	channels	= new ArrayList<ChannelImpl>();

	ChannelManagerImpl(MicroscopeInternal microscope)
	{
		this.microscope = microscope;
		microscope.addConfigurationListener(this);
	}

	@Override
	public void deviceRemoved(String deviceID)
	{
		for(ChannelImpl channel : channels)
		{
			channel.deviceRemoved(deviceID);
		}
	}

	public String[] getChannelGroupIDs()
	{
		Vector<String> channelGroups = new Vector<String>();
		for(ChannelImpl channel : channels)
		{
			if(!channelGroups.contains(channel.getChannelGroupID()))
				channelGroups.addElement(channel.getChannelGroupID());
		}
		String[] resVal = channelGroups.toArray(new String[0]);
		return resVal;
	}

	public ChannelImpl[] getChannels(String channelGroupID) throws SettingException
	{
		Vector<ChannelImpl> foundChannels = new Vector<ChannelImpl>();
		for(ChannelImpl channel : channels)
		{
			if(channel.getChannelGroupID().equals(channelGroupID))
				foundChannels.addElement(channel);
		}
		ChannelImpl[] resVal = foundChannels.toArray(new ChannelImpl[0]);
		return resVal;
	}

	public ChannelImpl[] getChannels()
	{
		ChannelImpl[] resVal = channels.toArray(new ChannelImpl[0]);
		return resVal;
	}

	public void removeChannel(String channelGroupID, String channelID, int accessID) throws MicroscopeLockedException, SettingException
	{
		microscope.lockExclusiveWrite(accessID);
		try
		{
			ChannelImpl channel = getChannel(channelGroupID, channelID);
			channels.remove(channel);
		}
		finally
		{
			microscope.unlockExclusiveWrite(accessID);
		}

		ServerSystem.out.println("Channel " + channelGroupID + "." + channelID + " removed.");
	}

	public ChannelImpl addChannel(String channelGroupID, String channelID, int accessID) throws MicroscopeLockedException, SettingException
	{
		ChannelImpl channel;

		microscope.lockExclusiveWrite(accessID);
		try
		{
			// First, simply try to return the channel if it is already defined.
			try
			{
				return getChannel(channelGroupID, channelID);
			}
			catch(@SuppressWarnings("unused") SettingException e)
			{
				// Do nothing, channel is simply yet not defined.
			}
			channel = new ChannelImpl(channelGroupID, channelID, microscope);
			channels.add(channel);

			// sort array, such that channels are given back in alphabetic order.
			Collections.sort(channels);
		}
		finally
		{
			microscope.unlockExclusiveWrite(accessID);
		}

		ServerSystem.out.println("New channel " + channelGroupID + "." + channelID + " created.");
		return channel;
	}

	public ChannelImpl getChannel(String channelGroupID, String channelID) throws SettingException
	{
		if(channelGroupID == null || channelID == null)
			return null;
		for(ChannelImpl channel : channels)
		{
			if(channel.getChannelGroupID().equals(channelGroupID) && channel.getChannelID().equals(channelID))
				return channel;
		}
		throw new SettingException("Channel " + channelGroupID + "." + channelID + " is not defined.");
	}

	@Override
	public void microscopeUninitialized()
	{
		// First update channels. Although they are forgotten in the next step, there might be still external references on them.
		for(ChannelImpl channel : channels)
		{
			channel.microscopeUninitialized();
		}

		// Now reset channel list.
		channels.clear();
	}

	@Override
	public void labelChanged(DeviceSetting oldLabel, DeviceSetting newLabel)
	{
		for(ChannelImpl channel : channels)
		{
			channel.labelChanged(oldLabel, newLabel);
		}
	}

	private class ChannelManagerRMI extends UnicastRemoteObject implements ChannelManager
	{

		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -3244187138882925670L;
		private int					accessID;

		ChannelManagerRMI(int accessID) throws RemoteException
		{
			super();
			this.accessID = accessID;
		}

		@Override
		public String[] getChannelGroupIDs() throws RemoteException
		{
			return ChannelManagerImpl.this.getChannelGroupIDs();
		}

		@Override
		public Channel[] getChannels(String channelGroupID) throws SettingException, RemoteException
		{
			ArrayList<Channel> foundChannels = new ArrayList<Channel>();
			for(ChannelImpl channel : channels)
			{
				if(channel.getChannelGroupID().equals(channelGroupID))
					foundChannels.add(channel.getRMIInterface(accessID));
			}
			Channel[] resVal = foundChannels.toArray(new Channel[0]);
			return resVal;
		}

		@Override
		public Channel[] getChannels() throws RemoteException
		{
			Channel[] resVal = new Channel[channels.size()];
			for(int i = 0; i < channels.size(); i++)
			{
				resVal[i] = channels.get(i).getRMIInterface(accessID);
			}
			return resVal;
		}

		@Override
		public Channel getChannel(String channelGroupID, String channelID) throws SettingException, RemoteException
		{
			return ChannelManagerImpl.this.getChannel(channelGroupID, channelID).getRMIInterface(accessID);
		}

		@Override
		public Channel addChannel(String channelGroupID, String channelID) throws MicroscopeLockedException, SettingException, RemoteException
		{
			return ChannelManagerImpl.this.addChannel(channelGroupID, channelID, accessID).getRMIInterface(accessID);
		}

		@Override
		public void removeChannel(String channelGroupID, String channelID) throws MicroscopeLockedException, SettingException, RemoteException
		{
			ChannelManagerImpl.this.removeChannel(channelGroupID, channelID, accessID);
		}
	}

	ChannelManager getRMIInterface(int accessID) throws RemoteException
	{
		return new ChannelManagerRMI(accessID);
	}
}
