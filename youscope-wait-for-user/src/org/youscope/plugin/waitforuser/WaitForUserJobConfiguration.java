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
package org.youscope.plugin.waitforuser;

import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.JobConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * This job/task makes images in a certain channel in regular intervals.
 * 
 * @author Moritz Lang
 */
@XStreamAlias("wait-for-user")
public class WaitForUserJobConfiguration implements JobConfiguration
{
	@XStreamAlias("message")
	@XStreamAsAttribute
	private String	message				= "No message";
	
	/**
	 * Serial version UID.
	 */
	private static final long	serialVersionUID	= 7144732041177941141L;

	@Override
	public String getDescription()
	{
		String description = "<p>display(\"" + message + "\")</p>" +
			"<p>wait(user)</p>";
		return description;
	}

	

	/**
	 * Returns the message which is displayed to the user.
	 * @return Message to be displayed
	 */
	public String getMessage()
	{
		return message;
	}

	/**
	 * Sets the message which is displayed to the user.
	 * @param message Message to be displayed.
	 */
	public void setMessage(String message)
	{
		if(message != null)
			this.message = message;
	}



	/**
	 * The identifier for this job type.
	 */
	public static final String	TYPE_IDENTIFIER	= "YouScope.WaitForUserJob";

	@Override
	public String getTypeIdentifier()
	{
		return TYPE_IDENTIFIER;
	}



	@Override
	public void checkConfiguration() throws ConfigurationException {
		// do nothing, always correct.
		
	}
}
