package org.youscope.plugin.slimjob;

import org.youscope.clientinterfaces.YouScopeClient;

/**
 * Class to conveniently get the value of the properties belonging to SLIM.
 * @author mlang
 *
 */
public class SlimProperties 
{
	private final YouScopeClient client;
	private final static String ATTENUATION_FACTOR_PROPERTY = "YouScope.SLIM.attenuationFactor";
	/**
	 * Constructor.
	 * @param client YouScope client.
	 */
	public SlimProperties(YouScopeClient client)
	{
		this.client = client;
	}
	/**
	 * Returns the last identified attenuation factor, or 1 if yet not identified.
	 * @return Last identified attenuation factor.
	 */
	public double getAttenuationFactor()
	{
		return client.getPropertyProvider().getProperty(ATTENUATION_FACTOR_PROPERTY, 1.0);
	}
	/**
	 * Sets the attenuation factor.
	 * @param attenuationFactor Attenuation factor.
	 */
	public void setAttenuationFactor(double attenuationFactor)
	{
		client.getPropertyProvider().setProperty(ATTENUATION_FACTOR_PROPERTY, attenuationFactor);
	}
}
