package org.youscope.plugin.darkskin;

import org.youscope.addon.skin.SkinFactoryAdapter;

/**
 * Dark Look and feel.
 * @author Moritz Lang
 *
 */
public class DarkSkinFactory extends SkinFactoryAdapter 
{
	/**
	 * Constructor.
	 */
	public DarkSkinFactory()
	{
		super(DarkSkin.class, DarkSkin.createMetadata());
	}
}
