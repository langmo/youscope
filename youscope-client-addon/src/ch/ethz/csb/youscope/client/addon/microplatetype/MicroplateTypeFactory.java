/**
 * 
 */
package ch.ethz.csb.youscope.client.addon.microplatetype;

import ch.ethz.csb.youscope.shared.MicroplateType;

/**
 * @author langmo
 */
public interface MicroplateTypeFactory
{

    /**
     * Returns a new MicroplateType for the given ID, or null if addon does not support tools with the given ID.
     * @param ID The ID for which a tool should be created.
     * @param client Interface describing microplate properties with for the given id.
     * @param server Interface to the server.
     * 
     * @return New Addon.
     */
    MicroplateType createMicroplateType(String ID);    
    
    /**
	 * Returns a list of all microplate types supported by this addon
	 * 
	 * @return List of supported tool types.
	 */
	String[] getSupportedMicroplateIDs();

	/**
	 * Returns true if this addon supports microplates with the given ID, false otherwise.
	 * @param ID The ID of the microplate type for which it should be queried if this addon supports it.
	 * @return True if this addon supports tools with the given ID, false otherwise.
	 */
	boolean supportsMicroplateID(String ID);
}
