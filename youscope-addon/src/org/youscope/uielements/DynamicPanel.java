/*******************************************************************************
 * Copyright (c) 2017 Moritz Lang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Moritz Lang - initial API and implementation
 ******************************************************************************/
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
	 * The element fills the line, and the next element will be placed below.
	 */
	public static final GridBagConstraints	CONSTRAINT_NEW_LINE	= new GridBagConstraints()
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
	 * The element will be centered at the line, and the next element will be placed below.
	 */
	public static final GridBagConstraints	CONSTRAINT_CENTER	= new GridBagConstraints()
	{
		/**
		 * Serial Version UID.
		 */
		private static final long serialVersionUID = 6565854468499172536L;

		{
			this.gridwidth = GridBagConstraints.REMAINDER;
			this.anchor = GridBagConstraints.CENTER;
			this.gridx = GridBagConstraints.RELATIVE;
			this.weightx = 1.0;
		}
	};
	
	/**
	 * The element fills the line, and the next element will be placed below. Furthermore, the component
	 * tries to fill the remaining vertical space. If more than one element is added with this option,
	 * the elements will share the remaining vertical space equally.
	 */
	private static final GridBagConstraints	CONSTRAINT_FILL	= new GridBagConstraints()
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
		return this.add(component, CONSTRAINT_NEW_LINE);
	}
	
	@Override
	public Component add(Component component, int index) throws IllegalArgumentException 
	{
		return this.add(component, index, CONSTRAINT_NEW_LINE);
	}
	
	/**
	 * Adding an element, setting the layout such that the element gets additional vertical space if available.
	 * @param component The component to add.
	 * @return The added component.
	 */
	public Component addFill(Component component)
	{
		return this.add(component, CONSTRAINT_FILL);
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
		return this.add(component, index, CONSTRAINT_FILL);
	}
	
	/**
	 * Adding an element, setting the layout such that it is centered in its own line..
	 * @param component The component to add.
	 * @return The added component.
	 */
	public Component addCenter(Component component)
	{
		return this.add(component, CONSTRAINT_CENTER);
	}
	
	/**
	 * Inserts an element at the given index, setting the layout such that the element gets centered in its own line.
	 * @param component The component to add.
	 * @param index The index where to add the component.
	 * @return The added component.
	 * @throws IllegalArgumentException If the index is invalid.
	 */
	public Component addCenter(Component component, int index) throws IllegalArgumentException 
	{
		return this.add(component, index, CONSTRAINT_CENTER);
	}
	
	/**
	 * Adds an empty {@link JPanel}, setting its layout such that it fills empty vertical space if available.
	 * Useful to consume additional space in a layout, if all other components should not be bigger than their preferred size.
	 * @return The added empty JPanel.
	 */
	public Component addFillEmpty()
	{
		JPanel emptyPanel = new JPanel();
		emptyPanel.setOpaque(false);
		return addFill(emptyPanel);
	}
	
	/**
	 * Adds an empty {@link JPanel}.
	 * Useful to implement a spacer.
	 * @return The added empty JPanel.
	 */
	public Component addEmpty()
	{
		JPanel emptyPanel = new JPanel();
		emptyPanel.setOpaque(false);
		return add(emptyPanel);
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
