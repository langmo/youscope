/**
 * 
 */
package ch.ethz.csb.youscope.addon.matlabfocusscores;
import ch.ethz.csb.youscope.addon.adapters.AddonFactoryAdapter;

/**
 * @author Moritz Lang
 */
public class MatlabScoresAddonFactory extends AddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public MatlabScoresAddonFactory()
	{
		super(MatlabScoresConfiguration.CONFIGURATION_ID, MatlabScoresConfiguration.class, MatlabScoresAddon.class);
	}
}
