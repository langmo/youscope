package org.youscope.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import org.youscope.clientinterfaces.MetadataDefinition;
import org.youscope.clientinterfaces.MetadataDefinitionProvider;

class MeasurementMetadataProviderImpl extends HashMap<String, MetadataDefinition> implements MetadataDefinitionProvider 
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 64314818791363585L;
	private static final String PROPERTY_ALLOW_CUSTOM_PROPERTIES = "Youscope.Client.AllowCustomProperties";
	public MeasurementMetadataProviderImpl() 
	{
		 setMetadataDefinitions(generateDefaults());
	}

	private static Collection<MetadataDefinition> generateDefaults()
	{
		ArrayList<MetadataDefinition> result = new ArrayList<>();
		result.add(new MetadataDefinition("Temperature", MetadataDefinition.Type.DEFAULT , true,0,100,null, "°C"));
		result.add(new MetadataDefinition("User", MetadataDefinition.Type.OPTIONAL, true));
		result.add(new MetadataDefinition("Species", MetadataDefinition.Type.DEFAULT, true, getModelOrganisms()));
		result.add(new MetadataDefinition("Strain", MetadataDefinition.Type.DEFAULT, true));
		return result;
	}
	private static String[] getModelOrganisms()
	{
		String[] result = new String[]
		{
				"Escherichia coli",
				"Dictyostelium discoideum",
				"Saccharomyces cerevisiae",
				"Schizosaccharomyces pombe",
				"Chlamydomonas reinhardtii",
				"Tetrahymena thermophila",
				"Emiliania huxleyi",
				"Caenorhabditis elegans",
				"Drosophila melanogaster",
				"Arabidopsis thaliana",
				"Physcomitrella patens",
				"Danio rerio",
				"Fundulus heteroclitus",
				"Nothobranchius furzeri",
				"Oryzias latipes",
				"Anolis carolinensis",
				"Mus musculus",
				"Xenopus laevis"
		};
		Arrays.sort(result);
		return result;
	}
	
	@Override
	public boolean isAllowCustomMetadata() {
		return PropertyProviderImpl.getInstance().getProperty(PROPERTY_ALLOW_CUSTOM_PROPERTIES, true);
	}

	@Override
	public Iterator<MetadataDefinition> iterator() {
		return getMetadataDefinitions().iterator();
	}
	
	@Override
	public Collection<MetadataDefinition> getMetadataDefinitions() {
		ArrayList<MetadataDefinition> result = new ArrayList<MetadataDefinition>(values());
		Collections.sort(result);
		return result;
	}

	@Override
	public Collection<MetadataDefinition> getMandatoryMetadataDefinitions() {
		ArrayList<MetadataDefinition> result = new ArrayList<MetadataDefinition>(values());
		for(Iterator<MetadataDefinition> iter = result.iterator(); iter.hasNext();)
		{
			if(iter.next().getType() != MetadataDefinition.Type.MANDATORY)
				iter.remove();
		}
		Collections.sort(result);
		return result;
	}
	
	@Override
	public Collection<MetadataDefinition> getDefaultMetadataDefinitions() {
		ArrayList<MetadataDefinition> result = new ArrayList<MetadataDefinition>(values());
		for(Iterator<MetadataDefinition> iter = result.iterator(); iter.hasNext();)
		{
			MetadataDefinition.Type type = iter.next().getType();
			if(type != MetadataDefinition.Type.MANDATORY && type != MetadataDefinition.Type.DEFAULT)
				iter.remove();
		}
		Collections.sort(result);
		return result;
	}

	@Override
	public MetadataDefinition getMetadataDefinition(String name) {
		return get(name);
	}

	@Override
	public void setMetadataDefinition(MetadataDefinition property) {
		put(property.getName(), property);
	}

	@Override
	public void setMetadataDefinitions(Collection<MetadataDefinition> properties) {
		for(MetadataDefinition property : properties)
		{
			setMetadataDefinition(property);
		}
	}

	@Override
	public boolean deleteMetadataDefinition(String name) {
		return remove(name) != null;
	}

	@Override
	public int getNumMetadataDefinitions() {
		return size();
	}
}
