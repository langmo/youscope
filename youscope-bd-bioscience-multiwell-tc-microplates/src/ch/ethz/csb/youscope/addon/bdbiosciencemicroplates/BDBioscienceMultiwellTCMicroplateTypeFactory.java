/**
 * 
 */
package ch.ethz.csb.youscope.addon.bdbiosciencemicroplates;

import ch.ethz.csb.youscope.client.addon.microplatetype.MicroplateTypeFactory;
import ch.ethz.csb.youscope.shared.MicroplateType;

/**
 * @author langmo
 *
 */
public class BDBioscienceMultiwellTCMicroplateTypeFactory implements MicroplateTypeFactory
{

	@Override
	public MicroplateType createMicroplateType(String ID)
	{
		if(ID.compareToIgnoreCase(BDBioscienceMultiwellTC6MicroplateType.TYPE_ID) == 0)
			return new BDBioscienceMultiwellTC6MicroplateType();
		if(ID.compareToIgnoreCase(BDBioscienceMultiwellTC12MicroplateType.TYPE_ID) == 0)
			return new BDBioscienceMultiwellTC12MicroplateType();
		if(ID.compareToIgnoreCase(BDBioscienceMultiwellTC24MicroplateType.TYPE_ID) == 0)
			return new BDBioscienceMultiwellTC24MicroplateType();
		return null;
	}

	@Override
	public String[] getSupportedMicroplateIDs()
	{
		return new String[]{BDBioscienceMultiwellTC6MicroplateType.TYPE_ID, BDBioscienceMultiwellTC12MicroplateType.TYPE_ID, BDBioscienceMultiwellTC24MicroplateType.TYPE_ID};
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
