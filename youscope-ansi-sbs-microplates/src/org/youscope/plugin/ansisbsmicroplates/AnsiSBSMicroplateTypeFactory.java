/**
 * 
 */
package org.youscope.plugin.ansisbsmicroplates;

import org.youscope.addon.microplate.MicroplateAddonFactory;
import org.youscope.common.MicroplateType;

/**
 * @author langmo
 *
 */
public class AnsiSBSMicroplateTypeFactory implements MicroplateAddonFactory
{

	@Override
	public MicroplateType createMicroplateType(String ID)
	{ 
		if(ID.compareToIgnoreCase(AnsiSBS96MicroplateType.TYPE_ID) == 0)
			return new AnsiSBS96MicroplateType();
		if(ID.compareToIgnoreCase(AnsiSBS384MicroplateType.TYPE_ID) == 0)
			return new AnsiSBS384MicroplateType();
		if(ID.compareToIgnoreCase(AnsiSBS1536MicroplateType.TYPE_ID) == 0)
			return new AnsiSBS1536MicroplateType();
		return null;
	}

	@Override
	public String[] getSupportedMicroplateIDs()
	{
		return new String[]{AnsiSBS96MicroplateType.TYPE_ID, AnsiSBS384MicroplateType.TYPE_ID, AnsiSBS1536MicroplateType.TYPE_ID};
	}

	@Override
	public boolean supportsMicroplateID(String ID)
	{
		for(String addonID : getSupportedMicroplateIDs())
		{
			if(addonID.compareToIgnoreCase(ID) == 0)
				return true;
		}
		return false;
	}
}
