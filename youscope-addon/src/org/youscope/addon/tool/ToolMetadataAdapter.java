package org.youscope.addon.tool;

import javax.swing.Icon;

import org.youscope.uielements.ImageLoadingTools;

/**
 * Adapter to simplify {@link ToolMetadata} construction.
 * @author langmo
 *
 */
public class ToolMetadataAdapter implements ToolMetadata
{
	private final String typeIdentifier;
	private final String typeName;
	private final String[] classification;
	private final Icon icon;
	private final Class<? extends ToolAddonUI> toolInterface;
	
	/**
	 * Constructor. Sets icon to default icon and tool interface to default interface (no special exposed functionality).
	 * @param typeIdentifier Unique type identifier of the tool type. Must not be null.
	 * @param typeName Human readable name of the tool. Must not be null.
	 * @param classification Classification of the tool. Set to null for unclassified.
	 */
	public ToolMetadataAdapter(String typeIdentifier, String typeName, String[] classification)
	{
		this(typeIdentifier, typeName, classification, (Icon)null, null);
	}
	
	/**
	 * Constructor. Sets tool interface to default interface (no special exposed functionality).
	 * @param typeIdentifier Unique type identifier of the tool type. Must not be null.
	 * @param typeName Human readable name of the tool. Must not be null.
	 * @param classification Classification of the tool. Set to null for unclassified.
	 * @param icon Icon of the tool. Set to null for default icon.
	 */
	public ToolMetadataAdapter(String typeIdentifier, String typeName, String[] classification, Icon icon)
	{
		this(typeIdentifier, typeName, classification, icon, null);
	}
	
	/**
	 * Constructor. Sets tool interface to default interface (no special exposed functionality).
	 * @param typeIdentifier Unique type identifier of the tool type. Must not be null.
	 * @param typeName Human readable name of the tool. Must not be null.
	 * @param classification Classification of the tool. Set to null for unclassified.
	 * @param iconPath Path of icon of the tool. Set to null for default icon.
	 */
	public ToolMetadataAdapter(String typeIdentifier, String typeName, String[] classification, String iconPath)
	{
		this(typeIdentifier, typeName, classification, iconPath, null);
	}
	
	/**
	 * Constructor. Sets icon to default icon.
	 * @param typeIdentifier Unique type identifier of the tool type. Must not be null.
	 * @param typeName Human readable name of the tool. Must not be null.
	 * @param classification Classification of the tool. Set to null for unclassified.
	 * @param toolInterface interface of the tool type. Set to null to use {@link ToolAddonUI#getClass()}.
	 */
	public ToolMetadataAdapter(String typeIdentifier, String typeName, String[] classification, Class<? extends ToolAddonUI> toolInterface)
	{
		this(typeIdentifier, typeName, classification, (Icon)null, toolInterface);
	}
	
	/**
	 * Constructor.
	 * @param typeIdentifier Unique type identifier of the tool type. Must not be null.
	 * @param typeName Human readable name of the tool. Must not be null.
	 * @param classification Classification of the tool. Set to null for unclassified.
	 * @param icon Icon of the tool. Set to null for default icon.
	 * @param toolInterface interface of the tool type. Set to null to use {@link ToolAddonUI#getClass()}.
	 */
	public ToolMetadataAdapter(String typeIdentifier, String typeName, String[] classification, Icon icon, Class<? extends ToolAddonUI> toolInterface)
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
		if(toolInterface != null)
			this.toolInterface = toolInterface;
		else
			this.toolInterface = ToolAddonUI.class;
	}
	/**
	 * Constructor.
	 * @param typeIdentifier Unique type identifier of the tool type. Must not be null.
	 * @param typeName Human readable name of the tool. Must not be null.
	 * @param classification Classification of the tool. Set to null for unclassified.
	 * @param iconPath Path of the icon of the tool. Set to null for default icon.
	 * @param toolInterface interface of the tool type. Set to null to use {@link ToolAddonUI#getClass()}.
	 */
	public ToolMetadataAdapter(String typeIdentifier, String typeName, String[] classification, String iconPath, Class<? extends ToolAddonUI> toolInterface)
	{
		this(typeIdentifier, typeName, classification, iconPath == null ? null : ImageLoadingTools.getResourceIcon(iconPath, typeName), toolInterface);
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

	@Override
	public Class<? extends ToolAddonUI> getToolInterface()
	{
		return toolInterface;
	}

}
