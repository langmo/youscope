/**
 * 
 */
package org.youscope.plugin.quickdetect;

import org.youscope.addon.component.ComponentAddonFactoryAdapter;

/**
 * @author Moritz Lang
 */
public class QuickDetectAddonFactory extends ComponentAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public QuickDetectAddonFactory()
	{
		super(QuickDetectConfiguration.TYPE_IDENTIFIER, QuickDetectConfiguration.class, QuickDetectAddon.class);
	}
}
