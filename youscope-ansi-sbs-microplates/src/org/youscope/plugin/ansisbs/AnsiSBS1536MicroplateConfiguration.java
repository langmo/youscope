package org.youscope.plugin.ansisbs;

import org.youscope.addon.microplate.MicroplateConfiguration;
import org.youscope.common.configuration.YSConfigAlias;
import org.youscope.common.configuration.YSConfigIcon;

/**
 * 1536 well microplate (ANSI/SBS 1-2004 through ANSI/SBS 4-2004).
 * @author Moritz Lang
 *
 */
@YSConfigAlias("1536 well microplate (ANSI/SBS 1-2004 through ANSI/SBS 4-2004)")
@YSConfigIcon("icons/map.png")
public class AnsiSBS1536MicroplateConfiguration extends MicroplateConfiguration
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 3032928283305729311L;
	/**
	 * Type identifier.
	 */
	public static final String TYPE_IDENTIFIER = "YouScope.microplate.AnsiSBS1536Microplate";

	@Override
	public String getTypeIdentifier() {
		return TYPE_IDENTIFIER;
	}

	

}
