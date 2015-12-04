/**
 * 
 */
package org.youscope.plugin.matlabfocusscores;
import org.youscope.addon.component.ComponentAddonFactoryAdapter;

/**
 * @author Moritz Lang
 */
public class MatlabScoresAddonFactory extends ComponentAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public MatlabScoresAddonFactory()
	{
		super(MatlabScoresConfiguration.CONFIGURATION_ID, MatlabScoresConfiguration.class, MatlabScoresAddon.class);
	}
}
