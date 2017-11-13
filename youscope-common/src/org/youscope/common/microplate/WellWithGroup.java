package org.youscope.common.microplate;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.io.Serializable;

import javax.swing.Icon;

import org.youscope.common.Well;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A tuple consisting of a well, and a group. This follows the logic that in certain conditions each well belongs to
 * a designated well groups. For example, all wells of a microplate belonging to the same group might perform
 * the same imaging protocol, but wells of different groups might perform different protocols.
 * 
 * @author Moritz Lang
 *
 */
@XStreamAlias("well-with-group")
public class WellWithGroup implements Serializable
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 9217362001562054051L;
	/**
	 * Each selected well belongs to a group, which could e.g. be subsets of wells having the same
	 * imaging protocol.
	 * @author Moritz Lang
	 *
	 */
	public enum WellGroup
	{
		/**
		 * The first well group.
		 */
		GROUP0(Color.GREEN),
		/**
		 * The second well group.
		 */
		GROUP1(Color.BLUE),
		/**
		 * The third well group.
		 */
		GROUP2(Color.YELLOW),
		/**
		 * The fourth well group.
		 */
		GROUP3(Color.RED),
		/**
		 * The fifth well group.
		 */
		GROUP4(Color.CYAN),
		/**
		 * The sixth well group.
		 */
		GROUP5(Color.ORANGE),
		/**
		 * The seventh well group.
		 */
		GROUP6(Color.MAGENTA)
		;
		
		WellGroup(Color color)
		{
			this.color = color;
		}
		/**
		 * Returns the color associated to this well group.
		 * @return Color of the well group.
		 */
		public Color getColor()
		{
			return color;
		}
		/**
		 * Returns the zweo based ID of this group.
		 * @return Zero based ID of group.
		 */
		public int getGroupId()
		{
			return ordinal();
		}
		/**
		 * Returns the name of the group, in the form "Well Group [i]", where [i] is the ONE based index of the group.
		 * @return Canonical name of the well group.
		 */
		public String getName()
		{
			return "Well Group " +Integer.toString(ordinal()+1);
		}
		/**
		 * Returns an icon for this group, which is nothing else than an area filled with the groups color.
		 * @return Icon for group
		 */
		public Icon getIcon()
		{
			return new Icon()
			{
				private final int WIDTH = 12;
				private final int HEIGHT = 12;
			    @Override
				public void paintIcon(Component c, Graphics g, int x, int y) 
			    {
			        g.setColor(getColor());
			        g.fillRect(x, y, WIDTH-1, HEIGHT-1);
			        g.setColor(Color.BLACK);
			        g.drawRect(x, y, WIDTH-1, HEIGHT-1);
			    }

			    @Override
				public int getIconWidth() {
			        return WIDTH;
			    }

			    @Override
				public int getIconHeight() {
			        return HEIGHT;
			    }
			};
		}
		private final Color color;
	}
	/**
	 * Constructor.
	 * @param well The well.
	 * @param group The group of the well.
	 */
	public WellWithGroup(Well well, WellGroup group) 
	{
		this.well = well;
		this.group = group;
	}
	@XStreamAlias("well")
	private final Well well;
	@XStreamAlias("group")
	private final WellGroup group;
	/**
	 * Returns the well belonging to this tuple.
	 * @return Well of tuple.
	 */
	public Well getWell()
	{
		return well;
	}
	/**
	 * Returns the group to which this well belongs.
	 * @return group of the well.
	 */
	public WellGroup getGroup()
	{
		return group;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		result = prime * result + ((well == null) ? 0 : well.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WellWithGroup other = (WellWithGroup) obj;
		if (group != other.group)
			return false;
		if (well == null) {
			if (other.well != null)
				return false;
		} else if (!well.equals(other.well))
			return false;
		return true;
	}
}
