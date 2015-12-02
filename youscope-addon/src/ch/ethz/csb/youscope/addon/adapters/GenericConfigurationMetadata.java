package ch.ethz.csb.youscope.addon.adapters;

import java.net.URL;

import javax.swing.ImageIcon;

import ch.ethz.csb.youscope.shared.configuration.Configuration;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationMetadata;
import ch.ethz.csb.youscope.shared.configuration.annotations.YSConfigAlias;
import ch.ethz.csb.youscope.shared.configuration.annotations.YSConfigClassification;
import ch.ethz.csb.youscope.shared.configuration.annotations.YSConfigIcon;
import ch.ethz.csb.youscope.shared.measurement.Component;

/**
 * Helper class to construct configuration metadata. Most of the metadata is directly extracted from the configuration class using java reflection.
 * During this extraction, the annotations defined in ch.ethz.csb.youscope.shared.configuration.annotations are used, if given, or generic names for the
 * respective properties is extracted given e.g. the name of the class. 
 * @author Moritz Lang
 *
 * @param <C> Specific subclass of configuration for which an UI should be created.
 */
public class GenericConfigurationMetadata<C extends Configuration> implements ConfigurationMetadata<C> 
{

	private final String typeIdentifier;
	private final Class<C> configurationClass;
	private final Class<? extends Component> componentInterface;
	/**
	 * Constructor.
	 * @param typeIdentifier Type identifier of the configuration.
	 * @param configurationClass Class of the configuration.
	 * @param componentInterface The interface of the measurement component which is created when the configuration is compiled.
	 */
	public GenericConfigurationMetadata(final String typeIdentifier, 
			final Class<C> configurationClass, 
			final Class<? extends Component> componentInterface) 
	{
		this.typeIdentifier = typeIdentifier;
		this.configurationClass = configurationClass;
		this.componentInterface = componentInterface;
	} 
	
	

	@Override
	public String getTypeName() 
	{
		YSConfigAlias alias = configurationClass.getAnnotation(YSConfigAlias.class);
		if(alias != null)
			return alias.value();
		return configurationClass.getSimpleName();
	}

	@Override
	public Class<C> getConfigurationClass() 
	{
		return configurationClass;
	}

	@Override
	public String getTypeIdentifier() 
	{
		return typeIdentifier;
	}

	@Override
	public ImageIcon getIcon() 
	{
		YSConfigIcon path = configurationClass.getAnnotation(YSConfigIcon.class);
		if(path == null)
			return null;
		URL iconURL = GenericConfigurationMetadata.class.getClassLoader().getResource(path.value());
		if (iconURL != null)
			return new ImageIcon(iconURL, getTypeName());
		return null;
	}

	@Override
	public String[] getConfigurationClassification() {
		YSConfigClassification classification = configurationClass.getAnnotation(YSConfigClassification.class);
		if(classification != null)
			return classification.value();
		return new String[0];
	}

	@Override
	public Class<? extends Component> getComponentInterface() {
		return componentInterface;
	}

}
