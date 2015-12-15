package org.youscope.plugin.systemskin;

import org.youscope.addon.skin.SkinFactoryAdapter;

/**
 * Look and feel adjusting to the operating system default look and feel.
 * @author Moritz Lang
 *
 */
public class SystemSkinFactory extends SkinFactoryAdapter 
{
	/**
	 * Constructor.
	 */
	public SystemSkinFactory()
	{
		super(SystemSkin.class, SystemSkin.createMetadata());
	}
}
