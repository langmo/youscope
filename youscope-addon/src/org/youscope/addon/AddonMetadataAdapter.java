package org.youscope.addon;

import javax.swing.Icon;

import org.youscope.uielements.ImageLoadingTools;

/**
 * Adapter to simplify {@link AddonMetadata} construction.
 * @author langmo
 *
 */
public class AddonMetadataAdapter implements AddonMetadata
{
	private final String typeIdentifier;
	private final String typeName;
	private final String[] classification;
	private final Icon icon;
	
	/**
	 * Constructor. Sets icon to default icon.
	 * @param typeIdentifier Unique type identifier of the addon. Must not be null.
	 * @param typeName Human readable name of the addon. Must not be null.
	 * @param classification Classification of the addon. Set to null for unclassified.
	 */
	public AddonMetadataAdapter(String typeIdentifier, String typeName, String[] classification)
	{
		this(typeIdentifier, typeName, classification, (Icon)null);
	}
	
	/**
	 * Constructor. 
	 * @param typeIdentifier Unique type identifier of the addon. Must not be null.
	 * @param typeName Human readable name of the addon. Must not be null.
	 * @param classification Classification of the addon. Set to null for unclassified.
	 * @param icon Icon of the addon. Set to null for default icon.
	 */
	public AddonMetadataAdapter(String typeIdentifier, String typeName, String[] classification, Icon icon)
	{
		if(typeIdentifier == null || typeName == null)
			throw new NullPointerException();
		
		this.typeIdentifier = typeIdentifier;
		this.typeName = typeName;
		if(classification != null)
			this.classification = classification;
		else
			this.classification = new String[0];
		this.icon = icon;
	}
	
	/**
	 * Constructor.
	 * @param typeIdentifier Unique type identifier of the addon. Must not be null.
	 * @param typeName Human readable name of the addon. Must not be null.
	 * @param classification Classification of the addon. Set to null for unclassified.
	 * @param iconPath Path of icon of the addon. Set to null for default icon.
	 */
	public AddonMetadataAdapter(String typeIdentifier, String typeName, String[] classification, String iconPath)
	{
		this(typeIdentifier, typeName, classification, iconPath == null ? null : ImageLoadingTools.getResourceIcon(iconPath, typeName));
	}

	@Override
	public String getTypeName()
	{
		return typeName;
	}

	@Override
	public String getTypeIdentifier()
	{
		return typeIdentifier;
	}

	@Override
	public Icon getIcon()
	{
		return icon;
	}

	@Override
	public String[] getClassification()
	{
		return classification;
	}
}
