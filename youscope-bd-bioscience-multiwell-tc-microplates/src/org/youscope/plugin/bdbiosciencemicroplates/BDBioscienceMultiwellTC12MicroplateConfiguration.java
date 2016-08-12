package org.youscope.plugin.bdbiosciencemicroplates;

import org.youscope.addon.microplate.MicroplateConfiguration;
import org.youscope.common.configuration.YSConfigAlias;
import org.youscope.common.configuration.YSConfigIcon;

/**
 * 12 well microplate (BD Bioscience™ - Multiwell™ TC Plate).
 * @author Moritz Lang
 *
 */
@YSConfigAlias("12 well microplate (BD Bioscience™ - Multiwell™ TC Plate)")
@YSConfigIcon("icons/map.png")
public class BDBioscienceMultiwellTC12MicroplateConfiguration extends MicroplateConfiguration
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -8043869569063229857L;
	
	/**
	 * Type identifier.
	 */
	public static final String TYPE_IDENTIFIER = "YouScope.microplate.DBioscienceMultiwellTC12Microplate";

	@Override
	public String getTypeIdentifier() {
		return TYPE_IDENTIFIER;
	}

	

}
