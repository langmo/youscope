/**
 * 
 */
package org.youscope.plugin.measurementappender;

import java.rmi.RemoteException;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonFactoryAdapter;
import org.youscope.addon.component.ComponentCreationException;
import org.youscope.addon.component.CustomAddonCreator;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.saving.SaveSettings;
import org.youscope.serverinterfaces.ConstructionContext;

/**
 * Factory for standard save settings.
 * @author Moritz Lang
 */
public class AppenderSaveSettingsFactory extends ComponentAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public AppenderSaveSettingsFactory()
	{
		super(AppenderSaveSettingsUI.class, new CustomAddonCreator<AppenderSaveSettingsConfiguration, AppenderSaveSettings>()
				{

					@Override
					public AppenderSaveSettings createCustom(PositionInformation positionInformation,
							AppenderSaveSettingsConfiguration configuration, ConstructionContext constructionContext)
									throws ConfigurationException, AddonException {
						try {
							SaveSettings encapsulatedSettings = constructionContext.getComponentProvider().createComponent(positionInformation, configuration.getEncapsulatedSaveSettings(), SaveSettings.class);
							return new AppenderSaveSettings(positionInformation, configuration, encapsulatedSettings);
						} catch (RemoteException | ComponentCreationException e) {
							throw new AddonException("Could not create enclosed save settings.", e);
						}
					}

					@Override
					public Class<AppenderSaveSettings> getComponentInterface() {
						return AppenderSaveSettings.class;
					}
			
				}, AppenderSaveSettingsUI.getMetadata());		
	}
}
