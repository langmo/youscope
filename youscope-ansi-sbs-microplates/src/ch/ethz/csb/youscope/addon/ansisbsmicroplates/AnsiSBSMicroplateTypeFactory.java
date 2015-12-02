/**
 * 
 */
package ch.ethz.csb.youscope.addon.ansisbsmicroplates;

import ch.ethz.csb.youscope.client.addon.microplatetype.MicroplateTypeFactory;
import ch.ethz.csb.youscope.shared.MicroplateType;

/**
 * @author langmo
 *
 */
public class AnsiSBSMicroplateTypeFactory implements MicroplateTypeFactory
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
