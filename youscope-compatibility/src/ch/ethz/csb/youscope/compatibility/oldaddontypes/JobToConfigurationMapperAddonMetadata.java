package ch.ethz.csb.youscope.compatibility.oldaddontypes;

import javax.swing.ImageIcon;

import ch.ethz.csb.youscope.client.addon.job.JobConfigurationAddonFactory;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationMetadata;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;
import ch.ethz.csb.youscope.shared.measurement.Component;
import ch.ethz.csb.youscope.shared.measurement.job.Job;

@SuppressWarnings("deprecation")
class JobToConfigurationMapperAddonMetadata<C extends JobConfiguration> implements ConfigurationMetadata<C>
{
	private final JobConfigurationAddonFactory nativeConfigurationFactory;
	private final String typeIdentifier;
	private final Class<C> configurationClass;
	private JobToConfigurationMapperAddonMetadata(String typeIdentifier, Class<C> configurationClass, JobConfigurationAddonFactory nativeConfigurationFactory) 
	{
		this.nativeConfigurationFactory = nativeConfigurationFactory;
		this.typeIdentifier = typeIdentifier;
		this.configurationClass = configurationClass;
	}
	
	static <C extends JobConfiguration> JobToConfigurationMapperAddonMetadata<C> getMetadata(String typeIdentifier, Class<C> configurationClass, JobConfigurationAddonFactory nativeConfigurationFactory)
	{
		return new JobToConfigurationMapperAddonMetadata<C>(typeIdentifier, configurationClass, nativeConfigurationFactory);
	}
	@Override
	public String getTypeName() 
	{
		String nativeName = nativeConfigurationFactory.getJobName(typeIdentifier);
		String[] classification = nativeName.split("/");
		if(classification.length <=1)
			return nativeName;
		return classification[classification.length-1];
	}
	@Override
	public Class<C> getConfigurationClass() 
	{
		return configurationClass;
	}
	@Override
	public String getTypeIdentifier() {
		return typeIdentifier;
	}
	@Override
	public ImageIcon getIcon() {
		return nativeConfigurationFactory.getJobIcon(typeIdentifier);
	}
	@Override
	public String[] getConfigurationClassification() 
	{
		String nativeName = nativeConfigurationFactory.getJobName(typeIdentifier);
		String[] classification = nativeName.split("/");
		if(classification.length <=1)
			return new String[0];
		String[] newClassification = new String[classification.length-1];
		System.arraycopy(classification, 0, newClassification, 0, newClassification.length);
		return newClassification;
	}

	@Override
	public Class<? extends Component> getComponentInterface() 
	{
		// we cannot determine the precise interface the job produced is implementing, thus, we return just the topmost interface (i.e. Job). 
		return Job.class;
	}

}
