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
package org.youscope.common.configuration;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * Configuration of a channel, identified by its channel name and configuration group name.
 * @author Moritz Lang
 *
 */
@XStreamAlias("channel-configuration")
public final class ChannelConfiguration implements Configuration, Comparable<ChannelConfiguration>
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -743245538792705615L;
	

	@XStreamAlias("channel")
	@XStreamAsAttribute
	private String channel;
	@XStreamAlias("channel-group")
	@XStreamAsAttribute
	private String channelGroup;
	
	/**
	 * Constructor.
	 * The channel and channel group names are initialized to null.
	 */
	public ChannelConfiguration()
	{
		this.channel = null;
		this.channelGroup = null;
	}
	
	/**
	 * Constructor.
	 * @param channelGroup The channel group name.
	 * @param channel The channel name.
	 */
	public ChannelConfiguration(String channelGroup, String channel) 
	{
		this.channel = channel;
		this.channelGroup = channelGroup;
	}
	
	/**
	 * Copy constructor.
	 * If configuration is null, sets channel and channel group to null.
	 * @param configuration The configuration to copy.
	 */
	public ChannelConfiguration(ChannelConfiguration configuration)
	{
		this.channel = configuration == null ? null : configuration.getChannel();
		this.channelGroup = configuration == null ? null : configuration.getChannelGroup();
	}

	@Override
	public String getTypeIdentifier() 
	{
		return "YouScope.Channel";
	}

	/**
	 * The channel name.
	 * @return Channel name.
	 */
	public String getChannel() 
	{
		return channel;
	}

	/**
	 * The channel group name.
	 * @return Channel group name.
	 */
	public String getChannelGroup() 
	{
		return channelGroup;
	}

	/**
	 * Sets the name of the channel. Should be always called together with setChannelGroup().
	 * @param channel Name of channel.
	 */
	public void setChannel(String channel) 
	{
		this.channel = channel;
	}
	
	/**
	 * Copies the content of the channel configuration into this configuration.
	 * If configuration is null, sets the channel and channel group to null.
	 * @param configuration The channel configuration to copy.
	 */
	public void copyConfiguration(ChannelConfiguration configuration)
	{
		if(configuration == null)
		{
			this.channel = null;
			this.channelGroup = null;
		}
	}
	
	/**
	 * Sets the channel and channel group names.
	 * @param channelGroup Name of channel group.
	 * @param channel Name of channel.
	 */
	public void setChannel(String channelGroup, String channel)
	{
		this.channel = channel;
		this.channelGroup = channelGroup;
	}

	/**
	 * Sets the name of the channel group. Should always be called with setChannel().
	 * @param channelGroup Channel group name.
	 */
	public void setChannelGroup(String channelGroup) {
		this.channelGroup = channelGroup;
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((channel == null) ? 0 : channel.hashCode());
		result = prime * result + ((channelGroup == null) ? 0 : channelGroup.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChannelConfiguration other = (ChannelConfiguration) obj;
		if (channel == null) {
			if (other.channel != null)
				return false;
		} else if (!channel.equals(other.channel))
			return false;
		if (channelGroup == null) {
			if (other.channelGroup != null)
				return false;
		} else if (!channelGroup.equals(other.channelGroup))
			return false;
		return true;
	}
	
	@Override
	public String toString() 
	{
		if(channel == null || channelGroup == null)
			return "uninitialized channel";
		return channelGroup+"."+channel;
	}

	@Override
	public int compareTo(ChannelConfiguration otherChannel) 
	{
		if(channelGroup != null)
		{
			if(otherChannel.channelGroup == null)
				return -1;
			int stringCompare = channel.compareTo(otherChannel.channelGroup);
			if(stringCompare != 0)
				return stringCompare;
		}
		else if(otherChannel.channelGroup != null)
				return 1;
		if(channel != null)
		{
			if(otherChannel.channel == null)
				return -1;
			return channel.compareTo(otherChannel.channelGroup);
		}
		else if(otherChannel.channel != null)
			return 1;
		return 0;

	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		if((channel == null && channelGroup != null) || (channelGroup == null && channel != null))
			throw new ConfigurationException("Either both channel and channel group have to be null to use default channel, or none has to be null");
		
	}
}
