package ch.ethz.csb.youscope.shared.resource.dropletmicrofluidics;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import ch.ethz.csb.youscope.shared.configuration.ResourceConfiguration;

/**
 * Configuration of a controller for droplet based microfluidics.
 * @author Moritz Lang
 *
 */
@XStreamAlias("droplet-controller-configuration")
public abstract class DropletControllerConfiguration  extends ResourceConfiguration
{

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 6268977109312936654L;

	

}
