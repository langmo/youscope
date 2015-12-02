package ch.ethz.csb.youscope.client.uielements;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.shared.configuration.Configuration;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationMetadata;
import ch.ethz.csb.youscope.shared.tools.TextTools;

/**
 * A dropdown list to select components to configure.
 * @author Moritz Lang
 *
 * @param <C> Basic type of components to configure, e.g. JobConfiguration.
 */
public class ComponentComboBox<C extends Configuration> extends JButton 
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 4762690329904024235L;
	private final JPopupMenu componentChooser;
	private final HashMap<String, Element> elements = new HashMap<String, Element>();	
	private final ArrayList<ActionListener> actionListeners = new ArrayList<ActionListener>();
	private Element selectedElement = null;
	private boolean customTextIcon = false;
	private int popupLocation = SwingConstants.BOTTOM;
	
	private final ActionListener elementSelectionListener = new ActionListener() 
	{
		@SuppressWarnings("rawtypes")
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			if(e.getSource() instanceof ComponentComboBox.Element)
			{
				setSelectedElement(((ComponentComboBox.Element)e.getSource()).getTypeIdentifier());
				fireActionEvent();
			}
		}
	};
	
	private static final Icon DOWN_ICON = new Icon()
	{

		@Override
		public int getIconHeight() {
			return 4;
		}

		@Override
		public int getIconWidth() {
			return 8;
		}

		@Override 
		public void paintIcon(Component c, Graphics g, int x, int y) {
			g.setColor(Color.BLACK);
			g.fillPolygon(new int[]{x,x+getIconWidth(),x+getIconWidth()/2}, new int[]{y,y,y+getIconHeight()}, 3);
		}

	};
			
	private static final Icon RIGHT_ICON = new Icon()
	{

		@Override
		public int getIconHeight() {
			return 8;
		}

		@Override
		public int getIconWidth() {
			return 4;
		}

		@Override 
		public void paintIcon(Component c, Graphics g, int x, int y) {
			g.setColor(Color.BLACK);
			g.fillPolygon(new int[]{x,x+getIconWidth(),x}, new int[]{y,y+getIconHeight()/2,y+getIconHeight()}, 3);
		}

	};
	
	/**
	 * Constructs a new combo box with all components added being of the given configuration class, and implementing all of the given required interfaces.
	 * @param client YouScope client.
	 * @param configurationClass Configuration class of components, e.g. JobConfiguration.class.
	 * @param requiredInterface Additional interfaces which are required, e.g. TableProducerConfiguration.class.
	 */
	public ComponentComboBox(YouScopeClient client, Class<C> configurationClass, Class<?>... requiredInterface)
	{
		this(configurationClass, getComponentMetadata(client, configurationClass, requiredInterface));
	}
	
	/**
	 * Constructs a new combo box with all components added being of the given configuration class.
	 * @param client YouScope client.
	 * @param configurationClass Configuration class of components, e.g. JobConfiguration.class.
	 */
	public ComponentComboBox(YouScopeClient client, Class<C> configurationClass)
	{
		this(configurationClass, getComponentMetadata(client, configurationClass));
	}
	
	@Override
	public void setText(String text)
	{
		customTextIcon = true;
		super.setText(text);
	}
	
	@Override
	public void setIcon(Icon icon)
	{
		customTextIcon = true;
		super.setIcon(icon);
	}
	
	private void updateVisuals()
	{
		if(customTextIcon)
		{
			return;
		}
		else if(selectedElement != null)
		{
			super.setText(selectedElement.getText());
			super.setIcon(selectedElement.getIcon());
		}
		else
		{
			super.setText("Component");
			super.setIcon(null);
		}
	}
	
	/**
	 * Returns the currently selected component, or null if no component is selected.
	 * @return Selected component's metadata.
	 */
	public ConfigurationMetadata<? extends C> getSelectedElement()
	{
		if(selectedElement != null)
			return selectedElement.metadata;
		return null;
	}
	
	/**
	 * Returns the type identifier of the currently selected component, or null if no component is selected.
	 * @return Currently selected component's type identifier.
	 */
	public String getSelectedTypeIdentifier()
	{
		if(selectedElement != null)
			return selectedElement.getTypeIdentifier();
		return null;
	}
	
	@Override
	public void addActionListener(ActionListener listener)
	{
		actionListeners.add(listener);
	}
	@Override
	public void removeActionListener(ActionListener listener)
	{
		actionListeners.remove(listener);
	}
	
	private void fireActionEvent()
	{
		for(ActionListener listener : actionListeners)
		{
			listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_FIRST, "Component changed"));
		}
	}
	
	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Icon icon;
		if(popupLocation == SwingConstants.BOTTOM)
		{
			icon = DOWN_ICON;
		}
		else
			icon = RIGHT_ICON;
	
		int x = getWidth() - icon.getIconWidth() - 10; 
		int y = (getHeight() - icon.getIconHeight()) / 2;
		icon.paintIcon(this, g, x, y);
			
	}
	/**
	 * Constructs a new combo box with all components added being passed as a parameter.
	 * 
	 * @param configurationClass Configuration class of components, e.g. JobConfiguration.class.
	 * @param componentMetadata Metadata of the components which can be selected.
	 */
	public ComponentComboBox(Class<C> configurationClass, ConfigurationMetadata<? extends C>[] componentMetadata)
	{
		setHorizontalAlignment(SwingConstants.LEFT);
		componentChooser = new JPopupMenu();
		super.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	if(popupLocation == SwingConstants.BOTTOM)
                		componentChooser.show(ComponentComboBox.this, 0, getHeight());
                	else
                		componentChooser.show(ComponentComboBox.this, getWidth(), 0);
                }
            }); 
		createHierachy(componentMetadata);
		setSelectedElement((String)null);
	}
	
	/**
	 * Sets the location of the popup menu relative to the button. Available choices are
	 * {@link SwingConstants#BOTTOM} or {@link SwingConstants#RIGHT}. All other choices will invoke an {@link IllegalArgumentException}.
	 * @param location Location where the popup should be shown relative to the button.
	 * @throws IllegalArgumentException Thrown if location is invalid.
	 */
	public void setPopupLocation(int location) throws IllegalArgumentException
	{
		if(location == SwingConstants.BOTTOM || location == SwingConstants.RIGHT)
			popupLocation = location;
		else
			throw new IllegalArgumentException("Location must be either SwingConstants.BOTTOM or SwingConstants.RIGHT.");
	}
	
	/**
	 * Sets the currently selected element. If element is not one of the selectable elements, does nothing. If element is null, selects a random element.
	 * @param element Element to select.
	 */
	public void setSelectedElement(ConfigurationMetadata<? extends C> element)
	{
		if(element == null)
			setSelectedElement((String)null);
		setSelectedElement(element.getTypeIdentifier());
	}
	
	/**
	 * Sets the currently selected element. If element is not one of the selectable elements, does nothing. If element is null, selects a random element.
	 * @param typeIdentifier Type identifier of the element to select.
	 */
	public void setSelectedElement(String typeIdentifier)
	{
		Element element;
		if(typeIdentifier == null)
		{
			if(elements.size() == 0)
				return;
			element = elements.values().iterator().next();
		}
		else
		{
			element = elements.get(typeIdentifier);
			if(element == null)
				return;
		}
		selectedElement = element;
		updateVisuals();
	}
	
	private class Element extends JMenuItem
	{
		/**
		 * Serial Version UID.
		 */
		private static final long serialVersionUID = -9172694469309763297L;
		private ConfigurationMetadata<? extends C> metadata;
		Element(ConfigurationMetadata<? extends C> metadata)
		{
			super(TextTools.capitalize(metadata.getTypeName()));
			ImageIcon icon = metadata.getIcon();
			if(icon == null)
			{
				setIcon(ImageLoadingTools.getResourceIcon("icons/block--plus.png", "Component"));
			}
			else
				setIcon(icon);
			this.metadata = metadata;
		}
		String getTypeIdentifier()
		{
			return metadata.getTypeIdentifier();
		}
		String[] getConfigurationClassification()
		{
			return metadata.getConfigurationClassification();
		}
	}
	
	private void createHierachy(ConfigurationMetadata<? extends C>[] componentMetadata)
	{
		ImageIcon folderIcon = ImageLoadingTools.getResourceIcon("icons/folder-horizontal-open.png", "Folder");
		JMenu rootMenu = new JMenu();
		for (ConfigurationMetadata<? extends C> addon : componentMetadata)
        {
			Element element = new Element(addon);
			element.addActionListener(elementSelectionListener);
			elements.put(element.getTypeIdentifier(), element);
            
			// Setup folder structure
			JMenu parentMenu = rootMenu;
			for(String classification : element.getConfigurationClassification())
			{
				// Iterate over all menus to check if it already exists
				boolean found = false;
				for(int i=0; i<parentMenu.getItemCount(); i++)
				{
					JMenuItem existingItem = parentMenu.getItem(i);
					if(!(existingItem instanceof JMenu))
						continue;
					if(((JMenu)existingItem).getText().compareToIgnoreCase(classification) == 0)
					{
						parentMenu = (JMenu)existingItem;
						found = true;
						break;
					}
				}
				
				if(!found)
				{
					JMenu newMenu = new JMenu(TextTools.capitalize(classification));
					if(folderIcon != null)
						newMenu.setIcon(folderIcon);
					parentMenu.add(newMenu);
					parentMenu = newMenu;
				}
			}
            parentMenu.add(element);
            
    	}
		
		shrinkOptions(rootMenu);
		
		for(Component component : rootMenu.getMenuComponents())
		{
			componentChooser.add(component);
		}
		//componentChooser.add(rootMenu);
      
	}
	private void shrinkOptions(JMenu parentMenu)
	{
		for(int i=0; i<parentMenu.getItemCount(); i++)
		{
			JMenuItem item = parentMenu.getItem(i);
			if(item!= null && item instanceof JMenu)
				shrinkOptions((JMenu) item);
		}
		if(parentMenu.getItemCount() == 1 && parentMenu.getMenuComponent(0) instanceof JMenu)
		{
			JMenu child = (JMenu)parentMenu.getMenuComponent(0);
			parentMenu.remove(child);
			for(Component component : child.getMenuComponents())
			{
				parentMenu.add(component);
			}
		}
	}
	
	private static <C extends Configuration> ConfigurationMetadata<? extends C>[] getComponentMetadata(YouScopeClient client, Class<C> configurationClass, Class<?>... requiredInterface)
	{
		ArrayList<ConfigurationMetadata<? extends C>> components = new ArrayList<ConfigurationMetadata<? extends C>>();
		outerIter:for(ConfigurationMetadata<? extends C> metadata : client.getAddonProvider().getConformingConfigurationMetadata(configurationClass))
		{
			for(Class<?> anInterface : requiredInterface)
			{
				if(anInterface != null && !anInterface.isAssignableFrom(metadata.getConfigurationClass()))
					continue outerIter;
			}
			components.add(metadata);
		}
		@SuppressWarnings("unchecked")
		ConfigurationMetadata<? extends C>[] returnVal = (ConfigurationMetadata<? extends C>[]) components.toArray(new ConfigurationMetadata<?>[components.size()]);
		return returnVal;
	}
}
