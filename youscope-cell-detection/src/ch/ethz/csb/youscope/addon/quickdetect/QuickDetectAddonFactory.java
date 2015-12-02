/**
 * 
 */
package ch.ethz.csb.youscope.addon.quickdetect;

import ch.ethz.csb.youscope.addon.adapters.AddonFactoryAdapter;

/**
 * @author Moritz Lang
 */
public class QuickDetectAddonFactory extends AddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public QuickDetectAddonFactory()
	{
		super(QuickDetectConfiguration.TYPE_IDENTIFIER, QuickDetectConfiguration.class, QuickDetectAddon.class);
	}
}
