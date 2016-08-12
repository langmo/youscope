package org.youscope.common.resource;

import org.youscope.common.configuration.Configuration;
import org.youscope.common.configuration.ConfigurationException;

/**
 * Base class of all configurations of measurement resources.
 * @author Moritz Lang
 *
 */
public abstract class ResourceConfiguration implements Configuration 
{

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -7383728961240600655L;

	@Override
	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		// do nothing.
		
	}
}