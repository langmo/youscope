/**
 * 
 */
package ch.ethz.csb.youscope.addon.custommicroplates;

import ch.ethz.csb.youscope.client.addon.microplatetype.MicroplateTypeFactory;
import ch.ethz.csb.youscope.shared.MicroplateType;

/**
 * @author langmo
 *
 */
public class CustomMicroplateTypeFactory implements MicroplateTypeFactory
{
	MicroplateType[] microplateTypes;
	/**
	 * Constructor.
	 */
	public CustomMicroplateTypeFactory()
	{
		microplateTypes = CustomMicroplatesManager.getMicroplateTypes();
	}
	@Override
	public MicroplateType createMicroplateType(String ID)
	{ 
		for(MicroplateType microplateType : microplateTypes)
		{
			if(ID.compareToIgnoreCase(microplateType.getMicroplateID()) == 0)
				return microplateType;
		}
		return null;
	}

	@Override
	public String[] getSupportedMicroplateIDs()
	{
		String[] ids = new String[microplateTypes.length];
		for(int i=0; i<microplateTypes.length; i++)
		{
			ids[i] = microplateTypes[i].getMicroplateID();
		}
		return ids;
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
