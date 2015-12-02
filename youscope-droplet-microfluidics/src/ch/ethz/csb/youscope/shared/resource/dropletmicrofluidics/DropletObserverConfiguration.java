package ch.ethz.csb.youscope.shared.resource.dropletmicrofluidics;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import ch.ethz.csb.youscope.shared.configuration.ResourceConfiguration;

/**
 * Configuration of an observer for droplet based microfluidics.
 * @author Moritz Lang
 *
 */
@XStreamAlias("droplet-observer-configuration")
public abstract class DropletObserverConfiguration  extends ResourceConfiguration
{

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 6268977101312936654L;

	

}
