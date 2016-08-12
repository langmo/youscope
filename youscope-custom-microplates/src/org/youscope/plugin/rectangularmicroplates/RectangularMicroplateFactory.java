/**
 * 
 */
package org.youscope.plugin.rectangularmicroplates;

import org.youscope.addon.component.ComponentAddonFactoryAdapter;
/**
 * Factory for Rectangular microplates.
 * @author Moritz Lang
 */
public class RectangularMicroplateFactory extends ComponentAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public RectangularMicroplateFactory()
	{
		super(RectangularMicroplateConfiguration.TYPE_IDENTIFIER, RectangularMicroplateConfiguration.class, RectangularMicroplateResource.class);
	}
}
