package org.youscope.addon.tool;

import javax.swing.Icon;

import org.youscope.addon.AddonMetadataAdapter;

/**
 * Adapter to simplify {@link ToolMetadata} construction.
 * @author langmo
 *
 */
public class ToolMetadataAdapter extends AddonMetadataAdapter implements ToolMetadata
{
	/**
	 * Constructor. Sets icon to default icon.
	 * @param typeIdentifier Unique type identifier of the tool type. Must not be null.
	 * @param typeName Human readable name of the tool. Must not be null.
	 * @param classification Classification of the tool. Set to null for unclassified.
	 */
	public ToolMetadataAdapter(String typeIdentifier, String typeName, String[] classification)
	{
		super(typeIdentifier, typeName, classification);
	}
	
	/**
	 * Constructor. 
	 * @param typeIdentifier Unique type identifier of the tool type. Must not be null.
	 * @param typeName Human readable name of the tool. Must not be null.
	 * @param classification Classification of the tool. Set to null for unclassified.
	 * @param icon Icon of the tool. Set to null for default icon.
	 */
	public ToolMetadataAdapter(String typeIdentifier, String typeName, String[] classification, Icon icon)
	{
		super(typeIdentifier, typeName, classification, icon);
	}
	
	/**
	 * Constructor.
	 * @param typeIdentifier Unique type identifier of the tool type. Must not be null.
	 * @param typeName Human readable name of the tool. Must not be null.
	 * @param classification Classification of the tool. Set to null for unclassified.
	 * @param iconPath Path of icon of the tool. Set to null for default icon.
	 */
	public ToolMetadataAdapter(String typeIdentifier, String typeName, String[] classification, String iconPath)
	{
		super(typeIdentifier, typeName, classification, iconPath);
	}
}
