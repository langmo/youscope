/**
 * 
 */
package org.youscope.plugin.custommicroplates;

import org.youscope.addon.microplate.MicroplateAddonFactory;
import org.youscope.common.Microplate;

/**
 * @author langmo
 *
 */
public class CustomMicroplateTypeFactory implements MicroplateAddonFactory
{
	Microplate[] microplateTypes;
	/**
	 * Constructor.
	 */
	public CustomMicroplateTypeFactory()
	{
		microplateTypes = CustomMicroplatesManager.getMicroplateTypes();
	}
	@Override
	public Microplate createMicroplateType(String ID)
	{ 
		for(Microplate microplateType : microplateTypes)
		{
			if(ID.compareToIgnoreCase(microplateType.getMicroplateID()) == 0)
				return microplateType;
		}
		return null;
	}

	@Override
	public String[] getSupportedTypeIdentifiers()
	{
		String[] ids = new String[microplateTypes.length];
		for(int i=0; i<microplateTypes.length; i++)
		{
			ids[i] = microplateTypes[i].getMicroplateID();
		}
		return ids;
	}

	@Override
	public boolean isSupportingTypeIdentifier(String ID)
	{
		for(String addonID : getSupportedTypeIdentifiers())
		{
			if(addonID.compareToIgnoreCase(ID) == 0)
				return true;
		}
		return false;
	}
}
