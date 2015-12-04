/**
 * 
 */
package org.youscope.plugin.custommicroplates;

import org.youscope.addon.microplate.MicroplateAddonFactory;
import org.youscope.common.MicroplateType;

/**
 * @author langmo
 *
 */
public class CustomMicroplateTypeFactory implements MicroplateAddonFactory
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
