/**
 * 
 */
package org.youscope.addon.microplate;

import org.youscope.addon.AddonException;
import org.youscope.common.Microplate;

/**
 * Factory to create layouts of microplates, i.e. how many wells they have, how they are layouted, and how big they are.
 * @author Moritz Lang
 */
public interface MicroplateAddonFactory
{

    /**
     * Returns a new MicroplateType for the given type identifier, or throws an {@link AddonException} if this factory does not support microplates with the given type identifier.
     * @param typeIdentifier The type identifier of a microplate for which the definition should be returned.
     * 
     * @return Information about the microplate type.
     * @throws AddonException Thrown if factory does not support microplate types with the given type identifier.
     */
    Microplate createMicroplateType(String typeIdentifier) throws AddonException;    
    
    /**
	 * Returns the type identifiers of all microplate types supported by this addon
	 * 
	 * @return Supported microplates' type identifiers.
	 */
	String[] getSupportedTypeIdentifiers();

	/**
	 * Returns true if this addon supports microplates with the given type identifier, and false otherwise.
	 * @param typeIdentifier The type identifier of the microplate type for which it should be queried if this addon supports it.
	 * @return True if this addon supports microplates with the given type identifier, and false otherwise.
	 */
	boolean isSupportingTypeIdentifier(String typeIdentifier);
}
