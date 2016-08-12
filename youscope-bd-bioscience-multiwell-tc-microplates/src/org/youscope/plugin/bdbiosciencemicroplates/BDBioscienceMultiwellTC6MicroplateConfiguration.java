package org.youscope.plugin.bdbiosciencemicroplates;

import org.youscope.addon.microplate.MicroplateConfiguration;
import org.youscope.common.configuration.YSConfigAlias;
import org.youscope.common.configuration.YSConfigIcon;

/**
 * 6 well microplate (BD Bioscience™ - Multiwell™ TC Plate).
 * @author Moritz Lang
 *
 */
@YSConfigAlias("6 well microplate (BD Bioscience™ - Multiwell™ TC Plate)")
@YSConfigIcon("icons/map.png")
public class BDBioscienceMultiwellTC6MicroplateConfiguration extends MicroplateConfiguration
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -2185219210941746488L;
	
	/**
	 * Type identifier.
	 */
	public static final String TYPE_IDENTIFIER = "YouScope.microplate.BDBioscienceMultiwellTC6Microplate";

	@Override
	public String getTypeIdentifier() {
		return TYPE_IDENTIFIER;
	}

}
