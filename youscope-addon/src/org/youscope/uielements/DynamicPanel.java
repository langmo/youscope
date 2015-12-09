/**
 * 
 */
package org.youscope.uielements;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;

/**
 * A panel simplifying with a grid bag layout simplifying to handle it.
 * @author Moritz Lang
 *
 */
public class DynamicPanel extends JPanel
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -8612056620799736785L;

	/**
	 * Standard constraints used when adding an element without arguments.
	 */
	private static final GridBagConstraints	NEW_LINE_CONSTRAINT	= new GridBagConstraints()
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -8234666817369826670L;

		{
			this.fill = GridBagConstraints.HORIZONTAL;
			this.gridwidth = GridBagConstraints.REMAINDER;
			this.anchor = GridBagConstraints.NORTHWEST;
			this.gridx = 0;
			this.weightx = 1.0;
		}
	};
	
	/**
	 * Constraint used when adding a component with the fill option.
	 */
	private static final GridBagConstraints	FILL_CONSTRAINT	= new GridBagConstraints()
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -8234666817369826671L;
		
		{
			this.weighty = 1.0;
			this.weightx = 1.0;
			this.fill = GridBagConstraints.BOTH;
			this.gridwidth = GridBagConstraints.REMAINDER;
		}
	};
	
	private final GridBagLayout layout = new GridBagLayout();
	
	/**
	 * Constructor.
	 */
	public DynamicPanel()
	{
		setLayout(layout);
		setOpaque(false);
	}
	
	@Override
	public Component add(Component component)
	{
		return this.add(component, NEW_LINE_CONSTRAINT);
	}
	
	@Override
	public Component add(Component component, int index) throws IllegalArgumentException 
	{
		return this.add(component, index, NEW_LINE_CONSTRAINT);
	}
	
	/**
	 * Adding an element, setting the layout such that the element gets additional vertical space if available.
	 * @param component The component to add.
	 * @return The added component.
	 */
	public Component addFill(Component component)
	{
		return this.add(component, FILL_CONSTRAINT);
	}
	
	/**
	 * Inserts an element at the given index, setting the layout such that the element gets additional vertical space if available.
	 * @param component The component to add.
	 * @param index The index where to add the component.
	 * @return The added component.
	 * @throws IllegalArgumentException If the index is invalid.
	 */
	public Component addFill(Component component, int index) throws IllegalArgumentException 
	{
		return this.add(component, index, FILL_CONSTRAINT);
	}
	
	/**
	 * Adds an empty document, setting its layout such that it fills empty vertical space if available.
	 * @return The added empty document.
	 */
	public Component addFillEmpty()
	{
		JPanel emptyPanel = new JPanel();
		emptyPanel.setOpaque(false);
		return addFill(emptyPanel);
	}
	
	/**
	 * Adds a component, setting the grid bag layout to the given constraints.
	 * @param component The component to add.
	 * @param constraint The constraints to set the layout to before adding.
	 * @return the added component.
	 */
	public Component add(Component component, GridBagConstraints constraint)
	{
		layout.setConstraints(component, constraint);
		return super.add(component);
	}
	
	/**
	 * Adds a component at a specific position, setting the grid bag layout to the given constraints.
	 * @param component The component to add.
	 * @param index Index where to add component. Must be greater or equal 0 and smaller than {@link #getComponentCount()}
	 * @param constraint The constraints to set the layout to before adding.
	 * @return the added component.
	 * @throws IllegalArgumentException if index is invalid 
	 * @throws NullPointerException if comp is null
	 */
	public Component add(Component component, int index, GridBagConstraints constraint) throws IllegalArgumentException, NullPointerException
	{
		layout.setConstraints(component, constraint);
		return super.add(component, index);
	}
}
