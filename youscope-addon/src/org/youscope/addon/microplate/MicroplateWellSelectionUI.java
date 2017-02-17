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
package org.youscope.addon.microplate;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.youscope.addon.AddonException;
import org.youscope.addon.AddonMetadata;
import org.youscope.addon.AddonMetadataAdapter;
import org.youscope.addon.AddonUIAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.PositionInformation;
import org.youscope.common.Well;
import org.youscope.common.measurement.SimpleMeasurementContext;
import org.youscope.common.microplate.MicroplateLayout;
import org.youscope.common.microplate.WellLayout;
import org.youscope.serverinterfaces.YouScopeServer;


/**
 * Component allowing to select the wells of a microplate design.
 * @author Moritz Lang
 *
 */
public class MicroplateWellSelectionUI extends AddonUIAdapter<AddonMetadata>
{
	protected static final String TITLE = "Well Selection:";
	private final static String TYPE_IDENTIFIER = "YouScope.MicroplateWellSelectionUI";
	private final ArrayList<ActionListener> wellChangeListeners = new ArrayList<>(1);
	private final MicroplateDisplay microplateDisplay = new MicroplateDisplay();
	
	private double minWellX= Double.MAX_VALUE;
	private double maxWellX = -Double.MAX_VALUE;
	private double minWellY = Double.MAX_VALUE;
	private double maxWellY = -Double.MAX_VALUE;
	private final HashMap<Well,WellDisplay> wellDisplays = new HashMap<>(200);
	private SelectionMode selectionMode = SelectionMode.MULTIPLE;
	private Well lastSelectedWell = null;
	private HashMap<Integer, Point2D.Double> xWellLabelPositions = null;
	private HashMap<Integer, Point2D.Double> yWellLabelPositions = null;
	private boolean showXWellLabels = true;
	private boolean showYWellLabels = true;
	
	private static final Font WELL_LABEL_FONT = new Font(Font.DIALOG, Font.BOLD, 12);
	private static final double WELL_LABEL_FONT_MARGIN = 3;
	private boolean center = false;
	private final Color textColor;
	
	/**
	 * Enumeration defining how many wells can be selected.
	 * @author Moritz Lang
	 *
	 */
	public enum SelectionMode
	{
		/**
		 * Multiple wells can be selected.
		 */
		MULTIPLE,
		/**
		 * Maximally one well can be selected.
		 */
		SINGLE,
		/**
		 * Exactly one well is always selected.
		 */
		EXACTLY_ONE,
		/**
		 * No well can be selected.
		 */
		NONE
	}
	
	/**
	 * Constructor.
	 * @param client YouScope client.
	 * @param server YouScope server
	 */
	public MicroplateWellSelectionUI(YouScopeClient client, YouScopeServer server)
	{
		super(getMetadata(), client, server);	
		setMicroplateLayout((MicroplateLayout)null);
		Color color = UIManager.getColor("Label.foreground");
		if(color != null)
			textColor = color;
		else
			textColor = Color.BLACK;
	}
	
	private static AddonMetadata getMetadata()
	{
		return new AddonMetadataAdapter(TYPE_IDENTIFIER, "Microplate Well Selection", new String[0],
				"User interface to select wells of a microplate.");
	}
	
	/**
	 * Returns true if the horizontal cell numbers should be shown above the microplate. A true value only has an effect if the microplate
	 * layout is regular enough such that these labels make sense.
	 * @return True if labels are shown.
	 */
	public boolean isShowXWellLabels() {
		return showXWellLabels;
	}
	/**
	 * Set to true if the horizontal cell numbers should be shown above the microplate. A true value only has an effect if the microplate
	 * layout is regular enough such that these labels make sense.
	 * @param showXWellLabels True if labels are shown.
	 */
	public void setShowXWellLabels(boolean showXWellLabels) {
		this.showXWellLabels = showXWellLabels;
	}
	/**
	 * Returns true if the vertical cell numbers should be shown left to the microplate. A true value only has an effect if the microplate
	 * layout is regular enough such that these labels make sense.
	 * @return True if labels are shown.
	 */
	public boolean isShowYWellLabels() {
		return showYWellLabels;
	}
	/**
	 * Set to true if the vertical cell numbers should be shown left to the microplate. A true value only has an effect if the microplate
	 * layout is regular enough such that these labels make sense.
	 * @param showYWellLabels True if labels are shown.
	 */
	public void setShowYWellLabels(boolean showYWellLabels) {
		this.showYWellLabels = showYWellLabels;
	}

	/**
	 * Returns the active selection mode, i.e. if no, one or multiple wells can be selected.
	 * @return Current selection mode.
	 */
	public SelectionMode getSelectionMode()
	{
		return selectionMode;
	}
	/**
	 * Sets the current selection mode, i.e. if no, one or multiple wells can be selected.
	 * @param selectionMode Current selection mode.
	 */
	public void setSelectionMode(SelectionMode selectionMode)
	{
		if(this.selectionMode == selectionMode || selectionMode == SelectionMode.MULTIPLE)
		{
			this.selectionMode = selectionMode;
			return;
		}
		this.selectionMode = selectionMode;
		if(selectionMode == SelectionMode.NONE)
			lastSelectedWell = null;
		for(WellDisplay wellDisplay : wellDisplays.values())
		{
			if(lastSelectedWell != null && lastSelectedWell.equals(wellDisplay.getWell()))
				continue;
			wellDisplay.setSelected(false);
		}
		if(lastSelectedWell == null && selectionMode == SelectionMode.EXACTLY_ONE)
			selectFirstWell();
		microplateDisplay.repaint();
	}
	
	/**
	 * Returns true if the microplate is centered in the {@link Component}, and false if it is at the top left.
	 * @return True if centered.
	 */
	public boolean isCenter()
	{
		return center;
	}
	
	/**
	 * Set to true to center the microplate in the {@link Component}, and to false to move it to the top left.
	 * @param center True if centered.
	 */
	public void setCenter(boolean center)
	{
		this.center = center;
		microplateDisplay.repaint();
	}
	
	/**
	 * Returns a collection of the well layouts selected by the user or programmatically selected.
	 * @return Selected wells.
	 */
	public Set<WellLayout> getSelectedWellLayouts()
	{
		HashSet<WellLayout> returnVal;
		if(selectionMode == SelectionMode.NONE)
		{
			returnVal = new HashSet<>(0);
		}
		else if(selectionMode == SelectionMode.SINGLE || selectionMode == SelectionMode.EXACTLY_ONE)
		{
			returnVal = new HashSet<>(1);
			if(lastSelectedWell != null)
				returnVal.add(wellDisplays.get(lastSelectedWell).getWellLayout());
		}
		else
		{
			returnVal = new HashSet<>(wellDisplays.size());
			for(WellDisplay well : wellDisplays.values())
			{
				if(well.isSelected())
					returnVal.add(well.getWellLayout());
			}
		}
		return returnVal;
	}
	
	/**
	 * Returns a collection of well identifiers of the wells selected by the user or programmatically selected.
	 * @return Well identifiers of selected wells.
	 */
	public Set<Well> getSelectedWells()
	{
		Set<WellLayout> selectedWells = getSelectedWellLayouts();
		HashSet<Well> returnVal = new HashSet<>(selectedWells.size());
		for(WellLayout well : selectedWells)
		{
			returnVal.add(well.getWell());
		}
		return returnVal;
	}
	/**
	 * Returns the last selected well, or null if no well was yet selected.
	 * @return Last selected well or null.
	 */
	public Well getSelectedWell()
	{
		return lastSelectedWell;
	}
	
	/**
	 * Returns the layout of the last selected well, or null if no well was yet selected.
	 * @return Layout of last selected well or null.
	 */
	public WellLayout getSelectedWellLayout()
	{
		return lastSelectedWell == null ? null : wellDisplays.get(lastSelectedWell).getWellLayout();
	}
	
	/**
	 * Selects or deselects the well with the given well identifier. Returns true if successful. Returns false if well does not exist or is disabled.
	 * @param well Well identifier of well to select or deselect.
	 * @param selected true if well should be selected, false if unselected.
	 * @return True if well could be selected/deselected, false if well does not exist or is disabled.
	 */
	public boolean setSelected(Well well, boolean selected)
	{
		if(selectionMode == SelectionMode.NONE || well == null)
			return false;
		WellDisplay wellDisplay = wellDisplays.get(well);
		if(wellDisplay == null)
			return false;
		if(!wellDisplay.isEnabled())
			return false;
		wellDisplay.setSelected(selected);
		
		if(selected)
		{
			if((selectionMode == SelectionMode.SINGLE || selectionMode == SelectionMode.EXACTLY_ONE) && lastSelectedWell != null && !lastSelectedWell.equals(well))
				wellDisplays.get(lastSelectedWell).setSelected(false);
			lastSelectedWell = well;
		}
		else if(lastSelectedWell != null && well.equals(lastSelectedWell))
		{
			if(selectionMode == SelectionMode.EXACTLY_ONE)
			{
				// veto the change
				wellDisplay.setSelected(true);
				return false;
			}
			lastSelectedWell = null;
		}
		microplateDisplay.repaint();
		return true;
	}
	private void selectFirstWell()
	{
		if(lastSelectedWell != null||wellDisplays.size() <= 0)
			return;
		for(WellDisplay wellDisplay : wellDisplays.values())
		{
			if(!wellDisplay.isEnabled())
				continue;
			wellDisplay.setSelected(true);
			lastSelectedWell = wellDisplay.getWell();
			return;
		}
		
	}
	/**
	 * Enables or disables the well with the given well identifier. Returns true if successful. Returns false if well does not exist.
	 * @param well the identifier of the well to enable or disable.
	 * @param enabled true if well should be enabled, false if disabled.
	 * @return True if well could be enabled, false if well does not exist.
	 */
	public boolean setEnabled(Well well, boolean enabled)
	{
		WellDisplay wellDisplay = wellDisplays.get(well);
		if(wellDisplay == null)
			return false;
		else if(wellDisplay.setEnabled(enabled))
		{
			microplateDisplay.repaint();
		}
		if(!enabled && lastSelectedWell != null && lastSelectedWell.equals(well))
		{
			lastSelectedWell = null;
			if(selectionMode == SelectionMode.EXACTLY_ONE)
				selectFirstWell();
		}
		return true;
	}
	
	/**
	 * Enables all wells with the well identifiers contained in the given collection, and disables all others.
	 * @param wells Wells which should be enabled.
	 */
	public void setEnabledWells(Collection<Well> wells)
	{
		HashSet<Well> inverseSet = new HashSet<>(wellDisplays.keySet());
		inverseSet.removeAll(wells);
		for(Well inversePosition : inverseSet)
		{
			wellDisplays.get(inversePosition).setEnabled(false);
			if(lastSelectedWell != null && lastSelectedWell.equals(inversePosition))
				lastSelectedWell = null;
		}
		for(Well position : wells)
		{
			wellDisplays.get(position).setEnabled(true);
		}
		if(lastSelectedWell == null && selectionMode == SelectionMode.EXACTLY_ONE)
			selectFirstWell();
	}
	/**
	 * Selects all wells with the well identifiers contained in the given collection, and de-selects all others.
	 * @param wells Wells which should be selected. If a well is not defined in the current layout, the well is ignored.
	 */
	public void setSelectedWells(Collection<Well> wells)
	{
		if(selectionMode == SelectionMode.NONE || wells == null)
			return;
		HashSet<Well> inverseSet = new HashSet<>(wellDisplays.keySet());
		inverseSet.removeAll(wells);
		for(Well inversePosition : inverseSet)
		{
			wellDisplays.get(inversePosition).setSelected(false);
		}
		lastSelectedWell = null;
		for(Well position : wells)
		{
			WellDisplay wellDisplay = wellDisplays.get(position);
			if(wellDisplay == null)
				continue;
			wellDisplay.setSelected(true);
			if((selectionMode == SelectionMode.EXACTLY_ONE || selectionMode == SelectionMode.SINGLE) && lastSelectedWell != null && !lastSelectedWell.equals(position))
			{
				wellDisplays.get(lastSelectedWell).setSelected(false);
			}
			lastSelectedWell = position;
		}
		if(lastSelectedWell == null && selectionMode == SelectionMode.EXACTLY_ONE)
			selectFirstWell();
		microplateDisplay.repaint();
	}
	
	/**
	 * Sets the design of the microplate whose wells can be selected by the user.
	 * @param microplateConfiguration Design of the microplate.
	 * @throws AddonException Thrown if microplate layout could not be loaded.
	 */
	public void setMicroplateLayout(final MicroplateConfiguration microplateConfiguration) throws AddonException
	{
		if(microplateConfiguration == null)
		{
			setMicroplateLayout((MicroplateLayout)null);
			return;
		}
		try
		{
			MicroplateResource microplateResource = getServer().getComponentProvider(null).createComponent(new PositionInformation(), microplateConfiguration, MicroplateResource.class);
			SimpleMeasurementContext measurementContext = new SimpleMeasurementContext();
			microplateResource.initialize(measurementContext);
			setMicroplateLayout(microplateResource.getMicroplateLayout());
			microplateResource.uninitialize(measurementContext);
		} catch (Exception e) 
		{
			setMicroplateLayout((MicroplateLayout)null);
			throw new AddonException("Could not get information on microscope layout.", e);
		}		
	}
	/**
	 * Sets the design of the microplate whose wells can be selected by the user.
	 * @param microplateLayout The microplate layout.
	 */
	public void setMicroplateLayout(final MicroplateLayout microplateLayout)
	{
		lastSelectedWell = null;
		wellDisplays.clear();
		minWellX= Double.MAX_VALUE;
		maxWellX = -Double.MAX_VALUE;
		minWellY = Double.MAX_VALUE;
		maxWellY = -Double.MAX_VALUE;
		xWellLabelPositions = new HashMap<>();
		yWellLabelPositions = new HashMap<>();
		if(microplateLayout != null)
		{
			for(WellLayout wellLayout : microplateLayout)
			{
				if(wellLayout == null)
					continue;
				WellDisplay wellDisplay = new WellDisplay(wellLayout);
				double x = wellDisplay.getX();
				double y = wellDisplay.getY();
				double width =  wellDisplay.getWidth();
				double height = wellDisplay.getHeight();
				wellDisplays.put(wellLayout.getWell(), wellDisplay);
				if(x < minWellX)
					minWellX = x;
				if(x+width > maxWellX)
					maxWellX = x+width;
				if(y < minWellY)
					minWellY = y;
				if(y+height > maxWellY)
					maxWellY = y+height;
				if(xWellLabelPositions != null)
				{
					int xpos = wellLayout.getWell().getWellX();
					Point2D.Double oldPoint = xWellLabelPositions.get(xpos);
					if(oldPoint == null)
						xWellLabelPositions.put(xpos, new Point2D.Double(x, width));
					else
					{
						if(x != oldPoint.getX() || width != oldPoint.getY())
							xWellLabelPositions = null;
					}
				}
				if(yWellLabelPositions != null)
				{
					int ypos = wellLayout.getWell().getWellY();
					Point2D.Double oldPoint = yWellLabelPositions.get(ypos);
					if(oldPoint == null)
						yWellLabelPositions.put(ypos, new Point2D.Double(y, height));
					else
					{
						if(y != oldPoint.getX() || height != oldPoint.getY())
							yWellLabelPositions = null;
					}
				}
			}
		}
		if(lastSelectedWell == null && selectionMode == SelectionMode.EXACTLY_ONE)
			selectFirstWell();
		microplateDisplay.repaint();
	}
	
	@Override
	protected Component createUI() throws AddonException 
	{
		setMaximizable(true);
		setResizable(true);
		setTitle(TITLE);
		//setPreferredSize(new Dimension(400,250));
		return microplateDisplay;
	}
	
	private class WellDisplay extends Rectangle2D.Double
	{
		/**
		 * Serial Version UID.
		 */
		private static final long serialVersionUID = -1940982800744370076L;
		boolean enabled = true;
		boolean selected = false;
		boolean temporarySelected = false;
		private final Color BACKGROUND_DISABLED = Color.DARK_GRAY;
		private final Color BACKGROUND_UNSELECTED = Color.LIGHT_GRAY;
		private final Color BACKGROUND_SELECTED = Color.GREEN;
		private final WellLayout wellLayout;
		WellDisplay(WellLayout wellLayout)
		{
			super(wellLayout.getX(),wellLayout.getY(),wellLayout.getWidth(),wellLayout.getHeight());
			this.wellLayout = wellLayout;
		}

		public void paint(Graphics2D g)
	    {
			if(!enabled)
				g.setColor(BACKGROUND_DISABLED);
			else if(selected != temporarySelected)
				g.setColor(BACKGROUND_SELECTED);
			else
				g.setColor(BACKGROUND_UNSELECTED);
			g.fill(this);
			g.setColor(Color.BLACK);
			g.draw(this);
	    }
		WellLayout getWellLayout()
		{
			return wellLayout;
		}
		Well getWell()
		{
			return wellLayout.getWell();
		}
		boolean isSelected()
		{
			return selected;
		}
		boolean isEnabled()
		{
			return enabled;
		}
		boolean setSelected(boolean selected)
		{
			if(!enabled || this.selected == selected)
				return false;
			this.selected = selected;
			return true;
		}
		boolean setEnabled(boolean enabled)
		{
			if(this.enabled == enabled && !selected && !temporarySelected)
				return false;
			this.enabled = enabled;
			this.selected = this.selected && enabled;
			this.temporarySelected = this.temporarySelected && enabled;
			return true;
		}
		/**
		 * Checks if the clicked position is inside the well, and selects the well if this is true. Returns true if selection state of the well changed, and false otherwise.
		 * @param pos Mouse click position.
		 * @return True if selection state of the well changed.
		 */
		boolean clicked(Point2D pos)
		{
			if(enabled && contains(pos))
			{
				selected = !selected;
				return true;
			}
			return false;
		}
		
		boolean isIn(Point2D pos)
		{
			return contains(pos);
		}
		
		String getToolTipText()
		{
			return getWell().toString();
		}
		
		void revertTemporary()
		{
			temporarySelected = false;
		}
		/**
		 * If the well was temporarily selected/unselected, this change is permanently accepted.
		 * @return True if selection state of the well changed.
		 */
		boolean acceptTemporary()
		{
			if(!temporarySelected)
				return false;
			selected = temporarySelected != selected;
			temporarySelected = false;
			return true;
		}

		public void temporaryMark(Rectangle2D selection) 
		{
			if(enabled)
				temporarySelected = this.intersects(selection);
		}
	}
	
	private class MicroplateDisplay extends JComponent
	{
		/**
		 *	Serial version UID. 
		 */
		private static final long serialVersionUID = -7184344164935348666L;
		
		private final double ZOOM_IN_STEP = Math.sqrt(2);
		private final double ZOOM_OUT_STEP = 1/Math.sqrt(2);
		private final double ZOOM_MAX = 100;
		private double zoom = 1;
		private double centerX = 0.5;
		private double centerY = 0.5;
		private final String NO_LAYOUT_LOADED_MESSAGE = "No microplate layout loaded.";
		private static final int REFERENCE_WIDTH = 400;
		private static final int REFERENCE_HEIGHT = 267;
		
		private MicroplateDisplay()
		{
			UserListener userListener = new UserListener();
			addMouseListener(userListener);
			addMouseMotionListener(userListener);
			addMouseWheelListener(userListener);
			
			setFocusable(true);
		}
		
		@Override
		public Dimension getPreferredSize()
		{
			if(minWellX == Double.MAX_VALUE || maxWellX == -Double.MAX_VALUE || minWellY == Double.MAX_VALUE || maxWellY == -Double.MAX_VALUE)
			{
				return new Dimension(REFERENCE_WIDTH, REFERENCE_HEIGHT);
			}
			FontMetrics fontMetrics = getFontMetrics(WELL_LABEL_FONT);
			double fontHeight = xWellLabelPositions == null || !showXWellLabels ? 0 : 2*WELL_LABEL_FONT_MARGIN + fontMetrics.getAscent();
			double fontWidth = yWellLabelPositions == null || !showYWellLabels ? 0 : 2*WELL_LABEL_FONT_MARGIN+fontMetrics.stringWidth("WW");
			double plateWidth = maxWellX-minWellX;
			double plateHeight = maxWellY-minWellY;
			if((REFERENCE_WIDTH-fontWidth)/plateWidth == (REFERENCE_HEIGHT-fontHeight) / plateHeight)
				return new Dimension(REFERENCE_WIDTH, REFERENCE_HEIGHT);
			else if((REFERENCE_WIDTH-fontWidth)/plateWidth < (REFERENCE_HEIGHT-fontHeight) / plateHeight)
				return new Dimension(REFERENCE_WIDTH, (int) Math.ceil((REFERENCE_WIDTH-fontWidth)/plateWidth*plateHeight + fontHeight));
			else
				return new Dimension((int) Math.ceil((REFERENCE_HEIGHT-fontHeight)/plateHeight*plateWidth+fontWidth), REFERENCE_HEIGHT);
		}
		
		
		@Override
	    public void paintComponent(Graphics grp)
	    {
			Graphics2D g = (Graphics2D)grp;
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
			
			if(wellDisplays.isEmpty())
			{
				int width = g.getFontMetrics().stringWidth(NO_LAYOUT_LOADED_MESSAGE);
				int height = g.getFontMetrics().getHeight();
				g.drawString(NO_LAYOUT_LOADED_MESSAGE, (getWidth()-width)/2, (getHeight()-height)/2);
			}
			else
			{
				AffineTransform oldTransform = g.getTransform();
				// create transform
				AffineTransform transform = getTransform();
				g.transform(transform);
				for(WellDisplay well : wellDisplays.values())
				{
					well.paint(g);
				}
				g.setColor(textColor);
				g.setFont(WELL_LABEL_FONT.deriveFont((float)(WELL_LABEL_FONT.getSize() / transform.getScaleY())));
				if(showXWellLabels && xWellLabelPositions != null)
				{
					for(Entry<Integer, Point2D.Double> entry : xWellLabelPositions.entrySet())
					{
						String message = Integer.toString(entry.getKey()+1);
						g.drawString(message, (float)entry.getValue().x + ((float)entry.getValue().y-g.getFontMetrics().stringWidth(message))/2, (float)(minWellY-WELL_LABEL_FONT_MARGIN/ transform.getScaleY()));
					}
				}
				if(showYWellLabels && yWellLabelPositions != null)
				{
					for(Entry<Integer, Point2D.Double> entry : yWellLabelPositions.entrySet())
					{
						String message = Well.getYWellName(entry.getKey());
						g.drawString(message, (float)(minWellX-g.getFontMetrics().stringWidth(message)-WELL_LABEL_FONT_MARGIN/ transform.getScaleY()), (float)entry.getValue().x +g.getFontMetrics().getAscent()+ ((float)entry.getValue().y-g.getFontMetrics().getAscent())/2);
					}
				}
				g.setTransform(oldTransform);
			}
	    }
		
		AffineTransform getTransform()
		{
			if(minWellX == Double.MAX_VALUE || maxWellX == -Double.MAX_VALUE || minWellY == Double.MAX_VALUE || maxWellY == -Double.MAX_VALUE)
			{
				return new AffineTransform();
			}
			FontMetrics fontMetrics = getFontMetrics(WELL_LABEL_FONT);
			double fontHeight = xWellLabelPositions == null || !showXWellLabels ? 0 : 2*WELL_LABEL_FONT_MARGIN + fontMetrics.getAscent();
			double fontWidth = yWellLabelPositions == null || !showYWellLabels ? 0 : 2*WELL_LABEL_FONT_MARGIN+fontMetrics.stringWidth("WW");
			double width = getWidth()-1-fontWidth;
			double height = getHeight()-1-fontHeight;
			double defaultScale = Math.min((width)/(maxWellX-minWellX), (height)/(maxWellY-minWellY));
			
			double imageWidth = defaultScale*(maxWellX-minWellX);
			double imageHeight = defaultScale*(maxWellY-minWellY);
			double scaledImageWidth = imageWidth * zoom;
			double scaledImageHeight = imageHeight * zoom;
			
			AffineTransform transform;
			double deltaX;
			if(scaledImageWidth <= width)
			{
				if(center)
					deltaX = (width-scaledImageWidth) / 2;
				else
					deltaX = 0;
			}
			else
			{
				deltaX = width/2 - centerX * scaledImageWidth;
				if(deltaX > 0)
					deltaX = 0;
				else if(deltaX < width-scaledImageWidth)
					deltaX = width-scaledImageWidth;
			}
			
			double deltaY;
			if(scaledImageHeight <= height)
			{
				if(center)
					deltaY = (height-scaledImageHeight) / 2;
				else
					deltaY = 0;
			}
			else
			{
				deltaY = height/2 - centerY * scaledImageHeight;
				if(deltaY > 0)
					deltaY = 0;
				else if(deltaY < height-scaledImageHeight)
					deltaY = height-scaledImageHeight;
			}
			transform = AffineTransform.getTranslateInstance(deltaX+fontWidth, deltaY+fontHeight);
			transform.concatenate(AffineTransform.getScaleInstance(defaultScale*zoom, defaultScale*zoom));
			transform.concatenate(AffineTransform.getTranslateInstance(-minWellX, -minWellY));
			return transform;
		}
		
		private class UserListener implements MouseListener, MouseMotionListener, MouseWheelListener
		{
			private Point2D lastDown = null;
			
			Point2D getMousePos(MouseEvent e)
			{
				if(e.getX() < 0 || e.getX() >= getWidth() || e.getY() < 0 || e.getY()>getHeight())
					return null;
				AffineTransform imageTransform = getTransform();
				try {
					return imageTransform.createInverse().transform(new Point2D.Double(e.getX(), e.getY()), null);
				} catch (@SuppressWarnings("unused") NoninvertibleTransformException e1) {
					// will not happen.
					return null;
				}
			}
			@Override
			public void mouseDragged(MouseEvent mouseEvent) 
			{
				if(selectionMode == SelectionMode.MULTIPLE && isLeftMouse(mouseEvent))
					temporaryMark(lastDown, getMousePos(mouseEvent));
			}
			private void temporaryMark(Point2D mouseDown, Point2D mouseCurrent)
			{
				if(mouseDown == null)
					return;
				else if(mouseCurrent == null)
				{
					for(WellDisplay well : wellDisplays.values())
					{
						well.revertTemporary();
					}
					repaint();
					return;
				}
				Rectangle2D.Double selection = new Rectangle2D.Double(Math.min(mouseDown.getX(), mouseCurrent.getX()), Math.min(mouseDown.getY(), mouseCurrent.getY()), Math.abs(mouseCurrent.getX()-mouseDown.getX()), Math.abs(mouseCurrent.getY()-mouseDown.getY())); 
				for(WellDisplay well : wellDisplays.values())
				{
					well.temporaryMark(selection);
				}
				
				repaint();
			}

			private boolean isLeftMouse(MouseEvent e)
			{
				return SwingUtilities.isLeftMouseButton(e) && !e.isControlDown();
			}
			
			@Override
			public void mouseMoved(MouseEvent mouseEvent) 
			{
				Point2D pos = getMousePos(mouseEvent);
				for(WellDisplay well : wellDisplays.values())
				{
					if(well.isIn(pos))
					{
						setToolTipText("<html>"+well.getToolTipText());
						return;
					}
				}
				setToolTipText(null);
			}
			

			@Override
			public void mouseClicked(MouseEvent mouseEvent) 
			{
				if(selectionMode == SelectionMode.NONE || !isLeftMouse(mouseEvent))
					return;
				Point2D pos = getMousePos(mouseEvent);
				if(pos==null)
					return;
				
				for(WellDisplay wellDisplay : wellDisplays.values())
				{
					if(wellDisplay.clicked(pos))
					{
						if(wellDisplay.isSelected())
						{
							if((selectionMode == SelectionMode.SINGLE || selectionMode == SelectionMode.EXACTLY_ONE) && lastSelectedWell != null && !lastSelectedWell.equals(wellDisplay.getWell()))
								wellDisplays.get(lastSelectedWell).setSelected(false);
							lastSelectedWell = wellDisplay.getWell();
						}
						else
						{
							if(lastSelectedWell != null && lastSelectedWell.equals(wellDisplay.getWell()))
							{
								if(selectionMode == SelectionMode.EXACTLY_ONE)
								{
									// don't allow this change
									wellDisplay.setSelected(true);
									return;
								}
								lastSelectedWell = null;
								
							}
						}
						repaint();
						notifySelectedWellsChanged();
						break;
					}
				}
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent) 
			{
				// do nothing.
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent) 
			{
				if(selectionMode != SelectionMode.MULTIPLE)
					return;
				for(WellDisplay well : wellDisplays.values())
				{
					well.revertTemporary();
				}
				repaint();
			}

			@Override
			public void mousePressed(MouseEvent mouseEvent) {
				if(isLeftMouse(mouseEvent))
					lastDown = getMousePos(mouseEvent);
			}

			@Override
			public void mouseReleased(MouseEvent mouseEvent) 
			{
				lastDown = null;
				if(selectionMode != SelectionMode.MULTIPLE)
					return;
				boolean changed = false;
				for(WellDisplay wellDisplay : wellDisplays.values())
				{
					if(wellDisplay.acceptTemporary())
					{
						changed = true;
						if(wellDisplay.isSelected())
							lastSelectedWell = wellDisplay.getWell();
						else if(lastSelectedWell != null && lastSelectedWell.equals(wellDisplay.getWell()))
							lastSelectedWell = null;
					}
				}
				
				if(changed)
				{
					repaint();
					notifySelectedWellsChanged();
				}
			}
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) 
			{
				int direction = e.getWheelRotation();
				if(direction == 0)
					return;
				boolean zoomIn = direction < 0;
				
				zoom(e, zoomIn);
			}
		}
		
		private void zoom(MouseEvent e, boolean zoomIn)
		{
			if(e == null)
				return;
			if(minWellX == Double.MAX_VALUE || maxWellX == -Double.MAX_VALUE || minWellY == Double.MAX_VALUE || maxWellY == -Double.MAX_VALUE)
			{
				return;
			}
			if(zoomIn)
			{
				AffineTransform imageTransform = getTransform();
				Point2D pos;
				try {
					pos = imageTransform.createInverse().transform(new Point(e.getX(), e.getY()), null);
				} catch (@SuppressWarnings("unused") NoninvertibleTransformException e1) {
					// will not happen.
					return;
				}
				centerX = pos.getX() / (maxWellX-minWellX);
				if(centerX < 0)
					centerX = 0;
				else if(centerX > 1)
					centerX = 1;
				centerY = pos.getY() / (maxWellY-minWellY);
				if(centerY < 0)
					centerY = 0;
				else if(centerY > 1)
					centerY = 1;
				
				zoom*=ZOOM_IN_STEP;
				if(zoom > ZOOM_MAX)
					zoom = ZOOM_MAX;
			}
			else
			{
				zoom*=ZOOM_OUT_STEP;
				if(zoom < 1)
					zoom = 1;
			}
			repaint();
		}
	}
	/**
	 * Adds a listener which gets notified if the selected wells changed by user interaction.
	 * @param listener The listener to be added.
	 */
	public void addWellsChangeListener(ActionListener listener)
	{
		wellChangeListeners.add(listener);
	}
	
	/**
	 * Removes a previously added listener.
	 * @param listener The listener to be removed.
	 */
	public void removeWellsChangeListener(ActionListener listener)
	{
		wellChangeListeners.remove(listener);
	}
	
	private void notifySelectedWellsChanged()
	{
		for(ActionListener listener : wellChangeListeners)
		{
			listener.actionPerformed(new ActionEvent(this, 1234, "wellsChanged"));
		}
	}
}
