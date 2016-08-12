package org.youscope.plugin.ansisbs;

import org.youscope.addon.microplate.MicroplateConfiguration;
import org.youscope.common.configuration.YSConfigAlias;
import org.youscope.common.configuration.YSConfigIcon;

/**
 * 96 well microplate (ANSI/SBS 1-2004 through ANSI/SBS 4-2004).
 * @author Moritz Lang
 *
 */
@YSConfigAlias("96 well microplate (ANSI/SBS 1-2004 through ANSI/SBS 4-2004)")
@YSConfigIcon("icons/map.png")
public class AnsiSBS96MicroplateConfiguration extends MicroplateConfiguration
{
	/** 
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 3032928883305729335L;
	/**
	 * Type identifier.
	 */
	public static final String TYPE_IDENTIFIER = "YouScope.microplate.AnsiSBS96Microplate";

	@Override
	public String getTypeIdentifier() {
		return TYPE_IDENTIFIER;
	}

}
